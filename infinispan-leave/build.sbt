name := "infinispan-leave"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.10.2"

organization := "littlewings"

resolvers += "JBoss Public Maven Repository Group" at "http://repository.jboss.org/nexus/content/groups/public-jboss"

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-core" % "5.3.0.Final",
  "net.jcip" % "jcip-annotations" % "1.0"
)
