package com.github.unchama.seichiassist.subsystems.seichilevelupgift.bukkit

import cats.data.Kleisli
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.data.ItemData
import com.github.unchama.seichiassist.subsystems.gachapoint.GachaPointApi
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.gachapoint.GachaPoint
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.Gift.Item
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.GiftItemInterpreter
import com.github.unchama.seichiassist.util.InventoryOperations.grantItemStacksEffect
import org.bukkit.entity.Player

class BukkitGiftItemInterpreter[F[_]: OnMinecraftServerThread, G[_]: ContextCoercion[*[_], F]](
  implicit gachaPointApi: GachaPointApi[F, G, Player]
) extends GiftItemInterpreter[F, Player] {

  override def apply(item: Item): Kleisli[F, Player, Unit] = {
    item match {
      case Item.GachaTicket =>
        gachaPointApi
          .addGachaPoint(GachaPoint.perGachaTicket)
          .mapK[F](ContextCoercion.asFunctionK)
      case _ =>
        val itemStack = item match {
          case Item.SuperPickaxe => ItemData.getSuperPickaxe(1)
          case Item.GachaApple   => ItemData.getGachaApple(1)
          case Item.Elsa         => ItemData.getElsa(1)
        }
        grantItemStacksEffect[F](itemStack)
    }
  }

}
