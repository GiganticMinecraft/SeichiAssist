package com.github.unchama.util.external

import cats.effect.IO
import com.github.unchama.seichiassist.data.XYZTuple
import inventory_search.{Coordinate, SearchResult, TileEntity}
import org.bukkit.World

object ExternalServices {
  // TODO inject this from config
  val defaultCommand: String = "dotnet /inventory-search/InventorySearch.dll"

  def getAllTileEntitiesWithInventories(inventorySearchCommand: String): IO[Map[World, Seq[XYZTuple]]] = IO {
    val mvc = ExternalPlugins.getMultiverseCore

    import scala.jdk.CollectionConverters._

    mvc.getMVWorldManager.getMVWorlds
      .asScala.toList.map(_.getCBWorld)
      .map { world =>
        val command = s"$inventorySearchCommand ${world.getWorldFolder}"
        val result =
          SearchResult.parseFrom(Runtime.getRuntime.exec(command).getInputStream)
            .result
            .flatMap {
              // https://bukkit.org/threads/combining-world-and-world_nether.60319/
              // Bukkitの挙動でワールドとフォルダが一対一に対応するため、dimIdは無視して良い
              case TileEntity(Some(Coordinate(x, y, z, _)), _, _) =>
                Some(XYZTuple(x, y, z))
              case _ =>
                None
            }

        world -> result
      }.toMap
  }
}
