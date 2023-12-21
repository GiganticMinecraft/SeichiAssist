package com.github.unchama.seichiassist.util

import org.bukkit.entity.EntityType
import org.bukkit.entity.EntityType.{
  BLAZE,
  CAVE_SPIDER,
  CREEPER,
  DROWNED,
  ELDER_GUARDIAN,
  ENDERMAN,
  ENDERMITE,
  EVOKER,
  GHAST,
  GUARDIAN,
  HOGLIN,
  HUSK,
  MAGMA_CUBE,
  PIGLIN,
  SHULKER,
  SILVERFISH,
  SKELETON,
  SLIME,
  SPIDER,
  STRAY,
  VEX,
  VINDICATOR,
  WITCH,
  WITHER_SKELETON,
  ZOMBIE,
  ZOMBIE_VILLAGER,
  ZOMBIFIED_PIGLIN
}

object EnemyEntity {
  def isEnemy(entityType: EntityType): Boolean = Set(
    BLAZE,
    CAVE_SPIDER,
    CREEPER,
    ELDER_GUARDIAN,
    ENDERMAN,
    ENDERMITE,
    EVOKER,
    GHAST,
    GUARDIAN,
    HUSK,
    MAGMA_CUBE,
    ZOMBIFIED_PIGLIN,
    SHULKER,
    SILVERFISH,
    SKELETON,
    SLIME,
    SPIDER,
    STRAY,
    VEX,
    VINDICATOR,
    WITCH,
    WITHER_SKELETON,
    ZOMBIE,
    ZOMBIE_VILLAGER,
    PIGLIN,
    HOGLIN,
    DROWNED
  ).contains(entityType)
}
