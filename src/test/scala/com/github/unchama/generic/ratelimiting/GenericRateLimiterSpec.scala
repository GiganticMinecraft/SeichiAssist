package com.github.unchama.generic.ratelimiting

import cats.effect.Timer
import com.github.unchama.generic.algebra.typeclasses.OrderedMonus
import com.github.unchama.testutil.concurrent.tests.ConcurrentEffectTest
import com.github.unchama.testutil.execution.MonixTestSchedulerTests
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.NonNegative
import eu.timepit.refined.refineV
import monix.catnap.SchedulerEffect
import monix.eval.Task
import monix.execution.ExecutionModel
import monix.execution.schedulers.TestScheduler
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.util.Random

/**
 * 一般の [[RateLimiter]] に関して成り立つべき性質をテストするテストスイート。
 *
 * Mix-inし、`newRandomRateLimiter` を各[[RateLimiter]]の実装でoverrideして利用されることを想定している。
 */
trait GenericRateLimiterSpec
    extends AnyWordSpecLike
    with Matchers
    with ConcurrentEffectTest
    with MonixTestSchedulerTests {

  implicit private val monixScheduler: TestScheduler = TestScheduler(
    ExecutionModel.SynchronousExecution
  )
  implicit private val monixTimer: Timer[Task] = SchedulerEffect.timer(monixScheduler)

  type Natural = Int Refined NonNegative

  implicit val natOrderedMonus: OrderedMonus[Natural] = new OrderedMonus[Natural] {
    override def empty: Natural = 0

    override def |-|(x: Natural, y: Natural): Natural =
      if (x >= y) refineV[NonNegative].unsafeFrom(x - y)
      else empty

    override def combine(x: Natural, y: Natural): Natural =
      refineV[NonNegative].unsafeFrom(x + y)

    override def compare(x: Natural, y: Natural): Int = x.value.compare(y.value)
  }

  /**
   * 新しい [[RateLimiter]] を
   *   - `seed` をパラメータ生成のシード
   *   - `monixTimer` をスケジューラ
   *
   * として作成する。
   */
  def newRandomRateLimiter(seed: Int)(
    implicit monixTimer: Timer[Task]
  ): Task[RateLimiter[Task, Natural]]

  /**
   * [[RateLimiter.peekAvailablePermissions]] の呼び出し自体が、[[RateLimiter]]の動作に干渉しないことをテストする。
   *
   * このテスト項目は、より具体的には以下のような手続きを取る：
   *   - レートリミッターを二つ作成し、片方で [[RateLimiter.peekAvailablePermissions]] を呼ぶ
   *   - ランダムな秒数 (60秒以下) 時計の針を進める
   *   - 二つのレートリミッターの [[RateLimiter]] を呼び、結果が等しいことを確認する
   */
  def keepPermitsEqual(): Unit = {
    import scala.concurrent.duration._

    "keep permits equal with another RateLimiter which has not been peeked, after sleeping for not more than 60 seconds" in {
      val maxSleep = 1.minute

      val program = for {
        randomSeed <- Task(Random.nextInt())
        rateLimiterA <- newRandomRateLimiter(randomSeed)
        rateLimiterB <- newRandomRateLimiter(randomSeed)
        _ <- rateLimiterA.peekAvailablePermissions

        sleepPeriod <- Task {
          val duration = Random.nextInt(60).seconds
          assert(duration <= maxSleep)
          duration
        }

        _ <- monixTimer.sleep(sleepPeriod)

        peekA <- rateLimiterA.peekAvailablePermissions
        peekB <- rateLimiterB.peekAvailablePermissions
      } yield {
        assert(peekA == peekB, s"peek result equal with seed: $randomSeed")
        ()
      }

      awaitForProgram(runConcurrent(program)(1000), maxSleep)
    }
  }
}
