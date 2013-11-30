name := "infinispan-cachestore-jpa-example"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.10.3"

organization := "littlewings"

fork in run := true

connectInput := true

resolvers += "Public JBoss Group" at "http://repository.jboss.org/nexus/content/groups/public-jboss"

libraryDependencies ++= Seq(
  "org.hibernate" % "hibernate-entitymanager" % "4.2.7.SP1",
  "org.infinispan" % "infinispan-cachestore-jpa" % "5.3.0.Final",
  "net.jcip" % "jcip-annotations" % "1.0",
  "mysql" % "mysql-connector-java" % "5.1.26" % "runtime"
)
