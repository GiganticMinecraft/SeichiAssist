package com.github.unchama.buildassist

class TemporaryMutableBuildAssistPlayerData {
  var ZoneSetSkillFlag = false
  var zsSkillDirtFlag = false

  // TODO: こいつは殺す
  var AREAint = 2

  /**
   * ブロックを並べるスキル設定フラグ
   */
  var lineFillAlign: VerticalAlign = VerticalAlign.Off
  var lineFillStepMode: StepPlaceMode = StepPlaceMode.Upper
  var lineFillBreakWeakBlocks = false
  var lineFillWithMinestack = false

  /**
   * ブロック範囲設置スキル設定フラグ
   */
  var zs_minestack_flag = false
}
