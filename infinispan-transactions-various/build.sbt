name := "infinispan-transactions-various"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.10.3"

organization := "org.littlewings"

scalacOptions ++= Seq("-Xlint", "-deprecation", "-unchecked")

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-core" % "6.0.1.Final"  excludeAll(
    ExclusionRule(organization = "org.jgroups", name = "jgroups"),
    ExclusionRule(organization = "org.jboss.marshalling", name = "jboss-marshalling-river"),
    ExclusionRule(organization = "org.jboss.marshalling", name = "jboss-marshalling"),
    ExclusionRule(organization = "org.jboss.logging", name = "jboss-logging"),
    ExclusionRule(organization = "org.jboss.spec.javax.transaction", name = "jboss-transaction-api_1.1_spec")
  ),
  "org.jgroups" % "jgroups" % "3.4.1.Final",
  "org.jboss.spec.javax.transaction" % "jboss-transaction-api_1.1_spec" % "1.0.1.Final",
  "org.jboss.marshalling" % "jboss-marshalling-river" % "1.3.18.GA",
  "org.jboss.marshalling" % "jboss-marshalling" % "1.3.18.GA",
  "org.jboss.logging" % "jboss-logging" % "3.1.2.GA",
  "net.jcip" % "jcip-annotations" % "1.0",
  "org.jboss.jbossts.jta" % "narayana-jta" % "4.17.13.Final",
  "org.scalatest" %% "scalatest" % "2.0" % "test"
)
