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
  var lineFillStatus: LineFillStatusFlag with Product = LineFillStatusFlag.Disabled

  var lineFillSlabPosition: LineFillSlabPosition with Product = LineFillSlabPosition.Lower

  var lineFillDestructWeakBlocks = false

  var lineFillPrioritizeMineStack = false

  /**
   * ブロック範囲設置スキル設定フラグ
   */
  var rectFillPrioritizeMineStack = false
}
