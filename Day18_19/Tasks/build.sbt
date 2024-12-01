import scala.collection.Seq

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.10"

lazy val root = (project in file("."))
  .settings(
    name := "Day18_19_Tasks"
  )

val sparkVersion = "3.2.1"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "com.google.cloud.bigdataoss" % "gcs-connector" % "hadoop3-2.2.5",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion,
  "org.apache.kafka" % "kafka-clients" % "3.5.0"
)