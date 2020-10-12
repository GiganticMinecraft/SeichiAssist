package com.github.unchama.seasonalevents.event.seizonsiki

import java.text.{ParseException, SimpleDateFormat}
import java.util.{Date, UUID}

import com.github.unchama.seasonalevents.SeasonalEvents
import com.github.unchama.seasonalevents.event.seizonsiki.SeizonsikiItemData._
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.player.PlayerData
import org.bukkit._
import org.bukkit.entity.{EntityType, Player}
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.{PlayerItemConsumeEvent, PlayerJoinEvent}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.plugin.Plugin

class Seizonsiki(private val plugin: Plugin) extends Listener {
  private var isdrop = false
  private val DROPDAY = "2017-01-16"
  private val DROPDAYDISP = "2017/01/15"
  private val FINISH = "2017-01-22"

  try {
    val format = new SimpleDateFormat("yyyy-MM-dd")
    val finishDate = format.parse(FINISH)
    val dropDate = format.parse(DROPDAY)

    val now = new Date
    // イベント開催中か判定
    if (now.before(finishDate)) plugin.getServer.getPluginManager.registerEvents(this, plugin)
    if (now.before(dropDate)) isdrop = true
  } catch {
    case e: ParseException => e.printStackTrace()
  }

  @EventHandler
  def onEntityDeath(event: EntityDeathEvent): Unit = {
    if (event.getEntity.getType == EntityType.ZOMBIE && event.getEntity.getKiller != null) {
      dropZongo(event.getEntity.getKiller, event.getEntity.getLocation)
    }
  }

  @EventHandler
  def onplayerJoinEvent(event: PlayerJoinEvent): Unit = {
    if (isdrop) {
      List(
        s"${ChatColor.LIGHT_PURPLE}${DROPDAYDISP}までの期間限定で、シーズナルイベント『チャラゾンビたちの成ゾン式！』を開催しています。",
        "詳しくは下記wikiをご覧ください。",
        s"${ChatColor.DARK_GREEN}${ChatColor.UNDERLINE}${SeasonalEvents.config.getWikiAddr}"
      ).foreach(
        event.getPlayer.sendMessage(_)
      )
    }
  }

  @EventHandler
  def onPlayerItemConsumeEvent(event: PlayerItemConsumeEvent): Unit = {
    if (isZongoConsumed(event.getItem)) increase10PctMana(event.getPlayer)
  }

  // プレイヤーにゾンビが倒されたとき発生
  private def dropZongo(killer: Player, loc: Location): Unit = {
    if (isdrop) {
      val dropRate: Double = SeasonalEvents.config.getDropRate
      val num: Double = Math.random * 100
      if (num < dropRate) {
        // 報酬をドロップ
        killer.getWorld.dropItemNaturally(loc, getZongoItemStack)
      }
    }
  }

  // ゾンごを使った時のマナ回復処理
  private def increase10PctMana(player: Player): Unit = {
    val uuid: UUID = player.getUniqueId
    val pd: PlayerData = SeichiAssist.playermap.getOrElse(uuid, return)
    val mana = pd.manaState
    val max = mana.calcMaxManaOnly(player, pd.level)
    // マナを10%回復する
    mana.increase(max * 0.1, player, pd.level)
    player.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F)
  }
}

object Seizonsiki {
  val FINISHDISP = "2017/01/21"
}