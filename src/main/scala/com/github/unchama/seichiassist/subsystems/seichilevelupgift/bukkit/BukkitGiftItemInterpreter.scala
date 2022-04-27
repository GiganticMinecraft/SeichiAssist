package com.github.unchama.seichiassist.subsystems.seichilevelupgift.bukkit

import cats.data.Kleisli
import cats.effect.Sync
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.data.{GachaSkullData, ItemData}
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.GiftItemInterpreter
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.Gift.Item
import com.github.unchama.seichiassist.util.InventoryOperations.grantItemStacksEffect
import org.bukkit.entity.Player

class BukkitGiftItemInterpreter[F[_]: OnMinecraftServerThread] extends GiftItemInterpreter[F] {
  override def apply(item: Item): Kleisli[F, Player, Unit] = {
    val itemStack = item match {
      case Item.GachaTicket  => GachaSkullData.gachaSkull
      case Item.SuperPickaxe => ItemData.getSuperPickaxe(1)
      case Item.GachaApple   => ItemData.getGachaApple(1)
      case Item.Elsa         => ItemData.getElsa(1)
    }
    grantItemStacksEffect[F](itemStack)
  }
}
