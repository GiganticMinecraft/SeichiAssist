package com.github.unchama.seichiassist.subsystems.gacha.domain

import cats.Functor

/**
 * 永続化されたガチャテーブルに対する操作の抽象。
 */
trait PersistedGachaPrizeTable[F[_], IS] {

  /**
   * テーブル全体の構造を [[Map]] として読みだす作用。
   *
   * UIレイヤなどは、ここで渡された [[GachaPrizeId]] を信頼して [[delete]] や [[set]] 等を行ってよい。
   * ただし、永続化されたテーブルは複数のプロセスから参照されるため、
   * 返ってきた [[GachaPrizeId]] に対応する [[GachaPrizeTemplate]] が存在し続ける保証はない。
   * 存在していなかった場合の挙動は [[delete]] 等のメソッドが各々定める。
   */
  val read: F[Map[GachaPrizeId, GachaPrizeTemplate[IS]]]

  /**
   * 現在の永続化されたガチャテーブルを読みだし、 [[GachaPrizeTable]] として戻す作用。
   */
  def readAsPrizeTable(implicit F: Functor[F]): F[GachaPrizeTable[IS]] =
    F.fmap(read)(map => new GachaPrizeTable(map.values.toList))

  /**
   * テーブルから `id` によって指定されるガチャ景品を削除する作用。
   */
  def delete(id: GachaPrizeId): F[TableModificationResult.OnDelete]

  /**
   * テーブルが `id` に対応して持っているガチャ景品を `gachaPrizeTemplate` により置き換える作用。
   */
  def set(id: GachaPrizeId, gachaPrizeTemplate: GachaPrizeTemplate[IS]): F[TableModificationResult]

  /**
   * テーブルへガチャ景品を登録する作用。
   */
  def register(template: GachaPrizeTemplate[IS]): F[TableModificationResult.OnRegister]

}
