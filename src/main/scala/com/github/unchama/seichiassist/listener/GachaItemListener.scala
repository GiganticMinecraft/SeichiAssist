package com.github.unchama.seichiassist.listener

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.util.Util
import org.bukkit.{Bukkit, Sound}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.event.player.PlayerItemConsumeEvent

import scala.jdk.CollectionConverters

class GachaItemListener extends Listener {
  private val playermap = SeichiAssist.playermap

  //private SeichiAssist instance = SeichiAssist.instance;
  @EventHandler def onPlayerItemConsumeEvent(e: PlayerItemConsumeEvent): Unit = {
    val player = e.getPlayer
    val playerdata = playermap.apply(player.getUniqueId)
    //念のためエラー分岐
    if (playerdata == null) {
      Util.sendPlayerDataNullMessage(player)
      Bukkit.getLogger.warning(player.getName + " -> PlayerData not found.")
      Bukkit.getLogger.warning("GachaItemListener.onPlayerItemConsumeEvent")
      return
    }
    val level =
      SeichiAssist.instance
        .breakCountSystem.api
        .seichiAmountDataRepository(player)
        .read.unsafeRunSync()
        .levelCorrespondingToExp.level
    val mana = playerdata.manaState
    val i = e.getItem
    //Material m = i.getType();
    val itemmeta = i.getItemMeta
    //これ以降は説明文あり
    if (!itemmeta.hasLore) return
    val lore = CollectionConverters.ListHasAsScala(itemmeta.getLore).asScala.toList
    if (Util.loreIndexOf(lore, "マナ完全回復") > 0) {
      mana.setFull(player, level)
      player.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F)
    }
    if (Util.loreIndexOf(lore, "マナ回復（小）") > 0) {
      mana.increase(300, player, level)
      player.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F)
    }
    if (Util.loreIndexOf(lore, "マナ回復（中）") > 0) {
      mana.increase(1500, player, level)
      player.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F)
    }
    if (Util.loreIndexOf(lore, "マナ回復（大）") > 0) {
      mana.increase(10000, player, level)
      player.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F)
    }
    if (Util.loreIndexOf(lore, "マナ回復（極）") > 0) {
      mana.increase(100000, player, level)
      player.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F)
    }
  }
}