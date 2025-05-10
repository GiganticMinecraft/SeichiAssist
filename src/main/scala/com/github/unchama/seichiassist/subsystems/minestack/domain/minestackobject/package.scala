package com.github.unchama.seichiassist.subsystems.minestack.domain

package object minestackobject {

  type MineStackObjectGroup[ItemStack] =
    Either[MineStackObject[ItemStack], MineStackObjectWithKindVariants[ItemStack]]

}
