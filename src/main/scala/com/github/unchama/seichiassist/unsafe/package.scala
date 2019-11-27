package com.github.unchama.seichiassist

import cats.effect.{ContextShift, IO}
import com.github.unchama.targetedeffect.TargetedEffect

package object unsafe {

  /**
   * `cs`に実行をシフトしてから、シフト後のコンテキストで`program`を実行する。
   *
   * このメソッドは呼び出し箇所での実行を「ブロックしない」。
   * メソッドを呼び出すときに掛かるブロック時間は、純粋にコンテキストを切り替えるコストのみになる。
   *
   * @param context エラーログ出力に用いられる実行内容の説明文
   */
  def fireShiftAndRunAsync(context: String, program: IO[Any])
                          (implicit cs: ContextShift[IO]): Unit = {
    runIOAsync(context, cs.shift.flatMap(_ => program))
  }

  /**
   * unsafeRunAsyncメソッドに例外ロギングのコールバックを渡すようなラッパメソッド。
   */
  def runIOAsync(context: String, program: IO[Any]): Unit = {
    program.unsafeRunAsync {
      case Left(error) =>
        println(s"${context}最中にエラーが発生しました。")
        error.printStackTrace()
      case Right(_) =>
    }
  }

  /**
   * `receiver`を`effect`に適用して得られる`IO`を例外を補足して実行する。
   * 何らかの例外が`IO`から投げられた場合、`context`の情報を含むエラーメッセージを出力する。
   *
   * このメソッドは、呼び出し箇所での実行を`effect`から得られる`IO`が別コンテキストに移るまでブロックする。
   *
   * @param context effectが何をすることを期待しているかの記述
   * @tparam T レシーバの型
   */
  def runAsyncTargetedEffect[T](receiver: T)(effect: TargetedEffect[T], context: String): Unit =
    runIOAsync(context, effect(receiver))
}
