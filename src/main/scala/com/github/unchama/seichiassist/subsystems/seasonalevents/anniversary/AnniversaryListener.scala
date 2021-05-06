package com.github.unchama.seichiassist.subsystems.seasonalevents.anniversary

import cats.effect.IO
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.subsystems.seasonalevents.anniversary.Anniversary.{ANNIVERSARY_COUNT, EVENT_DATE, blogArticleUrl}
import com.github.unchama.seichiassist.subsystems.seasonalevents.anniversary.AnniversaryItemData._
import com.github.unchama.seichiassist.util.StaticGachaPrizeFactory.getMaxRingo
import com.github.unchama.seichiassist.util.Util.grantItemStacksEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.{SequentialEffect, UnfocusedEffect}
import org.bukkit.ChatColor._
import org.bukkit.block.{Block, Chest}
import org.bukkit.event.block.{Action, BlockPlaceEvent}
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.{PlayerInteractEvent, PlayerJoinEvent}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.{Material, Sound, TreeType}

import java.time.LocalDate
import scala.util.Random
import scala.util.control.Breaks

class AnniversaryListener(implicit effectEnvironment: EffectEnvironment,
                          ioOnMainThread: OnMinecraftServerThread[IO]) extends Listener {

  @EventHandler
  def onPlayerJoin(event: PlayerJoinEvent): Unit = {
    val player = event.getPlayer

    if (LocalDate.now().isEqual(EVENT_DATE)) {
      List(
        s"${BLUE}本日でギガンティック☆整地鯖は${ANNIVERSARY_COUNT}周年を迎えます。",
        s"${BLUE}これを記念し、限定アイテムを入手可能です。詳しくは下記URLのサイトをご覧ください。",
        s"$DARK_GREEN$UNDERLINE$blogArticleUrl"
      ).foreach(player.sendMessage)
      player.playSound(player.getLocation, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
    }
  }

  @EventHandler
  def onPlayerDeath(event: PlayerDeathEvent): Unit = {
    if (!LocalDate.now().isEqual(EVENT_DATE)) return

    val player = event.getEntity
    val playerData: PlayerData = SeichiAssist.playermap(player.getUniqueId)
    if (playerData.anniversary) return

    effectEnvironment.runAsyncTargetedEffect(player)(
      SequentialEffect(
        grantItemStacksEffect(mineHead),
        MessageEffect(s"${BLUE}ギガンティック☆整地鯖${ANNIVERSARY_COUNT}周年の記念品を入手しました。"),
        FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f),
        UnfocusedEffect {
          playerData.anniversary = false
        }
      ),
      s"${ANNIVERSARY_COUNT}周年記念ヘッドを付与する"
    )
  }

  @EventHandler(ignoreCancelled = true)
  def onPlayerPlaceSapling(event: BlockPlaceEvent): Unit = {
    if (!isStrangeSapling(event.getItemInHand)) return

    val placedBlock = event.getBlock
    // 苗木をなくす
    placedBlock.setType(Material.AIR)
    val location = placedBlock.getLocation
    // オークの木を生やす
    location.getWorld.generateTree(location, TreeType.TREE)

    val breaks = new Breaks
    // Y座標を下に動かして（木の上方から）オークの木の頂点を探し、そのブロックを置き換えて、ループを抜ける
    breaks.breakable {
      for (relY <- 10 to 0 by -1) {
        val block = placedBlock.getRelative(0, relY, 0)

        if (block.getType == Material.LOG || block.getType == Material.LEAVES) {
          replaceBlockOnTreeTop(location.getBlock, event.getPlayer.getName)
          breaks.break
        }
      }
    }
  }

  @EventHandler(ignoreCancelled = true)
  def onPlayerRightClickMendingBook(event: PlayerInteractEvent): Unit = {
    val item = event.getItem
    if (!isMendingBook(item)) return
    if (event.getHand == EquipmentSlot.OFF_HAND) return

    val player = event.getPlayer
    val action = event.getAction
    val offHandItem = player.getInventory.getItemInOffHand
    if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return
    if (offHandItem == null) return

    offHandItem.setDurability(0.toShort)
    player.getInventory.removeItem(item)
  }

  /**
   * 指定されたブロックを[[strangeSaplingBlockSet]]野中のいずれかに変更する
   * ただし、[[strangeSaplingSiinaRate]]の確率で、椎名林檎5個が入ったチェストに変更する
   */
  private def replaceBlockOnTreeTop(block: Block, playerName: String): Unit = {
    if (new Random().nextDouble() < strangeSaplingSiinaRate) {
      block.setType(Material.CHEST)
      val chest = block.getState.asInstanceOf[Chest]
      chest.getBlockInventory.addItem(List.fill(5)(getMaxRingo(playerName)): _*)
    } else {
      val random = new Random().nextInt(strangeSaplingBlockSet.size)
      block.setType(strangeSaplingBlockSet.toVector(random))
    }
  }
}