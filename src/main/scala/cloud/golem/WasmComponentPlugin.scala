package cloud.golem

import sbt.*
import org.scalajs.sbtplugin.ScalaJSPlugin
import sbt.plugins.JvmPlugin

object WasmComponentPlugin extends AutoPlugin {
  object autoImport {
    lazy val wasmComponentOutputDirectory = SettingKey[File](
      "wasmComponentOutputDirectory",
      "Output directory",
      KeyRanks.Invisible
    )
    lazy val wasmComponentWitPath = SettingKey[File](
      "wasmComponentWitPath",
      "Path to the WIT directory",
      KeyRanks.Invisible
    )
    lazy val wasmComponentWitName = SettingKey[String](
      "wasmComponentWitName",
      "WIT filename without extension",
      KeyRanks.Invisible
    )
    lazy val wasmComponentPackageName = SettingKey[String](
      "wasmComponentPackageName",
      "Package name",
      KeyRanks.Invisible
    )
    lazy val wasmComponentWitBindgen =
      taskKey[Seq[File]](
        "Runs golem-scalajs-wit-bindgen to generate WIT bindings"
      )
    lazy val wasmComponent =
      taskKey[Unit]("Runs componentize-js on the generated ScalaJS file")
  }

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = JvmPlugin && ScalaJSPlugin

  override lazy val projectSettings: Seq[Setting[?]] =
    WasmComponentPluginInternal.baseSettings ++
      WasmComponentPluginInternal.scalaJsSettings ++
      WasmComponentPluginInternal.macroParadiseSettings
}
