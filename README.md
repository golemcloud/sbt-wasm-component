golem-scala
===========

Avoid any boilerplate in your project by using just one annotation to export your Golem worker from Scala to JS.

Setup
-----

Add golem-scala as a dependency in `project/plugins.sbt`:

```scala
addSbtPlugin("cloud.golem" % "golem-scala" % "x.y.z")
```

Usage
-----

Golem-scala is automatically loaded, it just needs to be enabled with `enablePlugins(GolemScalaPlugin)` in your `build.sbt`:

```scala
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.13"

lazy val root = (project in file("."))
  .enablePlugins(GolemScalaPlugin)
```

Then you will be able to annotate your Golem worker object with the `@cloud.golem.Worker` annotation:

```scala
package example

@cloud.golem.Worker
object ShoppingCart { self =>

  def initializeCart(userId: String): String = {
    println(s"Initializing cart for user $userId")
    if (math.random() > 0.1) userId
    else "Error while initializing cart"
  }
  
  // ...

}

```

Once done that, it will be enough to run `sbt fullLinkJS` and the plugin will take care of exporting your worker in JS.

