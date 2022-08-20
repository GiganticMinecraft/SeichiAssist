package com.github.unchama.seichiassist.subsystems.vote.bukkit.actions

import cats.effect.{Sync, SyncEffect}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.data.{GachaSkullData, ItemData}
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.application.actions.ReceiveVoteBenefits
import com.github.unchama.seichiassist.util.InventoryOperations.grantItemStacksEffect
import org.bukkit.entity.Player

class BukkitReceiveVoteBenefits[F[_]: OnMinecraftServerThread: Sync, G[
  _
]: SyncEffect: ContextCoercion[*[_], F]](
  implicit voteAPI: VoteAPI[F, Player],
  breakCountAPI: BreakCountAPI[F, G, Player]
) extends ReceiveVoteBenefits[F, G, Player] {

  import cats.implicits._

  /**
   * 投票特典を配布する
   */
  override def receive(player: Player): F[Unit] = {
    val uuid = player.getUniqueId
    for {
      notReceivedBenefits <- voteAPI.restVoteBenefits(uuid)
      _ <- voteAPI.increaseVoteBenefits(uuid, notReceivedBenefits) // 受け取ってない分を受け取ったことにする
      playerLevel <- ContextCoercion(breakCountAPI.seichiAmountDataRepository(player).read.map {
        _.levelCorrespondingToExp.level
      })
      items =
        Seq.fill(10 * notReceivedBenefits.value)(GachaSkullData.gachaForVoting) ++
          Seq.fill(notReceivedBenefits.value)(
            if (playerLevel < 50) ItemData.getSuperPickaxe(1)
            else ItemData.getVotingGift(1)
          )
      _ <- {
        ContextCoercion(voteAPI.increaseEffectPointsByTen(uuid))
          .replicateA(notReceivedBenefits.value) >>
          grantItemStacksEffect[F](items: _*).apply(player)
      }.whenA(notReceivedBenefits.value != 0)
    } yield ()
  }

}
