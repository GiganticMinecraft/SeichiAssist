package com.github.unchama.seasonalevents.seizonsiki

import com.github.unchama.seasonalevents.seizonsiki.SeizonsikiItemData.{isValidZongo, isZongo, seizonsikiZongo}
import com.github.unchama.seasonalevents.{SeasonalEvents, Utl}
import com.github.unchama.seichiassist.SeichiAssist
import org.bukkit.entity.EntityType
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.{PlayerItemConsumeEvent, PlayerJoinEvent}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.ChatColor.{DARK_GREEN, LIGHT_PURPLE, UNDERLINE}
import org.bukkit.{Bukkit, Sound}

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
        s"$LIGHT_PURPLE${Seizonsiki.DISPLAYED_END_DATE}までの期間限定で、限定イベント『チャラゾンビたちの成ゾン式！』を開催しています。",
        "詳しくは下記URLのサイトをご覧ください。",
        s"$DARK_GREEN$UNDERLINE${SeasonalEvents.config.blogArticleUrl}"
      ).foreach(
        event.getPlayer.sendMessage(_)
      )
    }
  }

  @EventHandler
  def onPlayerConsumedZongo(event: PlayerItemConsumeEvent): Unit = {
    val item = event.getItem
    if (!isZongo(item) || !isValidZongo(item)) return

    val player = event.getPlayer
    val playerUuid = player.getUniqueId
    // この条件分岐がfalseになる可能性は通常ないが、なっている事例があるので念の為
    // 参照：https://github.com/GiganticMinecraft/SeichiAssist/issues/707
    if (SeichiAssist.playermap.contains(playerUuid)) {
      val playerData = SeichiAssist.playermap(playerUuid)
      val manaState = playerData.manaState
      val maxMana = manaState.calcMaxManaOnly(player, playerData.level)
      // マナを10%回復する
      manaState.increase(maxMana * 0.1, player, playerData.level)
      player.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F)
    } else Bukkit.getServer.getLogger.info(s"${player.getName}によってゾんごが使用されましたが、プレイヤーデータが存在しなかったため、マナ回復が行われませんでした。")
  }
}