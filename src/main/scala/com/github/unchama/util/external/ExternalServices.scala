package com.github.unchama.util.external

import cats.effect.{ContextShift, IO}
import com.github.unchama.util.MillisecondTimer
import com.github.unchama.util.bukkit.WorldUtil
import inventory_search.{Chunk, ChunkCoord, SearchResult}
import org.bukkit.World

object ExternalServices {
  // TODO inject this from config
  val defaultCommand: String = "chunk-search-rs --protobuf --threads 12"

  def getAllGeneratedChunks(chunkSearchCommand: String)
                           (implicit contextShift: ContextShift[IO]): IO[Map[World, Seq[(Int, Int)]]] = {
    val mvc = ExternalPlugins.getMultiverseCore

    def getChunkCoordinates(world: World): IO[(World, Seq[(Int, Int)])] =
      MillisecondTimer.timeIO(s"Search for chunks in ${world.getName}")(IO {
        val command = s"$chunkSearchCommand ${WorldUtil.getAbsoluteWorldFolder(world)}"
        val result =
          SearchResult.parseFrom(Runtime.getRuntime.exec(command).getInputStream)
            .result
            .flatMap {
              // https://bukkit.org/threads/combining-world-and-world_nether.60319/
              // Bukkitの挙動でワールドとフォルダが一対一に対応するため、dimIdは無視して良い
              case Chunk(Some(ChunkCoord(x, z, _)), _, _) =>
                Some((x, z))
              case _ =>
                None
            }
        world -> result
      })

    import scala.jdk.CollectionConverters._
    import cats.implicits._

    for {
      worlds <- IO { mvc.getMVWorldManager.getMVWorlds.asScala.toList.map(_.getCBWorld) }
      result <- worlds.map(getChunkCoordinates).parSequence.map(_.toMap)
    } yield result
  }
}
