package com.github.unchama.itemmigration.target

import cats.effect.IO
import com.github.unchama.itemmigration.ItemMigration.ItemConversion
import com.github.unchama.itemmigration.{ItemMigrationTarget, MigrationHelper}
import org.bukkit.World
import org.bukkit.block.Container
import org.bukkit.entity.{Item, ItemFrame}
import org.bukkit.inventory.{InventoryHolder, ItemStack}

/**
 * マイグレーションターゲットとしてのワールドデータを表すデータ
 *
 * @param getWorlds                 変換対象であるワールドを列挙するプログラム
 * @param enumerateChunkCoordinates ワールド内で変換すべきチャンク座標を列挙するプログラム
 */
case class WorldLevelData(getWorlds: IO[IndexedSeq[World]],
                          enumerateChunkCoordinates: World => IO[Seq[(Int, Int)]]) extends ItemMigrationTarget[IO] {

  override def runMigration(conversion: ItemConversion): IO[Unit] = {
    import cats.implicits._

    def convertWorld(world: World): IO[Unit] =
      for {
        coords <- enumerateChunkCoordinates(world)
        _ <- WorldLevelData.convertChunkWise(world, coords, conversion)
      } yield ()

    for {
      worlds <- getWorlds
      _ <- worlds.toList.traverse(convertWorld)
    } yield ()
  }

}

object WorldLevelData {
  def convertChunkWise(world: World, targetChunks: Seq[(Int, Int)], conversion: ItemStack => ItemStack): IO[Unit] = IO {
    for {(chunkX, chunkZ) <- targetChunks} {
      val chunk = world.getChunkAt(chunkX, chunkZ)

      chunk.getTileEntities.foreach {
        case containerState: Container =>
          MigrationHelper.convertEachStackIn(containerState.getInventory)(conversion)
        case _ =>
      }

      chunk.getEntities.foreach {
        case inventoryHolder: InventoryHolder =>
          MigrationHelper.convertEachStackIn(inventoryHolder.getInventory)(conversion)
        case item: Item =>
          item.setItemStack(conversion(item.getItemStack))
        case frame: ItemFrame =>
          frame.setItem(conversion(frame.getItem))
        case _ =>
      }
    }
  }
}