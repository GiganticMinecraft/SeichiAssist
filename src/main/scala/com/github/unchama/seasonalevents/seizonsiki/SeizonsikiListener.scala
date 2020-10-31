package com.github.unchama.seasonalevents.seizonsiki

import java.time.LocalDate

import com.github.unchama.seasonalevents.seizonsiki.Seizonsiki.itemsWillBeDropped
import com.github.unchama.seasonalevents.seizonsiki.SeizonsikiItemData.{NBTTagConstants, isValidZongo, isZongo, seizonsikiZongo}
import com.github.unchama.seasonalevents.{SeasonalEvents, Util}
import com.github.unchama.seichiassist.SeichiAssist
import de.tr7zw.itemnbtapi.NBTItem
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
    if (!itemsWillBeDropped || entity == null) return

    if (entity.getType == EntityType.ZOMBIE && entity.getKiller != null) {
      Util.randomlyDropItemAt(entity, seizonsikiZongo)
    }
  }

  @EventHandler
  def onPlayerJoinEvent(event: PlayerJoinEvent): Unit = {
    if (itemsWillBeDropped) {
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

    val today = LocalDate.now()
    val exp = new NBTItem(item).getObject(NBTTagConstants.expirationDateTag, classOf[LocalDate])
    if (!isZongo(item) || !today.isBefore(exp)) return

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