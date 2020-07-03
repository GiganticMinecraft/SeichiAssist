package com.github.unchama.seichiassist.itemconversion

import cats.effect.IO
import com.github.unchama.itemmigration.{ItemMigrationTarget, MigrationHelper}
import com.github.unchama.util.MillisecondTimer
import com.github.unchama.util.external.{ExternalPlugins, ExternalServices}
import org.bukkit.World
import org.bukkit.block.Container
import org.bukkit.entity.{Item, ItemFrame}
import org.bukkit.inventory.{InventoryHolder, ItemStack}

object WorldLevelData extends ItemMigrationTarget[IO] {
  private def convertChunkWise(world: World, targetChunks: Seq[(Int, Int)], conversion: ItemStack => ItemStack): Unit = {
    for {(chunkX, chunkZ) <- targetChunks} {
      val chunk = world.getChunkAt(chunkX, chunkZ)

      chunk.getTileEntities.foreach {
        case containerState: Container =>
          MigrationHelper.convertEachContent(containerState.getInventory)(conversion)
        case _ =>
      }

      chunk.getEntities.foreach {
        case inventoryHolder: InventoryHolder =>
          MigrationHelper.convertEachContent(inventoryHolder.getInventory)(conversion)
        case item: Item =>
          item.setItemStack(conversion(item.getItemStack))
        case frame: ItemFrame =>
          frame.setItem(conversion(frame.getItem))
        case _ =>
      }
    }
  }

  override def runMigration(conversion: ItemStack => ItemStack): IO[Unit] = {
    val multiverseCore = ExternalPlugins.getMultiverseCore
    val command = ExternalServices.defaultCommand

    import cats.implicits._

    import scala.jdk.CollectionConverters._

    def runConversion(world: World): IO[Unit] = MillisecondTimer.timeIO {
      for {
        chunkCoordinates <- ExternalServices.getChunkCoordinates(command)(world)
        _ <- IO.delay {
          convertChunkWise(world, chunkCoordinates, conversion)
        }
      } yield ()
    }(s"${world.getName}内のアイテム変換を行いました。")

    for {
      worlds <- IO.delay {
        multiverseCore
          .getMVWorldManager
          .getMVWorlds
          .asScala.map(_.getCBWorld).toList
      }
      _ <- worlds.traverse(runConversion)
    } yield ()
  }

}
