package com.github.unchama.seichiassist.subsystems.anywhereender

import cats.data.Kleisli
import cats.effect.implicits._
import cats.effect.{Effect, IO, LiftIO}
import cats.implicits._
import cats.{Functor, Semigroupal}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiLevel
import com.github.unchama.seichiassist.subsystems.anywhereender.bukkit.command.EnderChestCommand
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.PlayerEffects
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

trait System[G[_]] extends Subsystem[G] {
  def accessApi: AnywhereEnderChestAPI[G]
}

object System {
  def wired[
    F[_]: BreakCountReadAPI[IO, *[_], Player] : Functor : Semigroupal : ContextCoercion[*[_], G],
    G[_]: Effect: LiftIO
  ](minimumRequiredLevel: Int)(
    implicit onMainThread: OnMinecraftServerThread[IO]
  ): System[G] = new System[G] {
    override val commands: Map[String, TabExecutor] = Map(
      "ec" -> EnderChestCommand.executor[G]
    )

    override implicit val accessApi: AnywhereEnderChestAPI[G] = new AnywhereEnderChestAPI[G] {
      override def canAccessEverywhereEnderChest(player: Player): G[Boolean] = {
        val f: F[SeichiAmountData] = implicitly[BreakCountReadAPI[IO, F, Player]]
          .seichiAmountDataRepository
          .apply(player)
          .read

        ContextCoercion(f)
          .map { sad =>
            sad.levelCorrespondingToExp >= minimumLevel
          }
      }

      override def openEnderChestOrNotifyInsufficientLevel: Kleisli[G, Player, Unit] = {
        Kleisli(player => {
          val effG = canAccessEverywhereEnderChest(player)
            .map { canOpen =>
              if (canOpen) {
                PlayerEffects.openInventoryEffect(player.getEnderChest)
              } else {
                MessageEffect(s"どこでもエンダーチェストを開くには整地レベルがLv${minimumLevel}以上である必要があります。")
              }
            }

          effG.toIO.flatMap(_ (player)).to[G]
        })
      }

      override def minimumLevel: SeichiLevel = SeichiLevel(minimumRequiredLevel)
    }
  }
}