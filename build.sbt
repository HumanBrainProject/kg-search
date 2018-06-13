import com.typesafe.sbt.packager.docker.{Cmd, ExecCmd}

name := """kg-service"""
organization := "eu.humanbrainproject"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
scalaVersion := "2.12.3"
resolvers += "Typesafe Simple Repository" at
  "http://repo.typesafe.com/typesafe/simple/maven-releases/"

libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += "com.typesafe.play" %% "play-iteratees" % "2.6.1"
libraryDependencies += "de.leanovate.play-mockws" %% "play-mockws" % "2.6.2" % Test
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "com.github.stijndehaes" %% "play-prometheus-filters" % "0.3.2"

// Apache poi support (excel)
libraryDependencies += "org.apache.poi" % "poi" % "3.17"
libraryDependencies += "org.apache.poi" % "poi-ooxml" % "3.17"
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "eu.humanbrainproject.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "eu.humanbrainproject.binders._"
sources in (Compile, doc) := Seq.empty

publishArtifact in (Compile, packageDoc) := false
