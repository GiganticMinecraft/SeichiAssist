package com.github.unchama.generic.ratelimiting

import cats.effect.concurrent.Ref
import cats.effect.{Clock, Sync}
import com.github.unchama.generic.algebra.typeclasses.OrderedMonus
import com.github.unchama.generic.algebra.typeclasses.OrderedMonus._

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

object FixedWindowRateLimiter {

  import cats.implicits._

  def in[G[_]: Sync: Clock, A: OrderedMonus](
    maxPermits: A,
    resetDuration: FiniteDuration,
    firstPermits: Option[A] = None
  ): G[RateLimiter[G, A]] = {
    case class RateLimiterState(lastResetTimeStampInMilli: Long, permitsUsedSoFar: A)

    val inMillis = TimeUnit.MILLISECONDS

    val zero = OrderedMonus[A].empty
    val initialCount = maxPermits |-| firstPermits.getOrElse(maxPermits)
    val resetDurationInMillis = resetDuration.toMillis

    for {
      initialTime <- Clock[G].realTime(inMillis)
      stateRef <- Ref[G].of(RateLimiterState(initialTime, initialCount))
    } yield new RateLimiter[G, A] {
      override protected val A: OrderedMonus[A] = implicitly
      override def requestPermission(a: A): G[A] = {
        for {
          latestTime <- Clock[G].realTime(inMillis)

          obtainedPermits <- stateRef.modify {
            case RateLimiterState(lastRestoredTimeStampInMilli, permitsUsedSoFar) =>
              // もし十分な時間が経っていれば、現在までのどこかの時点でカウントのリセットが発生しているはずだから、
              // そのようなリセットの数を数える。
              //
              // currentTimeが十分に古く、floorDivの結果が負になる可能性があることに注意。
              // これは滅多に発生しないが、そのような状況はlatestTimeが取得されたよりも後にlastRestoredTimeStampInMilliが
              // 更新されたことを示しており、この modify の中で我々が再度resetする理由は無い。
              val numberOfResetsHappenedSinceLastReset =
                Math.max(
                  0,
                  Math
                    .floorDiv(latestTime - lastRestoredTimeStampInMilli, resetDurationInMillis)
                )

              val resetHappened = numberOfResetsHappenedSinceLastReset > 0

              val basePermitsCount = if (resetHappened) zero else permitsUsedSoFar

              val newPermitsCount = (basePermitsCount |+| a) min maxPermits
              val obtainedPermits = newPermitsCount |-| basePermitsCount

              // 最後にカウントのリセットが発生したタイムスタンプ。
              val restoredTimeStamp =
                if (!resetHappened) lastRestoredTimeStampInMilli
                else {
                  // `lastRestoredTimeStampInMilli + n * resetDuration` で、
                  // `latestTime` 以下となる最大のものを探せばよいが、これは次の式で計算できる
                  lastRestoredTimeStampInMilli + numberOfResetsHappenedSinceLastReset * resetDurationInMillis
                }

              (RateLimiterState(restoredTimeStamp, newPermitsCount), obtainedPermits)
          }
        } yield obtainedPermits
      }

      override def peekAvailablePermissions: G[A] =
        stateRef.get.map(maxPermits |-| _.permitsUsedSoFar)
    }
  }
}
