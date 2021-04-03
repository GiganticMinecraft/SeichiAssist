package com.github.unchama.seichiassist.subsystems.gacha.domain

/**
 * ガチャ景品テーブルを操作したときの操作結果値。
 */
sealed trait TableModificationResult

object TableModificationResult {

  /**
   * ガチャ景品登録処理での結果値
   */
  sealed trait OnRegister
  sealed trait OnDelete

  case object NotFound extends TableModificationResult with OnDelete
  case object Success extends TableModificationResult with OnRegister with OnDelete

  /**
   * ガチャ景品テーブルの合計確率値が1.0を超過した際返される結果値。
   */
  case object Rejected extends TableModificationResult with OnRegister
}

