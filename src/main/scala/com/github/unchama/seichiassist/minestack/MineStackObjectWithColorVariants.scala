package com.github.unchama.seichiassist.minestack

import com.github.unchama.seichiassist.subsystems.minestack.domain.{MineStackObject, MineStackObjectCategory}

case class MineStackObjectWithColorVariants(
  representative: MineStackObject,
  coloredVariants: List[MineStackObject]
) {
  require(coloredVariants.forall(_.category == representative.category))

  def category: MineStackObjectCategory = representative.category
}
