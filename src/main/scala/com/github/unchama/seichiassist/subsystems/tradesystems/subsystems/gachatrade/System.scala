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
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.BukkitTradeActionFromInventory
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.generic.effect.unsafe.EffectEnvironment

trait System[F[_], Player, ItemStack] extends Subsystem[F] {
  val api: GachaTradeAPI[F, Player, ItemStack]
}

object System {

  import cats.implicits._

  def wired[F[_]: ConcurrentEffect: OnMinecraftServerThread, G[_]](
    implicit gachaPrizeAPI: GachaPrizeAPI[F, ItemStack, Player],
    gachaPointApi: GachaPointApi[F, G, Player],
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
    val gachaTradeRule: GachaTradeRule[ItemStack] =
      (playerName: String, gachaList: Vector[GachaPrizeTableEntry[ItemStack]]) =>
        new BukkitTrade(playerName, gachaList)

    implicit val inventoryTradeAction: TradeAction[F, Player, ItemStack, (BigOrRegular, Int)] =
      new BukkitTradeActionFromInventory[F, G]

    new System[F, Player, ItemStack] {
      override val api: GachaTradeAPI[F, Player, ItemStack] =
        new GachaTradeAPI[F, Player, ItemStack] {
          override def getTradableItems: Kleisli[F, Player, Vector[ItemStack]] =
            Kleisli { player =>
              gachaListProvider.readGachaList.map { gachaList =>
                gachaTradeRule.ruleFor(player.getName(), gachaList).tradableItems
              }
            }
        }

      override val listeners: Seq[Listener] = Seq(new GachaTradeListener[F, G](gachaTradeRule))
    }
  }

}
