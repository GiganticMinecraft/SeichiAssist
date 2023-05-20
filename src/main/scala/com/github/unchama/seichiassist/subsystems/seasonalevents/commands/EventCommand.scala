package com.github.unchama.seichiassist.subsystems.seasonalevents.commands

import cats.effect.IO
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.subsystems.seasonalevents.anniversary.AnniversaryItemData._
import com.github.unchama.seichiassist.subsystems.seasonalevents.christmas.ChristmasItemData._
import com.github.unchama.seichiassist.subsystems.seasonalevents.halloween.HalloweenItemData._
import com.github.unchama.seichiassist.subsystems.seasonalevents.newyear.NewYearItemData._
import com.github.unchama.seichiassist.subsystems.seasonalevents.valentine.ValentineItemData._
import com.github.unchama.seichiassist.util.InventoryOperations
import com.github.unchama.targetedeffect.TargetedEffect._
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class EventCommand(implicit ioOnMainThread: OnMinecraftServerThread[IO]) {

  import com.github.unchama.targetedeffect._

  val christsmasGrantEffect: TargetedEffect[Player] =
    InventoryOperations.grantItemStacksEffect(
      christmasCake(christmasCakeDefaultPieces),
      christmasTurkey,
      christmasPotion,
      christmasChestPlate,
      christmasSock
    )

  val newYearGrantEffect: TargetedEffect[Player] =
    InventoryOperations.grantItemStacksEffect(newYearApple, newYearBag)

  val halloweenGrantEffect: TargetedEffect[Player] =
    InventoryOperations.grantItemStacksEffect(halloweenPotion, halloweenHoe)

  val anniversaryGrantEffect: TargetedEffect[Player] =
    InventoryOperations.grantItemStacksEffect(
      mineHead,
      strangeSapling,
      mendingBook,
      anniversaryShovel
    )

  val valentineGrantEffect: TargetedEffect[Player] =
    InventoryOperations.grantItemStacksEffect(droppedCookie)

  val executor: TabExecutor = playerCommandBuilder
    .execution { context =>
      val effect = context.args.yetToBeParsed match {
        case "anniversary" :: _ => anniversaryGrantEffect
        case "christmas" :: _   => christsmasGrantEffect
        case "newyear" :: _     => newYearGrantEffect
        case "halloween" :: _   => halloweenGrantEffect
        case "valentine" :: _   => valentineGrantEffect
        case _                  => emptyEffect
      }

      IO.pure(effect)
    }
    .build()
    .asNonBlockingTabExecutor()
}
