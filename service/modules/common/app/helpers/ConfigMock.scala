package helpers

import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.duration.FiniteDuration

object ConfigMock {
  val nexusEndpoint: String = "http://www.nexus.com"
  val reconcileEndpoint: String = "http://www.reconcile.com"
  val blazegraphNameSpace: String = "kg"
  val sparqlEndpoint = "http://blazegraph:9999"
  val idm = "https://services.humanbrainproject.eu/idm/v1/api"
  val userInfo = "https://userinfo.com"
  val esHost = "https://eshost.com"
  val reconciledPrefix = "reconciled"
  val editorPrefix = "editor"
  val kgQueryEndpoint = "kgqueryEndpoint"
  val refreshTokenFile = "/opt/tokenfolder"
  val authEndpoint = "auth.com"
  val cacheExpiration = FiniteDuration(10, "min")
  val nexusIam = "nexus-iam.com"

  val fakeApplicationConfig = GuiceApplicationBuilder().configure(
    "play.http.filters" -> "play.api.http.NoHttpFilters",
    "nexus.endpoint" -> nexusEndpoint,
    "reconcile.endpoint" -> reconcileEndpoint,
    "blazegraph.namespace" -> blazegraphNameSpace,
    "blazegraph.endpoint" -> sparqlEndpoint,
    "idm.api" -> idm,
    "auth.userinfo" -> userInfo,
    "es.host" -> esHost,
    "nexus.reconciled.prefix" -> reconciledPrefix,
    "nexus.editor.prefix" -> editorPrefix,
    "kgquery.endpoint" -> kgQueryEndpoint,
    "auth.refreshTokenFile" -> refreshTokenFile,
    "auth.endpoint"-> authEndpoint,
    "proxy.cache.expiration" -> cacheExpiration.toMillis,
    "nexus.iam" -> nexusIam
  )
}
