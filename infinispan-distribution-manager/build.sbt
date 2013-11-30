name := "infinispan-distribution-manager"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.10.1"

organization := "littlewings"

fork in run := true

libraryDependencies += "org.infinispan" % "infinispan-core" % "5.3.0.CR1"
