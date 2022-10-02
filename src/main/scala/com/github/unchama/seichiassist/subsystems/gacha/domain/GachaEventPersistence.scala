package com.github.unchama.seichiassist.subsystems.gacha.domain

trait GachaEventPersistence[F[_]] {

  /**
   * @return ガチャイベントを新規登録します
   */
  def registerGachaEvent(gachaEvent: GachaEvent): F[GachaEventOperationResult]

  /**
   * @return ガチャイベントを削除します
   */
  def deleteGachaEvent(gachaEvent: GachaEvent): F[GachaEventOperationResult]

  /**
   * @return ガチャイベントの一覧を取得します
   */
  def gachaEvents: F[Vector[GachaEvent]]

}
