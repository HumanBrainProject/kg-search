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

import helpers.ResponseHelper._
import javax.inject.{Inject, Singleton}
import models.NexusPath
import monix.eval.Task
import play.api.Logger
import play.api.http.HttpEntity
import play.api.mvc._
import services.{ConfigurationService, NexusService, NexusSpaceService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NexusCommonController @Inject()(
  cc: ControllerComponents,
  config: ConfigurationService,
  nexusService: NexusService,
  nexusSpaceService: NexusSpaceService
) extends AbstractController(cc) {
  val logger = Logger(this.getClass)
  val apiEndpoint = s"${config.authEndpoint}/idm/v1/api"
  val orgNamePattern = "[a-z0-9]{3,}"
  implicit val scheduler = monix.execution.Scheduler.Implicits.global

  def createPrivateSpace(): Action[AnyContent] = Action.async { implicit request =>
    val tokenOpt = request.headers.toSimpleMap.get("Authorization")
    val result = tokenOpt match {
      case Some(token) =>
        request.body.asJson
          .map { jsonBody =>
            val groupName: String = (jsonBody \ "name").as[String].toLowerCase
            val isValidOrgName: Boolean = groupName.matches(orgNamePattern)
            if (isValidOrgName) {
              val description: String = (jsonBody \ "description").as[String]
              val nexusGroupName = s"nexus-$groupName"
              val adminGroupName = nexusGroupName + "-admin"
              for {
                nexusGroups <- nexusSpaceService
                  .createGroups(nexusGroupName, adminGroupName, description, token, apiEndpoint)
                nexusOrg  <- nexusSpaceService.createNexusOrg(groupName, token, config.nexusEndpoint)
                iamRights <- nexusSpaceService.grantIAMrights(groupName, token, config.iamEndpoint)
              } yield {
                val res = List(
                  s"OIDC group creation result: ${nexusGroups.statusText}\t content: ${nexusGroups.body}",
                  s"Nexus organization creation result: ${nexusOrg.statusText}\t content: ${nexusOrg.body}",
                  s"ACLs creation result: ${iamRights.statusText}\t content: ${iamRights.body}"
                )
                Ok(s"${res.mkString("\n")}")
              }
            } else {
              Task.pure(BadRequest("Invalid group name for nexus organization"))
            }
          }
          .getOrElse(Task.pure(BadRequest("Empty body")))
      case _ => Task.pure(Unauthorized)
    }
    result.runToFuture
  }

  def createSchema(
    organization: String,
    domain: String,
    entityType: String,
    version: String,
    namespace: Option[String]
  ): Action[AnyContent] = Action.async { implicit request =>
    val tokenOpt = request.headers.toSimpleMap.get("Authorization")
    val result = tokenOpt match {
      case Some(token) =>
        val path = NexusPath(organization, domain, entityType, version)
        nexusService
          .createSimpleSchema(
            config.nexusEndpoint,
            path,
            token,
            namespace
          )
          .map { response =>
            response.status match {
              case OK =>
                Ok(response.body)
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
      case None =>
        Task.pure(Unauthorized)
    }
    result.runToFuture
  }
}
