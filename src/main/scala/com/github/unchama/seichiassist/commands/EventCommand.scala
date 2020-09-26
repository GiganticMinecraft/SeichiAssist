package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.data.HalloweenItemData
import com.github.unchama.seichiassist.listener.new_year_event.{NewYearBagListener, NewYearItemListener}
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.targetedeffect.TargetedEffect._
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

object EventCommand {
  import com.github.unchama.targetedeffect._

  val newYearGrantEffect: TargetedEffect[Player] =
    Util.grantItemStacksEffect(
      NewYearBagListener.getNewYearBag,
      NewYearItemListener.getNewYearApple
    )

  val halloweenGrantEffect: TargetedEffect[Player] =
    Util.grantItemStacksEffect(
      HalloweenItemData.getHalloweenPotion
    )

  val executor: TabExecutor = playerCommandBuilder
    .execution { context =>
      val effect = context.args.yetToBeParsed match {
        case "newyear" :: _ => newYearGrantEffect
        case "halloween" :: _ => halloweenGrantEffect
        case _ => emptyEffect
      }

      IO.pure(effect)
    }
    .build()
    .asNonBlockingTabExecutor()
}