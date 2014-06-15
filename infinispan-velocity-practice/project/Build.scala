import sbt._
import sbt.Keys._

import com.earldouglas.xsbtwebplugin.WebPlugin

object BuildSettings {
  val buildOrganization = "org.littlewings"
  val buildVersion = "0.0.1-SNAPSHOT"
  val buildScalaVersion = "2.11.1"
  val buildScalacOptions = Seq("-Xlint", "-deprecation", "-unchecked", "-feature")

  val buildSettings = Seq(
    organization := buildOrganization,
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    scalacOptions ++= buildScalacOptions,
    incOptions := incOptions.value.withNameHashing(true)
  )
}

object Dependencies {
  val infinispanVersion = "6.0.2.Final"
  val infinispan = "org.infinispan" % "infinispan-core" % infinispanVersion excludeAll(
    ExclusionRule(organization = "org.jgroups", name = "jgroups"),
    ExclusionRule(organization = "org.jboss.marshalling", name = "jboss-marshalling-river"),
    ExclusionRule(organization = "org.jboss.marshalling", name = "jboss-marshalling"),
    ExclusionRule(organization = "org.jboss.logging", name = "jboss-logging"),
    ExclusionRule(organization = "org.jboss.spec.javax.transaction", name = "jboss-transaction-api_1.1_spec")
  )
  val infinispanAsProvided = "org.infinispan" % "infinispan-core" % infinispanVersion % "provided" excludeAll(
    ExclusionRule(organization = "org.jgroups", name = "jgroups"),
    ExclusionRule(organization = "org.jboss.marshalling", name = "jboss-marshalling-river"),
    ExclusionRule(organization = "org.jboss.marshalling", name = "jboss-marshalling"),
    ExclusionRule(organization = "org.jboss.logging", name = "jboss-logging"),
    ExclusionRule(organization = "org.jboss.spec.javax.transaction", name = "jboss-transaction-api_1.1_spec")
  )
  val jgroups = "org.jgroups" % "jgroups" % "3.4.1.Final"
  val jbossTransactionApi = "org.jboss.spec.javax.transaction" % "jboss-transaction-api_1.1_spec" % "1.0.1.Final"
  val jbossMarshallingRiver = "org.jboss.marshalling" % "jboss-marshalling-river" % "1.4.4.Final"
  val jbossMarshalling = "org.jboss.marshalling" % "jboss-marshalling" % "1.4.4.Final"
  val jbossLogging = "org.jboss.logging" % "jboss-logging" % "3.1.2.GA"
  val jcipAnnotations = "net.jcip" % "jcip-annotations" % "1.0"

  val velocity = "org.apache.velocity" % "velocity" % "1.7"
  val velocityTools = "org.apache.velocity" % "velocity-tools" % "2.0"

  val javaeeWebApi = "javax" % "javaee-web-api" % "7.0" % "provided"

  val containerJettyVersion = "9.2.1.v20140609"
  val containerJettyWebapp = "org.eclipse.jetty" % "jetty-webapp" % containerJettyVersion % "container"
  val containerJettyPlus = "org.eclipse.jetty" % "jetty-plus" % containerJettyVersion % "container"
}

object InfinispanVelocityPractice extends Build {
  import BuildSettings._
  import Dependencies._

  val templatePublisherDeps = Seq(
    infinispan,
    jgroups,
    jbossTransactionApi,
    jbossMarshallingRiver,
    jbossMarshalling,
    jbossLogging,
    jcipAnnotations
  )

  val webViewDeps = Seq(
    containerJettyWebapp,
    containerJettyPlus,
    javaeeWebApi,
    velocity,
    velocityTools,
    infinispanAsProvided
  ) ++ (Seq(
    jgroups,
    jbossTransactionApi,
    jbossMarshallingRiver,
    jbossMarshalling,
    jbossLogging,
    jcipAnnotations
  ) map ( _ % "provided"))

  lazy val root =
    Project("infinispan-velocity-practice",
            file("."),
            settings = buildSettings)
      .aggregate(templatePublisher)

  lazy val templatePublisher =
    Project("template-publisher",
            file("template-publisher"),
            settings = buildSettings ++ Seq(fork in run := true) ++ Seq(libraryDependencies ++= templatePublisherDeps))

  lazy val webView =
    Project("web-view",
            file("web-view"),
            settings =
              buildSettings
            ++ Seq(
                artifactName := { (version: ScalaVersion, module: ModuleID, artifact: Artifact) =>
                  "javaee7-web." + artifact.extension
                })
            ++ WebPlugin.webSettings
            ++ Seq(libraryDependencies ++= webViewDeps))
}
