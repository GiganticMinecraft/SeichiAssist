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
import scala.util.Random

class BukkitRecoveryMana[F[_]: ConcurrentEffect: JavaTime, G[_]: ContextCoercion[*[_], F]](
  player: Player,
  fairySpeech: FairySpeech[F, Player]
)(
  implicit manaApi: ManaApi[F, G, Player],
  fairyPersistence: FairyPersistence[F],
  mineStackAPI: MineStackAPI[F, Player, ItemStack]
) extends RecoveryMana[F] {

  private val uuid: UUID = player.getUniqueId

  import cats.implicits._

  override def recovery(counter: Int): F[Unit] =
    for {
      isFairyUsing <- fairyPersistence.isFairyUsing(uuid)
      fairyEndTimeOpt <- fairyPersistence.fairyEndTime(uuid)
      consumeStrategy <- fairyPersistence.appleConsumeStrategy(uuid)
      isRecoverTiming = consumeStrategy match {
        case FairyAppleConsumeStrategy.Permissible                 => true
        case FairyAppleConsumeStrategy.Consume if counter == 1     => true
        case FairyAppleConsumeStrategy.LessConsume if counter == 2 => true
        case FairyAppleConsumeStrategy.NoConsume if counter == 3   => true
        case _                                                     => false
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
      appleConsumeAmount <- Sync[F].delay(
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

        if (random <= 0.03) appleConsumeAmount * 0.3
        else 0
      }

      recoveryManaAmount <- Sync[F].delay(defaultRecoveryMana.recoveryMana * 0.7 + bonusRecoveryAmount)

      _ <- {
        fairyPersistence.increaseConsumedAppleAmountByFairy(
          uuid,
          AppleAmount(appleConsumeAmount)
        ) >>
          ContextCoercion(
            manaApi.manaAmount(player).restoreAbsolute(ManaAmount(recoveryManaAmount))
          ) >>
          fairySpeech.speechRandomly(
            player,
            if (appleConsumeAmount == 0)
              FairyManaRecoveryState.RecoveredWithoutApple
            else FairyManaRecoveryState.RecoveredWithApple
          ) >>
          mineStackAPI
            .mineStackRepository
            .subtractStackedAmountOf(player, gachaRingoObject.get, appleConsumeAmount) >>
          SequentialEffect(
            MessageEffectF(s"$RESET$YELLOW${BOLD}マナ妖精が${Math.floor(recoveryManaAmount)}マナを回復してくれました"),
            if (appleConsumeAmount != 0)
              MessageEffectF(s"$RESET$YELLOW${BOLD}あっ！${appleConsumeAmount}個のがちゃりんごが食べられてる！")
            else MessageEffectF(s"$RESET$YELLOW${BOLD}あなたは妖精にりんごを渡しませんでした。")
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
