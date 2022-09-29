package com.github.unchama.seichiassist.subsystems.minestack

import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.{MineStackObject, MineStackObjectWithColorVariants}

package object domain {

  type MineStackObjectGroup[ItemStack] = Either[MineStackObject[ItemStack], MineStackObjectWithColorVariants[ItemStack]]

}
