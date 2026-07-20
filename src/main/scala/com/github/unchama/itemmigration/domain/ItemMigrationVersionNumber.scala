package com.github.unchama.itemmigration.domain

import cats.data.NonEmptyList

case class ItemMigrationVersionNumber(components: NonEmptyList[ItemMigrationVersionComponent]) {

  def versionString: String = components.map(_.value.toString).toList.mkString(".")

}

object ItemMigrationVersionNumber {

  import cats.implicits._
  import eu.timepit.refined._
  import eu.timepit.refined.api.Refined
  import eu.timepit.refined.numeric.NonNegative

  import scala.quoted.{Expr, Quotes, Varargs}

  def apply(
    versionHead: ItemMigrationVersionComponent,
    versionRest: ItemMigrationVersionComponent*
  ): ItemMigrationVersionNumber = {
    ItemMigrationVersionNumber(NonEmptyList.of(versionHead, versionRest: _*))
  }

  /**
   * バージョン成分の整数リテラル列から [[ItemMigrationVersionNumber]] を構築する。
   *
   * 成分数は任意（1つ以上）で、Scala 2時代にrefined.auto._のマクロと可変長引数の
   * 組み合わせで可能だった構築（`ItemMigrationVersionNumber(1, 0)` 等）と同じ形を
   * 受け入れる。各成分が非負の整数リテラルであることはコンパイル時に検証される。
   */
  inline def apply(inline components: Int*): ItemMigrationVersionNumber =
    ${ literalApplyImpl('components) }

  private def literalApplyImpl(
    components: Expr[Seq[Int]]
  )(using quotes: Quotes): Expr[ItemMigrationVersionNumber] = {
    import quotes.reflect.report

    components match {
      case Varargs(componentExprs) =>
        val componentValues = componentExprs.map { componentExpr =>
          componentExpr
            .value
            .getOrElse(report.errorAndAbort("バージョン成分は整数リテラルでなければならない", componentExpr))
        }

        if (componentValues.isEmpty)
          report.errorAndAbort("バージョン成分は1つ以上指定しなければならない")

        if (componentValues.exists(_ < 0))
          report.errorAndAbort("バージョン成分は非負のリテラルでなければならない")

        val componentList = Expr.ofList(componentValues.toList.map { value =>
          '{ Refined.unsafeApply[Int, NonNegative](${ Expr(value) }) }
        })

        '{ ItemMigrationVersionNumber(NonEmptyList.fromListUnsafe($componentList)) }
      case _ =>
        report.errorAndAbort("バージョン成分はリテラルの列でなければならない")
    }
  }

  def fromString(string: String): Option[ItemMigrationVersionNumber] =
    string
      .split('.')
      .toList
      .traverse(_.toIntOption)
      .flatMap { components =>
        components.traverse(component => refineV[NonNegative](component)).toOption
      }
      .flatMap { componentList =>
        NonEmptyList.fromList(componentList).map(ItemMigrationVersionNumber(_))
      }

}
