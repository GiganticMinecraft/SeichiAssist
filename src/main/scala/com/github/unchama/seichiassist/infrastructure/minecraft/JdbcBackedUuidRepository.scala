package com.github.unchama.seichiassist.infrastructure.minecraft

import java.util.UUID

import cats.effect.IO
import com.github.unchama.seichiassist.domain.minecraft.UuidRepository
import org.slf4j.Logger

import scala.collection.mutable

/**
 * SeichiAssistのDBによる解決を先ず試み、
 * 解決できなければMojangに問い合わせるような [[UuidRepository]]。
 *
 * Mojangへの問い合わせを最小化したいため、結果は必ずキャッシュする。
 */
class JdbcBackedUuidRepository(implicit val logger: Logger) extends UuidRepository[IO] {

  private var isDBCacheCreated = false
  private var cache: mutable.Map[String, UUID] = mutable.Map()

  import cats.implicits._

  val loadDBCache: IO[Unit] = IO {
    ???

    isDBCacheCreated = true
    logger.info(s"DBから${0}件の名前-UUIDペアを読み込みました。")
  }

  def resolveUuidUsingMojangApi(playerName: String): IO[Option[UUID]] = {
    logger.warn(s"Mojangへ${playerName}のUUIDを問い合わせています...")

    ???
  }

  override def getUuid(playerName: String): IO[Option[UUID]] = {
    for {
      _ <- IO {
        if (!isDBCacheCreated) loadDBCache else IO.unit
      }.flatten
      cachedValue <- IO {
        cache.get(playerName)
      }
      result <- cachedValue match {
        case Some(resolvedCache) => IO.pure(Option(resolvedCache))
        case None => resolveUuidUsingMojangApi(playerName)
      }
    } yield result
  }
}

