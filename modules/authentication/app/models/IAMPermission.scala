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
package models

sealed trait IAMPermission

object IAMPermission {
  def fromString(s:String): IAMPermission = s match {
    case "read" => Read
    case "publish" => Publish
    case "write" => Write
    case "own" => Own
  }
  case object Read  extends IAMPermission
  case object Publish  extends IAMPermission
  case object Write  extends IAMPermission
  case object Own extends IAMPermission
}