package com.github.unchama.seichiassist.subsystems.vote.bukkit.actions

import cats.effect.{Sync, SyncEffect}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.data.ItemData
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.factories.BukkitGachaSkullData
import com.github.unchama.seichiassist.subsystems.vote.application.actions.ReceiveVoteBenefits
import com.github.unchama.seichiassist.subsystems.vote.domain.{VoteBenefit, VotePersistence}
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
      voteCounter <- votePersistence.voteCounter(uuid)
      receivedVote <- votePersistence.receivedVoteBenefits(uuid)
      notReceivedBenefits = VoteBenefit(voteCounter.value - receivedVote.value)
      _ <- votePersistence.increaseVoteBenefits(
        uuid,
        notReceivedBenefits
      ) // 受け取ってない分を受け取ったことにする
      playerLevel <- ContextCoercion(breakCountAPI.seichiAmountDataRepository(player).read.map {
        _.levelCorrespondingToExp.level
      })
      gachaSkulls = Seq.fill(10 * notReceivedBenefits.value)(
        BukkitGachaSkullData.gachaForVoting
      )
      elseVoteBenefits = Seq.fill(notReceivedBenefits.value)(
        if (playerLevel < 50) ItemData.getSuperPickaxe(1)
        else ItemData.getVotingGift(1)
      )
      grantItems = gachaSkulls ++ elseVoteBenefits
      _ <- {
        ContextCoercion(votePersistence.increaseEffectPoints(uuid))
          .replicateA(notReceivedBenefits.value) >>
          grantItemStacksEffect[F](grantItems: _*).apply(player)
      }.whenA(notReceivedBenefits.value != 0)
    } yield ()
  }

}
