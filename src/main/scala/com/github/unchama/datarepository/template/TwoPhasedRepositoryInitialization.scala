package com.github.unchama.datarepository.template

import java.util.UUID

/**
 * データレポジトリの初期化処理を記述するオブジェクト。
 *
 * マインクラフトのサーバーの仕様として、プレーヤーがサーバーに実際に参加する前に
 * プレーヤーのUUID/名前ペアを受け取れることになっている。
 *
 * このオブジェクトが記述するのは、そのような状況下において
 *  - ログイン処理後にUUID/名前を受け取り、中間データを作成する
 *  - 実際に [[Player]] のインスタンスが得られ次第 [[R]] を生成する
 *
 * ようなデータリポジトリの処理である。
 */
trait TwoPhasedRepositoryInitialization[F[_], Player, R] {

  type IntermediateData

  /**
   * 参加したプレーヤーのUUID/名前から[[IntermediateData]]を生成する作用。
   *
   * [[IntermediateData]] が何らかの理由により生成できなかった場合、[[PrefetchResult.Failed]]を返す可能性がある。
   */
  val prefetchIntermediateValue: (UUID, String) => F[PrefetchResult[IntermediateData]]

  val prepareData: (Player, IntermediateData) => F[R]

}
