package com.github.unchama.testutil.concurrent

trait Blocker[F[_]] {
  /**
   * ブロッカーが完了されるまで、ブロッキングを待ち続けるアクションを返す。
   */
  def await(): F[Unit]
}
