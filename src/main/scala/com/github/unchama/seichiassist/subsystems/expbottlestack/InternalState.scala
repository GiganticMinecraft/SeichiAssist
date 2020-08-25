package com.github.unchama.seichiassist.subsystems.expbottlestack

import com.github.unchama.generic.effect.ResourceScope
import org.bukkit.entity.ThrownExpBottle

case class InternalState[F[_], G[_]](managedBottleScope: ResourceScope[F, G, ThrownExpBottle])
