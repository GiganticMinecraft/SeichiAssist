package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.actions

import cats.effect.{ConcurrentEffect, IO, SyncIO}
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.mana.domain.ManaAmount
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.FairyAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions.RecoveryMana
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.FairySpeech
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.{
  AppleOpenState,
  FairyManaRecoveryState
}
import com.github.unchama.seichiassist.{MineStackObjectList, SeichiAssist}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.{SequentialEffect, UnfocusedEffect}
import org.bukkit.ChatColor._
import org.bukkit.entity.Player

import scala.util.Random

object BukkitRecoveryMana {

  def apply(player: Player)(
    implicit breakCountAPI: BreakCountAPI[IO, SyncIO, Player],
    fairyAPI: FairyAPI[IO, Player],
    voteAPI: VoteAPI[IO, Player],
    manaApi: ManaApi[IO, SyncIO, Player],
    concurrentEffect: ConcurrentEffect[IO]
  ): RecoveryMana[IO] = new RecoveryMana[IO] {
    override def recovery: IO[Unit] =
      manaApi
        .readManaAmount(player)
        .map { oldManaAmount =>
          if (oldManaAmount.isFull)
            new FairySpeech[IO]
              .speechRandomly(player, FairyManaRecoveryState.full)
              .unsafeRunSync()
          else {
            val uuid = player.getUniqueId

            val eff = for {
              seichiAmountData <- breakCountAPI.seichiAmountDataRepository(player).read.toIO
              defaultRecoveryManaAmount <- fairyAPI.fairyRecoveryMana(uuid)
              chainVoteNumber <- voteAPI.chainVoteDayNumber(uuid)
              appleOpenState <- fairyAPI.appleOpenState(uuid)
              gachaimoObjectOpt <- MineStackObjectList.findByName("gachaimo")
            } yield {
              val playerLevel = seichiAmountData.levelCorrespondingToExp

              val playerdata = SeichiAssist.playermap(player.getUniqueId)
              val gachaRingoObject = gachaimoObjectOpt.get
              val mineStackedGachaRingoAmount =
                playerdata.minestack.getStackedAmountOf(gachaRingoObject)

              val isAppleOpenStateIsOpenOROpenALittle =
                appleOpenState == AppleOpenState.OpenALittle || appleOpenState == AppleOpenState.Open
              val isManaExistsSeventyFivePercent = oldManaAmount.ratioToCap.exists(_ >= 0.75)

              val defaultAmount = Math.pow(playerLevel.level / 10, 2)

              val chainVoteDayNumber = chainVoteNumber.value
              // 連続投票を適用した除算量
              val chainVoteDivisionAmount =
                if (chainVoteDayNumber >= 30) 2
                else if (chainVoteDayNumber >= 10) 1.2
                else if (chainVoteDayNumber >= 3) 1.25
                else 1

              // りんごの開放状況を適用した除算量
              val appleOpenStateDivisionAmount =
                if (isAppleOpenStateIsOpenOROpenALittle && isManaExistsSeventyFivePercent) 2
                else 1

              // りんごの開放状況まで適用したりんごの消費量 (暫定)
              val appleOpenStateReflectedAmount =
                (defaultAmount / chainVoteDivisionAmount).toInt / appleOpenStateDivisionAmount

              // 妖精がつまみ食いする量
              val amountEatenByKnob =
                if (appleOpenStateReflectedAmount >= 10)
                  new Random().nextInt(appleOpenStateReflectedAmount / 10)
                else 0

              /*
                 最終的に算出されたりんごの消費量
                  (現時点では持っているりんごの数を
                  考慮していないので消費量は確定していない)
               */
              val finallyAppleConsumptionAmount =
                Math.max(appleOpenStateReflectedAmount + amountEatenByKnob, 1)

              // りんごの消費量
              val appleConsumptionAmount =
                if (appleOpenState == AppleOpenState.NotOpen) {
                  0
                } else {
                  Math.max(finallyAppleConsumptionAmount, mineStackedGachaRingoAmount)
                }

              // マナの回復量を算出する
              val recoveryManaAmount = {
                val appleOpenStateDivision = {
                  if (isAppleOpenStateIsOpenOROpenALittle && isManaExistsSeventyFivePercent) 2
                  else if (appleOpenState == AppleOpenState.NotOpen) 4
                  else 1
                }

                val reflectedAppleOpenStateAmount =
                  defaultRecoveryManaAmount.recoveryMana / appleOpenStateDivision

                // minestackに入っているりんごの数を適用したマナの回復量
                val reflectedMineStackedAmount =
                  if (finallyAppleConsumptionAmount > mineStackedGachaRingoAmount) {
                    if (mineStackedGachaRingoAmount == 0) {
                      reflectedAppleOpenStateAmount / {
                        if (appleOpenState == AppleOpenState.Open) 4
                        else if (appleOpenState == AppleOpenState.OpenALittle) 4
                        else 2
                      }
                    } else {
                      if ((mineStackedGachaRingoAmount / finallyAppleConsumptionAmount) <= 0.5)
                        (reflectedAppleOpenStateAmount * 0.5).toInt
                      else
                        (reflectedAppleOpenStateAmount * mineStackedGachaRingoAmount / finallyAppleConsumptionAmount).toInt
                    }
                  } else reflectedAppleOpenStateAmount

                (reflectedMineStackedAmount - reflectedMineStackedAmount / 100) +
                  (if (reflectedMineStackedAmount >= 50)
                     Random.nextInt(reflectedMineStackedAmount / 50)
                   else 0)
              }

              // minestackからりんごを消費する
              playerdata
                .minestack
                .subtractStackedAmountOf(gachaRingoObject, appleConsumptionAmount)

              // マナを回復する
              manaApi
                .manaAmount(player)
                .restoreAbsolute(ManaAmount(recoveryManaAmount))
                .unsafeRunSync()

              if (finallyAppleConsumptionAmount > mineStackedGachaRingoAmount) {
                MessageEffect(s"$RESET$YELLOW${BOLD}MineStackにがちゃりんごがないようです。。。")
                  .apply(player)
                  .unsafeRunSync()
              }

              SequentialEffect(
                UnfocusedEffect {
                  new FairySpeech[IO]
                    .speechRandomly(
                      player,
                      if (finallyAppleConsumptionAmount > mineStackedGachaRingoAmount)
                        FairyManaRecoveryState.notConsumptionApple
                      else FairyManaRecoveryState.consumptionApple
                    )
                    .unsafeRunSync()
                },
                MessageEffect(s"$RESET$YELLOW${BOLD}マナ妖精が${recoveryManaAmount}マナを回復してくれました"),
                if (appleConsumptionAmount != 0)
                  MessageEffect(
                    s"$RESET$YELLOW${BOLD}あっ！${appleConsumptionAmount}個のがちゃりんごが食べられてる！"
                  )
                else MessageEffect(s"$RESET$YELLOW${BOLD}あなたは妖精にりんごを渡しませんでした。")
              ).apply(player).unsafeRunSync()
            }
            eff.unsafeRunAsyncAndForget()
          }
        }
        .toIO

  }

}
