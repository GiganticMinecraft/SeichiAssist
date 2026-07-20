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

  // 以下のinlineオーバーロードは、Scala 2でrefined.auto._のマクロが担っていた
  // バージョン成分リテラルのコンパイル時検証を置き換えるもの。
  // 成分が非負リテラルでなければコンパイルエラーになる。

  inline def apply(inline v1: Int): ItemMigrationVersionNumber =
    inline if (v1 >= 0)
      ItemMigrationVersionNumber(NonEmptyList.of(Refined.unsafeApply(v1)))
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
