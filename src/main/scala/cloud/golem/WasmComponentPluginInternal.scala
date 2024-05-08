package cloud.golem

import buildinfo.BuildInfo
import sbt.*
import sbt.Keys.*
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.*

private[golem] object WasmComponentPluginInternal {
  import WasmComponentPlugin.autoImport.*

  private object Versions {
    val scalaMacrosParadise = "2.1.1"
  }

  lazy val baseSettings: Seq[Setting[?]] = {
    lazy val wasmComponentWitFullPath = Def.task(
      wasmComponentWitPath.value / s"${wasmComponentWitName.value}.wit"
    )
    def checkCommandOrFail(command: String)(error: => String): Unit = {
      import scala.sys.process.*
      val commandExists = Seq("bash", "-xc", s"which $command").! == 0
      if (!commandExists) sys.error(error)
    }
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
          checkCommandOrFail(bindGenCommand) {
            s"""
               |$bindGenCommand not found.
               |
               |Run `cargo install $bindGenCommand` or
               |refer to https://learn.golem.cloud/docs/building-components/tier-1/scala for installation instructions.
            """.stripMargin
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

        val npmCommand = "npm"
        checkCommandOrFail(npmCommand) {
          s"""
            |$npmCommand command not found.
            |
            |Refer to https://nodejs.org/en/download for installation instructions.
          """.stripMargin
        }

        Seq("bash", "-xc", s"$npmCommand install").!!
        Seq("bash", "-xc", s"$npmCommand run build").!!
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
      libraryDependencies += "cloud.golem" %% "sbt-wasm-component-macros" % BuildInfo.version
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
