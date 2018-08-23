
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

package editor.models

import play.api.libs.json.{JsNull, JsString, JsValue}
import play.libs.Json

object ReleaseStatus {
  val released = "RELEASED"
  val notReleased = "NOT_RELEASED"
  val hasChanged = "HAS_CHANGED"

  val status = List(JsString(released), JsString(notReleased), JsString(hasChanged))
  val childStatus = List(JsString(released), JsString(notReleased), JsString(hasChanged), JsNull)

  val r = scala.util.Random
  //TODO remove for test only
  def getRandomStatus(): JsValue ={
    status(r.nextInt(status.length))
  }

  def getRandomChildrenStatus(): JsValue = {
    childStatus(r.nextInt(childStatus.length))
  }


}
