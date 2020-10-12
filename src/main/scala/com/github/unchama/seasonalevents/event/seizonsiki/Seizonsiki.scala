package com.github.unchama.seasonalevents.event.seizonsiki

import java.text.{ParseException, SimpleDateFormat}
import java.util.{Date, UUID}

import com.github.unchama.seasonalevents.SeasonalEvents
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.player.PlayerData
import org.bukkit._
import org.bukkit.entity.{EntityType, Player}
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.{PlayerItemConsumeEvent, PlayerJoinEvent}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.Plugin

import scala.jdk.CollectionConverters._
import scala.util.chaining._

class Seizonsiki(private val plugin: Plugin) extends Listener {
  private var isdrop = false
  private val DROPDAY = "2017-01-16"
  private val DROPDAYDISP = "2017/01/15"
  private val FINISH = "2017-01-22"
  private val FINISHDISP = "2017/01/21"

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

  // TODO: 別ファイルに
  // アイテムがゾンごかどうかの判定
  private def isZongoConsumed(item: ItemStack): Boolean = {
    // Lore取得
    if (!item.hasItemMeta || !item.getItemMeta.hasLore) return false
    val itemLore = item.getItemMeta.getLore
    val prizeLore = getZongoLore
    // 比較
    itemLore.containsAll(prizeLore)
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

  // TODO: 別ファイルに
  private def getZongoItemStack = {
    val itemMeta: ItemMeta = Bukkit.getItemFactory.getItemMeta(Material.GOLDEN_APPLE)
      .tap(_.setDisplayName(s"${ChatColor.GOLD}${ChatColor.BOLD}ゾんご"))
      .tap(_.setLore(getZongoLore))

    val itemStack = new ItemStack(Material.GOLDEN_APPLE, 1)
    itemStack.setItemMeta(itemMeta)
    itemStack
  }

  // TODO: 別ファイルに
  private def getZongoLore =
    List(
      "",
      s"${ChatColor.RESET}${ChatColor.GRAY}成ゾン式で暴走していたチャラゾンビから没収した。",
      "ゾンビたちが栽培しているりんご。",
      "良質な腐葉土で1つずつ大切に育てられた。",
      "栄養豊富で、食べるとマナが10%回復する。",
      "腐りやすいため賞味期限を超えると効果が無くなる。",
      "",
      s"${ChatColor.RESET}${ChatColor.DARK_GREEN}賞味期限：$FINISHDISP",
      s"${ChatColor.RESET}${ChatColor.AQUA}マナ回復（10％）${ChatColor.GRAY} （期限内）"
    ).asJava
}