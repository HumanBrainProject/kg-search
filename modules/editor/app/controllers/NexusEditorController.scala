
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
import editor.models.{InMemoryKnowledge, Instance}
import helpers.authentication.OIDCHelper
import javax.inject.{Inject, Singleton}
import models.authentication.AuthenticatedUserAction
import play.api.{Configuration, Logger}
import play.api.http.HttpEntity
import play.api.http.Status.OK
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NexusEditorController @Inject()(cc: ControllerComponents, authenticatedUserAction: AuthenticatedUserAction, config: Configuration)(implicit ec: ExecutionContext, ws: WSClient)
  extends AbstractController(cc) {

  val blazegraphNameSpace = config.getOptional[String]("blazegraph.namespace").getOrElse("kg")
  val nexusEndpoint = config.getOptional[String]("nexus.endpoint").getOrElse("https://nexus-dev.humanbrainproject.org")
  val reconcileEndpoint = config.getOptional[String]("reconcile.endpoint").getOrElse("https://nexus-admin-dev.humanbrainproject.org/reconcile")
  val reconciledSpace = config.getOptional[String]("nexus.reconciledspace").getOrElse("reconciled/poc")
  val manualSpace = config.getOptional[String]("nexus.manualspace").getOrElse("manual/poc")
  val sparqlEndpoint = config.getOptional[String]("blazegraph.endpoint").getOrElse("http://localhost:9999")
  val oidcUserInfoEndpoint = config.get[String]("auth.userinfo")
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
         |	?instance a <https://nexus-dev.humanbrainproject.org/vocabs/nexus/core/terms/v0.1.0/Instance> .
         |  ?instance <https://nexus-dev.humanbrainproject.org/vocabs/nexus/core/terms/v0.1.0/deprecated> false .
         |  ?instance <https://nexus-dev.humanbrainproject.org/vocabs/nexus/core/terms/v0.1.0/schema> <https://nexus-dev.humanbrainproject.org/v0/schemas/${nexusPath.toString()}> .
         |      OPTIONAL{ ?instance <http://schema.org/description> ?description .}
         |    FILTER(!EXISTS{?i <http://hbp.eu/reconciled#original_parent> ?instance}) .
         |    }
         |   UNION {
         |	   ?reconciled <http://schema.org/name> ?name .
         |    OPTIONAL{ ?reconciled <http://schema.org/description> ?description .}
         |    ?reconciled <http://hbp.eu/reconciled#original_parent> ?instance .
         |  	?reconciled a <https://nexus-dev.humanbrainproject.org/vocabs/nexus/core/terms/v0.1.0/Instance> .
         |  ?reconciled <https://nexus-dev.humanbrainproject.org/vocabs/nexus/core/terms/v0.1.0/deprecated> false .
         |  ?reconciled <https://nexus-dev.humanbrainproject.org/vocabs/nexus/core/terms/v0.1.0/schema> <https://nexus-dev.humanbrainproject.org/v0/schemas/reconciled/poc/${nexusPath.schema}/v0.0.4> .
         |
         |  }
         |}
         |
      """.stripMargin
    val start = System.currentTimeMillis()
    val res = ws.url(s"$sparqlEndpoint/bigdata/namespace/$blazegraphNameSpace/sparql").withQueryStringParameters("query" -> sparqlPayload, "format" -> "json").get().map[Result] {
      res =>
        res.status match {
          case 200 =>
            val arr = BlazegraphHelper.extractResult(res.json)
            val duration = System.currentTimeMillis() - start
            println(s"sparql query: \n$sparqlPayload\n\nduration: ${duration}ms")
            Ok(Json.obj(
              "data" -> InstanceHelper.formatInstanceList(arr),
              "label" -> JsString((FormHelper.formRegistry \ nexusPath.org \ nexusPath.domain \ nexusPath.schema \ nexusPath.version \ "label").asOpt[String].getOrElse(datatype)))
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
    res
  }

  def getInstance(org: String, domain:String, datatype: String, version:String, id: String): Action[AnyContent] = Action.async { implicit request =>
    val token = request.headers.get("Authorization").getOrElse("")
    val nexusPath = NexusPath(org, domain, datatype, version)
    InstanceHelper.retrieveOriginalInstance(nexusEndpoint, nexusPath, id, token).flatMap[Result] {
      case Left(res) => Future.successful(Result(ResponseHeader(res.status, flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](res.headers))),
        HttpEntity.Strict(res.bodyAsBytes, getContentType(res.headers))))
      case Right(originalInstance) =>
        InstanceHelper.retrieveIncomingLinks(nexusEndpoint, originalInstance, token).map {
          instances =>
            val reconcileInstances = instances.filter(instance => instance.nexusPath.toString() contains s"$reconciledSpace/${nexusPath.schema}/${nexusPath.version}")
            if (reconcileInstances.nonEmpty) {
              val reconcileInstance = reconcileInstances.head
              FormHelper.getFormStructure(originalInstance.nexusPath, reconcileInstance.content) match {
                case JsNull => NotImplemented("Form template is not yet implemented")
                case json => Ok(json)
              }
            } else {
              logger.debug("Nothing found in the consolidated space")
              FormHelper.getFormStructure(originalInstance.nexusPath, originalInstance.content) match {
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
    InstanceHelper.retrieveOriginalInstance(nexusEndpoint, nexusPath, id, token).flatMap[Result] {
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

  def updateInstance(org: String, domain:String, datatype: String, version:String, id: String): Action[AnyContent] = authenticatedUserAction.async { implicit request =>
    val token = OIDCHelper.getTokenFromRequest(request)
    val instancePath = NexusPath(org, domain, datatype, version)
    //    val orgContentFuture = retrieve(id, request.headers) // call core of get id
    val newValue = request.body.asJson.get
    retrieveOriginalInstance(nexusEndpoint, instancePath, id, token).flatMap[Result] {
      case Left(res) => Future.successful(Result(ResponseHeader(res.status, flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](res.headers))),
        HttpEntity.Strict(res.bodyAsBytes, getContentType(res.headers))))
      case Right(originalInstance) =>
        retrieveIncomingLinks(nexusEndpoint, originalInstance, token).flatMap[Result] {
          incomingLinks =>
            val currentReconciledInstances = incomingLinks.filter(instance => instance.nexusPath.toString() contains reconciledSpace)
            val currentInstanceDisplayed = InstanceHelper.getCurrentInstanceDisplayed(currentReconciledInstances, originalInstance)

            // As we cannot pass / in the name of a field we have replaced them with %nexus-slash%
            val updateFromUI = Json.parse(FormHelper.unescapeSlash(newValue.toString())).as[JsObject] - "id"
            val updatedInstance = buildInstanceFromForm(originalInstance.content, updateFromUI)
            val userInfo = request.user
            val updateToBeStoredInManual = buildDiffEntity(currentInstanceDisplayed, updatedInstance.toString, originalInstance) + ("@type", JsString(s"http://hbp.eu/manual#${originalInstance.nexusPath.schema.capitalize}"))
            consolidateFromManualSpace(manualSpace, originalInstance, incomingLinks, updateToBeStoredInManual) match {
              case (consolidatedInstance, manualEntitiesDetailsOpt) =>
                logger.debug(s"Consolidated instance $updatedInstance")
                val re: Future[Result] = for {
                  createdSchemas <- createManualSchemaIfNeeded(nexusEndpoint, updateToBeStoredInManual, originalInstance.nexusPath, token, inMemoryManualSpaceSchemas, manualSpace, "manual")
                  preppedEntityForStorage = InstanceHelper.prepareManualEntityForStorage(updateToBeStoredInManual, userInfo)
                  createdInManualSpace <- upsertUpdateInManualSpace(nexusEndpoint, manualSpace, manualEntitiesDetailsOpt, userInfo, originalInstance.nexusPath.schema, preppedEntityForStorage, token)
                  createReconciledInstance <- upsertReconciledInstance(nexusEndpoint, reconciledSpace, manualSpace, incomingLinks, originalInstance, preppedEntityForStorage, updatedInstance, consolidatedInstance, token, userInfo, inMemoryManualSpaceSchemas)
                } yield {
                  logger.debug(s"Result from reconciled upsert: ${createReconciledInstance.status}")
                  logger.debug(createReconciledInstance.body)
                  (createReconciledInstance.status, createdInManualSpace.status) match {
                    case (OK, OK) => Ok(InstanceHelper.formatFromNexusToOption(updatedInstance))
                    case (OK, _) => Result(
                      ResponseHeader(
                        createdInManualSpace.status,
                        flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](createdInManualSpace.headers))
                      ),
                      HttpEntity.Strict(createdInManualSpace.bodyAsBytes, getContentType(createdInManualSpace.headers))
                    )
                    case (_, _) =>
                      Result(
                        ResponseHeader(
                          createReconciledInstance.status,
                          flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](createReconciledInstance.headers))
                        ),
                        HttpEntity.Strict(createReconciledInstance.bodyAsBytes, getContentType(createReconciledInstance.headers))
                      )
                  }
                }
                re
            }
        }
    }

  }

  def createInstance(org: String, domain:String, datatype: String, version:String): Action[AnyContent] = authenticatedUserAction.async { implicit request =>
    val instanceForm = request.body.asJson.get.as[JsObject]
    val instancePath = NexusPath(org, domain, datatype, version)
    val instance = buildNewInstanceFromForm( (instanceForm \ "fields").as[JsObject])
    val token = OIDCHelper.getTokenFromRequest(request)
    // TODO Get data type from the form
    val typedInstance = instance
      .+("@type" -> JsString(s"http://hbp.eu/manual#${datatype.capitalize}"))
      .+("http://schema.org/identifier" -> JsString(md5HashString((instance \ "http://schema.org/name").as[String] )))
      .+("http://hbp.eu/manual#origin", JsString(""))
    // Save instance to nexus
    createManualSchemaIfNeeded(nexusEndpoint, typedInstance, instancePath, token, inMemoryManualSpaceSchemas, manualSpace, "manual").flatMap[Result]{
      schemaCreated =>
        if(schemaCreated){
          ws.url(s"$nexusEndpoint/v0/data/$manualSpace/${instancePath.schema}/${instancePath.version}").post(typedInstance).map{
            res =>
              Result(
                ResponseHeader(res.status, flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](res.headers))),
                HttpEntity.Strict(res.bodyAsBytes, getContentType(res.headers))
              )
          }
        }else{
          Future.successful(BadRequest("Cannot create the schema"))
        }
    }
  }

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
         |  ?instance a <https://nexus-dev.humanbrainproject.org/vocabs/nexus/core/terms/v0.1.0/Instance> .
         |  ?instance <https://nexus-dev.humanbrainproject.org/vocabs/nexus/core/terms/v0.1.0/deprecated> false .
         |  ?instance <https://nexus-dev.humanbrainproject.org/vocabs/nexus/core/terms/v0.1.0/schema> ?schema .
         |  ?schema <https://nexus-dev.humanbrainproject.org/vocabs/nexus/core/terms/v0.1.0/organization> <https://nexus-dev.humanbrainproject.org/v0/organizations/$privateSpace> .
         |}GROUP BY ?schema
         |
      """.stripMargin
    val start = System.currentTimeMillis()
    val res = ws.url(s"$sparqlEndpoint/bigdata/namespace/$blazegraphNameSpace/sparql").withQueryStringParameters("query" -> sparqlPayload, "format" -> "json").get().map[Result] {
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


