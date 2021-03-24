package com.github.unchama.seichiassist.subsystems.seichilevelupgift

import cats.data.Kleisli
import cats.effect.IO
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.seichiassist.commands.legacy.GachaCommand
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.bukkit.GiftItemInterpreter
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.{Gift, GiftInterpreter}
import io.chrisdavenport.log4cats.ErrorLogger
import org.bukkit.entity.Player

object System {

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
  ](implicit breakCountReadApi: BreakCountReadAPI[IO, G, Player], ioLogger: ErrorLogger[IO]): IO[Nothing] = {
    StreamExtra.compileToRestartingStream {
      breakCountReadApi
        .seichiLevelUpdates
        .evalTap { case (player, diff) => interpreter.onLevelDiff(diff).run(player) }
    }
  }
}
