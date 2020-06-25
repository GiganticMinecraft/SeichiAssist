package com.github.unchama.util.external

import cats.effect.IO
import com.github.unchama.util.bukkit.WorldUtil
import inventory_search.{Chunk, ChunkCoord, SearchResult}
import org.bukkit.World

object ExternalServices {
  // TODO inject this from config
  val defaultCommand: String = "dotnet /inventory-search/InventorySearch.dll"

  def getAllChunkCoordsWithPotentialItems(inventorySearchCommand: String): IO[Map[World, Seq[(Int, Int)]]] = IO {
    val mvc = ExternalPlugins.getMultiverseCore

    import scala.jdk.CollectionConverters._

    mvc.getMVWorldManager.getMVWorlds
      .asScala.toList.map(_.getCBWorld)
      .map { world =>
        val command = s"$inventorySearchCommand ${WorldUtil.getAbsoluteWorldFolder(world)}"
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
      }.toMap
  }
}
