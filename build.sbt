name := "service"

version := "0.1"

scalaVersion := "2.12.6"

scalacOptions += "-Ypartial-unification"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test

libraryDependencies += "com.dimafeng" %% "testcontainers-scala" % "0.22.0" % "test"
libraryDependencies += "org.testcontainers" % "postgresql" % "1.10.2" % Test

libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.1.5"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.18"
libraryDependencies += "com.typesafe" % "config" % "1.3.2"

libraryDependencies += "com.h2database" % "h2" % "1.4.197"

val circeVersion = "0.10.1"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

resolvers += Resolver.sonatypeRepo("releases")
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

val doobieVersion = "0.6.0"
libraryDependencies ++= Seq(
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-specs2" % doobieVersion
)

libraryDependencies += "com.github.daddykotex" %% "courier" % "1.0.0"
lazy val root = (project in file(".")).enablePlugins(SbtTwirl)