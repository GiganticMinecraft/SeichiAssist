package com.github.unchama.seichiassist.subsystems.vote.bukkit.actions

import cats.effect.{IO, Sync, SyncEffect}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.data.ItemData
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.gachaprize.bukkit.factories.BukkitGachaSkullData
import com.github.unchama.seichiassist.subsystems.playerheadskin.PlayerHeadSkinAPI
import com.github.unchama.seichiassist.subsystems.vote.application.actions.ReceiveVoteBenefits
import com.github.unchama.seichiassist.subsystems.vote.domain.{EffectPoint, ReceivedVoteCount, VotePersistence}
import com.github.unchama.seichiassist.util.InventoryOperations.grantItemStacksEffect
import org.bukkit.entity.Player

class BukkitReceiveVoteBenefits[F[_]: OnMinecraftServerThread: Sync, G[
  _
]: SyncEffect: ContextCoercion[*[_], F]](
  implicit votePersistence: VotePersistence[F],
  breakCountAPI: BreakCountAPI[F, G, Player],
  playerHeadSkinAPI: PlayerHeadSkinAPI[IO, Player]
) extends ReceiveVoteBenefits[F, Player] {

  import cats.implicits._

  override def receive(player: Player): F[Unit] = {
    val uuid = player.getUniqueId
    for {
      totalVote <- votePersistence.currentVoteCount(uuid)
      receivedVote <- votePersistence.receivedCount(uuid)
      pendingCount = ReceivedVoteCount(totalVote.value - receivedVote.value)
      // cap at 64 (#1816)
      toBeClaimed = pendingCount.min(ReceivedVoteCount(64))
      _ <- votePersistence.claim(uuid, toBeClaimed)
      playerLevel <- ContextCoercion(breakCountAPI.seichiAmountDataRepository(player).read.map {
        _.levelCorrespondingToExp.level
      })
      gachaTicketAmount = Seq.fill(10 * toBeClaimed.value)(BukkitGachaSkullData.gachaForVoting)
      additionalVoteBenefit = Seq.fill(toBeClaimed.value)(
        if (playerLevel < 50) ItemData.getSuperPickaxe(1)
        else ItemData.getVotingGift(1)
      )
      grantItems = gachaTicketAmount ++ additionalVoteBenefit
      _ <- {
        ContextCoercion(votePersistence.increaseEffectPoints(uuid, EffectPoint(10)))
          .replicateA(toBeClaimed.value) >>
          grantItemStacksEffect[F](grantItems: _*).apply(player)
      }.whenA(toBeClaimed.value != 0)
    } yield ()
  }

}
