package com.github.unchama.seichiassist.effect.breaking

import com.github.unchama.seichiassist.effect.XYZIterator
import com.github.unchama.seichiassist.effect.XYZTuple
import kotlin.jvm.internal.Ref
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.scheduler.BukkitRunnable

abstract class AbstractBreakTask2 : BukkitRunnable() {
  private var blocks: List<Block>? = null
  /**
   * `loc`を中心にした3x3x3の立方体の範囲のブロックが一つでも`blocks`に格納されているか調べる
   * @param loc 中心点
   * @return 含まれているならtrue、含まれていないならfalse
   */
  protected fun isBreakBlock(blocks: List<Block>, loc: Location): Boolean {
    val b = loc.block
    if (blocks.contains(b)) return true
    // needs final rule
    val ret = Ref.BooleanRef()
    XYZIterator(XYZTuple(-1, -1, -1), XYZTuple(1, 1, 1)) {
      if (b.getRelative(it.x, it.y, it.z) in blocks) {
        ret.element = true
      }
    }.doAction()
    return ret.element
  }
}
