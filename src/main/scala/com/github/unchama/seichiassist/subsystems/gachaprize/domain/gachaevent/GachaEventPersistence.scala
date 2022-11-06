package com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaevent

trait GachaEventPersistence[F[_]] {

  /**
   * @return ガチャイベントを作成する作用
   */
  def createGachaEvent(gachaEvent: GachaEvent): F[Unit]

  /**
   * @return ガチャイベントを削除します
   */
  def deleteGachaEvent(eventName: GachaEventName): F[Unit]

  /**
   * @return ガチャイベントの一覧を取得します
   */
  def gachaEvents: F[Vector[GachaEvent]]

}
