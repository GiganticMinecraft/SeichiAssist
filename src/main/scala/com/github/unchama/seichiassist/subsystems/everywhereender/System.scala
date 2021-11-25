package com.github.unchama.seichiassist.subsystems.everywhereender

import cats.data.Kleisli
import cats.effect.implicits.toEffectOps
import cats.effect.{Effect, IO, LiftIO, Sync}
import cats.implicits._
import cats.{Functor, Semigroupal}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiLevel
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.PlayerEffects
import org.bukkit.entity.Player

trait System[G[_]] extends Subsystem[G] {
  def accessApi: EverywhereEnderChestAPI[G]
}

object System {
  def wired[
    F[_]: BreakCountReadAPI[IO, *[_], Player] : Functor : Semigroupal : ContextCoercion[*[_], G],
    G[_]: Effect: LiftIO
  ](implicit onMainThread: OnMinecraftServerThread[IO]): System[G] = new System[G] {
    override def accessApi: EverywhereEnderChestAPI[G] = new EverywhereEnderChestAPI[G] {
      override def canAccessEverywhereEnderChest(player: Player): G[Boolean] = {
        val f = implicitly[BreakCountReadAPI[IO, F, Player]].seichiAmountDataRepository
          .apply(player)
          .read

        val g = ContextCoercion(f)
          .product(minimumLevel)
          .map { case (sad, minLevel) =>
            sad.levelCorrespondingToExp >= minLevel
          }

        g
      }

      override def openEnderChestOrNotifyInsufficientLevel: Kleisli[G, Player, Unit] = {
        Kleisli(player => {
          val effG = canAccessEverywhereEnderChest(player)
            .product(minimumLevel)
            .map { case (canOpen, minLevel) =>
              if (canOpen) {
                PlayerEffects.openInventoryEffect(player.getEnderChest)
              } else {
                MessageEffect(s"どこでもエンダーチェストを開くには整地レベルがLv${minLevel.level}以上である必要があります。")
              }
            }

          effG.toIO.flatMap(_ (player)).to[G]
        })
      }

      override def minimumLevel: G[SeichiLevel] = Sync[G].delay {
        SeichiLevel(SeichiAssist.seichiAssistConfig.getDokodemoEnderlevel)
      }
    }
  }
}