package com.github.unchama.seichiassist.subsystems.gachapoint.domain

import cats.effect.{Concurrent, Sync}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.concurrent.RecoveringSemaphore
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.gachapoint.GachaPoint
import cats.Monad
import cats.effect.{Ref, Temporal}

/**
 * 特定のプレーヤーについてガチャポイント変換の制御を提供するオブジェクトのクラス。
 */
class BatchUsageSemaphore[F[_]: Monad, G[_]: [f[_]] =>> ContextCoercion[f, F]](
  gachaPointRef: Ref[G, GachaPoint],
  grantAction: GrantGachaTicketToAPlayer[F]
)(recoveringSemaphore: RecoveringSemaphore[F]) {

  import cats.implicits._

  /**
   * 576個(= 64 * 9スタック)のバッチでガチャポイント変換を行い、 [[BatchUsageSemaphore.usageInterval]]の間使用不可にする作用。
   */
  def tryLargeBatchTransaction: F[Unit] =
    recoveringSemaphore.tryUse(
      ContextCoercion {
        gachaPointRef.modify { _.useInLargeBatch.asTuple }
      }.flatTap(grantAction.give).void,
      Monad[F].unit
    )(BatchUsageSemaphore.usageInterval)

  /**
   * 64個(= 64 * 1スタック)のバッチでガチャポイント変換を行い、 [[BatchUsageSemaphore.usageInterval]]の間使用不可にする作用。
   */
  def trySmallBatchTransaction: F[Unit] =
    recoveringSemaphore.tryUse(
      ContextCoercion {
        gachaPointRef.modify { _.useInSmallBatch.asTuple }
      }.flatTap(grantAction.give).void,
      Monad[F].unit
    )(BatchUsageSemaphore.usageInterval)
}

object BatchUsageSemaphore {

  import cats.implicits._

  import scala.concurrent.duration._

  /**
   * バッチでガチャポイントをガチャ券へと変換する際のクールダウン時間。
   */
  final val usageInterval = 1.second

  /**
   * プレーヤーが持つガチャポイントとプレーヤーへガチャ券を与える作用から [[BatchUsageSemaphore]]を作成する。
   */
  def newIn[G[_]: Sync: [f[_]] =>> ContextCoercion[f, F], F[_]: cats.effect.Async](
    gachaPointRef: Ref[G, GachaPoint],
    grantAction: GrantGachaTicketToAPlayer[F]
  ): G[BatchUsageSemaphore[F, G]] =
    RecoveringSemaphore
      .newIn[G, F]
      .map(rs => new BatchUsageSemaphore(gachaPointRef, grantAction)(rs))

}
