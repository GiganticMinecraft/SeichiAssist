package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.routines

import cats.effect.{ConcurrentEffect, IO, SyncIO, Timer}
import com.github.unchama.concurrent.{RepeatingRoutine, RepeatingTaskContext}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions.FairyRoutine
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.actions.BukkitRecoveryMana
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.FairyPersistence
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.speech.FairySpeech
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

import scala.concurrent.duration.FiniteDuration

class BukkitFairyRoutine(fairySpeech: FairySpeech[IO, Player])(
  implicit manaApi: ManaApi[IO, SyncIO, Player],
  context: RepeatingTaskContext,
  fairyPersistence: FairyPersistence[IO],
  concurrentEffect: ConcurrentEffect[IO],
  minecraftServerThread: OnMinecraftServerThread[IO],
  mineStackAPI: MineStackAPI[IO, Player, ItemStack]
) extends FairyRoutine[IO, Player] {

  override def start(player: Player): IO[Nothing] = {

    val repeatInterval: IO[FiniteDuration] = IO {
      import scala.concurrent.duration._

      1.minute
    }

    implicit val timer: Timer[IO] = IO.timer(context)

    RepeatingRoutine.permanentRoutine(
      repeatInterval,
      minecraftServerThread.runAction {
        new BukkitRecoveryMana[IO, SyncIO](player, fairySpeech).recovery.runAsync(_ => IO.unit)
      }
    )
  }
}
