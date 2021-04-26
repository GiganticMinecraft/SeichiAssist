package com.github.unchama.seichiassist.subsystems.loginranking.inftastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.BuildExpAmount
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountData
import com.github.unchama.seichiassist.subsystems.loginranking.domain
import com.github.unchama.seichiassist.subsystems.loginranking.domain.{LoginTime, LoginTimeRankingRecord}
import com.github.unchama.seichiassist.subsystems.ranking.domain.RankingRecordPersistence
import scalikejdbc._

class JdbcReadAllLoginTimeRankingRecord[F[_] : Sync] extends RankingRecordPersistence[F, LoginTimeRankingRecord] {
  override def getAllRankingRecords: F[Vector[LoginTimeRankingRecord]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"""SELECT name,playtick from playerdata"""
        .map { rs =>
          LoginTimeRankingRecord(
            rs.string("name"),
            LoginTime(rs.int("playtick"))
          )
        }
        .list()
        .apply()
        .toVector
    }
  }
}
