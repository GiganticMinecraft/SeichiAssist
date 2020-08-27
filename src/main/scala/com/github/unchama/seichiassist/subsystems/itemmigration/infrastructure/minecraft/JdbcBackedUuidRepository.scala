package com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.minecraft

import java.util.UUID

import cats.Applicative
import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.itemmigration.domain.minecraft.UuidRepository
import org.slf4j.Logger

object JdbcBackedUuidRepository {

  trait ApplicableUuidRepository {
    def apply[F[_] : Applicative]: UuidRepository[F]
  }

  /**
   * DBに入っているデータから `UuidRepository` を作成する。
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
  def initializeStaticInstance[F[_] : Sync](implicit logger: Logger): F[ApplicableUuidRepository] = Sync[F].delay {
    import scalikejdbc._

    val databaseEntries = DB.readOnly { implicit session =>
      sql"select uuid, name from seichiassist.playerdata"
        .map { rs =>
          // プレーヤー名はcase-insensitive
          (rs.string("name").toLowerCase, UUID.fromString(rs.string("uuid")))
        }
        .list()
        .apply()
        .toMap
    }

    new ApplicableUuidRepository {
      override def apply[G[_] : Applicative]: UuidRepository[G] = {
        (playerName: String) => Applicative[G].pure(databaseEntries.get(playerName))
      }
    }
  }

  def initializeInstanceIn[F[_] : Sync, G[_] : Applicative](implicit logger: Logger): F[UuidRepository[G]] = {
    import cats.implicits._

    for {
      applicativeRepository <- initializeStaticInstance[F]
    } yield {
      applicativeRepository[G]
    }
  }

  def initializeInstance[F[_] : Sync](implicit logger: Logger): F[UuidRepository[F]] = initializeInstanceIn[F, F]
}