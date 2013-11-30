name := "infinispan-transactions-locks"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.10.1"

organization := "littlewings"

fork in run := true

resolvers += "JBoss Public Maven Repository Group" at "http://repository.jboss.org/nexus/content/groups/public-jboss/"

libraryDependencies += "org.infinispan" % "infinispan-core" % "5.2.1.Final"
