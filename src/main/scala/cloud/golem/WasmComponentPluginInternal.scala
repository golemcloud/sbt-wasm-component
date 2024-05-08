package cloud.golem

import sbt.*
import sbt.Keys.*
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.*

private[golem] object WasmComponentPluginInternal {
  import WasmComponentPlugin.autoImport.*

  private object Versions {
    val macros = "0.1.0"
    val scalaMacrosParadise = "2.1.1"
  }

  lazy val baseSettings: Seq[Setting[?]] = {
    lazy val wasmComponentWitFullPath = Def.task(
      wasmComponentWitPath.value / s"${wasmComponentWitName.value}.wit"
    )
    Def.settings(
      wasmComponentOutputDirectory := target.value / "dist",
      wasmComponentWitPath := (ThisBuild / baseDirectory).value / "wit",
      wasmComponentPackageName := moduleName.value,
      wasmComponentWitName := wasmComponentPackageName.value,
      wasmComponentWitBindgen := {
        if (!wasmComponentWitFullPath.value.exists()) {
          sys.error(s"""
          |'${wasmComponentWitFullPath.value.getAbsolutePath}' does not exist.
          |Make sure 'wasmComponentPackageName' is set correctly in your build.sbt
         """.stripMargin)
        } else {
          val wasmComponentWitBindgenOutput = (Compile / sourceManaged).value / "scala" / wasmComponentPackageName.value / "Api.scala"
          import scala.sys.process.*

          val bindGenCommand = "golem-scalajs-wit-bindgen"
          val bindgenCommandExists = Seq("bash", "-xc", s"which $bindGenCommand").! == 0
          if (!bindgenCommandExists) {
            sys.error(s"""
            |$bindGenCommand not found. Run `cargo install $bindGenCommand`.
            |https://learn.golem.cloud/docs/building-components/tier-1/scala
            """.stripMargin)
          }

          val output = Seq(
            "bash",
            "-xc",
            s"$bindGenCommand -w ${wasmComponentWitFullPath.value} -p ${wasmComponentPackageName.value}"
          ).!!

          IO.write(wasmComponentWitBindgenOutput, output)
          Seq(wasmComponentWitBindgenOutput)
        }
      },
      wasmComponent := {
        import scala.sys.process.*
        Seq("bash", "-xc", "npm install").!!
        Seq("bash", "-xc", "npm run build").!!
      },
      wasmComponent := (wasmComponent dependsOn (Compile / fullLinkJS)).value,
      Compile / sourceGenerators += Def.taskIf {
        if (wasmComponentWitFullPath.value.exists())
          wasmComponentWitBindgen.value
        else {
          println(
            s"""
               |'${wasmComponentWitFullPath.value.getAbsolutePath}' does not exist.
               |Make sure 'wasmComponentPackageName' is set correctly in your build.sbt""".stripMargin
          )
          Nil
        }
      }.taskValue
    )
  }

  lazy val scalaJsSettings: Seq[Setting[?]] =
    Def.settings(
      scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
      Compile / fullLinkJS / scalaJSLinkerOutputDirectory := wasmComponentOutputDirectory.value,
      Compile / fastLinkJS / scalaJSLinkerOutputDirectory := wasmComponentOutputDirectory.value,
      libraryDependencies += "cloud.golem" %% "sbt-wasm-component-macros" % Versions.macros
    )

  lazy val macroParadiseSettings: Seq[Setting[?]] = Def.settings(
    scalacOptions ++= {
      if (scalaVersion.value.startsWith("2.13")) Seq("-Ymacro-annotations")
      else Nil
    },
    libraryDependencies ++= {
      if (scalaVersion.value.startsWith("2.12")) {
        Seq(
          compilerPlugin(
            "org.scalamacros" % "paradise" % Versions.scalaMacrosParadise cross CrossVersion.full
          )
        )
      } else Nil
    }
  )
}
