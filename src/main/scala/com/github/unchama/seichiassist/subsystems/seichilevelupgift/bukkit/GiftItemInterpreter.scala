package com.github.unchama.seichiassist.subsystems.seichilevelupgift.bukkit

import cats.data.Kleisli
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.data.{GachaSkullData, ItemData}
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.Gift
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.Gift.Item
import com.github.unchama.seichiassist.util.Util.grantItemStacksEffect
import org.bukkit.entity.Player

/**
 * アイテムギフトの付与を実行するインタプリタ。
 */
class GiftItemInterpreter[F[_] : OnMinecraftServerThread] extends (Gift.Item => Kleisli[F, Player, Unit]) {

  override def apply(item: Gift.Item): Kleisli[F, Player, Unit] = {
    val itemStack = item match {
      case Item.GachaTicket => GachaSkullData.gachaForSeichiLevelUp
      case Item.SuperPickaxe => ItemData.getSuperPickaxe(1)
      case Item.GachaApple => ItemData.getGachaApple(1)
      case Item.Elsa => ItemData.getElsa(1)
    }

    grantItemStacksEffect(itemStack)
  }

}
