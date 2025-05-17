package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.routines

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, IO, SyncIO, Timer}
import com.github.unchama.concurrent.{RepeatingRoutine, RepeatingTaskContext}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions.FairyRoutine
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.actions.BukkitRecoveryMana
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.FairyPersistence
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.speech.FairySpeech
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

import scala.concurrent.duration.{DurationInt, FiniteDuration}

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

      30.seconds
    }

    implicit val timer: Timer[IO] = IO.timer(context)

    val seconds = Ref.unsafe(0.seconds)

    def countUp: IO[Unit] = seconds.update(_ + 30.seconds)

    RepeatingRoutine.permanentRoutine(
      repeatInterval,
      minecraftServerThread.runAction {
        (for {
          seconds <- seconds.get
          _ <- new BukkitRecoveryMana[IO, SyncIO](player, fairySpeech).recovery(seconds)
          _ <- countUp
        } yield ()).runAsync(_ => IO.unit)
      }
    )
  }
}
