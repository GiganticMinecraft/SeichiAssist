package com.github.unchama.seichiassist

import cats.effect.IO
import com.github.unchama.targetedeffect.TargetedEffect

package object unsafe {
  def runIOAsync(context: String, program: IO[Any]): Unit = {
    program.unsafeRunAsync {
      case Left(error) =>
        println(s"${context}最中にエラーが発生しました。")
        error.printStackTrace()
      case Right(_) =>
    }
  }

  /**
   * `receiver`を`effect`に適用して得られる`IO`を例外を補足して非同期に実行する。
   * 何らかの例外が`IO`から投げられた場合、`context`の情報を含むエラーメッセージを出力する。
   *
   * @param context effectが何をすることを期待しているかの記述
   * @tparam T レシーバの型
   */
  def runAsyncTargetedEffect[T](receiver: T)(effect: TargetedEffect[T], context: String): Unit =
    runIOAsync(context, effect(receiver))
}
