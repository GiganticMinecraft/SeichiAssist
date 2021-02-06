package com.github.unchama.seichiassist.subsystems.halfhourranking.application

import cats.Monad
import com.github.unchama.minecraft.actions.{BroadcastMinecraftMessage, SendMinecraftMessage}
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiExpAmount
import com.github.unchama.seichiassist.subsystems.halfhourranking.domain.RankingRecord
import org.bukkit.ChatColor._

object AnnounceRankingRecord {

  import cats.implicits._

  def apply[
    F[_]
    : Monad
    : SendMinecraftMessage[*[_], Player]
    : BroadcastMinecraftMessage,
    Player: HasUuid
  ](resolveName: Player => F[String]): RankingRecord[Player] => F[Unit] = { rankingRecord =>
    val rankingPositionColor = List(DARK_PURPLE, BLUE, DARK_AQUA)
    val sortedNonzeroRecords = rankingRecord.getSortedNonzeroRecords

    val totalBreakCount = sortedNonzeroRecords.map(_._2).foldLeft(SeichiExpAmount.zero)(_.add(_))

    val individualAnnouncements =
      sortedNonzeroRecords.map { case (player, SeichiExpAmount(amount)) =>
        SendMinecraftMessage[F, Player].string(
          player,
          s"あなたの整地量は $AQUA$amount$WHITE でした"
        )
      }

    val rankingAnnouncement = sortedNonzeroRecords
      .zip(rankingPositionColor)
      .zipWithIndex
      .map { case (((player, SeichiExpAmount(amount)), decorationColorCode), index) =>
        val position = index + 1
        val increaseAmountText = s"$AQUA$amount$WHITE"

        resolveName(player).flatMap { name =>
          val playerNameText = s"$decorationColorCode$name$WHITE"
          BroadcastMinecraftMessage[F].string(
            s"整地量第${position}位は${playerNameText}で、整地量は${increaseAmountText}でした"
          )
        }
      }

    val actions = List(
      BroadcastMinecraftMessage[F].string(
        "--------------30分間整地ランキング--------------"
      )
    ) ++ individualAnnouncements ++ List(
      BroadcastMinecraftMessage[F].string(
        s"全体の整地量は $AQUA${totalBreakCount.amount}$WHITE でした"
      )
    ) ++ rankingAnnouncement ++ List(
      BroadcastMinecraftMessage[F].string(
        "--------------------------------------------------"
      )
    )

    actions.sequence.as(())
  }

}
