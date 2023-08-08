package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade

import cats.effect.ConcurrentEffect
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gachapoint.GachaPointApi
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaPrizeAPI
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.{
  CanBeSignedAsGachaPrize,
  GachaPrizeTableEntry
}
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.listeners.GachaTradeListener
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.traderules.BukkitTrade
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.domain.{
  GachaListProvider,
  GachaTradeRule
}
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

object System {

  def wired[F[_]: ConcurrentEffect, G[_]](
    implicit gachaPrizeAPI: GachaPrizeAPI[F, ItemStack, Player],
    gachaPointApi: GachaPointApi[F, G, Player]
  ): Subsystem[F] = {
    implicit val canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack] =
      gachaPrizeAPI.canBeSignedAsGachaPrize
    implicit val gachaListProvider: GachaListProvider[F, ItemStack] =
      new GachaListProvider[F, ItemStack] {
        override def readGachaList: F[Vector[GachaPrizeTableEntry[ItemStack]]] = gachaPrizeAPI.listOfNow
      }
    val gachaTradeRule: GachaTradeRule[ItemStack] =
      (playerName: String, gachaList: Vector[GachaPrizeTableEntry[ItemStack]]) =>
        new BukkitTrade(playerName, gachaList)

    new Subsystem[F] {
      override val listeners: Seq[Listener] = Seq(new GachaTradeListener[F, G](gachaTradeRule))
    }
  }

}
