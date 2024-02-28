package com.github.unchama.seichiassist.subsystems.seasonalevents.commands

import cats.effect.IO
import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.subsystems.playerheadskin.PlayerHeadSkinAPI
import com.github.unchama.seichiassist.subsystems.seasonalevents.anniversary.AnniversaryItemData._
import com.github.unchama.seichiassist.subsystems.seasonalevents.christmas.ChristmasItemData._
import com.github.unchama.seichiassist.subsystems.seasonalevents.halloween.HalloweenItemData._
import com.github.unchama.seichiassist.subsystems.seasonalevents.newyear.NewYearItemData._
import com.github.unchama.seichiassist.subsystems.seasonalevents.valentine.ValentineItemData._
import com.github.unchama.seichiassist.util.InventoryOperations
import com.github.unchama.targetedeffect.TargetedEffect._
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class EventCommand(
  implicit ioOnMainThread: OnMinecraftServerThread[IO],
  playerHeadSkinAPI: PlayerHeadSkinAPI[IO, Player]
) {

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
    .thenParse(Parsers.identity)
    .buildWith { context =>
      val effect = context.args.parsed.head match {
        case "anniversary" => anniversaryGrantEffect
        case "christmas"   => christsmasGrantEffect
        case "newyear"     => newYearGrantEffect
        case "halloween"   => halloweenGrantEffect
        case "valentine"   => valentineGrantEffect
        case _             => emptyEffect
      }

      IO.pure(effect)
    }
    .asNonBlockingTabExecutor()
}
