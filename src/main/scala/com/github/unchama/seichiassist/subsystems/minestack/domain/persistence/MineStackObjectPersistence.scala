package com.github.unchama.seichiassist.subsystems.minestack.domain.persistence

import com.github.unchama.generic.RefDict
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObjectWithAmount

import java.util.UUID

trait MineStackObjectPersistence[F[_], ItemStack]
    extends RefDict[F, UUID, List[MineStackObjectWithAmount[ItemStack]]]
