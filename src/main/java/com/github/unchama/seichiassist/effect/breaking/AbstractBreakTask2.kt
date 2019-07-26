package com.github.unchama.seichiassist.effect.breaking

import com.github.unchama.seichiassist.effect.XYZIterator
import com.github.unchama.seichiassist.effect.XYZTuple
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.scheduler.BukkitRunnable
import kotlin.jvm.internal.Ref

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
    // needs final rule
    val ret = Ref.BooleanRef()
    val ll = { xyzTuple: XYZTuple ->
      if (blocks.contains(b.getRelative(xyzTuple.x, xyzTuple.y, xyzTuple.z))) {
        ret.element = true
      }
      Unit
    }
    XYZIterator(XYZTuple(-1, -1, -1), XYZTuple(1, 1, 1), ll).doAction()
    return ret.element
  }
}
