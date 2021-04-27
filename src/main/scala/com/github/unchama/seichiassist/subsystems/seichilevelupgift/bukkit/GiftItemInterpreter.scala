package com.github.unchama.seichiassist.subsystems.seichilevelupgift.bukkit

import cats.data.Kleisli
import cats.effect.{IO, LiftIO, Sync, SyncIO}
import cats.kernel.Monoid
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.data.{GachaSkullData, ItemData}
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.Gift
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.Gift.Item
import com.github.unchama.seichiassist.util.Util.grantItemStacksEffect
import com.github.unchama.targetedeffect.{SequentialEffect, TargetedEffect}
import com.github.unchama.targetedeffect.commandsender.{MessageEffect, MessageEffectF}
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * アイテムギフトの付与を実行するインタプリタ。
 */
class GiftItemInterpreter[F[_] : OnMinecraftServerThread : Sync] extends (Gift.Item => Kleisli[F, Player, Unit]) {

  override def apply(item: Gift.Item): Kleisli[F, Player, Unit] = {
    val itemStack = item match {
      case Item.GachaTicket => GachaSkullData.gachaSkull
      case Item.SuperPickaxe => ItemData.getSuperPickaxe(1)
      case Item.GachaApple => ItemData.getGachaApple(1)
      case Item.Elsa => ItemData.getElsa(1)
    }

    val message = item match {
      case Item.GachaTicket => Some(MessageEffectF[F]("レベルアップ記念のガチャ券を配布しました。"))
      case _ => None
    }

    import cats.implicits._
    import cats.effect.implicits._
    SequentialEffect(message.toList :+ grantItemStacksEffect[F]())
  }

}
