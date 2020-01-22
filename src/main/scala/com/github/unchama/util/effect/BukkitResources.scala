package com.github.unchama.util.effect

import cats.effect.{IO, Resource}
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.{Location, Material}

object BukkitResources {
  /**
   * 参照された`Block`が開放時に空気ブロックに書き換えられるような`Resource`としての`Block`
   */
  def vanishingBlockResource(reference: Block): Resource[IO, Block] =
    Resource.make(
      IO(reference)
    )(block =>
      IO { block.setType(Material.AIR) }
    )

  /**
   * 確保された`Entity`が開放時に除去されるような`Resource`としての`Block`
   */
  def vanishingEntityResource[E <: Entity](spawnLocation: Location, tag: Class[E]): Resource[IO, E] = {
    Resource.make(
      IO(spawnLocation.getWorld.spawn(spawnLocation, tag))
    )(e =>
      IO(e.remove())
    )
  }
}
