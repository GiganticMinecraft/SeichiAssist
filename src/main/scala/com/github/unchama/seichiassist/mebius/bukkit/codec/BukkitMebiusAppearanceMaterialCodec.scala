package com.github.unchama.seichiassist.mebius.bukkit.codec

import com.github.unchama.seichiassist.mebius.domain.MebiusLevel
import org.bukkit.Material

object BukkitMebiusAppearanceMaterialCodec {

  private val appearanceThresholds = List(
    1 -> Material.LEATHER_HELMET,
    5 -> Material.GOLD_HELMET,
    10 -> Material.CHAINMAIL_HELMET,
    20 -> Material.IRON_HELMET,
    25 -> Material.DIAMOND_HELMET
  )

  assert {
    val levelThresholds = appearanceThresholds.map(_._1)
    levelThresholds == levelThresholds.sorted
  }

  def appearanceMaterialAt(level: MebiusLevel): Material = {
    appearanceThresholds
      .find { case (threshold, _) => level.value <= threshold }
      .get // level.value >= 1
      ._2
  }

}
