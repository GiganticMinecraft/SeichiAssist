package com.github.unchama.itemmigration.domain

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
          .map(_.toIndexedSeq)
      }

  def convertToString(versionNumber: ItemMigrationVersionNumber): String =
    versionNumber.map(c => c.value.toString).mkString(".")

}
