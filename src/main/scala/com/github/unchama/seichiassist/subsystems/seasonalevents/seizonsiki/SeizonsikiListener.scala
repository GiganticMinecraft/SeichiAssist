package com.github.unchama.seichiassist.subsystems.seasonalevents.seizonsiki

import cats.effect.{SyncEffect, SyncIO}
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.subsystems.mana.ManaWriteApi
import com.github.unchama.seichiassist.subsystems.seasonalevents.Util.randomlyDropItemAt
import com.github.unchama.seichiassist.subsystems.seasonalevents.seizonsiki.Seizonsiki._
import com.github.unchama.seichiassist.subsystems.seasonalevents.seizonsiki.SeizonsikiItemData._
import com.github.unchama.seichiassist.util.SendMessageEffect.sendMessageToEveryoneIgnoringPreference
import com.github.unchama.seichiassist.util.EntityDeathCause.isEntityKilledByThornsEnchant
import de.tr7zw.nbtapi.NBTItem
import org.bukkit.ChatColor.{DARK_GREEN, LIGHT_PURPLE, UNDERLINE}
import org.bukkit.Sound
import org.bukkit.entity.{EntityType, Player}
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.{PlayerItemConsumeEvent, PlayerJoinEvent}
import org.bukkit.event.{EventHandler, Listener}

import java.time.LocalDate
import java.util.Random

class SeizonsikiListener[F[_], G[_]: SyncEffect](implicit manaApi: ManaWriteApi[G, Player])
    extends Listener {

  import cats.effect.implicits._

  @EventHandler
  def onZombieKilledByPlayer(event: EntityDeathEvent): Unit = {
    val entity = event.getEntity
    if (!isInEvent || entity == null) return
    val killer = entity.getKiller
    if (entity.getType != EntityType.ZOMBIE || killer == null) return

    killer match {
      case _: Player if isEntityKilledByThornsEnchant(entity) =>
        randomlyDropItemAt(entity, seizonsikiZongo, itemDropRate)
      case _ =>
    }
  }

  @EventHandler
  def onPlayerJoinEvent(event: PlayerJoinEvent): Unit = {
    if (isInEvent) {
      List(
        s"$LIGHT_PURPLE${END_DATE}までの期間限定で、イベント『チャラゾンビたちの成ゾン式！』を開催しています。",
        "詳しくは下記URLのサイトをご覧ください。",
        s"$DARK_GREEN$UNDERLINE$blogArticleUrl"
      ).foreach(event.getPlayer.sendMessage(_))
    }
  }

  @EventHandler
  def onPlayerConsumedZongo(event: PlayerItemConsumeEvent): Unit = {
    val item = event.getItem
    if (!isZongo(item)) return

    val player = event.getPlayer
    val today = LocalDate.now()
    val exp = LocalDate.ofEpochDay(new NBTItem(item).getLong(NBTTagConstants.expiryDateTag))
    if (today.isBefore(exp)) {
      // マナを10%回復する
      manaApi.manaAmount(player).restoreFraction(0.1).runSync[SyncIO].unsafeRunSync()
      player.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0f, 1.2f)
    } else {
      // END_DATEと同じ日かその翌日以降なら
      // 死ぬ
      player.setHealth(0)

      val messages = deathMessages(player.getName)
      sendMessageToEveryoneIgnoringPreference(messages(new Random().nextInt(messages.size)))
    }
  }
}
