package com.github.unchama.seichiassist.subsystems.seichilevelupgift

import cats.effect.Async
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.minecraft.actions.{OnMinecraftServerThread, SendMinecraftMessage}
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.gacha.GachaDrawAPI
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaPrizeAPI
import com.github.unchama.seichiassist.subsystems.gachapoint.GachaPointApi
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.bukkit.BukkitGrantLevelUpGift
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.GrantLevelUpGiftAlgebra
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.usecases.GrantGiftOnSeichiLevelDiff
import io.chrisdavenport.log4cats.ErrorLogger
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object System {

  def backGroundProcess[F[_]: OnMinecraftServerThread: ErrorLogger: Async, G[
    _
  ]: ContextCoercion[*[_], F]](
    implicit breakCountReadApi: BreakCountReadAPI[F, G, Player],
    send: SendMinecraftMessage[F, Player],
    gachaPrizeAPI: GachaPrizeAPI[F, ItemStack, Player],
    gachaPointApi: GachaPointApi[F, G, Player],
    gachaDrawAPI: GachaDrawAPI[F, Player]
  ): F[Nothing] = {
    implicit val grantLevelUpGiftAlgebra: GrantLevelUpGiftAlgebra[F, Player] =
      new BukkitGrantLevelUpGift
    StreamExtra.compileToRestartingStream("[SeichiLevelUpGift]") {
      breakCountReadApi.seichiLevelUpdates.evalTap {
        case (player, diff) =>
          GrantGiftOnSeichiLevelDiff.grantGiftTo(diff, player)
      }
    }
  }
}
