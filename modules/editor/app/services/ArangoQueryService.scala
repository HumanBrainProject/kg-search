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
import constants.SchemaFieldsConstants
import helpers.InstanceHelper
import models.instance.NexusInstance
import models.{EditorResponseObject, EditorResponseWithCount, FormRegistry, NexusPath}
import play.api.http.HeaderNames._
import play.api.http.Status._
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

class ArangoQueryService @Inject()(
                                    config: ConfigurationService,
                                    wSClient: WSClient,
                                    nexusService: NexusService,
                                    oIDCAuthService: OIDCAuthService,
                                    formService: FormService
                                  )(implicit executionContext: ExecutionContext) {

  def listInstances(nexusPath: NexusPath, from: Int, size: Int, search: String): Future[Either[WSResponse, EditorResponseWithCount]] = {
    wSClient.url(s"${config.kgQueryEndpoint}/arango/instances/${nexusPath.toString()}")
      .withQueryStringParameters(("search", search), ("from", from.toString), ("size", size.toString)).get().map{
      res =>
        res.status match {
          case OK =>
            val total = if((res.json \ "fullCount").as[Long] == 0){
              (res.json \ "count").as[Long]
            }else{
              (res.json \ "fullCount").as[Long]
            }
            val data = (res.json \ "data").as[JsArray]
            if(data.value.nonEmpty){
              val dataType = if((data.value.head \ "@type").asOpt[List[String]].isDefined){
                (data.value.head \ "@type").as[List[String]].head
              }else{
                (data.value.head \ "@type").as[String]
              }
              val result = EditorResponseWithCount(
                Json.toJson(InstanceHelper.formatInstanceList( data, config.reconciledPrefix)),
                dataType,
                (formService.formRegistry.registry \ nexusPath.org \ nexusPath.domain \ nexusPath.schema \ nexusPath.version \ "label").asOpt[String]
                  .getOrElse(nexusPath.toString()),
                total
              )
              Right(
                result
              )
            }else{
              Right(
                EditorResponseWithCount.empty
              )
            }
          case _ => Left(res)
        }
    }
  }

}