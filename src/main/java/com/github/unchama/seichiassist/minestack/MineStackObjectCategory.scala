package com.github.unchama.seichiassist.minestack

import enumeratum._

/**
 * マインスタックに収納するアイテムのカテゴリを表すオブジェクト.
 *
 * @param serializedValue この列挙体を永続化する際の識別子となる整数.
 * @param uiLabel UI上で表示する際のカテゴリの名前
 */
case class MineStackObjectCategory(serializedValue: Int, uiLabel: String) extends EnumEntry

case object MineStackObjectCategory extends Enum[MineStackObjectCategory] {
  case object ORES extends MineStackObjectCategory(0, "鉱石系アイテム")
  case object MOB_DROP extends MineStackObjectCategory(1, "ドロップ系アイテム")
  case object AGRICULTURAL extends MineStackObjectCategory(2, "農業・食料系アイテム")
  case object BUILDING extends MineStackObjectCategory(3, "建築系アイテム")
  case object REDSTONE_AND_TRANSPORTATION extends MineStackObjectCategory(4, "レッドストーン・移動系アイテム")
  case object GACHA_PRIZES extends MineStackObjectCategory(5, "ガチャ品")

  val values: IndexedSeq[MineStackObjectCategory] = findValues

  def fromSerializedValue(value: Int): Option[MineStackObjectCategory] = values.find(_.serializedValue == value)

  implicit class MineStackObjOps(val mineStackObj: MineStackObj) extends AnyVal {
    def category(): MineStackObjectCategory = mineStackObj.stackType
  }
}
