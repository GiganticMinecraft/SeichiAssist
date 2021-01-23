package com.github.unchama.datarepository.template

import java.util.UUID

/**
 * データレポジトリの初期化処理を記述するオブジェクト。
 *
 * マインクラフトのサーバーの仕様として、プレーヤーがサーバーに実際に参加する前に
 * プレーヤーのUUID/名前ペアを受け取れることになっている。
 *
 * このオブジェクトが記述するのは、そのような状況下において
 *  - ログイン処理後にUUID/名前を受け取り次第 [[R]] を生成する
 *
 * ようなデータリポジトリの処理である。
 */
trait SinglePhasedRepositoryInitialization[F[_], R] {

  /**
   * 参加したプレーヤーのUUID/名前から[[R]]を生成する作用。
   *
   * [[R]] が何らかの理由により生成できなかった場合、[[PrefetchResult.Failed]]を返す可能性がある。
   */
  def prepareData(uuid: UUID, name: String): F[PrefetchResult[R]]

}
