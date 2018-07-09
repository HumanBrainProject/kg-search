
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

import common.helpers.BlazegraphHelper
import common.helpers.ResponseHelper._
import editor.helper.InstanceHelper._
import common.models.NexusPath
import editor.helper.InstanceHelper
import editor.helpers.{FormHelper, NodeTypeHelper}
import editor.models.{InMemoryKnowledge, IncomingLinksInstances, Instance}
import helpers.ReconciledInstanceHelper
import helpers.authentication.OIDCHelper
import javax.inject.{Inject, Singleton}
import models.authentication.AuthenticatedUserAction
import play.api.{Configuration, Logger}
import play.api.http.HttpEntity
import play.api.http.Status.OK
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc._
import services.InstanceService
import akka.util.ByteString

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NexusEditorController @Inject()(
                                       cc: ControllerComponents,
                                       authenticatedUserAction: AuthenticatedUserAction,
                                       instanceService: InstanceService,
                                       config: Configuration,
                                       ws: WSClient
                                     )(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  val blazegraphNameSpace = config.getOptional[String]("blazegraph.namespace").getOrElse("kg")
  val nexusEndpoint = config.getOptional[String]("nexus.endpoint").getOrElse("https://nexus-dev.humanbrainproject.org")
  val reconcileEndpoint = config.getOptional[String]("reconcile.endpoint").getOrElse("https://nexus-admin-dev.humanbrainproject.org/reconcile")
  val reconciledSpace = config.getOptional[String]("nexus.reconciledspace").getOrElse("reconciled/poc")
  val manualSpace = config.getOptional[String]("nexus.manualspace").getOrElse("manual/poc")
  val sparqlEndpoint = config.getOptional[String]("blazegraph.endpoint").getOrElse("http://localhost:9999")
  val inMemoryManualSpaceSchemas = new InMemoryKnowledge(nexusEndpoint, manualSpace)
  val inMemoryReconciledSpaceSchemas = new InMemoryKnowledge(nexusEndpoint, reconciledSpace)


  val logger = Logger(this.getClass)

  // TODO Check for authentication and groups as for now everybody could see the whole graph
  def listInstances(org: String, domain:String, datatype: String, version:String): Action[AnyContent] = Action.async { implicit request =>
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
         |	   ?reconciled <http://schema.org/name> ?name .
         |    OPTIONAL{ ?reconciled <http://schema.org/description> ?description .}
         |    ?reconciled <http://hbp.eu/reconciled#original_parent> ?instance .
         |  	?reconciled a <$nexusEndpoint/vocabs/nexus/core/terms/v0.1.0/Instance> .
         |  ?reconciled <$nexusEndpoint/vocabs/nexus/core/terms/v0.1.0/deprecated> false .
         |  ?reconciled <$nexusEndpoint/vocabs/nexus/core/terms/v0.1.0/schema> <$nexusEndpoint/v0/schemas/reconciled/poc/${nexusPath.schema}/${nexusPath.version}> .
         |
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

  def getInstance(org: String, domain:String, datatype: String, version:String, id: String): Action[AnyContent] = Action.async { implicit request =>
    val token = request.headers.get("Authorization").getOrElse("")
    val nexusPath = NexusPath(org, domain, datatype, version)
    instanceService.retrieveOriginalInstance(nexusEndpoint, nexusPath, id, token).flatMap[Result] {
      case Left(res) => Future.successful(Result(ResponseHeader(res.status, flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](res.headers))),
        HttpEntity.Strict(res.bodyAsBytes, getContentType(res.headers))))
      case Right(originalInstance) =>
        instanceService.retrieveIncomingLinks(nexusEndpoint, originalInstance, token).map {
          instances =>
            val reconcileInstances = IncomingLinksInstances(instances, (originalInstance.content \ "@id").as[String]).reconciledInstances
            val path = if( (originalInstance.content \ "http://hbp.eu/manual#original_path").asOpt[String].isDefined){
              NexusPath((originalInstance.content \ "http://hbp.eu/manual#original_path").as[String].split("/"))
            }else{
              originalInstance.nexusPath
            }
            if (reconcileInstances.nonEmpty) {
              val reconcileInstance = reconcileInstances.head
              FormHelper.getFormStructure(path, reconcileInstance.content) match {
                case JsNull => NotImplemented("Form template is not yet implemented")
                case json => Ok(json)
              }
            } else {
              logger.debug("Nothing found in the consolidated space")
              FormHelper.getFormStructure(path, originalInstance.content) match {
                case JsNull => NotImplemented("Form template is not yet implemented")
                case json => Ok(json)
              }

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

  def getSpecificReconciledInstance( org: String, domain:String, datatype: String, version:String, id:String, revision:Int): Action[AnyContent] = Action.async { implicit request =>
    val token = request.headers.get("Authorization").getOrElse("")
    val nexusPath = NexusPath(org, domain, datatype, version)
    ws
      .url(s"https://$nexusEndpoint/v0/data/${nexusPath.toString()}/$id?rev=$revision&deprecated=false&fields=all")
      .withHttpHeaders("Authorization" -> token).get().map{
      response => response.status match {
        case OK =>
          val json = response.json
          val nexusId = Instance.getIdfromURL((json \ "http://hbp.eu/reconciled#original_parent" \ "@id").as[String])
          val datatype = nexusId.splitAt(nexusId.lastIndexOf("/"))
          val originalDatatype = NexusPath(datatype._1.split("/").toList)
          FormHelper.getFormStructure(originalDatatype, response.json) match {
            case JsNull => NotImplemented("Form template is not yet implemented")
            case json => Ok(json)
          }
        case _ =>
          Result(
            ResponseHeader(
              response.status,
              flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](response.headers))
            ),
            HttpEntity.Strict(response.bodyAsBytes, getContentType(response.headers))
          )
      }

    }
  }

  def updateInstance(org: String, domain: String, datatype: String, version: String, id: String): Action[AnyContent] = authenticatedUserAction.async { implicit request =>
    val token = OIDCHelper.getTokenFromRequest(request)
    val instancePath = NexusPath(org, domain, datatype, version)
    //    val orgContentFuture = retrieve(id, request.headers) // call core of get id
    val newValue = request.body.asJson.get
    instanceService.retrieveOriginalInstance(nexusEndpoint, instancePath, id, token).flatMap[Result] {
      case Left(res) => Future.successful(Result(ResponseHeader(res.status, flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](res.headers))),
        HttpEntity.Strict(res.bodyAsBytes, getContentType(res.headers))))
      case Right(originalInstance) =>
        instanceService.retrieveIncomingLinks(nexusEndpoint, originalInstance, token).flatMap[Result] {
          incomingLinks =>
            val incominglinkInstances = IncomingLinksInstances(incomingLinks, (originalInstance.content \ "@id").as[String])
            val currentInstanceDisplayed = InstanceHelper.getCurrentInstanceDisplayed(incominglinkInstances.reconciledInstances, originalInstance)

            // As we cannot pass / in the name of a field we have replaced them with %nexus-slash%
            val updateFromUI = Json.parse(FormHelper.unescapeSlash(newValue.toString())).as[JsObject] - "id"
            val updatedInstance = buildInstanceFromForm(originalInstance.content, updateFromUI, nexusEndpoint)
            val userInfo = request.user
            val updateToBeStoredInManual = buildDiffEntity(currentInstanceDisplayed, updatedInstance.toString, originalInstance) + ("@type", JsString(s"http://hbp.eu/manual#${originalInstance.nexusPath.schema.capitalize}"))
            consolidateFromManualSpace(nexusEndpoint, manualSpace, originalInstance, incominglinkInstances, updateToBeStoredInManual) match {
              case (consolidatedInstance, manualEntitiesDetailsOpt) =>
                logger.debug(s"Consolidated instance $updatedInstance")
                val preppedEntityForStorage = InstanceHelper.prepareManualEntityForStorage(updateToBeStoredInManual, userInfo)

                val manualSchema = instanceService.createManualSchemaIfNeeded(nexusEndpoint, updateToBeStoredInManual, originalInstance.nexusPath, token, inMemoryManualSpaceSchemas, manualSpace, "manual")
                val manualUpsert = instanceService.upsertUpdateInManualSpace(nexusEndpoint, manualSpace, manualEntitiesDetailsOpt, userInfo, originalInstance.nexusPath.schema, preppedEntityForStorage, token)
                val reconciledSchema = instanceService.createManualSchemaIfNeeded(nexusEndpoint, updatedInstance, originalInstance.nexusPath, token, inMemoryReconciledSpaceSchemas, reconciledSpace, "reconciled")
                val reconciledUpsert = instanceService.upsertReconciledInstance(nexusEndpoint, reconciledSpace, manualSpace, incominglinkInstances, originalInstance, preppedEntityForStorage, updatedInstance, consolidatedInstance, token, userInfo)

                val manualRes: Future[WSResponse] = manualSchema.flatMap(res => manualUpsert)
                val reconciledRes: Future[WSResponse] = reconciledSchema.flatMap(res => reconciledUpsert)
                manualRes.flatMap{ res =>
                  logger.debug(s"Creation of manual update ${res.body}")
                  res.status match {
                    case status if status < 300 =>
                      reconciledRes.map{ re =>
                        logger.debug(s"Creation of reconciled update ${re.body}")
                        re.status match {
                          case recStatus if recStatus < 300 => Ok(InstanceHelper.formatFromNexusToOption(updatedInstance))
                          case _ =>
                            Result(
                              ResponseHeader(
                                re.status,
                                flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](re.headers))
                              ),
                              HttpEntity.Strict(re.bodyAsBytes, getContentType(re.headers))
                            )
                        }
                      }
                    case _ =>
                      Future.successful(Result(
                        ResponseHeader(
                          res.status,
                          flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](res.headers))
                        ),
                        HttpEntity.Strict(res.bodyAsBytes, getContentType(res.headers))
                      ))
                  }

                }
            }
        }
    }

  }

  def createInstance(org: String, domain:String, datatype: String, version:String): Action[AnyContent] = authenticatedUserAction.async { implicit request =>
    val instanceForm = request.body.asJson.get.as[JsObject]
    val instancePath = NexusPath(org, domain, datatype, version)
    val instance = Json.parse(FormHelper.unescapeSlash(instanceForm.toString)).as[JsObject]
    val token = OIDCHelper.getTokenFromRequest(request)
    // TODO Get data type from the form
    val typedInstance = instance
      .+("@type" -> JsString(s"http://hbp.eu/manual#${datatype.capitalize}"))
      .+("http://schema.org/identifier" -> JsString(md5HashString((instance \ "http://schema.org/name").as[String] )))
      .+("http://hbp.eu/manual#origin", JsString(""))
      .+("http://hbp.eu/manual#user_created", JsBoolean(true))
      .+("http://hbp.eu/manual#original_path", JsString(instancePath.toString()))
    // Save instance to nexus


    for {
      manualSchema <- instanceService.createManualSchemaIfNeeded(nexusEndpoint, typedInstance, instancePath, token, inMemoryManualSpaceSchemas, manualSpace, "manual")
      reconciledSchema <- instanceService.createManualSchemaIfNeeded(nexusEndpoint, typedInstance, instancePath, token, inMemoryManualSpaceSchemas, manualSpace, "reconciled")
      manualRes <- ws.url(s"$nexusEndpoint/v0/data/$manualSpace/${instancePath.schema}/${instancePath.version}").post(typedInstance)
      reconciledInstance = ReconciledInstanceHelper.addReconciledMandatoryFields(typedInstance, instancePath, request.user, (manualRes.json \ "@id").as[String])
      reconciledRes <- instanceService.createReconcileInstance(nexusEndpoint, reconciledSpace, reconciledInstance, instancePath.schema, instancePath.version, token)
    } yield {
      if(manualSchema && reconciledSchema){
        (manualRes.status, reconciledRes.status) match {
          case (CREATED, CREATED) =>
            // reformat ouput to keep it consistent with update
            val output = manualRes.json.as[JsObject]
              .+("id", JsString((manualRes.json.as[JsObject] \ "@id").as[String].split("data/").tail.mkString("data/")))
              .-("@id")
              .-("@context")
              .-("nxv:rev")
            Result(
              ResponseHeader(manualRes.status, flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](manualRes.headers))),
              HttpEntity.Strict(ByteString(output.toString.getBytes("UTF-8")), getContentType(manualRes.headers))
            )
          case (_ , CREATED) =>
            Result(
              ResponseHeader(manualRes.status, flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](manualRes.headers))),
              HttpEntity.Strict(manualRes.bodyAsBytes, getContentType(manualRes.headers))
            )
          case (_, _) =>
            Result(
              ResponseHeader(reconciledRes.status, flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](reconciledRes.headers))),
              HttpEntity.Strict(reconciledRes.bodyAsBytes, getContentType(reconciledRes.headers))
            )
        }
      }else{
        BadRequest("Cannot create the schema")
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
          case 200 =>
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


