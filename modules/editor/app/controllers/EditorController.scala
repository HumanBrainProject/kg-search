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
import cats.data.EitherT
import constants.SchemaFieldsConstants
import helpers._
import javax.inject.{Inject, Singleton}
import models._
import models.instance._
import monix.eval.Task
import play.api.Logger
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc._
import services._
import services.specification.FormService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EditorController @Inject()(
  cc: ControllerComponents,
  authenticatedUserAction: AuthenticatedUserAction,
  editorService: EditorService,
  oIDCAuthService: OIDCAuthService,
  config: ConfigurationService,
  nexusService: NexusService,
  iAMAuthService: IAMAuthService,
  formService: FormService,
  metadataService: MetadataService,
  reverseLinkService: ReverseLinkService
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  val logger = Logger(this.getClass)

  implicit val s = monix.execution.Scheduler.Implicits.global

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

  def getInstance(
    org: String,
    domain: String,
    schema: String,
    version: String,
    id: String,
    databaseScope: Option[String]
  ): Action[AnyContent] =
    authenticatedUserAction.async { implicit request =>
      val nexusInstanceReference = NexusInstanceReference(org, domain, schema, version, id)
      formService
        .getRegistries()
        .flatMap { registries =>
          editorService
            .retrieveInstance(
              nexusInstanceReference,
              request.userToken,
              registries.queryRegistry,
              databaseScope
            )
            .flatMap {
              case Left(error) =>
                logger.error(
                  s"Error: Could not fetch instance : ${nexusInstanceReference.nexusPath.toString()}/$id - ${error.content}"
                )
                Task.pure(error.toResult)
              case Right(instance) =>
                FormService
                  .getFormStructure(
                    nexusInstanceReference.nexusPath,
                    instance.content,
                    registries.formRegistry
                  ) match {
                  case JsNull =>
                    Task.pure(NotImplemented("Form not implemented"))
                  case instanceForm =>
                    metadataService.getMetadata(nexusInstanceReference, instance).map {
                      case Right(metadata) =>
                        Ok(
                          Json.toJson(
                            EditorResponseObject(
                              instanceForm.as[JsObject] ++ Json.obj("metadata" -> Json.toJson(metadata))
                            )
                          )
                        )
                      case Left(error) =>
                        logger.error(error.toString)
                        Ok(Json.toJson(EditorResponseObject(instanceForm.as[JsObject])))
                    }
                }
            }
        }
        .runToFuture
    }

  def deleteInstance(org: String, domain: String, schema: String, version: String, id: String): Action[AnyContent] =
    authenticatedUserAction.async { implicit request =>
      val nexusInstanceReference = NexusInstanceReference(org, domain, schema, version, id)
      editorService
        .deleteInstance(nexusInstanceReference, request.userToken)
        .map {
          case Right(()) => Ok("Instance has been deleted")
          case Left(err) => err.toResult
        }
        .runToFuture
    }

  def getInstancesByIds(allFields: Boolean): Action[AnyContent] = authenticatedUserAction.async { implicit request =>
    val listOfIds = for {
      bodyContent <- request.body.asJson
      ids         <- bodyContent.asOpt[List[String]]
    } yield ids.map(NexusInstanceReference.fromUrl)
    formService
      .getRegistries()
      .flatMap { registries =>
        listOfIds match {
          case Some(ids) =>
            editorService
              .retrieveInstancesByIds(ids, request.userToken, if (allFields) "editorFull" else "editorPreview")
              .map {
                case Left(err) => err.toResult
                case Right(ls) =>
                  if (allFields) {
                    Ok(Json.toJson(EditorResponseObject(Json.toJson(ls.groupBy(_.id().get).map {
                      case (k, v) =>
                        FormService
                          .getFormStructure(v.head.nexusPath, v.head.content, registries.formRegistry)
                          .as[JsObject]
                    }))))
                  } else {
                    Ok(Json.toJson(ls.map(i => i.content.as[PreviewInstance])))
                  }
              }
          case None => Task.pure(BadRequest("Missing body content"))
        }
      }
      .runToFuture
  }

  def getInstanceNumberOfAvailableRevisions(
    org: String,
    domain: String,
    datatype: String,
    version: String,
    id: String
  ): Action[AnyContent] =
    authenticatedUserAction.async { implicit request =>
      val nexusInstanceRef = NexusInstanceReference(org, domain, datatype, version, id)
      formService
        .getRegistries()
        .flatMap { registries =>
          editorService
            .retrieveInstance(nexusInstanceRef, request.userToken, registries.queryRegistry)
            .flatMap[Result] {
              case Left(error) =>
                Task.pure(
                  error.toResult
                )
              case Right(originalInstance) =>
                val nbRevision = (originalInstance.content \ "nxv:rev").as[JsNumber]
                Task.pure(Ok(Json.obj("available_revisions" -> nbRevision, "path" -> id)))
            }
        }
        .runToFuture
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
  def updateInstance(org: String, domain: String, schema: String, version: String, id: String): Action[AnyContent] =
    (authenticatedUserAction andThen EditorUserAction.editorUserWriteAction(org, config.editorPrefix, iAMAuthService))
      .async { implicit request =>
        val instanceRef = NexusInstanceReference(org, domain, schema, version, id)
        editorService
          .updateInstanceFromForm(instanceRef, request.body.asJson, request.user, request.userToken, reverseLinkService)
          .flatMap {
            case Right(()) =>
              formService.getRegistries().flatMap { registries =>
                editorService
                  .retrieveInstance(instanceRef, request.userToken, registries.queryRegistry)
                  .flatMap {
                    case Right(instance) =>
                      FormService
                        .getFormStructure(
                          instanceRef.nexusPath,
                          instance.content,
                          registries.formRegistry
                        ) match {
                        case JsNull =>
                          Task.pure(NotImplemented("Form not implemented"))
                        case instanceForm =>
                          val specFlush = formService.shouldReloadSpecification(instanceRef.nexusPath).flatMap {
                            shouldReload =>
                              if (shouldReload) {
                                formService.flushSpec()
                              } else {
                                Task.pure(())
                              }
                          }
                          specFlush.map { _ =>
                            Ok(
                              Json.toJson(
                                EditorResponseObject(instanceForm.as[JsObject])
                              )
                            )
                          }
                      }
                    case Left(error) =>
                      logger.error(error.toString)
                      Task.pure(error.toResult)
                  }
              }
            case Left(error) =>
              logger.error(error.content.mkString("\n"))
              Task.pure(error.toResult)
          }
          .runToFuture
      }

  /**
    * Creation of a new instance in the editor
    * @param org The organization of the instance
    * @param domain The domain of the instance
    * @param schema The schema of the instance
    * @param version The version of the schema
    * @return 201 Created
    */
  def createInstance(
    org: String,
    domain: String,
    schema: String,
    version: String
  ): Action[AnyContent] =
    (authenticatedUserAction andThen EditorUserAction.editorUserWriteAction(org, config.editorPrefix, iAMAuthService))
      .async { implicit request =>
        val instancePath = NexusPath(org, domain, schema, version)
        editorService
          .insertInstance(NexusInstance(None, instancePath, Json.obj()), Some(request.user), request.userToken)
          .flatMap[Result] {
            case Left(error) => Task.pure(error.toResult)
            case Right(ref) =>
              request.body.asJson match {
                case Some(content) =>
                  formService.getRegistries().flatMap { registries =>
                    val nonEmptyInstance = FormService.buildNewInstanceFromForm(
                      ref,
                      config.nexusEndpoint,
                      content.as[JsObject],
                      registries.formRegistry
                    )
                    editorService
                      .updateInstance(nonEmptyInstance, ref, request.userToken, request.user.id)
                      .flatMap[Result] {
                        case Right(()) =>
                          val specFlush = formService.shouldReloadSpecification(instancePath).flatMap { shouldReload =>
                            if (shouldReload) {
                              formService.flushSpec()
                            } else {
                              Task.pure(())
                            }
                          }
                          specFlush.map { _ =>
                            Created(Json.toJson(EditorResponseObject(Json.toJson(ref))))
                          }
                        case Left(error) => Task.pure(error.toResult)
                      }
                  }
                case None => Task.pure(Created(Json.toJson(EditorResponseObject(Json.toJson(ref)))))
              }
          }
          .runToFuture
      }

  /**
    * Returns an empty form for a specific instance type
    * @param org The organization of the instance
    * @param domain The domain of the instance
    * @param schema The schema of the instance
    * @param version The version of the schema
    * @return 200
    */
  def getEmptyForm(org: String, domain: String, schema: String, version: String): Action[AnyContent] =
    authenticatedUserAction.async { implicit request =>
      val nexusPath = NexusPath(org, domain, schema, version)
      formService
        .getRegistries()
        .map { registries =>
          val form = FormService.getFormStructure(nexusPath, JsNull, registries.formRegistry)
          Ok(form)
        }
        .runToFuture
    }

}
