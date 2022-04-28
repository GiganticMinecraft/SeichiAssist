package com.github.unchama.seichiassist.util

import org.bukkit.entity.EntityType
import org.bukkit.entity.EntityType.{
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
  PIG_ZOMBIE,
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
  ZOMBIE_VILLAGER
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
    PIG_ZOMBIE,
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
    ZOMBIE_VILLAGER
  ).contains(entityType)
}
