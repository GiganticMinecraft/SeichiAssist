package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade

import cats.effect.ConcurrentEffect
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gacha.GachaAPI
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.BukkitCanBeSignedAsGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.domain.CanBeSignedAsGachaPrize
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.listeners.GachaTradeListener
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

object System {

  def wired[F[_]: ConcurrentEffect](implicit gachaAPI: GachaAPI[F, ItemStack, Player]): Subsystem[F] = {
    implicit val canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack] =
      BukkitCanBeSignedAsGachaPrize
    new Subsystem[F] {
      override val listeners: Seq[Listener] = Seq(new GachaTradeListener[F]())
    }
  }

}
