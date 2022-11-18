package com.github.unchama.seichiassist.subsystems.gridregion.domain

import com.github.unchama.generic.RefDict

trait RegionTemplatePersistence[F[_], Player] extends RefDict[F, Player, Vector[RegionUnits]]
