package fix

import scalafix.v1._

import scala.meta._

/**
 * Lints on string interpolation where its variable part contains `case class` without `toString`.
 */
// noinspection ScalaUnusedSymbol; referred from scalafix implicitly
// NOTE: see AST on https://xuwei-k.github.io/scalameta-ast/ or https://astexplorer.net
class WarnUnoverriddenImplicitToStringCallsOnCaseClass extends SemanticRule("WarnUnoverriddenImplicitToStringCallsOnCaseClass") {
  override def fix(implicit doc: SemanticDocument): Patch = {
    val s = doc.tree.collect {
      // string interpolation in standard library
      case Term.Interpolate((prefix, _, args)) =>
        if (prefix.value != "s") {
          return Patch.empty
        }

        Patch.fromIterable(
          args.collect { arg =>
            val tp = arg.symbol
            val info = tp.info.get
            // does `tp` point to some `case class`?
            val isCaseClass = info.isCase && info.isClass

            // lazily evaluated since most classes are not `case class`
            lazy val isToStringOverriden = info.overriddenSymbols.exists(overridenMethodSym => overridenMethodSym.value == "toString" && {
              val sig = overridenMethodSym.info.get.signature
              sig match {
                // もしtoString()のreturn typeがStringかそのサブタイプにならないような型であれば、
                // scalafixが走る前にコンパイルが落ちるのでここで改めて考慮する必要はない
                case MethodSignature(List(), List(), _) => true
                case _ => false
              }
            })

            if (!isCaseClass || isToStringOverriden) {
              return Patch.empty
            }

            Patch.lint(new Diagnostic {
              override def message: String = "Case class value shouldn't be interpolated, use `toString` " +
                "if you wish to interpolate the String representation into the string"

              // points to arg
              override def position: _root_.scala.meta.Position = arg.pos
            })
          }
        )
      case _ => Patch.empty
    }

    Patch.fromIterable(s)
  }
}
