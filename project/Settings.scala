import sbt.*
import sbt.Keys.*

object Settings {

  implicit final class ProjectSettings(project: sbt.Project) {

    def publishSettings: sbt.Project = {
      import com.jsuereth.sbtpgp.PgpKeys.*

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
                url("https://mdtorelli.it")
              )
            ),
            resolvers +=
              "Sonatype OSS Snapshots 01" at "https://s01.oss.sonatype.org/content/repositories/snapshots",
            pgpPassphrase := sys.env.get("PGP_PASSWORD").map(_.toArray),
            pgpPublicRing := file("/tmp/public.asc"),
            pgpSecretRing := file("/tmp/secret.asc"),
            resolvers +=
              "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
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
