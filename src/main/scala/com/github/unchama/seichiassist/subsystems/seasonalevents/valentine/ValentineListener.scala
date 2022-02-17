package com.github.unchama.seichiassist.subsystems.seasonalevents.valentine

import cats.effect.{ConcurrentEffect, IO, LiftIO}
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.subsystems.seasonalevents.Util.randomlyDropItemAt
import com.github.unchama.seichiassist.subsystems.seasonalevents.domain.LastQuitPersistenceRepository
import com.github.unchama.seichiassist.subsystems.seasonalevents.valentine.Valentine._
import com.github.unchama.seichiassist.subsystems.seasonalevents.valentine.ValentineCookieEffectsHandler._
import com.github.unchama.seichiassist.subsystems.seasonalevents.valentine.ValentineItemData._
import com.github.unchama.seichiassist.util.Util.{grantItemStacksEffect, sendMessageToEveryoneIgnoringPreference}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.{SequentialEffect, TargetedEffect}
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor._
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.{EntityType, Monster, Player}
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.{EntityDamageByEntityEvent, EntityExplodeEvent}
import org.bukkit.event.player.{PlayerItemConsumeEvent, PlayerJoinEvent}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.{PotionEffect, PotionEffectType}

import java.time.LocalDateTime
import java.util.{Random, UUID}
import scala.util.chaining._

class ValentineListener[
  F[_] : ConcurrentEffect : NonServerThreadContextShift
](implicit
  effectEnvironment: EffectEnvironment,
  repository: LastQuitPersistenceRepository[F, UUID],
  ioOnMainThread: OnMinecraftServerThread[IO]) extends Listener {

  @EventHandler
  def onEntityExplode(event: EntityExplodeEvent): Unit = {
    val entity = event.getEntity
    if (!isInEvent || entity == null) return

    if (entity.isInstanceOf[Monster] && entity.isDead) {
      randomlyDropItemAt(entity, droppedCookie, itemDropRate)
    }
  }

  // モンスターの死因がクリーパーによる爆発の場合、確率でアイテムをドロップ
  @EventHandler
  def onEntityDeath(event: EntityDamageByEntityEvent): Unit = {
    if (!isInEvent) return

    val damager = event.getDamager
    if (damager == null) return

    if (event.getCause != DamageCause.ENTITY_EXPLOSION || damager.getType != EntityType.CREEPER) return

    event.getEntity match {
      case monster: Monster =>
        val entityMaxHealth = monster.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue
        // monsterが死んだならば
        if (entityMaxHealth <= event.getDamage) {
          randomlyDropItemAt(monster, droppedCookie, itemDropRate)
        }
    }
  }

  @EventHandler
  def onPlayerJoinEvent(event: PlayerJoinEvent): Unit = {
    if (isInEvent) {
      Seq(
        s"$LIGHT_PURPLE${END_DATE}までの期間限定で、イベント『＜ブラックバレンタイン＞リア充 vs 整地民！』を開催しています。",
        "詳しくは下記URLのサイトをご覧ください。",
        s"$DARK_GREEN$UNDERLINE$blogArticleUrl"
      ).foreach(
        event.getPlayer.sendMessage(_)
      )
    }
  }

  @EventHandler
  def giveValentineCookieToPlayer(event: PlayerJoinEvent): Unit = {
    if (!isInEvent) return

    val player = event.getPlayer
    val playerUuid = player.getUniqueId

    import cats.implicits._
    val program = for {
      _ <- NonServerThreadContextShift[F].shift
      lastQuit <- repository.loadPlayerLastQuit(playerUuid)
      _ <- LiftIO[F].liftIO {
        val baseDateTime =
          // 2022: 0時を超えてログインし続けていた人と初見さんに対応するための条件分岐
          if (cookieUnGivenPlayers.contains(playerUuid)) LocalDateTime.of(2022, 2, 18, 4, 10)
          else START_DATETIME
        val hasNotJoinedBeforeYet = lastQuit.forall { quit => quit.isBefore(baseDateTime) || quit.isEqual(baseDateTime) }

        val effects =
          if (hasNotJoinedBeforeYet) SequentialEffect(
            grantItemStacksEffect(cookieOf(player)),
            MessageEffect(s"${AQUA}チョコチップクッキーを付与しました。"),
            FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f))
          else TargetedEffect.emptyEffect

        effects.run(player)
      }
    } yield ()

    effectEnvironment.unsafeRunEffectAsync("チョコチップクッキーを付与するかどうかを判定する", program)
  }

  @EventHandler
  def onPlayerItemConsumeEvent(event: PlayerItemConsumeEvent): Unit = {
    val item = event.getItem
    val player = event.getPlayer
    if (isDroppedCookie(item) && isUsableCookie(item)) useDroppedCookie(player)
    if (isGiftedCookie(item) && isUsableCookie(item)) useGiftedCookie(player, item)
  }

  private def useDroppedCookie(player: Player): Unit = {
    val effect = randomlySelectEffect
    player.tap { player =>
      import player._
      sendMessage(getMessage(effect))
      addPotionEffect(getEffect(effect)._2)
      playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F)
    }
  }

  private def useGiftedCookie(player: Player, item: ItemStack): Unit = {
    if (ownerOf(item).contains(player.getUniqueId)) {
      // HP最大値アップ
      player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 20 * 60 * 10, 10))
    } else {
      // 死ぬ
      player.setHealth(0)

      val messages = deathMessages(player.getName, new NBTItem(item).getString(NBTTagConstants.producerNameTag))
      sendMessageToEveryoneIgnoringPreference(messages(new Random().nextInt(messages.size)))
    }
    player.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F)
  }
}
