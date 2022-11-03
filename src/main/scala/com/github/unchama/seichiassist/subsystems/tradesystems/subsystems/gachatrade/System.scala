package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade

import cats.effect.ConcurrentEffect
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaAPI
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.CanBeSignedAsGachaPrize
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaprize.GachaPrize
import com.github.unchama.seichiassist.subsystems.gachapoint.GachaPointApi
import com.github.unchama.seichiassist.subsystems.tradesystems.domain.TradeRule
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.listeners.GachaTradeListener
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.traderules.{
  BigOrRegular,
  BukkitTrade
}
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.domain.{
  GachaListProvider,
  GachaTradeRule
}
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

object System {

  def wired[F[_]: ConcurrentEffect, G[_]: ContextCoercion[*[_], F]](
    implicit gachaAPI: GachaAPI[F, ItemStack, Player],
    gachaPointApi: GachaPointApi[F, G, Player]
  ): Subsystem[F] = {
    implicit val canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack] =
      gachaAPI.canBeSignedAsGachaPrize
    implicit val gachaListProvider: GachaListProvider[F, ItemStack] =
      new GachaListProvider[F, ItemStack] {
        override def readGachaList: F[Vector[GachaPrize[ItemStack]]] = gachaAPI.list
      }
    val gachaTradeRule: GachaTradeRule[ItemStack] = new GachaTradeRule[ItemStack] {
      override def ruleFor(
        playerName: String,
        gachaList: Vector[GachaPrize[ItemStack]]
      ): TradeRule[ItemStack, BigOrRegular] =
        new BukkitTrade(playerName, gachaList)
    }

    new Subsystem[F] {
      override val listeners: Seq[Listener] = Seq(new GachaTradeListener[F, G](gachaTradeRule))
    }
  }

}
