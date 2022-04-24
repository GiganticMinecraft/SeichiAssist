package com.github.unchama.seichiassist.subsystems.seichilevelupgift.bukkit

import cats.data.Kleisli
import cats.effect.Sync
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.data.{GachaSkullData, ItemData}
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.{
  Gift,
  GiftItemInterpreter
}
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.Gift.Item
import com.github.unchama.seichiassist.util.Util.grantItemStacksEffect
import org.bukkit.entity.Player

class BukkitGiftItemInterpreter[F[_]: OnMinecraftServerThread: Sync]
    extends GiftItemInterpreter {
  override def apply(item: Gift.Item): Kleisli[F, Player, Unit] = {
    Kleisli { player =>
      val itemStack = item match {
        case Item.GachaTicket  => GachaSkullData.gachaSkull
        case Item.SuperPickaxe => ItemData.getSuperPickaxe(1)
        case Item.GachaApple   => ItemData.getGachaApple(1)
        case Item.Elsa         => ItemData.getElsa(1)
      }

      grantItemStacksEffect[F](itemStack).run(player)
    }
  }
}
