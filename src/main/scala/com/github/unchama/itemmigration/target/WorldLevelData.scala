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
 * @param worlds                    変換対象であるワールドのコレクション
 * @param chunkCoordinateEnumerator ワールド内で変換すべきチャンク座標を列挙するプログラム
 */
case class WorldLevelData(worlds: IndexedSeq[World],
                          chunkCoordinateEnumerator: World => IO[Seq[(Int, Int)]]) extends ItemMigrationTarget[IO] {

  override def runMigration(conversion: ItemConversion): IO[Unit] = {
    import cats.implicits._

    worlds.toList.traverse { w =>
      for {
        coords <- chunkCoordinateEnumerator(w)
        _ <- WorldLevelData.convertChunkWise(w, coords, conversion)
      } yield ()
    }.as(())
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