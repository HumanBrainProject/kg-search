/*
 *   Copyright (c) 2020, EPFL/Human Brain Project PCO
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
package services.indexer

import java.nio.file.Path

import com.google.inject.ImplementedBy
import javax.inject.Inject
import models.DatabaseScope
import models.errors.ApiError
import monix.eval.Task
import play.api.libs.json.JsValue

@ImplementedBy(classOf[SitemapGeneratorImpl])
trait SitemapGenerator {

  def write(path: Path): Unit

  def addUrl(
    dataType: String,
    identifier: Option[String],
    releasedOnly: Boolean,
    completeRebuild: Boolean
  ): Unit
}

class SitemapGeneratorImpl @Inject()(
  ) extends SitemapGenerator {
  override def write(path: Path): Unit = ???

  override def addUrl(
    dataType: String,
    identifier: Option[String],
    releasedOnly: Boolean,
    completeRebuild: Boolean
  ): Unit = {}
}
