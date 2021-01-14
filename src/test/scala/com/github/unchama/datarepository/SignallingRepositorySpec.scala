package com.github.unchama.datarepository

import cats.effect.concurrent.Ref
import cats.effect.{Sync, SyncIO, Timer}
import com.github.unchama.testutil.concurrent.tests.ConcurrentEffectTest
import com.github.unchama.testutil.execution.MonixTestSchedulerTests
import monix.eval.Task
import monix.execution.ExecutionModel
import monix.execution.schedulers.TestScheduler
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.collection.concurrent.TrieMap

class SignallingRepositorySpec
  extends AnyWordSpec
    with ScalaCheckPropertyChecks
    with Matchers
    with ConcurrentEffectTest
    with MonixTestSchedulerTests {

  import scala.concurrent.duration._

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(timeout = 5.seconds, interval = 10.millis)
  implicit val monixScheduler: TestScheduler = TestScheduler(ExecutionModel.AlwaysAsyncExecution)
  implicit val monixTimer: Timer[Task] = Task.timer(monixScheduler)

  type Key = Long
  type Value = Int

  final class TestRepository[
    K, F[_] : Sync, Ref[_], V
  ](override protected val map: TrieMap[K, Ref[V]],
    override protected val initializeRef: (K, V) => F[Ref[V]]) extends KeyedWrappedValueRepository[K, F, Ref, V] {
    def add(k: K, v: V): F[Unit] = super.addPair(k, v)
  }

  val newSignallingRepository: SyncIO[
    (TestRepository[Key, SyncIO, Ref[SyncIO, *], Value], fs2.Stream[Task, (Key, Value)])
  ] = SignallingRepository[Task] {
    new RefRepositoryFactory[SyncIO, Key, Value, TestRepository[Key, SyncIO, *[_], *]] {
      override def instantiate[Ref[_]](refCreator: (Key, Value) => SyncIO[Ref[Value]]): SyncIO[TestRepository[Key, SyncIO, Ref, Value]] = {
        SyncIO {
          new TestRepository(TrieMap.empty[Key, Ref[Value]], refCreator)
        }
      }
    }
  }

  "KeyedWrappedValueRepository when made with SignallingRepository" should {
    import cats.implicits._

    "signal all the updates" in {
      val initialValue: Value = 0

      forAll(minSuccessful(100)) { updates: List[(Key, Value)] =>
        val updateMap: Map[Key, List[Value]] = updates.groupMap(_._1)(_._2)
        val keys: Set[Key] = updateMap.keySet
        val valueMap: Map[Key, List[Value]] = updateMap.view.mapValues(initialValue +: _).toMap

        val task = for {
          signallingRepository <- Task.liftFrom[SyncIO].apply(newSignallingRepository)
          (repository, valueStream) = signallingRepository
          // リポジトリを初期化する
          _ <- Task.liftFrom[SyncIO].apply(keys.toList.traverse(repository.add(_, initialValue)))
          getAllUpdatesFiber <- valueStream.take(keys.size + updates.size).compile.toList.start
          _ <- monixTimer.sleep(1.second)
          _ <- updateMap.toList.parTraverse { case (key, updates) =>
            Task.liftFrom[SyncIO].apply {
              updates.traverse(value => repository(key).set(value))
            }
          }
          values <- getAllUpdatesFiber.join
        } yield {
          values.groupMap(_._1)(_._2)
        }

        assertResult(valueMap)(awaitForProgram(task, 1.second))
      }
    }
  }
}
