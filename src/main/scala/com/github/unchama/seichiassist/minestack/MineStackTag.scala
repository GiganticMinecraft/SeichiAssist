package com.github.unchama.seichiassist.minestack

private sealed trait MineStackTag

object MineStackTag {
  trait Building extends MineStackTag
  trait MobDrop extends MineStackTag
  trait Arricultural extends MineStackTag
  trait GachaPrize extends MineStackTag
  trait Ores extends MineStackTag
  trait RedstoneAndTransportation extends MineStackTag
}
