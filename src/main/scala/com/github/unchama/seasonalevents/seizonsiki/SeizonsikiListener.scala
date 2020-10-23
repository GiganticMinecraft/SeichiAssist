package com.github.unchama.seasonalevents.seizonsiki

import com.github.unchama.seasonalevents.seizonsiki.SeizonsikiItemData.{isZongoConsumed, seizonsikiZongo}
import com.github.unchama.seasonalevents.{SeasonalEvents, Utl}
import com.github.unchama.seichiassist.SeichiAssist
import org.bukkit.entity.EntityType
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.{PlayerItemConsumeEvent, PlayerJoinEvent}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.ChatColor.{LIGHT_PURPLE, DARK_GREEN, UNDERLINE}
import org.bukkit.Sound

class SeizonsikiListener extends Listener {
  @EventHandler
  def onZombieKilledByPlayer(event: EntityDeathEvent): Unit = {
    val entity = event.getEntity
    if (!Seizonsiki.isDrop || entity == null) return

    if (entity.getType == EntityType.ZOMBIE && entity.getKiller != null) {
      Utl.dropItem(entity, seizonsikiZongo)
    }
  }

  @EventHandler
  def onPlayerJoinEvent(event: PlayerJoinEvent): Unit = {
    if (Seizonsiki.isDrop) {
      List(
        s"$LIGHT_PURPLE${Seizonsiki.DROPDAYDISP}までの期間限定で、シーズナルイベント『チャラゾンビたちの成ゾン式！』を開催しています。",
        "詳しくは下記wikiをご覧ください。",
        s"$DARK_GREEN$UNDERLINE${SeasonalEvents.config.getWikiAddr}"
      ).foreach(
        event.getPlayer.sendMessage(_)
      )
    }
  }

  @EventHandler
  def onPlayerConsumedZongo(event: PlayerItemConsumeEvent): Unit = {
    if (!isZongoConsumed(event.getItem)) return

    val player = event.getPlayer
    val playerUuid = player.getUniqueId
    if (SeichiAssist.playermap.contains(playerUuid)) {
      val playerData = SeichiAssist.playermap(playerUuid)
      val manaState = playerData.manaState
      val maxMana = manaState.calcMaxManaOnly(player, playerData.level)
      // マナを10%回復する
      manaState.increase(maxMana * 0.1, player, playerData.level)
      player.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F)
    }
  }
}