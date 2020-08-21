package com.github.unchama.seichiassist.mebius.domain.property

import cats.effect.Sync

import scala.util.Random

class MebiusEnchantmentLevels private(enchantmentLevels: Map[MebiusEnchantment, Int] = Map()) {

  enchantmentLevels.foreach { case m@(MebiusEnchantment(_, maxLevel, _), enchantmentLevel) =>
    require(
      1 <= enchantmentLevel && enchantmentLevel <= maxLevel,
      s"$enchantmentLevel is in [1, $maxLevel] for $m"
    )
  }

  def of(enchantment: MebiusEnchantment): Int = enchantmentLevels.getOrElse(enchantment, 0)

  def has(enchantment: MebiusEnchantment): Boolean = of(enchantment) >= 1

  import MebiusLevel.mebiusLevelOrder._

  def isValidAt(mebiusLevel: MebiusLevel): Boolean = {
    if (mebiusLevel.isMaximum && !has(MebiusEnchantment.Unbreakable)) {
      false
    } else {
      enchantmentLevels.keySet.forall(_.unlockLevel <= mebiusLevel)
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

      new MebiusEnchantmentLevels(enchantmentLevels.updated(choice, this.of(choice) + 1))
    }
  }

}

object MebiusEnchantmentLevels {

  def apply(levelMapping: (MebiusEnchantment, Int)*) = new MebiusEnchantmentLevels(Map(levelMapping: _*))

}
