package fix

import scalafix.v1._

import scala.meta._

/**
 * Lints on string interpolation where its variable part contains `case class` without `toString`.
 */
// noinspection ScalaUnusedSymbol; referred from scalafix implicitly
class ImplicitToStringCallOnCaseClass extends SemanticRule("ImplicitToStringCallOnCaseClass") {
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
                case MethodSignature(List(), List(), returnType) =>
                  val ret = simpleDealias(returnType)
                  ret match {
                    case TypeRef(_, symbol, _) => symbol.value == "java.lang.String"
                    case _ => false
                  }
                case _ => false
              }
            })

            if (!isCaseClass || isToStringOverriden) {
              return Patch.empty
            }

            Patch.lint(new Diagnostic {
              override def message: String = "Case class value shouldn't interpolated, please use `toString` if it is really intended snippet"

              // points to arg
              override def position: _root_.scala.meta.Position = arg.pos
            })
          }
        )
      case _ => Patch.empty
    }

    Patch.fromIterable(s)
  }

  def simpleDealias(tpe: SemanticType)(implicit sym: Symtab): SemanticType = {
    def dealiasSymbol(symbol: Symbol): Symbol =
      symbol.info.get.signature match {
        case TypeSignature(_, lowerBound@TypeRef(_, dealiased, _), upperBound)
          if lowerBound == upperBound =>
          dealiased
        case _ =>
          symbol
      }

    tpe match {
      case TypeRef(prefix, symbol, typeArguments) =>
        TypeRef(prefix, dealiasSymbol(symbol), typeArguments.map(simpleDealias))
      case other => other
    }
  }
}
