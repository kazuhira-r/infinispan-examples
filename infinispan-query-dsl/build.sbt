name := "infinispan-query-dsl"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.10.3"

organization := "org.littlewings"

fork in test := true

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-query" % "6.0.0.Final",
  "net.jcip" % "jcip-annotations" % "1.0",
  "org.scalatest" %% "scalatest" % "2.0" % "test"
)
