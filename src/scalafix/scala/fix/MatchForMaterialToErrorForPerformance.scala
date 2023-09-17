package fix

import scalafix.v1._

import scala.meta._

/**
 * Lints on string interpolation where its variable part contains `case class` without `toString`.
 */
// noinspection ScalaUnusedSymbol; referred from scalafix implicitly
// NOTE: see AST on https://xuwei-k.github.io/scalameta-ast/ or https://astexplorer.net
class MatchForMaterialToErrorForPerformance extends SemanticRule("MatchForMaterialToErrorForPerformance") {
  override def fix(implicit doc: SemanticDocument): Patch = {
    doc.tree.collect {
      case t @ Term.Match.After_4_4_5(term, _, _) =>
        term.symbol.info match {
          case Some(info) if info.signature.toString == "Material" =>
            val message =
              s"""
                |Don't use org.bukkit.Material in scrutinee of match expressions!
                |See https://github.com/GiganticMinecraft/SeichiAssist/issues/2226 for more detail.""".stripMargin
            Patch.lint(Diagnostic("error", message, t.pos))
          case _ => Patch.empty
        }
    }.asPatch
  }
}
