import sbt.*
import sbt.Keys.*

object Settings {

  implicit final class ProjectSettings(project: sbt.Project) {

    def publishSettings: sbt.Project = {
      import com.jsuereth.sbtpgp.PgpKeys.*
      import xerial.sbt.Sonatype
      import xerial.sbt.Sonatype.SonatypeKeys.*


      project.settings(
        inThisBuild(
          List(
            organization := "cloud.golem",
            homepage := Some(
              url("https://github.com/golemcloud/sbt-wasm-component")
            ),
            licenses := List(
              "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
            ),
            developers := List(
              Developer(
                "danieletorelli",
                "Daniele Torelli",
                "daniele.torelli@ziverge.com",
                url("https://github.com/danieletorelli")
              )
            ),
            pgpPassphrase := sys.env.get("PGP_PASSPHRASE").map(_.toArray),
            pgpPublicRing := file("/tmp/public.asc"),
            pgpSecretRing := file("/tmp/secret.asc"),
            sonatypeCredentialHost := Sonatype.sonatype01
          )
        )
      )
    }

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
