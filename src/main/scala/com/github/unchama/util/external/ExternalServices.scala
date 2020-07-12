package com.github.unchama.util.external

import cats.effect.IO
import chunk_search.{Chunk, ChunkCoord, SearchResult}
import com.github.unchama.util.MillisecondTimer
import com.github.unchama.util.bukkit.WorldUtil
import org.bukkit.World

object ExternalServices {

  def getChunkCoordinates(chunkSearchCommand: String)(world: World): IO[Seq[(Int, Int)]] =
  // 普通、この検索にはかなりの時間がかかるので要した時間をログに表示する
    MillisecondTimer.timeIO(IO {
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
      result
    })(s"${world.getName}内のチャンクを検索しました。")

}
