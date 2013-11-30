name := "infinispan-lucene"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.10.1"

organization := "littlewings"

net.virtualvoid.sbt.graph.Plugin.graphSettings

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-core" % "5.3.0.Beta2",
  "org.infinispan" % "infinispan-lucene-directory" % "5.3.0.Beta2",
  "org.apache.lucene" % "lucene-core" % "4.3.0",
  "org.apache.lucene" % "lucene-analyzers-common" % "4.3.0",
  "org.apache.lucene" % "lucene-queryparser" % "4.3.0"
)
