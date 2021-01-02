package com.github.unchama.seichiassist.subsystems.seasonalevents.newyear

import java.time.LocalDate
import java.util.{Random, UUID}

import cats.effect.{ConcurrentEffect, IO, LiftIO}
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.ManagedWorld._
import com.github.unchama.seichiassist.subsystems.seasonalevents.domain.LastQuitPersistenceRepository
import com.github.unchama.seichiassist.subsystems.seasonalevents.newyear.NewYear.{START_DATE, isInEvent, itemDropRate}
import com.github.unchama.seichiassist.subsystems.seasonalevents.newyear.NewYearItemData._
import com.github.unchama.seichiassist.util.Util.{addItem, dropItem, grantItemStacksEffect, isPlayerInventoryFull}
import com.github.unchama.seichiassist.{MaterialSets, SeichiAssist}
import com.github.unchama.targetedeffect.TargetedEffect.emptyEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor._
import org.bukkit.Sound
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.{PlayerItemConsumeEvent, PlayerJoinEvent}
import org.bukkit.event.{EventHandler, EventPriority, Listener}

class NewYearListener[F[_] : ConcurrentEffect : NonServerThreadContextShift]
  (implicit effectEnvironment: EffectEnvironment, repository: LastQuitPersistenceRepository[F, UUID]) extends Listener {

  import cats.implicits._

  @EventHandler
  def giveSobaToPlayer(event: PlayerJoinEvent): Unit = {
    if (!NewYear.sobaWillBeDistributed) return

    val player = event.getPlayer

    val program = for {
      _ <- NonServerThreadContextShift[F].shift
      lastQuit <- repository.loadPlayerLastQuit(player.getUniqueId)
      _ <- LiftIO[F].liftIO(IO{
        val hasNotJoinedInEventYet = lastQuit match {
          case Some(dateTime) => dateTime.isBefore(START_DATE.atStartOfDay())
          case None => true
        }

        val effects =
          if (hasNotJoinedInEventYet) List(
            grantItemStacksEffect(sobaHead),
            MessageEffect(s"${BLUE}大晦日ログインボーナスとして記念品を入手しました。"),
            FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f))
          else List(emptyEffect)

        effects.traverse(_.run(player))
      })
    } yield ()

    effectEnvironment.runEffectAsync("大晦日ログインボーナスヘッドを付与するかどうかを判定する", program)
  }

  @EventHandler
  def onPlayerConsumedNewYearApple(event: PlayerItemConsumeEvent): Unit = {
    val item = event.getItem
    if (!isNewYearApple(item)) return

    val player = event.getPlayer
    val today = LocalDate.now()
    val expiryDate = new NBTItem(item).getObject(NBTTagConstants.expiryDateTag, classOf[LocalDate])
    if (today.isBefore(expiryDate) || today.isEqual(expiryDate)) {
      val playerData = SeichiAssist.playermap(player.getUniqueId)
      val manaState = playerData.manaState
      val maxMana = manaState.calcMaxManaOnly(player, playerData.level)
      // マナを10%回復する
      manaState.increase(maxMana * 0.1, player, playerData.level)
      player.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F)
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  def onNewYearBagPopped(event: BlockBreakEvent): Unit = {
    if (!isInEvent) return

    val player = event.getPlayer
    val block = event.getBlock
    if (!player.getWorld.isSeichi) return
    if (!MaterialSets.materials.contains(block.getType)) return

    val rand = new Random().nextDouble()
    if (rand < itemDropRate) {
      if (isPlayerInventoryFull(player)) {
        dropItem(player, newYearBag)
        player.sendMessage(s"${RED}インベントリに空きがなかったため、「お年玉袋」は地面にドロップしました。")
      } else {
        addItem(player, newYearBag)
        player.sendMessage(s"$AQUA「お年玉袋」を見つけたよ！")
      }
      player.playSound(player.getLocation, Sound.BLOCK_NOTE_HARP, 3.0f, 1.0f)
    }
  }
}