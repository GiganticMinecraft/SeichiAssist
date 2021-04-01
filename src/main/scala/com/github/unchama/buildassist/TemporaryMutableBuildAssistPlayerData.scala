package com.github.unchama.buildassist

class TemporaryMutableBuildAssistPlayerData {
  var rectFillEnabled = false
  var rectFillIncludeUnderCaves = false

  // TODO: こいつは殺す
  var rectFillRangeStep = 2

  /**
   * 直列設置設定フラグ
   */
  var line_up_flg = 0
  var line_up_step_flg = 0
  var line_up_des_flg = 0
  var line_up_minestack_flg = 0

  /**
   * ブロック範囲設置スキル設定フラグ
   */
  var rectFillPreferMineStack = false
}
