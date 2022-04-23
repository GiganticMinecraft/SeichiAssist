package com.github.unchama.seichiassist.subsystems.seichilevelupgift.usecases

import cats.Applicative
import cats.data.Kleisli
import cats.effect.Sync
import com.github.unchama.generic.Diff
import com.github.unchama.generic.algebra.typeclasses.HasSuccessor
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.commands.legacy.GachaCommand
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiLevel
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.bukkit.GiftItemInterpreter
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.{
  Gift,
  GiftBundleTable
}
import org.bukkit.entity.Player

object GrantGiftOnSeichiLevelDiff {

  import cats.implicits._

  private def action[F[_]: OnMinecraftServerThread: Sync](
    gift: Gift
  ): Kleisli[F, Player, Unit] = {
    val giftItemInterpreter = new GiftItemInterpreter[F]

    gift match {
      case item: Gift.Item => giftItemInterpreter(item)
      case Gift.AutomaticGachaRun =>
        Kleisli { player =>
          Sync[F].delay {
            GachaCommand.Gachagive(player, 1, player.getName)
          }
        }
    }
  }

  def grantGiftOn[F[_]: OnMinecraftServerThread: Sync](
    player: Player,
    levelDiff: Diff[SeichiLevel]
  ): F[Unit] = {
    HasSuccessor[SeichiLevel]
      .leftOpenRightClosedRange(levelDiff.left, levelDiff.right)
      .toList
      .traverse { level =>
        GiftBundleTable.bundleAt(level).map.toList.traverse {
          case (gift, i) =>
            Sync[F].delay {
              gift match {
                case _: Gift.Item           => player.sendMessage("レベルアップ記念のアイテムを配布しました。")
                case Gift.AutomaticGachaRun => player.sendMessage("レベルアップ記念としてガチャを回しました。")
              }
              action(gift).replicateA(i)
            }
        }
      }
      .as(())
  }

}
