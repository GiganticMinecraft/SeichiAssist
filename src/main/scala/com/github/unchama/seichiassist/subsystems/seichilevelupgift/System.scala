package com.github.unchama.seichiassist.subsystems.seichilevelupgift

import cats.effect.Async
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.minecraft.actions.{OnMinecraftServerThread, SendMinecraftMessage}
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.gacha.GachaAPI
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.BukkitCanBeSignedAsGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.domain.CanBeSignedAsGachaPrize
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
    gachaAPI: GachaAPI[F, ItemStack, Player],
    gachaPointApi: GachaPointApi[F, G, Player]
  ): F[Nothing] = {
    implicit val canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack] =
      BukkitCanBeSignedAsGachaPrize
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
