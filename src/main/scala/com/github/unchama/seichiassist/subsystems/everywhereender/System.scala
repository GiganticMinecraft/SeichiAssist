package com.github.unchama.seichiassist.subsystems.everywhereender

import cats.Functor
import cats.data.Kleisli
import cats.effect.implicits.toEffectOps
import cats.effect.{Effect, IO, LiftIO, Sync}
import cats.implicits._
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.targetedeffect.commandsender.{MessageEffect, MessageEffectF}
import com.github.unchama.targetedeffect.player.PlayerEffects
import org.bukkit.entity.Player

trait System[F[_]] extends Subsystem[F] {
  def accessApi: EverywhereEnderChestAPI[F]
}

object System {
  /**
   * @tparam F read context
   * @return wired subsystem
   */
  def wired[
    F[_]: BreakCountReadAPI[IO, *, Player]: Effect
  ](implicit onMainThread: OnMinecraftServerThread[IO]): System[F] = new System[F] {
    override def accessApi: EverywhereEnderChestAPI[F] = new EverywhereEnderChestAPI[F] {
      override def canAccessEverywhereEnderChest(player: Player): F[Boolean] =
        implicitly[BreakCountReadAPI[IO, F, Player]].seichiAmountDataRepository
          .apply(player)
          .read
          .map(sad => sad.levelCorrespondingToExp.level >= 25)

      /**
       * [[canAccessEverywhereEnderChest]]が
       *   - `false`を返す場合はエラーメッセージを表示する。
       *   - `true`を返す場合はどこでもエンダーチェストを開ける。
       *
       * @return 上記したような作用を記述する[[Kleisli]]
       */
      override def openEnderChestOrError(player: Player): Kleisli[F, Player, Unit] = {
        val effF = canAccessEverywhereEnderChest(player).map(canOpen => {
          if (canOpen) {
            PlayerEffects.openInventoryEffect(player.getEnderChest)
          } else {
            MessageEffect("どこでもエンダーチェストを開くには整地レベルがLv25以上である必要があります。")
          }
        })

        Kleisli(player => effF.toIO.flatMap(targetedEffect => targetedEffect(player)).to[F])
      }
    }
  }
}