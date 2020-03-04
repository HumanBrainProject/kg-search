
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
package utils

import play.api.libs.json._

object JsonHandler {

  def findPathForValue(value: String, path: JsPath, root: JsValue): Seq[JsPath] = {
    var res = Seq.empty[JsPath]
    root match {
      case o:JsObject =>
        val it = o.fields.iterator
        while (it.hasNext) {
          val e = it.next()
          res = res ++ findPathForValue(value, path \ (e._1), e._2)
        }
      case JsString(x) if x==value =>
        res = res :+ path
      case JsArray(x) =>
        val it = x.zipWithIndex.iterator
        while (it.hasNext) {
          val e = it.next()
          res = res ++ findPathForValue(value, path(e._2), e._1)
        }
      case _ => None
    }
    res
  }

  def findPathForKey(key: String, path: JsPath, root: JsValue): Seq[JsPath] = {
    var res = Seq.empty[JsPath]
    root match {
      case o:JsObject =>
        val it = o.fields.iterator
        while (it.hasNext) {
          val e = it.next()
          if (e._1 == key) {
            res = res :+ path
            res = res ++ findPathForKey(key, path \ (e._1), e._2)
          } else {
            res = res ++ findPathForKey(key, path \ (e._1), e._2)
          }
        }
      case JsArray(x) =>
        val it = x.zipWithIndex.iterator
        while (it.hasNext) {
          val e = it.next()
          res = res ++ findPathForKey(key, path(e._2), e._1)
        }
      case _ => None
    }
    res
  }

}
