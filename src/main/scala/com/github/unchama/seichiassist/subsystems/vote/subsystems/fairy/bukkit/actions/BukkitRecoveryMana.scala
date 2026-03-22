package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.actions

import cats.effect.{ConcurrentEffect, Sync}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.mana.domain.ManaAmount
import com.github.unchama.seichiassist.subsystems.dragonnighttime.DragonNightTimeApi
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions.RecoveryMana
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.{
  FairyManaRecovery,
  FairyPersistence
}
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.{
  AppleAmount,
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
import scala.concurrent.duration.FiniteDuration
import scala.util.Random

class BukkitRecoveryMana[F[_]: ConcurrentEffect: JavaTime, G[_]: ContextCoercion[*[_], F]](
  player: Player,
  fairySpeech: FairySpeech[F, Player]
)(
  implicit manaApi: ManaApi[F, G, Player],
  fairyPersistence: FairyPersistence[F],
  mineStackAPI: MineStackAPI[F, Player, ItemStack],
  dragonNightTimeApi: DragonNightTimeApi
) extends RecoveryMana[F] {

  import cats.implicits._

  override def recovery(consumptionPeriod: FiniteDuration): F[Unit] = {
    for {
      uuid <- Sync[F].delay(player.getUniqueId)
      isFairyUsing <- fairyPersistence.isFairyUsing(uuid)
      fairyEndTimeOpt <- fairyPersistence.fairyEndTime(uuid)
      consumeStrategy <- fairyPersistence.appleConsumeStrategy(uuid)
      isRecoverTiming = consumeStrategy.isRecoveryTiming(consumptionPeriod)
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

      defaultRecoveryMana <- fairyPersistence.fairyBaseRecoveryMana(uuid)

      _ <- MessageEffectF(s"$RESET$YELLOW${BOLD}MineStackにがちゃりんごがないようです。。。")
        .apply(player)
        .whenA(
          isFairyUsing && isRecoverTiming && !nonRecoveredManaAmount.isFull && defaultRecoveryMana.amount / 300 > mineStackedGachaRingoAmount
        )

      bonusRoll <- Sync[F].delay(new Random().nextDouble())
      now <- JavaTime[F].getLocalDateTime(ZoneId.systemDefault())
      isDragonNightTime = dragonNightTimeApi.isInDragonNightTime(now)
      result = FairyManaRecovery.compute(
        defaultRecoveryMana,
        mineStackedGachaRingoAmount,
        bonusRoll,
        isDragonNightTime
      )

      _ <- {
        fairyPersistence.increaseConsumedAppleAmountByFairy(
          uuid,
          AppleAmount(result.consumedGachaAppleCount)
        ) >>
          ContextCoercion(
            manaApi.manaAmount(player).restoreAbsolute(ManaAmount(result.finalRecoveredMana))
          ) >>
          fairySpeech.speechRandomly(player, result.state) >>
          mineStackAPI
            .mineStackRepository
            .subtractStackedAmountOf(
              player,
              gachaRingoObject.get,
              result.consumedGachaAppleCount
            ) >>
          SequentialEffect(
            MessageEffectF(
              s"$RESET$YELLOW${BOLD}マナ妖精が${Math.floor(result.finalRecoveredMana)}マナを回復してくれました"
            ),
            result.state match {
              case FairyManaRecoveryState.RecoverWithoutAppleButLessThanAApple =>
                MessageEffectF(
                  s"$RESET$YELLOW${BOLD}回復量ががちゃりんご１つ分に満たなかったため、あなたは妖精にりんごを渡しませんでした。"
                )
              case FairyManaRecoveryState.RecoveredWithApple =>
                MessageEffectF(
                  s"$RESET$YELLOW${BOLD}あっ！${result.consumedGachaAppleCount}個のがちゃりんごが食べられてる！"
                )
              case _ =>
                MessageEffectF(s"$RESET$YELLOW${BOLD}あなたは妖精にりんごを渡しませんでした。")
            }
          ).apply(player)
      }.whenA(isFairyUsing && isRecoverTiming && !nonRecoveredManaAmount.isFull)
      isFairyTimeEnded = fairyEndTimeOpt.exists(_.endTime.isBefore(now))
      finishUse = isFairyUsing && isFairyTimeEnded
      _ <- {
        fairySpeech
          .bye(player) >> fairyPersistence.updateIsFairyUsing(uuid, isFairyUsing = false)
      }.whenA(finishUse)
    } yield ()
  }

}
