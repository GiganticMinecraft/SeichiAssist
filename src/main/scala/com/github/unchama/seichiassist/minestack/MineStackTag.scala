package com.github.unchama.seichiassist.minestack

private[minestack] sealed trait MineStackTag

object MineStackTag {
  trait Building extends MineStackTag
  trait MobDrop extends MineStackTag
  trait Agricultural extends MineStackTag
  trait GachaPrize extends MineStackTag
  trait Ores extends MineStackTag
  trait RedstoneAndTransportation extends MineStackTag
}
