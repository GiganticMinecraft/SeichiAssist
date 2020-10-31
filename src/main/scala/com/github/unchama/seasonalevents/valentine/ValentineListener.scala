package com.github.unchama.seasonalevents.valentine

import com.github.unchama.seasonalevents.valentine.Valentine.{DISPLAYED_END_DATE, isDrop}
import com.github.unchama.seasonalevents.valentine.ValentineItemData._
import com.github.unchama.seasonalevents.{SeasonalEvents, Util}
import org.bukkit.ChatColor.{DARK_GREEN, LIGHT_PURPLE, UNDERLINE}
import org.bukkit.attribute.Attribute
import org.bukkit.entity.{EntityType, Monster}
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.{EntityDamageByEntityEvent, EntityExplodeEvent}
import org.bukkit.event.player.{PlayerItemConsumeEvent, PlayerJoinEvent}
import org.bukkit.event.{EventHandler, Listener}

class ValentineListener extends Listener {
  @EventHandler
  def onEntityExplode(event: EntityExplodeEvent): Unit = {
    val entity = event.getEntity
    if (!isDrop || entity == null) return

    if (entity.isInstanceOf[Monster] && entity.isDead){
      Util.randomlyDropItemAt(entity, droppedCookie)
    }
  }

  // モンスターの死因がクリーパーによる爆発の場合、確率でアイテムをドロップ
  @EventHandler
  def onEntityDeath(event: EntityDamageByEntityEvent): Unit = {
    if (!isDrop) return

    val entity = event.getEntity
    if (entity == null || !entity.isInstanceOf[Monster]) return

    val damager = event.getDamager
    if (damager == null) return

    if (event.getCause != DamageCause.ENTITY_EXPLOSION || damager.getType != EntityType.CREEPER) return

    val entityMaxHealth = entity.asInstanceOf[Monster].getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue
    if (entityMaxHealth <= event.getDamage) {
      Util.randomlyDropItemAt(entity, droppedCookie)
    }
  }

  @EventHandler
  def onPlayerJoinEvent(event: PlayerJoinEvent): Unit = {
    if (isDrop) {
      Seq(
        s"$LIGHT_PURPLE${DISPLAYED_END_DATE}までの期間限定で、限定イベント『＜ブラックバレンタイン＞リア充 vs 整地民！』を開催しています。",
        "詳しくは下記URLのサイトをご覧ください。",
        s"$DARK_GREEN$UNDERLINE${SeasonalEvents.config.blogArticleUrl}"
      ).foreach(
        event.getPlayer.sendMessage(_)
      )
    }
  }

  @EventHandler
  def onPlayerItemConsumeEvent(event: PlayerItemConsumeEvent): Unit = {
    val item = event.getItem
    val player = event.getPlayer
    if (isDroppedCookie(item) && isValidCookie(item)) useDroppedCookie(player)
    if (isGiftedCookie(item) && isValidCookie(item)) useGiftedCookie(player, item)
  }
}