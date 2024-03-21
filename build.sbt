import Settings.*

ThisBuild / scalaVersion := Versions.scala2_12
ThisBuild / organization := "cloud.golem"

lazy val root = (project in file("."))
  .settings(
    name := "golem-scala",
    addSbtPlugin("org.scala-js" % "sbt-scalajs" % Versions.scalaJS)
  )
  .settings(scriptedLaunchOpts += s"-Dplugin.version=${version.value}")
  .enablePlugins(SbtPlugin)
  .dependsOn(macros)
  .aggregate(macros)

lazy val macros = project
  .settings(
    name := "golem-scala-macros",
    crossScalaVersions += Versions.scala2_13,
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  )
  .macroParadiseSettings
