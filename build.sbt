name := "simple-blockchain"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies += "org.scorexfoundation" %% "scrypto" % "2.1.6"
libraryDependencies += "io.bretty" % "console-tree-builder" % "2.0"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
libraryDependencies += "io.monix" %% "monix" % "3.0.0-RC2"
libraryDependencies += "com.google.guava" % "guava" % "27.0.1-jre"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"
