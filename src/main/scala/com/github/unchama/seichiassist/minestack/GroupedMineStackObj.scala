package com.github.unchama.seichiassist.minestack

case class GroupedMineStackObj(
  representative: MineStackObject,
  coloredVariants: List[MineStackObject]
) {
  require(coloredVariants.forall(_.category == representative.category))

  def category: MineStackObjectCategory = representative.category
}
