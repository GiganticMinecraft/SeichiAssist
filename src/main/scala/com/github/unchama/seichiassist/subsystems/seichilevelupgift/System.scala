package com.github.unchama.seichiassist.subsystems.seichilevelupgift

import cats.effect.Async
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.minecraft.actions.{OnMinecraftServerThread, SendMinecraftMessage}
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.GrantLevelUpGift
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.usecases.GrantGiftOnSeichiLevelDiff
import io.chrisdavenport.log4cats.ErrorLogger
import org.bukkit.entity.Player

object System {

  def backGroundProcess[F[_]: OnMinecraftServerThread: ErrorLogger: Async, G[_]](
    implicit breakCountReadApi: BreakCountReadAPI[F, G, Player],
    send: SendMinecraftMessage[F, Player],
    grant: GrantLevelUpGift[F, Player]
  ): F[Nothing] = {
    StreamExtra.compileToRestartingStream("[SeichiLevelUpGift]") {
      breakCountReadApi.seichiLevelUpdates.evalTap {
        case (player, diff) =>
          GrantGiftOnSeichiLevelDiff.grantGiftTo(diff, player)
      }
    }
  }
}
