package com.github.unchama.seichiassist.concurrent

import cats.effect.IO
import cats.effect.unsafe.IORuntimeBuilder
import org.scalatest.wordspec.AnyWordSpec

/**
 * [[PluginExecutionContexts]] の確保に関する回帰テスト。
 *
 * コンパニオンオブジェクトが公開する `implicit def`（`timer` など）は、確保済みインスタンスへ
 * 委譲する実装になっている。これらがコンパニオンオブジェクト内の構築処理から
 * ローカルスコープのimplicitとして解決されてしまうと、`Resource` を組み立てた時点で
 * 確保前の状態を参照し `IllegalStateException` を投げる。
 *
 * 実際にこれはプラグインのロード自体を失敗させていたため、ここで固定する。
 */
class PluginExecutionContextsSpec extends AnyWordSpec {

  // 構築処理はプラグインを参照しないため、ここではnullを渡して問題ない
  private val noPlugin: org.bukkit.plugin.java.JavaPlugin = null

  "PluginExecutionContexts.resource" should {

    "確保前であってもResourceの組み立て自体は例外を投げない" in {
      val runtime = IORuntimeBuilder().build()

      try {
        assert(PluginExecutionContexts.resource(noPlugin, runtime) ne null)
      } finally {
        runtime.shutdown()
      }
    }

    "確保している間だけ実行コンテキストを公開する" in {
      val runtime = IORuntimeBuilder().build()

      try {
        assertThrows[IllegalStateException](PluginExecutionContexts.timer)

        PluginExecutionContexts
          .resource(noPlugin, runtime)
          .use { contexts =>
            IO.delay {
              assert(PluginExecutionContexts.ioRuntime eq runtime)
              assert(PluginExecutionContexts.timer eq contexts.timer)
              assert(PluginExecutionContexts.clock eq contexts.clock)
              assert(PluginExecutionContexts.dispatcher eq contexts.dispatcher)
              assert(PluginExecutionContexts.cachedThreadPool eq contexts.cachedThreadPool)
              assert(!contexts.cachedThreadPool.isShutdown)
            }
          }
          .unsafeRunSync()(runtime)

        assertThrows[IllegalStateException](PluginExecutionContexts.timer)
      } finally {
        runtime.shutdown()
      }
    }

    "解放時にスレッドプールを停止する" in {
      val runtime = IORuntimeBuilder().build()

      try {
        val threadPool = PluginExecutionContexts
          .resource(noPlugin, runtime)
          .use(contexts => IO.pure(contexts.cachedThreadPool))
          .unsafeRunSync()(runtime)

        assert(threadPool.isShutdown)
      } finally {
        runtime.shutdown()
      }
    }

  }

}
