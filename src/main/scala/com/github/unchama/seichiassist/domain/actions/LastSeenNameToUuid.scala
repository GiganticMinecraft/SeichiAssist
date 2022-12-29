package com.github.unchama.seichiassist.domain.actions

import java.util.UUID

trait LastSeenNameToUuid[F[_], Error] {

  /**
   * サーバーに参加したことがあるプレイヤーのデータが格納されているストレージを全探索し、`playerName`に一致する名前のプレイヤーを捜索する。
   *
   * この時のプレイヤーの名前は、そのプレイヤーの名前の変更履歴の内、最も新しく、かつその名前がサーバーにとって既知であるという性質を持つ。
   *
   * @return `playerName`に紐づく[[UUID]]または取得する際に発生したエラー
   */
  def of(playerName: String): F[Either[Error, UUID]]

}
