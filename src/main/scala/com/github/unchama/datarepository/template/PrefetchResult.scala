package com.github.unchama.datarepository.template

/**
 * データリポジトリの中間データの生成結果。
 *  - [[R]] の値が入っている
 *  - 読み込みが失敗して(プレーヤーに表示するための)メッセージが入っている
 *
 * のどちらかである。
 */
sealed trait PrefetchResult[+R]

object PrefetchResult {

  case class Failed(kickMessage: Option[String]) extends PrefetchResult[Nothing]

  case class Success[+R](data: R) extends PrefetchResult[R]

}
