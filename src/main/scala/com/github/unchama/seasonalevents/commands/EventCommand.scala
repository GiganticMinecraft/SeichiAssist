package com.github.unchama.seasonalevents.commands

import cats.effect.IO
import com.github.unchama.seasonalevents.christmas.ChristmasItemData._
import com.github.unchama.seasonalevents.halloween.HalloweenItemData._
import com.github.unchama.seasonalevents.newyear.NewYearItemData._
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.targetedeffect.TargetedEffect._
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

object EventCommand {

  import com.github.unchama.targetedeffect._

  val christsmasGrantEffect: TargetedEffect[Player] =
    Util.grantItemStacksEffect(
      christmasCake(christmasCakeDefaultPieces),
      christmasTurkey,
      christmasPotion,
      christmasChestPlate,
      christmasPickaxe,
      christmasSock
    )

  val newYearGrantEffect: TargetedEffect[Player] =
    Util.grantItemStacksEffect(
      newYearApple,
      newYearBag
    )

  val halloweenGrantEffect: TargetedEffect[Player] =
    Util.grantItemStacksEffect(
      halloweenPotion,
      halloweenHoe
    )

  val executor: TabExecutor = playerCommandBuilder
    .execution { context =>
      val effect = context.args.yetToBeParsed match {
        case "christmas" :: _ => christsmasGrantEffect
        case "newyear" :: _ => newYearGrantEffect
        case "halloween" :: _ => halloweenGrantEffect
        case _ => emptyEffect
      }

      IO.pure(effect)
    }
    .build()
    .asNonBlockingTabExecutor()
}