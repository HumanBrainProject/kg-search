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
package models.instance

import constants.SchemaFieldsConstants
import models.user.User
import org.joda.time.DateTime
import play.api.libs.json._

case class EditorInstance(nexusInstance: NexusInstance){

  lazy val updaterId = nexusInstance.getField(EditorInstance.Fields.updaterId).get.as[String]


  def extractUpdateInfo(): (Option[String], Int, String, EditorInstance) = {
    ((this.nexusInstance.content \ NexusInstance.Fields.nexusId).asOpt[String],
      (this.nexusInstance.content \ NexusInstance.Fields.nexusRev).asOpt[Int].getOrElse(1),
      (this.nexusInstance.content \ EditorInstance.Fields.updaterId).asOpt[String].getOrElse(""),
      this
    )
  }

  def cleanManualData(): EditorInstance = {
    this.copy(this.nexusInstance.cleanManualData())
  }

  def mergeContent(instance: EditorInstance): EditorInstance = {
    this.copy(this.nexusInstance.copy(content = this.nexusInstance.content.deepMerge(instance.nexusInstance.content)))
  }

  def contentToMap(): Map[String, JsValue] = {
    this.nexusInstance.content.fields.toMap
  }

  def cleanInstanceManual(): EditorInstance = {
    this.copy(
      this.nexusInstance.copy(
        content = this.nexusInstance.content
      .-(EditorInstance.Fields.updaterId)
      )
    )
  }

  def removeNexusFields(): EditorInstance = {
    this.copy(this.nexusInstance.removeNexusFields())
  }

}

object EditorInstance {
  val contextOrg = "http://schema.hbp.eu/hbpkg#"
  object Fields {
    val updaterId = s"${contextOrg}updater_id"
    val updateTimeStamp = s"${contextOrg}update_timestamp"
    val alternatives = s"${contextOrg}alternatives"
  }
}
