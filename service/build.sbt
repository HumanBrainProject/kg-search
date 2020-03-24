name := """kg-service"""
organization := "eu.humanbrainproject"

version := "1.0-SNAPSHOT"

lazy val common = (project in file("modules/common"))
  .enablePlugins(PlayScala, SwaggerPlugin)

lazy val auth = (project in file("modules/authentication"))
  .enablePlugins(PlayScala, SwaggerPlugin)
  .dependsOn(common)

lazy val kg_service = (project in file("."))
  .enablePlugins(PlayScala, SwaggerPlugin)
  .aggregate(common, auth)
  .dependsOn(common, auth)

Common.settings

javaOptions in Universal ++= Seq("-Dpidfile.path=/dev/null")

sources in (Compile, doc) := Seq.empty

publishArtifact in (Compile, packageDoc) := false

enablePlugins(DockerPlugin)
dockerBaseImage := "adoptopenjdk:11-jre-hotspot"
swaggerV3 := true
swaggerDomainNameSpaces := Seq("models")
