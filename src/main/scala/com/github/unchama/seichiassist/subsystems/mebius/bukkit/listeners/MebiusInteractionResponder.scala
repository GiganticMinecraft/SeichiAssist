package com.github.unchama.seichiassist.subsystems.mebius.bukkit.listeners

import cats.effect.SyncIO
import com.github.unchama.datarepository.bukkit.player.PlayerDataRepository
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.MaterialSets
import com.github.unchama.seichiassist.subsystems.mebius.bukkit.codec.BukkitMebiusItemStackCodec
import com.github.unchama.seichiassist.subsystems.mebius.domain.resources.MebiusMessages
import com.github.unchama.seichiassist.subsystems.mebius.domain.speech.{
  MebiusSpeech,
  MebiusSpeechStrength
}
import com.github.unchama.seichiassist.subsystems.mebius.service.MebiusSpeechService
import com.github.unchama.targetedeffect.SequentialEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.ChatColor._
import org.bukkit.Sound
import org.bukkit.entity.{Monster, Player}
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.{EntityDamageByEntityEvent, EntityDeathEvent}
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}
import org.bukkit.inventory.meta.Damageable

class MebiusInteractionResponder(
  implicit serviceRepository: PlayerDataRepository[MebiusSpeechService[SyncIO]],
  effectEnvironment: EffectEnvironment
) extends Listener {
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  def onDamage(event: EntityDamageByEntityEvent): Unit = {
    // プレイヤーがダメージを受けた場合
    event.getEntity match {
      case player: Player =>
        val helmet = player.getInventory.getHelmet
        val mebiusProperty = BukkitMebiusItemStackCodec
          .decodePropertyOfOwnedMebius(player)(helmet)
          .getOrElse(return)

        val speechService = serviceRepository(player)

        val messageProgram =
          if (
            helmet.getItemMeta.asInstanceOf[Damageable].getDamage >= helmet
              .getType
              .getMaxDurability - 10
          ) {
            MebiusMessages.onDamageBreaking.pickOne[SyncIO].flatMap { message =>
              // 耐久閾値を超えていたら破損警告
              speechService.tryMakingSpeech(
                mebiusProperty,
                MebiusSpeech(
                  message.interpolate(mebiusProperty.ownerNickname),
                  MebiusSpeechStrength.Medium
                )
              )
            }
          } else
            event.getDamager match {
              case monster: Monster =>
                // モンスターからダメージを受けた場合の対モンスターメッセージ
                MebiusMessages.onDamageWarnEnemy.pickOne[SyncIO].flatMap { message =>
                  speechService.tryMakingSpeech(
                    mebiusProperty,
                    MebiusSpeech(
                      message.interpolate(mebiusProperty.ownerNickname, monster.getName),
                      MebiusSpeechStrength.Medium
                    )
                  )
                }
              case _ => SyncIO.unit
            }

        messageProgram.unsafeRunSync()
      case _ =>
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  def onBreak(event: PlayerItemBreakEvent): Unit = {
    val player = event.getPlayer

    BukkitMebiusItemStackCodec
      .decodePropertyOfOwnedMebius(player)(event.getBrokenItem)
      .foreach { property =>
        val speechService = serviceRepository(player)

        import cats.implicits._

        effectEnvironment.unsafeRunEffectAsync(
          "Mebius破壊時のエフェクトを再生する",
          MebiusMessages.onMebiusBreak.pickOne[SyncIO].toIO.flatMap { message =>
            speechService
              .makeSpeechIgnoringBlockage(
                property,
                MebiusSpeech(
                  message.interpolate(property.ownerNickname),
                  MebiusSpeechStrength.Medium
                )
              )
              .toIO >> SequentialEffect(
              MessageEffect(
                s"${BukkitMebiusItemStackCodec.displayNameOfMaterializedItem(property)}${RESET}が旅立ちました。"
              ),
              FocusedSoundEffect(Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 0.1f)
            ).run(player)
          }
        )
      }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  def onKill(event: EntityDeathEvent): Unit = {
    // プレイヤーがモンスターを倒した場合以外は除外
    val killedMonster = event.getEntity
    val player = killedMonster.getKiller
    if (killedMonster == null || player == null) return

    // もしモンスター名が取れなければ除外
    val killedMonsterName = killedMonster.getName
    if (killedMonsterName == "") return

    val mebiusProperty =
      BukkitMebiusItemStackCodec
        .decodePropertyOfOwnedMebius(player)(player.getInventory.getHelmet)
        .getOrElse(return)

    val speechService = serviceRepository(player)

    MebiusMessages
      .onMonsterKill
      .pickOne[SyncIO]
      .flatMap { message =>
        speechService.tryMakingSpeech(
          mebiusProperty,
          MebiusSpeech(
            message.interpolate(mebiusProperty.ownerNickname, killedMonsterName),
            MebiusSpeechStrength.Medium
          )
        )
      }
      .unsafeRunSync()
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
  def onBlockBreak(event: BlockBreakEvent): Unit = {
    if (!MaterialSets.materials.contains(event.getBlock.getType)) return

    val player = event.getPlayer

    val mebiusProperty =
      BukkitMebiusItemStackCodec
        .decodePropertyOfOwnedMebius(player)(player.getInventory.getHelmet)
        .getOrElse(return)

    val speechService = serviceRepository(player)

    MebiusMessages
      .onBlockBreak
      .pickOne[SyncIO]
      .flatMap { message =>
        speechService.tryMakingSpeech(
          mebiusProperty,
          MebiusSpeech(
            message.interpolate(mebiusProperty.ownerNickname),
            MebiusSpeechStrength.Medium
          )
        )
      }
      .unsafeRunSync()
  }
}
