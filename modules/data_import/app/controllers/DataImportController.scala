
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

package data_import.controllers

import java.io.FileInputStream

import data_import.helpers.excel_import.ExcelImportHelper
import javax.inject.{Inject, Singleton}
import org.apache.poi.xssf.usermodel._
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class ExcelImportController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext, ws: WSClient, config: Configuration)
  extends AbstractController(cc) {
  val logger = Logger(this.getClass)
  val nexusEndpoint = config.get[String]("nexus.endpoint")

  def extractDataFromExcel(action: Option[String]) = Action.async(parse.temporaryFile) { request =>
    val path = request.body.path
    val fis = new FileInputStream(path.toFile)

    val wb = new XSSFWorkbook(fis)
    ExcelImportHelper.formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator()
    val jsonData = ExcelImportHelper.buildJsonMindsDataFromExcel(wb)

    action.getOrElse(ExcelImportHelper.actionPreview) match {
      case ExcelImportHelper.actionInsert =>
        val tokenOpt = request.headers.toSimpleMap.get("Authorization")
        tokenOpt match {
          case Some(token) =>
            ExcelImportHelper.insertEntities(jsonData, nexusEndpoint, token).map {
              res =>
                Ok(JsObject(Seq(("insertion result", JsArray(res)))))
            }

          case None =>
            Future.successful(Ok(
              Json.parse("{\"error\": \"You're not allowed to write in KG dataworkbench space. Please check your access token\"}")
                .as[JsObject]))
        }

      case _ =>
        // insert elements SpecimenGroup, Activity and Dataset from jsonData
        Future.successful(Ok(jsonData))

    }
  }
}



