package com.github.unchama.seichiassist.subsystems.gacha.bukkit.actions

import cats.Monad
import cats.data.Kleisli
import cats.effect.Sync
import com.github.unchama.generic.ApplicativeExtra.whenAOrElse
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.subsystems.gacha.application.actions.GrantGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.domain.GrantState
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaprize.GachaPrize
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.CanBeSignedAsGachaPrize
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.seichiassist.util.InventoryOperations
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class BukkitGrantGachaPrize[F[_]: Sync: OnMinecraftServerThread](
  implicit canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack],
  mineStackAPI: MineStackAPI[F, Player, ItemStack]
) extends GrantGachaPrize[F, ItemStack] {

  import cats.implicits._

  override def tryInsertIntoMineStack(
    prize: GachaPrize[ItemStack]
  ): Kleisli[F, Player, Boolean] =
    Kleisli { player =>
      val itemStack = prize.itemStack
      for {
        currentAutoMineStackState <- mineStackAPI.autoMineStack(player)
        isSucceedTryIntoMineStack <- whenAOrElse(currentAutoMineStackState)(
          mineStackAPI
            .mineStackRepository
            .tryIntoMineStack(player, itemStack, itemStack.getAmount),
          false
        )
      } yield isSucceedTryIntoMineStack
    }

  override def insertIntoPlayerInventoryOrDrop(
    prize: GachaPrize[ItemStack],
    ownerName: Option[String]
  ): Kleisli[F, Player, GrantState] =
    Kleisli { player =>
      for {
        isInventoryFull <- Sync[F].delay(InventoryOperations.isPlayerInventoryFull(player))
        newItemStack = ownerName.fold(prize.itemStack)(prize.materializeWithOwnerSignature)
        _ <-
          InventoryOperations.grantItemStacksEffect(newItemStack).apply(player)
      } yield if (isInventoryFull) GrantState.AddedInventory else GrantState.Dropped

    }

  override implicit val F: Monad[F] = implicitly
}
