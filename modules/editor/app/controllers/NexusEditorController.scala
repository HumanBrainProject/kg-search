
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
import editor.models.{InMemoryKnowledge, IncomingLinksInstances, Instance}
import authentication.helpers.OIDCHelper
import helpers.ReconciledInstanceHelper
import javax.inject.{Inject, Singleton}
import authentication.models.AuthenticatedUserAction
import authentication.service.OIDCAuthService
import editor.actions.EditorUserAction
import play.api.{Configuration, Logger}
import play.api.http.HttpEntity
import play.api.http.Status.OK
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc._
import services.{InstanceService, NexusService}

import scala.concurrent.{ExecutionContext, Future}
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
              "data" -> InstanceHelper.formatInstanceList(arr),
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
    * If the instance possesses an Incoming Link from the ${org}reconciled space; returns the reconciled instance.
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
    instanceService.retrieveOriginalInstance(nexusEndpoint, nexusPath, id, token).flatMap[Result] {
      case Left(res) =>
        val resultBackLink = NavigationHelper.errorMessageWithBackLink(res.body, NavigationHelper.generateBackLink(nexusPath, reconciledPrefix))
        Future.successful(Result(ResponseHeader(res.status, flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](res.headers))),
        HttpEntity.Strict(ByteString(resultBackLink.toString()), Some("application/json"))))
      case Right(originalInstance) =>
        instanceService.retrieveIncomingLinks(nexusEndpoint, originalInstance, token).map {
          instances =>
            val reconcileInstances = IncomingLinksInstances(instances, (originalInstance.content \ "@id").as[String]).reconciledInstances
            val path = if( (originalInstance.content \ "http://hbp.eu/manual#original_path").asOpt[String].isDefined){
              NexusPath((originalInstance.content \ "http://hbp.eu/manual#original_path").as[String].split("/"))
            }else{
              originalInstance.nexusPath
            }
            val returnedInstance = if (reconcileInstances.nonEmpty) {
              FormHelper.getFormStructure(path, reconcileInstances.head.content)
            } else {
              logger.debug("Nothing found in the consolidated space")
              FormHelper.getFormStructure(path, originalInstance.content)
            }
            returnedInstance  match {
              case JsNull => NotImplemented(NavigationHelper.errorMessageWithBackLink("Form not implemented", NavigationHelper.generateBackLink(path, reconciledPrefix) ))
              case json => Ok(NavigationHelper.resultWithBackLink(json.as[JsObject], nexusPath,reconciledPrefix))
            }
        }
    }
  }

  def getInstanceNumberOfAvailableRevisions(org: String, domain:String, datatype: String, version:String, id: String): Action[AnyContent] = Action.async { implicit request =>
    val token = request.headers.get("Authorization").getOrElse("")
    val nexusPath = NexusPath(org, domain, datatype, version)
    instanceService.retrieveOriginalInstance(nexusEndpoint, nexusPath, id, token).flatMap[Result] {
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
            val nexusId = Instance.getIdfromURL((json \ "http://hbp.eu/reconciled#original_parent" \ "@id").as[String])
            val datatype = nexusId.splitAt(nexusId.lastIndexOf("/"))
            val originalDatatype = NexusPath(datatype._1.split("/").toList)
            FormHelper.getFormStructure(originalDatatype, response.json) match {
              case JsNull => NotImplemented(NavigationHelper.errorMessageWithBackLink("Form not implemented", NavigationHelper.generateBackLink(nexusPath, reconciledPrefix) ))
              case json => Ok(NavigationHelper.resultWithBackLink(json.as[JsObject], nexusPath,reconciledPrefix))
            }
          case _ =>
            val resultBackLink = NavigationHelper.errorMessageWithBackLink(response.body, NavigationHelper.generateBackLink(nexusPath, reconciledPrefix))
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

  def updateInstance(org: String,
                     domain: String,
                     datatype: String,
                     version: String,
                     id: String): Action[AnyContent] =
    (authenticatedUserAction andThen EditorUserAction.editorUserAction(org)).async { implicit request =>

      val token = OIDCHelper.getTokenFromRequest(request)
      val reconciledTokenFut = oIDCAuthService.getTechAccessToken()
      val instancePath = NexusPath(org, domain, datatype, version)
      //    val orgContentFuture = retrieve(id, request.headers) // call core of get id
      val newValue = request.body.asJson.get
      val editorSpace = EditorSpaceHelper.getGroupName(request.editorGroup, editorPrefix)
      val reconciledSpace = EditorSpaceHelper.getGroupName(request.editorGroup, reconciledPrefix)
      instanceService.retrieveOriginalInstance(nexusEndpoint, instancePath, id, token).flatMap[Result] {
        case Left(res) => Future.successful(Result(ResponseHeader(res.status, flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](res.headers))),
          HttpEntity.Strict(res.bodyAsBytes, getContentType(res.headers))))
        case Right(originalInstance) =>
          reconciledTokenFut.flatMap { reconciledToken =>
            instanceService.retrieveIncomingLinks(nexusEndpoint, originalInstance, token).flatMap[Result] {
              incomingLinks =>
                val incominglinkInstances = IncomingLinksInstances(incomingLinks, (originalInstance.content \ "@id").as[String])
                val currentInstanceDisplayed = InstanceHelper.getCurrentInstanceDisplayed(incominglinkInstances.reconciledInstances, originalInstance)

                // As we cannot pass / in the name of a field we have replaced them with %nexus-slash%
                val updateFromUI = Json.parse(FormHelper.unescapeSlash(newValue.toString())).as[JsObject] - "id"
                val updatedInstance = buildInstanceFromForm(originalInstance.content, updateFromUI, nexusEndpoint)
                val userInfo = request.user
                val updateToBeStoredInManual = buildDiffEntity(currentInstanceDisplayed, updatedInstance.toString, originalInstance) + ("@type", JsString(s"http://hbp.eu/${originalInstance.nexusPath.org}#${originalInstance.nexusPath.schema.capitalize}"))
                consolidateFromManualSpace(nexusEndpoint, editorSpace, originalInstance, incominglinkInstances, updateToBeStoredInManual) match {
                  case (consolidatedInstance, manualEntitiesDetailsOpt) =>
                    logger.debug(s"Consolidated instance $updatedInstance")
                    val preppedEntityForStorage = InstanceHelper.prepareManualEntityForStorage(updateToBeStoredInManual, userInfo)

                    val createEditorDomain = nexusService.createDomain(nexusEndpoint, editorSpace, originalInstance.nexusPath.domain, "", token)
                    val createReconciledDomain = nexusService.createDomain(nexusEndpoint, reconciledSpace, originalInstance.nexusPath.domain, "", reconciledToken)

                    val manualRes: Future[WSResponse] = createEditorDomain.flatMap { re =>
                      val manualSchema = instanceService.createManualSchemaIfNeeded(nexusEndpoint, updateToBeStoredInManual, originalInstance.nexusPath, token, editorSpace, "manual", EditorSpaceHelper.nexusEditorContext("manual"))
                      manualSchema.flatMap { res =>
                        val manualUpsert = instanceService.upsertUpdateInManualSpace(nexusEndpoint, editorSpace, manualEntitiesDetailsOpt, userInfo, originalInstance.nexusPath, preppedEntityForStorage, token)
                        manualUpsert.map { res =>
                          logger.debug(res.body)
                          res
                        }
                      }
                    }
                    val reconciledRes: Future[WSResponse] = createReconciledDomain.flatMap { re =>
                      val reconciledSchema = instanceService.createManualSchemaIfNeeded(nexusEndpoint, updatedInstance, originalInstance.nexusPath, reconciledToken, reconciledSpace, "reconciled", EditorSpaceHelper.nexusEditorContext("reconciled"))
                      reconciledSchema.flatMap { res =>
                        val reconciledUpsert = instanceService.upsertReconciledInstance(nexusEndpoint, reconciledSpace, editorSpace, incominglinkInstances, originalInstance, preppedEntityForStorage, updatedInstance, consolidatedInstance, reconciledToken, userInfo)
                        reconciledUpsert.map { res =>
                          res
                        }
                      }
                    }
                    manualRes.flatMap { res =>
                      logger.debug(s"Creation of manual update ${res.body}")
                      res.status match {
                        case status if status < 300 =>
                          reconciledRes.map { re =>
                            logger.debug(s"Creation of reconciled update ${re.body}")
                            re.status match {
                              case recStatus if recStatus < 300 =>
                                Ok(NavigationHelper.resultWithBackLink(InstanceHelper.formatFromNexusToOption(updatedInstance), instancePath, reconciledPrefix))
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
          }
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
          .createManualSchemaIfNeeded(nexusEndpoint, typedInstance, instancePath, token, editorSpace, "manual", EditorSpaceHelper.nexusEditorContext("manual"))
        createEditorSchema.flatMap{ res =>
          logger.debug(res.toString)
          createReconciledDomain.flatMap{ res =>
            logger.debug(res.body)
            val createReconciledSchema = instanceService.
              createManualSchemaIfNeeded(nexusEndpoint, typedInstance, instancePath, reconciledToken, reconciledSpace, "reconciled", EditorSpaceHelper.nexusEditorContext("reconciled"))
            createReconciledSchema.flatMap{ res =>
              logger.debug(res.toString)
              val insertNewInstance = instanceService.insertNewInstance(nexusEndpoint,editorSpace, typedInstance, instancePath, token)
              insertNewInstance.flatMap { instance =>
                logger.debug(instance.body)
                val reconciledInstance = ReconciledInstanceHelper.addReconciledMandatoryFields(typedInstance, instancePath, request.user, (instance.json \ "@id").as[String])
                instanceService
                  .createReconcileInstance(nexusEndpoint, reconciledSpace, reconciledInstance, instancePath.domain, instancePath.schema, instancePath.version, reconciledToken).map { res =>
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


