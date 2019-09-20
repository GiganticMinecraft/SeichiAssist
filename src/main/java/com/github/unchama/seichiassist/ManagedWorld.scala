package com.github.unchama.seichiassist

import org.bukkit.World

enum class ManagedWorld(
    val alphabetName: String,
    val japaneseName: String) {

  WORLD_SPAWN("world_spawn", "スポーンワールド"),
  WORLD_2("world_2", "メインワールド"), // "world"は旧メインワールドのidであり既に存在しない
  WORLD_SW("world_SW", "第一整地ワールド"),
  WORLD_SW_2("world_SW_2", "第二整地ワールド"),
  WORLD_SW_3("world_SW_3", "第三整地ワールド"),
  WORLD_SW_4("world_SW_4", "第四整地ワールド"),
  WORLD_SW_NETHER("world_SW_nether", "整地ネザー"),
  WORLD_SW_END("world_SW_the_end", "整地エンド");
}

object ManagedWorld {
  val seichiWorlds = values().filter { it.isSeichi }

  def fromName(worldName: String): ManagedWorld? = values().find { it.alphabetName == worldName }

  def fromBukkitWorld(world: World): ManagedWorld? = fromName(world.name)
}

val ManagedWorld.isSeichi: Boolean
  get() = when (this) {
    WORLD_SW, WORLD_SW_2, WORLD_SW_3, WORLD_SW_4, WORLD_SW_NETHER, WORLD_SW_END => true
    else => false
  }

/**
 * 保護を掛けて整地するワールドであるかどうか
 */
val ManagedWorld.isSeichiWorldWithWGRegions: Boolean
  get() = when (this) {
    WORLD_SW_2, WORLD_SW_4 => true
    else => false
  }

val ManagedWorld.shouldMuteCoreProtect: Boolean
  get() = this.isSeichiWorldWithWGRegions

def World.asManagedWorld(): ManagedWorld? = Companion.fromBukkitWorld(this)
