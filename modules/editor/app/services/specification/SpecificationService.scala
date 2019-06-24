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

import akka.Done
import cats.implicits._
import com.google.inject.Inject
import constants.{EditorConstants, JsonLDConstants, QueryConstants}
import javax.inject.Singleton
import models.errors.APIEditorError
import models.instance.{NexusInstance, NexusInstanceReference}
import models.{NexusPath, RefreshAccessToken}
import monix.eval.Task
import org.slf4j.LoggerFactory
import play.api.Environment
import play.api.http.HeaderNames.AUTHORIZATION
import play.api.http.Status.{CREATED, INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.libs.json.{JsNull, JsObject, JsValue, Json}
import play.api.libs.ws.{WSClient, WSResponse}
import services.instance.InstanceApiService
import services.{ConfigurationService, CredentialsService, OIDCAuthService}

import scala.concurrent.{ExecutionContext, Future}

final case class SpecificationFile(id: String, data: JsObject)

@Singleton
class SpecificationService @Inject()(
  WSClient: WSClient,
  config: ConfigurationService,
  OIDCAuthService: OIDCAuthService,
  clientCredentials: CredentialsService,
  env: Environment
) {
  private val specFieldIdQueryPath = NexusPath("meta", "minds", "specificationfield", "v0.0.1")
  private val specIdQueryPath = NexusPath("meta", "minds", "specification", "v0.0.1")
  private val specFieldIdQueryId = "specificationFieldIdentifier"
  private val specQueryId = "specificationIdentifier"
  private val specFieldTypeIdQueryPath = NexusPath("meta", "minds", "specificationfieldtype", "v0.0.1")
  private val specFieldTypeIdQueryId = "specificationFieldTypeIdentifier"
  private val log = LoggerFactory.getLogger(this.getClass)
  object instanceApiService extends InstanceApiService

  def init(): Task[Done] = {
    log.debug("Specification Service INITIALIZATION ---------------------------------")
    //Get by identifier all Specification field
    log.debug("Specification Service INITIALIZATION --- Fetching remote specification fields")

    OIDCAuthService.getTechAccessToken(forceRefresh = true).flatMap { token =>
      createSpecificationQueries("Queries", token).flatMap { _ =>
        createSpecificationQueries("SpecificationQueries", token).flatMap { _ =>
          log.info(s"Specification Service INITIALIZATION --- Done fetching and creating queries")
          getOrCreateSpecificationAndSpecificationFields(token).map { s =>
            Done
          }
        }
      }
    }
  }

  private def getOrCreateSpecificationAndSpecificationFields(token: RefreshAccessToken): Task[Done] = {
    runQuery(s"${specFieldIdQueryPath.toString}/$specFieldIdQueryId/instances", token)
      .flatMap { specFieldResult =>
        specFieldResult.status match {
          case OK =>
            val specFieldIdentifierMap: Map[String, NexusInstanceReference] =
              createInitialMapOfSpecificationIds(specFieldResult.json)
            // load spec from files
            log.debug("Specification Service INITIALIZATION --- Fetching local specification fields")
            val specsFieldsIds = fetchFile("SpecificationFields")

            log.debug("Specification Service INITIALIZATION --- Computing missing specification fields")
            val toCreate: List[SpecificationFile] =
              specsFieldsIds.filterNot(spec => specFieldIdentifierMap.contains(spec.id))
            log.debug(
              s"Specification Service INITIALIZATION --- Creating ${toCreate.size} missing specifications fields \n ${toCreate.map(_.id).mkString(",")}"
            )
            // Create if not exists
            val futToCreate = toCreate.map { el =>
              this.uploadSpec(el, token)
            }
            //Update the map of ids
            val fieldId = for {
              fieldIdList <- futToCreate.sequence
              fieldIdMap = fieldIdList.foldLeft(specFieldIdentifierMap) {
                case (acc, Right((id, ref))) => acc.updated(id, ref)
                case (acc, _)                => acc
              }
            } yield fieldIdMap
            fieldId.flatMap { fMap =>
              getSpecifications(fMap, token).map { _ =>
                log.info(s"Specification Service INITIALIZATION --- Done fetching and creating specification")
                Done
              }
            }
          case INTERNAL_SERVER_ERROR =>
            log.error("Could not fetch specification fields")
            Task.pure(Done)
          case _ =>
            log.error(
              s"Error while fetching Specification fields - ${specFieldResult.status} : ${specFieldResult.body}"
            )
            Task.pure(Done)
        }
      }
  }

  private def getSpecifications(
    fieldsIdMap: Map[String, NexusInstanceReference],
    token: RefreshAccessToken
  ): Task[Done] = {
    log.debug("Specification Service INITIALIZATION --- Fetching remote specifications")
    val futListOfSpecToUpload =
      runQuery(s"${specIdQueryPath.toString}/$specQueryId/instances", token)
        .map { res =>
          res.status match {
            case OK =>
              val specIdentifierMap = createInitialMapOfSpecificationIds(res.json)
              log.debug("Specification Service INITIALIZATION --- Fetching local specifications")
              val specsIds = fetchFile("Specifications")
              val specToCreate = specsIds.filter(spec => !specIdentifierMap.contains(spec.id))
              log.debug("Specification Service INITIALIZATION --- Computing missing specifications")
              val specToUpload = specToCreate.map { file =>
                val newFields = file.data
                  .value("https://schema.hbp.eu/meta/editor/fields")
                  .as[List[JsObject]]
                  .map(
                    js =>
                      js.value
                        .get(JsonLDConstants.ID)
                        .map { v =>
                          Json.obj(
                            JsonLDConstants.ID -> s"${config.nexusEndpoint}/v0/data/${fieldsIdMap(v.as[String]).toString}"
                          )
                        }
                        .getOrElse(js)
                  )
                file.copy(
                  data = Json
                    .toJson(file.data.value.updated("https://schema.hbp.eu/meta/editor/fields", Json.toJson(newFields)))
                    .as[JsObject]
                )
              }
              log.info(
                s"Specification service INITIALIZATION --- Preparing to create ${specToUpload.size} specifications"
              )
              specToUpload
            case _ =>
              log.error(
                s"Specification service INITIALIZATION --- Could not fetch specifications - ${res.status} - ${res.body}"
              )
              List[SpecificationFile]()
          }
        }
    futListOfSpecToUpload.flatMap { listOfSpecToUpload =>
      (listOfSpecToUpload
        .map { file =>
          uploadSpec(file, token)
        })
        .sequence
        .map { specCreated =>
          log.info(
            s"Specification Service INITIALIZATION --- Creating ${specCreated.size} missing specifications ${specCreated}"
          )
          Done
        }
    }
  }

  private def createInitialMapOfSpecificationIds(json: JsValue): Map[String, NexusInstanceReference] = {
    (json \ "results").as[List[JsObject]].foldLeft(Map[String, NexusInstanceReference]()) {
      case (l, obj) =>
        if (obj.value.get("identifier").isDefined && obj.value("identifier") != JsNull) {
          val tempL = if ((obj \ "identifier").asOpt[String].isDefined) {
            Map[String, NexusInstanceReference]()
              .updated(
                (obj \ "identifier").as[String],
                NexusInstanceReference.fromUrl((obj \ JsonLDConstants.ID).as[String])
              )
          } else {
            (obj \ "identifier")
              .as[List[String]]
              .map(el => el -> NexusInstanceReference.fromUrl((obj \ JsonLDConstants.ID).as[String]))
              .toMap
          }
          tempL.foldLeft(l) {
            case (acc, (k, v)) => acc.updated(k, v)
          }
        } else {
          l
        }
    }
  }

  private def getListOfFiles(dir: File): List[File] =
    if (dir.exists()) { dir.listFiles.filter(_.isFile).toList } else List()

  private def fetchFile(folder: String): List[SpecificationFile] = {
    val folderPath = env.getFile(s"conf/resources/$folder")
    getListOfFiles(folderPath).map { file =>
      val stream = new FileInputStream(file)
      val json = try { Json.parse(stream) } finally { stream.close() }
      SpecificationFile((json \ EditorConstants.METAIDENTIFIER).as[String], json.as[JsObject])
    }
  }

  private def uploadSpec(
    value: SpecificationFile,
    token: RefreshAccessToken
  ): Task[Either[APIEditorError, (String, NexusInstanceReference)]] = {

    val path = NexusInstanceReference.fromUrl(value.id).nexusPath
    instanceApiService
      .post(WSClient, config.kgQueryEndpoint, NexusInstance(None, path, value.data), None, token)(
        OIDCAuthService,
        clientCredentials
      )
      .map {
        case Left(res) =>
          log.error(
            s"Specification service --- could not upload specification - ${value.id} - ${res.status} - ${res.body}"
          )
          Left(APIEditorError(res.status, res.body))
        case Right(ref) => Right((value.id, ref))
      }
  }

  /**
    * Creating 2 lists of spec file one with the one already created
    * the second one with the ones that should be created
    * @param token the tech account token
    * @return
    */
  private def getSpecificationQueries(
    folder: String,
    token: RefreshAccessToken
  ): Task[(List[SpecificationFile], List[SpecificationFile])] = {
    fetchFile(folder).foldLeft(Task.pure((List[SpecificationFile](), List[SpecificationFile]()))) {
      case (previousFuture, file) =>
        val queryId = NexusInstanceReference.fromUrl(file.id)
        previousFuture.flatMap { previousResult =>
          runQuery(queryId.toString, token)
            .map { response =>
              response.status match {
                case OK        => (file :: previousResult._1, previousResult._2)
                case NOT_FOUND => (previousResult._1, file :: previousResult._2)
                case _ =>
                  log.error(s"Could not fetch specification queries - ${response.status} - ${response.body}")
                  previousResult
              }
            }
        }
    }
  }

  def fetchSpecificationQueries(token: RefreshAccessToken): Task[List[NexusInstanceReference]] = {
    getSpecificationQueries("SpecificationQueries", token).map { l =>
      l._1.map(f => NexusInstanceReference.fromUrl(f.id))
    }
  }

  private def createSpecificationQueries(
    folder: String,
    token: RefreshAccessToken
  ): Task[List[NexusInstanceReference]] = {
    log.info("Specification service INITIALIZATION --- Fetching local queries")
    val futQueriesToCreate = getSpecificationQueries(folder, token)
    futQueriesToCreate.flatMap { l =>
      log.info(s"Specification service INITIALIZATION --- Creating queries - ${l._2.map(_.id)}")
      val created = l._2.map { file =>
        Task
          .deferFuture(
            WSClient
              .url(s"${config.kgQueryEndpoint}/query/${file.id.toString()}")
              .addHttpHeaders(AUTHORIZATION -> token.token)
              .addQueryStringParameters(QueryConstants.VOCAB -> QueryConstants.DEFAULT_VOCAB)
              .put(file.data)
          )
          .map { res =>
            res.status match {
              case OK | CREATED => Some(NexusInstanceReference.fromUrl(file.id))
              case _ =>
                log.error(s"Could not create query - ${file.id} - ${res.body}")
                None
            }
          }
      }
      //Merging the two list to have all the queries that are in the DB
      created.sequence.map(_.collect { case Some(ref) => ref } ::: l._1.map(f => NexusInstanceReference.fromUrl(f.id)))
    }
  }

  def fetchSpecifications(token: RefreshAccessToken): Task[WSResponse] = {
    Task.deferFuture(
      WSClient
        .url(s"${config.kgQueryEndpoint}/query/meta/minds/specification/v0.0.1/specificationQuery/instances")
        .addHttpHeaders(AUTHORIZATION -> token.token)
        .addQueryStringParameters(QueryConstants.VOCAB -> EditorConstants.META)
        .get()
    )
  }

  private def runQuery(
    queryPath: String,
    token: RefreshAccessToken,
  ): Task[WSResponse] = {
    Task.deferFuture(
      WSClient
        .url(s"${config.kgQueryEndpoint}/query/$queryPath")
        .addHttpHeaders(AUTHORIZATION -> token.token)
        .addQueryStringParameters(QueryConstants.VOCAB -> QueryConstants.DEFAULT_VOCAB)
        .get()
    )
  }

}
