package com.github.unchama.seichiassist.subsystems.gridregion.domain

import com.github.unchama.generic.RefDict
import org.bukkit.entity.Player

trait RegionTemplatePersistence[F[_]] extends RefDict[F, Player, Vector[RegionUnits]]
