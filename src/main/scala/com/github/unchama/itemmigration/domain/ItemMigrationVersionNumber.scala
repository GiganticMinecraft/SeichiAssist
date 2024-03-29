package com.github.unchama.itemmigration.domain

import cats.data.NonEmptyList

case class ItemMigrationVersionNumber(components: NonEmptyList[ItemMigrationVersionComponent]) {

  def versionString: String = components.map(_.value.toString).toList.mkString(".")

}

object ItemMigrationVersionNumber {

  import cats.implicits._
  import eu.timepit.refined._
  import eu.timepit.refined.numeric.NonNegative

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
        components.traverse(component => refineV[NonNegative](component)).toOption
      }
      .flatMap { componentList =>
        NonEmptyList.fromList(componentList).map(ItemMigrationVersionNumber(_))
      }

}
