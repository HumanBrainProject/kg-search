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

import models.user.Group

object ESHelper {

  val publicIndex: String = "kg_public"
  val indicesPath: String = "_cat/indices"

  def filterNexusGroups(groups: Seq[Group], formatName: String => String = ESHelper.formatGroupName): Seq[String] = {
    groups.filter(s => s.name.startsWith("nexus-") && !s.name.endsWith("-admin")).map(s => formatName(s.name))
  }

  def formatGroupName(name: String): String = {
    name.replace("nexus-", "")
  }

  def transformToIndex(s: String): String = {
    s"kg_$s"
  }

  def replaceESIndex(esIndex: String, proxyUrl: String): String = {
    s"$esIndex/$proxyUrl"
  }

}
