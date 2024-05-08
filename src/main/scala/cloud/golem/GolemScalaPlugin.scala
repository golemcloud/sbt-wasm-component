package cloud.golem

import sbt.*
import org.scalajs.sbtplugin.ScalaJSPlugin
import sbt.plugins.JvmPlugin

object GolemScalaPlugin extends AutoPlugin {
  object autoImport {
    lazy val golemScalaOutputDirectory = SettingKey[File](
      "golemScalaOutputDirectory",
      "Output directory",
      KeyRanks.Invisible
    )
    lazy val golemScalaWitPath = SettingKey[File](
      "golemScalaWitPath",
      "Path to the wit file",
      KeyRanks.Invisible
    )
    lazy val golemScalaPackageName = SettingKey[String](
      "golemScalaPackageName",
      "Package name",
      KeyRanks.Invisible
    )
    lazy val witBindgen =
      taskKey[Seq[File]](
        "Runs golem-scalajs-wit-bindgen to generate WIT bindings"
      )
    lazy val component =
      taskKey[Unit]("Runs componentize-js on the generated main.js file")
  }

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = JvmPlugin && ScalaJSPlugin

  override lazy val projectSettings: Seq[Setting[?]] =
    GolemScalaPluginInternal.baseSettings ++
      GolemScalaPluginInternal.scalaJsSettings ++
      GolemScalaPluginInternal.macroParadiseSettings
}
