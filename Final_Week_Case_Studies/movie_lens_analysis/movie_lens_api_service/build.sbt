ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.15"

lazy val root = (project in file("."))
  .settings(
    name := "movie_lens_api_service"
  )

resolvers += "Akka library repository".at("https://repo.akka.io/maven")

val sparkVersion = "3.5.1"
lazy val akkaVersion = sys.props.getOrElse("akka.version", "2.9.3")

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % "early-semver"
ThisBuild / evictionErrorLevel                                   := Level.Warn

libraryDependencies ++= Seq(
  // spark
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion,
  //gcs
  "com.google.cloud.bigdataoss" % "gcs-connector" % "hadoop3-2.2.5",
  // CORS dependency
  "ch.megard" %% "akka-http-cors" % "1.1.3",
  // akka
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.2.15" % Test,
  "com.typesafe.akka" %% "akka-stream-kafka" % "6.0.0",
  "com.typesafe.akka" %% "akka-http" % "10.6.3",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.6.3",
  // kafka
  "ch.qos.logback" % "logback-classic" % "1.2.13",
  "org.slf4j" % "slf4j-api" % "1.7.36",
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion // Akka SLF4J bridge
)