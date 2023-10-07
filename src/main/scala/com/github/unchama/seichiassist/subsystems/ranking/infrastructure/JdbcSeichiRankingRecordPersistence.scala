package com.github.unchama.seichiassist.subsystems.ranking.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiExpAmount
import com.github.unchama.seichiassist.subsystems.ranking.domain.{
  RankingRecord,
  RankingRecordPersistence
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcSeichiRankingRecordPersistence[F[_]: Sync]
    extends RankingRecordPersistence[F, SeichiAmountData] {

  override def getAllRankingRecords: F[Vector[RankingRecord[SeichiAmountData]]] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"select name, totalbreaknum from playerdata"
          .map { rs =>
            RankingRecord(
              rs.string("name"),
              UUID.fromString(rs.string("uuid")),
              SeichiAmountData(
                SeichiExpAmount.ofNonNegative(rs.bigInt("totalbreaknum").longValueExact())
              )
            )
          }
          .list()
          .apply()
          .toVector
      }
    }
}
