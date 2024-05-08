package cloud.golem

import sbt.*
import sbt.Keys.*
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.*
import sbt.KeyRanks.BSetting
import sbt.plugins.JvmPlugin

object GolemScalaPlugin extends AutoPlugin {
  private object Versions {
    val macros = "0.1.0"
  }

  object autoImport {
    val outputDirectory =
      SettingKey[File](
        "outputDirectory",
        "Directory for linker output",
        BSetting
      )

    val witBindgen =
      taskKey[Unit]("Runs golem-scalajs-wit-bindgen to generate WIT bindings")

    val component =
      taskKey[Unit]("Runs componentize-js on the generated main.js file")

    lazy val baseSettings: Seq[Setting[?]] =
      Def.settings(
        scalacOptions := Seq("-Ymacro-annotations"),
        witBindgen := {
          import scala.sys.process.*
          Seq(
            "bash",
            "-xc",
            "golem-scalajs-wit-bindgen -w wit/main.wit -p example"
          ).!!
        },
        component := {
          import scala.sys.process.*
          Seq("bash", "-xc", "npm install").!!
          Seq("bash", "-xc", "npm run build").!!
        },
        component := (component dependsOn (Compile / fullLinkJS)).value
      )
  }

  import autoImport.*

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = JvmPlugin && ScalaJSPlugin

  override lazy val projectSettings: Seq[Setting[?]] = baseSettings ++ Seq(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    Compile / fullLinkJS / scalaJSLinkerOutputDirectory := outputDirectory.value,
    libraryDependencies += "cloud.golem" %% "golem-scala-macros" % Versions.macros
  )
}
