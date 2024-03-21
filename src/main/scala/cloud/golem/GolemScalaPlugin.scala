package cloud.golem

import sbt.*
import sbt.Keys.*
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.*

object GolemScalaPlugin extends AutoPlugin {
  private object Versions {
    val macros = "0.1.0"
    val scalaMacrosParadise = "2.1.1"
  }

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = ScalaJSPlugin

  override lazy val projectSettings: Seq[Setting[?]] = Seq(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    libraryDependencies += "cloud.golem" %% "golem-scala-macros" % Versions.macros,
  ) ++ macroParadiseSettings

  private lazy val macroParadiseSettings = Seq(
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
