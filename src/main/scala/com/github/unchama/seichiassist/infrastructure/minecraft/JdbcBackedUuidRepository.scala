package com.github.unchama.seichiassist.infrastructure.minecraft

import java.util.UUID

import cats.effect.IO
import com.github.unchama.seichiassist.domain.minecraft.UuidRepository
import org.slf4j.Logger

import scala.collection.mutable

/**
 * SeichiAssistのDBによるUUID解決を試みる [[UuidRepository]]。
 */
class JdbcBackedUuidRepository(implicit val logger: Logger) extends UuidRepository[IO] {

  private var isDBCacheCreated = false
  private val lowerCaseNameUuidCache: mutable.Map[String, UUID] = mutable.Map()

  import cats.implicits._

  val loadDBCache: IO[Unit] = IO {
    import scalikejdbc._

    val databaseEntries = DB readOnly { implicit session =>
      sql"select uuid, name from seichiassist.playerdata"
        .map { rs =>
          // プレーヤー名はcase-insensitive
          (rs.string("name").toLowerCase, UUID.fromString(rs.string("uuid")))
        }
        .list()
        .apply()
    }

    lowerCaseNameUuidCache.addAll(databaseEntries)
    isDBCacheCreated = true
    logger.info(s"DBから${databaseEntries.size}件の名前->UUIDペアを読み込みました。")
  }

  /**
   * DBに入っているデータからUUIDの解決を試みる。
   *
   * 初回問い合わせ時にのみ全データを読み込むため、
   * バッチ処理での使用を想定している。
   *
   * Minecraftのアカウントの仕様上、過去の名前のみから現在のUUIDを割り出すことは不可能であるため、
   * Mojangへの問い合わせは行っていない。具体的には、例えばプレーヤーがname1からname2に名前を変更した後、
   * 別のプレーヤーがname1の名前を使用することができる。
   *
   * このことから、最後にSeichiAssistが導入されていたサーバーで入った名前のみからUUIDを割り出すことにしている。
   */
  override def getUuid(playerName: String): IO[Option[UUID]] = {
    for {
      _ <- IO {
        if (!isDBCacheCreated) loadDBCache else IO.unit
      }.flatten
      cachedValue <- IO {
        // プレーヤー名はcase-insensitive
        lowerCaseNameUuidCache.get(playerName.toLowerCase)
      }
    } yield cachedValue
  }
}

