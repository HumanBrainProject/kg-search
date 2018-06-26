package common.helpers

import play.api.inject.guice.GuiceApplicationBuilder

object ConfigMock {
  val nexusEndpoint: String = "http://www.nexus.com"
  val reconcileEndpoint: String = "http://www.reconcile.com"
  val blazegraphNameSpace: String = "kg"
  val sparqlEndpoint = "http://blazegraph:9999"
  val idm = "https://services.humanbrainproject.eu/idm/v1/api"
  val userInfo = "https://userinfo.com"
  val esHost = "https://eshost.com"

  val fakeApplicationConfig = GuiceApplicationBuilder().configure(
    "play.http.filters" -> "play.api.http.NoHttpFilters",
    "nexus.endpoint" -> nexusEndpoint,
    "reconcile.endpoint" -> reconcileEndpoint,
    "blazegraph.namespace" -> blazegraphNameSpace,
    "blazegraph.endpoint" -> sparqlEndpoint,
    "idm.api" -> idm,
    "auth.userinfo" -> userInfo,
    "es.host" -> esHost
  )
}
