
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

object ResponseHelper {

  def getContentType(headers: Map[String, Seq[String]]): Option[String] = {
    headers.get("content-type") match {
      case Some(stringList) => Some(stringList.head)
      case None => None
    }
  }

  def flattenHeaders(headers: Map[String, Seq[String]], flatteningFunc: Seq[String] => String = defaultFlatteningFunc): Map[String, String] = {
    headers.map {
      case (key, stringList) => (key, flatteningFunc(stringList))
    }
  }

  def filterContentTypeAndLengthFromHeaders[T](headers: Map[String, T]): Map[String, T] = {
    val forbiddenKeys = Seq("content-type", "content-length")
    headers.filterNot(e => forbiddenKeys.contains(e._1))
  }

  def defaultFlatteningFunc(stringList: Seq[String]): String = {
    stringList.mkString(",")
  }
}
