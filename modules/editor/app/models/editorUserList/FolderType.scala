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
package models.editorUserList

sealed trait FolderType{
  val t: String
}

object FolderType {

  def unapply(f: FolderType): Option[String] = Some(f.t)

  def apply(s: String): FolderType = s match {
    case "BOOKMARK" => BOOKMARKFOLDER
    case "NODETYPE" => NODETYPEFOLDER
  }

  import play.api.libs.json._
  import play.api.libs.functional.syntax._
  implicit val folderTypeReads: Reads[FolderType] =
    (JsPath \ "folderType").read[String].map(s => FolderType(s))

  implicit val folderTypeWrites: Writes[FolderType] =
    (JsPath \ "folderType").write[String].contramap(_.t)
}
case object BOOKMARKFOLDER extends FolderType {
  override val t = "BOOKMARK"
}

case object NODETYPEFOLDER extends FolderType {
  override val t = "NODETYPE"
}
