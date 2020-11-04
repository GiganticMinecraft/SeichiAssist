package com.github.unchama.seasonalevents.newyear

import java.time.LocalDate
import java.util.Random

import com.github.unchama.seasonalevents.newyear.NewYear.{PREV_EVENT_YEAR, isInEvent, itemDropRate}
import com.github.unchama.seasonalevents.newyear.NewYearItemData._
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.util.Util.{addItem, dropItem, isPlayerInventoryFull}
import com.github.unchama.seichiassist.{ManagedWorld, SeichiAssist}
import com.github.unchama.util.external.WorldGuardWrapper.isRegionMember
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor.{AQUA, RED, UNDERLINE, YELLOW}
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.{PlayerItemConsumeEvent, PlayerJoinEvent}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.{Bukkit, Sound}

class NewYearListener(instance: SeichiAssist) extends Listener {
  @EventHandler
  def giveSobaToPlayer(event: PlayerJoinEvent): Unit = {
    if (!isInEvent) return

    val player: Player = event.getPlayer

    new BukkitRunnable {
      override def run(): Unit = {
        if (!SeichiAssist.playermap.contains(player.getUniqueId)) return

        val playerData: PlayerData = SeichiAssist.playermap(player.getUniqueId)
        if (playerData.hasNewYearSobaGive) return

        if (isPlayerInventoryFull(player)) {
          List(
            "インベントリに空きがなかったため、アイテムを配布できませんでした。",
            "インベントリに空きを作ってから、再度サーバーに参加してください。"
          ).map(str => s"$RED$UNDERLINE$str")
            .foreach(player.sendMessage)
        } else {
          // 配布しているヘッドはこれ： https://minecraft-heads.com/custom-heads/food-drinks/413-bowl-of-noodles
          val command = s"""give ${player.getName} skull 1 3 {display:{Name:"年越し蕎麦(${PREV_EVENT_YEAR}年)",Lore:["", "${YELLOW}大晦日記念アイテムだよ！"]},SkullOwner:{Id:"f15ab073-412e-4fe2-8668-1be12066e2ac",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjY4MzRiNWIyNTQyNmRlNjM1MzhlYzgyY2E4ZmJlY2ZjYmIzZTY4MmQ4MDYzNjQzZDJlNjdhNzYyMWJkIn19fQ=="}]}}}"""
          Bukkit.dispatchCommand(Bukkit.getConsoleSender, command)
          playerData.hasNewYearSobaGive_$eq(true)
        }
        player.playSound(player.getLocation, Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f)
      }
    }.runTaskLater(instance, 200L)
  }

  @EventHandler
  def onPlayerConsumedNewYearApple(event: PlayerItemConsumeEvent): Unit = {
    val item = event.getItem
    if (!isNewYearApple(item)) return

    val player = event.getPlayer
    val today = LocalDate.now()
    val exp = new NBTItem(item).getObject(NBTTagConstants.expirationDateTag, classOf[LocalDate])
    if (today.isBefore(exp) || today.isEqual(exp)) {
      val playerUuid = player.getUniqueId

      // この条件分岐がfalseになる可能性は通常ない（ログインしている限りplayerMapにはそのMCIDのデータが有るはずだ）が、なっている事例があるので念の為
      // 参照：https://github.com/GiganticMinecraft/SeichiAssist/issues/707
      if (SeichiAssist.playermap.contains(playerUuid)) {
        val playerData = SeichiAssist.playermap(playerUuid)
        val manaState = playerData.manaState
        val maxMana = manaState.calcMaxManaOnly(player, playerData.level)
        // マナを10%回復する
        manaState.increase(maxMana * 0.1, player, playerData.level)
        player.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F)
      } else {
        Bukkit.getServer.getLogger.info(s"${player.getName}によって正月りんごが使用されましたが、プレイヤーデータが存在しなかったため、マナ回復が行われませんでした。")
      }
    }
  }

  @EventHandler
  def onNewYearBagPopped(event: BlockBreakEvent): Unit = {
    if (!isInEvent) return
    if (event.isCancelled) return

    val player = event.getPlayer
    val block = event.getBlock
    if (player == null || block == null) return
    if (!ManagedWorld.WorldOps(player.getWorld).isSeichi) return
    if (!isRegionMember(player, block.getLocation)) return

    val playerUuid = player.getUniqueId
    if (!SeichiAssist.playermap.contains(playerUuid)) return

    val rand = new Random().nextInt(itemDropRate)
    if (rand == 0) {
      if (isPlayerInventoryFull(player)) {
        dropItem(player, newYearBag)
        player.sendMessage(s"${RED}インベントリに空きがなかったため、「お年玉袋」は地面にドロップしました。")
      } else {
        addItem(player, newYearBag)
        player.sendMessage(s"$AQUA「お年玉袋」を見つけたよ！")
      }
      player.playSound(player.getLocation, Sound.BLOCK_NOTE_HARP, 3.0f, 1.0f)

      val playerData = SeichiAssist.playermap(playerUuid)
      playerData.newYearBagAmount_$eq(playerData.newYearBagAmount + 1)
    }
  }
}