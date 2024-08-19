ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.13.13"
ThisBuild / crossScalaVersions += "2.12.19"

lazy val root = (project in file("."))
  .enablePlugins(WasmComponentPlugin)
  .settings(wasmComponentPackageName := "example")
