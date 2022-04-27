package com.github.unchama.seichiassist.subsystems.seichilevelupgift.usecases

import cats.Applicative
import cats.effect.Bracket.catsKleisliBracket
import cats.effect.Sync
import com.github.unchama.generic.Diff
import com.github.unchama.generic.algebra.typeclasses.HasSuccessor
import com.github.unchama.minecraft.actions.SendMinecraftMessage
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiLevel
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.{
  Gift,
  GiftBundleTable,
  GrantLevelUpGift
}

class GrantGiftOnSeichiLevelDiff[F[_], Player] {

  import cats.implicits._

  final def grantGiftTo(levelDiff: Diff[SeichiLevel], player: Player)(
    implicit F: Applicative[F],
    sync: Sync[F],
    send: SendMinecraftMessage[F, Player],
    grant: GrantLevelUpGift[F, Player]
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
              GrantLevelUpGift[F, Player].grant(gift).replicateA(count).run(player) >> {
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
