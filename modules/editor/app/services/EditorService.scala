
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

package services

import com.google.inject.Inject
import constants.{EditorClient, EditorConstants, JsonLDConstants, UiConstants}
import helpers._
import models.errors.APIEditorError
import play.api.http.Status._
import models.instance.{EditorInstance, NexusInstance, NexusInstanceReference, PreviewInstance}
import models.user.User
import models.{FormRegistry, NexusPath}
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSClient, WSResponse}
import services.instance.InstanceApiService
import services.query.QueryService

import scala.concurrent.{ExecutionContext, Future}

class EditorService @Inject()(
                               wSClient: WSClient,
                               config: ConfigurationService,
                               formService: FormService
                             )(implicit executionContext: ExecutionContext) {

  val logger = Logger(this.getClass)

  object instanceApiService extends InstanceApiService
  object queryService extends QueryService

  def insertInstance(
                      newInstance: NexusInstance,
                      token: String
                    ): Future[Either[APIEditorError, NexusInstanceReference]] = {
    val modifiedContent = FormService.removeClientKeysCorrectLinks(newInstance.content.as[JsValue], config.nexusEndpoint)
    instanceApiService.post(wSClient, config.kgQueryEndpoint, newInstance.copy(content = modifiedContent), token).map{
      case Right(ref) => Right(ref)
      case Left(res) => Left(APIEditorError(res.status, res.body))
    }
  }

  /**
    * Updating an instance
    *
    * @param diffInstance           The diff of the current instance and its modification
    * @param nexusInstanceReference The reference of the instance to update
    * @param token                  the user token
    * @param userId                 the id of the user sending the update
    * @return The updated instance
    */
  def updateInstance(
                      diffInstance: EditorInstance,
                      nexusInstanceReference: NexusInstanceReference,
                      token: String,
                      userId: String
                    ): Future[Either[APIEditorError, Unit]] = {
    instanceApiService.put(wSClient, config.kgQueryEndpoint, nexusInstanceReference, diffInstance, token, userId).map{
      case Left(res) => Left(APIEditorError(res.status, res.body))
      case Right(()) => Right(())
    }
  }

  /**
    * Return a instance by its nexus ID
    * Starting by checking if this instance is coming from a reconciled space.
    * Otherwise we try to return the instance from the original organization
    *
    * @param nexusInstanceReference The reference to the instace to retrieve
    * @param token                  The user access token
    * @return An error response or an the instance
    */
  def retrieveInstance(nexusInstanceReference: NexusInstanceReference, token: String): Future[Either[APIEditorError, NexusInstance]] = {
    instanceApiService.get(wSClient, config.kgQueryEndpoint, nexusInstanceReference, token).map{
      case Left(res) => Left(APIEditorError(res.status, res.body))
      case Right(instance) => Right(instance)
    }
  }

  def generateDiffAndUpdateInstance(
                                     instanceRef:NexusInstanceReference,
                                     updateFromUser: JsValue,
                                     token:String,
                                     user: User,
                                     formRegistry: FormRegistry
                                   ): Future[Either[APIEditorError, Unit]] = {
    retrieveInstance(instanceRef, token).flatMap {
      case Left(error) =>
        Future(Left(error))
      case Right(currentInstanceDisplayed) =>
        val cleanedOriginalInstance = InstanceHelper.removeInternalFields(currentInstanceDisplayed)
        val instanceUpdateFromUser = FormService.buildInstanceFromForm(cleanedOriginalInstance, updateFromUser, config.nexusEndpoint)
        val diff = InstanceHelper.buildDiffEntity(cleanedOriginalInstance, instanceUpdateFromUser)
        val updateToBeStored = InstanceHelper.removeEmptyFieldsNotInOriginal(cleanedOriginalInstance, diff)
        instanceApiService.get(wSClient, config.kgQueryEndpoint, instanceRef, token, EditorClient, Some(user.id)).flatMap{
          case Left(res) =>
            res.status match {
              case NOT_FOUND =>
                updateInstance(updateToBeStored, instanceRef, token, user.id)
              case _ =>
                Future(Left(APIEditorError(res.status, res.body)))
            }
          case Right(instance) =>
            val mergeInstanceWithPreviousUserUpdate = EditorInstance(InstanceHelper.removeInternalFields(instance).merge(updateToBeStored.nexusInstance))
            updateInstance(mergeInstanceWithPreviousUserUpdate, instanceRef, token, user.id)
        }
    }
  }

  def retrieveInstancesByIds(
                              instanceIds: List[NexusInstanceReference],
                              token: String
                            ): Future[Either[APIEditorError, List[PreviewInstance]]] = {
    val listOfResponse = for {
      id <- instanceIds
    } yield queryService.getInstancesWithId(wSClient, config.kgQueryEndpoint, id, EditorService.kgQueryGetPreviewInstance(), token)

    Future.sequence(listOfResponse).map {
      ls =>
        val r = ls.filter(l => l.status == OK)
        if (r.isEmpty) {
          Left(APIEditorError(INTERNAL_SERVER_ERROR, "Could not fetch all the instances"))
        } else {
          Right(r.map(res =>res.json.as[PreviewInstance].setLabel(formService.formRegistry)))
        }
    }
  }


}

object EditorService {


  def kgQueryGetPreviewInstance(
                                 context: String = EditorConstants.context
                               ): String =
    s"""
       |{
       |  "@context": $context,
       |  "schema:name": "",
       |  "fields": [
       |    {
       |      "fieldname": "name",
       |      "relative_path": "schema:name"
       |    },
       |    {
       |      "fieldname": "id",
       |      "relative_path": "base:relativeUrl"
       |    },
       |    {
       |      "fieldname": "description",
       |      "relative_path": "schema:description"
       |    },
       |    {
       |      "fieldname": "${UiConstants.DATATYPE}",
       |      "relative_path": "${JsonLDConstants.TYPE}"
       |    }
       |  ]
       |}
    """.stripMargin
}