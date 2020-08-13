package com.github.unchama.testutil.concurrent.sequencer

import cats.effect.{Async, Sync}
import cats.effect.concurrent.Deferred
import com.github.unchama.testutil.concurrent.Blocker
import cats.implicits._

class LinkedSequencer[F[_] : Async] extends Sequencer[F] {

  override val newBlockerList: F[LazyList[Blocker[F]]] = Async[F].delay {
    import LinkedSequencer._

    LazyList.iterate(
      LinkedBlocker(None, CompletableBlocker.unsafeBlocked)
    )(previous =>
      LinkedBlocker(Some(previous), CompletableBlocker.unsafeBlocked)
    )
  }

  override def toString: String = "LinkedSequencer"

}

object LinkedSequencer {

  /**
   * DeferredのBlockerとしてのラッパー
   */
  class CompletableBlocker[F[_]] private(promise: Deferred[F, Unit]) extends Blocker[F] {

    def complete: F[Unit] = promise.complete(())

    override def await(): F[Unit] = promise.get

  }

  object CompletableBlocker {
    def unsafeBlocked[F[_] : Async]: CompletableBlocker[F] = new CompletableBlocker[F](Deferred.unsafeUncancelable)
  }

  case class LinkedBlocker[F[_] : Async](previous: Option[LinkedBlocker[F]],
                                         blockedBlocker: CompletableBlocker[F]) extends Blocker[F] {

    /**
     * awaitが返す計算は、実行された時次の事後条件を満たす：
     *  - [[previous]] が空でない場合、中にある [[previous]] の [[blockedBlocker]] が完了している
     *  - このオブジェクトの [[blockedBlocker]] が完了している
     */
    override def await(): F[Unit] = {
      val waitPrevious = previous.map(_.blockedBlocker.await())

      waitPrevious.getOrElse(Sync[F].unit) >> blockedBlocker.complete
    }
  }

  def apply[F[_] : Async]: LinkedSequencer[F] = new LinkedSequencer[F]
}
