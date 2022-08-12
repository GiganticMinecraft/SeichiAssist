package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gttosiina

import cats.effect.ConcurrentEffect
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gacha.GachaAPI
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.BukkitCanBeSignedAsGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.domain.CanBeSignedAsGachaPrize
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gttosiina.bukkit.listeners.GtToSiinaringo
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

object System {

  def wired[F[_]: ConcurrentEffect](implicit gachaAPI: GachaAPI[F, ItemStack]): Subsystem[F] = {
    implicit val canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack] =
      new BukkitCanBeSignedAsGachaPrize
    new Subsystem[F] {
      override val listeners: Seq[Listener] = Seq(new GtToSiinaringo[F]())
    }
  }

}
