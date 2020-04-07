/*
 *   Copyright (c) 2020, EPFL/Human Brain Project PCO
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

import java.util.UUID

import akka.util.ByteString
import javax.inject.Inject
import models.errors.ApiError
import models.templates.TemplateType
import models.{DatabaseScope, PaginationParams}
import monix.eval.Task
import monix.execution.Scheduler
import play.api.http.HttpEntity
import play.api.libs.json.JsValue
import play.api.libs.ws.WSResponse
import play.api.mvc._
import services.indexer.Indexer

class IndexerController @Inject()(
  indexer: Indexer[JsValue, JsValue, Task, WSResponse, Either[ApiError, JsValue]],
  cc: ControllerComponents
) extends AbstractController(cc) {
  implicit val s: Scheduler = monix.execution.Scheduler.Implicits.global

  def index(
    databaseScope: DatabaseScope,
    completeRebuild: Boolean,
    releasedOnly: Boolean,
    simulate: Boolean,
    restrictToOrganizations: Option[String]
  ): Action[AnyContent] = Action.async { implicit request =>
    val result = request.headers.toSimpleMap.get("Authorization") match {
      case Some(token) =>
        indexer
          .index(
            completeRebuild,
            releasedOnly,
            simulate,
            restrictToOrganizations.map(_.split(",").toList).getOrElse(List()),
            databaseScope,
            token
          )
          .map {
            case Right(_) => Ok("Indexing done")
            case Left(e)  => e.toResults()
          }
      case None => Task.pure(Unauthorized("Please provide credentials"))
    }
    result.runToFuture
  }

  def indexByType(
    databaseScope: DatabaseScope,
    dataType: String,
    completeRebuild: Boolean,
    releasedOnly: Boolean,
    simulate: Boolean,
    restrictToOrganizations: Option[String]
  ): Action[AnyContent] = Action.async { implicit request =>
    val result = request.headers.toSimpleMap.get("Authorization") match {
      case Some(token) =>
        indexer
          .indexByType(
            completeRebuild,
            releasedOnly,
            simulate,
            restrictToOrganizations.map(_.split(",").toList).getOrElse(List()),
            databaseScope,
            dataType,
            token
          )
          .map {
            case Right(_) => Ok(s"Indexing done for type $dataType")
            case Left(e)  => e.toResults()
          }
      case None => Task.pure(Unauthorized("Please provide credentials"))
    }
    result.runToFuture
  }

  def indexByTypeAndId(databaseScope: DatabaseScope, templateType: TemplateType, id: UUID): Unit = ()

  def applyTemplateByType(
    databaseScope: DatabaseScope,
    templateType: TemplateType,
    restrictToOrg: Option[String],
    from: Option[Int],
    size: Option[Int]
  ): Action[AnyContent] =
    Action.async { implicit request =>
      val result = request.headers.toSimpleMap.get("Authorization") match {
        case Some(token) =>
          indexer
            .queryByType(
              templateType,
              databaseScope,
              PaginationParams(from, size),
              restrictToOrg.map(s => s.split(",").toList).getOrElse(List()),
              token
            )
            .map {
              case Right(v) => Ok(v)
              case Left(error) =>
                error.toResults()
            }

        case None => Task.pure(Unauthorized("Please provide credentials"))
      }
      result.runToFuture(s)
    }

  def applyTemplateByTypeAndId(
    databaseScope: DatabaseScope,
    templateType: TemplateType,
    id: UUID
  ): Action[AnyContent] =
    Action.async { implicit request =>
      val result = request.headers.toSimpleMap.get("Authorization") match {
        case Some(token) =>
          indexer
            .queryByTypeAndId(templateType, id, databaseScope, token)
            .map {
              case Right(v) => Ok(v)
              case Left(error) =>
                error.toResults()
            }

        case None => Task.pure(Unauthorized("Please provide credentials"))
      }
      result.runToFuture(s)
    }

  def applyMetaTemplateByType(
    templateType: TemplateType,
  ): Action[AnyContent] =
    Action.async { implicit request =>
      val result = request.headers.toSimpleMap.get("Authorization") match {
        case Some(token) =>
          indexer
            .metaByType(templateType, token)
            .map {
              case Right(v) => Ok(v)
              case Left(error) =>
                error.toResults()
            }

        case None => Task.pure(Unauthorized("Please provide credentials"))
      }
      result.runToFuture(s)
    }

  def getLabels(templateType: TemplateType): Action[AnyContent] =
    Action.async { implicit request =>
      val result = request.headers.toSimpleMap.get("Authorization") match {
        case Some(token) =>
          indexer
            .getLabels(templateType, token)
            .map {
              case Right(v) => Ok(v)
              case Left(error) =>
                error.toResults()
            }
        case None => Task.pure(Unauthorized("Please provide credentials"))
      }
      result.runToFuture(s)
    }
}
