package com.github.unchama.seichiassist.subsystems.minestack.domain

sealed trait MineStackCategory extends enumeratum.EnumEntry

object MineStackCategory extends enumeratum.Enum[MineStackCategory] {

  case object Build extends MineStackCategory
  case object MobDrops extends MineStackCategory
  case object Farming extends MineStackCategory
  case object GachaPrizes extends MineStackCategory
  case object Ores extends MineStackCategory
  case object Redstone extends MineStackCategory

  override def values: IndexedSeq[MineStackCategory] = findValues

}
