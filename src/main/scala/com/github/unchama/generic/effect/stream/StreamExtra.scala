package com.github.unchama.generic.effect.stream

import cats.Eq
import cats.effect.Concurrent
import cats.effect.concurrent.Ref
import com.github.unchama.generic.Diff
import fs2.{Pull, Stream}

object StreamExtra {

  import cats.implicits._

  /**
   * 与えられた [[Stream]] の最初の要素とその後続の [[Stream]] を高々一度だけ流す [[Stream]] を作成する。
   * 与えられた [[Stream]] が空だった場合、空の [[Stream]] が返る。
   */
  def uncons[F[_], O](stream: Stream[F, O]): Stream[F, (O, Stream[F, O])] =
    stream.pull.uncons1.flatMap {
      case Some((head, tail)) => Pull.output1(head, tail)
      case None => Pull.done
    }.stream

  /**
   * 与えられた `stream` に対してfoldの操作を行う。
   *
   * 結果として返されるストリームは、
   *  - 内部状態 [[B]] を持つ。 `initial` から開始し、 `stream` から値を受け取る度に `f` で内部状態を更新する。
   *  - 離散的なストリームである `flushSignal` が出力するのと同じタイミングで内部状態を出力し、
   *    内部状態を `initial` へと戻す
   */
  def foldGate[F[_] : Concurrent, A, B, U](stream: Stream[F, A],
                                           flushSignal: Stream[F, U],
                                           initial: B)(f: B => A => B): Stream[F, B] = {
    Stream.force {
      Ref[F].of(initial).map { ref =>
        flushSignal
          .evalMap(_ => ref.getAndSet(initial))
          .concurrently(stream.evalTap(a => ref.update(f(_)(a))))
      }
    }
  }

  /**
   * 与えられたキーと出力のストリームから、与えられたキーを左成分に持つ要素のみを取り出すストリームを作成する。
   */
  def filterKeys[F[_], K, O](stream: Stream[F, (K, O)], key: K): Stream[F, O] =
    stream.mapFilter { case (k, out) => Option.when(k == key)(out) }

  def keyedValueDiffs[F[_], K, O: Eq](stream: Stream[F, (K, O)]): Stream[F, (K, Diff[O])] = {
    stream
      .mapAccumulate(Map.empty[K, O]) { case (map, (key, o)) =>
        val output: Option[(K, Diff[O])] = map.get(key) match {
          case Some(previousValue) =>
            Diff.fromValues(previousValue, o).map(key -> _)
          case None =>
            None
        }

        (map.updated(key, o), output)
      }
      .mapFilter(_._2)
  }
}
