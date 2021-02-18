package com.github.unchama.seichiassist.task

import cats.effect.IO
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.timer
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.targetedeffect.{DelayEffect, SequentialEffect, TargetedEffect, UnfocusedEffect}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.auto._

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

object PlayerDataCoolDownResetEffects {
  def forSharedInventory(delayTick: Int Refined Positive, pd: PlayerData): IO[TargetedEffect[Any]] = IO {
    SequentialEffect(
      delayEffectInTick(delayTick),
      UnfocusedEffect {
        pd.coolingDownForSharedInventory = false
      }
    )
  }

  def forGacha(delayTick: Int Refined Positive, pd: PlayerData): IO[TargetedEffect[Any]] = IO {
    SequentialEffect(
      delayEffectInTick(delayTick),
      UnfocusedEffect {
        pd.coolingDownForGacha = false
      }
    )
  }

  def forVote(delayTick: Int Refined Positive, pd: PlayerData): IO[TargetedEffect[Any]] = IO {
    SequentialEffect(
      delayEffectInTick(delayTick),
      UnfocusedEffect {
        pd.coolingDownForVotePrize = false
      }
    )
  }

  private def delayEffectInTick(tick: Int Refined Positive) =
    DelayEffect(FiniteDuration(tick * 20, TimeUnit.MILLISECONDS))
}
