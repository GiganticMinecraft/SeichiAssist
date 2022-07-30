package com.github.unchama.seichiassist.subsystems.vote.bukkit.actions

import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import cats.effect.{ConcurrentEffect, SyncEffect}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.data.{GachaSkullData, ItemData}
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.application.actions.ReceiveVoteBenefits
import com.github.unchama.seichiassist.util.InventoryOperations.grantItemStacksEffect
import org.bukkit.entity.Player

object BukkitReceiveVoteBenefits {

  import cats.implicits._

  /**
   * 投票特典を配布する
   */
  def apply[F[_]: OnMinecraftServerThread: ConcurrentEffect, G[_]: SyncEffect: ContextCoercion[
    *[_],
    F
  ]]: ReceiveVoteBenefits[F, G, Player] =
    new ReceiveVoteBenefits[F, G, Player] {
      override def receive(player: Player)(
        implicit voteAPI: VoteAPI[F, Player],
        breakCountAPI: BreakCountAPI[F, G, Player]
      ): F[Unit] = {
        val uuid = player.getUniqueId
        for {
          notReceivedBenefits <- voteAPI.restVoteBenefits(uuid)
          _ <- voteAPI.increaseVoteBenefits(uuid, notReceivedBenefits) // 受け取ってない分を受け取ったことにする
        } yield {
          if (notReceivedBenefits.value != 0) {
            val playerLevel =
              ContextCoercion(breakCountAPI.seichiAmountDataRepository(player).read.map {
                _.levelCorrespondingToExp.level
              }).toIO.unsafeRunSync()

            val items = (0 until notReceivedBenefits.value).map { _ =>
              ContextCoercion(voteAPI.increaseEffectPointsByTen(uuid)).toIO.unsafeRunSync()
              Seq.fill(10)(GachaSkullData.gachaForVoting) ++
                Seq(
                  if (playerLevel < 50) ItemData.getSuperPickaxe(1)
                  else ItemData.getVotingGift(1)
                )
            }

            grantItemStacksEffect[F](items.flatten: _*)
          }
        }
      }
    }

}
