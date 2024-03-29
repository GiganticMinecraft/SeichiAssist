package com.github.unchama.util.external

import cats.effect.Sync
import chunk_search.{Chunk, ChunkCoord, SearchResult}
import com.github.unchama.util.MillisecondTimer
import com.github.unchama.util.bukkit.WorldUtil
import org.bukkit.World
import org.slf4j.Logger

object ExternalServices {

  def getChunkCoordinates[F[_]: Sync](
    chunkSearchCommand: String
  )(world: World)(implicit logger: Logger): F[Seq[(Int, Int)]] = {
    import cats.implicits._

    // 普通、この検索にはかなりの時間がかかるので要した時間をログに表示する
    MillisecondTimer
      .timeF(Sync[F].delay {
        val command = s"$chunkSearchCommand ${WorldUtil.getAbsoluteWorldFolder(world)}"
        val result =
          SearchResult
            .parseFrom(Runtime.getRuntime.exec(command).getInputStream)
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
      .flatTap { seq =>
        Sync[F].delay {
          logger.info(s"変換対象チャンク数${seq.size}")
        }
      }
  }

}
