package com.github.unchama.seichiassist.subsystems.seasonalevents.halloween

import com.github.unchama.seichiassist.subsystems.seasonalevents.halloween.Halloween.{
  END_DATE,
  blogArticleUrl,
  isInEvent
}
import com.github.unchama.seichiassist.subsystems.seasonalevents.halloween.HalloweenItemData.{
  isHalloweenHoe,
  isHalloweenPotion
}
import com.github.unchama.util.external.WorldGuardWrapper
import org.bukkit.ChatColor.{DARK_GREEN, LIGHT_PURPLE, UNDERLINE}
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.{PlayerInteractEvent, PlayerItemConsumeEvent, PlayerJoinEvent}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.{PotionEffect, PotionEffectType}

object HalloweenItemListener extends Listener {

  @EventHandler
  def onPlayerJoin(event: PlayerJoinEvent): Unit = {
    if (isInEvent) {
      Seq(
        s"$LIGHT_PURPLE${END_DATE}までの期間限定で、ハロウィンイベントを開催しています。",
        "詳しくは下記URLのサイトをご覧ください。",
        s"$DARK_GREEN$UNDERLINE$blogArticleUrl"
      ).foreach(event.getPlayer.sendMessage(_))
    }
  }

  @EventHandler
  def onPlayerConsumeHalloweenPotion(event: PlayerItemConsumeEvent): Unit = {
    if (isHalloweenPotion(event.getItem)) {
      // 1.12.2では、Saturationのポーションは効果がないので、PotionEffectとして直接Playerに付与する
      // 10分
      event
        .getPlayer
        .addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20 * 60 * 10, 0))
    }
  }

  @EventHandler
  def onPlayerRightClickWithHalloweenHoe(event: PlayerInteractEvent): Unit = {
    if (!isHalloweenHoe(event.getItem)) return

    /*
    特殊エンチャントの処理
    HalloweenHoeで右クリックされたブロックの半径4ブロック以内の草及び土ブロックを耕地に変える
    ただし、耕地になるのは、そのブロックが自分がOwnerかMemberになっている保護の領域内である場合のみ
     */
    if (event.getHand == EquipmentSlot.OFF_HAND) return
    if (event.getAction != Action.RIGHT_CLICK_BLOCK) return

    val clickedBlock = event.getClickedBlock
    if (clickedBlock == null) return

    val player = event.getPlayer
    // まず、Playerが自分でクリックしたブロックについて判定する
    if (!canBeReplacedWithSoil(player, clickedBlock)) return
    clickedBlock.setType(Material.FARMLAND)

    // 次にクリックされたブロックから半径4ブロック以内のブロックについて判定する
    for (relX <- -4 to 4; relZ <- -4 to 4) {
      val block = clickedBlock.getRelative(relX, 0, relZ)
      if (block != null && canBeReplacedWithSoil(player, block))
        block.setType(Material.FARMLAND)
    }
  }

  private def canBeReplacedWithSoil(player: Player, block: Block) = {
    (block.getType == Material.FARMLAND || block.getType == Material.GRASS_BLOCK || block
      .getType() == Material.DIRT) && WorldGuardWrapper.isRegionMember(
      player,
      block.getLocation
    )
  }
}
