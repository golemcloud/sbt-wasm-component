package cloud.golem

import sbt.*
import sbt.Keys.*
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.*

private[golem] object WasmComponentPluginInternal {

  import WasmComponentPlugin.autoImport.*

  lazy val baseSettings: Seq[Setting[?]] = {
    lazy val wasmComponentWitFullPath = Def.task(
      wasmComponentWitPath.value / s"${wasmComponentWitName.value}.wit"
    )

    def checkCommandOrFail(command: String)(error: => String): Unit = {
      import scala.sys.process.*
      val commandExists = Seq("sh", "-xc", s"which $command").! == 0
      if (!commandExists) sys.error(error)
    }

    Def.settings(
      wasmComponentOutputDirectory := target.value / "dist",
      wasmComponentWitPath := (ThisBuild / baseDirectory).value / "wit",
      wasmComponentPackageName := moduleName.value,
      wasmComponentWitName := wasmComponentPackageName.value,
      wasmComponentWitBindgen := {
        if (!wasmComponentWitFullPath.value.exists()) {
          sys.error(
            s"""
               |'${wasmComponentWitFullPath.value.getAbsolutePath}' does not exist.
               |Make sure 'wasmComponentPackageName' is set correctly in your build.sbt
         """.stripMargin)
        } else {
          val bindgenOutput = (Compile / sourceManaged).value / "scala"
          val version = (Compile / scalaVersion).value

          val scalaDialect = if (version.startsWith("3.")) "scala3" else "scala2"

          import scala.sys.process.*
          val bindGenCommand = "/home/vigoo/projects/wit-bindgen-scalajs/target/debug/wit-bindgen-scalajs" // TODO: replace to wit-bindgen-scalajs
          checkCommandOrFail(bindGenCommand) {
            s"""
               |$bindGenCommand not found.
               |
               |Run `cargo install $bindGenCommand` or
               |refer to https://learn.golem.cloud/docs/experimental-languages/scala-language-guide/setup for installation instructions.
            """.stripMargin
          }
          Seq(
            "sh",
            "-xc",
            s"$bindGenCommand scala-js ${wasmComponentWitFullPath.value} " +
              s"--out-dir $bindgenOutput " +
              s"--base-package '${wasmComponentPackageName.value}.bindings' " +
              s"--scala-dialect $scalaDialect"
          ).!

          ((bindgenOutput / wasmComponentPackageName.value / "bindings") ** "*.scala").get()
        }
      },
      wasmComponentRegenerateSkeleton := {
        if (!wasmComponentWitFullPath.value.exists()) {
          sys.error(
            s"""
               |'${wasmComponentWitFullPath.value.getAbsolutePath}' does not exist.
               |Make sure 'wasmComponentPackageName' is set correctly in your build.sbt
         """.stripMargin)
        } else {
          val base = (Compile / baseDirectory).value
          val bindingsOutput = (Compile / sourceManaged).value / "scala"
          val skeletonOutput = (Compile / sourceDirectory).value / "scala"
          val version = (Compile / scalaVersion).value

          val scalaDialect = if (version.startsWith("3.")) "scala3" else "scala2"

          val relativeBindgenOutput = bindingsOutput.relativeTo(base).get
          val relativeSkeletonOutput = skeletonOutput.relativeTo(base).get

          import scala.sys.process.*

          val bindGenCommand = "/home/vigoo/projects/wit-bindgen-scalajs/target/debug/wit-bindgen-scalajs" // TODO: replace to wit-bindgen-scalajs
          checkCommandOrFail(bindGenCommand) {
            s"""
               |$bindGenCommand not found.
               |
               |Run `cargo install $bindGenCommand` or
               |refer to https://learn.golem.cloud/docs/experimental-languages/scala-language-guide/setup for installation instructions.
            """.stripMargin
          }
          Seq(
            "sh",
            "-xc",
            s"$bindGenCommand scala-js ${wasmComponentWitFullPath.value} " +
              s"--out-dir $base " +
              s"--base-package '${wasmComponentPackageName.value}.bindings' " +
              s"--generate-skeleton " +
              s"--skeleton-base-package ${wasmComponentPackageName.value} " +
              s"--skeleton-root $relativeSkeletonOutput " +
              s"--binding-root $relativeBindgenOutput " +
              s"--scala-dialect $scalaDialect"
          ).!
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

        Seq("sh", "-xc", s"$npmCommand install").!!
        Seq("sh", "-xc", s"$npmCommand run build").!!
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
      scalaJSLinkerConfig ~= {
        _.withModuleKind(ModuleKind.ESModule)
      },
      Compile / fullLinkJS / scalaJSLinkerOutputDirectory := wasmComponentOutputDirectory.value,
      Compile / fastLinkJS / scalaJSLinkerOutputDirectory := wasmComponentOutputDirectory.value,
    )
}
