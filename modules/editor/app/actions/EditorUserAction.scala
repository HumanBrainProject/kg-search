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

package actions

import helpers.{EditorSpaceHelper, OIDCHelper}
import models.user.{EditorUser, EditorUserRequest, EditorUserWriteRequest}
import models.{user, IAMPermission, UserRequest}
import play.api.Logger
import play.api.mvc.Results._
import play.api.http.HeaderNames._
import play.api.mvc._
import services.{EditorUserService, IAMAuthService}

import scala.concurrent.{ExecutionContext, Future}

object EditorUserAction {
  val logger = Logger(this.getClass)

  def editorUserWriteAction(org: String, editorSuffix: String, iAMAuthService: IAMAuthService)(
    implicit ec: ExecutionContext
  ): ActionRefiner[UserRequest, EditorUserWriteRequest] =
    new ActionRefiner[UserRequest, EditorUserWriteRequest] {
      def executionContext: ExecutionContext = ec

      def refine[A](input: UserRequest[A]): Future[Either[Result, EditorUserWriteRequest[A]]] = {
        val editorOrg = if (org.endsWith(editorSuffix)) org else org + editorSuffix
        if (EditorSpaceHelper.isEditorGroup(input.user, editorOrg)) {
          iAMAuthService.getAcls(editorOrg, Seq(("self", "true"), ("parents", "true")), input.userToken).map {
            case Right(acls) =>
              if (IAMAuthService.hasAccess(acls, IAMPermission.Write)) {
                Right(EditorUserWriteRequest(input.user, org, input, input.userToken))
              } else {
                Left(Forbidden("You do not have sufficient access rights to proceed"))
              }
            case Left(response) =>
              logger.error(s"Fetching permission failed - ${response.body}")
              Left(InternalServerError("An error occurred while fetching permission"))
          }
        } else {
          Future.successful {
            logger.debug(s"Not allowed: ${input.user} for index: $org")
            Left(Forbidden("You are not allowed to perform this request"))
          }
        }
      }
    }

  def editorUserAction(
    editorUserService: EditorUserService
  )(implicit ec: ExecutionContext): ActionRefiner[UserRequest, EditorUserRequest] =
    new ActionRefiner[UserRequest, EditorUserRequest] {
      def executionContext: ExecutionContext = ec

      def refine[A](input: UserRequest[A]): Future[Either[Result, EditorUserRequest[A]]] = {
        editorUserService.getUser(input.user, input.userToken).map {
          case Right(Some(editorUser)) => Right(user.EditorUserRequest(editorUser, input, input.userToken))
          case Right(None)             => Left(NotFound("User not found"))
          case Left(err) =>
            logger.error(s"Fetching editor user failed - ${err.content}")
            Left(InternalServerError("An error occurred while fetching user information"))
        }
      }
    }

  private def isCurator(user: EditorUser, org: String): Boolean = {
    user.nexusUser.groups.exists(s => s.matches(s"^nexus-$org-curator$$"))
  }

  def curatorUserAction(
    org: String
  )(implicit ec: ExecutionContext): ActionFilter[EditorUserRequest] =
    new ActionFilter[EditorUserRequest] {
      def executionContext: ExecutionContext = ec

      def filter[A](input: EditorUserRequest[A]): Future[Option[Result]] = {
        Future.successful(if (isCurator(input.editorUser, org)) {
          None
        } else {
          Some(Forbidden("You do not have sufficient access rights to proceed"))
        })
      }
    }
}
