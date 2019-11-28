package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.listener.new_year_event.{NewYearBagListener, NewYearItemListener}
import com.github.unchama.seichiassist.util.Util
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

object EventCommand {
  import com.github.unchama.targetedeffect._

  val grantEffect: TargetedEffect[Player] =
    Util.grantItemStacksEffect(
      NewYearBagListener.getNewYearBag,
      NewYearItemListener.getNewYearApple
    )

  val executor: TabExecutor = playerCommandBuilder
    .execution { context =>
      val effect = context.args.yetToBeParsed match {
        case "get" :: _ => emptyEffect
        case _ => grantEffect
      }

      IO.pure(effect)
    }
    .build()
    .asNonBlockingTabExecutor()
}