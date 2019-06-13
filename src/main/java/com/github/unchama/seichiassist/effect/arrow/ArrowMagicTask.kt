package com.github.unchama.seichiassist.effect.arrow

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.entity.ThrownPotion
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType
import org.bukkit.util.Vector

class ArrowMagicTask(player: Player) : AbstractEffectTask() {
  override val additionalVector: Vector
    get() = Vector(0.0, 1.6, 0.0)

  override val vectorMultiplier: Double
    get() = 0.8

  init {
    //ポーションデータを生成
    val i = ItemStack(Material.SPLASH_POTION).apply {
      itemMeta = (Bukkit.getItemFactory().getItemMeta(Material.SPLASH_POTION) as PotionMeta).apply {
        basePotionData = PotionData(PotionType.INSTANT_HEAL)
      }
    }

    //プレイヤーの位置を取得
    val ploc = player.location
    //スキルを実行する処理
    runEffect<ThrownPotion>(ploc, player, false, Sound.ENTITY_WITCH_THROW) {
      item = i
    }
  }
}
