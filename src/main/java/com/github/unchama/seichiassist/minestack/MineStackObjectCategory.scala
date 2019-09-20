package com.github.unchama.seichiassist.minestack

/**
 * マインスタックに収納するアイテムのカテゴリを表すオブジェクトの列挙.
 *
 * @param serializedValue この列挙体を永続化する際の識別子となる整数.
 * @param uiLabel UI上で表示する際のカテゴリの名前
 */
enum class MineStackObjectCategory(val serializedValue: Int, val uiLabel: String) {
  ORES(0, "鉱石系アイテム"),
  MOB_DROP(1, "ドロップ系アイテム"),
  AGRICULTURAL(2, "農業・食料系アイテム"),
  BUILDING(3, "建築系アイテム"),
  REDSTONE_AND_TRANSPORTATION(4, "レッドストーン・移動系アイテム"),
  GACHA_PRIZES(5, "ガチャ品");
}

object MineStackObjectCategory {
  def fromSerializedValue(value: Int): MineStackObjectCategory? = values().find { it.serializedValue == value }

  def MineStackObj.category(): MineStackObjectCategory = this.stackType
}
