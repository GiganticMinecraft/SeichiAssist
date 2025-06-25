package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade

import cats.effect.{ConcurrentEffect, IO}
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gachapoint.GachaPointApi
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaPrizeAPI
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.{
  CanBeSignedAsGachaPrize,
  GachaPrizeTableEntry
}
import com.github.unchama.seichiassist.subsystems.playerheadskin.PlayerHeadSkinAPI
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.listeners.GachaTradeListener
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.traderules.BukkitTrade
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.domain.{
  GachaListProvider,
  GachaTradeRule
}
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import cats.data.Kleisli
import com.github.unchama.seichiassist.subsystems.tradesystems.application.TradeAction
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.traderules.BigOrRegular
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.actions.BukkitTradeActionFromInventory
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.subsystems.tradesystems.domain.TradeResult
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.actions.BukkitTradeActionFromMineStack
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObject
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.application.TradeFromMineStack
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.domain.TradeError

trait System[F[_], Player, ItemStack] extends Subsystem[F] {
  val api: GachaTradeAPI[F, Player, ItemStack]
}

object System {

  import cats.implicits._
  import com.github.unchama.minecraft.bukkit.algebra.BukkitPlayerHasName.instance

  def wired[F[_]: ConcurrentEffect: OnMinecraftServerThread, G[_]](
    implicit gachaPrizeAPI: GachaPrizeAPI[F, ItemStack, Player],
    gachaPointApi: GachaPointApi[F, G, Player],
    mineStackAPI: MineStackAPI[F, Player, ItemStack],
    playerHeadSkinAPI: PlayerHeadSkinAPI[IO, Player],
    effectEnvironment: EffectEnvironment
  ): System[F, Player, ItemStack] = {
    implicit val canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack] =
      gachaPrizeAPI.canBeSignedAsGachaPrize
    implicit val gachaListProvider: GachaListProvider[F, ItemStack] =
      new GachaListProvider[F, ItemStack] {
        override def readGachaList: F[Vector[GachaPrizeTableEntry[ItemStack]]] =
          gachaPrizeAPI.listOfNow
      }
    implicit val gachaTradeRule: GachaTradeRule[ItemStack, (BigOrRegular, Int)] =
      (playerName: String, gachaList: Vector[GachaPrizeTableEntry[ItemStack]]) =>
        new BukkitTrade(playerName, gachaList)

    implicit val mineStackTradeAction: TradeAction[F, Player, ItemStack, (BigOrRegular, Int)] =
      new BukkitTradeActionFromMineStack[F, G]

    val tradeFromMineStack =
      new TradeFromMineStack[F, ItemStack, Player, (BigOrRegular, Int)]

    new System[F, Player, ItemStack] {
      val inventoryTradeAction: TradeAction[F, Player, ItemStack, (BigOrRegular, Int)] =
        new BukkitTradeActionFromInventory[F, G]

      override val api: GachaTradeAPI[F, Player, ItemStack] =
        new GachaTradeAPI[F, Player, ItemStack] {
          override def getTradableItems: Kleisli[F, Player, Vector[ItemStack]] =
            Kleisli { player =>
              gachaListProvider.readGachaList.map { gachaList =>
                gachaTradeRule.ruleFor(player.getName(), gachaList).tradableItems
              }
            }

          override def tradeFromInventory(
            contents: List[ItemStack]
          ): Kleisli[F, Player, TradeResult[ItemStack, (BigOrRegular, Int)]] = {
            Kleisli { player =>
              gachaListProvider.readGachaList.flatMap { gachaList =>
                inventoryTradeAction.execute(player, contents)(
                  gachaTradeRule.ruleFor(player.getName(), gachaList)
                )
              }
            }
          }

          override def tryTradeFromMineStack(
            player: Player,
            mineStackObject: MineStackObject[ItemStack],
            amount: Int
          ): F[Either[TradeError, TradeResult[ItemStack, (BigOrRegular, Int)]]] = {
            tradeFromMineStack.tryTradeFromMineStack(player, mineStackObject, amount)
          }
        }

      override val listeners: Seq[Listener] = Seq(
        new GachaTradeListener[F, G](gachaTradeRule)(
          gachaListProvider,
          inventoryTradeAction,
          effectEnvironment
        )
      )
    }
  }

}
