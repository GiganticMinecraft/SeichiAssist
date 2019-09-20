package com.github.unchama.menuinventory

/**
 * メニュー一つに対応するオブジェクトへの抽象インターフェース
 */
trait Menu {

  /**
   * オブジェクトが表すメニューを[Player]に開かせる[TargetedEffect].
   */
  val open: TargetedEffect<Player>

}