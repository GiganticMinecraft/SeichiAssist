package com.github.unchama.seichiassist.subsystems.gacha.bukkit.actions

import cats.Monad
import cats.data.Kleisli
import cats.effect.Sync
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.subsystems.gacha.application.actions.GrantGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.domain.gachaprize.GachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.domain.{
  CanBeSignedAsGachaPrize,
  GrantState
}
import com.github.unchama.seichiassist.util.{BreakUtil, InventoryOperations}
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class BukkitGrantGachaPrize[F[_]: Sync: OnMinecraftServerThread](
  implicit canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack]
) extends GrantGachaPrize[F, ItemStack] {

  override def tryInsertIntoMineStack(
    prize: GachaPrize[ItemStack]
  ): Kleisli[F, Player, Boolean] =
    Kleisli { player =>
      Sync[F].delay {
        val signedItemStack = prize.materializeWithOwnerSignature(player.getName)
        BreakUtil.tryAddItemIntoMineStack(player, signedItemStack)
      }
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
