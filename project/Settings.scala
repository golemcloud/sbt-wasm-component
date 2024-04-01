import sbt.*
import sbt.Keys.*

object Settings {

  implicit final class ProjectSettings(project: sbt.Project) {

    def macroParadiseSettings: sbt.Project =
      project.settings(
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

}
