package com.github.unchama.seichiassist.subsystems.halfhourranking.application

import cats.{Functor, Monad}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.actions.{BroadcastMinecraftMessage, SendMinecraftMessage}
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiExpAmount
import com.github.unchama.seichiassist.subsystems.halfhourranking.domain.RankingRecord
import org.bukkit.ChatColor._

object AnnounceRankingRecord {

  import cats.implicits._

  def apply[F[_]: Monad: SendMinecraftMessage[*[_], Player]: BroadcastMinecraftMessage, G[
    _
  ]: ContextCoercion[*[_], F]: Functor, Player: HasUuid](
    breakCountReadApi: BreakCountReadAPI[F, G, Player]
  )(resolveName: Player => F[String]): RankingRecord[Player] => F[Unit] = { rankingRecord =>
    val rankingPositionColor = List(LIGHT_PURPLE, YELLOW, AQUA)
    val sortedNonzeroRecords = rankingRecord.getSortedNonzeroRecords

    val totalBreakCount =
      sortedNonzeroRecords.map(_._2).foldLeft(SeichiExpAmount.zero)(_.add(_))

    val individualAnnouncements =
      sortedNonzeroRecords.map {
        case (player, seichiExpAmount) =>
          SendMinecraftMessage[F, Player]
            .string(player, s"あなたの整地量は $AQUA${seichiExpAmount.formatted}$WHITE でした")
      }

    val rankingAnnouncement = sortedNonzeroRecords.zip(rankingPositionColor).zipWithIndex.map {
      case (((player, seichiExpAmount), decorationColorCode), index) =>
        val position = index + 1
        val increaseAmountText = s"$AQUA${seichiExpAmount.formatted}$WHITE"

        for {
          name <- resolveName(player)
          seichiExpAmountData <-
            ContextCoercion {
              breakCountReadApi
                .seichiAmountDataRepository
                .lift(player)
                .map[G[Option[SeichiAmountData]]](_.read.map(Some(_)))
                .getOrElse {
                  breakCountReadApi
                    .persistedSeichiAmountDataRepository(HasUuid[Player].of(player))
                    .read
                }
            }
          playerNameText = seichiExpAmountData match {
            case Some(data) =>
              val level = data.levelCorrespondingToExp.level
              val starLevel = data.starLevelCorrespondingToExp.level

              if (starLevel != 0)
                s"$decorationColorCode[Lv$level☆$starLevel] $name$WHITE"
              else
                s"$decorationColorCode[Lv$level] $name$WHITE"
            case None =>
              s"$decorationColorCode$name$WHITE"
          }
          _ <- BroadcastMinecraftMessage[F]
            .string(s"整地量第${position}位は${playerNameText}で、整地量は${increaseAmountText}でした")
        } yield ()
    }

    val actions = List(
      BroadcastMinecraftMessage[F].string("--------------30分間整地ランキング--------------")
    ) ++ individualAnnouncements ++ List(
      BroadcastMinecraftMessage[F]
        .string(s"全体の整地量は $AQUA${totalBreakCount.formatted}$WHITE でした")
    ) ++ rankingAnnouncement ++ List(
      BroadcastMinecraftMessage[F].string("--------------------------------------------------")
    )

    actions.sequence.as(())
  }

}
