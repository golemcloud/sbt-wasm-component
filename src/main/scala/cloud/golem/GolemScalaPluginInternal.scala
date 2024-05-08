package cloud.golem

import sbt.*
import sbt.Keys.*
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.*

private[golem] object GolemScalaPluginInternal {
  import GolemScalaPlugin.autoImport.*

  private object Versions {
    val macros = "0.1.0"
    val scalaMacrosParadise = "2.1.1"
  }

  lazy val baseSettings: Seq[Setting[?]] =
    Def.settings(
      golemScalaWitPath := (ThisBuild / baseDirectory).value / "wit",
      golemScalaPackageName := "main",
      witBindgen := {
        val golemScalaWitFullPath = (ThisBuild / baseDirectory).value / golemScalaWitPath.value.getPath / s"${golemScalaPackageName.value}.wit"
        import scala.sys.process.*
        Seq(
          "bash",
          "-xc",
          s"golem-scalajs-wit-bindgen -w $golemScalaWitFullPath -p ${golemScalaPackageName.value}"
        ).!!
      },
      component := {
        import scala.sys.process.*
        Seq("bash", "-xc", "npm install").!!
        Seq("bash", "-xc", "npm run build").!!
      },
      component := (component dependsOn (Compile / fullLinkJS)).value
    )

  lazy val scalaJsSettings: Seq[Setting[?]] =
    Def.settings(
      scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
      libraryDependencies += "cloud.golem" %% "golem-scala-macros" % Versions.macros
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
