
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

package helpers

import play.api.libs.json._


/*
    Build a flatten view of a JsObject by setting path info into key
 */
object JsFlattener {

  def apply(js: JsValue): Seq[(JsPath, JsValue)] = {
    buildFlatObject(js).fields.map {
      case (pathString, value) => (buildJsPathFromString(pathString), value)
    }
  }

  def buildFlatObject(js: JsValue): JsObject = flatten(js)

  def concat(oldKey: String, newKey: String): String = {
    if (oldKey.nonEmpty) s"$oldKey%%%$newKey" else newKey
  }

  def flatten(js: JsValue, prefix: String = ""): JsObject = {
    if (!js.isInstanceOf[JsObject]) return Json.obj(prefix -> js)
    js.as[JsObject].fields.foldLeft(Json.obj()) {
      case (o, (k, value)) => {
        o.deepMerge(value match {
          case jsArr: JsArray => jsArr.as[Seq[JsValue]].zipWithIndex.foldLeft(o) {
            case (o, (n, i: Int)) => o.deepMerge(
              flatten(n.as[JsValue], s"${concat(prefix, k)}[$i]")
            )
          }
          case jsObj: JsObject => flatten(jsObj, concat(prefix, k))
          case other => Json.obj(concat(prefix, k) -> other.as[JsValue])
        })
      }
    }
  }

  // build a JsPath from a string path of form xx/yy/zz
  def buildJsPathFromString(stringPath: String): JsPath = {
    val path = stringPath.split("%%%").foldLeft(JsPath()) {
      case (jsPath, subPathString) =>
        // detect array idx
        val extractIdx = ".*\\[(\\d+)\\]$".r
        subPathString match {
          case extractIdx(idx) =>
            jsPath ++ (__ \ subPathString \ idx.toInt)
          case _ =>
            jsPath ++ (__ \ subPathString)
        }
    }
    path
  }

}