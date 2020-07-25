package com.github.unchama.seichiassist.mebius.domain.resources

import com.github.unchama.seichiassist.mebius.domain.property.{MebiusEnchantment, MebiusLevel}
import org.bukkit.enchantments.Enchantment

object MebiusEnchantments {
  val list = List(
    MebiusEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, MebiusLevel(2), 10, "ダメージ軽減"),
    MebiusEnchantment(Enchantment.DURABILITY, MebiusLevel(2), 10, "耐久力"),
    MebiusEnchantment(Enchantment.PROTECTION_FIRE, MebiusLevel(6), 10, "火炎耐性"),
    MebiusEnchantment(Enchantment.PROTECTION_PROJECTILE, MebiusLevel(6), 10, "飛び道具耐性"),
    MebiusEnchantment(Enchantment.PROTECTION_EXPLOSIONS, MebiusLevel(6), 10, "爆発耐性"),
    MebiusEnchantment(Enchantment.OXYGEN, MebiusLevel(15), 3, "水中呼吸"),
    MebiusEnchantment(Enchantment.WATER_WORKER, MebiusLevel(15), 1, "水中採掘"),
  )

  assert({
    (2 to MebiusLevel.max).forall { level =>
      list
        .filter(_.unlockLevel.value <= level)
        .map(_.maxLevel)
        .sum >= level
    }
  }, "各レベルにおいて、そのレベルで付与できるエンチャントが存在する")
}
