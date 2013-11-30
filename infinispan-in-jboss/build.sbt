name := "infinispan-in-jboss"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.10.3"

organization := "littlewings"

seq(webSettings: _*)

artifactName := { (version: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  artifact.name + "." + artifact.extension
}

resolvers += "Public JBoss Group" at "http://repository.jboss.org/nexus/content/groups/public-jboss"

libraryDependencies ++= Seq(
  "org.eclipse.jetty" % "jetty-webapp" % "9.0.6.v20130930" % "container",
  "javax" % "javaee-web-api" % "6.0" % "provided",
  "org.infinispan" % "infinispan-core" % "5.3.0.Final" % "provided",
  "net.jcip" % "jcip-annotations" % "1.0" % "provided"
)

//packageOptions in (Compile, packageWar) +=
//  Package.ManifestAttributes(new java.util.jar.Attributes.Name("Dependencies") -> "org.infinispan:5.3 services")

