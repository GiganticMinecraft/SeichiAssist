package com.github.unchama.seichiassist

import org.bukkit.World

enum class ManagedWorld(
    val alphabetName: String,
    val japaneseName: String,
    val isSeichi: Boolean) {

  WORLD_SPAWN("world_spawn", "スポーンワールド", false),
  WORLD("world", "メインワールド", false),
  WORLD_SW("world_SW", "第一整地ワールド", true),
  WORLD_SW_2("world_SW_2", "第二整地ワールド", true),
  WORLD_SW_3("world_SW_3", "第三整地ワールド", true),
  WORLD_SW_NETHER("world_SW_nether", "整地ネザー", true),
  WORLD_SW_END("world_SW_the_end", "整地エンド", true);

  companion object {
    val seichiWorlds = values().filter { it.isSeichi }
    fun fromBukkitWorld(world: World): ManagedWorld? = values().find { it.name == world.name }
  }
}
