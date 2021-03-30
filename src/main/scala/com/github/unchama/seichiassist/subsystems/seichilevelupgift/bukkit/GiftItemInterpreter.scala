package com.github.unchama.seichiassist.subsystems.seichilevelupgift.bukkit

import cats.data.Kleisli
import cats.effect.IO
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.{GachaSkullData, ItemData}
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.Gift
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.Gift.Item
import com.github.unchama.seichiassist.util.Util.grantItemStacksEffect
import com.github.unchama.targetedeffect.{SequentialEffect, TargetedEffect}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.entity.Player

/**
 * アイテムギフトの付与を実行するインタプリタ。
 *
 * TODO IOをこのレイヤから引き剥がしたい。これによってsubsystemがIOに依存することになってしまっている。
 */
object GiftItemInterpreter extends (Gift.Item => Kleisli[IO, Player, Unit]) {

  override def apply(item: Gift.Item): Kleisli[IO, Player, Unit] = {
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

    SequentialEffect(message.toList :+ grantItemStacksEffect(itemStack))
  }

}
