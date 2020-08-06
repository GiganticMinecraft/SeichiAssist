package com.github.unchama.itemmigration.targets

import cats.effect.Concurrent
import com.github.unchama.itemmigration.domain.{ItemMigrationTarget, ItemStackConversion}
import com.github.unchama.itemmigration.util.MigrationHelper
import com.github.unchama.util.MillisecondTimer
import org.bukkit.World
import org.bukkit.block.Container
import org.bukkit.entity.{Item, ItemFrame}
import org.bukkit.inventory.{InventoryHolder, ItemStack}
import org.slf4j.Logger

/**
 * マイグレーションターゲットとしてのワールドデータを表すデータ
 *
 * `enumerateChunkCoordinates` は、ワールドを渡すとそのワールド内で変換すべきチャンク座標(x, z)を列挙する。
 *
 * @param getWorlds                 変換対象であるワールドを列挙するプログラム
 * @param enumerateChunkCoordinates ワールド内で変換すべきチャンク座標を列挙するプログラム
 */
case class WorldLevelData[F[_]](getWorlds: F[IndexedSeq[World]],
                                enumerateChunkCoordinates: World => F[Seq[(Int, Int)]])
                               (implicit metricsLogger: Logger, F: Concurrent[F]) extends ItemMigrationTarget[F] {

  override def runMigration(conversion: ItemStackConversion): F[Unit] = {
    import cats.implicits._

    def convertWorld(world: World): F[Unit] =
      MillisecondTimer.timeF {
        for {
          coords <- enumerateChunkCoordinates(world)
          _ <- WorldLevelData.convertChunkWise(world, coords, conversion)
          _ <- F.delay {
            metricsLogger.info(s"$world 内のアイテム変換済みチャンク数： ${coords.size}")
          }
        } yield ()
      }(s"$world 内のアイテムを変換しました。")

    for {
      worlds <- getWorlds
      _ <- worlds.toList.traverse(convertWorld)
    } yield ()
  }

}

object WorldLevelData {
  def convertChunkWise[F[_]](world: World, targetChunks: Seq[(Int, Int)], conversion: ItemStack => ItemStack)
                            (implicit F: Concurrent[F]): F[Unit] = {

    val chunkConversionEffects =
      for {
        (chunkX, chunkZ) <- targetChunks.toList
      } yield F.delay {
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

        // メモリ解放を促す
        if (!world.unloadChunk(chunk)) {
          println(s"チャンク(${chunk.getX}, ${chunk.getZ})はアンロードされませんでした。")
        }
      }

    import cats.implicits._

    val queueChunkSaverFlush =
      com.github.unchama.util.nms.v1_12_2.world
        .WorldChunkSaving
        .flushChunkSaverQueue[F]
        .as(())

    val chunkSaverQueueFlushInterval = 500

    chunkConversionEffects
      .mapWithIndex { case (effect, index) =>
        if (index % chunkSaverQueueFlushInterval == 0)
          effect >> queueChunkSaverFlush
        else
          effect
      }
      .sequence
      .as(())
  }
}