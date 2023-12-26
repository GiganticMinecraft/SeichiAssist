package com.github.unchama.seichiassist.seichiskill

import cats.effect.SyncIO
import com.github.unchama.seichiassist.MaterialSets
import com.github.unchama.seichiassist.MaterialSets.BlockBreakableBySkill
import com.github.unchama.seichiassist.data.XYZTuple
import com.github.unchama.seichiassist.util.BreakUtil
import com.github.unchama.util.external.ExternalPlugins
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.Waterlogged
import org.bukkit.entity.Player

import scala.collection.{Set, mutable}

object BlockSearching {
  case class Result(
    solids: List[BlockBreakableBySkill],
    waters: List[Block],
    lavas: List[Block]
  ) {
    def filterSolids(f: Block => Boolean): Result = copy(solids = solids.filter(f))

    def filterAll(f: Block => Boolean): Result =
      copy(solids = solids.filter(f), waters = waters.filter(f), lavas = lavas.filter(f))
  }

  def searchForBlocksBreakableWithSkill(
    player: Player,
    relativeVectors: Seq[XYZTuple],
    referencePoint: Block
  ): SyncIO[Result] = SyncIO {
    val solidBlocks = new mutable.HashSet[BlockBreakableBySkill]
    val waterBlocks = new mutable.HashSet[Block]
    val lavaBlocks = new mutable.HashSet[Block]

    val lockedBlocks = BreakUtil.unsafeGetLockedBlocks()

    relativeVectors.collect {
      case XYZTuple(x, y, z) =>
        val targetBlock = referencePoint.getRelative(x, y, z)
        val waterloggedMaterials = Set(
          Material.WATER,
          Material.BUBBLE_COLUMN,
          Material.TALL_SEAGRASS,
          Material.SEAGRASS,
          Material.KELP,
          Material.KELP_PLANT
        )

        if (BreakUtil.canBreakWithSkill(player, targetBlock, lockedBlocks)) {
          if (targetBlock.getType == Material.LAVA) {
            lavaBlocks.add(targetBlock)
          } else if (
            waterloggedMaterials.contains(targetBlock.getType) || (targetBlock
              .getBlockData
              .isInstanceOf[Waterlogged] && ExternalPlugins
              .getCoreProtectWrapper
              .isNotEditedBlock(targetBlock) && targetBlock
              .getBlockData
              .asInstanceOf[Waterlogged]
              .isWaterlogged)
          ) {
            waterBlocks.add(targetBlock)
          } else {
            MaterialSets
              .refineBlock(targetBlock, MaterialSets.materials)
              .foreach(b => solidBlocks.add(b))
          }
        }
    }

    Result(solidBlocks.toList, waterBlocks.toList, lavaBlocks.toList)
  }

  /**
   * `referenceBlock`をプレーヤーが破壊しスキルを発動させようとしているとき、 複数idブロック破壊を防ぐ必要がある場合がある。
   *
   * この関数は、そのような状況下で破壊可能ブロックを`filter`するための述語を与える。
   */
  def multiTypeBreakingFilterPredicate(referenceBlock: Block): Block => Boolean = {
    targetBlock =>
      val blockMaterials = Set(referenceBlock.getType, targetBlock.getType)

      val identifications = List(Set(Material.DIRT, Material.GRASS), Set(Material.REDSTONE_ORE))

      // マテリアルが同一視により等しくなるかどうか
      blockMaterials.size == 1 || identifications.exists(blockMaterials.subsetOf)
  }
}
