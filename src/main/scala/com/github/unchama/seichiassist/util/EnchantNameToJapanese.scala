package com.github.unchama.seichiassist.util

object EnchantNameToJapanese {

  private def getEnchantLevelRome(enchantlevel: Int): String = {
    enchantlevel match {
      case 1  => "Ⅰ"
      case 2  => "Ⅱ"
      case 3  => "Ⅲ"
      case 4  => "Ⅳ"
      case 5  => "Ⅴ"
      case 6  => "Ⅵ"
      case 7  => "Ⅶ"
      case 8  => "Ⅷ"
      case 9  => "Ⅸ"
      case 10 => "Ⅹ"
      case _  => enchantlevel.toString
    }

  }

  def getEnchantName(vaname: String, enchlevel: Int): String = {
    val levelLessEnchantmentMapping = Map(
      "WATER_WORKER" -> "水中採掘",
      "SILK_TOUCH" -> "シルクタッチ",
      "ARROW_FIRE" -> "フレイム",
      "ARROW_INFINITE" -> "無限",
      "MENDING" -> "修繕"
    )
    val leveledEnchantmentMapping = Map(
      "PROTECTION_ENVIRONMENTAL" -> "ダメージ軽減",
      "PROTECTION_FIRE" -> "火炎耐性",
      "PROTECTION_FALL" -> "落下耐性",
      "PROTECTION_EXPLOSIONS" -> "爆発耐性",
      "PROTECTION_PROJECTILE" -> "飛び道具耐性",
      "OXYGEN" -> "水中呼吸",
      "THORNS" -> "棘の鎧",
      "DEPTH_STRIDER" -> "水中歩行",
      "FROST_WALKER" -> "氷渡り",
      "DAMAGE_ALL" -> "ダメージ増加",
      "DAMAGE_UNDEAD" -> "アンデッド特効",
      "DAMAGE_ARTHROPODS" -> "虫特効",
      "KNOCKBACK" -> "ノックバック",
      "FIRE_ASPECT" -> "火属性",
      "LOOT_BONUS_MOBS" -> "ドロップ増加",
      "DIG_SPEED" -> "効率強化",
      "DURABILITY" -> "耐久力",
      "LOOT_BONUS_BLOCKS" -> "幸運",
      "ARROW_DAMAGE" -> "射撃ダメージ増加",
      "ARROW_KNOCKBACK" -> "パンチ",
      "LUCK" -> "宝釣り",
      "LURE" -> "入れ食い"
    )
    val enchantmentLevelRepresentation = getEnchantLevelRome(enchlevel)

    levelLessEnchantmentMapping
      .get(vaname)
      .orElse(
        leveledEnchantmentMapping
          .get(vaname)
          .map(localizedName => s"$localizedName $enchantmentLevelRepresentation")
      )
      .getOrElse(vaname)
  }
}
