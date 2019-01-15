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
package controllers

import com.google.inject.Inject
import models.{Cache, EditorUserCache, UserInfoCache}
import play.api.Logger
import play.api.cache.{AsyncCacheApi, NamedCache}
import play.api.mvc._
import play.cache.NamedCache
import services.CacheService
import services.specification.FormService

import scala.concurrent.{ExecutionContext, Future}

class NexusAdminController @Inject()(
  cc: ControllerComponents,
  @NamedCache("editor-userinfo-cache") editorCache: AsyncCacheApi,
  @NamedCache("userinfo-cache") userCache: AsyncCacheApi,
  formService: FormService
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  val log = Logger(this.getClass)
  object cacheService extends CacheService

  def clearCache(name: String): Action[AnyContent] = Action.async { implicit request =>
    Cache
      .fromString(name)
      .map {
        case EditorUserCache => cacheService.clearCache(editorCache).map(_ => Ok("Cache cleared"))
        case UserInfoCache   => cacheService.clearCache(userCache).map(_ => Ok("Cache cleared"))
      }
      .getOrElse(Future(NotFound("Cache not found")))
  }

  def deleteAndReloadSpecs: Action[AnyContent] = Action { implicit request =>
    formService.flushSpec()
    Ok("Specification reloaded")
  }

}
