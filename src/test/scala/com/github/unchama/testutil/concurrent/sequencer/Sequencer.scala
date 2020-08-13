package com.github.unchama.testutil.concurrent.sequencer

import com.github.unchama.testutil.concurrent.Blocker

trait Sequencer[F[_]] {

  /**
   * 一つ前のブロッカーのawaitが完了するまでブロックを行うブロッカーのリストを返す計算。
   * 返されるリストの最初のブロッカーは直ちに完了する。
   */
  val newBlockerList: F[LazyList[Blocker[F]]]

}
