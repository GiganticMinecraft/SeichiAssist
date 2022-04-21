package com.github.unchama.seichiassist.subsystems.buildcount.application.actions

import cats.Monad
import cats.effect.concurrent.Ref
import cats.effect.{Effect, Sync}
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.fs2.workaround.fs3.Fs3Topic
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.minecraft.actions.SendMinecraftMessage
import com.github.unchama.seichiassist.subsystems.buildcount.application.BuildExpMultiplier
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.BuildExpAmount
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountData

/**
 * [[Player]] が手でブロックを設置した際に建築量を加算するアクションを提供する型クラス。
 */
trait IncrementBuildExpWhenBuiltByHand[F[_], Player] {

  def of(player: Player): F[Unit] = of(player, BuildExpAmount(1))

  def of(player: Player, by: BuildExpAmount): F[Unit]

}

object IncrementBuildExpWhenBuiltByHand {

  import cats.implicits._

  def apply[F[_], Player](
    implicit ev: IncrementBuildExpWhenBuiltByHand[F, Player]
  ): IncrementBuildExpWhenBuiltByHand[F, Player] = ev

  def using[F[_]: Monad: ClassifyPlayerWorld[*[_], Player]: SendMinecraftMessage[
    *[_],
    Player
  ], G[_]: Effect: ContextCoercion[F, *[_]], Player](
    dataRepository: KeyedDataRepository[Player, Ref[F, BuildAmountData]],
    dataTopic: Fs3Topic[G, Option[(Player, BuildAmountData)]]
  )(
    implicit multiplier: BuildExpMultiplier,
    sync: Sync[F]
  ): IncrementBuildExpWhenBuiltByHand[F, Player] =
    (player: Player, by: BuildExpAmount) => {
      val F: Monad[F] = implicitly

      F.ifM(ClassifyPlayerWorld[F, Player].isInBuildWorld(player))(
        for {
          newData <-
            dataRepository(player).updateAndGet(_.addExpAmount(by))
          _ <- EffectExtra.runAsyncAndForget[G, F, Unit] {
            dataTopic.publish1(Some((player, newData))).void
          }
        } yield (),
        F.unit
      )
    }
}
