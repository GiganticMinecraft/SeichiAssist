package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.actions

import cats.effect.{ConcurrentEffect, LiftIO}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.mana.domain.ManaAmount
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.FairyAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions.RecoveryMana
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.BukkitFairySpeech
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.{
  AppleAmount,
  FairyAppleConsumeStrategy,
  FairyManaRecoveryState
}
import com.github.unchama.seichiassist.{MineStackObjectList, SeichiAssist}
import com.github.unchama.targetedeffect.SequentialEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffectF
import org.bukkit.ChatColor._
import org.bukkit.entity.Player

import java.time.LocalDateTime
import java.util.UUID
import scala.util.Random

class BukkitRecoveryMana[F[_]: ConcurrentEffect, G[_]: ContextCoercion[*[_], F]](
  player: Player
)(
  implicit breakCountAPI: BreakCountAPI[F, G, Player],
  fairyAPI: FairyAPI[F, G, Player],
  voteAPI: VoteAPI[F, Player],
  manaApi: ManaApi[F, G, Player]
) extends RecoveryMana[F] {

  private val uuid: UUID = player.getUniqueId
  private val playerdata = SeichiAssist.playermap(uuid)

  import cats.implicits._

  override def recovery: F[Unit] =
    for {
      isFairyUsing <- fairyAPI.isFairyUsing(player)
      fairyEndTimeOpt <- fairyAPI.fairyEndTime(player)
      endTime = fairyEndTimeOpt.get.endTimeOpt.get
      _ <- {
        new BukkitFairySpeech[F, G]
          .bye(player) >> fairyAPI.updateIsFairyUsing(player, isFairyUsing = false)
      }.whenA(
        // 終了時間が今よりも過去だったとき(つまり有効時間終了済み)
        isFairyUsing && endTime.isBefore(LocalDateTime.now())
      )
      oldManaAmount <- ContextCoercion {
        manaApi.readManaAmount(player)
      }
      _ <- {
        new BukkitFairySpeech[F, G].speechRandomly(player, FairyManaRecoveryState.Full)
      }.whenA(isFairyUsing && oldManaAmount.isFull)

      appleConsumptionAmount <- computeAppleConsumptionAmount
      finallyAppleConsumptionAmount <- computeFinallyAppleConsumptionAmount(
        appleConsumptionAmount
      )
      recoveryManaAmount <- computeManaRecoveryAmount(appleConsumptionAmount)

      gachaRingoObject <- LiftIO[F].liftIO {
        MineStackObjectList.findByName("gachaimo")
      }
      mineStackedGachaRingoAmount =
        playerdata.minestack.getStackedAmountOf(gachaRingoObject.get)

      _ <- MessageEffectF(s"$RESET$YELLOW${BOLD}MineStackにがちゃりんごがないようです。。。")
        .apply(player)
        .whenA(
          isFairyUsing && !oldManaAmount.isFull && appleConsumptionAmount > mineStackedGachaRingoAmount
        )

      _ <- {
        fairyAPI.increaseAppleAteByFairy(uuid, AppleAmount(finallyAppleConsumptionAmount)) >>
          ContextCoercion(
            manaApi.manaAmount(player).restoreAbsolute(ManaAmount(recoveryManaAmount))
          ) >>
          new BukkitFairySpeech[F, G].speechRandomly(
            player,
            if (finallyAppleConsumptionAmount > mineStackedGachaRingoAmount)
              FairyManaRecoveryState.NotConsumptionApple
            else FairyManaRecoveryState.ConsumptionApple
          ) >>
          SequentialEffect(
            MessageEffectF(s"$RESET$YELLOW${BOLD}マナ妖精が${recoveryManaAmount}マナを回復してくれました"),
            if (appleConsumptionAmount != 0)
              MessageEffectF(
                s"$RESET$YELLOW${BOLD}あっ！${appleConsumptionAmount}個のがちゃりんごが食べられてる！"
              )
            else MessageEffectF(s"$RESET$YELLOW${BOLD}あなたは妖精にりんごを渡しませんでした。")
          ).apply(player)
      }.whenA(isFairyUsing && !oldManaAmount.isFull)
    } yield ()

  /**
   * MineStackに入っているがちゃりんごの数を考慮していない
   * がちゃりんごの消費量を計算します。
   */
  private def computeAppleConsumptionAmount: F[Int] = for {
    seichiAmountData <- ContextCoercion(breakCountAPI.seichiAmountDataRepository(player).read)
    chainVoteNumber <- voteAPI.chainVoteDayNumber(uuid)
    appleOpenState <- fairyAPI.appleOpenState(uuid)
    oldManaAmount <- ContextCoercion {
      manaApi.readManaAmount(player)
    }
  } yield {
    val playerLevel = seichiAmountData.levelCorrespondingToExp

    val isAppleOpenStateIsOpenOrOpenALittle =
      appleOpenState == FairyAppleConsumeStrategy.LessConsume || appleOpenState == FairyAppleConsumeStrategy.Consume
    val isEnoughMana = oldManaAmount.ratioToCap.exists(_ >= 0.75)

    val defaultAmount = Math.pow(playerLevel.level / 10, 2)

    val chainVoteDayNumber = chainVoteNumber.value
    // 連続投票を適用した除算量
    val chainVoteDivisionAmount =
      if (chainVoteDayNumber >= 30) 2
      else if (chainVoteDayNumber >= 10) 1.5
      else if (chainVoteDayNumber >= 3) 1.25
      else 1

    // りんごの開放状況を適用した除算量
    val appleOpenStateDivisionAmount =
      if (isAppleOpenStateIsOpenOrOpenALittle && isEnoughMana) 2
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
    Math.max(appleOpenStateReflectedAmount + amountEatenByKnob, 1)
  }

  /**
   * MineStackに入っているがちゃりんごの数を考慮した
   * がちゃりんごの消費量を計算します。
   */
  private def computeFinallyAppleConsumptionAmount(appleConsumptionAmount: Int): F[Int] = for {
    appleOpenState <- fairyAPI.appleOpenState(uuid)
    gachaRingoObject <- LiftIO[F].liftIO {
      MineStackObjectList.findByName("gachaimo")
    }
  } yield {
    val mineStackedGachaRingoAmount =
      playerdata.minestack.getStackedAmountOf(gachaRingoObject.get)

    // りんごの消費量
    if (appleOpenState == FairyAppleConsumeStrategy.NoConsume)
      0
    else if (mineStackedGachaRingoAmount > appleConsumptionAmount)
      appleConsumptionAmount
    else mineStackedGachaRingoAmount.toInt
  }

  /**
   * マナの回復量を計算します。
   */
  private def computeManaRecoveryAmount(appleConsumptionAmount: Int): F[Int] = for {
    defaultRecoveryManaAmount <- fairyAPI.fairyRecoveryMana(uuid)
    appleOpenState <- fairyAPI.appleOpenState(uuid)
    oldManaAmount <- ContextCoercion {
      manaApi.readManaAmount(player)
    }
    gachaRingoObject <- LiftIO[F].liftIO {
      MineStackObjectList.findByName("gachaimo")
    }
  } yield {
    val isAppleOpenStateIsOpenOrOpenALittle =
      appleOpenState == FairyAppleConsumeStrategy.LessConsume || appleOpenState == FairyAppleConsumeStrategy.Consume
    val isEnoughMana = oldManaAmount.ratioToCap.exists(_ >= 0.75)

    val mineStackedGachaRingoAmount =
      playerdata.minestack.getStackedAmountOf(gachaRingoObject.get)

    // マナの回復量を算出する

    val appleOpenStateDivision =
      if (isAppleOpenStateIsOpenOrOpenALittle && isEnoughMana) 2
      else if (appleOpenState == FairyAppleConsumeStrategy.NoConsume) 4
      else 1

    val reflectedAppleOpenStateAmount =
      defaultRecoveryManaAmount.recoveryMana / appleOpenStateDivision

    // minestackに入っているりんごの数を適用したマナの回復量
    val mineStackBasedRegenValue =
      if (appleConsumptionAmount > mineStackedGachaRingoAmount) {
        if (mineStackedGachaRingoAmount == 0) {
          reflectedAppleOpenStateAmount / (
            if (appleOpenState == FairyAppleConsumeStrategy.Consume) 4
            else if (appleOpenState == FairyAppleConsumeStrategy.LessConsume) 4
            else 2
          )
        } else if ((mineStackedGachaRingoAmount / appleConsumptionAmount) <= 0.5)
          (reflectedAppleOpenStateAmount * 0.5).toInt
        else
          (reflectedAppleOpenStateAmount * mineStackedGachaRingoAmount / appleConsumptionAmount).toInt
      } else reflectedAppleOpenStateAmount

    val randomizedAdd =
      if (mineStackBasedRegenValue >= 50)
        Random.nextInt(mineStackBasedRegenValue / 50)
      else 0

    (mineStackBasedRegenValue - mineStackBasedRegenValue / 100) + randomizedAdd
  }

}