package com.github.unchama.itemmigration.domain

import cats.data.NonEmptyList

case class ItemMigrationVersionNumber(components: NonEmptyList[ItemMigrationVersionComponent]) {

  def versionString: String = components.map(_.value.toString).toList.mkString(".")

}

object ItemMigrationVersionNumber {

  import cats.implicits._
  import eu.timepit.refined._
  import eu.timepit.refined.numeric.NonNegative

  def fromString(string: String): Option[ItemMigrationVersionNumber] =
    string
      .split('.')
      .toList
      .map(_.toIntOption)
      .sequence
      .flatMap { components =>
        components
          .map(component => refineV[NonNegative](component))
          .sequence
          .toOption
      }.flatMap { componentList =>
      NonEmptyList
        .fromList(componentList)
        .map(ItemMigrationVersionNumber(_))
    }

}
