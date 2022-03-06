package com.github.unchama.seichiassist

import com.github.unchama.seichiassist.minestack.MineStackObj
import com.github.unchama.seichiassist.minestack.MineStackObjectCategory._
import com.github.unchama.seichiassist.util.StaticGachaPrizeFactory
import org.bukkit.Material

import scala.collection.mutable

object MineStackObjectList {
  // 採掘可能ブロック
  val minestacklistmine: List[MineStackObj] = List(
    new MineStackObj(ORES, "coal_ore", "石炭鉱石", 1, Material.COAL_ORE, 0),
    new MineStackObj(ORES, "coal", "石炭", 1, Material.COAL, 0),
    new MineStackObj(ORES, "coal_block", "石炭ブロック", 1, Material.COAL_BLOCK, 0),
    new MineStackObj(ORES, "coal_1", "木炭", 1, Material.COAL, 1),
    new MineStackObj(ORES, "iron_ore", "鉄鉱石", 1, Material.IRON_ORE, 0),
    new MineStackObj(ORES, "iron_ingot", "鉄インゴット", 1, Material.IRON_INGOT, 0),
    new MineStackObj(ORES, "iron_block", "鉄ブロック", 1, Material.IRON_BLOCK, 0),
    new MineStackObj(ORES, "quartz_ore", "ネザー水晶鉱石", 1, Material.QUARTZ_ORE, 0),
    new MineStackObj(ORES, "quartz", "ネザー水晶", 1, Material.QUARTZ, 0),
    new MineStackObj(ORES, "gold_ore", "金鉱石", 1, Material.GOLD_ORE, 0),
    new MineStackObj(ORES, "gold_ingot", "金インゴット", 1, Material.GOLD_INGOT, 0),
    new MineStackObj(ORES, "gold_block", "金ブロック", 1, Material.GOLD_BLOCK, 0),
    new MineStackObj(ORES, "redstone_ore", "レッドストーン鉱石", 1, Material.REDSTONE_ORE, 0),
    new MineStackObj(ORES, "lapis_ore", "ラピスラズリ鉱石", 1, Material.LAPIS_ORE, 0),
    new MineStackObj(ORES, "lapis_lazuli", "ラピスラズリ", 1, Material.INK_SACK, 4),
    new MineStackObj(ORES, "lapis_block", "ラピスラズリブロック", 1, Material.LAPIS_BLOCK, 0),
    new MineStackObj(ORES, "diamond_ore", "ダイヤモンド鉱石", 1, Material.DIAMOND_ORE, 0),
    new MineStackObj(ORES, "diamond", "ダイヤモンド", 1, Material.DIAMOND, 0),
    new MineStackObj(ORES, "diamond_block", "ダイヤモンドブロック", 1, Material.DIAMOND_BLOCK, 0),
    new MineStackObj(ORES, "emerald_ore", "エメラルド鉱石", 1, Material.EMERALD_ORE, 0),
    new MineStackObj(ORES, "emerald", "エメラルド", 1, Material.EMERALD, 0),
    new MineStackObj(ORES, "emerald_block", "エメラルドブロック", 1, Material.EMERALD_BLOCK, 0)
  )

  // モンスター+動物ドロップ
  val minestacklistdrop: List[MineStackObj] = List(
    new MineStackObj(MOB_DROP, "ender_pearl", "エンダーパール", 1, Material.ENDER_PEARL, 0),
    new MineStackObj(MOB_DROP, "ender_eye", "エンダーアイ", 1, Material.EYE_OF_ENDER, 0),
    new MineStackObj(MOB_DROP, "slime_ball", "スライムボール", 1, Material.SLIME_BALL, 0),
    new MineStackObj(MOB_DROP, "slime", "スライムブロック", 1, Material.SLIME_BLOCK, 0),
    new MineStackObj(MOB_DROP, "rotten_flesh", "腐った肉", 1, Material.ROTTEN_FLESH, 0),
    new MineStackObj(MOB_DROP, "bone", "骨", 1, Material.BONE, 0),
    new MineStackObj(MOB_DROP, "sulphur", "火薬", 1, Material.SULPHUR, 0),
    new MineStackObj(MOB_DROP, "arrow", "矢", 1, Material.ARROW, 0),
    new MineStackObj(MOB_DROP, "tipped_arrow", "鈍化の矢", 1, Material.TIPPED_ARROW, 0),
    new MineStackObj(MOB_DROP, "spider_eye", "蜘蛛の目", 1, Material.SPIDER_EYE, 0),
    new MineStackObj(MOB_DROP, "string", "糸", 1, Material.STRING, 0),
    new MineStackObj(MOB_DROP, "name_tag", "名札", 1, Material.NAME_TAG, 0),
    new MineStackObj(MOB_DROP, "lead", "リード", 1, Material.LEASH, 0),
    new MineStackObj(MOB_DROP, "glass_bottle", "ガラス瓶", 1, Material.GLASS_BOTTLE, 0),
    new MineStackObj(MOB_DROP, "gold_nugget", "金塊", 1, Material.GOLD_NUGGET, 0),
    new MineStackObj(MOB_DROP, "blaze_rod", "ブレイズロッド", 1, Material.BLAZE_ROD, 0),
    new MineStackObj(MOB_DROP, "blaze_powder", "ブレイズパウダー", 1, Material.BLAZE_POWDER, 0),
    new MineStackObj(MOB_DROP, "ghast_tear", "ガストの涙", 1, Material.GHAST_TEAR, 0),
    new MineStackObj(MOB_DROP, "magma_cream", "マグマクリーム", 1, Material.MAGMA_CREAM, 0),
    new MineStackObj(
      MOB_DROP,
      "prismarine_shard",
      "プリズマリンの欠片",
      1,
      Material.PRISMARINE_SHARD,
      0
    ),
    new MineStackObj(
      MOB_DROP,
      "prismarine_crystals",
      "プリズマリンクリスタル",
      1,
      Material.PRISMARINE_CRYSTALS,
      0
    ),
    new MineStackObj(MOB_DROP, "feather", "羽", 1, Material.FEATHER, 0),
    new MineStackObj(MOB_DROP, "leather", "革", 1, Material.LEATHER, 0),
    new MineStackObj(MOB_DROP, "rabbit_hide", "ウサギの皮", 1, Material.RABBIT_HIDE, 0),
    new MineStackObj(MOB_DROP, "rabbit_foot", "ウサギの足", 1, Material.RABBIT_FOOT, 0),
    new MineStackObj(MOB_DROP, "dragon_egg", "エンドラの卵", 1, Material.DRAGON_EGG, 0),
    new MineStackObj(MOB_DROP, "shulker_shell", "シュルカーの殻", 1, Material.SHULKER_SHELL, 0),
    new MineStackObj(MOB_DROP, "totem_of_undying", "不死のトーテム", 1, Material.TOTEM, 0),
    new MineStackObj(MOB_DROP, "dragon_head", "エンダードラゴンの頭", 1, Material.SKULL_ITEM, 5),
    new MineStackObj(
      MOB_DROP,
      "wither_skeleton_skull",
      "ウィザースケルトンの頭",
      1,
      Material.SKULL_ITEM,
      1
    )
  )

