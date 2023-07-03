package com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaevent

trait GachaEventPersistence[F[_]] {

  /**
   * @return ガチャイベントを作成する作用
   */
  def createGachaEvent(gachaEvent: GachaEvent): F[Unit]

  /**
   * @return ガチャイベントを削除する作用
   */
  def deleteGachaEvent(eventName: GachaEventName): F[Unit]

  /**
   * @return ガチャイベントの一覧を取得する作用
   */
  def gachaEvents: F[Vector[GachaEvent]]

}
