package com.github.unchama.seichiassist.itemmigration

import cats.effect.IO
import com.github.unchama.itemmigration.target.WorldLevelData
import com.github.unchama.util.external.{ExternalPlugins, ExternalServices}
import org.bukkit.World

object SeichiAssistWorldLevelData {

  val getWorlds: IO[IndexedSeq[World]] = {
    val multiverseCore = ExternalPlugins.getMultiverseCore

    import scala.jdk.CollectionConverters._

    IO {
      multiverseCore.getMVWorldManager
        .getMVWorlds.asScala
        .map(_.getCBWorld).toIndexedSeq
    }
  }

  val getWorldChunkCoordinates: World => IO[Seq[(Int, Int)]] = {
    val command = ExternalServices.defaultCommand

    ExternalServices.getChunkCoordinates(command)
  }

  val migrationTarget: WorldLevelData = WorldLevelData(getWorlds, getWorldChunkCoordinates)

}
