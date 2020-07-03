package com.github.unchama.seichiassist.itemconversion

import cats.effect.IO
import com.github.unchama.itemmigration.ItemMigrationTarget
import com.github.unchama.itemmigration.target.WorldLevelData
import com.github.unchama.util.external.{ExternalPlugins, ExternalServices}
import org.bukkit.inventory.ItemStack

object MVWorldLevelData extends ItemMigrationTarget[IO] {
  override def runMigration(conversion: ItemStack => ItemStack): IO[Unit] = {
    val multiverseCore = ExternalPlugins.getMultiverseCore
    val command = ExternalServices.defaultCommand

    import scala.jdk.CollectionConverters._

    for {
      worlds <- IO.delay {
        multiverseCore.getMVWorldManager
          .getMVWorlds.asScala
          .map(_.getCBWorld).toIndexedSeq
      }
      _ <- WorldLevelData(worlds, ExternalServices.getChunkCoordinates(command)).runMigration(conversion)
    } yield ()
  }

}
