package com.github.unchama.seichiassist

import com.github.unchama.generic.tag.tag
import com.github.unchama.generic.tag.tag.@@
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

object MaterialSets {

  private val notApplicableSeichiSkillMaterials =
    Set(
      Material.WATER,
      Material.LAVA,
      Material.AIR,
      Material.BEDROCK,
      Material.TORCH,
      Material.SOUL_TORCH,
      Material.REDSTONE_TORCH,
      Material.REPEATER,
      Material.COMPARATOR,
      Material.END_ROD,
      Material.FIRE,
      Material.WHITE_CARPET,
      Material.ORANGE_CARPET,
      Material.MAGENTA_CARPET,
      Material.LIGHT_BLUE_CARPET,
      Material.YELLOW_CARPET,
      Material.LIME_CARPET,
      Material.PINK_CARPET,
      Material.GRAY_CARPET,
      Material.LIGHT_GRAY_CARPET,
      Material.CYAN_CARPET,
      Material.PURPLE_CARPET,
      Material.BLUE_CARPET,
      Material.BROWN_CARPET,
      Material.GREEN_CARPET,
      Material.RED_CARPET,
      Material.BLACK_CARPET,
      Material.WHITE_BANNER,
      Material.ORANGE_BANNER,
      Material.MAGENTA_BANNER,
      Material.LIGHT_BLUE_BANNER,
      Material.YELLOW_BANNER,
      Material.LIME_BANNER,
      Material.PINK_BANNER,
      Material.GRAY_BANNER,
      Material.LIGHT_GRAY_BANNER,
      Material.CYAN_BANNER,
      Material.PURPLE_BANNER,
      Material.BLUE_BANNER,
      Material.BROWN_BANNER,
      Material.GREEN_BANNER,
      Material.RED_BANNER,
      Material.BLACK_BANNER,
      Material.RAIL,
      Material.POWERED_RAIL,
      Material.ACTIVATOR_RAIL,
      Material.DETECTOR_RAIL,
      Material.REDSTONE,
      Material.REDSTONE_WIRE,
      Material.TRIPWIRE_HOOK,
      Material.OAK_WALL_SIGN,
      Material.SPRUCE_WALL_SIGN,
      Material.BIRCH_WALL_SIGN,
      Material.ACACIA_WALL_SIGN,
      Material.JUNGLE_WALL_SIGN,
      Material.DARK_OAK_WALL_SIGN,
      Material.WARPED_WALL_SIGN,
      Material.OAK_SIGN,
      Material.SPRUCE_SIGN,
      Material.BIRCH_SIGN,
      Material.ACACIA_SIGN,
      Material.JUNGLE_SIGN,
      Material.DARK_OAK_SIGN,
      Material.WARPED_SIGN,
      Material.LEVER,
      Material.STONE_BUTTON,
      Material.POLISHED_BLACKSTONE_BUTTON,
      Material.OAK_BUTTON,
      Material.SPRUCE_BUTTON,
      Material.BIRCH_BUTTON,
      Material.JUNGLE_BUTTON,
      Material.ACACIA_BUTTON,
      Material.DARK_OAK_BUTTON,
      Material.CRIMSON_BUTTON,
      Material.WARPED_BUTTON,
      Material.STONE_PRESSURE_PLATE,
      Material.POLISHED_BLACKSTONE_PRESSURE_PLATE,
      Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
      Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
      Material.OAK_PRESSURE_PLATE,
      Material.SPRUCE_PRESSURE_PLATE,
      Material.BIRCH_PRESSURE_PLATE,
      Material.JUNGLE_PRESSURE_PLATE,
      Material.ACACIA_PRESSURE_PLATE,
      Material.DARK_OAK_PRESSURE_PLATE,
      Material.CRIMSON_PRESSURE_PLATE,
      Material.WARPED_PRESSURE_PLATE,
      Material.DROPPER,
      Material.PISTON,
      Material.OAK_SAPLING,
      Material.SPRUCE_SAPLING,
      Material.BIRCH_SAPLING,
      Material.JUNGLE_SAPLING,
      Material.ACACIA_SAPLING,
      Material.DARK_OAK_SAPLING,
      Material.DEAD_BUSH,
      Material.SEA_PICKLE,
      Material.CORNFLOWER,
      Material.LILY_OF_THE_VALLEY,
      Material.WITHER_ROSE,
      Material.SPORE_BLOSSOM,
      Material.IRON_BLOCK,
      Material.COPPER_BLOCK,
      Material.DIAMOND_BLOCK,
      Material.NETHERITE_BLOCK,
      Material.FLOWER_POT,
      Material.ANVIL,
      Material.BEACON,
      Material.ENCHANTING_TABLE,
      Material.LADDER,
      Material.SNOW,
      Material.WITHER_SKELETON_SKULL,
      Material.DRAGON_HEAD,
      Material.PLAYER_HEAD,
      Material.SHULKER_BOX,
      Material.JUKEBOX,
      Material.HOPPER,
      Material.DAYLIGHT_DETECTOR,
      Material.OBSERVER,
      Material.CAKE,
      Material.NOTE_BLOCK,
      Material.REDSTONE_LAMP,
      Material.EMERALD_BLOCK,
      Material.COAL_BLOCK,
      Material.LAPIS_BLOCK
    )

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
    Material.WOODEN_AXE,
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
    Material.GOLDEN_SHOVEL,
    Material.NETHERITE_PICKAXE,
    Material.NETHERITE_AXE,
    Material.NETHERITE_SHOVEL
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
