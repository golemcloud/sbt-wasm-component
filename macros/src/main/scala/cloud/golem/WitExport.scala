package cloud.golem

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

@compileTimeOnly("Enable macro paradise to expand macro annotations")
final class WitExport extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro WitExportMacro.impl
}

object WitExportMacro {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    val result = annottees.head.tree match {
      case q"$mods object $name extends $parent { ..$self => ..$stats }" =>
        val topLevelName = {
          val parentFullName = parent.toString
          // Define export top level as superclass name or "api", if missing
          var n =
            parentFullName.split("\\.").lastOption.getOrElse(parentFullName)
          n = n.replaceAll("\\$", "")
          n = if (n == "AnyRef") "api" else n
          n.toLowerCase
        }
        c.info(
          NoPosition,
          s"Exporting worker object $name to $topLevelName",
          force = false
        )
        val newMods = Modifiers(
          mods.flags | Flag.FINAL,
          mods.privateWithin,
          mods.annotations :+ q"new scala.scalajs.js.annotation.JSExportAll" :+ q"new scala.scalajs.js.annotation.JSExportTopLevel(${s"$topLevelName"})"
        )
        q"""
          $newMods object $name extends $parent { $self =>
            ..$stats
          }
        """

      case _ =>
        c.abort(c.enclosingPosition, "Failed to export worker object")
    }

    c.Expr[Any](result)
  }
}
