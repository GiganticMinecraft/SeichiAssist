package com.github.unchama.seichiassist.subsystems.minestack.domain

trait PlayerSettingPersistence[F[_]] {

  /**
   * @return 指定した`uuid`のプレイヤーの自動アイテム収集設定を取得する作用
   */
  def autoMineStackState: F[Boolean]

  /**
   * @return 指定した`uuid`のプレイヤーの自動アイテム収集を有効に切り替える作用
   */
  def turnOnAutoMineStack: F[Unit]

  /**
   * @return 指定した`uuid`のプレイヤーの自動アイテム収集を無効に切り替える作用
   */
  def turnOffAutoMineStack: F[Unit]

}
