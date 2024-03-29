package com.github.unchama.seichiassist.subsystems.gacha.bukkit.actions

import cats.Monad
import cats.data.Kleisli
import cats.effect.Sync
import com.github.unchama.generic.ApplicativeExtra.whenAOrElse
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.subsystems.gacha.application.actions.GrantGachaPrize
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.{
  CanBeSignedAsGachaPrize,
  GachaPrizeTableEntry
}
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.seichiassist.util.InventoryOperations
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class BukkitGrantGachaPrize[F[_]: Sync: OnMinecraftServerThread](
  implicit canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack],
  mineStackAPI: MineStackAPI[F, Player, ItemStack]
) extends GrantGachaPrize[F, ItemStack, Player] {

  import cats.implicits._

  override def tryInsertIntoMineStack(
    prizes: Vector[GachaPrizeTableEntry[ItemStack]]
  ): Kleisli[F, Player, Vector[GachaPrizeTableEntry[ItemStack]]] =
    Kleisli { player =>
      for {
        currentAutoMineStackState <- mineStackAPI.autoMineStack(player)
        results <- prizes.traverse { gachaPrize =>
          val itemStack = gachaPrize.itemStack
          whenAOrElse(currentAutoMineStackState)(
            mineStackAPI
              .mineStackRepository
              .tryIntoMineStack(player, itemStack, itemStack.getAmount)
              .map(hasBeenStoredInMineStack => gachaPrize -> hasBeenStoredInMineStack),
            gachaPrize -> false
          )
        }
      } yield results.collect {
        case (gachaPrize, result) if !result => gachaPrize
      }
    }

  override def insertIntoPlayerInventoryOrDrop(
    prizes: Vector[GachaPrizeTableEntry[ItemStack]]
  ): Kleisli[F, Player, Unit] =
    Kleisli { player =>
      val newItemStacks = prizes.map { prize =>
        prize.materializeWithOwnerSignature(player.getName)
      }

      InventoryOperations.grantItemStacksEffect(newItemStacks: _*).apply(player)
    }

  override implicit val F: Monad[F] = implicitly
}
