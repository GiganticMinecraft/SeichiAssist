package com.github.unchama.seichiassist.subsystems.seasonalevents.commands

import cats.effect.IO
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.subsystems.seasonalevents.christmas.ChristmasItemData._
import com.github.unchama.seichiassist.subsystems.seasonalevents.halloween.HalloweenItemData._
import com.github.unchama.seichiassist.subsystems.seasonalevents.newyear.NewYearItemData._
import com.github.unchama.seichiassist.util.InventoryUtil
import com.github.unchama.targetedeffect.TargetedEffect._
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

object EventCommand {

  import com.github.unchama.targetedeffect._

  val christsmasGrantEffect: TargetedEffect[Player] =
    InventoryUtil.grantItemStacksEffect(
      christmasCake(christmasCakeDefaultPieces),
      christmasTurkey,
      christmasPotion,
      christmasChestPlate,
      christmasPickaxe,
      christmasSock
    )

  val newYearGrantEffect: TargetedEffect[Player] =
    InventoryUtil.grantItemStacksEffect(
      newYearApple,
      newYearBag
    )

  val halloweenGrantEffect: TargetedEffect[Player] =
    InventoryUtil.grantItemStacksEffect(
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