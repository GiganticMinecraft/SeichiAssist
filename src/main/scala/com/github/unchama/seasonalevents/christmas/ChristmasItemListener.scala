package com.github.unchama.seasonalevents.christmas

import java.util.Random

import com.github.unchama.seasonalevents.Util
import com.github.unchama.seasonalevents.christmas.Christmas.{END_DATE, blogArticleUrl, isInEvent, itemDropRate}
import com.github.unchama.seasonalevents.christmas.ChristmasItemData._
import com.github.unchama.seichiassist.util.Util.{addItem, dropItem, isPlayerInventoryFull, removeItemfromPlayerInventory}
import com.github.unchama.seichiassist.{ManagedWorld, SeichiAssist}
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor._
import org.bukkit.entity.EntityType._
import org.bukkit.entity.{EntityType, LivingEntity, Player}
import org.bukkit.event.block.{Action, BlockBreakEvent}
import org.bukkit.event.entity.{EntityDeathEvent, EntityTargetLivingEntityEvent}
import org.bukkit.event.player.{PlayerInteractEvent, PlayerItemConsumeEvent, PlayerJoinEvent}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.{PotionEffect, PotionEffectType}
import org.bukkit.{Bukkit, Sound}

class ChristmasItemListener(instance: SeichiAssist) extends Listener {
  @EventHandler
  def onPlayerJoin(event: PlayerJoinEvent): Unit = {
    if (isInEvent) {
      Seq(
        s"$LIGHT_PURPLE${END_DATE}までの期間限定で、クリスマスイベントを開催しています。",
        "詳しくは下記URLのサイトをご覧ください。",
        s"$DARK_GREEN$UNDERLINE$blogArticleUrl"
      ).foreach(
        event.getPlayer.sendMessage(_)
      )
    }
  }

  @EventHandler
  def onPlayerConsumeChristmasCake(event: PlayerInteractEvent): Unit = {
    val item = event.getItem
    if (!isChristmasCake(item)) return

    if (event.getHand == EquipmentSlot.OFF_HAND) return
    if (event.getAction != Action.RIGHT_CLICK_AIR && event.getAction != Action.LEFT_CLICK_BLOCK) return

    event.setCancelled(true)

    val player = event.getPlayer

    val rand = new Random().nextDouble()
    val potionEffectType = if (rand > 0.5) PotionEffectType.LUCK else PotionEffectType.UNLUCK
    player.addPotionEffect(new PotionEffect(potionEffectType, 20 * 30, 0), true)

    removeItemfromPlayerInventory(player.getInventory, item, 1)

    val remainingPiece = new NBTItem(item).getByte(NBTTagConstants.cakePieceTag).toInt
    if (remainingPiece != 0) {
      val newItem = christmasCake(remainingPiece - 1)
      addItem(player, newItem)
    }
  }

  @EventHandler
  def onPlayerConsumeChristmasTurkey(event: PlayerItemConsumeEvent): Unit = {
    if (!isChristmasTurkey(event.getItem)) return

    val rand = new Random().nextDouble()
    val potionEffectType = if (rand > 0.5) PotionEffectType.SPEED else PotionEffectType.SLOW
    event.getPlayer.addPotionEffect(new PotionEffect(potionEffectType, 20 * 30, 0), true)
  }

  @EventHandler
  def onPlayerConsumeChristmasPotion(event: PlayerItemConsumeEvent): Unit = {
    if (!isChristmasPotion(event.getItem)) return

    val player = event.getPlayer
    val playerUuid = player.getUniqueId

    // 1分おきに計5回マナを一定量回復する
    for (i <- 1 to 5) {
      Bukkit.getServer.getScheduler.runTaskLater(instance, new Runnable {
        override def run(): Unit = {
          // この条件分岐がfalseになる可能性は通常ない（ログインしている限りplayerMapにはそのMCIDのデータが有るはずだ）が、なっている事例があるので念の為
          // 参照：https://github.com/GiganticMinecraft/SeichiAssist/issues/707
          if (SeichiAssist.playermap.contains(playerUuid)) {
            val playerData = SeichiAssist.playermap(playerUuid)
            val manaState = playerData.manaState
            val maxMana = manaState.calcMaxManaOnly(player, playerData.level)
            // マナを15%回復する
            manaState.increase(maxMana * 0.15, player, playerData.level)
            player.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F)
          } else {
            Bukkit.getServer.getLogger.info(s"${player.getName}によって「みんなの涙」が使用されましたが、プレイヤーデータが存在しなかったため、マナ回復が行われませんでした。")
          }
        }
      }, (20 * 60 * i).toLong)
    }
  }

  @EventHandler
  def onEntityTarget(event: EntityTargetLivingEntityEvent): Unit = {
    val enemies = Set(
      BLAZE, CREEPER, ELDER_GUARDIAN, ENDERMAN, ENDERMITE, EVOKER, GHAST, GUARDIAN, HUSK, MAGMA_CUBE, PIG_ZOMBIE,
      SHULKER, SILVERFISH, SKELETON, SLIME, SPIDER, STRAY, VEX, VINDICATOR, WITCH, WITHER_SKELETON, ZOMBIE, ZOMBIE_VILLAGER
    )

    val entity = event.getEntity
    if (entity == null || !enemies.contains(entity.getType)) return

    val target = event.getTarget
    // nullということは、EntityがTargetを忘れたということ
    if (target == null) return

    target match {
      case player: Player =>
        val chestPlate = player.getInventory.getChestplate
        if (!isChristmasChestPlate(chestPlate)) return

        val entityLocation = entity.getLocation
        val playerLocation = player.getLocation

        val distance = entityLocation.distance(playerLocation)
        val enchantLevel = new NBTItem(chestPlate).getByte(NBTTagConstants.camouflageEnchLevelTag).toInt
        // ここの数字に敵からの索敵距離を下げる
        try {
          val standard = calculateStandardDistance(enchantLevel, entity.getType)
          if (distance > standard) event.setCancelled(true)
        } catch {
          case err: IllegalArgumentException =>
            Bukkit.getServer.getLogger.info(s"${player.getName}によって、「迷彩」エンチャントのついたアイテムが使用されましたが、設定されているエンチャントレベルが不正なものでした。")
            err.printStackTrace()
        }
      case _ =>
    }
  }

  @EventHandler
  def onChristmasSockPopped(event: BlockBreakEvent): Unit = {
    if (!isInEvent) return
    if (event.isCancelled) return

    val player = event.getPlayer
    val block = event.getBlock
    if (block == null) return
    if (!ManagedWorld.WorldOps(player.getWorld).isSeichi) return

    val rand = new Random().nextDouble()
    if (rand < itemDropRate) {
      if (isPlayerInventoryFull(player)) {
        dropItem(player, christmasSock)
        player.sendMessage(s"${RED}インベントリに空きがなかったため、「靴下」は地面にドロップしました。")
      } else {
        addItem(player, christmasSock)
        player.sendMessage(s"$AQUA「靴下」を見つけたよ！")
      }
      player.playSound(player.getLocation, Sound.BLOCK_NOTE_HARP, 3.0f, 1.0f)
    }
  }

  @EventHandler
  def onStrayDeath(event: EntityDeathEvent): Unit = {
    if (!isInEvent) return

    event.getEntity match {
      case entity: LivingEntity =>
        if (entity.getType == EntityType.STRAY && entity.getKiller != null)
          Util.randomlyDropItemAt(entity, christmasSock, itemDropRate)
      case _ =>
    }
  }

}