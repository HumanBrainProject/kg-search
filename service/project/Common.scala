import sbt._
import Keys._
import sbt._
import Keys._
import play.sbt.PlayImport._
import play.sbt.routes.RoutesKeys.routesGenerator
import play.routes.compiler.InjectedRoutesGenerator

object Common {

  val settings: Seq[Setting[_]] = Seq(
    organization := "eu.humanbrainproject",
    version := "1.0.0-SNAPSHOT",
    scalaVersion := "2.13.1",
    libraryDependencies ++= baseDependencies
  )

  val baseDependencies = Seq(
    "de.leanovate.play-mockws" %% "play-mockws" % "2.8.0" % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
    "org.mockito" % "mockito-core" % "3.3.3" % Test,
    "commons-io" % "commons-io" % "2.6",
    "org.webjars" % "swagger-ui" % "3.18.1",
    "org.typelevel" %% "cats-core" % "2.1.0",
    "io.monix" %% "monix" % "3.1.0"
  )

  val playDependencies = Seq(
    guice,
    ws
  )

  val playSettings = settings ++ Seq(
    resolvers ++= Seq(
      "Typesafe Simple Repository" at "https://repo.typesafe.com/typesafe/simple/maven-releases/",
      Resolver.jcenterRepo
    ),
    routesGenerator := InjectedRoutesGenerator,
    libraryDependencies ++= playDependencies
  )
}
