package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gttosiina

import cats.effect.ConcurrentEffect
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gacha.GachaAPI
import com.github.unchama.seichiassist.subsystems.gacha.domain.CanBeSignedAsGachaPrize
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gttosiina.bukkit.BukkitStaticTradeItemFactory
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gttosiina.bukkit.listeners.GtToSiinaringo
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gttosiina.domain.StaticTradeItemFactory
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

trait System[F[_], ItemStack] extends Subsystem[F] {

  val api: GtToSiinaAPI[ItemStack]

}

object System {

  def wired[F[_]: ConcurrentEffect](
    implicit gachaAPI: GachaAPI[F, ItemStack, Player]
  ): System[F, ItemStack] = {
    implicit val canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack] =
      gachaAPI.canBeSignedAsGachaPrize
    implicit val tradeItemFactory: StaticTradeItemFactory[ItemStack] =
      BukkitStaticTradeItemFactory

    new System[F, ItemStack] {
      override val api: GtToSiinaAPI[ItemStack] = (name: String) =>
        tradeItemFactory.getMaxRingo(name)

      override val listeners: Seq[Listener] = Seq(new GtToSiinaringo[F])
    }
  }

}
