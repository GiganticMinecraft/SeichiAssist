package com.github.unchama.itemmigration.targets

import cats.effect.Concurrent
import cats.effect.concurrent.Ref
import com.github.unchama.itemmigration.domain.{ItemMigrationTarget, ItemStackConversion}
import com.github.unchama.itemmigration.util.MigrationHelper
import com.github.unchama.util.MillisecondTimer
import org.bukkit.block.Container
import org.bukkit.entity.{Item, ItemFrame}
import org.bukkit.inventory.{InventoryHolder, ItemStack}
import org.bukkit.{Bukkit, World, WorldCreator}
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
  def convertChunkWise[F[_]](originalWorld: World, targetChunks: Seq[(Int, Int)], conversion: ItemStack => ItemStack)
                            (implicit F: Concurrent[F], logger: Logger): F[Unit] = {

    val worldRef = Ref.unsafe(originalWorld)

    import cats.implicits._
    import com.github.unchama.util.nms.v1_12_2.world.WorldChunkSaving

    val migrateChunk: World => ((Int, Int)) => F[Unit] = world => chunkCoordinate => F.delay {
      val chunk = world.getChunkAt(chunkCoordinate._1, chunkCoordinate._2)

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

    val chunkConversionEffects: List[F[Unit]] =
      targetChunks.toList.map { chunkCoordinate =>
        worldRef.get >>=
          (migrateChunk(_)(chunkCoordinate))
      }

    val queueChunkSaverFlush =
      F.delay {
        logger.info("チャンクの保存キューの処理を要求します…")
      } >> F.start {
        WorldChunkSaving.relaxFileIOThreadThrottle[F] >> F.delay {
          logger.info("チャンクの保存キューが処理されました")
        }
      }.as(())

    val flushEntityRemovalQueue = worldRef.get >>= { world =>
      WorldChunkSaving.flushEntityRemovalQueue(world)
    }

    val chunkSaverQueueFlushInterval = 1000
    val reloadWorldInterval = 5000

    val reloadWorld = {
      worldRef.get >>= { world =>
        F.delay {
          logger.info(s"${world.getName}を再読み込みします…")

          val creator = WorldCreator.name(world.getName).copy(world)
          Bukkit.unloadWorld(world, true)
          Bukkit.createWorld(creator)
        }.flatTap {
          newWorld => F.delay(logger.info(s"${newWorld.getName}を再読み込みしました"))
        }
      } >>= worldRef.set
    }

    chunkConversionEffects
      .mapWithIndex { case (effect, index) =>
        if (index % chunkSaverQueueFlushInterval == 0)
          effect >> flushEntityRemovalQueue >> queueChunkSaverFlush
        else
          effect
      }
      .mapWithIndex { case (effect, index) =>
        if (index % reloadWorldInterval == 0)
          effect >> reloadWorld
        else
          effect
      }
      .sequence >> reloadWorld
  }
}
