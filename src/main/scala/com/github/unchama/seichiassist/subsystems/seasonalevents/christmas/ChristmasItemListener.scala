package com.github.unchama.seichiassist.subsystems.seasonalevents.christmas

import cats.effect.{SyncEffect, SyncIO}
import com.github.unchama.seichiassist.ManagedWorld._
import com.github.unchama.seichiassist.MaterialSets
import com.github.unchama.seichiassist.subsystems.mana.ManaWriteApi
import com.github.unchama.seichiassist.subsystems.seasonalevents.Util
import com.github.unchama.seichiassist.subsystems.seasonalevents.christmas.Christmas._
import com.github.unchama.seichiassist.subsystems.seasonalevents.christmas.ChristmasItemData._
import com.github.unchama.seichiassist.util.InventoryOperations.{
  addItem,
  dropItem,
  isPlayerInventoryFull,
  removeItemfromPlayerInventory
}
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor._
import org.bukkit.entity.EntityType._
import org.bukkit.entity.{EntityType, LivingEntity, Player}
import org.bukkit.event.block.{Action, BlockBreakEvent}
import org.bukkit.event.entity.{EntityDeathEvent, EntityTargetLivingEntityEvent}
import org.bukkit.event.player.{PlayerInteractEvent, PlayerItemConsumeEvent, PlayerJoinEvent}
import org.bukkit.event.{EventHandler, EventPriority, Listener}
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.{PotionEffect, PotionEffectType}
import org.bukkit.{Bukkit, Sound}

import java.util.Random

class ChristmasItemListener[F[_], G[_]: SyncEffect](instance: JavaPlugin)(
  implicit manaApi: ManaWriteApi[G, Player]
) extends Listener {
  @EventHandler
  def onPlayerJoin(event: PlayerJoinEvent): Unit = {
    if (isInEventNow) {
      Seq(
        s"$LIGHT_PURPLE${END_DATE}までの期間限定で、クリスマスイベントを開催しています。",
        "詳しくは下記URLのサイトをご覧ください。",
        s"$DARK_GREEN$UNDERLINE$blogArticleUrl"
      ).foreach(event.getPlayer.sendMessage(_))
    }
  }

  @EventHandler
  def onPlayerConsumeChristmasCake(event: PlayerInteractEvent): Unit = {
    val item = event.getItem
    if (!isChristmasCake(item)) return

    if (event.getHand == EquipmentSlot.OFF_HAND) return
    if (event.getAction != Action.RIGHT_CLICK_AIR && event.getAction != Action.LEFT_CLICK_BLOCK)
      return

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

    import cats.effect.implicits._

    val player = event.getPlayer

    // 1分おきに計5回マナを一定量回復する
    for (i <- 1 to 5) {
      Bukkit
        .getServer
        .getScheduler
        .runTaskLater(
          instance,
          new Runnable {
            override def run(): Unit = {
              // マナを15%回復する
              manaApi.manaAmount(player).restoreFraction(0.15).runSync[SyncIO].unsafeRunSync()
              player.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0f, 1.2f)
            }
          },
          (20 * 60 * i).toLong
        )
    }
  }

  @EventHandler
  def onEntityTarget(event: EntityTargetLivingEntityEvent): Unit = {
    val enemies = Set(
      BLAZE,
      CREEPER,
      ELDER_GUARDIAN,
      ENDERMAN,
      ENDERMITE,
      EVOKER,
      GHAST,
      GUARDIAN,
      HUSK,
      MAGMA_CUBE,
      PIG_ZOMBIE,
      SHULKER,
      SILVERFISH,
      SKELETON,
      SLIME,
      SPIDER,
      STRAY,
      VEX,
      VINDICATOR,
      WITCH,
      WITHER_SKELETON,
      ZOMBIE,
      ZOMBIE_VILLAGER
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
        val enchantLevel =
          new NBTItem(chestPlate).getByte(NBTTagConstants.camouflageEnchLevelTag).toInt
        // ここの数字に敵からの索敵距離を下げる
        try {
          val standard = calculateStandardDistance(enchantLevel, entity.getType)
          if (distance > standard) event.setCancelled(true)
        } catch {
          case err: IllegalArgumentException =>
            Bukkit
              .getServer
              .getLogger
              .info(
                s"${player.getName}によって、「迷彩」エンチャントのついたアイテムが使用されましたが、設定されているエンチャントレベルが不正なものでした。"
              )
            err.printStackTrace()
        }
      case _ =>
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  def onChristmasSockPopped(event: BlockBreakEvent): Unit = {
    if (!isInEventNow) return

    val player = event.getPlayer
    if (!player.getWorld.isSeichi) return
    // 整地スキルに対応していないブロックなら処理を終了
    if (!MaterialSets.materialsToCountBlockBreak.contains(event.getBlock.getType)) return

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
    if (!isInEventNow) return

    event.getEntity match {
      case entity: LivingEntity
          if entity.getType == EntityType.STRAY && entity.getKiller != null =>
        Util.randomlyDropItemAt(entity, christmasSock, itemDropRateFromStray)
      case _ =>
    }
  }

}
