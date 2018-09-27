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

import common.models.{NexusInstance, User}
import editor.helpers.InstanceHelper
import org.joda.time.DateTime
import play.api.libs.json._

case class EditorInstance(nexusInstance: NexusInstance){

  lazy val updaterId = nexusInstance.getField(EditorInstance.Fields.updaterId).get.as[String]


  def extractUpdateInfo(): (String, Int, String) = {
    ((this.nexusInstance.content \ NexusInstance.Fields.nexusId).as[String],
      (this.nexusInstance.content \ NexusInstance.Fields.nexusRev).as[Int],
      (this.nexusInstance.content \ EditorInstance.Fields.updaterId).asOpt[String].getOrElse("")
    )
  }

  def cleanManualData(): EditorInstance = {
    this.copy(this.nexusInstance.cleanManualData())
  }

  def contentToMap(): Map[String, JsValue] = {
    this.nexusInstance.content.fields.toMap
  }

  def cleanInstanceManual(): EditorInstance = {
    this.copy(
      this.nexusInstance.copy(
        content = this.nexusInstance.content.-(EditorInstance.Fields.parent)
      .-(EditorInstance.Fields.origin)
      .-(EditorInstance.Fields.updaterId)
      )
    )
  }


  def prepareManualEntityForStorage(userInfo: User, originLink: String): EditorInstance = {
    this.copy(
      this.nexusInstance.copy(
        content = this.nexusInstance
          .cleanManualData()
          .content
          .+(EditorInstance.Fields.updaterId, JsString(userInfo.id))
          .+(EditorInstance.Fields.updateTimeStamp, JsNumber(new DateTime().getMillis))
          +(EditorInstance.Fields.origin, JsString(originLink)) +
          (EditorInstance.Fields.parent, Json.obj("@id" -> JsString(originLink)))
      )
    )
  }
}

object EditorInstance {
  val contextOrg = "http://hbp.eu/manual#"
  object Fields {
    val updaterId = s"${contextOrg}updater_id"
    val updateTimeStamp = s"${contextOrg}update_timestamp"
    val parent = s"${contextOrg}parent"
    val origin = s"${contextOrg}origin"
    val originalPath = s"${contextOrg}original_path"
    val userCreated = s"${contextOrg}user_created"
  }

  def generateInstance(nexusInstance: NexusInstance,org: String, datatype: String, identifier: String, originalPath: String): EditorInstance = {
    val content = nexusInstance.content
      .+("@type" -> JsString(s"http://hbp.eu/${org}#${datatype.capitalize}"))
      .+("http://schema.org/identifier" -> JsString(identifier))
      .+(Fields.origin, JsString(""))
      .+(Fields.userCreated, JsBoolean(true))
      .+(Fields.originalPath, JsString(originalPath))
    EditorInstance(nexusInstance.copy(content=content))
  }
}
