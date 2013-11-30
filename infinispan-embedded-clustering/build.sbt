name := "infinispan-embedded-clustering"

version := "0.0.1"

scalaVersion := "2.10.0"

organization := "littlewings"

fork in run := true

resolvers += "jboss repository" at "http://repository.jboss.org/nexus/content/groups/public-jboss/"

libraryDependencies += "org.infinispan" % "infinispan-core" % "5.2.0.Final"

