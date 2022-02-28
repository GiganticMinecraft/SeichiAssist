package com.github.unchama.seichiassist.subsystems.manabar

import cats.effect.{ConcurrentEffect, SyncEffect}
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.mana.ManaReadApi
import com.github.unchama.seichiassist.subsystems.manabar.application.ManaBarSynchronizationRepository
import com.github.unchama.seichiassist.subsystems.manabar.bukkit.CreateFreshBossBar
import io.chrisdavenport.log4cats.ErrorLogger
import org.bukkit.entity.Player

object System {

  import cats.implicits._

  def wired[F[_]: ConcurrentEffect: ErrorLogger, G[_]: SyncEffect](
    implicit manaApi: ManaReadApi[F, G, Player]
  ): G[Subsystem[F]] = {
    import com.github.unchama.minecraft.bukkit.algebra.BukkitPlayerHasUuid.instance

    val definition =
      ManaBarSynchronizationRepository.withContext(manaApi)(CreateFreshBossBar.in[G, F])

    BukkitRepositoryControls.createHandles(definition).map { control =>
      new Subsystem[F] {
        override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] =
          List(control.coerceFinalizationContextTo[F])
      }
    }
  }

}
