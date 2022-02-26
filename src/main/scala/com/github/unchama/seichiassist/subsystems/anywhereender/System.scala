package com.github.unchama.seichiassist.subsystems.anywhereender

import cats.data.Kleisli
import cats.effect.{Effect, IO, LiftIO}
import cats.{Functor, Semigroupal}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.anywhereender.bukkit.command.EnderChestCommand
import com.github.unchama.seichiassist.subsystems.anywhereender.domain.{AccessDenialReason, CanAccessEverywhereEnderChest}
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiLevel
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.PlayerEffects
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

trait System[G[_]] extends Subsystem[G] {
  def accessApi: AnywhereEnderChestAPI[G]
}

object System {
  import ContextCoercion._
  import cats.implicits._

  def wired[
    F[_]: BreakCountReadAPI[IO, *[_], Player] : Functor : Semigroupal : ContextCoercion[*[_], G],
    G[_]: Effect
  ](minimumRequiredLevel: Int)(
    implicit onMainThread: OnMinecraftServerThread[IO]
  ): System[G] = new System[G] {

    override implicit val accessApi: AnywhereEnderChestAPI[G] = new AnywhereEnderChestAPI[G] {
      override def canAccessEverywhereEnderChest(player: Player): G[CanAccessEverywhereEnderChest] = {
        implicitly[BreakCountReadAPI[IO, F, Player]]
          .seichiAmountDataRepository(player).read
          .coerceTo[G]
          .map { seichiAmountData =>
            val currentLevel = seichiAmountData.levelCorrespondingToExp

            if (currentLevel < minimumLevel) {
              Left(AccessDenialReason.NotEnoughLevel(currentLevel, minimumLevel))
            } else {
              Right(())
            }
          }
      }

      override def openEnderChestOrNotifyInsufficientLevel: Kleisli[G, Player, CanAccessEverywhereEnderChest] =
        Kleisli(canAccessEverywhereEnderChest)
          .flatTap {
            case Left(AccessDenialReason.NotEnoughLevel(_, minimumLevel)) =>
              MessageEffect(
                s"どこでもエンダーチェストを開くには整地レベルがLv${minimumLevel}以上である必要があります。"
              ).mapK(LiftIO.liftK)
            case Right(_) =>
              Kleisli((player: Player) =>
                PlayerEffects.openInventoryEffect(player.getEnderChest).run(player)
              ).mapK(LiftIO.liftK)
          }

      override def minimumLevel: SeichiLevel = SeichiLevel(minimumRequiredLevel)
    }

    override val commands: Map[String, TabExecutor] = Map(
      "ec" -> EnderChestCommand.executor[G]
    )
  }
}