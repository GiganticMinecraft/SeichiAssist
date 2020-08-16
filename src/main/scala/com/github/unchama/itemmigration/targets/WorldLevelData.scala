package com.github.unchama.itemmigration.targets

import cats.Monad
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

  private implicit class ListHasModifyEvery[F[_]](val list: List[F[Unit]]) extends AnyVal {
    def atEvery(interval: Int)(actionAt: Int => F[Unit])(implicit F: Monad[F]): List[F[Unit]] = {
      import cats.implicits._

      list.mapWithIndex { case (x, index) =>
        if ((index + 1) % interval == 0) x >> actionAt(index) else x
      }
    }
  }

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
        logger.warn(s"チャンク(${chunk.getX}, ${chunk.getZ})はアンロードされませんでした。")
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
    val progressLogInterval = 1000
    val reloadWorldInterval = 10000

    val reloadWorld = {
      worldRef.get >>= { world =>
        F.delay {
          logger.info(s"${world.getName}を再読み込みします…")

          val creator = WorldCreator.name(world.getName).copy(world)
          if (!Bukkit.unloadWorld(world, true)) {
            logger.warn(s"${world.getName}はアンロードされませんでした。")
          }
          Bukkit.createWorld(creator)
        }.flatTap {
          newWorld => F.delay(logger.info(s"${newWorld.getName}を再読み込みしました"))
        }
      } >>= worldRef.set
    }

    def logProgress(chunkIndex: Int): F[Unit] = worldRef.get >>= { world =>
      F.delay {
        val processed = chunkIndex + 1
        logger.info(s"${world.getName} のマイグレーション: $processed / ${targetChunks.size} 完了")
      }
    }

    /*
     * flushEntityRemovalQueue及びqueueChunkSaverFlushが短期的なメモリ確保、
     * reloadWorldが長期的な(複数ワールド処理の範疇での)メモリ確保に寄与する。
     *
     * reloadWorldには比較的時間が掛かるので少なめ、しかし変換が終わると必ず実行するようにした。
     *
     * 同時に導入するプラグインによっては、チャンクやワールドのロード/アンロードのハンドラ内で
     * ワールドに対する参照を直接持ち、アンロード時のGCを妨げるものがある。
     *
     * OutOfMemoryErrorが観測された際には、プロファイラで残留しているワールドのインスタンスを確認し、
     * GC Rootからの参照パスを特定することを推奨する。
     */
    chunkConversionEffects
      .atEvery(chunkSaverQueueFlushInterval)(_ => flushEntityRemovalQueue >> queueChunkSaverFlush)
      .atEvery(progressLogInterval)(logProgress)
      .atEvery(reloadWorldInterval)(_ => reloadWorld)
      .sequence >> reloadWorld
  }
}
