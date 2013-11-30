name := "infinispan-remote"

version := "0.0.1"

scalaVersion := "2.10.0"

organization := "littlewings"

fork in run := true

resolvers += "JBoss Public Maven Repository Group" at "http://repository.jboss.org/nexus/content/groups/public-jboss/"

libraryDependencies += "org.infinispan" % "infinispan-client-hotrod" % "5.2.1.Final"
