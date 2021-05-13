package com.github.unchama.seichiassist.subsystems.ranking.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiExpAmount
import com.github.unchama.seichiassist.subsystems.ranking.domain.{GenericRankingRecordPersistence, RankingRecord}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

class JdbcSeichiRankingRecordPersistence[F[_] : Sync] extends GenericRankingRecordPersistence[F, SeichiAmountData] {

  override def getAllRankingRecords: F[Vector[RankingRecord[SeichiAmountData]]] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"select name, totalbreaknum from playerdata"
        .map { rs =>
          RankingRecord(
            rs.string("name"),
            SeichiAmountData(SeichiExpAmount.ofNonNegative(rs.bigInt("totalbreaknum").longValueExact()))
          )
        }
        .list().apply().toVector
    }
  }
}
