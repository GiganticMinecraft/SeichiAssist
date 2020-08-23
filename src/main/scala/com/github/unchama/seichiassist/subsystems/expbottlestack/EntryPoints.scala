package com.github.unchama.seichiassist.subsystems.expbottlestack

import cats.effect.IO
import com.github.unchama.generic.effect.ResourceScope
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.meta.StatelessSubsystemEntryPoints
import com.github.unchama.seichiassist.subsystems.expbottlestack.bukkit.listeners.ExpBottleStackUsageController
import org.bukkit.entity.ThrownExpBottle

object EntryPoints {
  def wired(implicit managedBottleScope: ResourceScope[IO, ThrownExpBottle],
            effectEnvironment: EffectEnvironment): StatelessSubsystemEntryPoints = {
    StatelessSubsystemEntryPoints(
      listenersToBeRegistered = Seq(
        new ExpBottleStackUsageController()
      ),
      commandsToBeRegistered = Map()
    )
  }
}
