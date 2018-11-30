name := "service"

version := "0.1"

scalaVersion := "2.12.6"

scalacOptions += "-Ypartial-unification"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test

libraryDependencies += "com.typesafe.akka" %% "akka-http"   % "10.1.5"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.18"
libraryDependencies += "org.typelevel" %% "cats-core" % "1.5.0-RC1"

val circeVersion = "0.10.1"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

resolvers += Resolver.sonatypeRepo("releases")
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)