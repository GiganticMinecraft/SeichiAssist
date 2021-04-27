package com.github.unchama.seichiassist.subsystems.seichilevelupgift.bukkit

import cats.data.Kleisli
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.data.{GachaSkullData, ItemData}
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.Gift
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.Gift.Item
import com.github.unchama.seichiassist.util.Util.grantItemStacksEffect
import com.github.unchama.targetedeffect.{SequentialEffect, TargetedEffect}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * アイテムギフトの付与を実行するインタプリタ。
 */
class GiftItemInterpreter[F[_] : OnMinecraftServerThread] extends (Gift.Item => Kleisli[F, Player, Unit]) {

  override def apply(item: Gift.Item): Kleisli[F, Player, Unit] = {
    val itemStack = item match {
      case Item.GachaTicket => GachaSkullData.gachaSkull
      case Item.SuperPickaxe => ItemData.getSuperPickaxe(1)
      case Item.GachaApple => ItemData.getGachaApple(1)
      case Item.Elsa => ItemData.getElsa(1)
    }

    val message = item match {
      case Item.GachaTicket => Some(MessageEffect("レベルアップ記念のガチャ券を配布しました。"))
      case _ => None
    }

    // この明示的な型変数がないとビルドが通らない。とはいえ返り値は自明なので最低限の表記を選択した
    SequentialEffect[_](message.toList :+ grantItemStacksEffect(itemStack))
  }

}
