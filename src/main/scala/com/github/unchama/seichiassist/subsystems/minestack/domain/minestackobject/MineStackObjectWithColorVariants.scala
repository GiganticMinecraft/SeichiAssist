package com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject

case class MineStackObjectWithColorVariants[ItemStack](
  representative: MineStackObject[ItemStack],
  coloredVariants: List[MineStackObject[ItemStack]]
) {
  require(coloredVariants.forall(_.category == representative.category))

  def category: MineStackObjectCategory = representative.category
}
