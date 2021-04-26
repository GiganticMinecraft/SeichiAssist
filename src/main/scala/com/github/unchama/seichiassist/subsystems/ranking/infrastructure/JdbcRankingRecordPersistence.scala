package com.github.unchama.seichiassist.subsystems.ranking.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiExpAmount
import com.github.unchama.seichiassist.subsystems.ranking.domain.{RankingRecordPersistence, SeichiRankingRecord}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

class JdbcRankingRecordPersistence[F[_] : Sync] extends RankingRecordPersistence[F, SeichiRankingRecord] {

  override def getAllRankingRecords: F[Vector[SeichiRankingRecord]] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"""
        select name, totalbreaknum from playerdata
      """.stripMargin
        .map { rs =>
          SeichiRankingRecord(
            rs.string("name"),
            SeichiAmountData(SeichiExpAmount.ofNonNegative(rs.bigInt("totalbreaknum").longValueExact()))
          )
        }
        .list()
        .apply()
        .toVector
    }
  }
}
