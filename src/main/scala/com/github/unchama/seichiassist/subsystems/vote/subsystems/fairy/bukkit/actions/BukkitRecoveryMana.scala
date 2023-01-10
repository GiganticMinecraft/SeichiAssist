package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.actions

import cats.effect.{ConcurrentEffect, Sync}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.mana.domain.ManaAmount
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions.RecoveryMana
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.FairyPersistence
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.{
  AppleAmount,
  FairyAppleConsumeStrategy,
  FairyManaRecoveryState
}
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.speech.FairySpeech
import com.github.unchama.targetedeffect.SequentialEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffectF
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

import java.time.LocalDateTime
import java.util.UUID
import scala.util.Random

class BukkitRecoveryMana[F[_]: ConcurrentEffect, G[_]: ContextCoercion[*[_], F]](
  player: Player,
  fairySpeech: FairySpeech[F, Player]
)(
  implicit breakCountAPI: BreakCountAPI[F, G, Player],
  voteAPI: VoteAPI[F, Player],
  manaApi: ManaApi[F, G, Player],
  fairyPersistence: FairyPersistence[F],
  mineStackAPI: MineStackAPI[F, Player, ItemStack]
) extends RecoveryMana[F] {

  private val uuid: UUID = player.getUniqueId

  import cats.implicits._

  override def recovery: F[Unit] =
    for {
      isFairyUsing <- fairyPersistence.isFairyUsing(uuid)
      fairyEndTimeOpt <- fairyPersistence.fairyEndTime(uuid)
      endTime = fairyEndTimeOpt.get.endTimeOpt.get
      finishUse <- Sync[F]
        .delay(LocalDateTime.now())
        .map(now => isFairyUsing && endTime.isBefore(now))
      _ <- {
        fairySpeech
          .bye(player) >> fairyPersistence.updateIsFairyUsing(uuid, isFairyUsing = false)
      }.whenA(finishUse)
      oldManaAmount <- ContextCoercion {
        manaApi.readManaAmount(player)
      }
      _ <- {
        fairySpeech.speechRandomly(player, FairyManaRecoveryState.Full)
      }.whenA(isFairyUsing && oldManaAmount.isFull)

      appleConsumptionAmount <- computeAppleConsumptionAmount
      finallyAppleConsumptionAmount <- computeFinallyAppleConsumptionAmount(
        appleConsumptionAmount
      )
      recoveryManaAmount <- computeManaRecoveryAmount(appleConsumptionAmount)

      gachaRingoObject <- mineStackAPI.mineStackObjectList.findByName("gachaimo")

      mineStackedGachaRingoAmount <- mineStackAPI
        .mineStackRepository
        .getStackedAmountOf(player, gachaRingoObject.get)

      _ <- MessageEffectF(s"$RESET$YELLOW${BOLD}MineStackにがちゃりんごがないようです。。。")
        .apply(player)
        .whenA(
          isFairyUsing && !oldManaAmount.isFull && appleConsumptionAmount > mineStackedGachaRingoAmount
        )

      _ <- {
        fairyPersistence.increaseConsumedAppleAmountByFairy(
          uuid,
          AppleAmount(finallyAppleConsumptionAmount)
        ) >>
          ContextCoercion(
            manaApi.manaAmount(player).restoreAbsolute(ManaAmount(recoveryManaAmount))
          ) >>
          fairySpeech.speechRandomly(
            player,
            if (finallyAppleConsumptionAmount > mineStackedGachaRingoAmount)
              FairyManaRecoveryState.RecoveredWithoutApple
            else FairyManaRecoveryState.RecoveredWithApple
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
    voteStreaks <- voteAPI.currentConsecutiveVoteStreakDays(uuid)
    appleOpenState <- fairyPersistence.appleConsumeStrategy(uuid)
    oldManaAmount <- ContextCoercion {
      manaApi.readManaAmount(player)
    }
  } yield {
    val playerLevel = seichiAmountData.levelCorrespondingToExp

    val isStrategyConsumeOrLessConsume =
      appleOpenState == FairyAppleConsumeStrategy.LessConsume || appleOpenState == FairyAppleConsumeStrategy.Consume
    val isEnoughMana = oldManaAmount.ratioToCap.exists(_ >= 0.75)

    val defaultAmount = Math.pow(playerLevel.level / 10, 2)

    val voteStreakDays = voteStreaks.value
    // 連続投票を適用した除算量
    val chainVoteDivisor =
      if (voteStreakDays >= 30) 2
      else if (voteStreakDays >= 10) 1.5
      else if (voteStreakDays >= 3) 1.25
      else 1

    // りんごの開放状況を適用した除算量
    val appleConsumeStrategyDivisor =
      if (isStrategyConsumeOrLessConsume && isEnoughMana) 2
      else 1

    // りんごの開放状況まで適用したりんごの消費量 (暫定)
    val appleOpenStateReflectedAmount =
      (defaultAmount / chainVoteDivisor).toInt / appleConsumeStrategyDivisor

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
    appleOpenState <- fairyPersistence.appleConsumeStrategy(uuid)
    gachaRingoObject <- mineStackAPI.mineStackObjectList.findByName("gachaimo")
    mineStackedGachaRingoAmount <- mineStackAPI
      .mineStackRepository
      .getStackedAmountOf(player, gachaRingoObject.get)
  } yield {
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
    defaultRecoveryManaAmount <- fairyPersistence.fairyRecoveryMana(uuid)
    appleOpenState <- fairyPersistence.appleConsumeStrategy(uuid)
    oldManaAmount <- ContextCoercion {
      manaApi.readManaAmount(player)
    }
    gachaRingoObject <- mineStackAPI.mineStackObjectList.findByName("gachaimo")
    mineStackedGachaRingoAmount <- mineStackAPI
      .mineStackRepository
      .getStackedAmountOf(player, gachaRingoObject.get)
  } yield {
    val isAppleOpenStateIsOpenOrOpenALittle =
      appleOpenState == FairyAppleConsumeStrategy.LessConsume || appleOpenState == FairyAppleConsumeStrategy.Consume
    val isEnoughMana = oldManaAmount.ratioToCap.exists(_ >= 0.75)

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
        } else {
          val multiplier = Math.max(mineStackedGachaRingoAmount / appleConsumptionAmount, 0.5)
          (reflectedAppleOpenStateAmount * multiplier).toInt
        }
      } else reflectedAppleOpenStateAmount

    val randomizedAdd =
      if (mineStackBasedRegenValue >= 50)
        Random.nextInt(mineStackBasedRegenValue / 50)
      else 0

    (mineStackBasedRegenValue - mineStackBasedRegenValue / 100) + randomizedAdd
  }

}
