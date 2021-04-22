package com.github.unchama.seichiassist.minestack

import com.github.unchama.seichiassist.minestack.MineStackTag._
import enumeratum._

/**
 * マインスタックに収納するアイテムのカテゴリを表すオブジェクト.
 *
 * @param serializedValue この列挙体を永続化する際の識別子となる整数.
 * @param uiLabel         UI上で表示する際のカテゴリの名前
 */
sealed abstract class MineStackObjectCategory[A](val serializedValue: Int, val uiLabel: String) extends EnumEntry

case object MineStackObjectCategory extends Enum[MineStackObjectCategory[_]] {

  val values: IndexedSeq[MineStackObjectCategory[_]] = findValues

  def fromSerializedValue(value: Int): Option[MineStackObjectCategory[_]] = values.find(_.serializedValue == value)

  case object ORES extends MineStackObjectCategory[Ores](0, "鉱石系アイテム")

  case object MOB_DROP extends MineStackObjectCategory[MobDrop](1, "ドロップ系アイテム")

  case object AGRICULTURAL extends MineStackObjectCategory[Agricultural](2, "農業・食料系アイテム")

  case object BUILDING extends MineStackObjectCategory[Building](3, "建築系アイテム")

  case object REDSTONE_AND_TRANSPORTATION extends MineStackObjectCategory[RedstoneAndTransportation](4, "レッドストーン・移動系アイテム")

  case object GACHA_PRIZES extends MineStackObjectCategory[GachaPrize](5, "ガチャ品")

  implicit class MineStackObjOps[A](val mineStackObj: MineStackObj[A]) extends AnyVal {
    def category(): MineStackObjectCategory[A] = mineStackObj.stackType
  }

}
