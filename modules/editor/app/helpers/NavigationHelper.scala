
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

package editor.helpers

import common.models.NexusPath
import play.api.libs.json.{JsObject, JsString, JsValue, Json}

object NavigationHelper {
  val backLinkField = "back_link"

  def addBackLink(instance:JsObject, backLinkStr: String): JsObject = {
    val backLink = backLinkField -> JsString(backLinkStr)
    instance + backLink
  }

  /**
    * This method provides a back_link based on the instance path
    * This back_link is used in the UI navigation
    * @param path The path of the instance
    * @param reconciledSuffix The term used to identify a reconciled space
    * @return A path as a string
    */
  def generateBackLink(path:NexusPath,reconciledSuffix:String): String = {
    val formattedPath = path.originalPath(reconciledSuffix)
    (FormHelper.formRegistry \ formattedPath.org \ formattedPath.domain \ formattedPath.schema).asOpt[JsObject] match {
      case Some(schema) =>
        s"${formattedPath.org}/${formattedPath.domain}/${formattedPath.schema}"
      case _ => ""
    }
  }

  def resultWithBackLink(instance: JsObject, path: NexusPath,reconciledSuffix: String): JsObject = {
    val backLink = generateBackLink(path, reconciledSuffix)
    addBackLink(instance, backLink)
  }

  def errorMessageWithBackLink(res: Any, backLink: String = ""): JsObject = {
    res match {
      case res: String => Json.obj("message" -> JsString(res) , backLinkField -> backLink)
      case res: JsValue => Json.obj("messages" -> res, backLinkField -> backLink)
    }
  }



}
