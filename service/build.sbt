
name := """kg-service"""
organization := "eu.humanbrainproject"

version := "1.0-SNAPSHOT"

lazy val common = (project in file("modules/common"))
  .enablePlugins(PlayScala)

lazy val auth = (project in file("modules/authentication"))
  .enablePlugins(PlayScala)
  .dependsOn(common)

lazy val proxy = (project in file("modules/proxy"))
  .enablePlugins(PlayScala)
  .dependsOn(common, auth)

lazy val kg_service = (project in file("."))
  .enablePlugins(PlayScala)
  .aggregate(common, auth, proxy)
  .dependsOn(common, auth, proxy)

Common.settings

sources in (Compile, doc) := Seq.empty

publishArtifact in (Compile, packageDoc) := false

scalacOptions += "-Ypartial-unification"
