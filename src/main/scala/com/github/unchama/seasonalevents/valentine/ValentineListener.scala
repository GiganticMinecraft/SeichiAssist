package com.github.unchama.seasonalevents.valentine

import java.util.Random

import com.github.unchama.seasonalevents.SeasonalEventsConfig
import com.github.unchama.seasonalevents.Util.randomlyDropItemAt
import com.github.unchama.seasonalevents.valentine.Valentine.{END_DATE, isInEvent}
import com.github.unchama.seasonalevents.valentine.ValentineCookieEffectsHandler._
import com.github.unchama.seasonalevents.valentine.ValentineItemData._
import com.github.unchama.seichiassist.util.Util.sendEveryMessage
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor.{DARK_GREEN, LIGHT_PURPLE, UNDERLINE}
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.{EntityType, Monster, Player}
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.{EntityDamageByEntityEvent, EntityExplodeEvent}
import org.bukkit.event.player.{PlayerItemConsumeEvent, PlayerJoinEvent}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.{PotionEffect, PotionEffectType}

import scala.util.chaining._

class ValentineListener(implicit config: SeasonalEventsConfig) extends Listener {
  @EventHandler
  def onEntityExplode(event: EntityExplodeEvent): Unit = {
    val entity = event.getEntity
    if (!isInEvent || entity == null) return

    if (entity.isInstanceOf[Monster] && entity.isDead){
      randomlyDropItemAt(entity, droppedCookie)
    }
  }

  // モンスターの死因がクリーパーによる爆発の場合、確率でアイテムをドロップ
  @EventHandler
  def onEntityDeath(event: EntityDamageByEntityEvent): Unit = {
    if (!isInEvent) return

    val entity = event.getEntity
    if (entity == null || !entity.isInstanceOf[Monster]) return

    val damager = event.getDamager
    if (damager == null) return

    if (event.getCause != DamageCause.ENTITY_EXPLOSION || damager.getType != EntityType.CREEPER) return

    val entityMaxHealth = entity.asInstanceOf[Monster].getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue
    if (entityMaxHealth <= event.getDamage) {
      randomlyDropItemAt(entity, droppedCookie)
    }
  }

  @EventHandler
  def onPlayerJoinEvent(event: PlayerJoinEvent): Unit = {
    val player = event.getPlayer

    if (isInEvent) {
      Seq(
        s"$LIGHT_PURPLE${END_DATE}までの期間限定で、限定イベント『＜ブラックバレンタイン＞リア充 vs 整地民！』を開催しています。",
        "詳しくは下記URLのサイトをご覧ください。",
        s"$DARK_GREEN$UNDERLINE${config.blogArticleUrl}"
      ).foreach(
        player.sendMessage
      )
    }
  }

  @EventHandler
  def onPlayerItemConsumeEvent(event: PlayerItemConsumeEvent): Unit = {
    val item = event.getItem
    val player = event.getPlayer
    if (isDroppedCookie(item) && isUsableCookie(item)) useDroppedCookie(player)
    if (isGiftedCookie(item) && isUsableCookie(item)) useGiftedCookie(player, item)
  }

  private def useDroppedCookie(player: Player): Unit = {
    val effect = randomlySelectEffect
    player
      .tap(_.sendMessage(getMessage(effect)))
      .tap(_.addPotionEffect(getEffect(effect)._2))
      .tap(_.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F))
  }

  private def useGiftedCookie(player: Player, item: ItemStack): Unit = {
    if (ownerOf(item).contains(player.getUniqueId)) {
      // HP最大値アップ
      player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 20 * 60 * 10, 10))
    } else {
      // 死ぬ
      player.setHealth(0)

      val messages = deathMessages(player.getName, new NBTItem(item).getString(NBTTagConstants.producerNameTag))
      sendEveryMessage(messages(new Random().nextInt(messages.size)))
    }
    player.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F)
  }
}