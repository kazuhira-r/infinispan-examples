name := "infinispan-cdi"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.10.3"

organization := "littlewings"

seq(webSettings :_*)

artifactName := { (version: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  //artifact.name + "." + artifact.extension
  "javaee6-web." + artifact.extension
}

resolvers += "Public JBoss Group" at "http://repository.jboss.org/nexus/content/groups/public-jboss"

libraryDependencies ++= Seq(
  "org.eclipse.jetty" % "jetty-webapp" % "9.1.0.v20131115" % "container",
  "javax" % "javaee-web-api" % "6.0" % "provided",
  "org.infinispan" % "infinispan-core" % "6.0.0.Final" excludeAll(
    ExclusionRule(organization = "org.jgroups", name = "jgroups"),
    ExclusionRule(organization = "org.jboss.marshalling", name = "jboss-marshalling-river"),
    ExclusionRule(organization = "org.jboss.marshalling", name = "jboss-marshalling"),
    ExclusionRule(organization = "org.jboss.logging", name = "jboss-logging"),
    ExclusionRule(organization = "org.jboss.spec.javax.transaction", name = "jboss-transaction-api_1.1_spec")
  ),
  //"org.infinispan" % "infinispan-cdi" % "6.0.0.Final",
  "org.jgroups" % "jgroups" % "3.4.1.Final",
  "org.jboss.spec.javax.transaction" % "jboss-transaction-api_1.1_spec" % "1.0.1.Final",
  "org.jboss.marshalling" % "jboss-marshalling-river" % "1.3.18.GA",
  "org.jboss.marshalling" % "jboss-marshalling" % "1.3.18.GA",
  "org.jboss.logging" % "jboss-logging" % "3.1.2.GA",
  "net.jcip" % "jcip-annotations" % "1.0"
)
