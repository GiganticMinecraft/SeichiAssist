package com.github.unchama.seichiassist.listener

import cats.effect.{IO, SyncIO}
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.mana.domain.ManaAmount
import com.github.unchama.seichiassist.util.ItemInformation
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.{EventHandler, Listener}

import scala.jdk.CollectionConverters

class GachaItemListener(implicit manaApi: ManaApi[IO, SyncIO, Player]) extends Listener {

  @EventHandler def onPlayerItemConsumeEvent(event: PlayerItemConsumeEvent): Unit = {
    val player = event.getPlayer

    val itemStack = event.getItem
    val itemMeta = itemStack.getItemMeta

    if (!itemMeta.hasLore) return

    val lore = CollectionConverters.ListHasAsScala(itemMeta.getLore).asScala.toList

    if (ItemInformation.loreIndexOf(lore, "マナ完全回復") > 0) {
      manaApi.manaAmount(player).restoreCompletely.unsafeRunSync()
      player.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0f, 1.2f)
    }

    if (ItemInformation.loreIndexOf(lore, "マナ回復（小）") > 0) {
      manaApi.manaAmount(player).restoreAbsolute(ManaAmount(300)).unsafeRunSync()
      player.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0f, 1.2f)
    }

    if (ItemInformation.loreIndexOf(lore, "マナ回復（中）") > 0) {
      manaApi.manaAmount(player).restoreAbsolute(ManaAmount(1500)).unsafeRunSync()
      player.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0f, 1.2f)
    }

    if (ItemInformation.loreIndexOf(lore, "マナ回復（大）") > 0) {
      manaApi.manaAmount(player).restoreAbsolute(ManaAmount(10000)).unsafeRunSync()
      player.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0f, 1.2f)
    }

    if (ItemInformation.loreIndexOf(lore, "マナ回復（極）") > 0) {
      manaApi.manaAmount(player).restoreAbsolute(ManaAmount(100000)).unsafeRunSync()
      player.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0f, 1.2f)
    }
  }
}
