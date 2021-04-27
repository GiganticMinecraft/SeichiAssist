package com.github.unchama.seichiassist.subsystems.seichilevelupgift

import cats.data.Kleisli
import cats.effect.{Async, Sync}
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.commands.legacy.GachaCommand
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.bukkit.GiftItemInterpreter
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.{Gift, GiftInterpreter}
import io.chrisdavenport.log4cats.ErrorLogger
import org.bukkit.entity.Player

object System {

  def backGroundProcess[
    F[_] : OnMinecraftServerThread : ErrorLogger : Async,
    G[_]
  ](implicit breakCountReadApi: BreakCountReadAPI[F, G, Player]): F[Nothing] = {

    val interpreter: GiftInterpreter[F, Player] = {
      val giftItemInterpreter = new GiftItemInterpreter[F]

      {
        case item: Gift.Item => giftItemInterpreter(item)
        case Gift.AutomaticGachaRun => Kleisli {
          player =>
            Sync[F].delay {
              GachaCommand.Gachagive(player, 1, player.getName)
            }
        }
      }
    }

    StreamExtra.compileToRestartingStream {
      breakCountReadApi
        .seichiLevelUpdates
        .evalTap { case (player, diff) => interpreter.onLevelDiff(diff).run(player) }
    }
  }
}
