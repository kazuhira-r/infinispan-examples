name := "infinispan-query-example"

version := "0.0.1"

scalaVersion := "2.10.1"

organization := "littlewings"

fork in run := true

scalacOptions += "-deprecation"

resolvers += "JBoss Public Maven Repository Group" at "http://repository.jboss.org/nexus/content/groups/public-jboss/"

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-core" % "5.2.1.Final",
  "org.infinispan" % "infinispan-query" % "5.2.1.Final"
)

net.virtualvoid.sbt.graph.Plugin.graphSettings
