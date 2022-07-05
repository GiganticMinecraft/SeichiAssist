package com.github.unchama.seichiassist.subsystems.seichilevelupgift.usecases

import cats.Applicative
import cats.effect.Bracket.catsKleisliBracket
import cats.effect.Sync
import com.github.unchama.generic.Diff
import com.github.unchama.generic.algebra.typeclasses.HasSuccessor
import com.github.unchama.minecraft.actions.SendMinecraftMessage
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiLevel
import com.github.unchama.seichiassist.subsystems.gachapoint.GachaPointApi
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.{
  Gift,
  GiftBundle,
  GiftBundleTable,
  GrantLevelUpGift
}

object GrantGiftOnSeichiLevelDiff {

  import cats.implicits._

  final def grantGiftTo[F[_]: Applicative: Sync, G[_], Player](
    levelDiff: Diff[SeichiLevel],
    player: Player,
    gachaPointApi: GachaPointApi[F, G, Player]
  )(
    implicit send: SendMinecraftMessage[F, Player],
    grant: GrantLevelUpGift[F, Player, G]
  ): F[Unit] = {
    val giftBundles = HasSuccessor[SeichiLevel]
      .leftOpenRightClosedRange(levelDiff.left, levelDiff.right)
      .toList
      .map { level => GiftBundleTable.bundleAt(level) }

    val giftBundle = giftBundles.fold(GiftBundle.empty)(_ combine _)
    giftBundle.traverseGifts { (gift, count) =>
      GrantLevelUpGift[F, Player, G]
        .grant(gift)(gachaPointApi)
        .replicateA(count)
        .run(player) >> {
        gift match {
          case _: Gift.Item =>
            SendMinecraftMessage[F, Player].string(player, "レベルアップ記念のアイテムを配布しました。")
          case Gift.AutomaticGachaRun =>
            SendMinecraftMessage[F, Player].string(player, "レベルアップ記念としてガチャを回しました。")
        }
      }
    }
  }

}
