package com.github.unchama.generic.effect.unsafe

import com.github.unchama.targetedeffect.TargetedEffectF

/**
 * `F`をインピュアなコールバックから実行する環境。
 *
 * この環境の寿命は、内部で使用する`Dispatcher[F]`の寿命より長くてはならない。
 */
trait EffectEnvironment[F[_]] {

  /**
   * `program` を `context` の文脈にてunsafeに実行する。
   *
   * このメソッドは一般に副作用をもたらすためunsafeである。 理想的には、これはプログラムの最も外側にて「一度だけ」呼び出されるべきである。
   *
   * このメソッドの実装は `context` を用いて実行に関するロギングを行ってよい。
   */
  def unsafeRunEffectAsync[U](context: String, program: F[U]): Unit

  /**
   * `receiver`を`effect`に適用して得られる`IO`を例外を補足して実行する。
   * 何らかの例外が`IO`から投げられた場合、`context`の情報を含むエラーメッセージを出力する。
   *
   * このメソッドは、呼び出し箇所での実行を`effect`から得られる`IO`が別コンテキストに移るまでブロックする。
   *
   * @param context
   *   effectが何をすることを期待しているかの記述
   * @tparam T
   *   レシーバの型
   * @deprecated
   *   use [[EffectEnvironment]]
   */
  def unsafeRunAsyncTargetedEffect[T](
    receiver: T
  )(effect: TargetedEffectF[F, T], context: String): Unit =
    unsafeRunEffectAsync(context, effect(receiver))

}
