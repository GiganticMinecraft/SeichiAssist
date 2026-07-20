package com.github.unchama.generic

import cats.effect.{IO, SyncIO}
import org.scalatest.wordspec.AnyWordSpec

/**
 * [[ContextCoercion]] の implicit 解決の回帰テスト。
 *
 * `ContextCoercion[SyncIO, IO]` には、専用インスタンス
 * [[ContextCoercion.catsEffectSyncIOToIOCoercion]] と汎用インスタンス
 * [[ContextCoercion.syncEffectToSync]] の両方が適用可能である。
 * Scala 2 では特殊化された val が優先されるが、Scala 3 では implicit の
 * 優先順位規則が変わるため、移行後も同じインスタンスが選択されることを
 * このテストで検証する（SeichiAssist の DI 配線は G=SyncIO, F=IO で
 * この解決に依存している）。
 */
class ContextCoercionResolutionSpec extends AnyWordSpec {

  "ContextCoercion[SyncIO, IO]の暗黙解決" should {
    "汎用のsyncEffectToSyncではなく専用のcatsEffectSyncIOToIOCoercionを選択する" in {
      val resolved = implicitly[ContextCoercion[SyncIO, IO]]

      assert(resolved eq ContextCoercion.catsEffectSyncIOToIOCoercion)
    }

    "SyncIOの計算を実行せずにIOへ持ち上げ、IOの実行時に副作用が起こる" in {
      var executed = false
      val syncIO = SyncIO { executed = true }

      val io = ContextCoercion[SyncIO, IO, Unit](syncIO)

      assert(!executed)
      io.unsafeRunSync()
      assert(executed)
    }

    "値を保ったままIOへ変換する" in {
      assert(ContextCoercion[SyncIO, IO, Int](SyncIO.pure(42)).unsafeRunSync() == 42)
    }
  }

  "ContextCoercion[F, F]の暗黙解決" should {
    "identityCoercionが解決され、計算を変更しない" in {
      val io = IO.pure(42)

      assert(implicitly[ContextCoercion[IO, IO]].apply(io).unsafeRunSync() == 42)
    }
  }

  "CoercibleComputationのcoerceTo構文" should {
    "SyncIOの計算をIOへ変換できる" in {
      import ContextCoercion._

      val syncIO = SyncIO.pure("value")

      assert(syncIO.coerceTo[IO].unsafeRunSync() == "value")
    }
  }
}
