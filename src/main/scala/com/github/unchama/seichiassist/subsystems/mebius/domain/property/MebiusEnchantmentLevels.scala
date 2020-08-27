package com.github.unchama.seichiassist.subsystems.mebius.domain.property

import cats.effect.Sync

import scala.util.Random

class MebiusEnchantmentLevels private(val mapping: Map[MebiusEnchantment, Int]) {

  mapping.foreach { case m@(MebiusEnchantment(_, maxLevel, _), enchantmentLevel) =>
    require(
      1 <= enchantmentLevel && enchantmentLevel <= maxLevel,
      s"$enchantmentLevel is in [1, $maxLevel] for $m"
    )
  }

  def of(enchantment: MebiusEnchantment): Int = mapping.getOrElse(enchantment, 0)

  def has(enchantment: MebiusEnchantment): Boolean = of(enchantment) >= 1

  def addNew(enchantment: MebiusEnchantment): MebiusEnchantmentLevels =
    if (!this.has(enchantment)) {
      new MebiusEnchantmentLevels(mapping.updated(enchantment, 1))
    } else {
      this
    }

  import MebiusLevel.mebiusLevelOrder._

  def isValidAt(mebiusLevel: MebiusLevel): Boolean = {
    if (mebiusLevel.isMaximum && !has(MebiusEnchantment.Unbreakable)) {
      false
    } else {
      mapping.keySet.forall(_.unlockLevel <= mebiusLevel)
    }
  }

  def upgradableEnchantmentsAt(mebiusLevel: MebiusLevel): Set[MebiusEnchantment] = {
    MebiusEnchantment
      .values
      .filter { mebiusEnchantment =>
        val unlocked = mebiusEnchantment.unlockLevel <= mebiusLevel
        val upgradable = this.of(mebiusEnchantment) < mebiusEnchantment.maxLevel

        unlocked && upgradable
      }
      .toSet
  }

  def randomlyUpgradeAt[F[_]](mebiusLevel: MebiusLevel)
                             (implicit F: Sync[F]): F[MebiusEnchantmentLevels] = {
    val upgradableEnchantments = upgradableEnchantmentsAt(mebiusLevel).toSeq

    F.delay {
      val choice = upgradableEnchantments(Random.nextInt(upgradableEnchantments.size))

      new MebiusEnchantmentLevels(mapping.updated(choice, this.of(choice) + 1))
    }
  }

  def differenceFrom(another: MebiusEnchantmentLevels): Set[MebiusEnchantment] = {
    this.mapping.keySet
      .union(another.mapping.keySet)
      .filter { e => this.of(e) != another.of(e) }
  }
}

object MebiusEnchantmentLevels {

  def apply(levelMapping: (MebiusEnchantment, Int)*) = new MebiusEnchantmentLevels(Map(levelMapping: _*))

  def fromUnsafeCounts(counter: MebiusEnchantment => Int): MebiusEnchantmentLevels = {
    new MebiusEnchantmentLevels(
      MebiusEnchantment.values
        .map { e => e -> counter(e) }
        .filter { case (e, l) => 1 <= l && l <= e.maxLevel }
        .toMap
    )
  }

}
