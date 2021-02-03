package com.github.unchama.seichiassist.subsystems.breakcount.application.actions

import cats.Monad
import cats.effect.concurrent.Ref
import cats.effect.{Effect, Sync}
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiExpAmount
import fs2.concurrent.Topic

trait IncrementSeichiExp[F[_], Player] {

  def of(player: Player, by: SeichiExpAmount): F[Unit]

}

object IncrementSeichiExp {

  import cats.implicits._

  def apply[F[_], Player](implicit ev: IncrementSeichiExp[F, Player]): IncrementSeichiExp[F, Player] = ev

  /**
   * 与えられたデータレポジトリと更新を流すトピックを用いてプレーヤーの整地量を増加させるような
   * 代数を作成する。
   */
  def using[
    F[_]
    : Sync
    : ClassifyPlayerWorld[*[_], Player],
    G[_] : Effect : ContextCoercion[F, *[_]],
    Player
  ](dataRepository: KeyedDataRepository[Player, Ref[F, SeichiAmountData]],
    dataTopic: Topic[G, Option[(Player, SeichiAmountData)]]): IncrementSeichiExp[F, Player] =
    (player, by) => {
      val F: Monad[F] = implicitly

      F.ifM(ClassifyPlayerWorld[F, Player].isInSeichiCountingWorld(player))(
        for {
          newData <- dataRepository(player).updateAndGet(_.addExpAmount(by))
          _ <- EffectExtra.runAsyncAndForget[G, F, Unit] {
            dataTopic.publish1(Some((player, newData)))
          }
        } yield (),
        F.unit
      )
    }
}
