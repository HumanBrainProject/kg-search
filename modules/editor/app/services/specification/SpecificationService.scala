/*
 *   Copyright (c) 2019, EPFL/Human Brain Project PCO
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

import java.io.{File, FileInputStream}

import com.google.inject.Inject
import constants.{QueryConstants, SchemaFieldsConstants}
import javax.inject.Singleton
import models.errors.APIEditorError
import models.{NexusPath, RefreshAccessToken}
import org.slf4j.LoggerFactory
import play.api.Environment
import play.api.http.HeaderNames.AUTHORIZATION
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.{JsArray, JsObject, Json}
import play.api.libs.ws.WSClient
import services.{ConfigurationService, OIDCAuthService}

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

case class SpecificationFile(id: String, data: JsObject)

@Singleton
class SpecificationService @Inject()(
  WSClient: WSClient,
  config: ConfigurationService,
  OIDCAuthService: OIDCAuthService,
  env: Environment
)(
  implicit executionContext: ExecutionContext
) {
  private val specFieldIdQueryPath = NexusPath("minds", "meta", "specificationfield", "v0.0.1")
  private val specFieldIdQueryId = "specificationFieldIdentifier"
  private val log = LoggerFactory.getLogger(this.getClass)

  init()

  def init(): Future[Unit] = {
    log.debug("INITIALIZATION ---------------------------------")
    // load spec from files
    log.debug("INITIALIZATION --- Fetching local specifications")

    val specsIds = fetchSpecificationFieldsFile()
    //Get by identifier all Specification field
    log.debug("INITIALIZATION --- Fetching remote specifications")
    for {
      token <- OIDCAuthService.getTechAccessToken(forceRefresh = true)
      specList <- WSClient
        .url(s"${config.kgQueryEndpoint}/query/${specFieldIdQueryPath.toString()}/$specFieldIdQueryId/instances")
        .addHttpHeaders(AUTHORIZATION -> token.token)
        .addQueryStringParameters(QueryConstants.VOCAB -> "https://schema.hbp.eu/myQuery/")
        .get()
        .map { res =>
          res.status match {
            case OK =>
              log.debug("INITIALIZATION --- Computing missing specifications")
              val arr: List[String] = (res.json \ "results").as[List[JsObject]].foldLeft(List[String]()) {
                case (l, obj) =>
                  val tempL = if ((obj \ "identifier").asOpt[String].isDefined) {
                    List((obj \ "identifier").as[String])
                  } else {
                    (obj \ "identifier").as[List[String]]
                  }
                  l ::: tempL
              }
              // Create if not exists
              val toCreate: List[SpecificationFile] = specsIds.filterNot(spec => arr.contains(spec.id))
              log.debug(
                s"INITIALIZATION --- Creating ${toCreate.size} missing specifications \n ${toCreate.map(_.id).mkString(",")}"
              )
              toCreate.map { el =>
                this.uploadSpec(el, token)
              }
            case INTERNAL_SERVER_ERROR => log.error("Could not fetch specification fields")
            case _                     => log.error(s"Error while fetching Specification fields - ${res.status} : ${res.body}")
          }
          ()
        }
    } yield specList

  }

  private def fetchSpecificationFieldsFile(): List[SpecificationFile] = {
    def getListOfFiles(dir: File): List[File] = dir.listFiles.filter(_.isFile).toList
    getListOfFiles(new File(env.getFile("modules/editor/resources/SpecificationFields").getPath)).map { file =>
      val stream = new FileInputStream(file)
      val json = try { Json.parse(stream) } finally { stream.close() }
      SpecificationFile((json \ SchemaFieldsConstants.IDENTIFIER).as[String], json.as[JsObject])
    }
  }

  def uploadSpec(value: SpecificationFile, token: RefreshAccessToken): Future[Either[APIEditorError, Unit]] = ???

}
