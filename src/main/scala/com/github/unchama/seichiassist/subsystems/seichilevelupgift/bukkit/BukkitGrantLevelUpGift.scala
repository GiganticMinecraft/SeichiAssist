package com.github.unchama.seichiassist.subsystems.seichilevelupgift.bukkit

import cats.data.Kleisli
import cats.effect.Sync
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.subsystems.gacha.GachaAPI
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.actions.BukkitDrawGacha
import com.github.unchama.seichiassist.subsystems.gachapoint.GachaPointApi
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.gachapoint.GachaPoint
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.{
  Gift,
  GiftItemInterpreter,
  GrantLevelUpGift
}
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class BukkitGrantLevelUpGift[F[_]: Sync: OnMinecraftServerThread, G[_]: ContextCoercion[*[
  _
], F]](implicit gachaPointApi: GachaPointApi[F, G, Player], gachaAPI: GachaAPI[F, ItemStack])
    extends GrantLevelUpGift[F, Player] {
  override def grant(gift: Gift): Kleisli[F, Player, Unit] = {
    val giftItemInterpreter: GiftItemInterpreter[F, Player] =
      new BukkitGiftItemInterpreter[F, G]

    gift match {
      case item: Gift.Item =>
        giftItemInterpreter(item)
      case Gift.GachaPointWorthSingleTicket =>
        gachaPointApi
          .addGachaPoint(GachaPoint.perGachaTicket)
          .mapK[F](ContextCoercion.asFunctionK)
      case Gift.AutomaticGachaRun =>
        Kleisli { player => new BukkitDrawGacha[F].draw(player, 1) }
    }
  }
}

object BukkitGrantLevelUpGift {

  implicit def apply[F[_]: Sync: OnMinecraftServerThread, G[_]: ContextCoercion[*[_], F]](
    implicit gachaAPI: GachaAPI[F, ItemStack],
    gachaPointApi: GachaPointApi[F, G, Player]
  ): GrantLevelUpGift[F, Player] =
    new BukkitGrantLevelUpGift[F, G]

}
