package com.github.unchama.seichiassist.subsystems.vote.bukkit.actions

import cats.effect.{Sync, SyncEffect}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.data.ItemData
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.gachaprize.bukkit.factories.BukkitGachaSkullData
import com.github.unchama.seichiassist.subsystems.vote.application.actions.ReceiveVoteBenefits
import com.github.unchama.seichiassist.subsystems.vote.domain.{
  EffectPoint,
  VoteBenefit,
  VotePersistence
}
import com.github.unchama.seichiassist.util.InventoryOperations.grantItemStacksEffect
import org.bukkit.entity.Player

class BukkitReceiveVoteBenefits[F[_]: OnMinecraftServerThread: Sync, G[
  _
]: SyncEffect: ContextCoercion[*[_], F]](
  implicit votePersistence: VotePersistence[F],
  breakCountAPI: BreakCountAPI[F, G, Player]
) extends ReceiveVoteBenefits[F, Player] {

  import cats.implicits._

  override def receive(player: Player): F[Unit] = {
    val uuid = player.getUniqueId
    for {
      totalVote <- votePersistence.currentVoteCount(uuid)
      receivedVote <- votePersistence.receivedVoteBenefits(uuid)
      pendingCount = VoteBenefit(totalVote.value - receivedVote.value)
      // 受け取ってない分を受け取ったことにする
      _ <- votePersistence.increaseVoteBenefits(uuid, pendingCount)
      playerLevel <- ContextCoercion(breakCountAPI.seichiAmountDataRepository(player).read.map {
        _.levelCorrespondingToExp.level
      })
      gachaTicketAmount = Seq.fill(10 * pendingCount.value)(BukkitGachaSkullData.gachaForVoting)
      additionalVoteBenefit = Seq.fill(pendingCount.value)(
        if (playerLevel < 50) ItemData.getSuperPickaxe(1)
        else ItemData.getVotingGift(1)
      )
      grantItems = gachaTicketAmount ++ additionalVoteBenefit
      _ <- {
        ContextCoercion(votePersistence.increaseEffectPoints(uuid, EffectPoint(10)))
          .replicateA(pendingCount.value) >>
          grantItemStacksEffect[F](grantItems: _*).apply(player)
      }.whenA(pendingCount.value != 0)
    } yield ()
  }

}
