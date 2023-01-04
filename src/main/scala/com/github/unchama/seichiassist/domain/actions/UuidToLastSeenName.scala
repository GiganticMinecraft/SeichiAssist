package com.github.unchama.seichiassist.domain.actions

import java.util.UUID

trait UuidToLastSeenName[F[_]] {

  /**
   * サーバーに参加したことがあるプレイヤーのデータが格納されているストレージを全探索し、 UUIDとそれに対応したプレイヤーの名前を列挙してMapとして返す。
   *
   * この時のプレイヤーの名前は、そのプレイヤーの名前の集合の内、 最も新しく、かつその名前がサーバーにとって既知であるという性質を持つ。
   * @return
   *   プレイヤーのUUIDとそれに対応する名前が組になったMapを計算する作用
   */
  def entries: F[Map[UUID, String]]

}

object UuidToLastSeenName {

  def apply[F[_]: UuidToLastSeenName]: UuidToLastSeenName[F] = implicitly[UuidToLastSeenName[F]]

}
