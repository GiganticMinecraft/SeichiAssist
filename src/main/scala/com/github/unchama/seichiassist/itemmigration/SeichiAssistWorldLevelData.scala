package com.github.unchama.seichiassist.itemmigration

import cats.effect.IO
import com.github.unchama.itemmigration.domain.ItemMigration.ItemStackConversion
import com.github.unchama.itemmigration.domain.ItemMigrationTarget
import com.github.unchama.itemmigration.target.WorldLevelData
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.util.external.{ExternalPlugins, ExternalServices}
import org.bukkit.World

object SeichiAssistWorldLevelData extends ItemMigrationTarget[IO] {

  val getWorlds: IO[IndexedSeq[World]] = {
    val multiverseCore = ExternalPlugins.getMultiverseCore

    import scala.jdk.CollectionConverters._

    IO {
      multiverseCore.getMVWorldManager
        .getMVWorlds.asScala
        .map(_.getCBWorld).toIndexedSeq
    }
  }

  val getWorldChunkCoordinates: World => IO[Seq[(Int, Int)]] =
    ExternalServices.getChunkCoordinates(SeichiAssist.seichiAssistConfig.chunkSearchCommandBase())

  override def runMigration(conversion: ItemStackConversion): IO[Unit] =
    WorldLevelData(getWorlds, getWorldChunkCoordinates).runMigration(conversion)
}
