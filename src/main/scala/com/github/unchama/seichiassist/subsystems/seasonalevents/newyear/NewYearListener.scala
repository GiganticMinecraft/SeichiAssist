package com.github.unchama.seichiassist.subsystems.seasonalevents.newyear

import cats.effect.{ConcurrentEffect, IO, LiftIO, SyncEffect, SyncIO}
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.ManagedWorld._
import com.github.unchama.seichiassist.MaterialSets
import com.github.unchama.seichiassist.subsystems.mana.ManaWriteApi
import com.github.unchama.seichiassist.subsystems.seasonalevents.domain.LastQuitPersistenceRepository
import com.github.unchama.seichiassist.subsystems.seasonalevents.newyear.NewYear._
import com.github.unchama.seichiassist.subsystems.seasonalevents.newyear.NewYearItemData._
import com.github.unchama.seichiassist.util.InventoryOperations.{addItem, dropItem, grantItemStacksEffect, isPlayerInventoryFull}
import com.github.unchama.targetedeffect.SequentialEffect
import com.github.unchama.targetedeffect.TargetedEffect.emptyEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import de.tr7zw.nbtapi.NBTItem
import org.bukkit.ChatColor._
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.{PlayerItemConsumeEvent, PlayerJoinEvent}
import org.bukkit.event.{EventHandler, EventPriority, Listener}

import java.time.LocalDate
import java.util.{Random, UUID}

class NewYearListener[F[_]: ConcurrentEffect: NonServerThreadContextShift, G[_]: SyncEffect](
  implicit effectEnvironment: EffectEnvironment,
  repository: LastQuitPersistenceRepository[F, UUID],
  manaApi: ManaWriteApi[G, Player],
  ioOnMainThread: OnMinecraftServerThread[IO]
) extends Listener {

  import cats.implicits._

  @EventHandler
  def onPlayerJoin(event: PlayerJoinEvent): Unit = {
    if (isInEvent) {
      val player = event.getPlayer

      List(
        s"$LIGHT_PURPLE${END_DATE}までの期間限定で、新年イベントを開催しています。",
        "詳しくは下記URLのサイトをご覧ください。",
        s"$DARK_GREEN$UNDERLINE$blogArticleUrl"
      ).foreach(player.sendMessage)
    }
  }

  @EventHandler
  def giveSobaToPlayer(event: PlayerJoinEvent): Unit = {
    if (!NewYear.sobaWillBeDistributed) return

    val player = event.getPlayer

    val program = for {
      _ <- NonServerThreadContextShift[F].shift
      lastQuit <- repository.loadPlayerLastQuit(player.getUniqueId)
      _ <- LiftIO[F].liftIO {
        val hasNotJoinedInEventYet = lastQuit.forall(NEW_YEAR_EVE.isEntirelyAfter)

        val effects =
          if (hasNotJoinedInEventYet)
            SequentialEffect(
              grantItemStacksEffect(sobaHead),
              MessageEffect(s"${BLUE}大晦日ログインボーナスとして記念品を入手しました。"),
              FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f)
            )
          else emptyEffect

        effects.run(player)
      }
    } yield ()

    effectEnvironment.unsafeRunEffectAsync("大晦日ログインボーナスヘッドを付与するかどうかを判定する", program)
  }

  @EventHandler
  def onPlayerConsumedNewYearApple(event: PlayerItemConsumeEvent): Unit = {
    val item = event.getItem
    if (!isNewYearApple(item)) return

    import cats.effect.implicits._

    val player = event.getPlayer
    val today = LocalDate.now()
    val expiryDate =
      new NBTItem(item).getObject(NBTTagConstants.expiryDateTag, classOf[LocalDate])
    if (today.isBefore(expiryDate) || today.isEqual(expiryDate)) {
      // マナを10%回復する
      manaApi.manaAmount(player).restoreFraction(0.1).runSync[SyncIO].unsafeRunSync()
      player.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0f, 1.2f)
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  def onNewYearBagPopped(event: BlockBreakEvent): Unit = {
    if (!isInEvent) return

    val player = event.getPlayer
    if (!player.getWorld.isSeichi) return
    // 整地スキルに対応していないブロックなら処理を終了
    if (!MaterialSets.materialsToCountBlockBreak.contains(event.getBlock.getType)) return

    val rand = new Random().nextDouble()
    if (rand < itemDropRate) {
      if (isPlayerInventoryFull(player)) {
        dropItem(player, newYearBag)
        player.sendMessage(s"${RED}インベントリに空きがなかったため、「お年玉袋」は地面にドロップしました。")
      } else {
        addItem(player, newYearBag)
        player.sendMessage(s"$AQUA「お年玉袋」を見つけたよ！")
      }
      player.playSound(player.getLocation, Sound.BLOCK_NOTE_BLOCK_HARP, 3.0f, 1.0f)
    }
  }
}
