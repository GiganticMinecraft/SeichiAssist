package com.github.unchama.seichiassist.activeskill

import com.github.unchama.seichiassist.data.XYZTuple
import org.bukkit.Material

sealed trait SkillRange {
  val effectChunkSize: XYZTuple
}

sealed trait ActiveSkillRange extends SkillRange

object ActiveSkillRange {
  case class MultiArea(effectChunkSize: XYZTuple, areaCount: Int) extends ActiveSkillRange

  object MultiArea {
    def apply(width: Int, height: Int, depth: Int)(count: Int): MultiArea =
      MultiArea(XYZTuple(width, height, depth), count)
  }

  def singleArea(width: Int, height: Int, depth: Int): MultiArea = MultiArea(width, height, depth)(1)

  case class RemoteArea(effectChunkSize: XYZTuple) extends ActiveSkillRange

  object RemoteArea {
    def apply(width: Int, height: Int, depth: Int): RemoteArea = RemoteArea(XYZTuple(width, height, depth))
  }
}

trait AssaultSkillRange extends SkillRange {
  val blockMaterialConversion: Material => Material
}

object AssaultSkillRange {
  type AssaultRangeBuilder = (Int, Int, Int) => AssaultSkillRange

  private val condenseLavaConversion: Material => Material = {
    case Material.LAVA => Material.MAGMA
    case x => x
  }

  private val condenseWaterConversion: Material => Material = {
    case Material.WATER => Material.ICE
    case x => x
  }

  private val condenseLiquidConversion: Material => Material = condenseLavaConversion.compose(condenseWaterConversion)

  private def withConversion(f: Material => Material): AssaultRangeBuilder = {
    case (width, height, depth) => new AssaultSkillRange {
      override val effectChunkSize: XYZTuple = XYZTuple(width, height, depth)
      override val blockMaterialConversion: Material => Material = f
    }
  }

  val armor: AssaultRangeBuilder = withConversion(_ => Material.AIR)
  val condenseWater: AssaultRangeBuilder = withConversion(condenseWaterConversion)
  val condenseLava: AssaultRangeBuilder = withConversion(condenseLavaConversion)
  val condenseLiquid: AssaultRangeBuilder = withConversion(condenseLiquidConversion)
}
