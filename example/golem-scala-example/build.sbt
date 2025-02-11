ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.5"

lazy val root = (project in file("."))
  .settings(
    name := "golem-scala-example",
    wasmComponentPackageName := "example",
    wasmComponentWitPath := file("wit-generated")
  )
  .enablePlugins(WasmComponentPlugin)

