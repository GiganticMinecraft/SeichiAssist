package com.github.unchama.seichiassist.subsystems.seasonalevents.anniversary

import cats.effect.IO
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.playerheadskin.PlayerHeadSkinAPI
import com.github.unchama.seichiassist.subsystems.seasonalevents.anniversary.Anniversary.{
  ANNIVERSARY_COUNT,
  blogArticleUrl,
  isInEvent
}
import com.github.unchama.seichiassist.subsystems.seasonalevents.anniversary.AnniversaryItemData._
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gttosiina.GtToSiinaAPI
import com.github.unchama.seichiassist.util.EnemyEntity.isEnemy
import com.github.unchama.seichiassist.util.InventoryOperations.{
  grantItemStacksEffect,
  removeItemfromPlayerInventory
}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.{SequentialEffect, UnfocusedEffect}
import org.bukkit.ChatColor._
import org.bukkit.block.{Block, Chest}
import org.bukkit.entity.{LivingEntity, Player}
import org.bukkit.event.block.{Action, BlockBreakEvent, BlockPlaceEvent}
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.{PlayerInteractEvent, PlayerJoinEvent}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.{EquipmentSlot, ItemStack}
import org.bukkit.{Material, Sound, TreeType}

import scala.jdk.CollectionConverters._
import scala.util.Random

class AnniversaryListener(
  implicit effectEnvironment: EffectEnvironment,
  ioOnMainThread: OnMinecraftServerThread[IO],
  gtToSiinaAPI: GtToSiinaAPI[ItemStack],
  playerHeadSkinAPI: PlayerHeadSkinAPI[IO, Player]
) extends Listener {

  @EventHandler
  def onPlayerJoin(event: PlayerJoinEvent): Unit = {
    val player = event.getPlayer

    if (isInEvent) {
      List(
        s"${BLUE}ギガンティック☆整地鯖は${ANNIVERSARY_COUNT}周年を迎えました。",
        s"${BLUE}これを記念し、限定アイテムを入手可能です。詳しくは下記URLのサイトをご覧ください。",
        s"$DARK_GREEN$UNDERLINE$blogArticleUrl"
      ).foreach(player.sendMessage)
      player.playSound(player.getLocation, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
    }
  }

  @EventHandler
  def onPlayerDeath(event: PlayerDeathEvent): Unit = {
    if (!isInEvent) return

    val player = event.getEntity
    val playerUuid = player.getUniqueId
    val playerData = SeichiAssist.playermap(playerUuid)
    if (playerData.anniversary) return

    effectEnvironment.unsafeRunAsyncTargetedEffect(player)(
      SequentialEffect(
        grantItemStacksEffect(mineHead),
        MessageEffect(s"${BLUE}ギガンティック☆整地鯖${ANNIVERSARY_COUNT}周年の記念品を入手しました。"),
        FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f),
        UnfocusedEffect {
          SeichiAssist
            .databaseGateway
            .playerDataManipulator
            .setAnniversary(false, Some(playerUuid))
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
    val rootLocation = placedBlock.getLocation
    // オークの木を生やす
    rootLocation.getWorld.generateTree(rootLocation, TreeType.TREE)

    // Y座標を下に動かして（木の上方から）オークの木の頂点を探し、そのブロックを置き換える
    (10 to 0 by -1)
      .map(placedBlock.getRelative(0, _, 0))
      .find(block => block.getType == Material.OAK_LOG || block.getType == Material.OAK_LEAVES)
      .foreach(replaceBlockOnTreeTop(_, event.getPlayer.getName))
  }

  @EventHandler(ignoreCancelled = true)
  def onPlayerRightClickMendingBook(event: PlayerInteractEvent): Unit = {
    val item = event.getItem
    if (!isMendingBook(item)) return

    if (event.getHand == EquipmentSlot.OFF_HAND) return

    val action = event.getAction
    if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return

    val player = event.getPlayer
    val offHandItem = player.getInventory.getItemInOffHand
    if (offHandItem == null) return

    removeItemfromPlayerInventory(player.getInventory, item, 1)
  }

  @EventHandler(ignoreCancelled = true)
  def onPlayerBreakBlockWithAnniversaryShovel(event: BlockBreakEvent): Unit = {
    val player = event.getPlayer
    if (!isAnniversaryShovel(player.getInventory.getItemInMainHand)) return

    player
      .getNearbyEntities(20.0, 20.0, 20.0)
      .asScala
      .filter(mob => isEnemy(mob.getType))
      .foreach {
        case enemy: LivingEntity => enemy.damage(10.0)
        case _                   =>
      }
  }

  /**
   * 指定されたブロックを[[strangeSaplingBlockSet]]の中のいずれかに変更する
   * ただし、[[strangeSaplingSiinaRate]]の確率で、椎名林檎5個が入ったチェストに変更する
   */
  private def replaceBlockOnTreeTop(block: Block, playerName: String): Unit = {
    if (new Random().nextDouble() <= strangeSaplingSiinaRate) {
      block.setType(Material.CHEST)
      val chest = block.getState.asInstanceOf[Chest]
      chest
        .getBlockInventory
        .addItem(List.fill(5)(gtToSiinaAPI.getMaxSiinaRingo(playerName)): _*)
    } else {
      val random = new Random().nextInt(strangeSaplingBlockSet.size)
      block.setType(strangeSaplingBlockSet.toVector(random))
    }
  }
}
