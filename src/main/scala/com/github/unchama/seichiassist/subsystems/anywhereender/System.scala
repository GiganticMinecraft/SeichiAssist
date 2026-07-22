package com.github.unchama.seichiassist.subsystems.anywhereender

import cats.data.Kleisli
import cats.effect.{Async, IO}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.anywhereender.bukkit.command.EnderChestCommand
import com.github.unchama.seichiassist.subsystems.anywhereender.domain.{
  AccessDenialReason,
  AnywhereEnderAccessPermitted
}
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.targetedeffect.commandsender.MessageEffectF
import com.github.unchama.targetedeffect.player.PlayerEffects
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

trait System[G[_]] extends Subsystem[G] {
  def accessApi: AnywhereEnderChestAPI[G]
}

object System {
  import ContextCoercion._
  import cats.implicits._

  def wired[F[_]: [g[_]] =>> BreakCountReadAPI[IO, g, Player]: [f[_]] =>> ContextCoercion[
    f,
    G
  ], G[_]: Async: [g[_]] =>> ContextCoercion[IO, g]: [g[_]] =>> ContextCoercion[g, IO]](
    configuration: SystemConfiguration
  )(implicit onMainThread: OnMinecraftServerThread[IO]): System[G] = new System[G] {

    override implicit val accessApi: AnywhereEnderChestAPI[G] = new AnywhereEnderChestAPI[G] {
      override def canAccessAnywhereEnderChest(
        player: Player
      ): G[AnywhereEnderAccessPermitted] = {
        implicitly[BreakCountReadAPI[IO, F, Player]]
          .seichiAmountDataRepository(player)
          .read
          .coerceTo[G]
          .map { _.levelCorrespondingToExp }
          .map { currentLevel =>
            if (currentLevel < configuration.requiredMinimumLevel) {
              Left(
                AccessDenialReason
                  .NotEnoughLevel(currentLevel, configuration.requiredMinimumLevel)
              )
            } else {
              Right(())
            }
          }
      }

      override def openEnderChestOrNotifyInsufficientLevel
        : Kleisli[G, Player, AnywhereEnderAccessPermitted] =
        Kleisli(canAccessAnywhereEnderChest).flatTap {
          case Left(AccessDenialReason.NotEnoughLevel(_, minimumLevel)) =>
            MessageEffectF[G](s"どこでもエンダーチェストを開くには整地レベルがLv${minimumLevel.level}以上である必要があります。")
          case Right(_) =>
            Kleisli((player: Player) =>
              PlayerEffects.openInventoryEffect(player.getEnderChest).run(player)
            ).mapK(ContextCoercion.asFunctionK[IO, G])
        }
    }

    override val commands: Map[String, TabExecutor] = Map("ec" -> EnderChestCommand.executor[G])
  }
}
