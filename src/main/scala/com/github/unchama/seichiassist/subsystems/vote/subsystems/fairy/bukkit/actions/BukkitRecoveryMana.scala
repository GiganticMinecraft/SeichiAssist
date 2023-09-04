package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.actions

import cats.effect.{ConcurrentEffect, Sync}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.mana.domain.ManaAmount
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
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
import io.chrisdavenport.cats.effect.time.JavaTime
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

import java.time.ZoneId
import java.util.UUID
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.Random

class BukkitRecoveryMana[F[_]: ConcurrentEffect: JavaTime, G[_]: ContextCoercion[*[_], F]](
  player: Player,
  fairySpeech: FairySpeech[F, Player]
)(
  implicit manaApi: ManaApi[F, G, Player],
  fairyPersistence: FairyPersistence[F],
  mineStackAPI: MineStackAPI[F, Player, ItemStack]
) extends RecoveryMana[F] {

  import cats.implicits._

  override def recovery(consumptionPeriod: FiniteDuration): F[Unit] = {
    val uuid: UUID = player.getUniqueId
    for {
      isFairyUsing <- fairyPersistence.isFairyUsing(uuid)
      fairyEndTimeOpt <- fairyPersistence.fairyEndTime(uuid)
      consumeStrategy <- fairyPersistence.appleConsumeStrategy(uuid)
      isRecoverTiming = consumeStrategy match {
        case FairyAppleConsumeStrategy.Permissible                               => true
        case FairyAppleConsumeStrategy.Consume if consumptionPeriod == 1.minutes => true
        case FairyAppleConsumeStrategy.LessConsume
            if consumptionPeriod == 1.minutes + 30.seconds =>
          true
        case FairyAppleConsumeStrategy.NoConsume if consumptionPeriod == 2.minutes => true
        case _                                                                     => false
      }
      nonRecoveredManaAmount <- ContextCoercion {
        manaApi.readManaAmount(player)
      }
      _ <- {
        fairySpeech.speechRandomly(player, FairyManaRecoveryState.Full)
      }.whenA(isFairyUsing && isRecoverTiming && nonRecoveredManaAmount.isFull)

      gachaRingoObject <- mineStackAPI.mineStackObjectList.findByName("gachaimo")

      mineStackedGachaRingoAmount <- mineStackAPI
        .mineStackRepository
        .getStackedAmountOf(player, gachaRingoObject.get)

      defaultRecoveryMana <- fairyPersistence.fairyRecoveryMana(uuid)
      // MineStackに入っているガチャりんごの数を考慮していないりんごの消費量
      pureAppleConsumeAmount <- Sync[F].delay(defaultRecoveryMana.recoveryMana / 300)
      // MineStackに入っているガチャりんごの数を考慮したりんごの消費量
      appleConsumeAmountFromMineStack <- Sync[F].delay(
        Math.min(pureAppleConsumeAmount, mineStackedGachaRingoAmount).toInt
      )

      _ <- MessageEffectF(s"$RESET$YELLOW${BOLD}MineStackにがちゃりんごがないようです。。。")
        .apply(player)
        .whenA(
          isFairyUsing && isRecoverTiming && !nonRecoveredManaAmount.isFull && pureAppleConsumeAmount > mineStackedGachaRingoAmount
        )

      // NOTE: 3%の確率で最大の回復量まで回復する
      bonusRecoveryAmount <- Sync[F].delay {
        val random = new Random().nextDouble()

        if (random <= 0.03) appleConsumeAmountFromMineStack * 0.3
        else 0
      }

      recoveryManaAmount <- Sync[F].pure(defaultRecoveryMana.recoveryMana * 0.7 + bonusRecoveryAmount)

      recoveryManaAmountInMinedGachaRingo <- Sync[F].delay(
        recoveryManaAmount * (appleConsumeAmountFromMineStack.toDouble / pureAppleConsumeAmount)
      )

      manaRecoveryState <- Sync[F].delay {
        // NOTE: recoveryManaAmountが300を下回ると、がちゃりんごを一つも消費しないが、
        //       りんごを消費できなかったときと同じ処理を行うと仕様として紛らわしいので、
        //       回復量が300未満だった場合はりんごを消費して回復したことにする
        if (recoveryManaAmount == 0 && recoveryManaAmountInMinedGachaRingo < 300)
          FairyManaRecoveryState.RecoverWithoutAppleButLessThanAApple
        else if (recoveryManaAmountInMinedGachaRingo == 0)
          FairyManaRecoveryState.RecoveredWithoutApple
        else FairyManaRecoveryState.RecoveredWithApple
      }

      _ <- {
        fairyPersistence.increaseConsumedAppleAmountByFairy(
          uuid,
          AppleAmount(appleConsumeAmountFromMineStack)
        ) >>
          ContextCoercion(
            manaApi.manaAmount(player).restoreAbsolute(ManaAmount(recoveryManaAmountInMinedGachaRingo))
          ) >>
          fairySpeech.speechRandomly(player, manaRecoveryState) >>
          mineStackAPI
            .mineStackRepository
            .subtractStackedAmountOf(player, gachaRingoObject.get, appleConsumeAmountFromMineStack) >>
          SequentialEffect(
            MessageEffectF(
              s"$RESET$YELLOW${BOLD}マナ妖精が${Math.floor(recoveryManaAmountInMinedGachaRingo)}マナを回復してくれました"
            ),
            manaRecoveryState match {
              case FairyManaRecoveryState.RecoverWithoutAppleButLessThanAApple =>
                MessageEffectF(
                  s"$RESET$YELLOW${BOLD}回復量ががちゃりんご１つ分に満たなかったため、あなたは妖精にりんごを渡しませんでした。"
                )
              case FairyManaRecoveryState.RecoveredWithApple =>
                MessageEffectF(s"$RESET$YELLOW${BOLD}あっ！${appleConsumeAmountFromMineStack}個のがちゃりんごが食べられてる！")
              case _ =>
                MessageEffectF(s"$RESET$YELLOW${BOLD}あなたは妖精にりんごを渡しませんでした。")
            }
          ).apply(player)
      }.whenA(isFairyUsing && isRecoverTiming && !nonRecoveredManaAmount.isFull)
      finishUse <- JavaTime[F]
        .getLocalDateTime(ZoneId.systemDefault())
        .map(now => isFairyUsing && fairyEndTimeOpt.exists(_.endTime.isBefore(now)))
      _ <- {
        fairySpeech
          .bye(player) >> fairyPersistence.updateIsFairyUsing(uuid, isFairyUsing = false)
      }.whenA(finishUse)
    } yield ()
  }

}
