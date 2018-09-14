package editor.services

import com.google.inject.Inject
import play.api.{Configuration, Logger}
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.http.Status._
import play.api.http.HeaderNames._
import play.api.http.ContentTypes._

import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc.Results._

class ReleaseService @Inject()(ws: WSClient,
                              config: Configuration
                             )(implicit executionContext: ExecutionContext) {
  val kgQueryEndpoint: String = config.getOptional[String]("kgquery.endpoint").getOrElse("http://localhost:8600")
  val logger = Logger(this.getClass)

  def releaseStatus(org: String, domain: String, schema: String, version: String, id:String):Future[Either[Option[WSResponse],  collection.Map[String, JsValue]]] = {

    ws.url(s"${kgQueryEndpoint}/arango/release/$org/$domain/$schema/$version/$id").addHttpHeaders(CONTENT_TYPE -> JSON).get().map{
      res => res.status match{
        case OK =>
          val item = res.json.as[JsObject]
          if(item != null){
            val spec = ReleaseService.specs(item)
            Right(spec)
          }else{
            Left(None)
          }
        case _ => logger.error(res.body)
          Left(Some(res))
      }
    }
  }
}

object ReleaseService {

  def specs(item: JsObject): collection.Map[String, JsValue] = {
    item.value.map{ k =>
      k._1 match {
        case "http://schema.org/name" => "label" -> k._2
        case "@type" => "type" -> JsString(k._2.as[String].split("#").last)
        case "children" => k._1 -> Json.toJson(k._2.as[JsArray]
          .value.groupBy(js => (js \ "@id").as[String]).map{
          case (k,v) =>
            val edgeTypes = v.foldLeft(List[String]()) {
              case (list, js) => (js \ "linkType").as[String].split("/").head.split("-").last :: list
            }
            val transformer = (__ \ 'linkType).json.put(Json.toJson(edgeTypes))
            val linkType = v.head.transform(transformer)
            k -> v.head.as[JsObject].++(linkType.get)
        }.values.map( j => specs(j.as[JsObject]))).as[JsValue]
        case "linkType" =>
          k._1 -> k._2
        case _ => k._1 -> k._2
      }
    }
  }
}
