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

package common.models

import play.api.libs.json.JsObject

class ReleaseInstance( override val nexusUUID : String,override val  nexusPath: NexusPath,override val  content: JsObject, instanceRevision: Long) extends NexusInstance(nexusUUID ,nexusPath, content){
  lazy val revision = this.getRevision()

  def getRevision():Long = {
    (content \ "nxv:rev").as[Long]
  }
}

object ReleaseInstance {
  def apply(jsObject: JsObject): ReleaseInstance ={
    val id = (jsObject \ "@id").as[String]
    val (uuid, nexusPath) = NexusInstance.extractIdAndPathFromString(id)
    val rev = (jsObject \ "http://hbp.eu/instanceRev").asOpt[Long].getOrElse(1L)
    new ReleaseInstance(uuid, nexusPath, jsObject, rev)
  }

  def template(instanceUrl: String, releaseIdentifier: String, instanceRev:Long ): String = s"""
      {
        "@type": "http://hbp.eu/minds#Release",
        "http://hbp.eu/minds#releaseinstance":
          {
            "@id": "${instanceUrl}"
          },
        "http://hbp.eu/minds#releasestate": "released",
        "http://schema.org/identifier": "${releaseIdentifier}",
        "http://hbp.eu/instanceRev": ${instanceRev}
      }"""
}

