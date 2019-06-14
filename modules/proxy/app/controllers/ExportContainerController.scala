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

import java.io._
import java.net.URL
import java.util.zip.{ZipEntry, ZipOutputStream}

import akka.Done
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, StreamConverters}
import akka.util.ByteString
import com.google.inject.Inject
import controllers.ExportContainerController.MAX_CONTAINER_SIZE
import play.api.{Configuration, Logger}
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc._
import play.api.http.HeaderNames._
import play.api.http.ContentTypes._
import play.api.http.HttpEntity
import play.api.http.Status.OK

import scala.concurrent.{ExecutionContext, Future}

class ExportContainerController @Inject()(cc: ControllerComponents)(
  implicit ec: ExecutionContext,
  ws: WSClient,
  config: Configuration,
  mat: Materializer
) extends AbstractController(cc) {
  private val log = Logger(this.getClass)

  def exportContainer(source: String, filter: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    // capture unhandled params to forward them to swift API call
    val fowardedParams = (request.target.queryMap - "container" - "filter").map(entry => (entry._1, entry._2.head))
    // build url container from source
    val sourceUrl = if (source.matches("^https://.*")) source else s"https://${source}"
    // get container details
    ExportContainerController
      .getContainerFilteredContent(sourceUrl, filter, fowardedParams)
      .map[Result] {
        // check containerFilteredSize
        case Right(filesInfo) =>
          val archiveSizeInMb =
            filesInfo.foldLeft(0.0)((size, fileInfo) => size + ExportContainerController.getFileSizeInMb(fileInfo))

          archiveSizeInMb match {
            case 0.0 =>
              BadRequest("Empty export requested")
            case size if (size > 0.0 && size <= config.get[Double](MAX_CONTAINER_SIZE)) =>
              log.info(s"Estimated archive size: ${archiveSizeInMb} MB")
              // create stream redirection
              val in = new PipedInputStream()
              val out = new PipedOutputStream(in)
              // start stream feeding
              val zipStreamResult = ExportContainerController.streamContainer(sourceUrl, filter, out)
              // close stream to finalize
              zipStreamResult.andThen {
                case zipStreamTry =>
                  zipStreamTry.map {
                    case zipStream =>
                      log.info(s"All resources streamed. Closing Zip")
                      zipStream.close()
                  }
              }
              // Stream output to make download start as soon as possible
              val filename = sourceUrl.split("\\/").last
              log.info(s"Container export - source: $sourceUrl, size: ${archiveSizeInMb} MB")
              Ok.chunked(StreamConverters.fromInputStream(() => in))
                .withHeaders(
                  "Content-Disposition" -> s"attachment; filename = ${filename}.zip; filename*=utf-8''${filename}.zip"
                )
            case _ =>
              val sourceUri = new URL(sourceUrl)
              val containerUrl = new URL(sourceUri.getProtocol, sourceUri.getHost, sourceUri.getPath)
              BadRequest(
                views.html.fileTooBigTemplate(
                  BigDecimal(archiveSizeInMb).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble,
                  containerUrl.toExternalForm,
                  filesInfo.filter(i => (i \ "bytes").as[Long] > 0)
                )
              )
          }
        case Left(r) =>
          Result(
            ResponseHeader(r.status),
            HttpEntity.Strict(ByteString(r.body), None)
          )
      }
  }
}

object ExportContainerController {

  val MAX_CONTAINER_SIZE = "export.max_size_in_mega_bytes"
  val MEGABYTE_BYTES = 1000.0 * 1000.0
  private val log = Logger(this.getClass)

  def getContainerFilteredContent(
    sourceContainer: String,
    filter: Option[String],
    forwardedParams: Map[String, String]
  )(
    implicit ec: ExecutionContext,
    ws: WSClient,
    config: Configuration,
    mat: Materializer
  ): Future[Either[WSResponse, Seq[JsObject]]] = {
    log.info(s"getting following container details: ${sourceContainer}")

    // get json view of container files to keep
    ws.url(sourceContainer)
      .addHttpHeaders(ACCEPT -> JSON)
      .withQueryStringParameters(forwardedParams.toSeq: _*)
      .get()
      .map { queryResponse =>
        queryResponse.status match {
          case OK => Right(collectFiles(queryResponse.json.as[JsArray], filter))
          case _  => Left(queryResponse)
        }
      }
  }

  def streamContainer(
    sourceContainer: String,
    filter: Option[String],
    out: PipedOutputStream
  )(implicit ec: ExecutionContext, ws: WSClient, config: Configuration, mat: Materializer): Future[ZipOutputStream] = {
    Logger.info(s"starting export of following container: ${sourceContainer}")
    // get json view of container
    val containerFiles =
      ws.url(sourceContainer).addHttpHeaders(ACCEPT -> JSON).get().map[Seq[JsObject]] { queryResponse =>
        // Get container's content to zip
        collectFiles(queryResponse.json.as[JsArray], filter)
      }

    // build necessary streams
    val zos = new ZipOutputStream(out)
    zos.setLevel(0) // minimal compression
    val sink = Sink.foreach[ByteString] { bytes =>
      zos.write(bytes.toArray)
    }

    containerFiles
      .map[Future[ZipOutputStream]] {
        case files =>
          Logger.debug(s"${files.size} files to be included in archive")
          // use foldleft to ensure sequential ingestion of resources and build a valid archive
          files.foldLeft(Future.successful[ZipOutputStream](zos)) {
            case (futureRes, fileInfo) =>
              futureRes.flatMap { _ =>
                streamResourceToArchiveStream(zos, fileInfo, sourceContainer, sink)
                  .map(_ => zos) // simply keep initial zip stream
              }
          }
      }
      .flatten
  }

  def streamResourceToArchiveStream(
    zos: ZipOutputStream,
    fileInfo: JsObject,
    sourceContainer: String,
    sink: Sink[ByteString, Future[Done]]
  )(implicit ws: WSClient, ec: ExecutionContext, mat: Materializer): Future[Done] = {
    Logger.debug(s"entry added for ${getFileName(fileInfo)}")
    ws.url(s"${sourceContainer}/${getFileUrl(fileInfo)}")
      .withMethod("GET")
      .stream()
      .map {
        case response =>
          zos.putNextEntry(new ZipEntry(getFileName(fileInfo)))
          response.bodyAsSource.runWith(sink)
      }
      .flatten
  }

  def collectFiles(fileList: JsArray, filterTypes: Option[String] = None): Seq[JsObject] = {
    fileList.value.collect(buildFilter(filterTypes))
  }

  def getFileUrl(fileInfo: JsObject): String = {
    (fileInfo \ "name").as[String]
  }

  // get file size as double since limit is not known. 0 if not present
  def getFileSizeInMb(fileInfo: JsObject): Double = {
    (fileInfo \ "bytes").asOpt[Double].getOrElse(0.0) / MEGABYTE_BYTES
  }

  def getFileName(fileInfo: JsObject): String = {
    (fileInfo \ "name").as[String]
  }

  def buildFilter(filterTypes: Option[String]): PartialFunction[JsValue, JsObject] = {
    new PartialFunction[JsValue, JsObject] {
      def apply(fileDetails: JsValue) = fileDetails.as[JsObject]

      def isDefinedAt(fileDetails: JsValue) = {
        filterTypes match {
          case None => true
          case Some(filter) =>
            val filterSeq = filter.split(";")
            val fileInfo = fileDetails.as[JsObject]
            val fileExtension = (fileInfo \ "name").as[String].split("\\.").last
            filterSeq.contains((fileInfo \ "content_type").as[String]) ||
            filterSeq.contains(fileExtension)
        }
      }
    }
  }

}
