
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

package controllers

import actions.EditorUserAction
import helpers._
import javax.inject.{Inject, Singleton}
import models._
import models.instance.{EditorInstance, NexusInstance, ReconciledInstance}
import models.user.EditorUserWriteRequest
import play.api.Logger
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc._
import services._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NexusEditorController @Inject()(
                                       cc: ControllerComponents,
                                       authenticatedUserAction: AuthenticatedUserAction,
                                       editorService: EditorService,
                                       oIDCAuthService: OIDCAuthService,
                                       config: ConfigurationService,
                                       nexusService: NexusService,
                                       arangoQueryService: ArangoQueryService,
                                       iAMAuthService: IAMAuthService,
                                       formService: FormService,
                                       ws: WSClient
                                     )(implicit ec: ExecutionContext)
  extends AbstractController(cc) {


  val logger = Logger(this.getClass)

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
        EditorResponseHelper.errorResultWithBackLink(r.status, r.headers, r.body, nexusPath, config.reconciledPrefix, formService)
      case Right(instance) =>
//        val instanceWithCorrectLinks = instance.modificationOfLinks(config.nexusEndpoint, config.reconciledPrefix)
        FormService.getFormStructure(nexusPath, instance.content, config.reconciledPrefix, formService.formRegistry) match {
          case JsNull =>
            NotImplemented(
              NavigationHelper.errorMessageWithBackLink(
                "Form not implemented",
                NavigationHelper.generateBackLink(nexusPath, config.reconciledPrefix, formService)
              )
            )
          case instanceForm =>
            Ok(NavigationHelper.resultWithBackLink(EditorResponseObject(instanceForm.as[JsObject]), nexusPath, config.reconciledPrefix, formService))
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
          EditorResponseHelper.forwardResultResponse(res)
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
    editorService.retrieveInstance(nexusPath, id, token, List(("fields", "all"),("deprecated", "false"),("rev", revision.toString))).map {
      case Right(instance) =>
        val json = instance.content
        val nexusId = NexusInstance.getIdForEditor((json \ "http://hbp.eu/reconciled#original_parent" \ "@id").as[String], config.reconciledPrefix)
        val datatype = nexusId.splitAt(nexusId.lastIndexOf("/"))
        val originalDatatype = NexusPath(datatype._1.split("/").toList)
        FormService.getFormStructure(originalDatatype, json, config.reconciledPrefix, formService.formRegistry) match {
          case JsNull =>
            NotImplemented(
              NavigationHelper.errorMessageWithBackLink(
                "Form not implemented",
                NavigationHelper.generateBackLink(nexusPath, config.reconciledPrefix, formService)
              )
            )
          case instanceContent => Ok(
            NavigationHelper.resultWithBackLink(
              EditorResponseObject(instanceContent),
              nexusPath,
              config.reconciledPrefix,
              formService
            )
          )
        }
      case Left(response) =>
        EditorResponseHelper.errorResultWithBackLink(response.status, response.headers, response.body, nexusPath, config.reconciledPrefix, formService)
    }

  }

  /**
    * This private method is called when it is the first time an update is done on an instance.
    * This method will create if needed the correct organizations, domains and schemas, for the editor space.
    * @param originalInstance The instance currently displayed (which is not from a reconciled space)
    * @param request The current user request
    * @return The updated instance or an error from nexus. Every response has a black link for the UI.
    */
  private def initialUpdate(originalInstance: NexusInstance)(implicit request: EditorUserWriteRequest[AnyContent]) = {
    val token = OIDCHelper.getTokenFromRequest(request)
    val reconciledTokenFut = oIDCAuthService.getTechAccessToken()
    val instancePath = originalInstance.nexusPath
    val editorSpace = EditorSpaceHelper.getGroupName(request.editorGroup, config.editorPrefix)
    val reconciledSpace = EditorSpaceHelper.getGroupName(request.editorGroup, config.reconciledPrefix)
    reconciledTokenFut.flatMap { techToken =>
      val originalInstanceWithAllFields =  InstanceHelper.addDefaultFields(originalInstance.removeNexusFields(), instancePath, formService.formRegistry)
      val updatedInstance = FormService.buildInstanceFromForm(originalInstanceWithAllFields, request.body.asJson.get, config.nexusEndpoint)
      val updateToBeStoredInManual = editorService.preppingEntitiesForSave(
        updatedInstance,
        originalInstanceWithAllFields,
        originalInstanceWithAllFields.id().get,
        originalInstanceWithAllFields.nexusPath,
        request.user,
        token
      )
      val consolidatedInstance = ReconciledInstance(
        FormService.buildInstanceFromForm(originalInstance, updateToBeStoredInManual.nexusInstance.content, config.nexusEndpoint)
      )
      editorService
        .createDomainsAndSchemasSync(editorSpace, reconciledSpace, originalInstance.nexusPath, token, techToken)
        .flatMap {
        case (true, true) =>
          editorService.saveEditorUpdate(
            editorSpace,
            reconciledSpace,
            request.user,
            updateToBeStoredInManual,
            consolidatedInstance,
            originalInstance,
            instancePath,
            true,
            None,
            token,
            techToken
          ).map {
            case Left(res) =>
              EditorResponseHelper.errorResultWithBackLink(res.status, res.headers, res.body, instancePath, config.reconciledPrefix, formService)
            case Right(instance) =>
              Ok(
                NavigationHelper
                  .resultWithBackLink(EditorResponseObject(instance.formatFromNexusToOption(config.reconciledPrefix)), instancePath, config.reconciledPrefix, formService)
              )
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
                                  )(implicit request: EditorUserWriteRequest[AnyContent]) = {
    val token = OIDCHelper.getTokenFromRequest(request)
    val editorSpace = EditorSpaceHelper.getGroupName(request.editorGroup, config.editorPrefix)
    val reconciledSpace = EditorSpaceHelper.getGroupName(request.editorGroup, config.reconciledPrefix)
    val originalIdAndPath = currentInstanceDisplayed.getOriginalParent().splitAt(currentInstanceDisplayed.getOriginalParent().lastIndexOf("/"))
    // Get the original instance either in the Editor space or the original space
    nexusService.getInstance(NexusPath(originalIdAndPath._1), originalIdAndPath._2, token).flatMap {
      case Right(instanceFromOriginalSpace) =>
        val originalInstance =  instanceFromOriginalSpace.removeNexusFields().copy(nexusUUID = Some(originalIdAndPath._2), nexusPath = NexusPath(originalIdAndPath._1))
        // Get the editor instances
        val editorInstanceIds = currentInstanceDisplayed.getEditorInstanceIds()
          .getOrElse(List())
          .map(js => NexusInstance.extractIdAndPath(js)._1)
        oIDCAuthService.getTechAccessToken().flatMap { techToken =>
          nexusService.retrieveInstances(editorInstanceIds, originalPath.reconciledPath(config.editorPrefix), token).flatMap[Result] {
            editorInstancesRes =>
              if (editorInstancesRes.forall(_.isRight)) {
                val editorInstances = editorInstancesRes.map { e => EditorInstance(e.toOption.get)}
                val reconciledInstanceCleaned = currentInstanceDisplayed.removeNexusFields()
                val updatedInstance = FormService.buildInstanceFromForm(reconciledInstanceCleaned.nexusInstance, request.body.asJson.get, config.nexusEndpoint)
                val currentInstanceDisplayedWithAllFields = currentInstanceDisplayed.copy(
                  InstanceHelper.addDefaultFields(currentInstanceDisplayed.nexusInstance, originalPath, formService.formRegistry)
                )

                //Generate the data that should be stored in the manual space
                val updateToBeStoredInManual = editorService.preppingEntitiesForSave(
                  updatedInstance,
                  currentInstanceDisplayedWithAllFields.nexusInstance,
                  s"${originalIdAndPath._2.toString()}/${originalIdAndPath._1}",
                  originalPath,
                  request.user,
                  token
                )
                val mergedUpdateWithPreviousVersion = editorInstances.find(_.updaterId == request.user.id).map{ instance =>
                  instance.mergeContent(updateToBeStoredInManual)
                }.getOrElse(updateToBeStoredInManual)
                val listOfEditorInstance = mergedUpdateWithPreviousVersion :: editorInstances.filter(_.updaterId != request.user.id)
                logger.debug(s"Consolidated instance $updatedInstance")
                InstanceHelper.generateInstanceWithReconciliationLogic(
                  config.nexusEndpoint,
                  config.reconciledPrefix,
                  currentInstanceDisplayedWithAllFields.nexusInstance,
                  listOfEditorInstance
                ) match {
                  case (consolidatedInstance, manualEntitiesDetailsOpt) =>

                    editorService.saveEditorUpdate(
                      editorSpace,
                      reconciledSpace,
                      request.user,
                      mergedUpdateWithPreviousVersion,
                      consolidatedInstance,
                      originalInstance,
                      originalPath,
                      false,
                      manualEntitiesDetailsOpt,
                      token,
                      techToken
                    ).map{
                      case Left(res) =>
                        EditorResponseHelper.errorResultWithBackLink(res.status, res.headers, res.body, originalPath, config.reconciledPrefix, formService)
                      case Right(instance) =>
                        Ok(
                          NavigationHelper
                            .resultWithBackLink(
                              EditorResponseObject(instance.formatFromNexusToOption(config.reconciledPrefix)),
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
                Future.successful(
                  EditorResponseHelper
                    .errorResultWithBackLink(
                      errors.head.status,
                      errors.head.headers,
                      errors.head.body,
                      originalPath,
                      config.reconciledPrefix,
                      formService
                    )
                )
              }
          }
        }
      case Left(res) => Future.successful(
        EditorResponseHelper
          .errorResultWithBackLink(res.status, res.headers, res.body, originalPath, config.reconciledPrefix, formService) )
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
    (authenticatedUserAction andThen EditorUserAction.editorUserWriteAction(org,config.editorPrefix, iAMAuthService)).async { implicit request =>

      val token = OIDCHelper.getTokenFromRequest(request)
      val instancePath = NexusPath(org, domain, schema, version)
      editorService.retrieveInstance(instancePath, id, token).flatMap[Result] {
        case Left(res) =>
          Future.successful(
            EditorResponseHelper.errorResultWithBackLink(res.status, res.headers, res.body, instancePath, config.reconciledPrefix, formService)
          )
        case Right(currentInstanceDisplayed) =>
//          if(currentInstanceDisplayed.nexusPath.isReconciled(config.reconciledPrefix)){
//            logger.debug("It is a reconciled instance")
            updateWithReconciled(ReconciledInstance(currentInstanceDisplayed), instancePath)
//          } else {
//            initialUpdate(currentInstanceDisplayed)
//          }
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
                    ): Action[AnyContent] = (authenticatedUserAction andThen EditorUserAction.editorUserWriteAction(org, config.editorPrefix, iAMAuthService)).async { implicit request =>
    val newInstance = request.body.asJson.get.as[JsObject]
    val instancePath = NexusPath(org, domain, schema, version)
    val editorPath = instancePath.reconciledPath(config.editorPrefix)
    val instance = FormService.buildNewInstanceFromForm(config.nexusEndpoint, instancePath, newInstance, formService.formRegistry)

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
                Created(NavigationHelper.resultWithBackLink(EditorResponseObject(output), instancePath, config.reconciledPrefix, formService))
              case _ =>
                logger.error(res.body)
                EditorResponseHelper.errorResultWithBackLink(res.status, res.headers, res.body, instancePath, config.reconciledPrefix, formService)

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
  def getEmptyForm(org: String, domain: String, schema: String, version: String): Action[AnyContent] = authenticatedUserAction {
    implicit request =>
      val nexusPath = NexusPath(org, domain, schema, version)
      val form = FormService.getFormStructure(nexusPath, JsNull, config.reconciledPrefix, formService.formRegistry)
      Ok(form)
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
      case Left(response) => EditorResponseHelper.forwardResultResponse(response)
    }
  }


}


