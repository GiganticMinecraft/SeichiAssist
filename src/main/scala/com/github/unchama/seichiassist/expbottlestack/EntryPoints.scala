package com.github.unchama.seichiassist.expbottlestack

import cats.effect.IO
import com.github.unchama.generic.effect.ResourceScope
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.SubsystemEntryPoints
import com.github.unchama.seichiassist.expbottlestack.bukkit.listeners.ExpBottleStackUsageController
import org.bukkit.entity.ThrownExpBottle

object EntryPoints {
  def wired(implicit managedBottleScope: ResourceScope[IO, ThrownExpBottle],
            effectEnvironment: EffectEnvironment): SubsystemEntryPoints = {
    SubsystemEntryPoints(
      listenersToBeRegistered = Seq(
        new ExpBottleStackUsageController()
      ),
      commandsToBeRegistered = Map()
    )
  }
}
