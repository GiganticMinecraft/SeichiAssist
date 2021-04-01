package com.github.unchama.buildassist

import com.github.unchama.buildassist.enums.{LineFillSlabPosition, LineFillStatusFlag}

class TemporaryMutableBuildAssistPlayerData {
  var rectFillEnabled = false
  var rectFillIncludeUnderCaves = false

  // TODO: こいつは殺す
  var rectFillRangeStep = 2

  /**
   * 直列設置設定フラグ
   */
  @deprecated("unsafe enums backed by int.")
  var line_up_flg = 0
  var lineFillStatus = LineFillStatusFlag.Disabled
  @deprecated("unsafe enums backed by int.")
  var line_up_step_flg = 0
  var lineFillSlabPosition = LineFillSlabPosition.Lower
  @deprecated("unsafe enums backed by int. Consider migrate this to Boolean.")
  var line_up_des_flg = 0
  var lineFillDestructWeakBlocks = false
  @deprecated("unsafe enums backed by int. Consider migrate this to Boolean.")
  var line_up_minestack_flg = 0
  var lineFillPrioritizeMineStack = false

  /**
   * ブロック範囲設置スキル設定フラグ
   */
  var rectFillPreferMineStack = false
}
