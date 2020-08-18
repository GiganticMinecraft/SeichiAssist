package com.github.unchama.generic.effect.unsafe

import cats.effect.Effect

/**
 * [[Effect]]を実行する環境
 */
trait EffectEnvironment {

  /**
   * `program` を `context` の文脈にてunsafeに実行する。
   *
   * このメソッドは一般に副作用をもたらすためunsafeである。
   * 理想的には、これはプログラムの最も外側にて「一度だけ」呼び出されるべきである。
   *
   * このメソッドの実装は `context` を用いて実行に関するロギングを行ってよい。
   */
  def runEffectAsync[U, F[_] : Effect](context: String, program: F[U]): Unit

}
