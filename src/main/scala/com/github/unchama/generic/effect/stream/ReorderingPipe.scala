package com.github.unchama.generic.effect.stream

import com.github.unchama.generic.Token
import fs2.{Chunk, Pipe}

import scala.annotation.tailrec

object ReorderingPipe {

  /**
   * シーケンスされたタイムスタンプ付きの値。
   *
   * @param currentStamp
   *   この値のタイムスタンプ
   * @param nextStamp
   *   次の値のタイムスタンプ
   */
  case class TimeStamped[+A](currentStamp: Token, nextStamp: Token, value: A)

  /**
   * ReorderingPipeの内部状態。
   *
   * @param nextToken
   *   次の値のタイムスタンプ
   * @param waitMap
   *   [[TimeStamped.currentStamp]]をキーに、パイプにすでに到着した値とその次のタイムスタンプを保持する [[Map]]
   */
  private case class WaitMap[A](nextToken: Token, waitMap: Map[Token, (A, Token)]) {
    def flushWith(nextChunk: Chunk[TimeStamped[A]]): (WaitMap[A], Chunk[A]) = {

      @tailrec def go(
        map: Map[Token, (A, Token)],
        next: Token,
        accum: Vector[A]
      ): (WaitMap[A], Chunk[A]) = {
        map.get(next) match {
          case Some((a, newNext)) => go(map.removed(next), newNext, accum.appended(a))
          case None               => (WaitMap(next, map), Chunk.vector(accum))
        }
      }

      val combinedMap: Map[Token, (A, Token)] = Map.from {
        waitMap.toVector.appendedAll {
          nextChunk.toVector.map {
            case TimeStamped(stamp, nextStamp, value) => stamp -> (value, nextStamp)
          }
        }
      }

      go(combinedMap, nextToken, Vector.empty)
    }
  }

  /**
   * シーケンスされたタイムスタンプ付きの値を流す [[Stream]] を並び替える [[Pipe]]。
   *
   * `token` よりもタイムスタンプが古い要素は返されるストリームに出力されない。
   *
   * @param token
   *   出力 [[Stream]] の最初の要素となることを期待される入力が持つタイムスタンプ
   */
  def withInitialToken[F[_], A](token: Token): Pipe[F, TimeStamped[A], A] =
    _.scanChunks(WaitMap[A](token, Map.empty)) {
      case (waitMap, nextChunk) =>
        waitMap.flushWith(nextChunk)
    }
}