  // 採掘で入手可能な農業系ブロック
  val minestacklistfarm: List[MineStackObj] = List(
    new MineStackObj(AGRICULTURAL, "seeds", "種", 1, Material.SEEDS, 0),
    new MineStackObj(AGRICULTURAL, "apple", "リンゴ", 1, Material.APPLE, 0),
    new MineStackObj(AGRICULTURAL, "long_grass1", "草", 1, Material.LONG_GRASS, 1),
    new MineStackObj(AGRICULTURAL, "long_grass2", "シダ", 1, Material.LONG_GRASS, 2),
    new MineStackObj(AGRICULTURAL, "dead_bush", "枯れ木", 1, Material.DEAD_BUSH, 0),
    new MineStackObj(AGRICULTURAL, "cactus", "サボテン", 1, Material.CACTUS, 0),
    new MineStackObj(AGRICULTURAL, "vine", "ツタ", 1, Material.VINE, 0),
    new MineStackObj(AGRICULTURAL, "water_lily", "スイレンの葉", 1, Material.WATER_LILY, 0),
    new MineStackObj(AGRICULTURAL, "yellow_flower", "タンポポ", 1, Material.YELLOW_FLOWER, 0),
    new MineStackObj(AGRICULTURAL, "red_rose0", "ポピー", 1, Material.RED_ROSE, 0),
    new MineStackObj(AGRICULTURAL, "red_rose1", "ヒスイラン", 1, Material.RED_ROSE, 1),
    new MineStackObj(AGRICULTURAL, "red_rose2", "アリウム", 1, Material.RED_ROSE, 2),
    new MineStackObj(AGRICULTURAL, "red_rose3", "ヒナソウ", 1, Material.RED_ROSE, 3),
    new MineStackObj(AGRICULTURAL, "red_rose4", "赤色のチューリップ", 1, Material.RED_ROSE, 4),
    new MineStackObj(AGRICULTURAL, "red_rose5", "橙色のチューリップ", 1, Material.RED_ROSE, 5),
    new MineStackObj(AGRICULTURAL, "red_rose6", "白色のチューリップ", 1, Material.RED_ROSE, 6),
    new MineStackObj(AGRICULTURAL, "red_rose7", "桃色のチューリップ", 1, Material.RED_ROSE, 7),
    new MineStackObj(AGRICULTURAL, "red_rose8", "フランスギク", 1, Material.RED_ROSE, 8),
    new MineStackObj(AGRICULTURAL, "leaves", "オークの葉", 1, Material.LEAVES, 0),
    new MineStackObj(AGRICULTURAL, "leaves1", "マツの葉", 1, Material.LEAVES, 1),
    new MineStackObj(AGRICULTURAL, "leaves2", "シラカバの葉", 1, Material.LEAVES, 2),
    new MineStackObj(AGRICULTURAL, "leaves3", "ジャングルの葉", 1, Material.LEAVES, 3),
    new MineStackObj(AGRICULTURAL, "leaves_2", "アカシアの葉", 1, Material.LEAVES_2, 0),
    new MineStackObj(AGRICULTURAL, "leaves_21", "ダークオークの葉", 1, Material.LEAVES_2, 1),
    new MineStackObj(AGRICULTURAL, "double_plant0", "ヒマワリ", 1, Material.DOUBLE_PLANT, 0),
    new MineStackObj(AGRICULTURAL, "double_plant1", "ライラック", 1, Material.DOUBLE_PLANT, 1),
    new MineStackObj(AGRICULTURAL, "double_plant2", "高い草", 1, Material.DOUBLE_PLANT, 2),
    new MineStackObj(AGRICULTURAL, "double_plant3", "大きなシダ", 1, Material.DOUBLE_PLANT, 3),
    new MineStackObj(AGRICULTURAL, "double_plant4", "バラの低木", 1, Material.DOUBLE_PLANT, 4),
    new MineStackObj(AGRICULTURAL, "double_plant5", "ボタン", 1, Material.DOUBLE_PLANT, 5),
    new MineStackObj(AGRICULTURAL, "sugar_cane", "サトウキビ", 1, Material.SUGAR_CANE, 0),
    new MineStackObj(AGRICULTURAL, "pumpkin", "カボチャ", 1, Material.PUMPKIN, 0),
    new MineStackObj(AGRICULTURAL, "ink_sack3", "カカオ豆", 1, Material.INK_SACK, 3),
    new MineStackObj(AGRICULTURAL, "huge_mushroom_1", "キノコ", 1, Material.HUGE_MUSHROOM_1, 0),
    new MineStackObj(AGRICULTURAL, "huge_mushroom_2", "キノコ", 1, Material.HUGE_MUSHROOM_2, 0),
    new MineStackObj(AGRICULTURAL, "melon", "スイカ", 1, Material.MELON, 0),
    new MineStackObj(AGRICULTURAL, "melon_block", "スイカ", 1, Material.MELON_BLOCK, 0),
    new MineStackObj(AGRICULTURAL, "brown_mushroom", "キノコ", 1, Material.BROWN_MUSHROOM, 0),
    new MineStackObj(AGRICULTURAL, "red_mushroom", "キノコ", 1, Material.RED_MUSHROOM, 0),
    new MineStackObj(AGRICULTURAL, "sapling", "オークの苗木", 1, Material.SAPLING, 0),
    new MineStackObj(AGRICULTURAL, "sapling1", "マツの苗木", 1, Material.SAPLING, 1),
    new MineStackObj(AGRICULTURAL, "sapling2", "シラカバの苗木", 1, Material.SAPLING, 2),
    new MineStackObj(AGRICULTURAL, "sapling3", "ジャングルの苗木", 1, Material.SAPLING, 3),
    new MineStackObj(AGRICULTURAL, "sapling4", "アカシアの苗木", 1, Material.SAPLING, 4),
    new MineStackObj(AGRICULTURAL, "sapling5", "ダークオークの苗木", 1, Material.SAPLING, 5),
    new MineStackObj(AGRICULTURAL, "beetroot", "ビートルート", 1, Material.BEETROOT, 0),
    new MineStackObj(AGRICULTURAL, "beetroot_seeds", "ビートルートの種", 1, Material.BEETROOT_SEEDS, 0),
    new MineStackObj(AGRICULTURAL, "carrot_item", "ニンジン", 1, Material.CARROT_ITEM, 0),
    new MineStackObj(AGRICULTURAL, "potato_item", "ジャガイモ", 1, Material.POTATO_ITEM, 0),
    new MineStackObj(
      AGRICULTURAL,
      "poisonous_potato",
      "青くなったジャガイモ",
      1,
      Material.POISONOUS_POTATO,
      0
    ),
    new MineStackObj(AGRICULTURAL, "wheat", "小麦", 1, Material.WHEAT, 0),
    new MineStackObj(AGRICULTURAL, "pumpkin_seeds", "カボチャの種", 1, Material.PUMPKIN_SEEDS, 0),
    new MineStackObj(AGRICULTURAL, "melon_seeds", "スイカの種", 1, Material.MELON_SEEDS, 0),
    new MineStackObj(AGRICULTURAL, "nether_stalk", "ネザーウォート", 1, Material.NETHER_STALK, 0),
    new MineStackObj(AGRICULTURAL, "chorus_fruit", "コーラスフルーツ", 1, Material.CHORUS_FRUIT, 0),
    new MineStackObj(AGRICULTURAL, "chorus_flower", "コーラスフラワー", 1, Material.CHORUS_FLOWER, 0),
    new MineStackObj(
      AGRICULTURAL,
      "popped_chorus_fruit",
      "焼いたコーラスフルーツ",
      1,
      Material.CHORUS_FRUIT_POPPED,
      0
    ),
    new MineStackObj(AGRICULTURAL, "egg", "卵", 1, Material.EGG, 0),
    new MineStackObj(AGRICULTURAL, "pork", "生の豚肉", 1, Material.PORK, 0),
    new MineStackObj(AGRICULTURAL, "cooked_porkchop", "焼き豚", 1, Material.GRILLED_PORK, 0),
    new MineStackObj(AGRICULTURAL, "raw_chicken", "生の鶏肉", 1, Material.RAW_CHICKEN, 0),
    new MineStackObj(AGRICULTURAL, "cooked_chicken", "焼き鳥", 1, Material.COOKED_CHICKEN, 0),
    new MineStackObj(AGRICULTURAL, "mutton", "生の羊肉", 1, Material.MUTTON, 0),
    new MineStackObj(AGRICULTURAL, "cooked_mutton", "焼いた羊肉", 1, Material.COOKED_MUTTON, 0),
    new MineStackObj(AGRICULTURAL, "raw_beef", "生の牛肉", 1, Material.RAW_BEEF, 0),
    new MineStackObj(AGRICULTURAL, "cooked_beaf", "ステーキ", 1, Material.COOKED_BEEF, 0),
    new MineStackObj(AGRICULTURAL, "rabbit", "生の兎肉", 1, Material.RABBIT, 0),
    new MineStackObj(AGRICULTURAL, "cooked_rabbit", "焼き兎肉", 1, Material.COOKED_RABBIT, 0),
    new MineStackObj(AGRICULTURAL, "raw_fish0", "生魚", 1, Material.RAW_FISH, 0),
    new MineStackObj(AGRICULTURAL, "cooked_fish0", "焼き魚", 1, Material.COOKED_FISH, 0),
    new MineStackObj(AGRICULTURAL, "raw_fish1", "生鮭", 1, Material.RAW_FISH, 1),
    new MineStackObj(AGRICULTURAL, "cooked_fish1", "焼き鮭", 1, Material.COOKED_FISH, 1),
    new MineStackObj(AGRICULTURAL, "raw_fish2", "クマノミ", 1, Material.RAW_FISH, 2),
    new MineStackObj(AGRICULTURAL, "raw_fish3", "フグ", 1, Material.RAW_FISH, 3),
    new MineStackObj(AGRICULTURAL, "bread", "パン", 1, Material.BREAD, 0),
    new MineStackObj(AGRICULTURAL, "sugar", "砂糖", 1, Material.SUGAR, 0),
    new MineStackObj(AGRICULTURAL, "baked_potato", "ベイクドポテト", 1, Material.BAKED_POTATO, 0),
    new MineStackObj(AGRICULTURAL, "cake", "ケーキ", 1, Material.CAKE, 0),
    new MineStackObj(AGRICULTURAL, "mushroom_stew", "キノコシチュー", 1, Material.MUSHROOM_SOUP, 0),
    new MineStackObj(AGRICULTURAL, "rabbit_stew", "ウサギシチュー", 1, Material.RABBIT_STEW, 0),
    new MineStackObj(AGRICULTURAL, "beetroot_soup", "ビートルートスープ", 1, Material.BEETROOT_SOUP, 0),
    new MineStackObj(AGRICULTURAL, "bowl", "ボウル", 1, Material.BOWL, 0),
    new MineStackObj(AGRICULTURAL, "milk_bucket", "牛乳", 1, Material.MILK_BUCKET, 0)
  )

