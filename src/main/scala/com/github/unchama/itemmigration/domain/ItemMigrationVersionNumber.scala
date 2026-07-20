package com.github.unchama.itemmigration.domain

import cats.data.NonEmptyList

case class ItemMigrationVersionNumber(components: NonEmptyList[ItemMigrationVersionComponent]) {

  def versionString: String = components.map(_.toString).toList.mkString(".")

}

object ItemMigrationVersionNumber {

  import cats.implicits._
  import io.github.iltotore.iron.constraint.numeric.GreaterEqual
  import io.github.iltotore.iron.refineEither

  /**
   * バージョン成分から [[ItemMigrationVersionNumber]] を構築する。
   *
   * 成分数は任意（1つ以上）。リテラルを渡した場合、非負であることはIronにより
   * コンパイル時に検証される（Scala 2時代のrefined.auto._と同じ受け入れ範囲）。
   */
  def apply(
    versionHead: ItemMigrationVersionComponent,
    versionRest: ItemMigrationVersionComponent*
  ): ItemMigrationVersionNumber = {
    ItemMigrationVersionNumber(NonEmptyList.of(versionHead, versionRest: _*))
  }

  def fromString(string: String): Option[ItemMigrationVersionNumber] =
    string
      .split('.')
      .toList
      .traverse(_.toIntOption)
      .flatMap { components =>
        components.traverse(component => component.refineEither[GreaterEqual[0]]).toOption
      }
      .flatMap { componentList =>
        NonEmptyList.fromList(componentList).map(ItemMigrationVersionNumber(_))
      }

}
