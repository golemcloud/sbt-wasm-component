package cloud.golem

import sbt.*
import org.scalajs.sbtplugin.ScalaJSPlugin
import sbt.plugins.JvmPlugin

object GolemScalaPlugin extends AutoPlugin {
  object autoImport {
    lazy val golemScalaWitPath = settingKey[File]("Path to the wit file")
    lazy val golemScalaPackageName = settingKey[String]("Package name")
    lazy val witBindgen =
      taskKey[Unit]("Runs golem-scalajs-wit-bindgen to generate WIT bindings")
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
