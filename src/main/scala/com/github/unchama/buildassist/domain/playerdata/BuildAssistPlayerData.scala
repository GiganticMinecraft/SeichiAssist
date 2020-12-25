package com.github.unchama.buildassist.domain.playerdata

import com.github.unchama.buildassist.domain.explevel.{BuildAssistExpTable, BuildExpAmount, BuildLevel}

/**
 * BuildAssistが管理するプレーヤーのデータ。
 *
 * 建築レベル [[desyncedLevel]] は建築量 [[expAmount]] と同期関係に無い。
 * これはレベルアップをプレーヤーに通知したいという要求によるものである。
 * 例えばプレーヤーが退出した際、まだ加算されていない建築量を永続化の直前に [[expAmount]] に加算する。
 * この時に建築レベルを更新してしまうと、レベルアップにプレーヤーが気付くことが無いが、
 * 更新されていない建築レベルを保持しておくことで次回参加時にレベル変化の通知を行うことができる。
 */
case class BuildAssistPlayerData(expAmount: BuildExpAmount,
                                 desyncedLevel: BuildLevel) {

  /**
   * このデータの経験値を基に建築レベルが同期されたデータを計算する。
   */
  def withSyncedLevel: BuildAssistPlayerData =
    this.copy(desyncedLevel = BuildAssistExpTable.levelAt(expAmount))

}
