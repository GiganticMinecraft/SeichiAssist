package com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject

import com.github.unchama.generic.RefDict

import java.util.UUID

trait MineStackObjectPersistence[F[_], ItemStack <: Cloneable]
    extends RefDict[F, UUID, List[MineStackObjectWithAmount[ItemStack]]]
