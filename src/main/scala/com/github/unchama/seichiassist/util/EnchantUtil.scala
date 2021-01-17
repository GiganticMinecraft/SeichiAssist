package com.github.unchama.seichiassist.util

import org.bukkit.enchantments.Enchantment

import java.util.NoSuchElementException

object EnchantUtil {
  private val levelLessEnchantmentMapping = Map(
    Enchantment.WATER_WORKER -> "水中採掘",
    Enchantment.SILK_TOUCH -> "シルクタッチ",
    Enchantment.ARROW_FIRE -> "フレイム",
    Enchantment.ARROW_INFINITE -> "無限",
    Enchantment.MENDING -> "修繕"
  )
  private val leveledEnchantmentMapping = Map(
    Enchantment.PROTECTION_ENVIRONMENTAL -> "ダメージ軽減",
    Enchantment.PROTECTION_FIRE -> "火炎耐性",
    Enchantment.PROTECTION_FALL -> "落下耐性",
    Enchantment.PROTECTION_EXPLOSIONS -> "爆発耐性",
    Enchantment.PROTECTION_PROJECTILE -> "飛び道具耐性",
    Enchantment.OXYGEN -> "水中呼吸",
    Enchantment.THORNS -> "棘の鎧",
    Enchantment.DEPTH_STRIDER -> "水中歩行",
    Enchantment.FROST_WALKER -> "氷渡り",
    Enchantment.DAMAGE_ALL -> "ダメージ増加",
    Enchantment.DAMAGE_UNDEAD -> "アンデッド特効",
    Enchantment.DAMAGE_ARTHROPODS -> "虫特効",
    Enchantment.KNOCKBACK -> "ノックバック",
    Enchantment.FIRE_ASPECT -> "火属性",
    Enchantment.LOOT_BONUS_MOBS -> "ドロップ増加",
    Enchantment.DIG_SPEED -> "効率強化",
    Enchantment.DURABILITY -> "耐久力",
    Enchantment.LOOT_BONUS_BLOCKS -> "幸運",
    Enchantment.ARROW_DAMAGE -> "射撃ダメージ増加",
    Enchantment.ARROW_KNOCKBACK -> "パンチ",
    Enchantment.LUCK -> "宝釣り",
    Enchantment.LURE -> "入れ食い"
  )
  @deprecated
  def getEnchantName(vaname: String, enchlevel: Int): String = {
    getEnchantName(Enchantment.values().find(_.getName == vaname).getOrElse(throw new NoSuchElementException()), enchlevel)
  }

  def getEnchantName(enchant: Enchantment, level: Int): String = {
    val enchantmentLevelRepresentation = getEnchantLevelRome(level)

    levelLessEnchantmentMapping.get(enchant).orElse(
      leveledEnchantmentMapping.get(enchant)
        .map(localizedName => s"$localizedName $enchantmentLevelRepresentation")
    ).getOrElse(enchant)
  }

  private def getEnchantLevelRome(enchantlevel: Int): String = {
    enchantlevel match {
      case 1 => "Ⅰ"
      case 2 => "Ⅱ"
      case 3 => "Ⅲ"
      case 4 => "Ⅳ"
      case 5 => "Ⅴ"
      case 6 => "Ⅵ"
      case 7 => "Ⅶ"
      case 8 => "Ⅷ"
      case 9 => "Ⅸ"
      case 10 => "Ⅹ"
      case _ => enchantlevel.toString
    }

  }
}
