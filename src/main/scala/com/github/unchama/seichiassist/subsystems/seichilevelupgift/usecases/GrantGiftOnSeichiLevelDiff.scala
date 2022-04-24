package com.github.unchama.seichiassist.subsystems.seichilevelupgift.usecases

import cats.Applicative
import cats.data.Kleisli
import cats.effect.Sync
import com.github.unchama.generic.Diff
import com.github.unchama.generic.algebra.typeclasses.HasSuccessor
import com.github.unchama.minecraft.actions.SendMinecraftMessage
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiLevel
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.{
  Gift,
  GiftBundleTable
}

trait GrantGiftOnSeichiLevelDiff[F[_], Player] {

  import cats.implicits._

  def onGift(gift: Gift): Kleisli[F, Player, Unit]

  final def grantGiftOn(levelDiff: Diff[SeichiLevel], player: Player)(
    implicit F: Applicative[F],
    sync: Sync[F],
    send: SendMinecraftMessage[F, Player]
  ): F[Unit] = {
    val giftBundles = HasSuccessor[SeichiLevel]
      .leftOpenRightClosedRange(levelDiff.left, levelDiff.right)
      .toList
      .map { level => GiftBundleTable.bundleAt(level) }

    giftBundles
      .traverse { giftBundle =>
        giftBundle
          .map
          .toList
          .traverse {
            case (gift, count) =>
              onGift(gift).replicateA(count).run(player) >> {
                gift match {
                  case _: Gift.Item =>
                    SendMinecraftMessage[F, Player].string(player, "レベルアップ記念のアイテムを配布しました。")
                  case Gift.AutomaticGachaRun =>
                    SendMinecraftMessage[F, Player].string(player, "レベルアップ記念としてガチャを回しました。")
                }
              }
          }
          .as(())
      }
      .as(())
  }

}
