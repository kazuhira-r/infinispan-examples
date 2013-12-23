name := "infinispan-statetransfer"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.10.3"

organization := "org.littlewings"

fork in Test := true

scalacOptions ++= Seq("-deprecation")

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-core" % "6.0.0.Final",
  "net.jcip" % "jcip-annotations" % "1.0",
  "org.scalatest" %% "scalatest" % "2.0" % "test" 
)
