package com.github.unchama.seichiassist.subsystems.seasonalevents.newyear

import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.subsystems.seasonalevents.newyear.NewYear.{isInEvent, itemDropRate}
import com.github.unchama.seichiassist.subsystems.seasonalevents.newyear.NewYearItemData._
import com.github.unchama.seichiassist.util.Util.{addItem, dropItem, isPlayerInventoryFull}
import com.github.unchama.seichiassist.{ManagedWorld, SeichiAssist}
import com.github.unchama.util.external.WorldGuardWrapper.isRegionMember
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor._
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.{PlayerItemConsumeEvent, PlayerJoinEvent}
import org.bukkit.event.{EventHandler, EventPriority, Listener}
import java.time.LocalDate
import java.util.Random

object NewYearListener extends Listener {
  @EventHandler
  def giveSobaToPlayer(event: PlayerJoinEvent): Unit = {
    if (!isInEvent) return

    val player: Player = event.getPlayer

    val playerData: PlayerData = SeichiAssist.playermap(player.getUniqueId)
    if (playerData.hasNewYearSobaGive) return

    if (isPlayerInventoryFull(player)) {
      List(
        "インベントリに空きがなかったため、アイテムを配布できませんでした。",
        "インベントリに空きを作ってから、再度サーバーに参加してください。"
      ).map(str => s"$RED$UNDERLINE$str")
        .foreach(player.sendMessage)
    } else {
      addItem(player, sobaHead)
      playerData.hasNewYearSobaGive = true
      player.sendMessage(s"${BLUE}大晦日ログインボーナスとして記念品を入手しました。")
    }
    player.playSound(player.getLocation, Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f)
  }

  @EventHandler
  def onPlayerConsumedNewYearApple(event: PlayerItemConsumeEvent): Unit = {
    val item = event.getItem
    if (!isNewYearApple(item)) return

    val player = event.getPlayer
    val today = LocalDate.now()
    val expiryDate = new NBTItem(item).getObject(NBTTagConstants.expiryDateTag, classOf[LocalDate])
    if (today.isBefore(expiryDate) || today.isEqual(expiryDate)) {
      val playerData = SeichiAssist.playermap(player.getUniqueId)
      val manaState = playerData.manaState
      val maxMana = manaState.calcMaxManaOnly(player, playerData.level)
      // マナを10%回復する
      manaState.increase(maxMana * 0.1, player, playerData.level)
      player.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F)
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  def onNewYearBagPopped(event: BlockBreakEvent): Unit = {
    if (!isInEvent) return
    if (event.isCancelled) return

    val player = event.getPlayer
    val block = event.getBlock
    if (block == null) return
    if (!ManagedWorld.WorldOps(player.getWorld).isSeichi) return
    if (!isRegionMember(player, block.getLocation)) return

    val rand = new Random().nextDouble()
    if (rand < itemDropRate) {
      if (isPlayerInventoryFull(player)) {
        dropItem(player, newYearBag)
        player.sendMessage(s"${RED}インベントリに空きがなかったため、「お年玉袋」は地面にドロップしました。")
      } else {
        addItem(player, newYearBag)
        player.sendMessage(s"$AQUA「お年玉袋」を見つけたよ！")
      }
      player.playSound(player.getLocation, Sound.BLOCK_NOTE_HARP, 3.0f, 1.0f)
    }
  }
}