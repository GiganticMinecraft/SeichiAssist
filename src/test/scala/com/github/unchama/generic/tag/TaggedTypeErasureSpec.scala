package com.github.unchama.generic.tag

import com.github.unchama.generic.tag.tag.@@
import org.scalatest.wordspec.AnyWordSpec

import java.util.concurrent.{CountDownLatch, TimeUnit}
import scala.concurrent.ExecutionContext

/**
 * タグ付き型の実行時表現の回帰テスト。
 *
 * Scala 2時代の交差型エンコーディング（`T with Tagged[U]` + asInstanceOf）は、
 * Scala 3では交差型の消去規則の違いによりフィールドのJVM型が `Tagged` 側へ消去され、
 * objectの初期化子でタグ付き値をフィールドへ格納した時点で ClassCastException が
 * 発生した（PluginExecutionContexts の初期化失敗によるプラグインロード不能）。
 * このテストは、opaque type によるエンコーディングでその問題が再発しないことを固定する。
 */
class TaggedTypeErasureSpec extends AnyWordSpec {

  trait Marker

  private object Holder {
    // 交差型エンコーディングではこのフィールドへの格納が実行時に失敗していた
    val taggedContext: ExecutionContext @@ Marker =
      tag.apply[Marker][ExecutionContext](ExecutionContext.global)
  }

  "タグ付き型" should {
    "objectの初期化子でフィールドへ格納しても実行時例外にならない" in {
      assert(Holder.taggedContext ne null)
    }

    "タグを付与したまま元の型のメソッドをそのまま利用できる" in {
      val latch = new CountDownLatch(1)

      Holder.taggedContext.execute(() => latch.countDown())

      assert(latch.await(5, TimeUnit.SECONDS))
    }
  }
}