  // 建築系ブロック
  val minestacklistbuild: List[MineStackObj] = List(
    new MineStackObj(BUILDING, "log", "オークの原木", 1, Material.LOG, 0),
    new MineStackObj(BUILDING, "wood", "オークの木材", 1, Material.WOOD, 0),
    new MineStackObj(BUILDING, "wood_step0", "オークの木材ハーフブロック", 1, Material.WOOD_STEP, 0),
    new MineStackObj(BUILDING, "oak_stairs", "オークの木の階段", 1, Material.WOOD_STAIRS, 0),
    new MineStackObj(BUILDING, "fence", "オークのフェンス", 1, Material.FENCE, 0),
    new MineStackObj(BUILDING, "log1", "マツの原木", 1, Material.LOG, 1),
    new MineStackObj(BUILDING, "wood_1", "マツの木材", 1, Material.WOOD, 1),
    new MineStackObj(BUILDING, "wood_step1", "マツの木材ハーフブロック", 1, Material.WOOD_STEP, 1),
    new MineStackObj(BUILDING, "spruce_stairs", "マツの木の階段", 1, Material.SPRUCE_WOOD_STAIRS, 0),
    new MineStackObj(BUILDING, "spruce_fence", "マツのフェンス", 1, Material.SPRUCE_FENCE, 0),
    new MineStackObj(BUILDING, "log2", "シラカバの原木", 1, Material.LOG, 2),
    new MineStackObj(BUILDING, "wood_2", "シラカバの木材", 1, Material.WOOD, 2),
    new MineStackObj(BUILDING, "wood_step2", "シラカバの木材ハーフブロック", 1, Material.WOOD_STEP, 2),
    new MineStackObj(BUILDING, "birch_stairs", "シラカバの木の階段", 1, Material.BIRCH_WOOD_STAIRS, 0),
    new MineStackObj(BUILDING, "birch_fence", "シラカバのフェンス", 1, Material.BIRCH_FENCE, 0),
    new MineStackObj(BUILDING, "log3", "ジャングルの原木", 1, Material.LOG, 3),
    new MineStackObj(BUILDING, "wood_3", "ジャングルの木材", 1, Material.WOOD, 3),
    new MineStackObj(BUILDING, "wood_step3", "ジャングルの木材ハーフブロック", 1, Material.WOOD_STEP, 3),
    new MineStackObj(
      BUILDING,
      "jungle_stairs",
      "ジャングルの木の階段",
      1,
      Material.JUNGLE_WOOD_STAIRS,
      0
    ),
    new MineStackObj(BUILDING, "jungle_fence", "ジャングルのフェンス", 1, Material.JUNGLE_FENCE, 0),
    new MineStackObj(BUILDING, "log_2", "アカシアの原木", 1, Material.LOG_2, 0),
    new MineStackObj(BUILDING, "wood_4", "アカシアの木材", 1, Material.WOOD, 4),
    new MineStackObj(BUILDING, "wood_step4", "アカシアの木材ハーフブロック", 1, Material.WOOD_STEP, 4),
    new MineStackObj(BUILDING, "acacia_stairs", "アカシアの木の階段", 1, Material.ACACIA_STAIRS, 0),
    new MineStackObj(BUILDING, "acacia_fence", "アカシアのフェンス", 1, Material.ACACIA_FENCE, 0),
    new MineStackObj(BUILDING, "log_21", "ダークオークの原木", 1, Material.LOG_2, 1),
    new MineStackObj(BUILDING, "wood_5", "ダークオークの木材", 1, Material.WOOD, 5),
    new MineStackObj(BUILDING, "wood_step5", "ダークオークの木材ハーフブロック", 1, Material.WOOD_STEP, 5),
    new MineStackObj(
      BUILDING,
      "dark_oak_stairs",
      "ダークオークの木の階段",
      1,
      Material.DARK_OAK_STAIRS,
      0
    ),
    new MineStackObj(BUILDING, "dark_oak_fence", "ダークオークのフェンス", 1, Material.DARK_OAK_FENCE, 0),
    new MineStackObj(BUILDING, "cobblestone", "丸石", 1, Material.COBBLESTONE, 0),
    new MineStackObj(BUILDING, "step3", "丸石ハーフブロック", 1, Material.STEP, 3),
    new MineStackObj(BUILDING, "stone_stairs", "丸石の階段", 1, Material.COBBLESTONE_STAIRS, 0),
    new MineStackObj(BUILDING, "cobblestone_wall_0", "丸石の壁", 1, Material.COBBLE_WALL, 0),
    new MineStackObj(BUILDING, "mossy_cobblestone", "苔石", 1, Material.MOSSY_COBBLESTONE, 0),
    new MineStackObj(BUILDING, "cobblestone_wall_1", "苔石の壁", 1, Material.COBBLE_WALL, 1),
    new MineStackObj(BUILDING, "stone", "石", 1, Material.STONE, 0),
    new MineStackObj(BUILDING, "step0", "石ハーフブロック", 1, Material.STEP, 0),
    new MineStackObj(BUILDING, "smooth_brick0", "石レンガ", 1, Material.SMOOTH_BRICK, 0),
    new MineStackObj(BUILDING, "step5", "石レンガハーフブロック", 1, Material.STEP, 5),
    new MineStackObj(BUILDING, "smooth_stairs", "石レンガの階段", 1, Material.SMOOTH_STAIRS, 0),
    new MineStackObj(BUILDING, "smooth_brick3", "模様入り石レンガ", 1, Material.SMOOTH_BRICK, 3),
    new MineStackObj(BUILDING, "smooth_brick1", "苔石レンガ", 1, Material.SMOOTH_BRICK, 1),
    new MineStackObj(BUILDING, "smooth_brick2", "ひびの入った石レンガ", 1, Material.SMOOTH_BRICK, 2),
    new MineStackObj(BUILDING, "sand", "砂", 1, Material.SAND, 0),
    new MineStackObj(BUILDING, "sandstone", "砂岩", 1, Material.SANDSTONE, 0),
    new MineStackObj(BUILDING, "step1", "砂岩ハーフブロック", 1, Material.STEP, 1),
    new MineStackObj(BUILDING, "standstone_stairs", "砂岩の階段", 1, Material.SANDSTONE_STAIRS, 0),
    new MineStackObj(BUILDING, "sandstone1", "模様入りの砂岩", 1, Material.SANDSTONE, 1),
    new MineStackObj(BUILDING, "sandstone2", "なめらかな砂岩", 1, Material.SANDSTONE, 2),
    new MineStackObj(BUILDING, "red_sand", "赤い砂", 1, Material.SAND, 1),
    new MineStackObj(BUILDING, "red_sandstone", "赤い砂岩", 1, Material.RED_SANDSTONE, 0),
    new MineStackObj(BUILDING, "stone_slab20", "赤い砂岩ハーフブロック", 1, Material.STONE_SLAB2, 0),
    new MineStackObj(
      BUILDING,
      "red_sandstone_stairs",
      "赤い砂岩の階段",
      1,
      Material.RED_SANDSTONE_STAIRS,
      0
    ),
    new MineStackObj(BUILDING, "red_sandstone1", "模様入りの赤い砂岩", 1, Material.RED_SANDSTONE, 1),
    new MineStackObj(BUILDING, "red_sandstone2", "なめらかな赤い砂岩", 1, Material.RED_SANDSTONE, 2),
    new MineStackObj(BUILDING, "clay_ball", "粘土", 1, Material.CLAY_BALL, 0),
    new MineStackObj(BUILDING, "clay", "粘土(ブロック)", 1, Material.CLAY, 0),
    new MineStackObj(BUILDING, "brick_item", "レンガ", 1, Material.CLAY_BRICK, 0),
    new MineStackObj(BUILDING, "brick", "レンガ(ブロック)", 1, Material.BRICK, 0),
    new MineStackObj(BUILDING, "step4", "レンガハーフブロック", 1, Material.STEP, 4),
    new MineStackObj(BUILDING, "brick_stairs", "レンガの階段", 1, Material.BRICK_STAIRS, 0),
    new MineStackObj(BUILDING, "quartz_block", "ネザー水晶ブロック", 1, Material.QUARTZ_BLOCK, 0),
    new MineStackObj(BUILDING, "step7", "ネザー水晶ハーフブロック", 1, Material.STEP, 7),
    new MineStackObj(BUILDING, "quartz_stairs", "ネザー水晶の階段", 1, Material.QUARTZ_STAIRS, 0),
    new MineStackObj(BUILDING, "quartz_block1", "模様入りネザー水晶ブロック", 1, Material.QUARTZ_BLOCK, 1),
    new MineStackObj(BUILDING, "quartz_block2", "柱状ネザー水晶ブロック", 1, Material.QUARTZ_BLOCK, 2),
    new MineStackObj(BUILDING, "netherrack", "ネザーラック", 1, Material.NETHERRACK, 0),
    new MineStackObj(BUILDING, "nether_brick_item", "ネザーレンガ", 1, Material.NETHER_BRICK_ITEM, 0),
    new MineStackObj(BUILDING, "nether_brick", "ネザーレンガ(ブロック)", 1, Material.NETHER_BRICK, 0),
    new MineStackObj(BUILDING, "step6", "ネザーレンガハーフブロック", 1, Material.STEP, 6),
    new MineStackObj(
      BUILDING,
      "nether_brick_stairs",
      "ネザーレンガの階段",
      1,
      Material.NETHER_BRICK_STAIRS,
      0
    ),
    new MineStackObj(
      BUILDING,
      "nether_brick_fence",
      "ネザーレンガのフェンス",
      1,
      Material.NETHER_FENCE,
      0
    ),
    new MineStackObj(BUILDING, "red_nether_brick", "赤いネザーレンガ", 1, Material.RED_NETHER_BRICK, 0),
    new MineStackObj(
      BUILDING,
      "nether_wart_block",
      "ネザ－ウォートブロック",
      1,
      Material.NETHER_WART_BLOCK,
      0
    ),
    new MineStackObj(BUILDING, "ender_stone", "エンドストーン", 1, Material.ENDER_STONE, 0),
    new MineStackObj(BUILDING, "end_bricks", "エンドストーンレンガ", 1, Material.END_BRICKS, 0),
    new MineStackObj(BUILDING, "purpur_block", "プルパーブロック", 1, Material.PURPUR_BLOCK, 0),
    new MineStackObj(BUILDING, "purpur_pillar", "柱状プルパーブロック", 1, Material.PURPUR_PILLAR, 0),
    new MineStackObj(BUILDING, "purpur_slab", "プルパーハーフブロック", 1, Material.PURPUR_SLAB, 0),
    new MineStackObj(BUILDING, "purpur_stairs", "プルパーの階段", 1, Material.PURPUR_STAIRS, 0),
    new MineStackObj(BUILDING, "prismarine0", "プリズマリン", 1, Material.PRISMARINE, 0),
    new MineStackObj(BUILDING, "prismarine1", "プリズマリンレンガ", 1, Material.PRISMARINE, 1),
    new MineStackObj(BUILDING, "prismarine2", "ダークプリズマリン", 1, Material.PRISMARINE, 2),
    new MineStackObj(BUILDING, "sea_lantern", "シーランタン", 1, Material.SEA_LANTERN, 0),
    new MineStackObj(BUILDING, "granite", "花崗岩", 1, Material.STONE, 1),
    new MineStackObj(BUILDING, "polished_granite", "磨かれた花崗岩", 1, Material.STONE, 2),
    new MineStackObj(BUILDING, "diorite", "閃緑岩", 1, Material.STONE, 3),
    new MineStackObj(BUILDING, "polished_diorite", "磨かれた閃緑岩", 1, Material.STONE, 4),
    new MineStackObj(BUILDING, "andesite", "安山岩", 1, Material.STONE, 5),
    new MineStackObj(BUILDING, "polished_andesite", "磨かれた安山岩", 1, Material.STONE, 6),
    new MineStackObj(BUILDING, "dirt", "土", 1, Material.DIRT, 0),
    new MineStackObj(BUILDING, "grass", "草ブロック", 1, Material.GRASS, 0),
    new MineStackObj(BUILDING, "gravel", "砂利", 1, Material.GRAVEL, 0),
    new MineStackObj(BUILDING, "flint", "火打石", 1, Material.FLINT, 0),
    new MineStackObj(BUILDING, "flint_and_steel", "火打石と打ち金", 1, Material.FLINT_AND_STEEL, 0),
    new MineStackObj(BUILDING, "dirt1", "粗い土", 1, Material.DIRT, 1),
    new MineStackObj(BUILDING, "dirt2", "ポドゾル", 1, Material.DIRT, 2),
    new MineStackObj(BUILDING, "snow_block", "雪", 1, Material.SNOW_BLOCK, 0),
    new MineStackObj(BUILDING, "snow_layer", "雪タイル", 1, Material.SNOW, 0),
    new MineStackObj(BUILDING, "snow_ball", "雪玉", 1, Material.SNOW_BALL, 0),
    new MineStackObj(BUILDING, "ice", "氷", 1, Material.ICE, 0),
    new MineStackObj(BUILDING, "packed_ice", "氷塊", 1, Material.PACKED_ICE, 0),
    new MineStackObj(BUILDING, "mycel", "菌糸", 1, Material.MYCEL, 0),
    new MineStackObj(BUILDING, "bone_block", "骨ブロック", 1, Material.BONE_BLOCK, 0),
    new MineStackObj(BUILDING, "sponge", "スポンジ", 1, Material.SPONGE, 0),
    new MineStackObj(BUILDING, "wet_sponge", "濡れたスポンジ", 1, Material.SPONGE, 1),
    new MineStackObj(BUILDING, "soul_sand", "ソウルサンド", 1, Material.SOUL_SAND, 0),
    new MineStackObj(BUILDING, "magma", "マグマブロック", 1, Material.MAGMA, 0),
    new MineStackObj(BUILDING, "obsidian", "黒曜石", 1, Material.OBSIDIAN, 0),
    new MineStackObj(BUILDING, "glowstone_dust", "グロウストーンダスト", 1, Material.GLOWSTONE_DUST, 0),
    new MineStackObj(BUILDING, "glowstone", "グロウストーン", 1, Material.GLOWSTONE, 0),
    new MineStackObj(BUILDING, "torch", "松明", 1, Material.TORCH, 0),
    new MineStackObj(BUILDING, "jack_o_lantern", "ジャック・オ・ランタン", 1, Material.JACK_O_LANTERN, 0),
    new MineStackObj(BUILDING, "end_rod", "エンドロッド", 1, Material.END_ROD, 0),
    new MineStackObj(BUILDING, "bucket", "バケツ", 1, Material.BUCKET, 0),
    new MineStackObj(BUILDING, "water_bucket", "水入りバケツ", 1, Material.WATER_BUCKET, 0),
    new MineStackObj(BUILDING, "lava_bucket", "溶岩入りバケツ", 1, Material.LAVA_BUCKET, 0),
    new MineStackObj(BUILDING, "web", "クモの巣", 1, Material.WEB, 0),
    new MineStackObj(BUILDING, "rails", "レール", 1, Material.RAILS, 0),
    new MineStackObj(BUILDING, "furnace", "かまど", 1, Material.FURNACE, 0),
    new MineStackObj(BUILDING, "chest", "チェスト", 1, Material.CHEST, 0),
    new MineStackObj(BUILDING, "book", "本", 1, Material.BOOK, 0),
    new MineStackObj(BUILDING, "bookshelf", "本棚", 1, Material.BOOKSHELF, 0),
    new MineStackObj(BUILDING, "iron_bars", "鉄格子", 1, Material.IRON_FENCE, 0),
    new MineStackObj(BUILDING, "anvil", "金床", 1, Material.ANVIL, 0),
    new MineStackObj(BUILDING, "cauldron", "大釜", 1, Material.CAULDRON_ITEM, 0),
    new MineStackObj(BUILDING, "brewing_stand", "醸造台", 1, Material.BREWING_STAND_ITEM, 0),
    new MineStackObj(BUILDING, "flower_pot", "植木鉢", 1, Material.FLOWER_POT_ITEM, 0),
    new MineStackObj(BUILDING, "hay_block", "干し草の俵", 1, Material.HAY_BLOCK, 0),
    new MineStackObj(BUILDING, "ladder", "はしご", 1, Material.LADDER, 0),
    new MineStackObj(BUILDING, "sign", "看板", 1, Material.SIGN, 0),
    new MineStackObj(BUILDING, "item_frame", "額縁", 1, Material.ITEM_FRAME, 0),
    new MineStackObj(BUILDING, "painting", "絵画", 1, Material.PAINTING, 0),
    new MineStackObj(BUILDING, "beacon", "ビーコン", 1, Material.BEACON, 0),
    new MineStackObj(BUILDING, "armor_stand", "アーマースタンド", 1, Material.ARMOR_STAND, 0),
    new MineStackObj(BUILDING, "end_crystal", "エンドクリスタル", 1, Material.END_CRYSTAL, 0),
    new MineStackObj(
      BUILDING,
      "enchanting_table",
      "エンチャントテーブル",
      1,
      Material.ENCHANTMENT_TABLE,
      0
    ),
    new MineStackObj(BUILDING, "jukebox", "ジュークボックス", 1, Material.JUKEBOX, 0),
    new MineStackObj(BUILDING, "hard_clay", "テラコッタ", 1, Material.HARD_CLAY, 0),
    new MineStackObj(BUILDING, "stained_clay", "白色のテラコッタ", 1, Material.STAINED_CLAY, 0),
    new MineStackObj(BUILDING, "stained_clay1", "橙色のテラコッタ", 1, Material.STAINED_CLAY, 1),
    new MineStackObj(BUILDING, "stained_clay2", "赤紫色のテラコッタ", 1, Material.STAINED_CLAY, 2),
    new MineStackObj(BUILDING, "stained_clay3", "空色のテラコッタ", 1, Material.STAINED_CLAY, 3),
    new MineStackObj(BUILDING, "stained_clay4", "黄色のテラコッタ", 1, Material.STAINED_CLAY, 4),
    new MineStackObj(BUILDING, "stained_clay5", "黄緑色のテラコッタ", 1, Material.STAINED_CLAY, 5),
    new MineStackObj(BUILDING, "stained_clay6", "桃色のテラコッタ", 1, Material.STAINED_CLAY, 6),
    new MineStackObj(BUILDING, "stained_clay7", "灰色のテラコッタ", 1, Material.STAINED_CLAY, 7),
    new MineStackObj(BUILDING, "stained_clay8", "薄灰色のテラコッタ", 1, Material.STAINED_CLAY, 8),
    new MineStackObj(BUILDING, "stained_clay9", "青緑色のテラコッタ", 1, Material.STAINED_CLAY, 9),
    new MineStackObj(BUILDING, "stained_clay10", "紫色のテラコッタ", 1, Material.STAINED_CLAY, 10),
    new MineStackObj(BUILDING, "stained_clay11", "青色のテラコッタ", 1, Material.STAINED_CLAY, 11),
    new MineStackObj(BUILDING, "stained_clay12", "茶色のテラコッタ", 1, Material.STAINED_CLAY, 12),
    new MineStackObj(BUILDING, "stained_clay13", "緑色のテラコッタ", 1, Material.STAINED_CLAY, 13),
    new MineStackObj(BUILDING, "stained_clay14", "赤色のテラコッタ", 1, Material.STAINED_CLAY, 14),
    new MineStackObj(BUILDING, "stained_clay15", "黒色のテラコッタ", 1, Material.STAINED_CLAY, 15),
    new MineStackObj(BUILDING, "concrete", "白色のコンクリート", 1, Material.CONCRETE, 0),
    new MineStackObj(BUILDING, "concrete1", "橙色のコンクリート", 1, Material.CONCRETE, 1),
    new MineStackObj(BUILDING, "concrete2", "赤紫色のコンクリート", 1, Material.CONCRETE, 2),
    new MineStackObj(BUILDING, "concrete3", "空色のコンクリート", 1, Material.CONCRETE, 3),
    new MineStackObj(BUILDING, "concrete4", "黄色のコンクリート", 1, Material.CONCRETE, 4),
    new MineStackObj(BUILDING, "concrete5", "黄緑色のコンクリート", 1, Material.CONCRETE, 5),
    new MineStackObj(BUILDING, "concrete6", "桃色のコンクリート", 1, Material.CONCRETE, 6),
    new MineStackObj(BUILDING, "concrete7", "灰色のコンクリート", 1, Material.CONCRETE, 7),
    new MineStackObj(BUILDING, "concrete8", "薄灰色のコンクリート", 1, Material.CONCRETE, 8),
    new MineStackObj(BUILDING, "concrete9", "青緑色のコンクリート", 1, Material.CONCRETE, 9),
    new MineStackObj(BUILDING, "concrete10", "紫色のコンクリート", 1, Material.CONCRETE, 10),
    new MineStackObj(BUILDING, "concrete11", "青色のコンクリート", 1, Material.CONCRETE, 11),
    new MineStackObj(BUILDING, "concrete12", "茶色のコンクリート", 1, Material.CONCRETE, 12),
    new MineStackObj(BUILDING, "concrete13", "緑色のコンクリート", 1, Material.CONCRETE, 13),
    new MineStackObj(BUILDING, "concrete14", "赤色のコンクリート", 1, Material.CONCRETE, 14),
    new MineStackObj(BUILDING, "concrete15", "黒色のコンクリート", 1, Material.CONCRETE, 15),
    new MineStackObj(
      BUILDING,
      "concrete_powder",
      "白色のコンクリートパウダー",
      1,
      Material.CONCRETE_POWDER,
      0
    ),
    new MineStackObj(
      BUILDING,
      "concrete_powder1",
      "橙色のコンクリートパウダー",
      1,
      Material.CONCRETE_POWDER,
      1
    ),
    new MineStackObj(
      BUILDING,
      "concrete_powder2",
      "赤紫色のコンクリートパウダー",
      1,
      Material.CONCRETE_POWDER,
      2
    ),
    new MineStackObj(
      BUILDING,
      "concrete_powder3",
      "空色のコンクリートパウダー",
      1,
      Material.CONCRETE_POWDER,
      3
    ),
    new MineStackObj(
      BUILDING,
      "concrete_powder4",
      "黄色のコンクリートパウダー",
      1,
      Material.CONCRETE_POWDER,
      4
    ),
    new MineStackObj(
      BUILDING,
      "concrete_powder5",
      "黄緑色のコンクリートパウダー",
      1,
      Material.CONCRETE_POWDER,
      5
    ),
    new MineStackObj(
      BUILDING,
      "concrete_powder6",
      "桃色のコンクリートパウダー",
      1,
      Material.CONCRETE_POWDER,
      6
    ),
    new MineStackObj(
      BUILDING,
      "concrete_powder7",
      "灰色のコンクリートパウダー",
      1,
      Material.CONCRETE_POWDER,
      7
    ),
    new MineStackObj(
      BUILDING,
      "concrete_powder8",
      "薄灰色のコンクリートパウダー",
      1,
      Material.CONCRETE_POWDER,
      8
    ),
    new MineStackObj(
      BUILDING,
      "concrete_powder9",
      "青緑色のコンクリートパウダー",
      1,
      Material.CONCRETE_POWDER,
      9
    ),
    new MineStackObj(
      BUILDING,
      "concrete_powder10",
      "紫色のコンクリートパウダー",
      1,
      Material.CONCRETE_POWDER,
      10
    ),
    new MineStackObj(
      BUILDING,
      "concrete_powder11",
      "青色のコンクリートパウダー",
      1,
      Material.CONCRETE_POWDER,
      11
    ),
    new MineStackObj(
      BUILDING,
      "concrete_powder12",
      "茶色のコンクリートパウダー",
      1,
      Material.CONCRETE_POWDER,
      12
    ),
    new MineStackObj(
      BUILDING,
      "concrete_powder13",
      "緑色のコンクリートパウダー",
      1,
      Material.CONCRETE_POWDER,
      13
    ),
    new MineStackObj(
      BUILDING,
      "concrete_powder14",
      "赤色のコンクリートパウダー",
      1,
      Material.CONCRETE_POWDER,
      14
    ),
    new MineStackObj(
      BUILDING,
      "concrete_powder15",
      "黒色のコンクリートパウダー",
      1,
      Material.CONCRETE_POWDER,
      15
    ),
    new MineStackObj(
      BUILDING,
      "white_glazed_terracotta",
      "白色の彩釉テラコッタ",
      1,
      Material.WHITE_GLAZED_TERRACOTTA,
      0
    ),
    new MineStackObj(
      BUILDING,
      "orange_glazed_terracotta",
      "橙色の彩釉テラコッタ",
      1,
      Material.ORANGE_GLAZED_TERRACOTTA,
      0
    ),
    new MineStackObj(
      BUILDING,
      "magenta_glazed_terracotta",
      "赤紫色の彩釉テラコッタ",
      1,
      Material.MAGENTA_GLAZED_TERRACOTTA,
      0
    ),
    new MineStackObj(
      BUILDING,
      "light_blue_glazed_terracotta",
      "空色の彩釉テラコッタ",
      1,
      Material.LIGHT_BLUE_GLAZED_TERRACOTTA,
      0
    ),
    new MineStackObj(
      BUILDING,
      "yellow_glazed_terracotta",
      "黄色の彩釉テラコッタ",
      1,
      Material.YELLOW_GLAZED_TERRACOTTA,
      0
    ),
    new MineStackObj(
      BUILDING,
      "lime_glazed_terracotta",
      "黄緑色の彩釉テラコッタ",
      1,
      Material.LIME_GLAZED_TERRACOTTA,
      0
    ),
    new MineStackObj(
      BUILDING,
      "pink_glazed_terracotta",
      "桃色の彩釉テラコッタ",
      1,
      Material.PINK_GLAZED_TERRACOTTA,
      0
    ),
    new MineStackObj(
      BUILDING,
      "gray_glazed_terracotta",
      "灰色の彩釉テラコッタ",
      1,
      Material.GRAY_GLAZED_TERRACOTTA,
      0
    ),
    new MineStackObj(
      BUILDING,
      "silver_glazed_terracotta",
      "薄灰色の彩釉テラコッタ",
      1,
      Material.SILVER_GLAZED_TERRACOTTA,
      0
    ),
    new MineStackObj(
      BUILDING,
      "cyan_glazed_terracotta",
      "青緑色の彩釉テラコッタ",
      1,
      Material.CYAN_GLAZED_TERRACOTTA,
      0
    ),
    new MineStackObj(
      BUILDING,
      "purple_glazed_terracotta",
      "紫色の彩釉テラコッタ",
      1,
      Material.PURPLE_GLAZED_TERRACOTTA,
      0
    ),
    new MineStackObj(
      BUILDING,
      "blue_glazed_terracotta",
      "青色の彩釉テラコッタ",
      1,
      Material.BLUE_GLAZED_TERRACOTTA,
      0
    ),
    new MineStackObj(
      BUILDING,
      "brown_glazed_terracotta",
      "茶色の彩釉テラコッタ",
      1,
      Material.BROWN_GLAZED_TERRACOTTA,
      0
    ),
    new MineStackObj(
      BUILDING,
      "green_glazed_terracotta",
      "緑色の彩釉テラコッタ",
      1,
      Material.GREEN_GLAZED_TERRACOTTA,
      0
    ),
    new MineStackObj(
      BUILDING,
      "red_glazed_terracotta",
      "赤色の彩釉テラコッタ",
      1,
      Material.RED_GLAZED_TERRACOTTA,
      0
    ),
    new MineStackObj(
      BUILDING,
      "black_glazed_terracotta",
      "黒色の彩釉テラコッタ",
      1,
      Material.BLACK_GLAZED_TERRACOTTA,
      0
    ),
    new MineStackObj(BUILDING, "wool_0", "羊毛", 1, Material.WOOL, 0),
    new MineStackObj(BUILDING, "wool_1", "橙色の羊毛", 1, Material.WOOL, 1),
    new MineStackObj(BUILDING, "wool_2", "赤紫色の羊毛", 1, Material.WOOL, 2),
    new MineStackObj(BUILDING, "wool_3", "空色の羊毛", 1, Material.WOOL, 3),
    new MineStackObj(BUILDING, "wool_4", "黄色の羊毛", 1, Material.WOOL, 4),
    new MineStackObj(BUILDING, "wool_5", "黄緑色の羊毛", 1, Material.WOOL, 5),
    new MineStackObj(BUILDING, "wool_6", "桃色の羊毛", 1, Material.WOOL, 6),
    new MineStackObj(BUILDING, "wool_7", "灰色の羊毛", 1, Material.WOOL, 7),
    new MineStackObj(BUILDING, "wool_8", "薄灰色の羊毛", 1, Material.WOOL, 8),
    new MineStackObj(BUILDING, "wool_9", "青緑色の羊毛", 1, Material.WOOL, 9),
    new MineStackObj(BUILDING, "wool_10", "紫色の羊毛", 1, Material.WOOL, 10),
    new MineStackObj(BUILDING, "wool_11", "青色の羊毛", 1, Material.WOOL, 11),
    new MineStackObj(BUILDING, "wool_12", "茶色の羊毛", 1, Material.WOOL, 12),
    new MineStackObj(BUILDING, "wool_13", "緑色の羊毛", 1, Material.WOOL, 13),
    new MineStackObj(BUILDING, "wool_14", "赤色の羊毛", 1, Material.WOOL, 14),
    new MineStackObj(BUILDING, "wool_15", "黒色の羊毛", 1, Material.WOOL, 15),
    new MineStackObj(BUILDING, "carpet_0", "カーペット", 1, Material.CARPET, 0),
    new MineStackObj(BUILDING, "carpet_1", "橙色のカーペット", 1, Material.CARPET, 1),
    new MineStackObj(BUILDING, "carpet_2", "赤紫色のカーペット", 1, Material.CARPET, 2),
    new MineStackObj(BUILDING, "carpet_3", "空色のカーペット", 1, Material.CARPET, 3),
    new MineStackObj(BUILDING, "carpet_4", "黄色のカーペット", 1, Material.CARPET, 4),
    new MineStackObj(BUILDING, "carpet_5", "黄緑色のカーペット", 1, Material.CARPET, 5),
    new MineStackObj(BUILDING, "carpet_6", "桃色のカーペット", 1, Material.CARPET, 6),
    new MineStackObj(BUILDING, "carpet_7", "灰色のカーペット", 1, Material.CARPET, 7),
    new MineStackObj(BUILDING, "carpet_8", "薄灰色のカーペット", 1, Material.CARPET, 8),
    new MineStackObj(BUILDING, "carpet_9", "青緑色のカーペット", 1, Material.CARPET, 9),
    new MineStackObj(BUILDING, "carpet_10", "紫色のカーペット", 1, Material.CARPET, 10),
    new MineStackObj(BUILDING, "carpet_11", "青色のカーペット", 1, Material.CARPET, 11),
    new MineStackObj(BUILDING, "carpet_12", "茶色のカーペット", 1, Material.CARPET, 12),
    new MineStackObj(BUILDING, "carpet_13", "緑色のカーペット", 1, Material.CARPET, 13),
    new MineStackObj(BUILDING, "carpet_14", "赤色のカーペット", 1, Material.CARPET, 14),
    new MineStackObj(BUILDING, "carpet_15", "黒色のカーペット", 1, Material.CARPET, 15),
    new MineStackObj(BUILDING, "glass", "ガラス", 1, Material.GLASS, 0),
    new MineStackObj(BUILDING, "stained_glass_0", "白色の色付きガラス", 1, Material.STAINED_GLASS, 0),
    new MineStackObj(BUILDING, "stained_glass_1", "橙色の色付きガラス", 1, Material.STAINED_GLASS, 1),
    new MineStackObj(BUILDING, "stained_glass_2", "赤紫色の色付きガラス", 1, Material.STAINED_GLASS, 2),
    new MineStackObj(BUILDING, "stained_glass_3", "空色の色付きガラス", 1, Material.STAINED_GLASS, 3),
    new MineStackObj(BUILDING, "stained_glass_4", "黄色の色付きガラス", 1, Material.STAINED_GLASS, 4),
    new MineStackObj(BUILDING, "stained_glass_5", "黄緑色の色付きガラス", 1, Material.STAINED_GLASS, 5),
    new MineStackObj(BUILDING, "stained_glass_6", "桃色の色付きガラス", 1, Material.STAINED_GLASS, 6),
    new MineStackObj(BUILDING, "stained_glass_7", "灰色の色付きガラス", 1, Material.STAINED_GLASS, 7),
    new MineStackObj(BUILDING, "stained_glass_8", "薄灰色の色付きガラス", 1, Material.STAINED_GLASS, 8),
    new MineStackObj(BUILDING, "stained_glass_9", "青緑色の色付きガラス", 1, Material.STAINED_GLASS, 9),
    new MineStackObj(BUILDING, "stained_glass_10", "紫色の色付きガラス", 1, Material.STAINED_GLASS, 10),
    new MineStackObj(BUILDING, "stained_glass_11", "青色の色付きガラス", 1, Material.STAINED_GLASS, 11),
    new MineStackObj(BUILDING, "stained_glass_12", "茶色の色付きガラス", 1, Material.STAINED_GLASS, 12),
    new MineStackObj(BUILDING, "stained_glass_13", "緑色の色付きガラス", 1, Material.STAINED_GLASS, 13),
    new MineStackObj(BUILDING, "stained_glass_14", "赤色の色付きガラス", 1, Material.STAINED_GLASS, 14),
    new MineStackObj(BUILDING, "stained_glass_15", "黒色の色付きガラス", 1, Material.STAINED_GLASS, 15),
    new MineStackObj(BUILDING, "glass_panel", "板ガラス", 1, Material.THIN_GLASS, 0),
    new MineStackObj(
      BUILDING,
      "glass_panel_0",
      "白色の色付きガラス板",
      1,
      Material.STAINED_GLASS_PANE,
      0
    ),
    new MineStackObj(
      BUILDING,
      "glass_panel_1",
      "橙色の色付きガラス板",
      1,
      Material.STAINED_GLASS_PANE,
      1
    ),
    new MineStackObj(
      BUILDING,
      "glass_panel_2",
      "赤紫色の色付きガラス板",
      1,
      Material.STAINED_GLASS_PANE,
      2
    ),
    new MineStackObj(
      BUILDING,
      "glass_panel_3",
      "空色の色付きガラス板",
      1,
      Material.STAINED_GLASS_PANE,
      3
    ),
    new MineStackObj(
      BUILDING,
      "glass_panel_4",
      "黄色の色付きガラス板",
      1,
      Material.STAINED_GLASS_PANE,
      4
    ),
    new MineStackObj(
      BUILDING,
      "glass_panel_5",
      "黄緑色の色付きガラス板",
      1,
      Material.STAINED_GLASS_PANE,
      5
    ),
    new MineStackObj(
      BUILDING,
      "glass_panel_6",
      "桃色の色付きガラス板",
      1,
      Material.STAINED_GLASS_PANE,
      6
    ),
    new MineStackObj(
      BUILDING,
      "glass_panel_7",
      "灰色の色付きガラス板",
      1,
      Material.STAINED_GLASS_PANE,
      7
    ),
    new MineStackObj(
      BUILDING,
      "glass_panel_8",
      "薄灰色の色付きガラス板",
      1,
      Material.STAINED_GLASS_PANE,
      8
    ),
    new MineStackObj(
      BUILDING,
      "glass_panel_9",
      "青緑色の色付きガラス板",
      1,
      Material.STAINED_GLASS_PANE,
      9
    ),
    new MineStackObj(
      BUILDING,
      "glass_panel_10",
      "紫色の色付きガラス板",
      1,
      Material.STAINED_GLASS_PANE,
      10
    ),
    new MineStackObj(
      BUILDING,
      "glass_panel_11",
      "青色の色付きガラス板",
      1,
      Material.STAINED_GLASS_PANE,
      11
    ),
    new MineStackObj(
      BUILDING,
      "glass_panel_12",
      "茶色の色付きガラス板",
      1,
      Material.STAINED_GLASS_PANE,
      12
    ),
    new MineStackObj(
      BUILDING,
      "glass_panel_13",
      "緑色の色付きガラス板",
      1,
      Material.STAINED_GLASS_PANE,
      13
    ),
    new MineStackObj(
      BUILDING,
      "glass_panel_14",
      "赤色の色付きガラス板",
      1,
      Material.STAINED_GLASS_PANE,
      14
    ),
    new MineStackObj(
      BUILDING,
      "glass_panel_15",
      "黒色の色付きガラス板",
      1,
      Material.STAINED_GLASS_PANE,
      15
    ),
    new MineStackObj(BUILDING, "dye_1", "赤色の染料", 1, Material.INK_SACK, 1),
    new MineStackObj(BUILDING, "dye_2", "緑色の染料", 1, Material.INK_SACK, 2),
    new MineStackObj(BUILDING, "dye_5", "紫色の染料", 1, Material.INK_SACK, 5),
    new MineStackObj(BUILDING, "dye_6", "青緑色の染料", 1, Material.INK_SACK, 6),
    new MineStackObj(BUILDING, "dye_7", "薄灰色の染料", 1, Material.INK_SACK, 7),
    new MineStackObj(BUILDING, "dye_8", "灰色の染料", 1, Material.INK_SACK, 8),
    new MineStackObj(BUILDING, "dye_9", "桃色の染料", 1, Material.INK_SACK, 9),
    new MineStackObj(BUILDING, "dye_10", "黄緑色の染料", 1, Material.INK_SACK, 10),
    new MineStackObj(BUILDING, "dye_11", "黄色の染料", 1, Material.INK_SACK, 11),
    new MineStackObj(BUILDING, "dye_12", "空色の染料", 1, Material.INK_SACK, 12),
    new MineStackObj(BUILDING, "dye_13", "赤紫色の染料", 1, Material.INK_SACK, 13),
    new MineStackObj(BUILDING, "dye_14", "橙色の染料", 1, Material.INK_SACK, 14),
    new MineStackObj(BUILDING, "dye_15", "骨粉", 1, Material.INK_SACK, 15),
    new MineStackObj(BUILDING, "ink_sack0", "イカスミ", 1, Material.INK_SACK, 0),
    new MineStackObj(BUILDING, "workbench", "作業台", 1, Material.WORKBENCH, 0),
    new MineStackObj(BUILDING, "bed", "白色のベッド", 1, Material.BED, 0),
    new MineStackObj(BUILDING, "bed_1", "橙色のベッド", 1, Material.BED, 1),
    new MineStackObj(BUILDING, "bed_2", "赤紫色のベッド", 1, Material.BED, 2),
    new MineStackObj(BUILDING, "bed_3", "空色のベッド", 1, Material.BED, 3),
    new MineStackObj(BUILDING, "bed_4", "黄色のベッド", 1, Material.BED, 4),
    new MineStackObj(BUILDING, "bed_5", "黄緑色のベッド", 1, Material.BED, 5),
    new MineStackObj(BUILDING, "bed_6", "桃色のベッド", 1, Material.BED, 6),
    new MineStackObj(BUILDING, "bed_7", "灰色のベッド", 1, Material.BED, 7),
    new MineStackObj(BUILDING, "bed_8", "薄灰色のベッド", 1, Material.BED, 8),
    new MineStackObj(BUILDING, "bed_9", "青緑色のベッド", 1, Material.BED, 9),
    new MineStackObj(BUILDING, "bed_10", "紫色のベッド", 1, Material.BED, 10),
    new MineStackObj(BUILDING, "bed_11", "青色のベッド", 1, Material.BED, 11),
    new MineStackObj(BUILDING, "bed_12", "茶色のベッド", 1, Material.BED, 12),
    new MineStackObj(BUILDING, "bed_13", "緑色のベッド", 1, Material.BED, 13),
    new MineStackObj(BUILDING, "bed_14", "赤色のベッド", 1, Material.BED, 14),
    new MineStackObj(BUILDING, "bed_15", "黒色のベッド", 1, Material.BED, 15)
  )

