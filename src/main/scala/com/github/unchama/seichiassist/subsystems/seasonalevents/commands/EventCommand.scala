package com.github.unchama.seichiassist.subsystems.seasonalevents.commands

import cats.effect.IO
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.subsystems.seasonalevents.anniversary.AnniversaryItemData._
import com.github.unchama.seichiassist.subsystems.seasonalevents.christmas.ChristmasItemData._
import com.github.unchama.seichiassist.subsystems.seasonalevents.halloween.HalloweenItemData._
import com.github.unchama.seichiassist.subsystems.seasonalevents.newyear.NewYearItemData._
import com.github.unchama.seichiassist.subsystems.seasonalevents.valentine.ValentineItemData.cookieOf
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.targetedeffect.TargetedEffect._
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

import java.util.UUID

class EventCommand(implicit ioOnMainThread: OnMinecraftServerThread[IO]) {

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

  val anniversaryGrantEffect: TargetedEffect[Player] =
    Util.grantItemStacksEffect(
      mineHead,
      strangeSapling,
      mendingBook,
      anniversaryShovel
    )

  def valentineGrantEffect: TargetedEffect[Player] =
    Util.grantItemStacksEffect(cookieOf("kinton", UUID.fromString("85dd5867-db09-4a2f-bae7-8d38d5a9c547")))

  val executor: TabExecutor = playerCommandBuilder
    .execution { context =>
      val effect = context.args.yetToBeParsed match {
        case "anniversary" :: _ => anniversaryGrantEffect
        case "christmas" :: _ => christsmasGrantEffect
        case "newyear" :: _ => newYearGrantEffect
        case "halloween" :: _ => halloweenGrantEffect
        case "valentine" :: _ => valentineGrantEffect
        case _ => emptyEffect
      }

      IO.pure(effect)
    }
    .build()
    .asNonBlockingTabExecutor()
}