package com.github.unchama.seichiassist

import com.github.unchama.generic.tag.tag
import com.github.unchama.generic.tag.tag.@@
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

object MaterialSets {

  private val notApplicableSeichiSkillMaterials =
    Set(Material.WATER, Material.LAVA, Material.AIR, Material.BEDROCK)

  // このMaterialは整地スキルに対応する
  // TODO(1.18): 1.18のコードに書き換えるときに、materialsの列挙をホワイトリスト方式からブラックリスト方式に書き換えたので、
  //  元のコードとMaterial.values()のdiffを取って、なぜホワイトリスト方式が採用されたかを考えてみる
  val materials: Set[Material] = Material.values().toSet.diff(notApplicableSeichiSkillMaterials)

  // これらのマテリアルを持つブロックは破壊を整地量に計上しない
  val exclude: Set[Material] = Set(
    Material.DIRT_PATH,
    Material.FARMLAND,
    Material.SPAWNER,
    Material.CAULDRON,
    Material.ENDER_CHEST,
    Material.END_PORTAL_FRAME,
    Material.END_PORTAL
  )

  val materialsToCountBlockBreak: Set[Material] = materials -- exclude

  /**
   * これらのマテリアルを用いてブロックの破壊試行を行う。
   *
   * 整地スキル使用時のブロックから取れるアイテムは、 プレーヤーの使用ツールのマテリアルをこれらに張り替えた時のドロップのmaxとして計算される。
   *
   * 例えば石をシャベルで掘った時にも、ツールのエンチャントを保ったままダイヤツルハシで掘ったものとして計算し、 結果得られるスタック数が最大のものが結果として採用される。
   */
  val breakTestToolMaterials: Seq[Material] =
    Seq(Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.DIAMOND_SHOVEL)

  val breakToolMaterials: Set[Material] = Set(
    Material.WOODEN_PICKAXE,
    Material.WOODEN_SHOVEL,
    Material.STONE_PICKAXE,
    Material.STONE_AXE,
    Material.STONE_SHOVEL,
    Material.IRON_PICKAXE,
    Material.IRON_AXE,
    Material.IRON_SHOVEL,
    Material.GOLDEN_PICKAXE,
    Material.GOLDEN_AXE,
    Material.GOLDEN_SHOVEL
  ) ++ breakTestToolMaterials

  val cancelledMaterials: Set[Material] = Set(
    Material.CHEST,
    Material.ENDER_CHEST,
    Material.TRAPPED_CHEST,
    Material.ANVIL,
    Material.ARMOR_STAND,
    Material.BEACON,
    Material.BIRCH_DOOR,
    Material.BIRCH_FENCE_GATE,
    Material.BIRCH_STAIRS,
    Material.BIRCH_BOAT,
    Material.OAK_BOAT,
    Material.ACACIA_BOAT,
    Material.JUNGLE_BOAT,
    Material.DARK_OAK_BOAT,
    Material.SPRUCE_BOAT,
    Material.FURNACE,
    Material.CRAFTING_TABLE,
    Material.HOPPER,
    Material.MINECART
  )

  val transparentMaterials: Set[Material] = Set(Material.BEDROCK, Material.AIR)

  val gravityMaterials: Set[Material] =
    Set(
      Material.ACACIA_LOG,
      Material.BIRCH_LOG,
      Material.DARK_OAK_LOG,
      Material.JUNGLE_LOG,
      Material.OAK_LOG,
      Material.OAK_LEAVES,
      Material.BIRCH_LEAVES,
      Material.JUNGLE_LEAVES,
      Material.ACACIA_LEAVES,
      Material.DARK_OAK_LEAVES,
      Material.SPRUCE_LEAVES
    )

  val fluidMaterials: Set[Material] =
    Set(Material.WATER, Material.LAVA)

  trait MaterialOf[S <: Set[Material]]

  type ItemStackOf[S <: Set[Material]] = ItemStack @@ MaterialOf[S]
  type BlockOf[S <: Set[Material]] = Block @@ MaterialOf[S]

  type BreakTool = ItemStackOf[breakToolMaterials.type]
  type BlockBreakableBySkill = BlockOf[materials.type]

  def refineItemStack(
    stack: ItemStack,
    set: collection.immutable.Set[Material]
  ): Option[ItemStackOf[set.type]] =
    if (set.contains(stack.getType))
      Some(tag.apply[MaterialOf[set.type]][ItemStack](stack))
    else
      None

  def refineBlock(
    block: Block,
    set: collection.immutable.Set[Material]
  ): Option[BlockOf[set.type]] =
    if (set.contains(block.getType))
      Some(tag.apply[MaterialOf[set.type]][Block](block))
    else
      None
}
