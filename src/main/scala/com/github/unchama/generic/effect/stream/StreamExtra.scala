package com.github.unchama.generic.effect.stream

import fs2.{Pull, Stream}

object StreamExtra {

  /**
   * 与えられた [[Stream]] の最初の要素とその後続の [[Stream]] を高々一度だけ流す [[Stream]] を作成する。
   * 与えられた [[Stream]] が空だった場合、空の [[Stream]] が返る。
   */
  def uncons[F[_], O](stream: Stream[F, O]): Stream[F, (O, Stream[F, O])] =
    stream.pull.uncons1.flatMap {
      case Some((head, tail)) => Pull.output1(head, tail)
      case None => Pull.done
    }.stream
}
