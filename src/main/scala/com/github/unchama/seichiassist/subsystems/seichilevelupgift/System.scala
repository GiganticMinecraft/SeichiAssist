package com.github.unchama.seichiassist.subsystems.seichilevelupgift

import cats.data.Kleisli
import cats.effect.IO
import com.github.unchama.generic.Diff
import com.github.unchama.generic.algebra.typeclasses.HasSuccessor
import com.github.unchama.seichiassist.commands.legacy.GachaCommand
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiLevel
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.bukkit.GiftItemInterpreter
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.{Gift, GiftBundleTable, GiftInterpreter}
import org.bukkit.entity.Player

object System {

  import cats.implicits._

  private val interpreter: GiftInterpreter[IO, Player] = {
    case item: Gift.Item => GiftItemInterpreter(item)
    case Gift.AutomaticGachaRun => Kleisli {
      player =>
        IO {
          GachaCommand.Gachagive(player, 1, player.getName)
        }
    }
  }

  def backGroundProcess[
    G[_]
  ](implicit breakCountReadApi: BreakCountReadAPI[IO, G, Player]): IO[Nothing] = {
    breakCountReadApi
      .seichiLevelUpdates
      .evalMap { case (player, Diff(left, right)) =>
        HasSuccessor[SeichiLevel]
          .range(left, right)
          .toList
          .traverse { level =>
            val bundle = GiftBundleTable.bundleAt(level)
            val effect = interpreter.grantEffectOnBundle(bundle)
            effect(player)
          }
      }
      .compile.drain
      .flatMap(_ => IO.never)
  }

}
