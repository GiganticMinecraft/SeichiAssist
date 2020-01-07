package com.github.unchama.seichiassist.activeskill

import cats.effect.SyncIO
import com.github.unchama.seichiassist.MaterialSets
import com.github.unchama.seichiassist.data.XYZTuple
import com.github.unchama.seichiassist.util.BreakUtil
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player

import scala.collection.{Set, mutable}

object BlockSearching {
  case class Result(solids: List[Block], waters: List[Block], lavas: List[Block]) {

    def mapSolids(f: List[Block] => List[Block]): Result = copy(solids = f(solids))

    def mapAll(f: List[Block] => List[Block]): Result = copy(solids = f(solids), waters = f(waters), lavas = f(lavas))

  }

  def searchForBreakableBlocks(player: Player, relativeVectors: Seq[XYZTuple], referencePoint: Block): SyncIO[Result] = SyncIO {
    val solidBlocks = new mutable.HashSet[Block]
    val waterBlocks = new mutable.HashSet[Block]
    val lavaBlocks  = new mutable.HashSet[Block]

    relativeVectors.collect { case XYZTuple(x, y, z) =>
      val targetBlock = referencePoint.getRelative(x, y, z)

      if (BreakUtil.canBreak(player, Some.apply(targetBlock)))
        targetBlock.getType match {
          case Material.STATIONARY_LAVA | Material.LAVA =>
            lavaBlocks.add(targetBlock)
          case Material.STATIONARY_WATER | Material.WATER =>
            waterBlocks.add(targetBlock)
          case material if MaterialSets.materials.contains(material) =>
            solidBlocks.add(targetBlock)
          case _ =>
        }
    }

    Result(solidBlocks.toList, waterBlocks.toList, lavaBlocks.toList)
  }

  /**
   * `referenceBlock`をプレーヤーが破壊しスキルを発動させようとしているとき、
   * 複数idブロック破壊を防ぐ必要がある場合がある。
   *
   * この関数は、そのような状況下で破壊可能ブロックを`filter`するための述語を与える。
   */
  def multiTypeBreakingFilterPredicate(referenceBlock: Block): Block => Boolean = { targetBlock =>
    val blockMaterials = Set(referenceBlock.getType, targetBlock.getType)

    val identifications = List(
      Set(Material.DIRT, Material.GRASS),
      Set(Material.REDSTONE_ORE, Material.GLOWING_REDSTONE_ORE)
    )

    // マテリアルが同一視により等しくなるかどうか
    blockMaterials.size == 1 || identifications.exists(blockMaterials.subsetOf)
  }
}
