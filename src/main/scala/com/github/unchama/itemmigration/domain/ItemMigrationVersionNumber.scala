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

  def apply(
    versionHead: ItemMigrationVersionComponent,
    versionRest: ItemMigrationVersionComponent*
  ): ItemMigrationVersionNumber = {
    ItemMigrationVersionNumber(NonEmptyList.of(versionHead, versionRest: _*))
  }

  /**
   * バージョン成分の整数リテラル列から [[ItemMigrationVersionNumber]] を構築する。
   *
   * Scala 2時代はrefined.auto._のマクロと可変長引数の組み合わせにより任意の成分数を
   * 受け入れていたが、Scala 3移行に際して、リテラル構築で受け入れる成分数は3つまでと
   * することで合意した（既存のバージョン定義はすべて3成分以下）。
   * 各成分が非負の整数リテラルであることはコンパイル時に検証される。
   * 4成分以上が必要になった場合はオーバーロードを追加すること。
   */
  inline def apply(inline v1: Int): ItemMigrationVersionNumber =
    inline if (v1 >= 0)
      ItemMigrationVersionNumber(NonEmptyList.of(Refined.unsafeApply(v1)))
    else
      scala.compiletime.error("バージョン成分は非負のリテラルでなければならない")

  inline def apply(inline v1: Int, inline v2: Int): ItemMigrationVersionNumber =
    inline if (v1 >= 0 && v2 >= 0)
      ItemMigrationVersionNumber(
        NonEmptyList.of(Refined.unsafeApply(v1), Refined.unsafeApply(v2))
      )
    else
      scala.compiletime.error("バージョン成分は非負のリテラルでなければならない")

  inline def apply(inline v1: Int, inline v2: Int, inline v3: Int): ItemMigrationVersionNumber =
    inline if (v1 >= 0 && v2 >= 0 && v3 >= 0)
      ItemMigrationVersionNumber(
        NonEmptyList
          .of(Refined.unsafeApply(v1), Refined.unsafeApply(v2), Refined.unsafeApply(v3))
      )
    else
      scala.compiletime.error("バージョン成分は非負のリテラルでなければならない")

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
