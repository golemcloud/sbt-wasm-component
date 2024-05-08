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

  lazy val baseSettings: Seq[Setting[?]] = {
    lazy val golemScalaWitFullPath =
      Def
        .task(golemScalaWitPath.value / s"${golemScalaPackageName.value}.wit")
    Def.settings(
      golemScalaOutputDirectory := target.value / "dist",
      golemScalaWitPath := (ThisBuild / baseDirectory).value / "wit",
      golemScalaPackageName := moduleName.value,
      witBindgen := {
        if (!golemScalaWitFullPath.value.exists()) {
          sys.error(s"""
          |'${golemScalaWitFullPath.value.getAbsolutePath}' does not exist.
          |Make sure 'golemScalaPackageName' is set correctly in your build.sbt
         """.stripMargin)
        } else {
          val golemScalaWitBindgenOutput = (Compile / sourceManaged).value / "scala" / golemScalaPackageName.value / "Api.scala"
          import scala.sys.process.*

          val output = Seq(
            "bash",
            "-xc",
            s"golem-scalajs-wit-bindgen -w ${golemScalaWitFullPath.value} -p ${golemScalaPackageName.value}"
          ).!!

          IO.write(golemScalaWitBindgenOutput, output)
          Seq(golemScalaWitBindgenOutput)
        }
      },
      component := {
        import scala.sys.process.*
        Seq("bash", "-xc", "npm install").!!
        Seq("bash", "-xc", "npm run build").!!
      },
      component := (component dependsOn (Compile / fullLinkJS)).value,
      Compile / sourceGenerators += Def.taskIf {
        if (golemScalaWitFullPath.value.exists()) witBindgen.value
        else {
          println(
            s"""
               |'${golemScalaWitFullPath.value.getAbsolutePath}' does not exist.
               |Make sure 'golemScalaPackageName' is set correctly in your build.sbt""".stripMargin
          )
          Nil
        }
      }.taskValue
    )
  }

  lazy val scalaJsSettings: Seq[Setting[?]] =
    Def.settings(
      scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
      Compile / fullLinkJS / scalaJSLinkerOutputDirectory := golemScalaOutputDirectory.value,
      Compile / fastLinkJS / scalaJSLinkerOutputDirectory := golemScalaOutputDirectory.value,
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
