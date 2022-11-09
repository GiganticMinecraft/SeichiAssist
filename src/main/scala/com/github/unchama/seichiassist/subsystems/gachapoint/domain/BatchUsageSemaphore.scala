package com.github.unchama.seichiassist.subsystems.gachapoint.domain

import cats.FlatMap
import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, Sync, Timer}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.concurrent.RecoveringSemaphore
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.gachapoint.GachaPoint

/**
 * 特定のプレーヤーについてガチャポイント変換の制御を提供するオブジェクトのクラス。
 */
class BatchUsageSemaphore[F[_]: FlatMap, G[_]: ContextCoercion[*[_], F]](
  gachaPointRef: Ref[G, GachaPoint],
  grantAction: GrantGachaTicketToAPlayer[F]
)(recoveringSemaphore: RecoveringSemaphore[F]) {

  import cats.implicits._

  /**
   * バッチでのガチャポイント変換を行い、 [[BatchUsageSemaphore.usageInterval]]の間使用不可にする作用。
   */
  def tryBatchTransaction: F[Unit] =
    recoveringSemaphore.tryUse {
      ContextCoercion {
        gachaPointRef.modify { point => point.useInBatch.asTuple }
      }.flatTap(grantAction.give)
    }(BatchUsageSemaphore.usageInterval)

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
  def newIn[G[_]: Sync: ContextCoercion[*[_], F], F[_]: Concurrent: Timer](
    gachaPointRef: Ref[G, GachaPoint],
    grantAction: GrantGachaTicketToAPlayer[F]
  ): G[BatchUsageSemaphore[F, G]] =
    RecoveringSemaphore
      .newIn[G, F]
      .map(rs => new BatchUsageSemaphore(gachaPointRef, grantAction)(rs))

}
