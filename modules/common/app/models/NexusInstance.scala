
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

import play.api.libs.json._


case class NexusInstance(nexusUUID: String, nexusPath: NexusPath, content:JsObject) {

  def id():String = {
    s"${this.nexusPath}/${this.nexusUUID}"
  }

  def getField(fieldName: String): JsValue = content.value(fieldName)

  def modificationOfLinks(nexusEndpoint: String, reconciledPrefix: String): NexusInstance = {
    val id = (this.content \ "@id").as[String]
    val correctedId = s"$nexusEndpoint/v0/data/${NexusInstance.getIdForEditor(id, reconciledPrefix)}"
    val jsonTransformer = (__ ).json.update(
      __.read[JsObject].map{ o => o ++ Json.obj("@id" -> correctedId)}
    )
    NexusInstance(this.nexusUUID, this.nexusPath, this.content.transform(jsonTransformer).get)
  }

//  def cleanManualDataFromNexus(): Map[String, JsValue] = {
//    cleanManualData().content.fields.toMap
//  }

  def cleanManualData(): NexusInstance = {
    this.content - ("@context") - ("@id") - ("@type") - ("links") - ("nxv:rev") - ("nxv:deprecated")
    this.copy(content = this.content)
  }


  def removeNexusFields(): NexusInstance = {
    this.content - ("@context") - ("@type") - ("links") - ("nxv:deprecated")
    this.copy(content = this.content)
  }

  def getRevision(): Long ={
    this.getField(NexusInstance.Fields.nexusRev).as[Long]
  }

}

object NexusInstance {
//  def apply(jsValue: JsValue): NexusInstance = {
//    val (id, path) = extractIdAndPath(jsValue)
//    new NexusInstance(id, path , jsValue.as[JsObject])
//  }

  def extractIdAndPath(jsValue: JsValue): (String, NexusPath) = {
    val nexusUrl = (jsValue \ "@id").as[String]
    extractIdAndPathFromString(nexusUrl)
  }

  def extractIdAndPathFromString(url: String): (String, NexusPath) = {
    val nexusId = getIdfromURL(url)
    val datatype = nexusId.splitAt(nexusId.lastIndexOf("/"))
    val nexusType = NexusPath(datatype._1.split("/").toList)
    (datatype._2.substring(1) , nexusType)
  }

  def getIdfromURL(url: String): String = {
    assert(url contains "v0/data/")
    url.split("v0/data/").tail.head
  }

  def getIdForEditor(url: String, reconciledPrefix: String): String = {
    assert(url contains "v0/data/")
    val pathString = url.split("v0/data/").tail.head
    val id = pathString.split("/").last
    NexusPath(pathString)
      .originalPath(reconciledPrefix)
      .toString() + "/" + id
  }
  // extract id, rev and userId of updator for this update

  object Fields{
    val nexusId = "@id"
    val nexusRev = "nxv:rev"
  }

  import play.api.libs.functional.syntax._

  implicit val editorUserReads: Reads[NexusInstance] = (
    (JsPath \ "@id").read[String].map(extractIdAndPathFromString(_)._1) and
      (JsPath \ "@id").read[String].map(extractIdAndPathFromString(_)._2) and
      JsPath.read[JsObject]
    ) (NexusInstance.apply _)


}


