package com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject

case class MineStackObjectWithKindVariants[ItemStack](
  representative: MineStackObject[ItemStack],
  kindVariants: List[MineStackObject[ItemStack]]
) {
  require(kindVariants.forall(_.category == representative.category))

  def category: MineStackObjectCategory = representative.category
}
