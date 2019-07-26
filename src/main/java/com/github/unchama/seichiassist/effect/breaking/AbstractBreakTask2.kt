package com.github.unchama.seichiassist.effect.breaking

import com.github.unchama.seichiassist.effect.AxisAlignedCuboid
import com.github.unchama.seichiassist.effect.XYZTuple
import com.github.unchama.seichiassist.effect.forEachGridPoint
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.scheduler.BukkitRunnable

abstract class AbstractBreakTask2 : BukkitRunnable() {
  protected abstract val blocks: List<Block>

  /**
   * `loc`を中心にした3x3x3の立方体の範囲のブロックが一つでも`blocks`に格納されているか調べる
   * @param loc 中心点
   * @return 含まれているならtrue、含まれていないならfalse
   */
  protected fun isBreakBlock(loc: Location): Boolean {
    val b = loc.block
    if (blocks.contains(b)) return true

    AxisAlignedCuboid(XYZTuple(-1, -1, -1), XYZTuple(1, 1, 1)).forEachGridPoint { (x, y, z) ->
      if (blocks.contains(b.getRelative(x, y, z))) return true
    }

    return false
  }
}

/**
 * [location]を中心としたチェビシェフ距離が[distance]以下の領域に
 * [matchAgainst]のブロックが一つでも含まれているかを返す
 */
fun containsBlockAround(location: Location, distance: Int, matchAgainst: Set<Block>): Boolean {
  val cuboidToLookFor =
      AxisAlignedCuboid(XYZTuple(-distance, -distance, -distance), XYZTuple(distance, distance, distance))

  cuboidToLookFor.forEachGridPoint { (x, y, z) ->
    if (matchAgainst.contains(location.block.getRelative(x, y, z))) return true
  }

  return false
}