name := "infinispan-cap"

version := "0.0.1"

scalaVersion := "2.10.1"

organization := "littlewings"

fork in run := true

javaOptions += "-Xmx1024M"

scalacOptions += "-deprecation"

resolvers += "JBoss Public Maven Repository Group" at "http://repository.jboss.org/nexus/content/groups/public-jboss/"

libraryDependencies += "org.infinispan" % "infinispan-client-hotrod" % "5.2.1.Final"

