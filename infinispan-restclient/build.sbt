name := "infinispan-restclient"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.10.2"

organization := "littlewings"

resolvers += "jboss" at "http://repository.jboss.org/nexus/content/groups/public/"

libraryDependencies ++= Seq(
  "org.jboss.resteasy" % "resteasy-jaxrs" % "3.0.4.Final",
  "org.jboss.resteasy" % "resteasy-client" % "3.0.4.Final"
)
