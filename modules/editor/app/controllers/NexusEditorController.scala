
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
import common.models.{NexusInstance, NexusPath, User}
import editor.actions.EditorUserAction
import editor.helper.InstanceHelper
import editor.helpers.{EditorSpaceHelper, FormHelper, NavigationHelper, NodeTypeHelper}
import editor.models._
import authentication.helpers.OIDCHelper
import helpers.{ReconciledInstanceHelper, ResponseHelper}
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
import editor.services.{InstanceService, ReleaseService}
import nexus.services.NexusService
import editor.services.ArangoQueryService

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
                                       releaseService: ReleaseService,
                                       arangoQueryService: ArangoQueryService,
                                       ws: WSClient
                                     )(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  val blazegraphNameSpace: String = config.getOptional[String]("blazegraph.namespace").getOrElse("kg")
  val nexusEndpoint: String = config.getOptional[String]("nexus.endpoint").getOrElse("https://nexus-dev.humanbrainproject.org")
  val reconciledPrefix: String = config.getOptional[String]("nexus.reconciled.prefix").getOrElse("reconciled")
  val editorPrefix: String = config.getOptional[String]("nexus.editor.prefix").getOrElse("editor")
  val sparqlEndpoint: String = config.getOptional[String]("blazegraph.endpoint").getOrElse("http://localhost:9999")
  val kgQueryEndpoint: String = config.getOptional[String]("kgquery.endpoint").getOrElse("http://localhost:8600")

  val logger = Logger(this.getClass)

  def listInstances(org: String, domain:String, datatype: String, version:String, from: Int, size: Int, search:String): Action[AnyContent] = authenticatedUserAction.async  { implicit request =>
    val nexusPath = NexusPath(org, domain, datatype, version)
    ws.url(s"$nexusEndpoint/v0/organizations/$org").addHttpHeaders("Authorization" -> request.headers.get("Authorization").getOrElse("")).get().flatMap{
      res =>
        res.status match {
          case UNAUTHORIZED =>
            val resultBackLink = NavigationHelper.errorMessageWithBackLink("You are not allowed to perform this request")
            Future.successful(
              Unauthorized(resultBackLink)
            )
          case OK =>
            val start = System.currentTimeMillis()
            ws.url(s"$kgQueryEndpoint/arango/instances/${nexusPath.toString()}?from=$from&size=$size&search=$search").get().map{
              res =>
                res.status match {
                  case OK =>
                    Ok(
                    Json.obj("data" -> InstanceHelper.formatInstanceList(res.json.as[JsArray], reconciledPrefix),
                      "label" -> JsString(
                        (FormHelper.formRegistry \ nexusPath.org \ nexusPath.domain \ nexusPath.schema \ nexusPath.version \ "label").asOpt[String]
                          .getOrElse(nexusPath.toString())
                      ))
                  )
                  case _ => ResponseHelper.forwardResultResponse(res)
                }
            }
        }
    }
  }

  /**
    * Return a instance by its nexus ID
    * The response is sent with a json object "back_link" with the path of the schema of the instance
    * if the schema exists returns e.g myorg/mydomain/myschema otherwise returns an empty string
    *
    * @param org The organization of the instance
    * @param domain The domain of the instance
    * @param schema The schema of the instance
    * @param version The version of the schema
    * @param id The id of the instance
    * @return An error message with a back_link or a form configuration populated with the instance information
    */

  def getInstance(org: String, domain: String, schema: String, version: String, id: String): Action[AnyContent] = Action.async { implicit request =>
    val token = request.headers.get("Authorization").getOrElse("")
    val nexusPath = NexusPath(org, domain, schema, version)
    //Check if instance is from Reconciled space
    instanceService.retrieveInstance(nexusPath, id, token).map[Result] {
      case Left(r) =>
        logger.error(s"Error: Could not fetch instance : ${nexusPath.toString()}/$id - ${r.body}")
        ResponseHelper.errorResultWithBackLink(r.status, r.headers, r.body, nexusPath, reconciledPrefix)
      case Right(instance) =>
        val instanceWithCorrectLinks = instance.modificationOfLinks(nexusEndpoint, reconciledPrefix)
        FormHelper.getFormStructure(nexusPath, instanceWithCorrectLinks.content, reconciledPrefix) match {
          case JsNull =>
            NotImplemented(
              NavigationHelper.errorMessageWithBackLink("Form not implemented", NavigationHelper.generateBackLink(nexusPath, reconciledPrefix))
            )
          case instanceForm =>
            val i = instanceForm.as[JsObject] + ("status" -> ReleaseStatus.getRandomStatus()) + ("childrenStatus" -> ReleaseStatus.getRandomChildrenStatus())
            Ok(NavigationHelper.resultWithBackLink(i, nexusPath, reconciledPrefix))
        }
    }
  }

  def getInstanceNumberOfAvailableRevisions(org: String, domain:String, datatype: String, version:String, id: String): Action[AnyContent] =
    Action.async { implicit request =>
    val token = request.headers.get("Authorization").getOrElse("")
    val nexusPath = NexusPath(org, domain, datatype, version)
    instanceService.retrieveInstance(nexusPath, id, token).flatMap[Result] {
      case Left(res) =>
        Future.successful(
          ResponseHelper.forwardResultResponse(res)
        )
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
                                     schema: String,
                                     version: String,
                                     id: String,
                                     revision: Int
                                   ): Action[AnyContent] = Action.async { implicit request =>
    val token = request.headers.get("Authorization").getOrElse("")
    val nexusPath = NexusPath(org, domain, schema, version)
    instanceService.retrieveInstance(nexusPath, id, token, List( ("rev", revision.toString) ) ).map {
      case Right(instance) =>
        val json = instance.content
        val nexusId = NexusInstance.getIdForEditor((json \ "http://hbp.eu/reconciled#original_parent" \ "@id").as[String], reconciledPrefix)
        val datatype = nexusId.splitAt(nexusId.lastIndexOf("/"))
        val originalDatatype = NexusPath(datatype._1.split("/").toList)
        FormHelper.getFormStructure(originalDatatype, json, reconciledPrefix) match {
          case JsNull =>
            NotImplemented(
              NavigationHelper.errorMessageWithBackLink("Form not implemented", NavigationHelper.generateBackLink(nexusPath, reconciledPrefix))
            )
          case instanceContent => Ok(NavigationHelper.resultWithBackLink(instanceContent.as[JsObject], nexusPath, reconciledPrefix))
        }
      case Left(response) =>
        ResponseHelper.errorResultWithBackLink(response.status, response.headers, response.body, nexusPath, reconciledPrefix)
    }

  }

  /**
    * This private method is called when it is the first time an update is done on an instance.
    * This method will create if needed the correct organizations, domains and schemas, for the editor space.
    * @param originalInstance The instance currently displayed (which is not from a reconciled space)
    * @param request The current user request
    * @return The updated instance or an error from nexus. Every response has a black link for the UI.
    */
  private def initialUpdate(originalInstance: NexusInstance)(implicit request: EditorUserRequest[AnyContent]) = {
    val token = OIDCHelper.getTokenFromRequest(request)
    val reconciledTokenFut = oIDCAuthService.getTechAccessToken()
    val instancePath = originalInstance.nexusPath
    val newValue = request.body.asJson.get
    val editorSpace = EditorSpaceHelper.getGroupName(request.editorGroup, editorPrefix)
    val reconciledSpace = EditorSpaceHelper.getGroupName(request.editorGroup, reconciledPrefix)
    reconciledTokenFut.flatMap { reconciledToken =>
      val originalInstanceCleaned = NexusInstance(removeNexusFields(originalInstance.content))
      val originLink = (originalInstance.content \ "@id").as[String]
      val (
        updatedInstance,
        updateToBeStoredInManual,
        preppedEntityForStorage
        ) = NexusEditorController.preppingEntitesForSave(
        nexusEndpoint,
        newValue,
        originalInstanceCleaned.content,
        originalInstanceCleaned,
        originalInstanceCleaned.nexusPath,
        originLink,
        request.user,
        reconciledPrefix,
        instanceService,
        token
      )

      val consolidatedInstance = buildInstanceFromForm(originalInstance.content, updateToBeStoredInManual, nexusEndpoint)
      logger.debug(s"Consolidated instance $consolidatedInstance")
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
                  ResponseHelper.errorResultWithBackLink(re.status, re.headers, re.body, instancePath, reconciledPrefix)
              }
            }
          case _ =>
            Future.successful(ResponseHelper.errorResultWithBackLink(res.status, res.headers, res.body, instancePath, reconciledPrefix) )
        }
      }
    }
  }

  /**
    * This private method is called if the instance currently being updated is from a reconciled space
    * @param currentInstanceDisplayed The instance on which the update is performed
    * @param originalPath The path of the original instance (could be from a original organization or an editor space)
    * @param request The current user request
    * @return The updated instance or an error from nexus. Every response has a black link for the UI.
    */
  private def updateWithReconciled(currentInstanceDisplayed: NexusInstance, originalPath: NexusPath)(implicit request: EditorUserRequest[AnyContent]) = {
    val token = OIDCHelper.getTokenFromRequest(request)
    val reconciledTokenFut = oIDCAuthService.getTechAccessToken()
    val newValue = request.body.asJson.get
    val editorSpace = EditorSpaceHelper.getGroupName(request.editorGroup, editorPrefix)
    val originalIdAndPath = NexusInstance.extractIdAndPath((currentInstanceDisplayed.content \ "http://hbp.eu/reconciled#original_parent").as[JsValue])
    // Get the original instance either in the Editor space or the original space
    instanceService.getInstance(originalIdAndPath._2, originalIdAndPath._1, token).flatMap {
      case Right(instanceFromOriginalSpace) =>
        val originalInstance = NexusInstance(originalIdAndPath._1, originalPath, removeNexusFields(instanceFromOriginalSpace.content))
        // Get the editor instances
        val editorInstanceIds = (currentInstanceDisplayed.content \ "http://hbp.eu/reconciled#parents").asOpt[List[JsObject]]
          .getOrElse(List())
          .map(js => NexusInstance.extractIdAndPath(js)._1)
        reconciledTokenFut.flatMap { reconciledToken =>
          instanceService.retrieveInstances(editorInstanceIds, originalPath.reconciledPath(editorPrefix), token).flatMap[Result] {
            editorInstancesRes =>
              if (editorInstancesRes.forall(_.isRight)) {
                val editorInstances = editorInstancesRes.map { e => e.toOption.get}
                val reconciledInstanceCleaned = removeNexusFields(currentInstanceDisplayed.content)
                //Create the manual update
                // As we cannot pass / in the name of a field we have replaced them with %nexus-slash%
                val reconciledLink =  (currentInstanceDisplayed.content \ "@id").as[String]
                //Generate the data that should be stored in the manual space
                val (
                  updatedInstance,
                  updateToBeStoredInManual,
                  preppedEntityForStorage
                  ) = NexusEditorController.preppingEntitesForSave(
                  nexusEndpoint,
                  newValue,
                  reconciledInstanceCleaned,
                  currentInstanceDisplayed,
                  originalPath,
                  reconciledLink,
                  request.user,
                  reconciledPrefix,
                  instanceService,
                  token
                )
                logger.debug(s"Consolidated instance $updatedInstance")
                consolidateFromManualSpace(nexusEndpoint, editorSpace, currentInstanceDisplayed, editorInstances, updateToBeStoredInManual, request.user) match {
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
                                ResponseHelper.errorResultWithBackLink(re.status, re.headers, re.body, originalPath, reconciledPrefix)
                            }
                          }
                        case _ =>
                          Future.successful(ResponseHelper.errorResultWithBackLink(res.status, res.headers, res.body, originalPath, reconciledPrefix))
                      }
                    }
                }
              } else {
                val errors = editorInstancesRes.filter(_.isLeft).map {
                  case Left(res) => Some(res)
                  case _ => None
                }.filter(_.isDefined).map(_.get)
                logger.error(s"Could not fetch editor instances ${errors}")
                Future.successful(ResponseHelper.errorResultWithBackLink(errors.head.status, errors.head.headers,errors.head.body, originalPath, reconciledPrefix))
              }
          }
        }
      case Left(res) => Future.successful( ResponseHelper.errorResultWithBackLink(res.status, res.headers, res.body, originalPath, reconciledPrefix) )
    }
  }

  /**
    * Entry point when updating an instance
    * @param org The organization of the instance
    * @param domain The domain of the instance
    * @param schema The schema of the instance
    * @param version The version of the schema
    * @param id The id of the instance
    * @return A result with the instance updated or an error message
    */
  def updateInstance(org: String,
                     domain: String,
                     schema: String,
                     version: String,
                     id: String): Action[AnyContent] =
    (authenticatedUserAction andThen EditorUserAction.editorUserAction(org)).async { implicit request =>

      val token = OIDCHelper.getTokenFromRequest(request)
      val instancePath = NexusPath(org, domain, schema, version)
      instanceService.retrieveInstance(instancePath, id, token).flatMap[Result] {
        case Left(res) =>
          Future.successful(
            ResponseHelper.errorResultWithBackLink(res.status, res.headers, res.body, instancePath, reconciledPrefix)
          )
        case Right(currentInstanceDisplayed) =>
          if(currentInstanceDisplayed.nexusPath.isReconciled(reconciledPrefix)){
            logger.debug("It is a reconciled instance")
            updateWithReconciled(currentInstanceDisplayed, instancePath)
          } else {
            initialUpdate(currentInstanceDisplayed)
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
    val instance = buildNewInstanceFromForm(nexusEndpoint, instancePath, FormHelper.formRegistry, newInstance )

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
                      val id:String = (res.json.as[JsObject] \ "@id").as[String].split("v0/data/").last.split("/").last
                      val output = res.json.as[JsObject]
                        .+("id", JsString(instancePath.toString() + "/" +id))
                        .-("@id")
                        .-("@context")
                        .-("nxv:rev")
                      Created(NavigationHelper.resultWithBackLink(output.as[JsObject], instancePath, reconciledPrefix))
                    case _ =>
                      ResponseHelper.errorResultWithBackLink(res.status, res.headers, res.body, instancePath, reconciledPrefix)
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
    * @param org The organization of the instance
    * @param domain The domain of the instance
    * @param schema The schema of the instance
    * @param version The version of the schema
    * @return
    */
  def getEmptyForm(org: String, domain: String, schema: String, version: String): Action[AnyContent] = Action {
    implicit request =>
      val nexusPath = NexusPath(org, domain, schema, version)
      val form = FormHelper.getFormStructure(nexusPath, JsNull, reconciledPrefix)
      Ok(form)
  }

  def listEntities(privateSpace: String): Action[AnyContent] = Action {
    // Editable instance types are the one for which form creation is known
    Ok(FormHelper.editableEntitiyTypes)
  }



  def graphEntities(
                     org: String, domain: String, schema: String, version: String, id:String, step: Int
                   ): Action[AnyContent] = authenticatedUserAction.async { implicit  request =>
    val token = request.headers.toSimpleMap.get("Authorization").getOrElse("")
    arangoQueryService.graphEntities(org,domain, schema, version, id, step, token).map{
      case Right(json) => Ok(json)
      case Left(response) => ResponseHelper.forwardResultResponse(response)
    }
  }


  /**
    * Retrieve up to 6 level of an instance child with their release status
    * @param org
    * @param domain
    * @param schema
    * @param version
    * @param id
    * @return
    */
  def releaseInstance(
                     org: String, domain: String, schema: String, version: String, id:String
                   ): Action[AnyContent] = Action.async { implicit request =>
    val token = request.headers.toSimpleMap("Authorization")
    val path = NexusPath(org, domain, schema, version)
    releaseService.releaseInstance(path, id, token).map{
      case Left(None) => ResponseHelper.errorResultWithBackLink(NOT_FOUND, request.headers.toMap, "Could not find main instance.", NexusPath(org, domain,schema, version), reconciledPrefix)
      case Left(Some(res)) => ResponseHelper.forwardResultResponse(res)
      case Right(data) => Ok(Json.toJson(data))
    }

  }

  def releaseStatus(): Action[AnyContent] = Action.async { implicit request =>
    request.body.asJson match {
      case Some(json) =>
        val instances = json.as[List[String]]
        val futList = instances.map { id =>
          val path = id.split("/")
          releaseService.releaseStatus(path(0), path(1), path(2), path(3), path(4))
        }
        val array: Future[List[JsObject]] = Future.sequence(futList).map {
          _.foldLeft(List[JsObject]()) {
            (arr, response) =>
              response match {
                case Left((id, res)) => arr.+:(Json.obj("id" -> id, "error" -> res.body))
                case Right(json) => arr.+:(json)
              }
          }
        }
        array.map(l => Ok(Json.toJson(l)))
      case None => Future {
        BadRequest("No data provided")
      }
    }
  }

  /**
    * Releasing a list of instance
    * @return
    */
  def releaseInstances(): Action[AnyContent] = authenticatedUserAction.async { implicit request =>
    val token = request.headers.toSimpleMap("Authorization")
    request.body.asJson.map {  jsonBody =>
      // TODO check user access right with EditorSpaceHelper.isEditorGroup()
      val releasedInstancesResult = jsonBody.as[JsArray].value.map(instanceData => nexusService.releaseInstance(instanceData, token))
      releasedInstancesResult.foldLeft(Future.successful(JsArray.empty)) {
        case (accF, resF) =>
          resF.flatMap { res =>
            accF.map { acc =>
              acc.:+(res)
            }
          }
      }.map(array => Ok(array))
    }.getOrElse(Future.successful(NoContent))
  }


}

object NexusEditorController {

  def preppingEntitesForSave(nexusEndpoint: String, formValues: JsValue, cleanInstance: JsObject, currentlyDisplayedInstance: NexusInstance, originalPath:NexusPath, originLink: String, userInfo: User, reconciledPrefix: String, instanceService: InstanceService, token:String) = {
    val updateFromUI = Json.parse(FormHelper.unescapeSlash(formValues.toString())).as[JsObject] - "id"
    val updatedInstance = buildInstanceFromForm(cleanInstance, updateFromUI, nexusEndpoint)
    val diffEntity = buildDiffEntity(currentlyDisplayedInstance, updatedInstance.toString, cleanInstance) +
      ("@type", JsString(s"http://hbp.eu/${originalPath.org}#${originalPath.schema.capitalize}"))
    val correctedLinks = Json.toJson(diffEntity.value.map{
      case (k, v) => recursiveCheckOfIds(k, v, reconciledPrefix, instanceService, token)
    }).as[JsObject]
    val preppedEntityForStorage = InstanceHelper.prepareManualEntityForStorage(correctedLinks,userInfo) +
      ("http://hbp.eu/manual#origin", JsString(originLink)) +
      ("http://hbp.eu/manual#parent", Json.obj("@id" -> JsString(originLink)))
    (updatedInstance, correctedLinks, preppedEntityForStorage)
  }

  def recursiveCheckOfIds(k: String, v: JsValue, reconciledPrefix: String, instanceService: InstanceService, token: String): (String, JsValue) = {
    if (k == "@id") {
      val url = v.as[String]
      val base = url.split("v0/data/").head
      val (id, path) = NexusInstance.extractIdAndPathFromString(url)
      val reconciledPath = path.reconciledPath(reconciledPrefix)
      val res = Await.result(instanceService.getInstance(reconciledPath, id, token), 10.seconds)
      if (res.isRight) {
        k -> JsString(s"${base}v0/data/${reconciledPath.toString()}/${id}")
      } else {
        k -> JsString(s"${base}v0/data/${path.toString()}/${id}")
      }
    } else {
      v match {
        case v if v.asOpt[JsObject].isDefined =>
          val obj = v.as[JsObject].value.map {
            case (childK, childV) =>
              recursiveCheckOfIds(childK, childV, reconciledPrefix, instanceService, token)
          }
          k -> Json.toJson(obj)
        case v if v.asOpt[JsArray].isDefined =>
          val arr = v.as[JsArray].value.map { item =>
            if (item.asOpt[JsObject].isDefined) {
              val r = item.as[JsObject].value.map {
                case (childK, childV) =>
                  recursiveCheckOfIds(childK, childV, reconciledPrefix, instanceService, token)
              }
              Json.toJson(r)
            } else {
              item
            }
          }
          k -> Json.toJson(arr)
        case v => k -> v
      }
    }

  }
}




