name := "infinispan-configured-cache"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.2"

organization := "org.littlewings"

scalacOptions ++= Seq("-Xlint", "-deprecation", "-unchecked", "-feature")

incOptions := incOptions.value.withNameHashing(true)

jetty()

artifactName := { (version: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  //artifact.name + "." + artifact.extension
  "javaee7-web." + artifact.extension
}

val jettyVersion = "9.2.2.v20140723"

libraryDependencies ++= Seq(
  "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "container",
  "org.eclipse.jetty" % "jetty-plus"   % jettyVersion % "container",
  "javax" % "javaee-web-api" % "7.0" % "provided",
  "org.infinispan" % "infinispan-core" % "6.0.2.Final" % "provided" excludeAll(
    ExclusionRule(organization = "org.jgroups", name = "jgroups"),
    ExclusionRule(organization = "org.jboss.marshalling", name = "jboss-marshalling-river"),
    ExclusionRule(organization = "org.jboss.marshalling", name = "jboss-marshalling"),
    ExclusionRule(organization = "org.jboss.logging", name = "jboss-logging"),
    ExclusionRule(organization = "org.jboss.spec.javax.transaction", name = "jboss-transaction-api_1.1_spec")
  ),
  "org.jgroups" % "jgroups" % "3.4.1.Final" % "provided",
  "org.jboss.spec.javax.transaction" % "jboss-transaction-api_1.1_spec" % "1.0.1.Final" % "provided",
  "org.jboss.marshalling" % "jboss-marshalling-river" % "1.4.4.Final" % "provided",
  "org.jboss.marshalling" % "jboss-marshalling" % "1.4.4.Final" % "provided",
  "org.jboss.logging" % "jboss-logging" % "3.1.2.GA" % "provided",
  "net.jcip" % "jcip-annotations" % "1.0" % "provided",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.2"
)
