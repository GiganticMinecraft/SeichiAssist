package com.github.unchama.seichiassist

import com.github.unchama.generic.tag.tag
import com.github.unchama.generic.tag.tag.@@
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

object MaterialSets {
  val fortuneMaterials: Set[Material] = Set(
    Material.COAL_ORE, Material.DIAMOND_ORE,
    Material.LAPIS_ORE, Material.EMERALD_ORE,
    Material.REDSTONE_ORE, Material.GLOWING_REDSTONE_ORE,
    Material.QUARTZ_ORE
  )

  // このMaterialは整地スキルに対応するブロック群を示しています。
  val materials: Set[Material] = Set(
    Material.STONE, Material.NETHERRACK, Material.NETHER_BRICK, Material.DIRT, Material.GRAVEL, Material.LOG,
    Material.LOG_2, Material.GRASS, Material.IRON_ORE, Material.GOLD_ORE, Material.SAND,
    Material.SANDSTONE, Material.END_BRICKS, Material.ENDER_STONE, Material.ICE,
    Material.PACKED_ICE, Material.OBSIDIAN, Material.MAGMA, Material.SOUL_SAND, Material.LEAVES, Material.LEAVES_2,
    Material.CLAY, Material.STAINED_CLAY, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE, Material.HARD_CLAY,
    Material.MONSTER_EGGS, Material.WEB, Material.WOOD, Material.FENCE, Material.DARK_OAK_FENCE, Material.RAILS,
    Material.MYCEL, Material.SNOW_BLOCK, Material.HUGE_MUSHROOM_1, Material.HUGE_MUSHROOM_2, Material.BONE_BLOCK,
    Material.PURPUR_BLOCK, Material.PURPUR_PILLAR, Material.SEA_LANTERN, Material.PRISMARINE, Material.SMOOTH_BRICK,
    Material.GLOWSTONE, Material.STAINED_GLASS, Material.STAINED_GLASS_PANE, Material.THIN_GLASS, Material.GLASS,
    Material.WOOD_STAIRS, Material.BIRCH_WOOD_STAIRS, Material.SPRUCE_WOOD_STAIRS, Material.ACACIA_STAIRS,
    Material.DARK_OAK_STAIRS, Material.BIRCH_FENCE, Material.SPRUCE_FENCE, Material.ACACIA_FENCE, Material.FENCE_GATE,
    Material.BIRCH_FENCE_GATE, Material.SPRUCE_FENCE_GATE, Material.ACACIA_FENCE_GATE, Material.DARK_OAK_FENCE_GATE,
    Material.COBBLESTONE_STAIRS, Material.SANDSTONE_STAIRS, Material.BRICK_STAIRS, Material.QUARTZ_STAIRS,
    Material.BOOKSHELF, Material.IRON_FENCE, Material.ICE, Material.WOOL, Material.GOLD_BLOCK, Material.END_ROD,
    Material.PUMPKIN, Material.MELON_BLOCK, Material.STONE_SLAB2, Material.SPONGE, Material.SOIL, Material.GRASS_PATH,
    Material.MOB_SPAWNER, Material.WORKBENCH, Material.FURNACE, Material.QUARTZ_BLOCK, Material.CHEST,
    Material.TRAPPED_CHEST, Material.NETHER_FENCE, Material.NETHER_BRICK_STAIRS, Material.CAULDRON, Material.END_ROD,
    Material.PURPUR_STAIRS, Material.END_BRICKS, Material.PURPUR_SLAB, Material.ENDER_CHEST, Material.PURPUR_SLAB, Material.STEP,
    Material.DOUBLE_STEP,Material.ENDER_PORTAL_FRAME,Material.ENDER_PORTAL
  ) ++ fortuneMaterials

  /**
   * これらのマテリアルを用いてブロックの破壊試行を行う。
   *
   * 整地スキル使用時のブロックから取れるアイテムは、
   * プレーヤーの使用ツールのマテリアルをこれらに張り替えた時のドロップのmaxとして計算される。
   *
   * 例えば石をシャベルで掘った時にも、ツールのエンチャントを保ったままダイヤツルハシで掘ったものとして計算し、
   * 結果得られるスタック数が最大のものが結果として採用される。
   */
  val breakTestToolMaterials: Seq[Material] = Seq(
    Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.DIAMOND_SPADE
  )

  val breakToolMaterials: Set[Material] = Set(
    Material.WOOD_PICKAXE, Material.WOOD_SPADE,
    Material.IRON_PICKAXE, Material.IRON_AXE, Material.IRON_SPADE,
    Material.GOLD_PICKAXE, Material.GOLD_AXE, Material.GOLD_SPADE
  ) ++ breakTestToolMaterials

  val cancelledMaterials: Set[Material] = Set(
    Material.CHEST, Material.ENDER_CHEST, Material.TRAPPED_CHEST, Material.ANVIL, Material.ARMOR_STAND,
    Material.BEACON, Material.BIRCH_DOOR, Material.BIRCH_FENCE_GATE, Material.BIRCH_WOOD_STAIRS,
    Material.BOAT, Material.FURNACE, Material.WORKBENCH, Material.HOPPER, Material.MINECART
  )

  val transparentMaterials: Set[Material] = Set(
    Material.BEDROCK, Material.AIR
  )

  val gravityMaterials: Set[Material] = Set(
    Material.LOG, Material.LOG_2, Material.LEAVES, Material.LEAVES_2
  )

  trait MaterialOf[S <: Set[Material]]

  type ItemStackOf[S <: Set[Material]] = ItemStack @@ MaterialOf[S]
  type BlockOf[S <: Set[Material]] = Block @@ MaterialOf[S]

  type BreakTool = ItemStackOf[breakToolMaterials.type]
  type BlockBreakableBySkill = BlockOf[materials.type]

  def refineItemStack(stack: ItemStack, set: collection.immutable.Set[Material]): Option[ItemStackOf[set.type]] =
    if (set.contains(stack.getType))
      Some(tag.apply[MaterialOf[set.type]][ItemStack](stack))
    else
      None

  def refineBlock(block: Block, set: collection.immutable.Set[Material]): Option[BlockOf[set.type]] =
    if (set.contains(block.getType))
      Some(tag.apply[MaterialOf[set.type]][Block](block))
    else
      None
}
