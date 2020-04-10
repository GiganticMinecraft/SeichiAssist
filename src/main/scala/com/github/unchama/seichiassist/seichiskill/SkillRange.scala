package com.github.unchama.seichiassist.seichiskill

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

sealed trait AssaultSkillRange extends SkillRange {
  val blockMaterialConversion: Material => Material
}

object AssaultSkillRange {
  type AssaultRangeBuilder = (Int, Int, Int) => AssaultSkillRange

  case class Armor(effectChunkSize: XYZTuple) extends AssaultSkillRange {
    override val blockMaterialConversion: Material => Material = _ => Material.AIR
  }

  case class Water(effectChunkSize: XYZTuple) extends AssaultSkillRange {
    override val blockMaterialConversion: Material => Material = {
      case Material.WATER => Material.ICE
      case x => x
    }
  }

  case class Lava(effectChunkSize: XYZTuple) extends AssaultSkillRange {
    override val blockMaterialConversion: Material => Material = {
      case Material.LAVA => Material.MAGMA
      case x => x
    }
  }

  case class Liquid(effectChunkSize: XYZTuple) extends AssaultSkillRange {
    override val blockMaterialConversion: Material => Material = {
      case Material.WATER => Material.ICE
      case Material.LAVA => Material.MAGMA
      case x => x
    }
  }

  private def build(f: XYZTuple => AssaultSkillRange): AssaultRangeBuilder = {
    case (width, height, depth) => f(XYZTuple(width, height, depth))
  }

  val armor: AssaultRangeBuilder = build(Armor)
  val condenseWater: AssaultRangeBuilder = build(Water)
  val condenseLava: AssaultRangeBuilder = build(Lava)
  val condenseLiquid: AssaultRangeBuilder = build(Liquid)
}
