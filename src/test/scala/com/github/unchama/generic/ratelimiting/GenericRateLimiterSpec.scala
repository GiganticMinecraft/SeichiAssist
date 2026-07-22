package com.github.unchama.generic.ratelimiting

import cats.effect.IO
import com.github.unchama.generic.algebra.typeclasses.OrderedMonus
import com.github.unchama.testutil.concurrent.tests.ConcurrentEffectTest
import com.github.unchama.testutil.execution.CatsEffectTestControl
import io.github.iltotore.iron.:|

import io.github.iltotore.iron.constraint.numeric.GreaterEqual
import io.github.iltotore.iron.refineUnsafe
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
    with CatsEffectTestControl {

  type Natural = Int :| GreaterEqual[0]

  implicit val natOrderedMonus: OrderedMonus[Natural] = new OrderedMonus[Natural] {
    override def empty: Natural = (0).refineUnsafe[GreaterEqual[0]]

    override def |-|(x: Natural, y: Natural): Natural =
      if (x >= y) (x - y).refineUnsafe[GreaterEqual[0]]
      else empty

    override def combine(x: Natural, y: Natural): Natural =
      (x + y).refineUnsafe[GreaterEqual[0]]

    override def compare(x: Natural, y: Natural): Int = (x: Int).compare(y)
  }

  /**
   * 新しい [[RateLimiter]] を
   *   - `seed` をパラメータ生成のシード
   * として作成する。
   */
  def newRandomRateLimiter(seed: Int): IO[RateLimiter[IO, Natural]]

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
        randomSeed <- IO.delay(Random.nextInt())
        rateLimiterA <- newRandomRateLimiter(randomSeed)
        rateLimiterB <- newRandomRateLimiter(randomSeed)
        _ <- rateLimiterA.peekAvailablePermissions

        sleepPeriod <- IO.delay {
          val duration = Random.nextInt(60).seconds
          assert(duration <= maxSleep)
          duration
        }

        _ <- IO.sleep(sleepPeriod)

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
