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
    val preEffect = item match {
      case Item.GachaTicket => Some(
        SequentialEffect(
          MessageEffect("レベルアップ記念のガチャ券を配布しました。"),
          TargetedEffect.delay[Player](p =>
            SeichiAssist.playermap(p.getUniqueId).gachapoint += SeichiAssist.seichiAssistConfig.getGachaPresentInterval
          )
        )
      )
      case _ => None
    }

    val itemStack = item match {
      case Item.GachaTicket => GachaSkullData.gachaSkull
      case Item.SuperPickaxe => ItemData.getSuperPickaxe(1)
      case Item.GachaApple => ItemData.getGachaApple(1)
      case Item.Elsa => ItemData.getElsa(1)
    }

    SequentialEffect(preEffect ++: grantItemStacksEffect(itemStack) +: Nil)
  }

}
