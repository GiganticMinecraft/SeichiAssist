package com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.targets

import cats.effect.{Concurrent, Sync}
import com.github.unchama.itemmigration.bukkit.targets.WorldLevelData
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.util.external.{ExternalPlugins, ExternalServices}
import org.bukkit.World
import org.slf4j.Logger

private object DelegatedImpls {
  def getWorlds[F[_]: Sync]: F[IndexedSeq[World]] = {
    val multiverseCore = ExternalPlugins.getMultiverseCore

    import scala.jdk.CollectionConverters._

    Sync[F].delay {
      multiverseCore.getMVWorldManager.getMVWorlds.asScala.map(_.getCBWorld).toIndexedSeq
    }
  }

  def getWorldChunkCoordinates[F[_]: Sync](
    implicit logger: Logger
  ): World => F[Seq[(Int, Int)]] =
    ExternalServices.getChunkCoordinates[F](
      SeichiAssist.seichiAssistConfig.chunkSearchCommandBase
    )
}

class SeichiAssistWorldLevelData[F[_]](implicit metricsLogger: Logger, F: Concurrent[F])
    extends WorldLevelData[F](DelegatedImpls.getWorlds, DelegatedImpls.getWorldChunkCoordinates)
