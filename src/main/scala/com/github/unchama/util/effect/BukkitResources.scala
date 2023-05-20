package com.github.unchama.util.effect

import cats.effect.{Resource, Sync, SyncIO}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.{Location, Material}

object BukkitResources {

  /**
   * 参照された`Block`達が開放時に空気ブロックに書き換えられるような`Resource`としての`Block`
   */
  def vanishingBlockSetResource[F[_]: Sync: OnMinecraftServerThread, B <: Block](
    referenceSet: Set[B]
  ): Resource[F, Set[B]] =
    Resource.make(Sync[F].delay(referenceSet))(block =>
      OnMinecraftServerThread[F].runAction[SyncIO, Unit] {
        SyncIO {
          block.foreach(_.setType(Material.AIR))
        }
      }
    )

  /**
   * 確保された`Entity`が開放時に除去されるような`Resource`としての`Entity`
   */
  def vanishingEntityResource[F[_]: Sync: OnMinecraftServerThread, E <: Entity](
    spawnLocation: Location,
    tag: Class[E]
  ): Resource[F, E] = {
    Resource.make(OnMinecraftServerThread[F].runAction[SyncIO, E] {
      SyncIO {
        spawnLocation.getWorld.spawn(spawnLocation, tag)
      }
    })(e =>
      OnMinecraftServerThread[F].runAction[SyncIO, Unit] {
        SyncIO {
          e.remove()
        }
      }
    )
  }
}
