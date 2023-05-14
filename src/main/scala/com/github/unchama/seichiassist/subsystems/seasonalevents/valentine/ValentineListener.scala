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
import com.github.unchama.seichiassist.util.InventoryOperations.grantItemStacksEffect
import com.github.unchama.seichiassist.util.SendMessageEffect.sendMessageToEveryoneIgnoringPreference
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.{SequentialEffect, TargetedEffect}
import de.tr7zw.nbtapi.NBTItem
import org.bukkit.ChatColor._
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.{Creeper, EntityType, Monster, Player}
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.{EntityDamageByEntityEvent, EntityExplodeEvent}
import org.bukkit.event.player.{PlayerItemConsumeEvent, PlayerJoinEvent}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.{PotionEffect, PotionEffectType}

import java.util.{Random, UUID}
import scala.util.chaining._

class ValentineListener[F[_]: ConcurrentEffect: NonServerThreadContextShift](
  implicit effectEnvironment: EffectEnvironment,
  repository: LastQuitPersistenceRepository[F, UUID],
  ioOnMainThread: OnMinecraftServerThread[IO]
) extends Listener {

  // クリーパーが爆発した場合、確率でアイテムをドロップ
  @EventHandler
  def onEntityExplode(event: EntityExplodeEvent): Unit = {
    if (!isInEvent) return

    event.getEntity match {
      case creeper: Creeper =>
        randomlyDropItemAt(creeper, droppedCookie, itemDropRate)
      case _ =>
    }
  }

  // モンスターの死因がクリーパーによる爆発の場合、確率でアイテムをドロップ
  @EventHandler
  def onEntityDeath(event: EntityDamageByEntityEvent): Unit = {
    if (!isInEvent) return
    if (event.getCause != DamageCause.ENTITY_EXPLOSION) return

    val damager = event.getDamager
    if (damager == null || damager.getType != EntityType.CREEPER) return

    val excludedMonsters = Set(EntityType.WITCH, EntityType.PIG_ZOMBIE)
    event.getEntity match {
      case damaged: Monster if !excludedMonsters.contains(damaged.getType) =>
        val entityMaxHealth = damaged.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue
        // 巻き込まれたMonsterが死んだならば
        if (entityMaxHealth <= event.getDamage) {
          randomlyDropItemAt(damaged, droppedCookie, itemDropRate)
        }
      case _ =>
    }
  }

  @EventHandler
  def onPlayerJoinEvent(event: PlayerJoinEvent): Unit = {
    if (isInEvent) {
      Seq(
        s"$LIGHT_PURPLE${END_DATE_TIME}までの期間限定で、イベント『バレンタインイベント$EVENT_YEAR』を開催しています。",
        "詳しくは下記URLのサイトをご覧ください。",
        s"$DARK_GREEN$UNDERLINE$blogArticleUrl"
      ).foreach(event.getPlayer.sendMessage(_))
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
        val hasNotJoinedBeforeYet = lastQuit.forall(EVENT_DURATION.isEntirelyAfter)

        val effects =
          if (hasNotJoinedBeforeYet)
            SequentialEffect(
              grantItemStacksEffect(giftedCookieOf(player.getName, playerUuid)),
              MessageEffect(s"${AQUA}チョコチップクッキーを付与しました。"),
              FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f)
            )
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
      playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0f, 1.2f)
    }
  }

  private def useGiftedCookie(player: Player, item: ItemStack): Unit = {
    if (ownerOf(item).contains(player.getUniqueId)) {
      // HP最大値アップ
      player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 20 * 60 * 10, 10))
    } else {
      // 死ぬ
      player.setHealth(0)

      val messages = deathMessages(
        player.getName,
        new NBTItem(item).getString(NBTTagConstants.producerNameTag)
      )
      sendMessageToEveryoneIgnoringPreference(messages(new Random().nextInt(messages.size)))
    }
    player.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0f, 1.2f)
  }
}