  // レッドストーン系ブロック
  val minestacklistrs: List[MineStackObj] = List(
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "redstone",
      "レッドストーン",
      1,
      Material.REDSTONE,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "stone_button",
      "石のボタン",
      1,
      Material.STONE_BUTTON,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "wood_button",
      "木のボタン",
      1,
      Material.WOOD_BUTTON,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "stone_plate",
      "石の感圧版",
      1,
      Material.STONE_PLATE,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "wood_plate",
      "木の感圧版",
      1,
      Material.WOOD_PLATE,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "fence_gate",
      "オークのフェンスゲート",
      1,
      Material.FENCE_GATE,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "spruce_fence_gate",
      "マツのフェンスゲート",
      1,
      Material.SPRUCE_FENCE_GATE,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "birch_fence_gate",
      "シラカバのフェンスゲート",
      1,
      Material.BIRCH_FENCE_GATE,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "jungle_fence_gate",
      "ジャングルのフェンスゲート",
      1,
      Material.JUNGLE_FENCE_GATE,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "dark_oak_fence_gate",
      "ダークオークのフェンスゲート",
      1,
      Material.DARK_OAK_FENCE_GATE,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "acacia_fence_gate",
      "アカシアのフェンスゲート",
      1,
      Material.ACACIA_FENCE_GATE,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "redstone_block",
      "レッドストーンブロック",
      1,
      Material.REDSTONE_BLOCK,
      0
    ),
    new MineStackObj(REDSTONE_AND_TRANSPORTATION, "lever", "レバー", 1, Material.LEVER, 0),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "redstone_torch_on",
      "レッドストーントーチ",
      1,
      Material.REDSTONE_TORCH_ON,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "trap_door",
      "木のトラップドア",
      1,
      Material.TRAP_DOOR,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "iron_trapdoor",
      "鉄のトラップドア",
      1,
      Material.IRON_TRAPDOOR,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "gold_plate",
      "重量感圧版 (軽) ",
      1,
      Material.GOLD_PLATE,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "iron_plate",
      "重量感圧版 (重) ",
      1,
      Material.IRON_PLATE,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "wood_door",
      "オークのドア",
      1,
      Material.WOOD_DOOR,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "spruce_door_item",
      "マツのドア",
      1,
      Material.SPRUCE_DOOR_ITEM,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "birch_door_item",
      "シラカバのドア",
      1,
      Material.BIRCH_DOOR_ITEM,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "jungle_door_item",
      "ジャングルのドア",
      1,
      Material.JUNGLE_DOOR_ITEM,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "acacia_door_item",
      "アカシアのドア",
      1,
      Material.ACACIA_DOOR_ITEM,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "dark_oak_door_item",
      "ダークオークのドア",
      1,
      Material.DARK_OAK_DOOR_ITEM,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "note_block",
      "音符ブロック",
      1,
      Material.NOTE_BLOCK,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "redstone_lamp_off",
      "レッドストーンランプ",
      1,
      Material.REDSTONE_LAMP_OFF,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "tripwire_hook",
      "トリップワイヤーフック",
      1,
      Material.TRIPWIRE_HOOK,
      0
    ),
    new MineStackObj(REDSTONE_AND_TRANSPORTATION, "dropper", "ドロッパー", 1, Material.DROPPER, 0),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "piston_sticky_base",
      "粘着ピストン",
      1,
      Material.PISTON_STICKY_BASE,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "piston_base",
      "ピストン",
      1,
      Material.PISTON_BASE,
      0
    ),
    new MineStackObj(REDSTONE_AND_TRANSPORTATION, "tnt", "TNT", 1, Material.TNT, 0),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "trapped_chest",
      "トラップチェスト",
      1,
      Material.TRAPPED_CHEST,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "daylight_detector",
      "日照センサー",
      1,
      Material.DAYLIGHT_DETECTOR,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "iron_door",
      "鉄のドア",
      1,
      Material.IRON_DOOR,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "diode",
      "レッドストーンリピーター",
      1,
      Material.DIODE,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "dispenser",
      "ディスペンサー",
      1,
      Material.DISPENSER,
      0
    ),
    new MineStackObj(REDSTONE_AND_TRANSPORTATION, "hopper", "ホッパー", 1, Material.HOPPER, 0),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "redstone_comparator",
      "レッドストーンコンパレーター",
      1,
      Material.REDSTONE_COMPARATOR,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "powered_rail",
      "パワードレール",
      1,
      Material.POWERED_RAIL,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "detector_rail",
      "ディテクターレール",
      1,
      Material.DETECTOR_RAIL,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "activator_rail",
      "アクティベーターレール",
      1,
      Material.ACTIVATOR_RAIL,
      0
    ),
    new MineStackObj(REDSTONE_AND_TRANSPORTATION, "boat", "オークのボート", 1, Material.BOAT, 0),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "spruce_boat",
      "マツのボート",
      1,
      Material.BOAT_SPRUCE,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "birch_boat",
      "シラカバのボート",
      1,
      Material.BOAT_BIRCH,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "jungle_boat",
      "ジャングルのボート",
      1,
      Material.BOAT_JUNGLE,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "acacia_boat",
      "アカシアのボート",
      1,
      Material.BOAT_ACACIA,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "dark_oak_boat",
      "ダークオークのボート",
      1,
      Material.BOAT_DARK_OAK,
      0
    ),
    new MineStackObj(REDSTONE_AND_TRANSPORTATION, "saddle", "サドル", 1, Material.SADDLE, 0),
    new MineStackObj(REDSTONE_AND_TRANSPORTATION, "minecart", "トロッコ", 1, Material.MINECART, 0),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "chest_minecart",
      "チェスト付きトロッコ",
      1,
      Material.STORAGE_MINECART,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "furnace_minecart",
      "かまど付きトロッコ",
      1,
      Material.POWERED_MINECART,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "hopper_minecart",
      "ホッパー付きトロッコ",
      1,
      Material.HOPPER_MINECART,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "iron_horse_armor",
      "鉄の馬鎧",
      1,
      Material.IRON_BARDING,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "golden_horse_armor",
      "金の馬鎧",
      1,
      Material.GOLD_BARDING,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "diamond_horse_armor",
      "ダイヤの馬鎧",
      1,
      Material.DIAMOND_BARDING,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "record_13",
      "レコード",
      1,
      Material.GOLD_RECORD,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "record_cat",
      "レコード",
      1,
      Material.GREEN_RECORD,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "record_blocks",
      "レコード",
      1,
      Material.RECORD_3,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "record_chirp",
      "レコード",
      1,
      Material.RECORD_4,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "record_far",
      "レコード",
      1,
      Material.RECORD_5,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "record_mall",
      "レコード",
      1,
      Material.RECORD_6,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "record_mellohi",
      "レコード",
      1,
      Material.RECORD_7,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "record_stal",
      "レコード",
      1,
      Material.RECORD_8,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "record_strad",
      "レコード",
      1,
      Material.RECORD_9,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "record_ward",
      "レコード",
      1,
      Material.RECORD_10,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "record_11",
      "レコード",
      1,
      Material.RECORD_11,
      0
    ),
    new MineStackObj(
      REDSTONE_AND_TRANSPORTATION,
      "record_wait",
      "レコード",
      1,
      Material.RECORD_12,
      0
    )
  )

  /**
   * デフォルトでガチャの内容に含まれている景品。
   */
  val minestackBuiltinGachaPrizes: List[MineStackObj] = List(
    new MineStackObj(
      "gachaimo",
      None,
      1,
      StaticGachaPrizeFactory.getGachaRingo,
      true,
      -1,
      GACHA_PRIZES
    ),
    new MineStackObj(
      "exp_bottle",
      Some("エンチャントの瓶"),
      1,
      Material.EXP_BOTTLE,
      0,
      false,
      -1,
      GACHA_PRIZES
    )
  )

  /**
   * マインスタックに格納できるガチャ景品。
   */
  // これは後に変更されるのでミュータブルでないといけない
  val minestackGachaPrizes: mutable.ArrayBuffer[MineStackObj] =
    mutable.ArrayBuffer.from(minestackBuiltinGachaPrizes)

  // ランダムアクセスしないので
  val minestacklist: mutable.ArrayBuffer[MineStackObj] = mutable.ArrayBuffer()
}
