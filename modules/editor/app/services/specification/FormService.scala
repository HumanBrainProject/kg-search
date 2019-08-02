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

package services.specification

import com.google.inject.{Inject, Singleton}
import models._
import models.specification._
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.slf4j
import org.slf4j.LoggerFactory
import play.api.http.Status.OK
import play.api.libs.json._
import play.api.libs.ws.WSClient
import services.{ConfigurationService, TokenAuthService}

import scala.concurrent.duration.FiniteDuration

final case class FormRegistries(formRegistry: FormRegistry[UISpec], queryRegistry: FormRegistry[QuerySpec])

@Singleton
class FormService @Inject()(
  config: ConfigurationService,
  ws: WSClient,
  OIDCAuthService: TokenAuthService,
  specificationService: SpecificationService
) {

  private var stateSpec: Option[FormRegistries] = None

  val timeout = FiniteDuration(30, "sec")
  val retryTime = 5000 //ms
  val logger: slf4j.Logger = LoggerFactory.getLogger(this.getClass)
  specificationService.init().runSyncUnsafe(timeout)

  /**
    * @return the specification and stored queries
    */
  def getRegistries(): Task[FormRegistries] = stateSpec match {
    case Some(spec) => Task.pure(spec)
    case None       => loadFormConfiguration()
  }

  def flushSpec(): Task[FormRegistries] =
    specificationService.init().flatMap { _ =>
      loadFormConfiguration()
    }

  private final def loadFormConfiguration(): Task[FormRegistries] = {
    logger.info("Form Service INITIALIZATION --- Starting to load specification")
    OIDCAuthService.getTechAccessToken(true).flatMap { token =>
      Task
        .deferFuture(ws.url(s"${config.kgQueryEndpoint}/arango/internalDocuments/editor_specifications").get())
        .flatMap { querySpec =>
          querySpec.status match {
            case OK =>
              FormService.getRegistry(querySpec.json.as[List[JsObject]], specificationService, token).map {
                registries =>
                  logger.info(s"Form Service INITIALIZATION --- Done loading form specification")
                  stateSpec = Some(registries)
                  registries
              }
            case _ =>
              logger.error(s"Form Service INITIALIZATION --- Could not load configuration")
              Task.raiseError(
                new Exception(s"Form Service INITIALIZATION --- Could not load configuration - ${querySpec.body}")
              )
          }
        }
    }
  }

  def shouldReloadSpecification(path: NexusPath): Task[Boolean] = {
    this.getRegistries().map { registries =>
      registries.formRegistry.registry
        .get(path)
        .map(spec => spec.refreshSpecification.exists(identity))
        .exists(identity)
    }
  }

}

object FormService {
  val log: slf4j.Logger = LoggerFactory.getLogger(this.getClass)

  private val timeout = FiniteDuration(30, "sec")

  def getRegistry(
    js: List[JsObject],
    specificationService: SpecificationService,
    token: RefreshAccessToken
  ): Task[FormRegistries] = {
    val formRegistries = extractRegistries(js)
    for {
      systemQueries  <- specificationService.fetchSpecificationQueries(token)
      uiSpecResponse <- specificationService.fetchSpecifications(token)
    } yield {
      val completeQueries = FormRegistry(
        systemQueries.foldLeft(formRegistries.queryRegistry.registry) {
          case (acc, el) => acc.updated(el.nexusPath, QuerySpec(Json.obj(), Some(el.id)))
        }
      )

      val uiSpecFromDB = uiSpecResponse.status match {
        case OK => (uiSpecResponse.json \ "results").as[List[JsObject]]
        case _ =>
          log.error(
            s"Could not fetch specification from DB - Status: ${uiSpecResponse.status} Content: ${uiSpecResponse.body}"
          )
          List()
      }
      val uiSpecRegistry = formRegistries.formRegistry.copy(
        registry = uiSpecFromDB.foldLeft(formRegistries.formRegistry.registry) {
          case (acc, el) =>
            if ((el \ "targetType").asOpt[String].isDefined) {
              val path = NexusPath((el \ "targetType").as[String])
              acc.updated(path, el.as[UISpec])
            } else {
              acc
            }
        }
      )
      FormRegistries(uiSpecRegistry, completeQueries)
    }
  }

  def extractRegistries(js: List[JsObject]): FormRegistries = {
    FormRegistries(extractToRegistry[UISpec](js, "uiSpec"), extractToRegistry[QuerySpec](js, "query"))
  }

  private def extractToRegistry[A](js: List[JsObject], field: String)(implicit r: Reads[A]): FormRegistry[A] = {
    FormRegistry(
      js.foldLeft(Map[NexusPath, A]()) {
        case (acc, el) =>
          val listOfMap = (el \ field)
            .asOpt[JsObject]
            .map { f =>
              for {
                (org, json)  <- f.value
                (domain, d)  <- json.as[JsObject].value
                (schema, s)  <- d.as[JsObject].value
                (version, v) <- s.as[JsObject].value
              } yield NexusPath(org, domain, schema, version) -> v.as[A](r)
            }
            .getOrElse(List())
          acc ++ listOfMap
      }
    )
  }
}
