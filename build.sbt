import Settings.*

ThisBuild / scalaVersion := Versions.scala2_12
ThisBuild / organization := "cloud.golem"

lazy val root = (project in file("."))
  .settings(
    name := "sbt-wasm-component",
    addSbtPlugin("org.scala-js" % "sbt-scalajs" % Versions.scalaJS)
  )
  .settings(scriptedLaunchOpts += s"-Dplugin.version=${version.value}")
  .enablePlugins(SbtPlugin)
  .enablePlugins(BuildInfoPlugin)
  .publishSettings
