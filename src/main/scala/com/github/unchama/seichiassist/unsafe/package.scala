package com.github.unchama.seichiassist

import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.targetedeffect.TargetedEffect

package object unsafe {


  /**
   * `receiver`を`effect`に適用して得られる`IO`を例外を補足して実行する。
   * 何らかの例外が`IO`から投げられた場合、`context`の情報を含むエラーメッセージを出力する。
   *
   * このメソッドは、呼び出し箇所での実行を`effect`から得られる`IO`が別コンテキストに移るまでブロックする。
   *
   * @param context effectが何をすることを期待しているかの記述
   * @tparam T レシーバの型
   * @deprecated use [[EffectEnvironment]]
   */
  @deprecated def runAsyncTargetedEffect[T](receiver: T)(effect: TargetedEffect[T], context: String): Unit = {
    effect(receiver).unsafeRunAsync {
      case Left(error) =>
        println(s"${context}最中にエラーが発生しました。")
        error.printStackTrace()
      case Right(_) =>
    }
  }
}
