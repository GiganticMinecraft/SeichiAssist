package com.github.unchama.seichiassist.subsystems.seichilevelupgift.bukkit

import cats.data.Kleisli
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.data.GachaSkullData
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.Gift
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.Gift.Item
import com.github.unchama.seichiassist.util.Util.grantItemStacksEffect
import com.github.unchama.seichiassist.util.itemcodec.{ElsaCodec, GachaRingoCodec, VotePickaxeCodec}
import org.bukkit.entity.Player

/**
 * アイテムギフトの付与を実行するインタプリタ。
 */
class GiftItemInterpreter[F[_] : OnMinecraftServerThread] extends (Gift.Item => Kleisli[F, Player, Unit]) {

  override def apply(item: Gift.Item): Kleisli[F, Player, Unit] = {
    val itemStack = item match {
      case Item.GachaTicket => GachaSkullData.gachaForSeichiLevelUp
      case Item.SuperPickaxe => VotePickaxeCodec.create(())
      case Item.GachaApple => GachaRingoCodec.create(())
      case Item.Elsa => ElsaCodec.create(())
    }

    grantItemStacksEffect(itemStack)
  }

}
