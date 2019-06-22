package com.github.unchama.seichiassist.minestack

enum class MineStackObjectCategory(val serializedValue: Int) {
  ORES(0),
  MOB_DROP(1),
  AGRICULTURAL(2),
  BUILDING(3),
  REDSTONE_AND_TRANSPORTATION(4),
  GACHA_PRIZES(5);

  companion object {
    fun fromSerializedValue(value: Int): MineStackObjectCategory? = values().find { it.serializedValue == value }
  }
}

fun MineStackObj.category(): MineStackObjectCategory = MineStackObjectCategory.fromSerializedValue(this.stackType)!!
