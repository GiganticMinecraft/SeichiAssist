package com.github.unchama.seichiassist.subsystems.gacha.bukkit.actions

import cats.Monad
import cats.data.Kleisli
import cats.effect.Sync
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

  override def tryInsertIntoMineStack(
    prize: GachaPrize[ItemStack]
  ): Kleisli[F, Player, Boolean] =
    Kleisli { player =>
      val itemStack = prize.itemStack
      mineStackAPI.tryIntoMineStack.apply(player, itemStack, itemStack.getAmount)
    }

  override def insertIntoPlayerInventoryOrDrop(
    prize: GachaPrize[ItemStack]
  ): Kleisli[F, Player, GrantState] =
    Kleisli { player =>
      Sync[F].delay {
        val newItemStack = prize.materializeWithOwnerSignature(player.getName)
        if (!InventoryOperations.isPlayerInventoryFull(player)) {
          InventoryOperations.addItem(player, newItemStack)
          GrantState.AddedInventory
        } else {
          InventoryOperations.dropItem(player, newItemStack)
          GrantState.Dropped
        }
      }
    }

  override implicit val F: Monad[F] = implicitly
}
