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

  import cats.implicits._

  override def insertIntoPlayerInventoryOrDrop(
    prize: GachaPrize[ItemStack]
  ): Kleisli[F, Player, GrantState] =
    Kleisli { player =>
      val newItemStack = prize.materializeWithOwnerSignature(player.getName)
      for {
        isInventoryFull <- Sync[F].delay(InventoryOperations.isPlayerInventoryFull(player))
        _ <-
          if (isInventoryFull) {
            Sync[F].delay(InventoryOperations.addItem(player, newItemStack))
          } else {
            InventoryOperations.grantItemStacksEffect(newItemStack).apply(player)
          }
      } yield if (isInventoryFull) GrantState.AddedInventory else GrantState.Dropped

    }
  override implicit val F: Monad[F] = implicitly
}
