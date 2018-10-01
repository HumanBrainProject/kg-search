
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
import common.models.{NexusInstance, NexusPath, User}
import editor.actions.EditorUserAction
import editor.helpers._
import editor.models._
import authentication.helpers.OIDCHelper
import helpers.ResponseHelper
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
import editor.services.{EditorService, ReleaseService}
import nexus.services.NexusService
import editor.services.ArangoQueryService
import common.services.ConfigurationService

import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class NexusEditorController @Inject()(
                                       cc: ControllerComponents,
                                       authenticatedUserAction: AuthenticatedUserAction,
                                       editorService: EditorService,
                                       oIDCAuthService: OIDCAuthService,
                                       config: ConfigurationService,
                                       nexusService: NexusService,
                                       releaseService: ReleaseService,
                                       arangoQueryService: ArangoQueryService,
                                       formService: FormService,
                                       ws: WSClient
                                     )(implicit ec: ExecutionContext)
  extends AbstractController(cc) {


  val logger = Logger(this.getClass)

  def listInstances(
                     org: String,
                     domain: String,
                     datatype: String,
                     version: String,
                     from: Option[Int],
                     size: Option[Int],
                     search: String
                   ): Action[AnyContent] = authenticatedUserAction.async  { implicit request =>
    val nexusPath = NexusPath(org, domain, datatype, version)
    arangoQueryService.listInstances(nexusPath, from, size, search).map{
      case Right(json) => Ok(json)
      case Left(res) => ResponseHelper.forwardResultResponse(res)
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
    editorService.retrieveInstance(nexusPath, id, token).map[Result] {
      case Left(r) =>
        logger.error(s"Error: Could not fetch instance : ${nexusPath.toString()}/$id - ${r.body}")
        ResponseHelper.errorResultWithBackLink(r.status, r.headers, r.body, nexusPath, config.reconciledPrefix, formService)
      case Right(instance) =>
        val instanceWithCorrectLinks = instance.modificationOfLinks(config.nexusEndpoint, config.reconciledPrefix)
        formService.getFormStructure(nexusPath, instanceWithCorrectLinks.content, config.reconciledPrefix) match {
          case JsNull =>
            NotImplemented(
              NavigationHelper.errorMessageWithBackLink("Form not implemented", NavigationHelper.generateBackLink(nexusPath, config.reconciledPrefix, formService))
            )
          case instanceForm =>
            Ok(NavigationHelper.resultWithBackLink(instanceForm.as[JsObject], nexusPath, config.reconciledPrefix, formService))
        }
    }
  }

  def getInstanceNumberOfAvailableRevisions(org: String, domain:String, datatype: String, version:String, id: String): Action[AnyContent] =
    Action.async { implicit request =>
    val token = request.headers.get("Authorization").getOrElse("")
    val nexusPath = NexusPath(org, domain, datatype, version)
    editorService.retrieveInstance(nexusPath, id, token).flatMap[Result] {
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
    editorService.retrieveInstance(nexusPath, id, token, List( ("rev", revision.toString) ) ).map {
      case Right(instance) =>
        val json = instance.content
        val nexusId = NexusInstance.getIdForEditor((json \ "http://hbp.eu/reconciled#original_parent" \ "@id").as[String], config.reconciledPrefix)
        val datatype = nexusId.splitAt(nexusId.lastIndexOf("/"))
        val originalDatatype = NexusPath(datatype._1.split("/").toList)
        formService.getFormStructure(originalDatatype, json, config.reconciledPrefix) match {
          case JsNull =>
            NotImplemented(
              NavigationHelper.errorMessageWithBackLink("Form not implemented", NavigationHelper.generateBackLink(nexusPath, config.reconciledPrefix, formService))
            )
          case instanceContent => Ok(NavigationHelper.resultWithBackLink(instanceContent.as[JsObject], nexusPath, config.reconciledPrefix, formService))
        }
      case Left(response) =>
        ResponseHelper.errorResultWithBackLink(response.status, response.headers, response.body, nexusPath, config.reconciledPrefix, formService)
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
    val modificationFromUser = request.body.asJson.get


    val editorSpace = EditorSpaceHelper.getGroupName(request.editorGroup, config.editorPrefix)
    val reconciledSpace = EditorSpaceHelper.getGroupName(request.editorGroup, config.reconciledPrefix)
    reconciledTokenFut.flatMap { techToken =>
      val originalInstanceCleaned = originalInstance.removeNexusFields()
      val originalInstanceWithAllFields =  InstanceHelper.addDefaultFields( originalInstanceCleaned,instancePath, formService.formRegistry)
      val updateFromUI = Json.parse(formService.unescapeSlash(modificationFromUser.toString())).as[JsObject] - "id"
      val updatedInstance = InstanceHelper.buildInstanceFromForm(originalInstanceCleaned, updateFromUI, config.nexusEndpoint, formService)

      val updateToBeStoredInManual = NexusEditorController.preppingEntitiesForSave(
        config.nexusEndpoint,
        updatedInstance,
        originalInstanceWithAllFields,
        originalInstanceWithAllFields,
        originalInstanceCleaned.nexusPath,
        request.user,
        config.reconciledPrefix,
        config.editorPrefix,
        editorService,
        token
      )
      val consolidatedInstance = ReconciledInstance(
        InstanceHelper.buildInstanceFromForm(originalInstance, updateToBeStoredInManual.nexusInstance.content, config.nexusEndpoint, formService)
      )
      logger.debug(s"Consolidated instance $consolidatedInstance")

      val createDomains = editorService.createDomainsAndSchemasSync(editorSpace, reconciledSpace, originalInstance.nexusPath, token, techToken)

      createDomains.flatMap {
        case (true, true) =>
          editorService.saveEditorUpdate(
            editorSpace,
            reconciledSpace,
            request.user,
            updateToBeStoredInManual,
            consolidatedInstance,
            originalInstance,
            true,
            None,
            token,
            techToken
          ).map {
            case Left(res) =>
              ResponseHelper.errorResultWithBackLink(res.status, res.headers, res.body, instancePath, config.reconciledPrefix, formService)
            case Right(instance) =>
              Ok(NavigationHelper.resultWithBackLink(instance.formatFromNexusToOption(config.reconciledPrefix), instancePath, config.reconciledPrefix, formService))
          }
        case _ =>
          Future {
            InternalServerError(s"An error occurred while creating a schema for the instance - ${originalInstance.id()}")
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
  private def updateWithReconciled(
                                    currentInstanceDisplayed: ReconciledInstance,
                                    originalPath: NexusPath
                                  )(implicit request: EditorUserRequest[AnyContent]) = {
    val token = OIDCHelper.getTokenFromRequest(request)
    val reconciledTokenFut = oIDCAuthService.getTechAccessToken()
    val modificationFromUser = request.body.asJson.get
    val editorSpace = EditorSpaceHelper.getGroupName(request.editorGroup, config.editorPrefix)
    val reconciledSpace = EditorSpaceHelper.getGroupName(request.editorGroup, config.reconciledPrefix)
    val originalIdAndPath = NexusInstance.extractIdAndPath(currentInstanceDisplayed.getOriginalParent())
    // Get the original instance either in the Editor space or the original space
    editorService.getInstance(originalIdAndPath._2, originalIdAndPath._1, token).flatMap {
      case Right(instanceFromOriginalSpace) =>
        val originalInstance =  instanceFromOriginalSpace.removeNexusFields().copy(nexusUUID = Some(originalIdAndPath._1), nexusPath = originalIdAndPath._2)
        // Get the editor instances
        val editorInstanceIds = currentInstanceDisplayed.getEditorInstanceIds()
          .getOrElse(List())
          .map(js => NexusInstance.extractIdAndPath(js)._1)
        reconciledTokenFut.flatMap { techToken =>
          editorService.retrieveInstances(editorInstanceIds, originalPath.reconciledPath(config.editorPrefix), token).flatMap[Result] {
            editorInstancesRes =>
              if (editorInstancesRes.forall(_.isRight)) {
                val editorInstances = editorInstancesRes.map { e => EditorInstance(e.toOption.get)}
                val reconciledInstanceCleaned = currentInstanceDisplayed.removeNexusFields()
                val updateFromUI = Json.parse(formService.unescapeSlash(modificationFromUser.toString())).as[JsObject] - "id"
                val updatedInstance = InstanceHelper.buildInstanceFromForm(reconciledInstanceCleaned.nexusInstance, updateFromUI, config.nexusEndpoint, formService)
                val reconciledInstanceWithAllFields = reconciledInstanceCleaned.copy(
                  InstanceHelper.addDefaultFields(reconciledInstanceCleaned.nexusInstance, originalPath, formService.formRegistry)
                )
                val currentInstanceDisplayedWithAllFields = currentInstanceDisplayed.copy(
                  InstanceHelper.addDefaultFields(currentInstanceDisplayed.nexusInstance, originalPath, formService.formRegistry)
                )
                //Create the manual update
                // As we cannot pass / in the name of a field we have replaced them with %nexus-slash%
                //Generate the data that should be stored in the manual space
                val updateToBeStoredInManual = NexusEditorController.preppingEntitiesForSave(
                  config.nexusEndpoint,
                  updatedInstance,
                  reconciledInstanceWithAllFields.nexusInstance,
                  currentInstanceDisplayedWithAllFields.nexusInstance,
                  originalPath,
                  request.user,
                  config.reconciledPrefix,
                  config.editorPrefix,
                  editorService,
                  token
                )
                logger.debug(s"Consolidated instance $updatedInstance")
                InstanceHelper.consolidateFromManualSpace(
                  config.nexusEndpoint,
                  config.reconciledPrefix,
                  currentInstanceDisplayedWithAllFields.nexusInstance,
                  editorInstances,
                  updateToBeStoredInManual,
                  request.user
                ) match {
                  case (consolidatedInstance, manualEntitiesDetailsOpt) =>

                    editorService.saveEditorUpdate(
                      editorSpace,
                      reconciledSpace,
                      request.user,
                      updateToBeStoredInManual,
                      consolidatedInstance,
                      originalInstance,
                      false,
                      manualEntitiesDetailsOpt,
                      token,
                      techToken
                    ).map{
                      case Left(res) =>
                        ResponseHelper.errorResultWithBackLink(res.status, res.headers, res.body, originalPath, config.reconciledPrefix, formService)
                      case Right(instance) =>
                        Ok(
                          NavigationHelper
                            .resultWithBackLink(
                              instance.formatFromNexusToOption(config.reconciledPrefix),
                              originalPath,
                              config.reconciledPrefix,
                              formService
                            )
                        )
                    }
                }
              } else {
                val errors = editorInstancesRes.filter(_.isLeft).map {
                  case Left(res) => Some(res)
                  case _ => None
                }.filter(_.isDefined).map(_.get)
                logger.error(s"Could not fetch editor instances ${errors}")
                Future.successful(ResponseHelper.errorResultWithBackLink(errors.head.status, errors.head.headers,errors.head.body, originalPath, config.reconciledPrefix, formService))
              }
          }
        }
      case Left(res) => Future.successful( ResponseHelper.errorResultWithBackLink(res.status, res.headers, res.body, originalPath, config.reconciledPrefix, formService) )
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
      editorService.retrieveInstance(instancePath, id, token).flatMap[Result] {
        case Left(res) =>
          Future.successful(
            ResponseHelper.errorResultWithBackLink(res.status, res.headers, res.body, instancePath, config.reconciledPrefix, formService)
          )
        case Right(currentInstanceDisplayed) =>
          if(currentInstanceDisplayed.nexusPath.isReconciled(config.reconciledPrefix)){
            logger.debug("It is a reconciled instance")
            updateWithReconciled(ReconciledInstance(currentInstanceDisplayed), instancePath)
          } else {
            initialUpdate(currentInstanceDisplayed)
          }
      }
    }

  /**
    * Creation of a new instance in the editor space and its reconciled version
    * @param org The organization of the instance
    * @param domain The domain of the instance
    * @param schema The schema of the instance
    * @param version The version of the schema
    * @return 201 Created
    */
  def createInstance(
                      org: String,
                      domain:String,
                      schema: String,
                      version:String
                    ): Action[AnyContent] = (authenticatedUserAction andThen EditorUserAction.editorUserAction(org)).async { implicit request =>
    val newInstance = request.body.asJson.get.as[JsObject]
    val instancePath = NexusPath(org, domain, schema, version)
    val editorPath = instancePath.reconciledPath(config.editorPrefix)
    val instance = InstanceHelper.buildNewInstanceFromForm(config.nexusEndpoint, instancePath, formService.formRegistry, newInstance, formService)

    val token = OIDCHelper.getTokenFromRequest(request)
    val reconciledTokenFut = oIDCAuthService.getTechAccessToken()
    val editorSpace = EditorSpaceHelper.getGroupName(request.editorGroup, config.editorPrefix)
    val reconciledSpace = EditorSpaceHelper.getGroupName(request.editorGroup, config.reconciledPrefix)
    import java.util.UUID.randomUUID
    val identifier = randomUUID().toString
    val originalPath = instancePath.toString()
    val typedInstance: EditorInstance = EditorInstance.generateInstance(NexusInstance(None, editorPath, instance), org, schema, identifier, originalPath)

    // Save instance to nexus
    reconciledTokenFut.flatMap { techToken =>
      val createSchemasAndDomain = editorService.createDomainsAndSchemasSync(editorSpace, reconciledSpace, instancePath, token, techToken)
      createSchemasAndDomain.flatMap { res =>
        logger.debug(res.toString)
        val insertNewInstance = editorService.insertInstance(editorSpace, typedInstance.nexusInstance, instancePath, token)
        insertNewInstance.flatMap { instance =>
          logger.debug(instance.body)
          val reconciledInstance = ReconciledInstance(typedInstance.nexusInstance)
            .addReconciledMandatoryFields(instancePath, request.user, (instance.json \ "@id").as[String])
          editorService
            .insertInstance(reconciledSpace, reconciledInstance.nexusInstance, instancePath, techToken).map { res =>
            res.status match {
              case CREATED => // reformat ouput to keep it consistent with update
                val id: String = (res.json.as[JsObject] \ "@id").as[String].split("v0/data/").last.split("/").last
                val output = res.json.as[JsObject]
                  .+("id", JsString(instancePath.toString() + "/" + id))
                  .-("@id")
                  .-("@context")
                  .-("nxv:rev")
                Created(NavigationHelper.resultWithBackLink(output.as[JsObject], instancePath, config.reconciledPrefix, formService))
              case _ =>
                logger.error(res.body)
                ResponseHelper.errorResultWithBackLink(res.status, res.headers, res.body, instancePath, config.reconciledPrefix, formService)

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
    * @return 200
    */
  def getEmptyForm(org: String, domain: String, schema: String, version: String): Action[AnyContent] = Action {
    implicit request =>
      val nexusPath = NexusPath(org, domain, schema, version)
      val form = formService.getFormStructure(nexusPath, JsNull, config.reconciledPrefix)
      Ok(form)
  }

  /**
    *  Return the list of entity types available for the editor
    * @param privateSpace
    * @return 200
    */
  def listEditableEntityTypes(privateSpace: String): Action[AnyContent] = Action {
    // Editable instance types are the one for which form creation is known
    Ok(formService.editableEntitiyTypes)
  }


  /**
    * @param org The organization of the instance
    * @param domain The domain of the instance
    * @param schema The schema of the instance
    * @param version The version of the schema
    * @param id The id of the instance
    * @param step  The depth of the graph
    * @return
    */
  def graphEntities(
                     org: String, domain: String, schema: String, version: String, id:String, step: Int
                   ): Action[AnyContent] = authenticatedUserAction.async { implicit  request =>
    val token = request.headers.toSimpleMap.getOrElse("Authorization", "")
    arangoQueryService.graphEntities(org,domain, schema, version, id, step, token).map{
      case Right(json) => Ok(json)
      case Left(response) => ResponseHelper.forwardResultResponse(response)
    }
  }


  /**
    * Retrieve up to 6 level of an instance child with their release status
    * @param org The organization of the instance
    * @param domain The domain of the instance
    * @param schema The schema of the instance
    * @param version The version of the schema
    * @param id The id of the instance
    * @return
    */
  def getInstanceAndChildrenReleaseStatus(
                     org: String, domain: String, schema: String, version: String, id:String
                   ): Action[AnyContent] = Action.async { implicit request =>
    val token = request.headers.toSimpleMap("Authorization")
    val path = NexusPath(org, domain, schema, version)
    releaseService.releaseInstance(path, id, token).map{
      case Left(None) => ResponseHelper.errorResultWithBackLink(NOT_FOUND,
        request.headers.toMap,
        "Could not find main instance.",
        NexusPath(org, domain,schema, version),
        config.reconciledPrefix,
        formService
      )
      case Left(Some(res)) => ResponseHelper.forwardResultResponse(res)
      case Right(data) =>
        Ok(Json.toJson(data))
    }

  }

  /**
    * Retrieve the release status of a list of instances
    * @return An array with the id of the instance and its release status
    */
  def getReleaseStatus(): Action[AnyContent] = Action.async { implicit request =>
    request.body.asJson match {
      case Some(json) =>
        val instances = json.as[List[String]]
        val futList = instances.map { url =>
          val (path, id )= url.splitAt(url.lastIndexOf("/"))
          releaseService.releaseStatus(NexusPath(path.split("/").toSeq), id.replaceFirst("/", ""))
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

  def preppingEntitiesForSave(
                               nexusEndpoint: String,
                               updatedInstance: NexusInstance,
                               cleanInstance: NexusInstance,
                               currentlyDisplayedInstance: NexusInstance,
                               originalPath: NexusPath,
                               userInfo: User,
                               reconciledPrefix: String,
                               editorPrefix: String,
                               instanceService: EditorService,
                               token: String
                            ): EditorInstance = {
    val entityType = s"http://hbp.eu/${originalPath.org}#${originalPath.schema.capitalize}"
    val diffEntity = InstanceHelper.buildDiffEntity(currentlyDisplayedInstance, updatedInstance)
    val correctedLinks = EditorInstance(
      NexusInstance(
        None, originalPath.reconciledPath(editorPrefix), Json.toJson(diffEntity.value.map {
          case (k, v) => recursiveCheckOfIds(k, v, reconciledPrefix, instanceService, token)
        }).as[JsObject]
      )
    ).prepareManualEntityForStorage(
      userInfo,
      s"$nexusEndpoint/v0/data/${currentlyDisplayedInstance.id().get}",
      entityType
    )
    correctedLinks.cleanReconciledFields()
  }

  def recursiveCheckOfIds(k: String,
                          v: JsValue,
                          reconciledPrefix: String,
                          instanceService: EditorService,
                          token: String
                         ): (String, JsValue) = {
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




