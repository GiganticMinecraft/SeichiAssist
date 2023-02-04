package com.github.unchama.seichiassist.subsystems.minestack.bukkit

import cats.Functor
import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.minecraft.bukkit.algebra.CloneableBukkitItemStack._
import com.github.unchama.minecraft.objects.MinecraftMaterial
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaPrizeAPI
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObject.{
  MineStackObjectByItemStack,
  MineStackObjectByMaterial
}
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObjectCategory._
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject._
import com.github.unchama.seichiassist.subsystems.minestack.domain.persistence.MineStackGachaObjectPersistence
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class BukkitMineStackObjectList[F[_]: Sync](
  implicit gachaPrizeAPI: GachaPrizeAPI[F, ItemStack, Player],
  mineStackGachaObjectPersistence: MineStackGachaObjectPersistence[F, ItemStack],
  minecraftMaterial: MinecraftMaterial[Material, ItemStack]
) extends MineStackObjectList[F, ItemStack, Player] {

  private def leftElems[A](elems: A*): List[Either[A, Nothing]] = elems.toList.map(Left.apply)

  private def rightElems[B](elems: B*): List[Either[Nothing, B]] = elems.toList.map(Right.apply)

  // @formatter:off

  // 採掘可能ブロック
  private val minestacklistmine: List[MineStackObjectGroup[ItemStack]] = leftElems(
    MineStackObjectByMaterial(ORES, "coal_ore", "石炭鉱石", Material.COAL_ORE, 0),
    MineStackObjectByMaterial(ORES, "coal", "石炭", Material.COAL, 0),
    MineStackObjectByMaterial(ORES, "coal_block", "石炭ブロック", Material.COAL_BLOCK, 0),
    MineStackObjectByMaterial(ORES, "coal_1", "木炭", Material.COAL, 1),
    MineStackObjectByMaterial(ORES, "iron_ore", "鉄鉱石", Material.IRON_ORE, 0),
    MineStackObjectByMaterial(ORES, "iron_ingot", "鉄インゴット", Material.IRON_INGOT, 0),
    MineStackObjectByMaterial(ORES, "iron_block", "鉄ブロック", Material.IRON_BLOCK, 0),
    MineStackObjectByMaterial(ORES, "quartz_ore", "ネザー水晶鉱石", Material.QUARTZ_ORE, 0),
    MineStackObjectByMaterial(ORES, "quartz", "ネザー水晶", Material.QUARTZ, 0),
    MineStackObjectByMaterial(ORES, "gold_ore", "金鉱石", Material.GOLD_ORE, 0),
    MineStackObjectByMaterial(ORES, "gold_ingot", "金インゴット", Material.GOLD_INGOT, 0),
    MineStackObjectByMaterial(ORES, "gold_block", "金ブロック", Material.GOLD_BLOCK, 0),
    MineStackObjectByMaterial(ORES, "redstone_ore", "レッドストーン鉱石", Material.REDSTONE_ORE, 0),
    MineStackObjectByMaterial(ORES, "lapis_ore", "ラピスラズリ鉱石", Material.LAPIS_ORE, 0),
    MineStackObjectByMaterial(ORES, "lapis_lazuli", "ラピスラズリ", Material.INK_SACK, 4),
    MineStackObjectByMaterial(ORES, "lapis_block", "ラピスラズリブロック", Material.LAPIS_BLOCK, 0),
    MineStackObjectByMaterial(ORES, "diamond_ore", "ダイヤモンド鉱石", Material.DIAMOND_ORE, 0),
    MineStackObjectByMaterial(ORES, "diamond", "ダイヤモンド", Material.DIAMOND, 0),
    MineStackObjectByMaterial(ORES, "diamond_block", "ダイヤモンドブロック", Material.DIAMOND_BLOCK, 0),
    MineStackObjectByMaterial(ORES, "emerald_ore", "エメラルド鉱石", Material.EMERALD_ORE, 0),
    MineStackObjectByMaterial(ORES, "emerald", "エメラルド", Material.EMERALD, 0),
    MineStackObjectByMaterial(ORES, "emerald_block", "エメラルドブロック", Material.EMERALD_BLOCK, 0)
  )

  // モンスター+動物ドロップ
  private val minestacklistdrop: List[Either[MineStackObject[ItemStack], MineStackObjectWithColorVariants[ItemStack]]] = leftElems(
    MineStackObjectByMaterial(MOB_DROP, "ender_pearl", "エンダーパール", Material.ENDER_PEARL, 0),
    MineStackObjectByMaterial(MOB_DROP, "ender_eye", "エンダーアイ", Material.EYE_OF_ENDER, 0),
    MineStackObjectByMaterial(MOB_DROP, "slime_ball", "スライムボール", Material.SLIME_BALL, 0),
    MineStackObjectByMaterial(MOB_DROP, "slime", "スライムブロック", Material.SLIME_BLOCK, 0),
    MineStackObjectByMaterial(MOB_DROP, "rotten_flesh", "腐った肉", Material.ROTTEN_FLESH, 0),
    MineStackObjectByMaterial(MOB_DROP, "bone", "骨", Material.BONE, 0),
    MineStackObjectByMaterial(MOB_DROP, "sulphur", "火薬", Material.SULPHUR, 0),
    MineStackObjectByMaterial(MOB_DROP, "arrow", "矢", Material.ARROW, 0),
    MineStackObjectByMaterial(MOB_DROP, "tipped_arrow", "鈍化の矢", Material.TIPPED_ARROW, 0),
    MineStackObjectByMaterial(MOB_DROP, "spider_eye", "蜘蛛の目", Material.SPIDER_EYE, 0),
    MineStackObjectByMaterial(MOB_DROP, "string", "糸", Material.STRING, 0),
    MineStackObjectByMaterial(MOB_DROP, "name_tag", "名札", Material.NAME_TAG, 0),
    MineStackObjectByMaterial(MOB_DROP, "lead", "リード", Material.LEASH, 0),
    MineStackObjectByMaterial(MOB_DROP, "glass_bottle", "ガラス瓶", Material.GLASS_BOTTLE, 0),
    MineStackObjectByMaterial(MOB_DROP, "gold_nugget", "金塊", Material.GOLD_NUGGET, 0),
    MineStackObjectByMaterial(MOB_DROP, "blaze_rod", "ブレイズロッド", Material.BLAZE_ROD, 0),
    MineStackObjectByMaterial(MOB_DROP, "blaze_powder", "ブレイズパウダー", Material.BLAZE_POWDER, 0),
    MineStackObjectByMaterial(MOB_DROP, "ghast_tear", "ガストの涙", Material.GHAST_TEAR, 0),
    MineStackObjectByMaterial(MOB_DROP, "magma_cream", "マグマクリーム", Material.MAGMA_CREAM, 0),
    MineStackObjectByMaterial(MOB_DROP, "prismarine_shard", "プリズマリンの欠片", Material.PRISMARINE_SHARD, 0),
    MineStackObjectByMaterial(MOB_DROP, "prismarine_crystals", "プリズマリンクリスタル", Material.PRISMARINE_CRYSTALS, 0),
    MineStackObjectByMaterial(MOB_DROP, "feather", "羽", Material.FEATHER, 0),
    MineStackObjectByMaterial(MOB_DROP, "leather", "革", Material.LEATHER, 0),
    MineStackObjectByMaterial(MOB_DROP, "rabbit_hide", "ウサギの皮", Material.RABBIT_HIDE, 0),
    MineStackObjectByMaterial(MOB_DROP, "rabbit_foot", "ウサギの足", Material.RABBIT_FOOT, 0),
    MineStackObjectByMaterial(MOB_DROP, "dragon_egg", "エンドラの卵", Material.DRAGON_EGG, 0),
    MineStackObjectByMaterial(MOB_DROP, "shulker_shell", "シュルカーの殻", Material.SHULKER_SHELL, 0),
    MineStackObjectByMaterial(MOB_DROP, "totem_of_undying", "不死のトーテム", Material.TOTEM, 0),
    MineStackObjectByMaterial(MOB_DROP, "dragon_head", "エンダードラゴンの頭", Material.SKULL_ITEM, 5),
    MineStackObjectByMaterial(MOB_DROP, "wither_skeleton_skull", "ウィザースケルトンの頭", Material.SKULL_ITEM, 1),
    MineStackObjectByMaterial(MOB_DROP, "stick", "棒", Material.STICK, 0)
  )

  // 採掘で入手可能な農業系ブロック
  private val minestacklistfarm: List[MineStackObjectGroup[ItemStack]] = leftElems(
    MineStackObjectByMaterial(AGRICULTURAL, "seeds", "種", Material.SEEDS, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "apple", "リンゴ", Material.APPLE, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "long_grass1", "草", Material.LONG_GRASS, 1),
    MineStackObjectByMaterial(AGRICULTURAL, "long_grass2", "シダ", Material.LONG_GRASS, 2),
    MineStackObjectByMaterial(AGRICULTURAL, "dead_bush", "枯れ木", Material.DEAD_BUSH, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "cactus", "サボテン", Material.CACTUS, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "vine", "ツタ", Material.VINE, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "water_lily", "スイレンの葉", Material.WATER_LILY, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "yellow_flower", "タンポポ", Material.YELLOW_FLOWER, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "red_rose0", "ポピー", Material.RED_ROSE, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "red_rose1", "ヒスイラン", Material.RED_ROSE, 1),
    MineStackObjectByMaterial(AGRICULTURAL, "red_rose2", "アリウム", Material.RED_ROSE, 2),
    MineStackObjectByMaterial(AGRICULTURAL, "red_rose3", "ヒナソウ", Material.RED_ROSE, 3),
    MineStackObjectByMaterial(AGRICULTURAL, "red_rose4", "赤色のチューリップ", Material.RED_ROSE, 4),
    MineStackObjectByMaterial(AGRICULTURAL, "red_rose5", "橙色のチューリップ", Material.RED_ROSE, 5),
    MineStackObjectByMaterial(AGRICULTURAL, "red_rose6", "白色のチューリップ", Material.RED_ROSE, 6),
    MineStackObjectByMaterial(AGRICULTURAL, "red_rose7", "桃色のチューリップ", Material.RED_ROSE, 7),
    MineStackObjectByMaterial(AGRICULTURAL, "red_rose8", "フランスギク", Material.RED_ROSE, 8),
    MineStackObjectByMaterial(AGRICULTURAL, "leaves", "オークの葉", Material.LEAVES, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "leaves1", "マツの葉", Material.LEAVES, 1),
    MineStackObjectByMaterial(AGRICULTURAL, "leaves2", "シラカバの葉", Material.LEAVES, 2),
    MineStackObjectByMaterial(AGRICULTURAL, "leaves3", "ジャングルの葉", Material.LEAVES, 3),
    MineStackObjectByMaterial(AGRICULTURAL, "leaves_2", "アカシアの葉", Material.LEAVES_2, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "leaves_21", "ダークオークの葉", Material.LEAVES_2, 1),
    MineStackObjectByMaterial(AGRICULTURAL, "double_plant0", "ヒマワリ", Material.DOUBLE_PLANT, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "double_plant1", "ライラック", Material.DOUBLE_PLANT, 1),
    MineStackObjectByMaterial(AGRICULTURAL, "double_plant2", "高い草", Material.DOUBLE_PLANT, 2),
    MineStackObjectByMaterial(AGRICULTURAL, "double_plant3", "大きなシダ", Material.DOUBLE_PLANT, 3),
    MineStackObjectByMaterial(AGRICULTURAL, "double_plant4", "バラの低木", Material.DOUBLE_PLANT, 4),
    MineStackObjectByMaterial(AGRICULTURAL, "double_plant5", "ボタン", Material.DOUBLE_PLANT, 5),
    MineStackObjectByMaterial(AGRICULTURAL, "sugar_cane", "サトウキビ", Material.SUGAR_CANE, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "pumpkin", "カボチャ", Material.PUMPKIN, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "ink_sack3", "カカオ豆", Material.INK_SACK, 3),
    MineStackObjectByMaterial(AGRICULTURAL, "huge_mushroom_1", "キノコ", Material.HUGE_MUSHROOM_1, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "huge_mushroom_2", "キノコ", Material.HUGE_MUSHROOM_2, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "melon", "スイカ", Material.MELON, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "melon_block", "スイカ", Material.MELON_BLOCK, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "brown_mushroom", "キノコ", Material.BROWN_MUSHROOM, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "red_mushroom", "キノコ", Material.RED_MUSHROOM, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "sapling", "オークの苗木", Material.SAPLING, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "sapling1", "マツの苗木", Material.SAPLING, 1),
    MineStackObjectByMaterial(AGRICULTURAL, "sapling2", "シラカバの苗木", Material.SAPLING, 2),
    MineStackObjectByMaterial(AGRICULTURAL, "sapling3", "ジャングルの苗木", Material.SAPLING, 3),
    MineStackObjectByMaterial(AGRICULTURAL, "sapling4", "アカシアの苗木", Material.SAPLING, 4),
    MineStackObjectByMaterial(AGRICULTURAL, "sapling5", "ダークオークの苗木", Material.SAPLING, 5),
    MineStackObjectByMaterial(AGRICULTURAL, "beetroot", "ビートルート", Material.BEETROOT, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "beetroot_seeds", "ビートルートの種", Material.BEETROOT_SEEDS, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "carrot_item", "ニンジン", Material.CARROT_ITEM, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "potato_item", "ジャガイモ", Material.POTATO_ITEM, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "poisonous_potato", "青くなったジャガイモ", Material.POISONOUS_POTATO, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "wheat", "小麦", Material.WHEAT, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "pumpkin_seeds", "カボチャの種", Material.PUMPKIN_SEEDS, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "melon_seeds", "スイカの種", Material.MELON_SEEDS, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "nether_stalk", "ネザーウォート", Material.NETHER_STALK, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "chorus_fruit", "コーラスフルーツ", Material.CHORUS_FRUIT, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "chorus_flower", "コーラスフラワー", Material.CHORUS_FLOWER, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "popped_chorus_fruit", "焼いたコーラスフルーツ", Material.CHORUS_FRUIT_POPPED, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "egg", "卵", Material.EGG, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "pork", "生の豚肉", Material.PORK, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "cooked_porkchop", "焼き豚", Material.GRILLED_PORK, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "raw_chicken", "生の鶏肉", Material.RAW_CHICKEN, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "cooked_chicken", "焼き鳥", Material.COOKED_CHICKEN, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "mutton", "生の羊肉", Material.MUTTON, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "cooked_mutton", "焼いた羊肉", Material.COOKED_MUTTON, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "raw_beef", "生の牛肉", Material.RAW_BEEF, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "cooked_beaf", "ステーキ", Material.COOKED_BEEF, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "rabbit", "生の兎肉", Material.RABBIT, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "cooked_rabbit", "焼き兎肉", Material.COOKED_RABBIT, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "raw_fish0", "生魚", Material.RAW_FISH, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "cooked_fish0", "焼き魚", Material.COOKED_FISH, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "raw_fish1", "生鮭", Material.RAW_FISH, 1),
    MineStackObjectByMaterial(AGRICULTURAL, "cooked_fish1", "焼き鮭", Material.COOKED_FISH, 1),
    MineStackObjectByMaterial(AGRICULTURAL, "raw_fish2", "クマノミ", Material.RAW_FISH, 2),
    MineStackObjectByMaterial(AGRICULTURAL, "raw_fish3", "フグ", Material.RAW_FISH, 3),
    MineStackObjectByMaterial(AGRICULTURAL, "bread", "パン", Material.BREAD, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "sugar", "砂糖", Material.SUGAR, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "baked_potato", "ベイクドポテト", Material.BAKED_POTATO, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "cake", "ケーキ", Material.CAKE, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "mushroom_stew", "キノコシチュー", Material.MUSHROOM_SOUP, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "rabbit_stew", "ウサギシチュー", Material.RABBIT_STEW, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "beetroot_soup", "ビートルートスープ", Material.BEETROOT_SOUP, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "bowl", "ボウル", Material.BOWL, 0),
    MineStackObjectByMaterial(AGRICULTURAL, "milk_bucket", "牛乳", Material.MILK_BUCKET, 0)
  )

  // 建築系ブロック
  private val minestacklistbuild: List[MineStackObjectGroup[ItemStack]] = leftElems(
    MineStackObjectByMaterial(BUILDING, "log", "オークの原木", Material.LOG, 0),
    MineStackObjectByMaterial(BUILDING, "wood", "オークの木材", Material.WOOD, 0),
    MineStackObjectByMaterial(BUILDING, "wood_step0", "オークの木材ハーフブロック", Material.WOOD_STEP, 0),
    MineStackObjectByMaterial(BUILDING, "oak_stairs", "オークの木の階段", Material.WOOD_STAIRS, 0),
    MineStackObjectByMaterial(BUILDING, "fence", "オークのフェンス", Material.FENCE, 0),
    MineStackObjectByMaterial(BUILDING, "log1", "マツの原木", Material.LOG, 1),
    MineStackObjectByMaterial(BUILDING, "wood_1", "マツの木材", Material.WOOD, 1),
    MineStackObjectByMaterial(BUILDING, "wood_step1", "マツの木材ハーフブロック", Material.WOOD_STEP, 1),
    MineStackObjectByMaterial(BUILDING, "spruce_stairs", "マツの木の階段", Material.SPRUCE_WOOD_STAIRS, 0),
    MineStackObjectByMaterial(BUILDING, "spruce_fence", "マツのフェンス", Material.SPRUCE_FENCE, 0),
    MineStackObjectByMaterial(BUILDING, "log2", "シラカバの原木", Material.LOG, 2),
    MineStackObjectByMaterial(BUILDING, "wood_2", "シラカバの木材", Material.WOOD, 2),
    MineStackObjectByMaterial(BUILDING, "wood_step2", "シラカバの木材ハーフブロック", Material.WOOD_STEP, 2),
    MineStackObjectByMaterial(BUILDING, "birch_stairs", "シラカバの木の階段", Material.BIRCH_WOOD_STAIRS, 0),
    MineStackObjectByMaterial(BUILDING, "birch_fence", "シラカバのフェンス", Material.BIRCH_FENCE, 0),
    MineStackObjectByMaterial(BUILDING, "log3", "ジャングルの原木", Material.LOG, 3),
    MineStackObjectByMaterial(BUILDING, "wood_3", "ジャングルの木材", Material.WOOD, 3),
    MineStackObjectByMaterial(BUILDING, "wood_step3", "ジャングルの木材ハーフブロック", Material.WOOD_STEP, 3),
    MineStackObjectByMaterial(BUILDING, "jungle_stairs", "ジャングルの木の階段", Material.JUNGLE_WOOD_STAIRS, 0),
    MineStackObjectByMaterial(BUILDING, "jungle_fence", "ジャングルのフェンス", Material.JUNGLE_FENCE, 0),
    MineStackObjectByMaterial(BUILDING, "log_2", "アカシアの原木", Material.LOG_2, 0),
    MineStackObjectByMaterial(BUILDING, "wood_4", "アカシアの木材", Material.WOOD, 4),
    MineStackObjectByMaterial(BUILDING, "wood_step4", "アカシアの木材ハーフブロック", Material.WOOD_STEP, 4),
    MineStackObjectByMaterial(BUILDING, "acacia_stairs", "アカシアの木の階段", Material.ACACIA_STAIRS, 0),
    MineStackObjectByMaterial(BUILDING, "acacia_fence", "アカシアのフェンス", Material.ACACIA_FENCE, 0),
    MineStackObjectByMaterial(BUILDING, "log_21", "ダークオークの原木", Material.LOG_2, 1),
    MineStackObjectByMaterial(BUILDING, "wood_5", "ダークオークの木材", Material.WOOD, 5),
    MineStackObjectByMaterial(BUILDING, "wood_step5", "ダークオークの木材ハーフブロック", Material.WOOD_STEP, 5),
    MineStackObjectByMaterial(BUILDING, "dark_oak_stairs", "ダークオークの木の階段", Material.DARK_OAK_STAIRS, 0),
    MineStackObjectByMaterial(BUILDING, "dark_oak_fence", "ダークオークのフェンス", Material.DARK_OAK_FENCE, 0),
    MineStackObjectByMaterial(BUILDING, "cobblestone", "丸石", Material.COBBLESTONE, 0),
    MineStackObjectByMaterial(BUILDING, "step3", "丸石ハーフブロック", Material.STEP, 3),
    MineStackObjectByMaterial(BUILDING, "stone_stairs", "丸石の階段", Material.COBBLESTONE_STAIRS, 0),
    MineStackObjectByMaterial(BUILDING, "cobblestone_wall_0", "丸石の壁", Material.COBBLE_WALL, 0),
    MineStackObjectByMaterial(BUILDING, "mossy_cobblestone", "苔石", Material.MOSSY_COBBLESTONE, 0),
    MineStackObjectByMaterial(BUILDING, "cobblestone_wall_1", "苔石の壁", Material.COBBLE_WALL, 1),
    MineStackObjectByMaterial(BUILDING, "stone", "石", Material.STONE, 0),
    MineStackObjectByMaterial(BUILDING, "step0", "石ハーフブロック", Material.STEP, 0),
    MineStackObjectByMaterial(BUILDING, "smooth_brick0", "石レンガ", Material.SMOOTH_BRICK, 0),
    MineStackObjectByMaterial(BUILDING, "step5", "石レンガハーフブロック", Material.STEP, 5),
    MineStackObjectByMaterial(BUILDING, "smooth_stairs", "石レンガの階段", Material.SMOOTH_STAIRS, 0),
    MineStackObjectByMaterial(BUILDING, "smooth_brick3", "模様入り石レンガ", Material.SMOOTH_BRICK, 3),
    MineStackObjectByMaterial(BUILDING, "smooth_brick1", "苔石レンガ", Material.SMOOTH_BRICK, 1),
    MineStackObjectByMaterial(BUILDING, "smooth_brick2", "ひびの入った石レンガ", Material.SMOOTH_BRICK, 2),
    MineStackObjectByMaterial(BUILDING, "sand", "砂", Material.SAND, 0),
    MineStackObjectByMaterial(BUILDING, "sandstone", "砂岩", Material.SANDSTONE, 0),
    MineStackObjectByMaterial(BUILDING, "step1", "砂岩ハーフブロック", Material.STEP, 1),
    MineStackObjectByMaterial(BUILDING, "standstone_stairs", "砂岩の階段", Material.SANDSTONE_STAIRS, 0),
    MineStackObjectByMaterial(BUILDING, "sandstone1", "模様入りの砂岩", Material.SANDSTONE, 1),
    MineStackObjectByMaterial(BUILDING, "sandstone2", "なめらかな砂岩", Material.SANDSTONE, 2),
    MineStackObjectByMaterial(BUILDING, "red_sand", "赤い砂", Material.SAND, 1),
    MineStackObjectByMaterial(BUILDING, "red_sandstone", "赤い砂岩", Material.RED_SANDSTONE, 0),
    MineStackObjectByMaterial(BUILDING, "stone_slab20", "赤い砂岩ハーフブロック", Material.STONE_SLAB2, 0),
    MineStackObjectByMaterial(BUILDING, "red_sandstone_stairs", "赤い砂岩の階段", Material.RED_SANDSTONE_STAIRS, 0),
    MineStackObjectByMaterial(BUILDING, "red_sandstone1", "模様入りの赤い砂岩", Material.RED_SANDSTONE, 1),
    MineStackObjectByMaterial(BUILDING, "red_sandstone2", "なめらかな赤い砂岩", Material.RED_SANDSTONE, 2),
    MineStackObjectByMaterial(BUILDING, "clay_ball", "粘土", Material.CLAY_BALL, 0),
    MineStackObjectByMaterial(BUILDING, "clay", "粘土(ブロック)", Material.CLAY, 0),
    MineStackObjectByMaterial(BUILDING, "brick_item", "レンガ", Material.CLAY_BRICK, 0),
    MineStackObjectByMaterial(BUILDING, "brick", "レンガ(ブロック)", Material.BRICK, 0),
    MineStackObjectByMaterial(BUILDING, "step4", "レンガハーフブロック", Material.STEP, 4),
    MineStackObjectByMaterial(BUILDING, "brick_stairs", "レンガの階段", Material.BRICK_STAIRS, 0),
    MineStackObjectByMaterial(BUILDING, "quartz_block", "ネザー水晶ブロック", Material.QUARTZ_BLOCK, 0),
    MineStackObjectByMaterial(BUILDING, "step7", "ネザー水晶ハーフブロック", Material.STEP, 7),
    MineStackObjectByMaterial(BUILDING, "quartz_stairs", "ネザー水晶の階段", Material.QUARTZ_STAIRS, 0),
    MineStackObjectByMaterial(BUILDING, "quartz_block1", "模様入りネザー水晶ブロック", Material.QUARTZ_BLOCK, 1),
    MineStackObjectByMaterial(BUILDING, "quartz_block2", "柱状ネザー水晶ブロック", Material.QUARTZ_BLOCK, 2),
    MineStackObjectByMaterial(BUILDING, "netherrack", "ネザーラック", Material.NETHERRACK, 0),
    MineStackObjectByMaterial(BUILDING, "nether_brick_item", "ネザーレンガ", Material.NETHER_BRICK_ITEM, 0),
    MineStackObjectByMaterial(BUILDING, "nether_brick", "ネザーレンガ(ブロック)", Material.NETHER_BRICK, 0),
    MineStackObjectByMaterial(BUILDING, "step6", "ネザーレンガハーフブロック", Material.STEP, 6),
    MineStackObjectByMaterial(BUILDING, "nether_brick_stairs", "ネザーレンガの階段", Material.NETHER_BRICK_STAIRS, 0),
    MineStackObjectByMaterial(BUILDING, "nether_brick_fence", "ネザーレンガのフェンス", Material.NETHER_FENCE, 0),
    MineStackObjectByMaterial(BUILDING, "red_nether_brick", "赤いネザーレンガ", Material.RED_NETHER_BRICK, 0),
    MineStackObjectByMaterial(BUILDING, "nether_wart_block", "ネザ－ウォートブロック", Material.NETHER_WART_BLOCK, 0),
    MineStackObjectByMaterial(BUILDING, "ender_stone", "エンドストーン", Material.ENDER_STONE, 0),
    MineStackObjectByMaterial(BUILDING, "end_bricks", "エンドストーンレンガ", Material.END_BRICKS, 0),
    MineStackObjectByMaterial(BUILDING, "purpur_block", "プルパーブロック", Material.PURPUR_BLOCK, 0),
    MineStackObjectByMaterial(BUILDING, "purpur_pillar", "柱状プルパーブロック", Material.PURPUR_PILLAR, 0),
    MineStackObjectByMaterial(BUILDING, "purpur_slab", "プルパーハーフブロック", Material.PURPUR_SLAB, 0),
    MineStackObjectByMaterial(BUILDING, "purpur_stairs", "プルパーの階段", Material.PURPUR_STAIRS, 0),
    MineStackObjectByMaterial(BUILDING, "prismarine0", "プリズマリン", Material.PRISMARINE, 0),
    MineStackObjectByMaterial(BUILDING, "prismarine1", "プリズマリンレンガ", Material.PRISMARINE, 1),
    MineStackObjectByMaterial(BUILDING, "prismarine2", "ダークプリズマリン", Material.PRISMARINE, 2),
    MineStackObjectByMaterial(BUILDING, "sea_lantern", "シーランタン", Material.SEA_LANTERN, 0),
    MineStackObjectByMaterial(BUILDING, "granite", "花崗岩", Material.STONE, 1),
    MineStackObjectByMaterial(BUILDING, "polished_granite", "磨かれた花崗岩", Material.STONE, 2),
    MineStackObjectByMaterial(BUILDING, "diorite", "閃緑岩", Material.STONE, 3),
    MineStackObjectByMaterial(BUILDING, "polished_diorite", "磨かれた閃緑岩", Material.STONE, 4),
    MineStackObjectByMaterial(BUILDING, "andesite", "安山岩", Material.STONE, 5),
    MineStackObjectByMaterial(BUILDING, "polished_andesite", "磨かれた安山岩", Material.STONE, 6),
    MineStackObjectByMaterial(BUILDING, "dirt", "土", Material.DIRT, 0),
    MineStackObjectByMaterial(BUILDING, "grass", "草ブロック", Material.GRASS, 0),
    MineStackObjectByMaterial(BUILDING, "gravel", "砂利", Material.GRAVEL, 0),
    MineStackObjectByMaterial(BUILDING, "flint", "火打石", Material.FLINT, 0),
    MineStackObjectByMaterial(BUILDING, "flint_and_steel", "火打石と打ち金", Material.FLINT_AND_STEEL, 0),
    MineStackObjectByMaterial(BUILDING, "dirt1", "粗い土", Material.DIRT, 1),
    MineStackObjectByMaterial(BUILDING, "dirt2", "ポドゾル", Material.DIRT, 2),
    MineStackObjectByMaterial(BUILDING, "snow_block", "雪", Material.SNOW_BLOCK, 0),
    MineStackObjectByMaterial(BUILDING, "snow_layer", "雪タイル", Material.SNOW, 0),
    MineStackObjectByMaterial(BUILDING, "snow_ball", "雪玉", Material.SNOW_BALL, 0),
    MineStackObjectByMaterial(BUILDING, "ice", "氷", Material.ICE, 0),
    MineStackObjectByMaterial(BUILDING, "packed_ice", "氷塊", Material.PACKED_ICE, 0),
    MineStackObjectByMaterial(BUILDING, "mycel", "菌糸", Material.MYCEL, 0),
    MineStackObjectByMaterial(BUILDING, "bone_block", "骨ブロック", Material.BONE_BLOCK, 0),
    MineStackObjectByMaterial(BUILDING, "sponge", "スポンジ", Material.SPONGE, 0),
    MineStackObjectByMaterial(BUILDING, "wet_sponge", "濡れたスポンジ", Material.SPONGE, 1),
    MineStackObjectByMaterial(BUILDING, "soul_sand", "ソウルサンド", Material.SOUL_SAND, 0),
    MineStackObjectByMaterial(BUILDING, "magma", "マグマブロック", Material.MAGMA, 0),
    MineStackObjectByMaterial(BUILDING, "obsidian", "黒曜石", Material.OBSIDIAN, 0),
    MineStackObjectByMaterial(BUILDING, "glowstone_dust", "グロウストーンダスト", Material.GLOWSTONE_DUST, 0),
    MineStackObjectByMaterial(BUILDING, "glowstone", "グロウストーン", Material.GLOWSTONE, 0),
    MineStackObjectByMaterial(BUILDING, "torch", "松明", Material.TORCH, 0),
    MineStackObjectByMaterial(BUILDING, "jack_o_lantern", "ジャック・オ・ランタン", Material.JACK_O_LANTERN, 0),
    MineStackObjectByMaterial(BUILDING, "end_rod", "エンドロッド", Material.END_ROD, 0),
    MineStackObjectByMaterial(BUILDING, "bucket", "バケツ", Material.BUCKET, 0),
    MineStackObjectByMaterial(BUILDING, "water_bucket", "水入りバケツ", Material.WATER_BUCKET, 0),
    MineStackObjectByMaterial(BUILDING, "lava_bucket", "溶岩入りバケツ", Material.LAVA_BUCKET, 0),
    MineStackObjectByMaterial(BUILDING, "web", "クモの巣", Material.WEB, 0),
    MineStackObjectByMaterial(BUILDING, "rails", "レール", Material.RAILS, 0),
    MineStackObjectByMaterial(BUILDING, "furnace", "かまど", Material.FURNACE, 0),
    MineStackObjectByMaterial(BUILDING, "chest", "チェスト", Material.CHEST, 0),
    MineStackObjectByMaterial(BUILDING, "book", "本", Material.BOOK, 0),
    MineStackObjectByMaterial(BUILDING, "bookshelf", "本棚", Material.BOOKSHELF, 0),
    MineStackObjectByMaterial(BUILDING, "iron_bars", "鉄格子", Material.IRON_FENCE, 0),
    MineStackObjectByMaterial(BUILDING, "anvil", "金床", Material.ANVIL, 0),
    MineStackObjectByMaterial(BUILDING, "cauldron", "大釜", Material.CAULDRON_ITEM, 0),
    MineStackObjectByMaterial(BUILDING, "brewing_stand", "醸造台", Material.BREWING_STAND_ITEM, 0),
    MineStackObjectByMaterial(BUILDING, "flower_pot", "植木鉢", Material.FLOWER_POT_ITEM, 0),
    MineStackObjectByMaterial(BUILDING, "hay_block", "干し草の俵", Material.HAY_BLOCK, 0),
    MineStackObjectByMaterial(BUILDING, "ladder", "はしご", Material.LADDER, 0),
    MineStackObjectByMaterial(BUILDING, "sign", "看板", Material.SIGN, 0),
    MineStackObjectByMaterial(BUILDING, "item_frame", "額縁", Material.ITEM_FRAME, 0),
    MineStackObjectByMaterial(BUILDING, "painting", "絵画", Material.PAINTING, 0),
    MineStackObjectByMaterial(BUILDING, "beacon", "ビーコン", Material.BEACON, 0),
    MineStackObjectByMaterial(BUILDING, "armor_stand", "アーマースタンド", Material.ARMOR_STAND, 0),
    MineStackObjectByMaterial(BUILDING, "end_crystal", "エンドクリスタル", Material.END_CRYSTAL, 0),
    MineStackObjectByMaterial(BUILDING, "enchanting_table", "エンチャントテーブル", Material.ENCHANTMENT_TABLE, 0),
    MineStackObjectByMaterial(BUILDING, "jukebox", "ジュークボックス", Material.JUKEBOX, 0),
    MineStackObjectByMaterial(BUILDING, "hard_clay", "テラコッタ", Material.HARD_CLAY, 0),
    MineStackObjectByMaterial(BUILDING, "workbench", "作業台", Material.WORKBENCH, 0)
  ) ++ rightElems(
    MineStackObjectWithColorVariants(
      MineStackObjectByMaterial(BUILDING, "bed", "白色のベッド", Material.BED, 0),
      List(
        MineStackObjectByMaterial(BUILDING, "bed_1", "橙色のベッド", Material.BED, 1),
        MineStackObjectByMaterial(BUILDING, "bed_2", "赤紫色のベッド", Material.BED, 2),
        MineStackObjectByMaterial(BUILDING, "bed_3", "空色のベッド", Material.BED, 3),
        MineStackObjectByMaterial(BUILDING, "bed_4", "黄色のベッド", Material.BED, 4),
        MineStackObjectByMaterial(BUILDING, "bed_5", "黄緑色のベッド", Material.BED, 5),
        MineStackObjectByMaterial(BUILDING, "bed_6", "桃色のベッド", Material.BED, 6),
        MineStackObjectByMaterial(BUILDING, "bed_7", "灰色のベッド", Material.BED, 7),
        MineStackObjectByMaterial(BUILDING, "bed_8", "薄灰色のベッド", Material.BED, 8),
        MineStackObjectByMaterial(BUILDING, "bed_9", "青緑色のベッド", Material.BED, 9),
        MineStackObjectByMaterial(BUILDING, "bed_10", "紫色のベッド", Material.BED, 10),
        MineStackObjectByMaterial(BUILDING, "bed_11", "青色のベッド", Material.BED, 11),
        MineStackObjectByMaterial(BUILDING, "bed_12", "茶色のベッド", Material.BED, 12),
        MineStackObjectByMaterial(BUILDING, "bed_13", "緑色のベッド", Material.BED, 13),
        MineStackObjectByMaterial(BUILDING, "bed_14", "赤色のベッド", Material.BED, 14),
        MineStackObjectByMaterial(BUILDING, "bed_15", "黒色のベッド", Material.BED, 15)
      )
    ),
    MineStackObjectWithColorVariants(
      MineStackObjectByMaterial(BUILDING, "stained_clay", "白色のテラコッタ", Material.STAINED_CLAY, 0),
      List(
        MineStackObjectByMaterial(BUILDING, "stained_clay1", "橙色のテラコッタ", Material.STAINED_CLAY, 1),
        MineStackObjectByMaterial(BUILDING, "stained_clay2", "赤紫色のテラコッタ", Material.STAINED_CLAY, 2),
        MineStackObjectByMaterial(BUILDING, "stained_clay3", "空色のテラコッタ", Material.STAINED_CLAY, 3),
        MineStackObjectByMaterial(BUILDING, "stained_clay4", "黄色のテラコッタ", Material.STAINED_CLAY, 4),
        MineStackObjectByMaterial(BUILDING, "stained_clay5", "黄緑色のテラコッタ", Material.STAINED_CLAY, 5),
        MineStackObjectByMaterial(BUILDING, "stained_clay6", "桃色のテラコッタ", Material.STAINED_CLAY, 6),
        MineStackObjectByMaterial(BUILDING, "stained_clay7", "灰色のテラコッタ", Material.STAINED_CLAY, 7),
        MineStackObjectByMaterial(BUILDING, "stained_clay8", "薄灰色のテラコッタ", Material.STAINED_CLAY, 8),
        MineStackObjectByMaterial(BUILDING, "stained_clay9", "青緑色のテラコッタ", Material.STAINED_CLAY, 9),
        MineStackObjectByMaterial(BUILDING, "stained_clay10", "紫色のテラコッタ", Material.STAINED_CLAY, 10),
        MineStackObjectByMaterial(BUILDING, "stained_clay11", "青色のテラコッタ", Material.STAINED_CLAY, 11),
        MineStackObjectByMaterial(BUILDING, "stained_clay12", "茶色のテラコッタ", Material.STAINED_CLAY, 12),
        MineStackObjectByMaterial(BUILDING, "stained_clay13", "緑色のテラコッタ", Material.STAINED_CLAY, 13),
        MineStackObjectByMaterial(BUILDING, "stained_clay14", "赤色のテラコッタ", Material.STAINED_CLAY, 14),
        MineStackObjectByMaterial(BUILDING, "stained_clay15", "黒色のテラコッタ", Material.STAINED_CLAY, 15)
      )
    ),
    MineStackObjectWithColorVariants(
      MineStackObjectByMaterial(BUILDING, "concrete", "白色のコンクリート", Material.CONCRETE, 0),
      List(
        MineStackObjectByMaterial(BUILDING, "concrete1", "橙色のコンクリート", Material.CONCRETE, 1),
        MineStackObjectByMaterial(BUILDING, "concrete2", "赤紫色のコンクリート", Material.CONCRETE, 2),
        MineStackObjectByMaterial(BUILDING, "concrete3", "空色のコンクリート", Material.CONCRETE, 3),
        MineStackObjectByMaterial(BUILDING, "concrete4", "黄色のコンクリート", Material.CONCRETE, 4),
        MineStackObjectByMaterial(BUILDING, "concrete5", "黄緑色のコンクリート", Material.CONCRETE, 5),
        MineStackObjectByMaterial(BUILDING, "concrete6", "桃色のコンクリート", Material.CONCRETE, 6),
        MineStackObjectByMaterial(BUILDING, "concrete7", "灰色のコンクリート", Material.CONCRETE, 7),
        MineStackObjectByMaterial(BUILDING, "concrete8", "薄灰色のコンクリート", Material.CONCRETE, 8),
        MineStackObjectByMaterial(BUILDING, "concrete9", "青緑色のコンクリート", Material.CONCRETE, 9),
        MineStackObjectByMaterial(BUILDING, "concrete10", "紫色のコンクリート", Material.CONCRETE, 10),
        MineStackObjectByMaterial(BUILDING, "concrete11", "青色のコンクリート", Material.CONCRETE, 11),
        MineStackObjectByMaterial(BUILDING, "concrete12", "茶色のコンクリート", Material.CONCRETE, 12),
        MineStackObjectByMaterial(BUILDING, "concrete13", "緑色のコンクリート", Material.CONCRETE, 13),
        MineStackObjectByMaterial(BUILDING, "concrete14", "赤色のコンクリート", Material.CONCRETE, 14),
        MineStackObjectByMaterial(BUILDING, "concrete15", "黒色のコンクリート", Material.CONCRETE, 15)
      )
    ),
    MineStackObjectWithColorVariants(
      MineStackObjectByMaterial(BUILDING, "concrete_powder", "白色のコンクリートパウダー", Material.CONCRETE_POWDER, 0),
      List(
        MineStackObjectByMaterial(BUILDING, "concrete_powder1", "橙色のコンクリートパウダー", Material.CONCRETE_POWDER, 1),
        MineStackObjectByMaterial(BUILDING, "concrete_powder2", "赤紫色のコンクリートパウダー", Material.CONCRETE_POWDER, 2),
        MineStackObjectByMaterial(BUILDING, "concrete_powder3", "空色のコンクリートパウダー", Material.CONCRETE_POWDER, 3),
        MineStackObjectByMaterial(BUILDING, "concrete_powder4", "黄色のコンクリートパウダー", Material.CONCRETE_POWDER, 4),
        MineStackObjectByMaterial(BUILDING, "concrete_powder5", "黄緑色のコンクリートパウダー", Material.CONCRETE_POWDER, 5),
        MineStackObjectByMaterial(BUILDING, "concrete_powder6", "桃色のコンクリートパウダー", Material.CONCRETE_POWDER, 6),
        MineStackObjectByMaterial(BUILDING, "concrete_powder7", "灰色のコンクリートパウダー", Material.CONCRETE_POWDER, 7),
        MineStackObjectByMaterial(BUILDING, "concrete_powder8", "薄灰色のコンクリートパウダー", Material.CONCRETE_POWDER, 8),
        MineStackObjectByMaterial(BUILDING, "concrete_powder9", "青緑色のコンクリートパウダー", Material.CONCRETE_POWDER, 9),
        MineStackObjectByMaterial(BUILDING, "concrete_powder10", "紫色のコンクリートパウダー", Material.CONCRETE_POWDER, 10),
        MineStackObjectByMaterial(BUILDING, "concrete_powder11", "青色のコンクリートパウダー", Material.CONCRETE_POWDER, 11),
        MineStackObjectByMaterial(BUILDING, "concrete_powder12", "茶色のコンクリートパウダー", Material.CONCRETE_POWDER, 12),
        MineStackObjectByMaterial(BUILDING, "concrete_powder13", "緑色のコンクリートパウダー", Material.CONCRETE_POWDER, 13),
        MineStackObjectByMaterial(BUILDING, "concrete_powder14", "赤色のコンクリートパウダー", Material.CONCRETE_POWDER, 14),
        MineStackObjectByMaterial(BUILDING, "concrete_powder15", "黒色のコンクリートパウダー", Material.CONCRETE_POWDER, 15)
      )
    ),
    MineStackObjectWithColorVariants(
      MineStackObjectByMaterial(BUILDING, "white_glazed_terracotta", "白色の彩釉テラコッタ", Material.WHITE_GLAZED_TERRACOTTA, 0),
      List(
        MineStackObjectByMaterial(BUILDING, "orange_glazed_terracotta", "橙色の彩釉テラコッタ", Material.ORANGE_GLAZED_TERRACOTTA, 0),
        MineStackObjectByMaterial(BUILDING, "magenta_glazed_terracotta", "赤紫色の彩釉テラコッタ", Material.MAGENTA_GLAZED_TERRACOTTA, 0),
        MineStackObjectByMaterial(BUILDING, "light_blue_glazed_terracotta", "空色の彩釉テラコッタ", Material.LIGHT_BLUE_GLAZED_TERRACOTTA, 0),
        MineStackObjectByMaterial(BUILDING, "yellow_glazed_terracotta", "黄色の彩釉テラコッタ", Material.YELLOW_GLAZED_TERRACOTTA, 0),
        MineStackObjectByMaterial(BUILDING, "lime_glazed_terracotta", "黄緑色の彩釉テラコッタ", Material.LIME_GLAZED_TERRACOTTA, 0),
        MineStackObjectByMaterial(BUILDING, "pink_glazed_terracotta", "桃色の彩釉テラコッタ", Material.PINK_GLAZED_TERRACOTTA, 0),
        MineStackObjectByMaterial(BUILDING, "gray_glazed_terracotta", "灰色の彩釉テラコッタ", Material.GRAY_GLAZED_TERRACOTTA, 0),
        MineStackObjectByMaterial(BUILDING, "silver_glazed_terracotta", "薄灰色の彩釉テラコッタ", Material.SILVER_GLAZED_TERRACOTTA, 0),
        MineStackObjectByMaterial(BUILDING, "cyan_glazed_terracotta", "青緑色の彩釉テラコッタ", Material.CYAN_GLAZED_TERRACOTTA, 0),
        MineStackObjectByMaterial(BUILDING, "purple_glazed_terracotta", "紫色の彩釉テラコッタ", Material.PURPLE_GLAZED_TERRACOTTA, 0),
        MineStackObjectByMaterial(BUILDING, "blue_glazed_terracotta", "青色の彩釉テラコッタ", Material.BLUE_GLAZED_TERRACOTTA, 0),
        MineStackObjectByMaterial(BUILDING, "brown_glazed_terracotta", "茶色の彩釉テラコッタ", Material.BROWN_GLAZED_TERRACOTTA, 0),
        MineStackObjectByMaterial(BUILDING, "green_glazed_terracotta", "緑色の彩釉テラコッタ", Material.GREEN_GLAZED_TERRACOTTA, 0),
        MineStackObjectByMaterial(BUILDING, "red_glazed_terracotta", "赤色の彩釉テラコッタ", Material.RED_GLAZED_TERRACOTTA, 0),
        MineStackObjectByMaterial(BUILDING, "black_glazed_terracotta", "黒色の彩釉テラコッタ", Material.BLACK_GLAZED_TERRACOTTA, 0)
      )
    ),
    MineStackObjectWithColorVariants(
      MineStackObjectByMaterial(BUILDING, "wool_0", "羊毛", Material.WOOL, 0),
      List(
        MineStackObjectByMaterial(BUILDING, "wool_1", "橙色の羊毛", Material.WOOL, 1),
        MineStackObjectByMaterial(BUILDING, "wool_2", "赤紫色の羊毛", Material.WOOL, 2),
        MineStackObjectByMaterial(BUILDING, "wool_3", "空色の羊毛", Material.WOOL, 3),
        MineStackObjectByMaterial(BUILDING, "wool_4", "黄色の羊毛", Material.WOOL, 4),
        MineStackObjectByMaterial(BUILDING, "wool_5", "黄緑色の羊毛", Material.WOOL, 5),
        MineStackObjectByMaterial(BUILDING, "wool_6", "桃色の羊毛", Material.WOOL, 6),
        MineStackObjectByMaterial(BUILDING, "wool_7", "灰色の羊毛", Material.WOOL, 7),
        MineStackObjectByMaterial(BUILDING, "wool_8", "薄灰色の羊毛", Material.WOOL, 8),
        MineStackObjectByMaterial(BUILDING, "wool_9", "青緑色の羊毛", Material.WOOL, 9),
        MineStackObjectByMaterial(BUILDING, "wool_10", "紫色の羊毛", Material.WOOL, 10),
        MineStackObjectByMaterial(BUILDING, "wool_11", "青色の羊毛", Material.WOOL, 11),
        MineStackObjectByMaterial(BUILDING, "wool_12", "茶色の羊毛", Material.WOOL, 12),
        MineStackObjectByMaterial(BUILDING, "wool_13", "緑色の羊毛", Material.WOOL, 13),
        MineStackObjectByMaterial(BUILDING, "wool_14", "赤色の羊毛", Material.WOOL, 14),
        MineStackObjectByMaterial(BUILDING, "wool_15", "黒色の羊毛", Material.WOOL, 15)
      )
    ),
    MineStackObjectWithColorVariants(
      MineStackObjectByMaterial(BUILDING, "carpet_0", "カーペット", Material.CARPET, 0),
      List(
        MineStackObjectByMaterial(BUILDING, "carpet_1", "橙色のカーペット", Material.CARPET, 1),
        MineStackObjectByMaterial(BUILDING, "carpet_2", "赤紫色のカーペット", Material.CARPET, 2),
        MineStackObjectByMaterial(BUILDING, "carpet_3", "空色のカーペット", Material.CARPET, 3),
        MineStackObjectByMaterial(BUILDING, "carpet_4", "黄色のカーペット", Material.CARPET, 4),
        MineStackObjectByMaterial(BUILDING, "carpet_5", "黄緑色のカーペット", Material.CARPET, 5),
        MineStackObjectByMaterial(BUILDING, "carpet_6", "桃色のカーペット", Material.CARPET, 6),
        MineStackObjectByMaterial(BUILDING, "carpet_7", "灰色のカーペット", Material.CARPET, 7),
        MineStackObjectByMaterial(BUILDING, "carpet_8", "薄灰色のカーペット", Material.CARPET, 8),
        MineStackObjectByMaterial(BUILDING, "carpet_9", "青緑色のカーペット", Material.CARPET, 9),
        MineStackObjectByMaterial(BUILDING, "carpet_10", "紫色のカーペット", Material.CARPET, 10),
        MineStackObjectByMaterial(BUILDING, "carpet_11", "青色のカーペット", Material.CARPET, 11),
        MineStackObjectByMaterial(BUILDING, "carpet_12", "茶色のカーペット", Material.CARPET, 12),
        MineStackObjectByMaterial(BUILDING, "carpet_13", "緑色のカーペット", Material.CARPET, 13),
        MineStackObjectByMaterial(BUILDING, "carpet_14", "赤色のカーペット", Material.CARPET, 14),
        MineStackObjectByMaterial(BUILDING, "carpet_15", "黒色のカーペット", Material.CARPET, 15)
      )
    ),
    MineStackObjectWithColorVariants(
      MineStackObjectByMaterial(BUILDING, "glass", "ガラス", Material.GLASS, 0),
      List(
        MineStackObjectByMaterial(BUILDING, "stained_glass_0", "白色の色付きガラス", Material.STAINED_GLASS, 0),
        MineStackObjectByMaterial(BUILDING, "stained_glass_1", "橙色の色付きガラス", Material.STAINED_GLASS, 1),
        MineStackObjectByMaterial(BUILDING, "stained_glass_2", "赤紫色の色付きガラス", Material.STAINED_GLASS, 2),
        MineStackObjectByMaterial(BUILDING, "stained_glass_3", "空色の色付きガラス", Material.STAINED_GLASS, 3),
        MineStackObjectByMaterial(BUILDING, "stained_glass_4", "黄色の色付きガラス", Material.STAINED_GLASS, 4),
        MineStackObjectByMaterial(BUILDING, "stained_glass_5", "黄緑色の色付きガラス", Material.STAINED_GLASS, 5),
        MineStackObjectByMaterial(BUILDING, "stained_glass_6", "桃色の色付きガラス", Material.STAINED_GLASS, 6),
        MineStackObjectByMaterial(BUILDING, "stained_glass_7", "灰色の色付きガラス", Material.STAINED_GLASS, 7),
        MineStackObjectByMaterial(BUILDING, "stained_glass_8", "薄灰色の色付きガラス", Material.STAINED_GLASS, 8),
        MineStackObjectByMaterial(BUILDING, "stained_glass_9", "青緑色の色付きガラス", Material.STAINED_GLASS, 9),
        MineStackObjectByMaterial(BUILDING, "stained_glass_10", "紫色の色付きガラス", Material.STAINED_GLASS, 10),
        MineStackObjectByMaterial(BUILDING, "stained_glass_11", "青色の色付きガラス", Material.STAINED_GLASS, 11),
        MineStackObjectByMaterial(BUILDING, "stained_glass_12", "茶色の色付きガラス", Material.STAINED_GLASS, 12),
        MineStackObjectByMaterial(BUILDING, "stained_glass_13", "緑色の色付きガラス", Material.STAINED_GLASS, 13),
        MineStackObjectByMaterial(BUILDING, "stained_glass_14", "赤色の色付きガラス", Material.STAINED_GLASS, 14),
        MineStackObjectByMaterial(BUILDING, "stained_glass_15", "黒色の色付きガラス", Material.STAINED_GLASS, 15)
      )
    ),
    MineStackObjectWithColorVariants(
      MineStackObjectByMaterial(BUILDING, "glass_panel", "板ガラス", Material.THIN_GLASS, 0),
      List(
        MineStackObjectByMaterial(BUILDING, "glass_panel_0", "白色の色付きガラス板", Material.STAINED_GLASS_PANE, 0),
        MineStackObjectByMaterial(BUILDING, "glass_panel_1", "橙色の色付きガラス板", Material.STAINED_GLASS_PANE, 1),
        MineStackObjectByMaterial(BUILDING, "glass_panel_2", "赤紫色の色付きガラス板", Material.STAINED_GLASS_PANE, 2),
        MineStackObjectByMaterial(BUILDING, "glass_panel_3", "空色の色付きガラス板", Material.STAINED_GLASS_PANE, 3),
        MineStackObjectByMaterial(BUILDING, "glass_panel_4", "黄色の色付きガラス板", Material.STAINED_GLASS_PANE, 4),
        MineStackObjectByMaterial(BUILDING, "glass_panel_5", "黄緑色の色付きガラス板", Material.STAINED_GLASS_PANE, 5),
        MineStackObjectByMaterial(BUILDING, "glass_panel_6", "桃色の色付きガラス板", Material.STAINED_GLASS_PANE, 6),
        MineStackObjectByMaterial(BUILDING, "glass_panel_7", "灰色の色付きガラス板", Material.STAINED_GLASS_PANE, 7),
        MineStackObjectByMaterial(BUILDING, "glass_panel_8", "薄灰色の色付きガラス板", Material.STAINED_GLASS_PANE, 8),
        MineStackObjectByMaterial(BUILDING, "glass_panel_9", "青緑色の色付きガラス板", Material.STAINED_GLASS_PANE, 9),
        MineStackObjectByMaterial(BUILDING, "glass_panel_10", "紫色の色付きガラス板", Material.STAINED_GLASS_PANE, 10),
        MineStackObjectByMaterial(BUILDING, "glass_panel_11", "青色の色付きガラス板", Material.STAINED_GLASS_PANE, 11),
        MineStackObjectByMaterial(BUILDING, "glass_panel_12", "茶色の色付きガラス板", Material.STAINED_GLASS_PANE, 12),
        MineStackObjectByMaterial(BUILDING, "glass_panel_13", "緑色の色付きガラス板", Material.STAINED_GLASS_PANE, 13),
        MineStackObjectByMaterial(BUILDING, "glass_panel_14", "赤色の色付きガラス板", Material.STAINED_GLASS_PANE, 14),
        MineStackObjectByMaterial(BUILDING, "glass_panel_15", "黒色の色付きガラス板", Material.STAINED_GLASS_PANE, 15)
      )
    ),
    MineStackObjectWithColorVariants(
      MineStackObjectByMaterial(BUILDING, "dye_1", "赤色の染料", Material.INK_SACK, 1),
      List(
        MineStackObjectByMaterial(BUILDING, "dye_2", "緑色の染料", Material.INK_SACK, 2),
        MineStackObjectByMaterial(BUILDING, "dye_5", "紫色の染料", Material.INK_SACK, 5),
        MineStackObjectByMaterial(BUILDING, "dye_6", "青緑色の染料", Material.INK_SACK, 6),
        MineStackObjectByMaterial(BUILDING, "dye_7", "薄灰色の染料", Material.INK_SACK, 7),
        MineStackObjectByMaterial(BUILDING, "dye_8", "灰色の染料", Material.INK_SACK, 8),
        MineStackObjectByMaterial(BUILDING, "dye_9", "桃色の染料", Material.INK_SACK, 9),
        MineStackObjectByMaterial(BUILDING, "dye_10", "黄緑色の染料", Material.INK_SACK, 10),
        MineStackObjectByMaterial(BUILDING, "dye_11", "黄色の染料", Material.INK_SACK, 11),
        MineStackObjectByMaterial(BUILDING, "dye_12", "空色の染料", Material.INK_SACK, 12),
        MineStackObjectByMaterial(BUILDING, "dye_13", "赤紫色の染料", Material.INK_SACK, 13),
        MineStackObjectByMaterial(BUILDING, "dye_14", "橙色の染料", Material.INK_SACK, 14),
        MineStackObjectByMaterial(BUILDING, "dye_15", "骨粉", Material.INK_SACK, 15),
        MineStackObjectByMaterial(BUILDING, "ink_sack0", "イカスミ", Material.INK_SACK, 0)
      )
    )
  )

  // レッドストーン系ブロック
  private val minestacklistrs: List[MineStackObjectGroup[ItemStack]] = leftElems(
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "redstone", "レッドストーン", Material.REDSTONE, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "stone_button", "石のボタン", Material.STONE_BUTTON, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "wood_button", "木のボタン", Material.WOOD_BUTTON, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "stone_plate", "石の感圧版", Material.STONE_PLATE, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "wood_plate", "木の感圧版", Material.WOOD_PLATE, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "fence_gate", "オークのフェンスゲート", Material.FENCE_GATE, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "spruce_fence_gate", "マツのフェンスゲート", Material.SPRUCE_FENCE_GATE, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "birch_fence_gate", "シラカバのフェンスゲート", Material.BIRCH_FENCE_GATE, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "jungle_fence_gate", "ジャングルのフェンスゲート", Material.JUNGLE_FENCE_GATE, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "dark_oak_fence_gate", "ダークオークのフェンスゲート", Material.DARK_OAK_FENCE_GATE, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "acacia_fence_gate", "アカシアのフェンスゲート", Material.ACACIA_FENCE_GATE, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "redstone_block", "レッドストーンブロック", Material.REDSTONE_BLOCK, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "lever", "レバー", Material.LEVER, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "redstone_torch_on", "レッドストーントーチ", Material.REDSTONE_TORCH_ON, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "trap_door", "木のトラップドア", Material.TRAP_DOOR, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "iron_trapdoor", "鉄のトラップドア", Material.IRON_TRAPDOOR, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "gold_plate", "重量感圧版 (軽) ", Material.GOLD_PLATE, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "iron_plate", "重量感圧版 (重) ", Material.IRON_PLATE, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "wood_door", "オークのドア", Material.WOOD_DOOR, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "spruce_door_item", "マツのドア", Material.SPRUCE_DOOR_ITEM, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "birch_door_item", "シラカバのドア", Material.BIRCH_DOOR_ITEM, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "jungle_door_item", "ジャングルのドア", Material.JUNGLE_DOOR_ITEM, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "acacia_door_item", "アカシアのドア", Material.ACACIA_DOOR_ITEM, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "dark_oak_door_item", "ダークオークのドア", Material.DARK_OAK_DOOR_ITEM, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "note_block", "音符ブロック", Material.NOTE_BLOCK, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "redstone_lamp_off", "レッドストーンランプ", Material.REDSTONE_LAMP_OFF, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "tripwire_hook", "トリップワイヤーフック", Material.TRIPWIRE_HOOK, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "dropper", "ドロッパー", Material.DROPPER, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "piston_sticky_base", "粘着ピストン", Material.PISTON_STICKY_BASE, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "piston_base", "ピストン", Material.PISTON_BASE, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "tnt", "TNT", Material.TNT, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "trapped_chest", "トラップチェスト", Material.TRAPPED_CHEST, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "daylight_detector", "日照センサー", Material.DAYLIGHT_DETECTOR, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "iron_door", "鉄のドア", Material.IRON_DOOR, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "diode", "レッドストーンリピーター", Material.DIODE, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "dispenser", "ディスペンサー", Material.DISPENSER, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "hopper", "ホッパー", Material.HOPPER, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "redstone_comparator", "レッドストーンコンパレーター", Material.REDSTONE_COMPARATOR, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "powered_rail", "パワードレール", Material.POWERED_RAIL, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "detector_rail", "ディテクターレール", Material.DETECTOR_RAIL, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "activator_rail", "アクティベーターレール", Material.ACTIVATOR_RAIL, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "boat", "オークのボート", Material.BOAT, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "spruce_boat", "マツのボート", Material.BOAT_SPRUCE, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "birch_boat", "シラカバのボート", Material.BOAT_BIRCH, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "jungle_boat", "ジャングルのボート", Material.BOAT_JUNGLE, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "acacia_boat", "アカシアのボート", Material.BOAT_ACACIA, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "dark_oak_boat", "ダークオークのボート", Material.BOAT_DARK_OAK, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "saddle", "サドル", Material.SADDLE, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "minecart", "トロッコ", Material.MINECART, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "chest_minecart", "チェスト付きトロッコ", Material.STORAGE_MINECART, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "furnace_minecart", "かまど付きトロッコ", Material.POWERED_MINECART, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "hopper_minecart", "ホッパー付きトロッコ", Material.HOPPER_MINECART, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "iron_horse_armor", "鉄の馬鎧", Material.IRON_BARDING, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "golden_horse_armor", "金の馬鎧", Material.GOLD_BARDING, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "diamond_horse_armor", "ダイヤの馬鎧", Material.DIAMOND_BARDING, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "record_13", "レコード", Material.GOLD_RECORD, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "record_cat", "レコード", Material.GREEN_RECORD, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "record_blocks", "レコード", Material.RECORD_3, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "record_chirp", "レコード", Material.RECORD_4, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "record_far", "レコード", Material.RECORD_5, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "record_mall", "レコード", Material.RECORD_6, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "record_mellohi", "レコード", Material.RECORD_7, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "record_stal", "レコード", Material.RECORD_8, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "record_strad", "レコード", Material.RECORD_9, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "record_ward", "レコード", Material.RECORD_10, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "record_11", "レコード", Material.RECORD_11, 0),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "record_wait", "レコード", Material.RECORD_12, 0)
  )

  /**
   * デフォルトでガチャの内容に含まれている景品。
   */
  private val minestackBuiltinGachaPrizes: List[MineStackObjectGroup[ItemStack]] = leftElems(
    MineStackObjectByItemStack(GACHA_PRIZES, "gachaimo", None, hasNameLore = true, gachaPrizeAPI.staticGachaPrizeFactory.gachaRingo),
    MineStackObjectByItemStack(GACHA_PRIZES, "exp_bottle", Some("エンチャントの瓶"), hasNameLore = false, new ItemStack(Material.EXP_BOTTLE, 1))
  )

  // @formatter:on

  import cats.implicits._

  private val gachaPrizesObjects: F[Ref[F, Vector[MineStackObject[ItemStack]]]] = for {
    gachaObjects <- mineStackGachaObjectPersistence.getAllMineStackGachaObjects
    prizes <- Ref.of[F, Vector[MineStackObject[ItemStack]]](
      gachaObjects.sortBy(_.gachaPrize.id.id).map { gachaObject =>
        MineStackObjectByItemStack(
          GACHA_PRIZES,
          gachaObject.objectName,
          None,
          hasNameLore = true,
          gachaObject.gachaPrize.itemStack
        )
      }
    )
  } yield prizes

  // ガチャアイテムを除外したMineStackGroups
  private val exceptGachaItemMineStackGroups: List[MineStackObjectGroup[ItemStack]] = List(
    minestacklistbuild,
    minestacklistdrop,
    minestacklistfarm,
    minestacklistmine,
    minestacklistrs,
    minestackBuiltinGachaPrizes
  ).flatten

  private val allMineStackGroups: F[List[MineStackObjectGroup[ItemStack]]] = for {
    gachaPrizesReference <- gachaPrizesObjects
    gachaPrizes <- gachaPrizesReference.get
    leftGachaPrizes = gachaPrizes.flatMap(leftElems(_))
  } yield {
    exceptGachaItemMineStackGroups ++ leftGachaPrizes
  }

  override def allMineStackObjects: F[Vector[MineStackObject[ItemStack]]] =
    allMineStackGroups.map(_.flatMap {
      case Left(mineStackObject: MineStackObject[ItemStack]) => List(mineStackObject)
      case Right(group) => List(group.representative) ++ group.coloredVariants
    }.toVector)

  override def getAllObjectGroupsInCategory(
    category: MineStackObjectCategory
  ): F[List[MineStackObjectGroup[ItemStack]]] = {
    def categoryOf(group: MineStackObjectGroup[ItemStack]): MineStackObjectCategory = {
      group match {
        case Left(mineStackObject) => mineStackObject.category
        case Right(groupedObjects) => groupedObjects.category
      }
    }

    allMineStackGroups.map(_.filter { group => categoryOf(group) == category })
  }

  override def findBySignedItemStack(
    itemStack: ItemStack,
    player: Player
  ): F[Option[MineStackObject[ItemStack]]] = for {
    foundGachaPrizeOpt <- gachaPrizeAPI.findOfRegularPrizesBySignedItemStack(
      itemStack,
      player.getName
    )
    isGachaPrize = foundGachaPrizeOpt.nonEmpty
    mineStackObjects <- allMineStackObjects
  } yield {
    // ItemStackのLoreはnullの可能性がある
    val isSignedItemStack =
      Option(itemStack.getItemMeta.getLore).exists(_.contains("所有者："))
    if (isGachaPrize && foundGachaPrizeOpt.get.signOwner) {
      mineStackObjects.find { mineStackObject =>
        foundGachaPrizeOpt.get.itemStack.isSimilar(mineStackObject.itemStack)
      }
    } else if (isSignedItemStack) {
      // 所有者名が違うとガチャ景品として認識しないが、違ったらそもそも見つかっていない
      // 記名が入っていないアイテムは収納できてしまうが仕様
      None
    } else {
      mineStackObjects.find(_.itemStack.isSimilar(itemStack))
    }
  }

  override protected implicit val F: Functor[F] = implicitly
}
