name := "infinispan-remote-query"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.10.3"

organization := "org.littlewings"

resolvers += "JBoss Public Maven Repository Group" at "http://repository.jboss.org/nexus/content/groups/public-jboss/"

scalacOptions ++= Seq("-Xlint", "-deprecation", "-unchecked")

{
  val infinispanVersion = "6.0.0.Final"
  libraryDependencies ++= Seq(
    "org.infinispan" % "infinispan-client-hotrod" % infinispanVersion excludeAll(
      ExclusionRule(organization = "org.jboss.marshalling", name = "jboss-marshalling-river"),
      ExclusionRule(organization = "org.jboss.logging", name = "jboss-logging")
    ),
    "org.infinispan" % "infinispan-query-dsl" % infinispanVersion,
    //"org.infinispan" % "infinispan-remote-query-client" % infinispanVersion,
    "org.infinispan" % "infinispan-remote-query-server" % infinispanVersion,
    "org.jboss.marshalling" % "jboss-marshalling-river" % "1.3.18.GA",
    "org.jboss.logging" % "jboss-logging" % "3.1.2.GA",
    "net.jcip" % "jcip-annotations" % "1.0",
    "org.jboss.remotingjmx" % "remoting-jmx" % "2.0.0.Final",
    "org.scalatest" %% "scalatest" % "2.0"
  )
}
