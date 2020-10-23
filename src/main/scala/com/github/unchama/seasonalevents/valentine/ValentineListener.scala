package com.github.unchama.seasonalevents.valentine

import com.github.unchama.seasonalevents.valentine.Valentine.{DISPLAYED_END_DATE, isDrop}
import com.github.unchama.seasonalevents.valentine.ValentineItemData._
import com.github.unchama.seasonalevents.{SeasonalEvents, Utl}
import org.bukkit.ChatColor.{DARK_GREEN, LIGHT_PURPLE, UNDERLINE}
import org.bukkit.entity.Monster
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.{EntityDeathEvent, EntityExplodeEvent}
import org.bukkit.event.player.{PlayerItemConsumeEvent, PlayerJoinEvent}
import org.bukkit.event.{EventHandler, Listener}

class ValentineListener extends Listener {
  @EventHandler
  def onEntityExplode(event: EntityExplodeEvent): Unit = {
    val entity = event.getEntity
    if (!isDrop || entity == null) return

    if (entity.isInstanceOf[Monster] && entity.isDead){
      Utl.dropItem(entity, droppedCookie)
    }
  }

  // 現状、以下のTODOは仕様扱いだが、意図した挙動ではない
  // TODO TNTで爆破死した敵からも出るのを直す
  // TODO 爆破死したモンスター以外のmob(スノーゴーレム、プレイヤーなど)からもチョコチップクッキーが出るのを直す
  @EventHandler
  def onEntityDeath(event: EntityDeathEvent): Unit = {
    val entity = event.getEntity
    if (!isDrop || entity == null) return

    if (entity.getLastDamageCause.getCause == DamageCause.ENTITY_EXPLOSION) {
      // 死因が爆発の場合、確率でアイテムをドロップ
      Utl.dropItem(entity, droppedCookie)
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