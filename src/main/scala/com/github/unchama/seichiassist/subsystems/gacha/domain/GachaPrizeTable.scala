package com.github.unchama.seichiassist.subsystems.gacha.domain

import cats.effect.Sync

import scala.annotation.tailrec
import scala.util.Random

/**
 * オンメモリの、読み取り専用のガチャ景品リスト。
 */
class GachaPrizeTable[IS](list: List[GachaPrizeTemplate[IS]]) {

  require(
    list.map(_.probability).sum <= 1.0,
    "ガチャ景品リストの確率値合計は1以下である必要があります。"
  )

  import cats.implicits._

  /**
   * ガチャを回す作用。
   */
  def draw[F[_]: Sync]: F[GachaResult[IS]] =
    Sync[F]
      .delay(Random.nextDouble())
      .map(GachaPrizeTable.traverseList(_, list))

}

object GachaPrizeTable {

  /**
   * 景品リストを総舐めして、確率値合計が `toSkip` を超えた最初の景品を結果として定義する。
   * もし確率値合計が `toSkip` を超えなかった場合、外れくじという扱いにする。
   */
  @tailrec
  def traverseList[IS](toSkip: Double, remainingList: List[GachaPrizeTemplate[IS]]): GachaResult[IS] =
    remainingList match {
      case ::(head, next) =>
        if (toSkip < head.probability) GachaResult.Won(head)
        else traverseList(toSkip - head.probability, next)
      case Nil => GachaResult.Blank
    }

}
