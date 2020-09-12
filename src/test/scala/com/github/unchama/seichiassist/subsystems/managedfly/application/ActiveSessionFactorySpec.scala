package com.github.unchama.seichiassist.subsystems.managedfly.application

import java.util.concurrent.Executors

import cats.Monad
import cats.effect.{ContextShift, IO, SyncIO, Timer}
import com.github.unchama.seichiassist.subsystems.managedfly.domain.RemainingFlyDuration
import com.github.unchama.testutil.concurrent.ParallelIOTest
import monix.catnap.SchedulerEffect
import monix.execution.schedulers.TestScheduler
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.concurrent.{Await, ExecutionContext}

class ActiveSessionFactorySpec
  extends AnyWordSpec
    with ScalaCheckPropertyChecks
    with Matchers with ScalaFutures with Eventually
    with ParallelIOTest {
  val cachedThreadPool: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  implicit val shift: ContextShift[IO] = IO.contextShift(cachedThreadPool)

  val monixScheduler: TestScheduler = TestScheduler()
  implicit val monixTimer: Timer[IO] = SchedulerEffect.timer(monixScheduler)
  val realTimeTimer: Timer[IO] = IO.timer(cachedThreadPool)

  val mock = new Mock[IO, SyncIO]

  import mock._

  import scala.concurrent.duration._

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(timeout = 5.seconds, interval = 10.millis)

  "Fly session" should {
    "be able to tell if it is active or not" in {
      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(
          expConsumptionAmount = 0
        )

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] = playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[IO, PlayerMockReference]()

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          InfiniteExperience,
          initiallyIdle = false
        ).toIO

        // when
        session <- factory.start[SyncIO](RemainingFlyDuration.Infinity).run(playerRef)
        // then
        _ <- IO {
          eventually {
            session.isActive.unsafeRunSync() shouldBe true
          }
        }

        // when
        _ <- session.finish
        // then
        _ <- IO {
          eventually {
            session.isActive.unsafeRunSync() shouldBe false
          }
        }
      } yield ()

      runParallel(10)(program).unsafeRunSync()
    }

    "synchronize player's fly status once started" in {
      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(
          expConsumptionAmount = 0
        )

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] = playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[IO, PlayerMockReference]()

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          InfiniteExperience,
          initiallyIdle = false
        ).toIO

        // when
        session <- factory.start[SyncIO](RemainingFlyDuration.Infinity).run(playerRef)

        // then
        _ <- IO {
          eventually {
            playerRef.isFlyingMutex.readLatest.unsafeRunSync() shouldBe true
          }
        }

        // cleanup
        _ <- session.finish
      } yield ()

      runParallel(10)(program).unsafeRunSync()
    }

    "synchronize player's fly status when cancelled or complete" in {
      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(
          expConsumptionAmount = 0
        )

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] = playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[IO, PlayerMockReference]()

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          InfiniteExperience,
          initiallyIdle = false
        ).toIO

        // when
        session <- factory.start[SyncIO](RemainingFlyDuration.Infinity).run(playerRef)
        _ <- session.finish

        // then
        _ <- IO {
          eventually {
            playerRef.isFlyingMutex.readLatest.unsafeRunSync() shouldBe false
          }
        }
      } yield ()

      runParallel(10)(program).unsafeRunSync()
    }

    "terminate immediately if the player does not have enough experience" in {
      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(
          expConsumptionAmount = 100
        )

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] = playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[IO, PlayerMockReference]()

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          FiniteNonNegativeExperience(99),
          initiallyIdle = false
        ).toIO

        // when
        session <- factory.start[SyncIO](RemainingFlyDuration.Infinity).run(playerRef)

        // then
        _ <- IO {
          eventually {
            session.isActive.unsafeRunSync() shouldBe false
          }
        }
      } yield ()

      runParallel(10)(program).unsafeRunSync()
    }

    "not consume player experience in first 1 minute even if terminated" in {
      val originalExp = FiniteNonNegativeExperience(150)

      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(
          expConsumptionAmount = 100
        )

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] = playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[IO, PlayerMockReference]()

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          initialExperience = originalExp,
          initiallyIdle = false
        ).toIO

        // when
        session <- factory.start[SyncIO](RemainingFlyDuration.Infinity).run(playerRef)
        // セッションが有効になるまで待つ
        _ <- IO {
          eventually {
            session.isActive.unsafeRunSync() shouldBe true
          }
        }
        _ <- session.finish

        // then
        _ <- IO {
          eventually {
            playerRef.experienceMutex.readLatest.unsafeRunSync() shouldBe originalExp
          }
        }
      } yield ()

      runParallel(10)(program).unsafeRunSync()
    }

    "consume player experience every minute as specified by the configuration" in {
      val originalExp = 100000
      val minutesToWait = 100

      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(
          expConsumptionAmount = 100
        )

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] = playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[IO, PlayerMockReference]()

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          FiniteNonNegativeExperience(originalExp),
          initiallyIdle = false
        ).toIO

        // when
        session <- factory.start[SyncIO](RemainingFlyDuration.Infinity).run(playerRef)
        // セッションが有効になるまで待つ
        _ <- IO {
          eventually {
            session.isActive.unsafeRunSync() shouldBe true
          }
        }

        // then
        _ <- monixTimer.sleep(30.seconds)
        _ <- Monad[IO].iterateWhileM(0) { sleptMinute =>
          val expectedExperience = FiniteNonNegativeExperience(originalExp - sleptMinute * 100)

          for {
            _ <- IO {
              eventually {
                playerRef.experienceMutex.readLatest.unsafeRunSync() shouldBe expectedExperience
              }
            }
            _ <- monixTimer.sleep(1.minute)
          } yield sleptMinute + 1
        }(_ < minutesToWait)

        // cleanup
        _ <- session.finish
      } yield ()

      val programs = runParallel(10)(program)

      val future = programs.unsafeToFuture()


      // FIXME Thread.sleepは各FiberがmonixTimer.sleepに到達するのを待っている。これを除去できるか？
      monixScheduler.tick(30.seconds)
      Thread.sleep(200)
      for (_ <- 0 to minutesToWait) {
        monixScheduler.tick(1.minute)
        Thread.sleep(20)
      }

      Await.result(future, 30.seconds)
    }

    "not consume player experience whenever player is idle" in {
      val originalExp = 100000
      val minutesToWait = 100

      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(
          expConsumptionAmount = 100
        )

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] = playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[IO, PlayerMockReference]()

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          FiniteNonNegativeExperience(originalExp),
          initiallyIdle = false
        ).toIO

        // when
        session <- factory.start[SyncIO](RemainingFlyDuration.Infinity).run(playerRef)
        // セッションが有効になるまで待つ
        _ <- IO {
          eventually {
            session.isActive.unsafeRunSync() shouldBe true
          }
        }

        // then
        _ <- monixTimer.sleep(30.seconds)
        _ <- Monad[IO].iterateWhileM(0) { sleptMinute =>
          val expectedExperience = FiniteNonNegativeExperience(originalExp - sleptMinute * 100)

          for {
            _ <- IO {
              eventually {
                playerRef.experienceMutex.readLatest.unsafeRunSync() shouldBe expectedExperience
              }
            }
            _ <- monixTimer.sleep(1.minute)
          } yield sleptMinute + 1
        }(_ < minutesToWait)
        _ <- playerRef.isIdleMutex.lockAndUpdate(_ => IO.pure(true))
        _ <- Monad[IO].iterateWhileM(0) { sleptMinute =>
          val expectedExperience = FiniteNonNegativeExperience(originalExp - minutesToWait * 100)

          for {
            _ <- IO {
              eventually {
                playerRef.experienceMutex.readLatest.unsafeRunSync() shouldBe expectedExperience
              }
            }
            _ <- monixTimer.sleep(1.minute)
          } yield sleptMinute + 1
        }(_ < minutesToWait)
        _ <- playerRef.isIdleMutex.lockAndUpdate(_ => IO.pure(false))
        _ <- Monad[IO].iterateWhileM(0) { sleptMinute =>
          val expectedExperience = FiniteNonNegativeExperience(originalExp - (minutesToWait + sleptMinute) * 100)

          for {
            _ <- IO {
              eventually {
                playerRef.experienceMutex.readLatest.unsafeRunSync() shouldBe expectedExperience
              }
            }
            _ <- monixTimer.sleep(1.minute)
          } yield sleptMinute + 1
        }(_ < minutesToWait)

        // cleanup
        _ <- session.finish
      } yield ()

      val programs = runParallel(10)(program)

      val future = programs.unsafeToFuture()

      // FIXME Thread.sleepは各FiberがmonixTimer.sleepに到達するのを待っている。これを除去できるか？
      monixScheduler.tick(30.seconds)
      Thread.sleep(200)
      for (_ <- 0 to minutesToWait * 3) {
        monixScheduler.tick(1.minute)
        Thread.sleep(20)
      }

      Await.result(future, 30.seconds)
    }

    "terminate when player's experience is below per-minute experience consumption" in {
      val originalExp = 10000
      val minutesToWait = 100

      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(
          expConsumptionAmount = 1000
        )

      // 消費は丁度10回でき、11回目の経験値チェックで飛行セッションが閉じるべきなので、
      // 最初の30秒のスリープと合わせて11分30秒の経過を期待する。

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] = playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[IO, PlayerMockReference]()

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          FiniteNonNegativeExperience(originalExp),
          initiallyIdle = false
        ).toIO

        initialTime <- monixTimer.clock.realTime(SECONDS)

        // when
        session <- factory.start[SyncIO](RemainingFlyDuration.Infinity).run(playerRef)
        // セッションが有効になるまで待つ
        _ <- IO {
          eventually {
            session.isActive.unsafeRunSync() shouldBe true
          }
        }
        _ <- monixTimer.sleep(30.seconds)
        _ <- Monad[IO].iterateWhileM(0) { sleptMinute =>
          for {
            _ <- monixTimer.sleep(1.minute)
          } yield sleptMinute + 1
        }(_ < minutesToWait)
        _ <- session.waitForCompletion

        // then
        endTime <- monixTimer.clock.realTime(SECONDS)

        _ <- IO {
          endTime - initialTime shouldBe (11.minutes + 30.seconds).toSeconds
        }
      } yield ()

      val programs = runParallel(10)(program)

      val future = programs.unsafeToFuture()

      // FIXME Thread.sleepは各FiberがmonixTimer.sleepに到達するのを待っている。これを除去できるか？
      monixScheduler.tick(30.seconds)
      Thread.sleep(200)
      for (_ <- 1 to 11) {
        monixScheduler.tick(1.minute)
        Thread.sleep(20)
      }

      Await.result(future, 30.seconds)
    }

    "send appropriate notification when player does not have enough experience" in {
      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(
          expConsumptionAmount = 100
        )

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] = playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[IO, PlayerMockReference]()

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          FiniteNonNegativeExperience(99),
          initiallyIdle = false
        ).toIO

        // when
        session <- factory.start[SyncIO](RemainingFlyDuration.Infinity).run(playerRef)
        _ <- session.waitForCompletion

        // then
        _ <- IO {
          eventually {
            playerRef.messageLog.readLatest.unsafeRunSync() shouldBe Vector(PlayerExpNotEnough)
          }
        }
      } yield ()

      runParallel(10)(program).unsafeRunSync()
    }
  }

  "Finite fly session" should {
    "terminate exactly when the minute specified has passed if the player has enough experience" in {
      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(
          expConsumptionAmount = 100
        )

      val sessionLengthInMinutes = 10
      val originalSessionLength = RemainingFlyDuration.PositiveMinutes.fromPositive(sessionLengthInMinutes)

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] = playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[IO, PlayerMockReference]()

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          InfiniteExperience,
          initiallyIdle = false
        ).toIO

        // when
        session <- factory.start[SyncIO](originalSessionLength).run(playerRef)

        _ <- monixTimer.sleep(sessionLengthInMinutes.minutes + 30.seconds)

        // then
        _ <- IO {
          eventually {
            session.isActive.unsafeRunSync() shouldBe false
          }
        }
      } yield ()

      val programs = runParallel(10)(program)

      val future = programs.unsafeToFuture()

      // FIXME Thread.sleepは各FiberがmonixTimer.sleepに到達するのを待っている。これを除去できるか？
      monixScheduler.tick(30.seconds)
      Thread.sleep(200)
      for (_ <- 1 to 11) {
        monixScheduler.tick(1.minute)
        Thread.sleep(20)
      }

      Await.result(future, 30.seconds)
    }

    "send appropriate notification when a session expires" in {
      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(
          expConsumptionAmount = 100
        )

      val sessionLengthInMinutes = 10
      val originalSessionLength = RemainingFlyDuration.PositiveMinutes.fromPositive(sessionLengthInMinutes)

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] = playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[IO, PlayerMockReference]()

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          InfiniteExperience,
          initiallyIdle = false
        ).toIO

        // when
        _ <- factory.start[SyncIO](originalSessionLength).run(playerRef)

        _ <- monixTimer.sleep(sessionLengthInMinutes.minutes + 30.seconds)

        // then
        _ <- IO {
          eventually {
            playerRef.messageLog.readLatest.unsafeRunSync() shouldBe Vector(FlyDurationExpired)
          }
        }
      } yield ()

      val programs = runParallel(10)(program)

      val future = programs.unsafeToFuture()

      // FIXME Thread.sleepは各FiberがmonixTimer.sleepに到達するのを待っている。これを除去できるか？
      monixScheduler.tick(30.seconds)
      Thread.sleep(200)
      for (_ <- 1 to 11) {
        monixScheduler.tick(1.minute)
        Thread.sleep(20)
      }

      Await.result(future, 30.seconds)
    }
  }
}
