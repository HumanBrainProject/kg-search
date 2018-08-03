
/*
*   Copyright (c) 2018, EPFL/Human Brain Project PCO
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

package editor.controllers

import akka.util.ByteString
import common.helpers.BlazegraphHelper
import common.helpers.ResponseHelper._
import editor.helper.InstanceHelper._
import common.models.NexusPath
import editor.actions.EditorUserAction
import editor.helper.InstanceHelper
import editor.helpers.{EditorSpaceHelper, FormHelper, NavigationHelper, NodeTypeHelper}
import editor.models.{EditorUserRequest, InMemoryKnowledge, IncomingLinksInstances, Instance}
import authentication.helpers.OIDCHelper
import helpers.ReconciledInstanceHelper
import javax.inject.{Inject, Singleton}
import authentication.models.{AuthenticatedUserAction, UserRequest}
import authentication.service.OIDCAuthService
import editor.actions.EditorUserAction
import play.api.{Configuration, Logger}
import play.api.http.HttpEntity
import play.api.http.Status.OK
import play.api.libs.json
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc._
import services.{InstanceService, NexusService}

import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class NexusEditorController @Inject()(
                                       cc: ControllerComponents,
                                       authenticatedUserAction: AuthenticatedUserAction,
                                       instanceService: InstanceService,
                                       oIDCAuthService: OIDCAuthService,
                                       config: Configuration,
                                       nexusService: NexusService,
                                       ws: WSClient
                                     )(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  val blazegraphNameSpace: String = config.getOptional[String]("blazegraph.namespace").getOrElse("kg")
  val nexusEndpoint: String = config.getOptional[String]("nexus.endpoint").getOrElse("https://nexus-dev.humanbrainproject.org")
  val reconciledPrefix: String = config.getOptional[String]("nexus.reconciled.prefix").getOrElse("reconciled")
  val editorPrefix: String = config.getOptional[String]("nexus.editor.prefix").getOrElse("editor")
  val sparqlEndpoint: String = config.getOptional[String]("blazegraph.endpoint").getOrElse("http://localhost:9999")

  val logger = Logger(this.getClass)

  // TODO Check for authentication and groups as for now everybody could see the whole graph
  def listInstances(org: String, domain:String, datatype: String, version:String): Action[AnyContent] = authenticatedUserAction.async  { implicit request =>
    logger.debug(s"User info ${request.user}")
    val nexusPath = NexusPath(org, domain, datatype, version)
    val sparqlPayload =
      s"""
         |SELECT DISTINCT ?instance ?name  ?description
         |WHERE {
         |  {
         |    ?instance <http://schema.org/name> ?name .
         |	?instance a <$nexusEndpoint/vocabs/nexus/core/terms/v0.1.0/Instance> .
         |  ?instance <$nexusEndpoint/vocabs/nexus/core/terms/v0.1.0/deprecated> false .
         |  ?instance <$nexusEndpoint/vocabs/nexus/core/terms/v0.1.0/schema> <$nexusEndpoint/v0/schemas/${nexusPath.toString()}> .
         |      OPTIONAL{ ?instance <http://schema.org/description> ?description .}
         |    FILTER(!EXISTS{?i <http://hbp.eu/reconciled#original_parent> ?instance}) .
         |    }
         |   UNION {
         |	   ?instance <http://schema.org/name> ?name .
         |    OPTIONAL{ ?instance <http://schema.org/description> ?description .}
         |  	?instance a <$nexusEndpoint/vocabs/nexus/core/terms/v0.1.0/Instance> .
         |  ?instance <$nexusEndpoint/vocabs/nexus/core/terms/v0.1.0/deprecated> false .
         |  ?instance <$nexusEndpoint/vocabs/nexus/core/terms/v0.1.0/schema> <$nexusEndpoint/v0/schemas/${nexusPath.org}reconciled/${nexusPath.domain}/${nexusPath.schema}/${nexusPath.version}> .
         |  ?original   <$nexusEndpoint/vocabs/nexus/core/terms/v0.1.0/schema> ?originalSchema .
         |     ?instance <http://hbp.eu/reconciled#original_parent> ?original .
         |     FILTER( ?originalSchema IN(<$nexusEndpoint/v0/schemas/${nexusPath.toString()}>, <$nexusEndpoint/v0/schemas/${nexusPath.org}editor/${nexusPath.domain}/${nexusPath.schema}/${nexusPath.version}>) )
         |  }
         |}
         |
      """.stripMargin

    val start = System.currentTimeMillis()

    ws.url(s"$sparqlEndpoint/bigdata/namespace/$blazegraphNameSpace/sparql")
      .withQueryStringParameters("query" -> sparqlPayload, "format" -> "json").get().map[Result] {
      res =>
        res.status match {
          case 200 =>
            val arr = BlazegraphHelper.extractResult(res.json)
            val duration = System.currentTimeMillis() - start
            logger.debug(s"sparql query: \n$sparqlPayload\n\nduration: ${duration}ms")
            Ok(Json.obj(
              "data" -> InstanceHelper.formatInstanceList(arr, reconciledPrefix),
              "label" -> JsString((FormHelper.formRegistry \ nexusPath.org \ nexusPath.domain \ nexusPath.schema \ nexusPath.version \ "label").asOpt[String].getOrElse(nexusPath.toString())))
            )
          case _ =>
            Result(
              ResponseHeader(
                res.status,
                flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](res.headers))
              ),
              HttpEntity.Strict(res.bodyAsBytes, getContentType(res.headers))
            )
        }
    }
  }

  /**
    * Return a instance by its nexus ID
    * Stating by checking if this instance is coming from a reconciled space.
    * Otherwise we try to return the instance from the original organization
    * All responses are sent with a json object "back_link" with the path of the schema of the instance
    * if the schema exists returns e.g myorg/mydomain/myschema otherwise returns an empty string
    *
    * @param org The org of the instance
    * @param domain The domain of the instance
    * @param schema The schema of the instance
    * @param version The version of the schema
    * @param id The id of the instance
    * @return An error message with a back_link or a form configuration populated with the instance information
    */

  def getInstance(org: String, domain:String, schema: String, version:String, id: String): Action[AnyContent] = Action.async { implicit request =>
    val token = request.headers.get("Authorization").getOrElse("")
    val nexusPath = NexusPath(org, domain, schema, version)
    //Check if instance is from Reconciled space
    val reconciledPath = NexusPath(NavigationHelper.addSuffixToOrg(org, reconciledPrefix), domain,schema, version)
    instanceService.retrieveInstance(reconciledPath, id, token).flatMap[Result] {
      case Left(_) => // Check in the original space
        instanceService.retrieveInstance(nexusPath, id, token).map[Result]{
          case Left(r) =>
            logger.error(s"Error: Could not fetch instance : ${nexusPath.toString()}/$id - ${r.body}")
            val resultBackLink = NavigationHelper.errorMessageWithBackLink(r.body, NavigationHelper.generateBackLink(nexusPath,  reconciledPrefix))
            Result(ResponseHeader(r.status, flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](r.headers))),
            HttpEntity.Strict(ByteString(resultBackLink.toString()), Some("application/json")))
          case Right(originalInstance) =>
            val instanceForm = FormHelper.getFormStructure(nexusPath, originalInstance.content)
            Ok(NavigationHelper.resultWithBackLink(instanceForm.as[JsObject], nexusPath, reconciledPrefix))
        }
      case Right(instance) =>
        val id = (instance.content \ "@id").as[String]
        val correctedId = s"$nexusEndpoint/v0/data/${Instance.getIdForEditor(id, reconciledPrefix)}"
        val jsonTransformer = (__ ).json.update(
          __.read[JsObject].map{ o => o ++ Json.obj("@id" -> correctedId)}
        )
        instance.content.transform(jsonTransformer) match {
          case JsSuccess(data, path) =>
            val instanceForm = FormHelper.getFormStructure(nexusPath, data)
            Future.successful(
              Ok(NavigationHelper.resultWithBackLink(instanceForm.as[JsObject], nexusPath, reconciledPrefix))
            )
          case _ => Future.successful(InternalServerError(NavigationHelper.errorMessageWithBackLink("Could not fetch the requested data", NavigationHelper.generateBackLink(nexusPath,  reconciledPrefix))))
        }

    }
  }

  def getInstanceNumberOfAvailableRevisions(org: String, domain:String, datatype: String, version:String, id: String): Action[AnyContent] = Action.async { implicit request =>
    val token = request.headers.get("Authorization").getOrElse("")
    val nexusPath = NexusPath(org, domain, datatype, version)
    instanceService.retrieveInstance(nexusPath, id, token).flatMap[Result] {
      case Left(res) => Future.successful(Result(ResponseHeader(res.status, flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](res.headers))),
        HttpEntity.Strict(res.bodyAsBytes, getContentType(res.headers))))
      case Right(originalInstance) =>
        val nbRevision = (originalInstance.content \ "nxv:rev").as[JsNumber]
        Future.successful(Ok(
          Json.obj("available_revisions" -> nbRevision,
                    "path" -> id))
        )
    }
  }

  def getSpecificReconciledInstance(
                                     org: String,
                                     domain: String,
                                     datatype: String,
                                     version: String,
                                     id: String,
                                     revision: Int
                                   ): Action[AnyContent] = Action.async { implicit request =>
    val token = request.headers.get("Authorization").getOrElse("")
    val nexusPath = NexusPath(org, domain, datatype, version)
    ws
      .url(s"https://$nexusEndpoint/v0/data/${nexusPath.toString()}/$id?rev=$revision&deprecated=false&fields=all")
      .withHttpHeaders("Authorization" -> token).get().map {
      response =>
        response.status match {
          case OK =>
            val json = response.json
            val nexusId = Instance.getIdForEditor((json \ "http://hbp.eu/reconciled#original_parent" \ "@id").as[String],  reconciledPrefix)
            val datatype = nexusId.splitAt(nexusId.lastIndexOf("/"))
            val originalDatatype = NexusPath(datatype._1.split("/").toList)
            FormHelper.getFormStructure(originalDatatype, response.json) match {
              case JsNull => NotImplemented(NavigationHelper.errorMessageWithBackLink("Form not implemented", NavigationHelper.generateBackLink(nexusPath, reconciledPrefix) ))
              case json => Ok(NavigationHelper.resultWithBackLink(json.as[JsObject], nexusPath,reconciledPrefix))
            }
          case _ =>
            val resultBackLink = NavigationHelper.errorMessageWithBackLink(response.body, NavigationHelper.generateBackLink(nexusPath,reconciledPrefix))
            Result(
              ResponseHeader(
                response.status,
                flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](response.headers))
              ),
              HttpEntity.Strict(ByteString(resultBackLink.toString()), Some("application/json"))
            )
        }

    }
  }

  private def initialUpdate(originalInstance: Instance)(implicit request: EditorUserRequest[AnyContent]) = {
    val token = OIDCHelper.getTokenFromRequest(request)
    val reconciledTokenFut = oIDCAuthService.getTechAccessToken()
    val instancePath = originalInstance.nexusPath
    val newValue = request.body.asJson.get
    val editorSpace = EditorSpaceHelper.getGroupName(request.editorGroup, editorPrefix)
    val reconciledSpace = EditorSpaceHelper.getGroupName(request.editorGroup, reconciledPrefix)
    reconciledTokenFut.flatMap { reconciledToken =>
      val originLink = (originalInstance.content \ "@id").as[JsValue]
      // As we cannot pass / in the name of a field we have replaced them with %nexus-slash%
      val updateFromUI = Json.parse(FormHelper.unescapeSlash(newValue.toString())).as[JsObject] - "id"
      val updatedInstance = buildInstanceFromForm(originalInstance.content, updateFromUI, nexusEndpoint)
      val updateToBeStoredInManual = buildDiffEntity(originalInstance, updatedInstance.toString, originalInstance.content) +
        ("@type", JsString(s"http://hbp.eu/${originalInstance.nexusPath.org}#${originalInstance.nexusPath.schema.capitalize}"))
      val consolidatedInstance = buildInstanceFromForm(originalInstance.content, updateToBeStoredInManual, nexusEndpoint)
      logger.debug(s"Consolidated instance $consolidatedInstance")
      val preppedEntityForStorage = InstanceHelper.prepareManualEntityForStorage(updateToBeStoredInManual, request.user) +
        ("http://hbp.eu/manual#origin", originLink) +
        ("http://hbp.eu/manual#parent", Json.obj("@id" -> originLink))

      val createEditorDomain = nexusService.createDomain(nexusEndpoint, editorSpace, originalInstance.nexusPath.domain, "", token)
      val createReconciledDomain = nexusService.createDomain(nexusEndpoint, reconciledSpace, originalInstance.nexusPath.domain, "", reconciledToken)

      val manualRes: Future[WSResponse] = createEditorDomain.flatMap { re =>
        val manualSchema = instanceService.createManualSchemaIfNeeded(updateToBeStoredInManual, originalInstance.nexusPath, token, editorSpace, "manual", EditorSpaceHelper.nexusEditorContext("manual"))
        manualSchema.flatMap { res =>
          instanceService.upsertUpdateInManualSpace(editorSpace, None,  request.user, originalInstance.nexusPath, preppedEntityForStorage, token)
        }
      }

      manualRes.flatMap { res =>
        logger.debug(s"Creation of manual update ${res.body}")
        res.status match {
          case status if status < 300 =>
            val newManualUpdateId = (res.json \"@id").as[String]
            val reconciledRes: Future[WSResponse] = createReconciledDomain.flatMap { re =>
              val reconciledSchema = instanceService.createManualSchemaIfNeeded(updatedInstance, originalInstance.nexusPath, reconciledToken, reconciledSpace, "reconciled", EditorSpaceHelper.nexusEditorContext("reconciled"))
              reconciledSchema.flatMap { res =>
                instanceService.insertReconciledInstance(reconciledSpace, editorSpace, originalInstance, preppedEntityForStorage, newManualUpdateId, consolidatedInstance, reconciledToken, request.user)
              }
            }
            reconciledRes.map { re =>
              logger.debug(s"Creation of reconciled update ${re.body}")
              re.status match {
                case recStatus if recStatus < 300 =>
                   val id = (re.json \ "@id").as[JsString]
                  Ok(NavigationHelper.resultWithBackLink(InstanceHelper.formatFromNexusToOption(updatedInstance, reconciledPrefix), instancePath, reconciledPrefix))
                case _ =>
                  val resultBackLink = NavigationHelper.errorMessageWithBackLink(re.body, NavigationHelper.generateBackLink(instancePath, reconciledPrefix))
                  Result(
                    ResponseHeader(
                      re.status,
                      flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](re.headers))
                    ),
                    HttpEntity.Strict(ByteString(resultBackLink.toString()), Some("application/json"))
                  )
              }
            }
          case _ =>
            val resultBackLink = NavigationHelper.errorMessageWithBackLink(res.body, NavigationHelper.generateBackLink(instancePath, reconciledPrefix))
            Future.successful(Result(
              ResponseHeader(
                res.status,
                flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](res.headers))
              ),
              HttpEntity.Strict(ByteString(resultBackLink.toString()), Some("application/json"))
            ))
        }

      }
    }
  }

  private def updateWithReconciled(currentInstanceDisplayed: Instance, originalPath: NexusPath)(implicit request: EditorUserRequest[AnyContent]) = {
    val token = OIDCHelper.getTokenFromRequest(request)
    val reconciledTokenFut = oIDCAuthService.getTechAccessToken()
    val newValue = request.body.asJson.get
    val editorSpace = EditorSpaceHelper.getGroupName(request.editorGroup, editorPrefix)
    val originalIdAndPath = Instance.extractIdAndPath((currentInstanceDisplayed.content \ "http://hbp.eu/reconciled#original_parent").as[JsValue])
    // Get the original instance either in the Editor space or the original space
    instanceService.retrieveInstance(originalIdAndPath._2, originalIdAndPath._1, token).flatMap {
      case Right(instanceFromOriginalSpace) =>
        val originalInstance = Instance(removeNexusFields(instanceFromOriginalSpace.content))
        // Get the editor instances
        val editorInstanceIds = (currentInstanceDisplayed.content \ "http://hbp.eu/reconciled#parents").as[List[JsObject]]
          .map(js => Instance.extractIdAndPath(js)._1)
        reconciledTokenFut.flatMap { reconciledToken =>
          instanceService.retrieveInstances(editorInstanceIds, NexusPath(NavigationHelper.addSuffixToOrg(originalPath.org, editorPrefix), originalPath.domain, originalPath.schema, originalPath.version), token).flatMap[Result] {
            editorInstancesRes =>
              if (editorInstancesRes.forall(_.isRight)) {
                val editorInstances = editorInstancesRes.map { e => e.toOption.get}
                val reconciledInstanceCleaned = removeNexusFields(currentInstanceDisplayed.content)
                //Create the manual update
                // As we cannot pass / in the name of a field we have replaced them with %nexus-slash%
                val updateFromUI = Json.parse(FormHelper.unescapeSlash(newValue.toString())).as[JsObject] - "id"
                val updatedInstance = buildInstanceFromForm(reconciledInstanceCleaned, updateFromUI, nexusEndpoint)
                logger.debug(s"Consolidated instance $updatedInstance")
                //Generate the data that should be stored in the manual space
                val diffEntity = buildDiffEntity(currentInstanceDisplayed, updatedInstance.toString, originalInstance.content) + ("@type", JsString(s"http://hbp.eu/${originalPath.org}#${originalPath.schema.capitalize}"))
                val reconciledLink =  (currentInstanceDisplayed.content \ "@id").as[JsString]
                val updateToBeStoredInManual = diffEntity  +
                  ("http://hbp.eu/manual#origin", reconciledLink) +
                  ("http://hbp.eu/manual#parent", Json.obj("@id" -> reconciledLink))

                val preppedEntityForStorage = InstanceHelper.prepareManualEntityForStorage(updateToBeStoredInManual, request.user)

                consolidateFromManualSpace(nexusEndpoint, editorSpace, currentInstanceDisplayed, editorInstances, updateToBeStoredInManual) match {
                  case (consolidatedInstance, manualEntitiesDetailsOpt) =>

                    val manualUpsert = instanceService.upsertUpdateInManualSpace(editorSpace, manualEntitiesDetailsOpt, request.user, originalPath, preppedEntityForStorage, token)
                    manualUpsert.flatMap { res =>
                      logger.debug(s"Creation of manual update ${res.body}")
                      res.status match {
                        case status if status < 300 =>
                          val newManualUpdateId = (res.json \ "@id").as[String]
                          val reconciledUpsert = instanceService.updateReconciledInstance(editorSpace, currentInstanceDisplayed, editorInstances, originalInstance, preppedEntityForStorage, newManualUpdateId, consolidatedInstance.content, reconciledToken, request.user)
                          reconciledUpsert.map { re =>
                            logger.debug(s"Creation of reconciled update ${re.body}")
                            re.status match {
                              case recStatus if recStatus < 300 =>
                                Ok(NavigationHelper.resultWithBackLink(InstanceHelper.formatFromNexusToOption(consolidatedInstance.content, reconciledPrefix), originalPath, reconciledPrefix))
                              case _ =>
                                val resultBackLink = NavigationHelper.errorMessageWithBackLink(re.body, NavigationHelper.generateBackLink(originalPath, reconciledPrefix))
                                Result(
                                  ResponseHeader(
                                    re.status,
                                    flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](re.headers))
                                  ),
                                  HttpEntity.Strict(ByteString(resultBackLink.toString()), Some("application/json"))
                                )
                            }
                          }
                        case _ =>
                          val resultBackLink = NavigationHelper.errorMessageWithBackLink(res.body, NavigationHelper.generateBackLink(originalPath, reconciledPrefix))
                          Future.successful(Result(
                            ResponseHeader(
                              res.status,
                              flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](res.headers))
                            ),
                            HttpEntity.Strict(ByteString(resultBackLink.toString()), Some("application/json"))
                          ))
                      }

                    }
                }
              } else {
                val errors = editorInstancesRes.filter(_.isLeft).map {
                  case Left(res) => Some(res)
                  case _ => None
                }.filter(_.isDefined).map(_.get)
                val resultBackLink = NavigationHelper.errorMessageWithBackLink(errors.head.body, NavigationHelper.generateBackLink(originalPath, reconciledPrefix))
                logger.error(s"Could not fetch editor instances ${errors}")
                Future.successful(Result(
                  ResponseHeader(
                    errors.head.status,
                    flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](errors.head.headers))
                  ),
                  HttpEntity.Strict(ByteString(resultBackLink.toString()), Some("application/json"))
                ))
              }
          }
        }
      case Left(res) =>
        Future{
          val resultBackLink = NavigationHelper.errorMessageWithBackLink(res.body, NavigationHelper.generateBackLink(originalPath,reconciledPrefix))
          Result(
            ResponseHeader(
              res.status,
              flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](res.headers))
            ),
            HttpEntity.Strict(ByteString(resultBackLink.toString()), Some("application/json"))
          )
        }
    }
  }

  def updateInstance(org: String,
                     domain: String,
                     schema: String,
                     version: String,
                     id: String): Action[AnyContent] =
    (authenticatedUserAction andThen EditorUserAction.editorUserAction(org)).async { implicit request =>

      val token = OIDCHelper.getTokenFromRequest(request)
      val instancePath = NexusPath(org, domain, schema, version)
      val reconciledPath = NexusPath(NavigationHelper.addSuffixToOrg(org, reconciledPrefix), domain, schema, version)
      instanceService.retrieveInstance(reconciledPath, id, token).flatMap[Result] {
        case Left(res) =>
          instanceService.retrieveReconciledFromOriginal(instancePath, reconciledPath.org, id, token).flatMap {
            case Left(r) =>
              val resultBackLink = NavigationHelper
                .errorMessageWithBackLink(r.body, NavigationHelper.generateBackLink(instancePath, reconciledPrefix))
              Future.successful(Result(
                ResponseHeader(
                  r.status,
                  flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](r.headers))
                ),
                HttpEntity.Strict(ByteString(resultBackLink.toString()), Some("application/json"))
              ))
            case Right(reconciledInstance) =>
              reconciledInstance match {
                case Some(instance) =>
                  logger.debug("It is an original instance but a reconciled instance exists already")
                  updateWithReconciled(instance, instancePath)
                case _ =>
                  instanceService.retrieveInstance(instancePath, id, token).flatMap {
                    case Left(response) =>
                      val resultBackLink = NavigationHelper
                        .errorMessageWithBackLink(response.body, NavigationHelper.generateBackLink(instancePath, reconciledPrefix))
                      Future.successful(Result(
                        ResponseHeader(
                          response.status,
                          flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](response.headers))
                        ),
                        HttpEntity.Strict(ByteString(resultBackLink.toString()), Some("application/json"))
                      ))
                    case Right(originalInstance) =>
                      initialUpdate(originalInstance)
                  }

              }

          }
        case Right(currentInstanceDisplayed) =>
          logger.debug("It is a reconciled instance")
          updateWithReconciled(currentInstanceDisplayed, instancePath)
      }
    }


  def createInstance(
                      org: String,
                      domain:String,
                      datatype: String,
                      version:String
                    ): Action[AnyContent] = (authenticatedUserAction andThen EditorUserAction.editorUserAction(org)).async { implicit request =>
    val newInstance = request.body.asJson.get.as[JsObject]
    val instancePath = NexusPath(org, domain, datatype, version)
    val instance = buildNewInstanceFromForm( instancePath, FormHelper.formRegistry, newInstance )
    val token = OIDCHelper.getTokenFromRequest(request)
    val reconciledTokenFut = oIDCAuthService.getTechAccessToken()
    val editorSpace = EditorSpaceHelper.getGroupName(request.editorGroup, editorPrefix)
    val reconciledSpace = EditorSpaceHelper.getGroupName(request.editorGroup, reconciledPrefix)
    // TODO Get data type from the form
    val typedInstance = instance
      .+("@type" -> JsString(s"http://hbp.eu/${org}#${datatype.capitalize}"))
      .+("http://schema.org/identifier" -> JsString(md5HashString((instance \ "http://schema.org/name").as[String] )))
      .+("http://hbp.eu/manual#origin", JsString(""))
      .+("http://hbp.eu/manual#user_created", JsBoolean(true))
      .+("http://hbp.eu/manual#original_path", JsString(instancePath.toString()))
    // Save instance to nexus
    val createEditorDomain = nexusService.createDomain(nexusEndpoint, editorSpace, instancePath.domain, "", token)
    reconciledTokenFut.flatMap{ reconciledToken =>
      val createReconciledDomain = nexusService.createDomain(nexusEndpoint, reconciledSpace, instancePath.domain, "", reconciledToken)
      createEditorDomain flatMap{res =>
        logger.debug(res.body)
        val createEditorSchema = instanceService
          .createManualSchemaIfNeeded(typedInstance, instancePath, token, editorSpace, "manual", EditorSpaceHelper.nexusEditorContext("manual"))
        createEditorSchema.flatMap{ res =>
          logger.debug(res.toString)
          createReconciledDomain.flatMap{ res =>
            logger.debug(res.body)
            val createReconciledSchema = instanceService.
              createManualSchemaIfNeeded(typedInstance, instancePath, reconciledToken, reconciledSpace, "reconciled", EditorSpaceHelper.nexusEditorContext("reconciled"))
            createReconciledSchema.flatMap{ res =>
              logger.debug(res.toString)
              val insertNewInstance = instanceService.insertInstance(editorSpace, typedInstance, instancePath, token)
              insertNewInstance.flatMap { instance =>
                logger.debug(instance.body)
                val reconciledInstance = ReconciledInstanceHelper.addReconciledMandatoryFields(typedInstance, instancePath, request.user, (instance.json \ "@id").as[String])
                instanceService
                  .insertInstance(reconciledSpace, reconciledInstance, instancePath, reconciledToken).map { res =>
                  logger.debug(res.body)
                  res.status match {
                    case CREATED => // reformat ouput to keep it consistent with update
                      val output = res.json.as[JsObject]
                        .+("id", JsString((res.json.as[JsObject] \ "@id").as[String].split("data/").tail.mkString("data/")))
                        .-("@id")
                        .-("@context")
                        .-("nxv:rev")
                      Created(NavigationHelper.resultWithBackLink(output.as[JsObject], instancePath, reconciledPrefix))
                    case _ =>
                      val resultBackLink = NavigationHelper.errorMessageWithBackLink(res.body, NavigationHelper.generateBackLink(instancePath, reconciledPrefix))
                      Result(
                        ResponseHeader(res.status, flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](res.headers))),
                        HttpEntity.Strict(ByteString(resultBackLink.toString()), Some("application/json"))
                      )
                  }


                }
              }
            }
          }
        }
      }
    }
  }

  /**
    * Returns an empty form for a specific instance type
    * @param org
    * @param domain
    * @param datatype
    * @param version
    * @return
    */
  def getEmptyForm(org: String, domain: String, datatype: String, version: String): Action[AnyContent] = Action {
    implicit request =>
      val nexusPath = NexusPath(org, domain, datatype, version)
      val form = FormHelper.getFormStructure(nexusPath, JsNull)
      Ok(form)
  }


  // TODO Check for authentication and groups as for now everybody could see the whole graph
  def listWithBlazeGraph(privateSpace: String): Action[AnyContent] = Action.async { implicit request =>
    val sparqlPayload =
      s"""
         |SELECT ?schema  WHERE {
         |  ?instance a <$nexusEndpoint/vocabs/nexus/core/terms/v0.1.0/Instance> .
         |  ?instance <$nexusEndpoint/vocabs/nexus/core/terms/v0.1.0/deprecated> false .
         |  ?instance <$nexusEndpoint/vocabs/nexus/core/terms/v0.1.0/schema> ?schema .
         |  ?schema <$nexusEndpoint/vocabs/nexus/core/terms/v0.1.0/organization> <$nexusEndpoint/v0/organizations/$privateSpace> .
         |}GROUP BY ?schema
      """.stripMargin
    val start = System.currentTimeMillis()
    val res = ws
      .url(s"$sparqlEndpoint/bigdata/namespace/$blazegraphNameSpace/sparql")
      .withQueryStringParameters("query" -> sparqlPayload, "format" -> "json").get().map[Result] {
      res =>
        res.status match {
          case OK =>
            val arr = BlazegraphHelper.extractResult(res.json)
            val duration = System.currentTimeMillis() - start
            println(s"sparql query: \n$sparqlPayload\n\nduration: ${duration}ms")
            Ok(NodeTypeHelper.formatNodetypeList(arr))
          case _ =>
            Result(
              ResponseHeader(res.status, flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](res.headers))),
              HttpEntity.Strict(res.bodyAsBytes, getContentType(res.headers))
            )
        }
    }
    res
  }

  def listEntities(privateSpace: String): Action[AnyContent] = Action {
    // Editable instance types are the one for which form creation is known
    Ok(FormHelper.editableEntitiyTypes)
  }
}


