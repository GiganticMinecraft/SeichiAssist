package com.github.unchama.seichiassist.subsystems.minestack.bukkit

import cats.Functor
import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.minecraft.bukkit.algebra.CloneableBukkitItemStack._
import com.github.unchama.minecraft.objects.MinecraftMaterial
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaPrizeAPI
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.CanBeSignedAsGachaPrize
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObject.{
  MineStackObjectByItemStack,
  MineStackObjectByMaterial
}
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObjectCategory._
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject._
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.{PotionData, PotionType}

class BukkitMineStackObjectList[F[_]: Sync](
  implicit gachaPrizeAPI: GachaPrizeAPI[F, ItemStack, Player],
  minecraftMaterial: MinecraftMaterial[Material, ItemStack]
) extends MineStackObjectList[F, ItemStack, Player] {

  private def leftElems[A](elems: A*): List[Either[A, Nothing]] = elems.toList.map(Left.apply)

  private def rightElems[B](elems: B*): List[Either[Nothing, B]] = elems.toList.map(Right.apply)

  // @formatter:off

  // 採掘可能ブロック
  private val minestacklistmine: List[MineStackObjectGroup[ItemStack]] = leftElems(
    MineStackObjectByMaterial(ORES, "coal_ore", "石炭鉱石", Material.COAL_ORE),
    MineStackObjectByMaterial(ORES, "coal", "石炭", Material.COAL),
    MineStackObjectByMaterial(ORES, "coal_block", "石炭ブロック", Material.COAL_BLOCK),
    MineStackObjectByMaterial(ORES, "coal_1", "木炭", Material.CHARCOAL),
    MineStackObjectByMaterial(ORES, "iron_ore", "鉄鉱石", Material.IRON_ORE),
    MineStackObjectByMaterial(ORES, "iron_ingot", "鉄インゴット", Material.IRON_INGOT),
    MineStackObjectByMaterial(ORES, "iron_block", "鉄ブロック", Material.IRON_BLOCK),
    MineStackObjectByMaterial(ORES, "quartz_ore", "ネザー水晶鉱石", Material.NETHER_QUARTZ_ORE),
    MineStackObjectByMaterial(ORES, "quartz", "ネザー水晶", Material.QUARTZ),
    MineStackObjectByMaterial(ORES, "gold_ore", "金鉱石", Material.GOLD_ORE),
    MineStackObjectByMaterial(ORES, "gold_ingot", "金インゴット", Material.GOLD_INGOT),
    MineStackObjectByMaterial(ORES, "gold_block", "金ブロック", Material.GOLD_BLOCK),
    MineStackObjectByMaterial(ORES, "redstone_ore", "レッドストーン鉱石", Material.REDSTONE_ORE),
    MineStackObjectByMaterial(ORES, "lapis_ore", "ラピスラズリ鉱石", Material.LAPIS_ORE),
    MineStackObjectByMaterial(ORES, "lapis_lazuli", "ラピスラズリ", Material.LAPIS_LAZULI),
    MineStackObjectByMaterial(ORES, "lapis_block", "ラピスラズリブロック", Material.LAPIS_BLOCK),
    MineStackObjectByMaterial(ORES, "diamond_ore", "ダイヤモンド鉱石", Material.DIAMOND_ORE),
    MineStackObjectByMaterial(ORES, "diamond", "ダイヤモンド", Material.DIAMOND),
    MineStackObjectByMaterial(ORES, "diamond_block", "ダイヤモンドブロック", Material.DIAMOND_BLOCK),
    MineStackObjectByMaterial(ORES, "emerald_ore", "エメラルド鉱石", Material.EMERALD_ORE),
    MineStackObjectByMaterial(ORES, "emerald", "エメラルド", Material.EMERALD),
    MineStackObjectByMaterial(ORES, "emerald_block", "エメラルドブロック", Material.EMERALD_BLOCK),
    MineStackObjectByMaterial(ORES, "copper_ore", "銅鉱石", Material.COPPER_ORE),
    MineStackObjectByMaterial(ORES, "deepslate_coal_ore", "深層石炭鉱石", Material.DEEPSLATE_COAL_ORE),
    MineStackObjectByMaterial(ORES, "deepslate_iron_ore", "深層鉄鉱石", Material.DEEPSLATE_IRON_ORE),
    MineStackObjectByMaterial(ORES, "deepslate_gold_ore", "深層金鉱石", Material.DEEPSLATE_GOLD_ORE),
    MineStackObjectByMaterial(ORES, "deepslate_redstone_ore", "深層レッドストーン鉱石", Material.DEEPSLATE_REDSTONE_ORE),
    MineStackObjectByMaterial(ORES, "deepslate_emerald_ore", "深層エメラルド鉱石", Material.DEEPSLATE_EMERALD_ORE),
    MineStackObjectByMaterial(ORES, "deepslate_lapis_ore", "深層ラピスラズリ鉱石", Material.DEEPSLATE_LAPIS_ORE),
    MineStackObjectByMaterial(ORES, "deepslate_diamond_ore", "深層ダイヤモンド鉱石", Material.DEEPSLATE_DIAMOND_ORE),
    MineStackObjectByMaterial(ORES, "deepslate_copper_ore", "深層銅鉱石", Material.DEEPSLATE_COPPER_ORE),
    MineStackObjectByMaterial(ORES, "nether_gold_ore", "ネザー金鉱石", Material.NETHER_GOLD_ORE),
    MineStackObjectByMaterial(ORES, "ancient_debris", "古代の残骸", Material.ANCIENT_DEBRIS),
    MineStackObjectByMaterial(ORES, "raw_iron_block", "鉄の原石ブロック", Material.RAW_IRON_BLOCK),
    MineStackObjectByMaterial(ORES, "raw_gold_block", "金の原石ブロック", Material.RAW_GOLD_BLOCK),
    MineStackObjectByMaterial(ORES, "amethyst_block", "アメジストブロック", Material.AMETHYST_BLOCK),
    MineStackObjectByMaterial(ORES, "budding_amethyst", "芽生えたアメジスト", Material.BUDDING_AMETHYST),
    MineStackObjectByMaterial(ORES, "netherite_block", "ネザライトブロック", Material.NETHERITE_BLOCK),
    MineStackObjectByMaterial(ORES, "amethyst_shard", "アメジストの欠片", Material.AMETHYST_SHARD),
    MineStackObjectByMaterial(ORES, "raw_copper", "銅の原石", Material.RAW_COPPER),
    MineStackObjectByMaterial(ORES, "raw_iron", "鉄の原石", Material.RAW_IRON),
    MineStackObjectByMaterial(ORES, "raw_gold", "金の原石", Material.RAW_GOLD),
    MineStackObjectByMaterial(ORES, "netherite_ingot", "ネザライトインゴット", Material.NETHERITE_INGOT),
    MineStackObjectByMaterial(ORES, "netherite_scrap", "ネザライトの欠片", Material.NETHERITE_SCRAP),
    MineStackObjectByMaterial(ORES, "small_amethyst_bud", "小さなアメジストの芽", Material.SMALL_AMETHYST_BUD),
    MineStackObjectByMaterial(ORES, "medium_amethyst_bud", "中くらいのアメジストの芽", Material.MEDIUM_AMETHYST_BUD),
    MineStackObjectByMaterial(ORES, "large_amethyst_bud", "大きなアメジストの芽", Material.LARGE_AMETHYST_BUD),
    MineStackObjectByMaterial(ORES, "amethyst_cluster", "アメジストの塊", Material.AMETHYST_CLUSTER),
    MineStackObjectByMaterial(ORES, "iron_nugget", "鉄塊", Material.IRON_NUGGET),
  )

  import scala.util.chaining._

  // モンスター+動物ドロップ
  private val minestacklistdrop: List[MineStackObjectGroup[ItemStack]] = leftElems(
    MineStackObjectByMaterial(MOB_DROP, "ender_pearl", "エンダーパール", Material.ENDER_PEARL),
    MineStackObjectByMaterial(MOB_DROP, "ender_eye", "エンダーアイ", Material.ENDER_EYE),
    MineStackObjectByMaterial(MOB_DROP, "slime_ball", "スライムボール", Material.SLIME_BALL),
    MineStackObjectByMaterial(MOB_DROP, "slime", "スライムブロック", Material.SLIME_BLOCK),
    MineStackObjectByMaterial(MOB_DROP, "rotten_flesh", "腐った肉", Material.ROTTEN_FLESH),
    MineStackObjectByMaterial(MOB_DROP, "bone", "骨", Material.BONE),
    MineStackObjectByMaterial(MOB_DROP, "sulphur", "火薬", Material.GUNPOWDER),
    MineStackObjectByMaterial(MOB_DROP, "arrow", "矢", Material.ARROW),
    MineStackObjectByMaterial(MOB_DROP, "spider_eye", "蜘蛛の目", Material.SPIDER_EYE),
    MineStackObjectByItemStack(MOB_DROP, "tipped_arrow", Some("鈍化の矢"), hasNameLore = false, new ItemStack(Material.TIPPED_ARROW).tap { itemStack =>
      val meta = itemStack.getItemMeta.asInstanceOf[PotionMeta]
      meta.setBasePotionData(new PotionData(PotionType.SLOWNESS))
      itemStack.setItemMeta(meta)
    }),
    MineStackObjectByMaterial(MOB_DROP, "string", "糸", Material.STRING),
    MineStackObjectByMaterial(MOB_DROP, "name_tag", "名札", Material.NAME_TAG),
    MineStackObjectByMaterial(MOB_DROP, "lead", "リード", Material.LEAD),
    MineStackObjectByMaterial(MOB_DROP, "glass_bottle", "ガラス瓶", Material.GLASS_BOTTLE),
    MineStackObjectByMaterial(MOB_DROP, "gold_nugget", "金塊", Material.GOLD_NUGGET),
    MineStackObjectByMaterial(MOB_DROP, "blaze_rod", "ブレイズロッド", Material.BLAZE_ROD),
    MineStackObjectByMaterial(MOB_DROP, "blaze_powder", "ブレイズパウダー", Material.BLAZE_POWDER),
    MineStackObjectByMaterial(MOB_DROP, "ghast_tear", "ガストの涙", Material.GHAST_TEAR),
    MineStackObjectByMaterial(MOB_DROP, "magma_cream", "マグマクリーム", Material.MAGMA_CREAM),
    MineStackObjectByMaterial(MOB_DROP, "prismarine_shard", "プリズマリンの欠片", Material.PRISMARINE_SHARD),
    MineStackObjectByMaterial(MOB_DROP, "prismarine_crystals", "プリズマリンクリスタル", Material.PRISMARINE_CRYSTALS),
    MineStackObjectByMaterial(MOB_DROP, "feather", "羽", Material.FEATHER),
    MineStackObjectByMaterial(MOB_DROP, "leather", "革", Material.LEATHER),
    MineStackObjectByMaterial(MOB_DROP, "rabbit_hide", "ウサギの皮", Material.RABBIT_HIDE),
    MineStackObjectByMaterial(MOB_DROP, "rabbit_foot", "ウサギの足", Material.RABBIT_FOOT),
    MineStackObjectByMaterial(MOB_DROP, "dragon_egg", "エンドラの卵", Material.DRAGON_EGG),
    MineStackObjectByMaterial(MOB_DROP, "shulker_shell", "シュルカーの殻", Material.SHULKER_SHELL),
    MineStackObjectByMaterial(MOB_DROP, "totem_of_undying", "不死のトーテム", Material.TOTEM_OF_UNDYING),
    MineStackObjectByMaterial(MOB_DROP, "dragon_head", "エンダードラゴンの頭", Material.DRAGON_HEAD),
    MineStackObjectByMaterial(MOB_DROP, "wither_skeleton_skull", "ウィザースケルトンの頭", Material.WITHER_SKELETON_SKULL),
    MineStackObjectByMaterial(MOB_DROP, "stick", "棒", Material.STICK),
    MineStackObjectByMaterial(MOB_DROP, "scute", "カメのウロコ", Material.SCUTE),
    MineStackObjectByMaterial(MOB_DROP, "fermented_spider_eye", "発酵したクモの目", Material.FERMENTED_SPIDER_EYE),
    MineStackObjectByMaterial(MOB_DROP, "golden_carrot", "金のニンジン", Material.GOLDEN_CARROT),
    MineStackObjectByMaterial(MOB_DROP, "skeleton_skull", "スケルトンの頭蓋骨", Material.SKELETON_SKULL),
    MineStackObjectByMaterial(MOB_DROP, "zombie_head", "ゾンビの頭", Material.ZOMBIE_HEAD),
    MineStackObjectByMaterial(MOB_DROP, "creeper_head", "クリーパーの頭", Material.CREEPER_HEAD),
    MineStackObjectByMaterial(MOB_DROP, "nether_star", "ネザースター", Material.NETHER_STAR),
    MineStackObjectByMaterial(MOB_DROP, "dragon_breath", "ドラゴンブレス", Material.DRAGON_BREATH),
    MineStackObjectByMaterial(MOB_DROP, "phantom_membrane", "ファントムの皮膜", Material.PHANTOM_MEMBRANE),
    MineStackObjectByMaterial(MOB_DROP, "nautilus_shell", "オウムガイの殻", Material.NAUTILUS_SHELL),
    MineStackObjectByMaterial(MOB_DROP, "heart_of_the_sea", "海洋の心", Material.HEART_OF_THE_SEA),
  ) ++ rightElems(
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(MOB_DROP, "music_disc_otherside", "レコード", Material.MUSIC_DISC_OTHERSIDE),
      List(
        MineStackObjectByMaterial(MOB_DROP, "music_disc_pigstep", "レコード", Material.MUSIC_DISC_PIGSTEP),
        MineStackObjectByMaterial(MOB_DROP,"record_13","レコード",Material.MUSIC_DISC_13),
        MineStackObjectByMaterial(MOB_DROP,"record_cat","レコード",Material.MUSIC_DISC_CAT),
        MineStackObjectByMaterial(MOB_DROP,"record_blocks","レコード",Material.MUSIC_DISC_BLOCKS),
        MineStackObjectByMaterial(MOB_DROP,"record_chirp","レコード",Material.MUSIC_DISC_CHIRP),
        MineStackObjectByMaterial(MOB_DROP,"record_far","レコード",Material.MUSIC_DISC_FAR),
        MineStackObjectByMaterial(MOB_DROP,"record_mall","レコード",Material.MUSIC_DISC_MALL),
        MineStackObjectByMaterial(MOB_DROP,"record_mellohi","レコード",Material.MUSIC_DISC_MELLOHI),
        MineStackObjectByMaterial(MOB_DROP,"record_stal","レコード",Material.MUSIC_DISC_STAL),
        MineStackObjectByMaterial(MOB_DROP,"record_strad","レコード",Material.MUSIC_DISC_STRAD),
        MineStackObjectByMaterial(MOB_DROP,"record_ward","レコード",Material.MUSIC_DISC_WARD),
        MineStackObjectByMaterial(MOB_DROP,"record_11","レコード",Material.MUSIC_DISC_11),
        MineStackObjectByMaterial(MOB_DROP,"record_wait","レコード",Material.MUSIC_DISC_WAIT),
      )
    )
  )

  // 採掘で入手可能な農業系ブロック
  private val minestacklistfarm: List[MineStackObjectGroup[ItemStack]] = leftElems(
    MineStackObjectByMaterial(AGRICULTURAL, "seeds", "種", Material.WHEAT_SEEDS),
    MineStackObjectByMaterial(AGRICULTURAL, "apple", "リンゴ", Material.APPLE),
    MineStackObjectByMaterial(AGRICULTURAL, "long_grass2", "シダ", Material.FERN),
    MineStackObjectByMaterial(AGRICULTURAL, "dead_bush", "枯れ木", Material.DEAD_BUSH),
    MineStackObjectByMaterial(AGRICULTURAL, "cactus", "サボテン", Material.CACTUS),
    MineStackObjectByMaterial(AGRICULTURAL, "vine", "ツタ", Material.VINE),
    MineStackObjectByMaterial(AGRICULTURAL, "water_lily", "スイレンの葉", Material.LILY_PAD),
    MineStackObjectByMaterial(AGRICULTURAL, "yellow_flower", "タンポポ", Material.DANDELION),
    MineStackObjectByMaterial(AGRICULTURAL, "red_rose0", "ポピー", Material.POPPY),
    MineStackObjectByMaterial(AGRICULTURAL, "red_rose1", "ヒスイラン", Material.BLUE_ORCHID),
    MineStackObjectByMaterial(AGRICULTURAL, "red_rose2", "アリウム", Material.ALLIUM),
    MineStackObjectByMaterial(AGRICULTURAL, "red_rose3", "ヒナソウ", Material.AZURE_BLUET),
    MineStackObjectByMaterial(AGRICULTURAL, "red_rose4", "赤色のチューリップ", Material.RED_TULIP),
    MineStackObjectByMaterial(AGRICULTURAL, "red_rose5", "橙色のチューリップ", Material.ORANGE_TULIP),
    MineStackObjectByMaterial(AGRICULTURAL, "red_rose6", "白色のチューリップ", Material.WHITE_TULIP),
    MineStackObjectByMaterial(AGRICULTURAL, "red_rose7", "桃色のチューリップ", Material.PINK_TULIP),
    MineStackObjectByMaterial(AGRICULTURAL, "red_rose8", "フランスギク", Material.OXEYE_DAISY),
    MineStackObjectByMaterial(AGRICULTURAL, "leaves", "オークの葉", Material.OAK_LEAVES),
    MineStackObjectByMaterial(AGRICULTURAL, "leaves1", "マツの葉", Material.SPRUCE_LEAVES),
    MineStackObjectByMaterial(AGRICULTURAL, "leaves2", "シラカバの葉", Material.BIRCH_LEAVES),
    MineStackObjectByMaterial(AGRICULTURAL, "leaves3", "ジャングルの葉", Material.JUNGLE_LEAVES),
    MineStackObjectByMaterial(AGRICULTURAL, "leaves_2", "アカシアの葉", Material.ACACIA_LEAVES),
    MineStackObjectByMaterial(AGRICULTURAL, "leaves_21", "ダークオークの葉", Material.DARK_OAK_LEAVES),
    MineStackObjectByMaterial(AGRICULTURAL, "double_plant0", "ヒマワリ", Material.SUNFLOWER),
    MineStackObjectByMaterial(AGRICULTURAL, "double_plant1", "ライラック", Material.LILAC),
    MineStackObjectByMaterial(AGRICULTURAL, "double_plant2", "高い草", Material.TALL_GRASS),
    MineStackObjectByMaterial(AGRICULTURAL, "double_plant3", "大きなシダ", Material.LARGE_FERN),
    MineStackObjectByMaterial(AGRICULTURAL, "double_plant4", "バラの低木", Material.ROSE_BUSH),
    MineStackObjectByMaterial(AGRICULTURAL, "double_plant5", "ボタン", Material.PEONY),
    MineStackObjectByMaterial(AGRICULTURAL, "sugar_cane", "サトウキビ", Material.SUGAR_CANE),
    MineStackObjectByMaterial(AGRICULTURAL, "pumpkin", "カボチャ", Material.PUMPKIN),
    MineStackObjectByMaterial(AGRICULTURAL, "ink_sack3", "カカオ豆", Material.COCOA_BEANS),
    MineStackObjectByMaterial(AGRICULTURAL, "huge_mushroom_1", "キノコ", Material.BROWN_MUSHROOM),
    MineStackObjectByMaterial(AGRICULTURAL, "huge_mushroom_2", "キノコ", Material.RED_MUSHROOM),
    MineStackObjectByMaterial(AGRICULTURAL, "melon", "スイカの薄切り", Material.MELON_SLICE),
    MineStackObjectByMaterial(AGRICULTURAL, "melon_block", "スイカ", Material.MELON),
    MineStackObjectByMaterial(AGRICULTURAL, "sapling", "オークの苗木", Material.OAK_SAPLING),
    MineStackObjectByMaterial(AGRICULTURAL, "sapling1", "マツの苗木", Material.SPRUCE_SAPLING),
    MineStackObjectByMaterial(AGRICULTURAL, "sapling2", "シラカバの苗木", Material.BIRCH_SAPLING),
    MineStackObjectByMaterial(AGRICULTURAL, "sapling3", "ジャングルの苗木", Material.JUNGLE_SAPLING),
    MineStackObjectByMaterial(AGRICULTURAL, "sapling4", "アカシアの苗木", Material.ACACIA_SAPLING),
    MineStackObjectByMaterial(AGRICULTURAL, "sapling5", "ダークオークの苗木", Material.DARK_OAK_SAPLING),
    MineStackObjectByMaterial(AGRICULTURAL, "beetroot", "ビートルート", Material.BEETROOT),
    MineStackObjectByMaterial(AGRICULTURAL, "beetroot_seeds", "ビートルートの種", Material.BEETROOT_SEEDS),
    MineStackObjectByMaterial(AGRICULTURAL, "carrot_item", "ニンジン", Material.CARROT),
    MineStackObjectByMaterial(AGRICULTURAL, "potato_item", "ジャガイモ", Material.POTATO),
    MineStackObjectByMaterial(AGRICULTURAL, "poisonous_potato", "青くなったジャガイモ", Material.POISONOUS_POTATO),
    MineStackObjectByMaterial(AGRICULTURAL, "wheat", "小麦", Material.WHEAT),
    MineStackObjectByMaterial(AGRICULTURAL, "pumpkin_seeds", "カボチャの種", Material.PUMPKIN_SEEDS),
    MineStackObjectByMaterial(AGRICULTURAL, "melon_seeds", "スイカの種", Material.MELON_SEEDS),
    MineStackObjectByMaterial(AGRICULTURAL, "nether_stalk", "ネザーウォート", Material.NETHER_WART),
    MineStackObjectByMaterial(AGRICULTURAL, "chorus_fruit", "コーラスフルーツ", Material.CHORUS_FRUIT),
    MineStackObjectByMaterial(AGRICULTURAL, "chorus_flower", "コーラスフラワー", Material.CHORUS_FLOWER),
    MineStackObjectByMaterial(AGRICULTURAL, "popped_chorus_fruit", "焼いたコーラスフルーツ", Material.POPPED_CHORUS_FRUIT),
    MineStackObjectByMaterial(AGRICULTURAL, "egg", "卵", Material.EGG),
    MineStackObjectByMaterial(AGRICULTURAL, "pork", "生の豚肉", Material.PORKCHOP),
    MineStackObjectByMaterial(AGRICULTURAL, "cooked_porkchop", "焼き豚", Material.COOKED_PORKCHOP),
    MineStackObjectByMaterial(AGRICULTURAL, "raw_chicken", "生の鶏肉", Material.CHICKEN),
    MineStackObjectByMaterial(AGRICULTURAL, "cooked_chicken", "焼き鳥", Material.COOKED_CHICKEN),
    MineStackObjectByMaterial(AGRICULTURAL, "mutton", "生の羊肉", Material.MUTTON),
    MineStackObjectByMaterial(AGRICULTURAL, "cooked_mutton", "焼いた羊肉", Material.COOKED_MUTTON),
    MineStackObjectByMaterial(AGRICULTURAL, "raw_beef", "生の牛肉", Material.BEEF),
    MineStackObjectByMaterial(AGRICULTURAL, "cooked_beaf", "ステーキ", Material.COOKED_BEEF),
    MineStackObjectByMaterial(AGRICULTURAL, "rabbit", "生の兎肉", Material.RABBIT),
    MineStackObjectByMaterial(AGRICULTURAL, "cooked_rabbit", "焼き兎肉", Material.COOKED_RABBIT),
    MineStackObjectByMaterial(AGRICULTURAL, "raw_fish0", "生魚", Material.COD),
    MineStackObjectByMaterial(AGRICULTURAL, "cooked_fish0", "焼き魚", Material.COOKED_COD),
    MineStackObjectByMaterial(AGRICULTURAL, "raw_fish1", "生鮭", Material.SALMON),
    MineStackObjectByMaterial(AGRICULTURAL, "cooked_fish1", "焼き鮭", Material.COOKED_SALMON),
    MineStackObjectByMaterial(AGRICULTURAL, "raw_fish2", "クマノミ", Material.TROPICAL_FISH),
    MineStackObjectByMaterial(AGRICULTURAL, "raw_fish3", "フグ", Material.PUFFERFISH),
    MineStackObjectByMaterial(AGRICULTURAL, "bread", "パン", Material.BREAD),
    MineStackObjectByMaterial(AGRICULTURAL, "sugar", "砂糖", Material.SUGAR),
    MineStackObjectByMaterial(AGRICULTURAL, "baked_potato", "ベイクドポテト", Material.BAKED_POTATO),
    MineStackObjectByMaterial(AGRICULTURAL, "cake", "ケーキ", Material.CAKE),
    MineStackObjectByMaterial(AGRICULTURAL, "mushroom_stew", "キノコシチュー", Material.MUSHROOM_STEW),
    MineStackObjectByMaterial(AGRICULTURAL, "rabbit_stew", "ウサギシチュー", Material.RABBIT_STEW),
    MineStackObjectByMaterial(AGRICULTURAL, "beetroot_soup", "ビートルートスープ", Material.BEETROOT_SOUP),
    MineStackObjectByMaterial(AGRICULTURAL, "bowl", "ボウル", Material.BOWL),
    MineStackObjectByMaterial(AGRICULTURAL, "milk_bucket", "牛乳", Material.MILK_BUCKET),
    MineStackObjectByMaterial(AGRICULTURAL, "kelp", "コンブ", Material.KELP),
    MineStackObjectByMaterial(AGRICULTURAL, "crimson_hyphae", "真紅の菌糸", Material.CRIMSON_HYPHAE),
    MineStackObjectByMaterial(AGRICULTURAL, "warped_hyphae", "歪んだ菌糸", Material.WARPED_HYPHAE),
    MineStackObjectByMaterial(AGRICULTURAL, "azalea_leaves", "ツツジの葉", Material.AZALEA_LEAVES),
    MineStackObjectByMaterial(AGRICULTURAL, "flowering_azalea_leaves", "開花したツツジの葉", Material.FLOWERING_AZALEA_LEAVES),
    MineStackObjectByMaterial(AGRICULTURAL, "carved_pumpkin", "くり抜かれたカボチャ", Material.CARVED_PUMPKIN),
    MineStackObjectByMaterial(AGRICULTURAL, "pufferfish_bucket", "フグ入りバケツ", Material.PUFFERFISH_BUCKET),
    MineStackObjectByMaterial(AGRICULTURAL, "salmon_bucket", "サケ入りバケツ", Material.SALMON_BUCKET),
    MineStackObjectByMaterial(AGRICULTURAL, "cod_bucket", "タラ入りバケツ", Material.COD_BUCKET),
    MineStackObjectByMaterial(AGRICULTURAL, "tropical_fish_bucket", "熱帯魚入りバケツ", Material.TROPICAL_FISH_BUCKET),
    MineStackObjectByMaterial(AGRICULTURAL, "axolotl_bucket", "ウーパールーパー入りバケツ", Material.AXOLOTL_BUCKET),
    MineStackObjectByMaterial(AGRICULTURAL, "dried_kelp_block", "乾燥した昆布ブロック", Material.DRIED_KELP_BLOCK),
    MineStackObjectByMaterial(AGRICULTURAL, "suspicious_stew", "怪しげなシチュー", Material.SUSPICIOUS_STEW),
    MineStackObjectByMaterial(AGRICULTURAL, "cookie", "クッキー", Material.COOKIE),
    MineStackObjectByMaterial(AGRICULTURAL, "dried_kelp", "乾燥した昆布", Material.DRIED_KELP),
    MineStackObjectByMaterial(AGRICULTURAL, "glistering_melon_slice", "きらめくスイカの薄切り", Material.GLISTERING_MELON_SLICE),
  )

  // 建築系ブロック
  private val minestacklistbuild: List[MineStackObjectGroup[ItemStack]] = leftElems(
    MineStackObjectByMaterial(BUILDING, "cobblestone", "丸石", Material.COBBLESTONE),
    MineStackObjectByMaterial(BUILDING, "cobblestone_wall_0", "丸石の壁", Material.COBBLESTONE_WALL),
    MineStackObjectByMaterial(BUILDING, "mossy_cobblestone", "苔石", Material.MOSSY_COBBLESTONE),
    MineStackObjectByMaterial(BUILDING, "cobblestone_wall_1", "苔石の壁", Material.MOSSY_COBBLESTONE_WALL),
    MineStackObjectByMaterial(BUILDING, "stone", "石", Material.STONE),
    MineStackObjectByMaterial(BUILDING, "smooth_brick0", "石レンガ", Material.STONE_BRICKS),
    MineStackObjectByMaterial(BUILDING, "smooth_brick3", "模様入り石レンガ", Material.CHISELED_STONE_BRICKS),
    MineStackObjectByMaterial(BUILDING, "smooth_brick1", "苔石レンガ", Material.MOSSY_STONE_BRICKS),
    MineStackObjectByMaterial(BUILDING, "smooth_brick2", "ひびの入った石レンガ", Material.CRACKED_STONE_BRICKS),
    MineStackObjectByMaterial(BUILDING, "sand", "砂", Material.SAND),
    MineStackObjectByMaterial(BUILDING, "sandstone", "砂岩", Material.SANDSTONE),
    MineStackObjectByMaterial(BUILDING, "sandstone1", "模様入りの砂岩", Material.CHISELED_SANDSTONE),
    MineStackObjectByMaterial(BUILDING, "sandstone2", "なめらかな砂岩", Material.SMOOTH_SANDSTONE),
    MineStackObjectByMaterial(BUILDING, "red_sand", "赤い砂", Material.RED_SAND),
    MineStackObjectByMaterial(BUILDING, "red_sandstone", "赤い砂岩", Material.RED_SANDSTONE),
    MineStackObjectByMaterial(BUILDING, "red_sandstone1", "模様入りの赤い砂岩", Material.CHISELED_RED_SANDSTONE),
    MineStackObjectByMaterial(BUILDING, "red_sandstone2", "なめらかな赤い砂岩", Material.SMOOTH_RED_SANDSTONE),
    MineStackObjectByMaterial(BUILDING, "clay_ball", "粘土", Material.CLAY_BALL),
    MineStackObjectByMaterial(BUILDING, "clay", "粘土(ブロック)", Material.CLAY),
    MineStackObjectByMaterial(BUILDING, "brick_item", "レンガ", Material.BRICK),
    MineStackObjectByMaterial(BUILDING, "brick", "レンガ(ブロック)", Material.BRICKS),
    MineStackObjectByMaterial(BUILDING, "quartz_block", "ネザー水晶ブロック", Material.QUARTZ_BLOCK),
    MineStackObjectByMaterial(BUILDING, "quartz_block1", "模様入りネザー水晶ブロック", Material.CHISELED_QUARTZ_BLOCK),
    MineStackObjectByMaterial(BUILDING, "quartz_block2", "柱状ネザー水晶ブロック", Material.QUARTZ_PILLAR),
    MineStackObjectByMaterial(BUILDING, "netherrack", "ネザーラック", Material.NETHERRACK),
    MineStackObjectByMaterial(BUILDING, "nether_brick_item", "ネザーレンガ", Material.NETHER_BRICK),
    MineStackObjectByMaterial(BUILDING, "nether_brick", "ネザーレンガ(ブロック)", Material.NETHER_BRICKS),
    MineStackObjectByMaterial(BUILDING, "red_nether_brick", "赤いネザーレンガ", Material.RED_NETHER_BRICKS),
    MineStackObjectByMaterial(BUILDING, "nether_wart_block", "ネザ－ウォートブロック", Material.NETHER_WART_BLOCK),
    MineStackObjectByMaterial(BUILDING, "ender_stone", "エンドストーン", Material.END_STONE),
    MineStackObjectByMaterial(BUILDING, "end_bricks", "エンドストーンレンガ", Material.END_STONE_BRICKS),
    MineStackObjectByMaterial(BUILDING, "purpur_block", "プルプァブロック", Material.PURPUR_BLOCK),
    MineStackObjectByMaterial(BUILDING, "purpur_pillar", "柱状プルプァブロック", Material.PURPUR_PILLAR),
    MineStackObjectByMaterial(BUILDING, "prismarine0", "プリズマリン", Material.PRISMARINE),
    MineStackObjectByMaterial(BUILDING, "prismarine1", "プリズマリンレンガ", Material.PRISMARINE_BRICKS),
    MineStackObjectByMaterial(BUILDING, "prismarine2", "ダークプリズマリン", Material.DARK_PRISMARINE),
    MineStackObjectByMaterial(BUILDING, "sea_lantern", "シーランタン", Material.SEA_LANTERN),
    MineStackObjectByMaterial(BUILDING, "granite", "花崗岩", Material.GRANITE),
    MineStackObjectByMaterial(BUILDING, "polished_granite", "磨かれた花崗岩", Material.POLISHED_GRANITE),
    MineStackObjectByMaterial(BUILDING, "diorite", "閃緑岩", Material.DIORITE),
    MineStackObjectByMaterial(BUILDING, "polished_diorite", "磨かれた閃緑岩", Material.POLISHED_DIORITE),
    MineStackObjectByMaterial(BUILDING, "andesite", "安山岩", Material.ANDESITE),
    MineStackObjectByMaterial(BUILDING, "polished_andesite", "磨かれた安山岩", Material.POLISHED_ANDESITE),
    MineStackObjectByMaterial(BUILDING, "dirt", "土", Material.DIRT),
    MineStackObjectByMaterial(BUILDING, "grass", "草ブロック", Material.GRASS),
    MineStackObjectByMaterial(BUILDING, "gravel", "砂利", Material.GRAVEL),
    MineStackObjectByMaterial(BUILDING, "flint", "火打石", Material.FLINT),
    MineStackObjectByMaterial(BUILDING, "flint_and_steel", "火打石と打ち金", Material.FLINT_AND_STEEL),
    MineStackObjectByMaterial(BUILDING, "dirt1", "粗い土", Material.COARSE_DIRT),
    MineStackObjectByMaterial(BUILDING, "dirt2", "ポドゾル", Material.PODZOL),
    MineStackObjectByMaterial(BUILDING, "snow_block", "雪", Material.SNOW_BLOCK),
    MineStackObjectByMaterial(BUILDING, "snow_layer", "雪タイル", Material.SNOW),
    MineStackObjectByMaterial(BUILDING, "snow_ball", "雪玉", Material.SNOWBALL),
    MineStackObjectByMaterial(BUILDING, "ice", "氷", Material.ICE),
    MineStackObjectByMaterial(BUILDING, "packed_ice", "氷塊", Material.PACKED_ICE),
    MineStackObjectByMaterial(BUILDING, "mycel", "菌糸", Material.MYCELIUM),
    MineStackObjectByMaterial(BUILDING, "bone_block", "骨ブロック", Material.BONE_BLOCK),
    MineStackObjectByMaterial(BUILDING, "sponge", "スポンジ", Material.SPONGE),
    MineStackObjectByMaterial(BUILDING, "wet_sponge", "濡れたスポンジ", Material.WET_SPONGE),
    MineStackObjectByMaterial(BUILDING, "soul_sand", "ソウルサンド", Material.SOUL_SAND),
    MineStackObjectByMaterial(BUILDING, "magma", "マグマブロック", Material.MAGMA_BLOCK),
    MineStackObjectByMaterial(BUILDING, "obsidian", "黒曜石", Material.OBSIDIAN),
    MineStackObjectByMaterial(BUILDING, "glowstone_dust", "グロウストーンダスト", Material.GLOWSTONE_DUST),
    MineStackObjectByMaterial(BUILDING, "glowstone", "グロウストーン", Material.GLOWSTONE),
    MineStackObjectByMaterial(BUILDING, "torch", "松明", Material.TORCH),
    MineStackObjectByMaterial(BUILDING, "jack_o_lantern", "ジャック・オ・ランタン", Material.JACK_O_LANTERN),
    MineStackObjectByMaterial(BUILDING, "end_rod", "エンドロッド", Material.END_ROD),
    MineStackObjectByMaterial(BUILDING, "bucket", "バケツ", Material.BUCKET),
    MineStackObjectByMaterial(BUILDING, "water_bucket", "水入りバケツ", Material.WATER_BUCKET),
    MineStackObjectByMaterial(BUILDING, "lava_bucket", "溶岩入りバケツ", Material.LAVA_BUCKET),
    MineStackObjectByMaterial(BUILDING, "web", "クモの巣", Material.COBWEB),
    MineStackObjectByMaterial(BUILDING, "rails", "レール", Material.RAIL),
    MineStackObjectByMaterial(BUILDING, "furnace", "かまど", Material.FURNACE),
    MineStackObjectByMaterial(BUILDING, "chest", "チェスト", Material.CHEST),
    MineStackObjectByMaterial(BUILDING, "book", "本", Material.BOOK),
    MineStackObjectByMaterial(BUILDING, "bookshelf", "本棚", Material.BOOKSHELF),
    MineStackObjectByMaterial(BUILDING, "iron_bars", "鉄格子", Material.IRON_BARS),
    MineStackObjectByMaterial(BUILDING, "anvil", "金床", Material.ANVIL),
    MineStackObjectByMaterial(BUILDING, "cauldron", "大釜", Material.CAULDRON),
    MineStackObjectByMaterial(BUILDING, "brewing_stand", "醸造台", Material.BREWING_STAND),
    MineStackObjectByMaterial(BUILDING, "flower_pot", "植木鉢", Material.FLOWER_POT),
    MineStackObjectByMaterial(BUILDING, "hay_block", "干し草の俵", Material.HAY_BLOCK),
    MineStackObjectByMaterial(BUILDING, "ladder", "はしご", Material.LADDER),
    MineStackObjectByMaterial(BUILDING, "item_frame", "額縁", Material.ITEM_FRAME),
    MineStackObjectByMaterial(BUILDING, "painting", "絵画", Material.PAINTING),
    MineStackObjectByMaterial(BUILDING, "beacon", "ビーコン", Material.BEACON),
    MineStackObjectByMaterial(BUILDING, "armor_stand", "アーマースタンド", Material.ARMOR_STAND),
    MineStackObjectByMaterial(BUILDING, "end_crystal", "エンドクリスタル", Material.END_CRYSTAL),
    MineStackObjectByMaterial(BUILDING, "enchanting_table", "エンチャントテーブル", Material.ENCHANTING_TABLE),
    MineStackObjectByMaterial(BUILDING, "jukebox", "ジュークボックス", Material.JUKEBOX),
    MineStackObjectByMaterial(BUILDING, "workbench", "作業台", Material.CRAFTING_TABLE),
    MineStackObjectByMaterial(BUILDING, "deepslate", "深層岩", Material.DEEPSLATE),
    MineStackObjectByMaterial(BUILDING, "cobbled_deepslate", "深層岩の丸石", Material.COBBLED_DEEPSLATE),
    MineStackObjectByMaterial(BUILDING, "polished_deepslate", "磨かれた深層岩", Material.POLISHED_DEEPSLATE),
    MineStackObjectByMaterial(BUILDING, "calcite", "方解石", Material.CALCITE),
    MineStackObjectByMaterial(BUILDING, "tuff", "凝灰岩", Material.TUFF),
    MineStackObjectByMaterial(BUILDING, "dripstone_block", "鍾乳石ブロック", Material.DRIPSTONE_BLOCK),
    MineStackObjectByMaterial(BUILDING, "grass_block", "草ブロック", Material.GRASS_BLOCK),
    MineStackObjectByMaterial(BUILDING, "rooted_dirt", "根付いた土", Material.ROOTED_DIRT),
    MineStackObjectByMaterial(BUILDING, "crimson_nylium", "真紅のナイリウム", Material.CRIMSON_NYLIUM),
    MineStackObjectByMaterial(BUILDING, "warped_nylium", "歪んだナイリウム", Material.WARPED_NYLIUM),
    MineStackObjectByMaterial(BUILDING, "crimson_stem", "真紅の幹", Material.CRIMSON_STEM),
    MineStackObjectByMaterial(BUILDING, "warped_stem", "歪んだ幹", Material.WARPED_STEM),
    MineStackObjectByMaterial(BUILDING, "stripped_crimson_stem", "表皮を剥いだ真紅の幹", Material.STRIPPED_CRIMSON_STEM),
    MineStackObjectByMaterial(BUILDING, "stripped_warped_stem", "表皮を剥いだ歪んだ幹", Material.STRIPPED_WARPED_STEM),
    MineStackObjectByMaterial(BUILDING, "stripped_crimson_hyphae", "表皮を剥いだ真紅の菌糸", Material.STRIPPED_CRIMSON_HYPHAE),
    MineStackObjectByMaterial(BUILDING, "stripped_warped_hyphae", "表皮を剥いだ歪んだ菌糸", Material.STRIPPED_WARPED_HYPHAE),
    MineStackObjectByMaterial(BUILDING, "tinted_glass", "遮光ガラス", Material.TINTED_GLASS),
    MineStackObjectByMaterial(BUILDING, "cut_sandstone", "研がれた砂岩", Material.CUT_SANDSTONE),
    MineStackObjectByMaterial(BUILDING, "azalea", "ツツジ", Material.AZALEA),
    MineStackObjectByMaterial(BUILDING, "flowering_azalea", "開花したツツジ", Material.FLOWERING_AZALEA),
    MineStackObjectByMaterial(BUILDING, "seagrass", "海草", Material.SEAGRASS),
    MineStackObjectByMaterial(BUILDING, "sea_pickle", "シーピクルス", Material.SEA_PICKLE),
    MineStackObjectByMaterial(BUILDING, "cornflower", "ヤグルマギク", Material.CORNFLOWER),
    MineStackObjectByMaterial(BUILDING, "lily_of_the_valley", "スズラン", Material.LILY_OF_THE_VALLEY),
    MineStackObjectByMaterial(BUILDING, "wither_rose", "ウィザーローズ", Material.WITHER_ROSE),
    MineStackObjectByMaterial(BUILDING, "spore_blossom", "胞子の花", Material.SPORE_BLOSSOM),
    MineStackObjectByMaterial(BUILDING, "crimson_fungus", "真紅のキノコ", Material.CRIMSON_FUNGUS),
    MineStackObjectByMaterial(BUILDING, "warped_fungus", "歪んだキノコ", Material.WARPED_FUNGUS),
    MineStackObjectByMaterial(BUILDING, "crimson_roots", "真紅の根", Material.CRIMSON_ROOTS),
    MineStackObjectByMaterial(BUILDING, "warped_roots", "歪んだ根", Material.WARPED_ROOTS),
    MineStackObjectByMaterial(BUILDING, "nether_sprouts", "ネザースプラウト", Material.NETHER_SPROUTS),
    MineStackObjectByMaterial(BUILDING, "weeping_vines", "しだれツタ", Material.WEEPING_VINES),
    MineStackObjectByMaterial(BUILDING, "twisting_vines", "ねじれツタ", Material.TWISTING_VINES),
    MineStackObjectByMaterial(BUILDING, "moss_carpet", "苔のカーペット", Material.MOSS_CARPET),
    MineStackObjectByMaterial(BUILDING, "moss_block", "苔ブロック", Material.MOSS_BLOCK),
    MineStackObjectByMaterial(BUILDING, "hanging_roots", "垂れ根", Material.HANGING_ROOTS),
    MineStackObjectByMaterial(BUILDING, "big_dripleaf", "大きなドリップリーフ", Material.BIG_DRIPLEAF),
    MineStackObjectByMaterial(BUILDING, "small_dripleaf", "小さなドリップリーフ", Material.SMALL_DRIPLEAF),
    MineStackObjectByMaterial(BUILDING, "bamboo", "竹", Material.BAMBOO),
    MineStackObjectByMaterial(BUILDING, "smooth_quartz", "滑らかなクォーツブロック", Material.SMOOTH_QUARTZ),
    MineStackObjectByMaterial(BUILDING, "smooth_stone", "滑らかな石", Material.SMOOTH_STONE),
    MineStackObjectByMaterial(BUILDING, "chorus_plant", "コーラスプラント", Material.CHORUS_PLANT),
    MineStackObjectByMaterial(BUILDING, "soul_soil", "ソウルソイル", Material.SOUL_SOIL),
    MineStackObjectByMaterial(BUILDING, "basalt", "玄武岩", Material.BASALT),
    MineStackObjectByMaterial(BUILDING, "polished_basalt", "磨かれた玄武岩", Material.POLISHED_BASALT),
    MineStackObjectByMaterial(BUILDING, "smooth_basalt", "滑らかな玄武岩", Material.SMOOTH_BASALT),
    MineStackObjectByMaterial(BUILDING, "soul_torch", "魂の松明", Material.SOUL_TORCH),
    MineStackObjectByMaterial(BUILDING, "infested_stone", "虫食い石", Material.INFESTED_STONE),
    MineStackObjectByMaterial(BUILDING, "infested_cobblestone", "虫食い丸石", Material.INFESTED_COBBLESTONE),
    MineStackObjectByMaterial(BUILDING, "infested_stone_bricks", "虫食い石レンガ", Material.INFESTED_STONE_BRICKS),
    MineStackObjectByMaterial(BUILDING, "infested_mossy_stone_bricks", "苔むした虫食い石レンガ", Material.INFESTED_MOSSY_STONE_BRICKS),
    MineStackObjectByMaterial(BUILDING, "infested_cracked_stone_bricks", "ひび割れた虫食い石レンガ", Material.INFESTED_CRACKED_STONE_BRICKS),
    MineStackObjectByMaterial(BUILDING, "infested_chiseled_stone_bricks", "模様入りの虫食い石レンガ", Material.INFESTED_CHISELED_STONE_BRICKS),
    MineStackObjectByMaterial(BUILDING, "infested_deepslate", "虫食い深層岩", Material.INFESTED_DEEPSLATE),
    MineStackObjectByMaterial(BUILDING, "deepslate_bricks", "深層岩レンガ", Material.DEEPSLATE_BRICKS),
    MineStackObjectByMaterial(BUILDING, "cracked_deepslate_bricks", "ひび割れた深層岩レンガ", Material.CRACKED_DEEPSLATE_BRICKS),
    MineStackObjectByMaterial(BUILDING, "deepslate_tiles", "深層岩タイル", Material.DEEPSLATE_TILES),
    MineStackObjectByMaterial(BUILDING, "cracked_deepslate_tiles", "ひび割れた深層岩タイル", Material.CRACKED_DEEPSLATE_TILES),
    MineStackObjectByMaterial(BUILDING, "chiseled_deepslate", "模様入りの深層岩", Material.CHISELED_DEEPSLATE),
    MineStackObjectByMaterial(BUILDING, "brown_mushroom_block", "茶色のキノコブロック", Material.BROWN_MUSHROOM_BLOCK),
    MineStackObjectByMaterial(BUILDING, "red_mushroom_block", "赤色のキノコブロック", Material.RED_MUSHROOM_BLOCK),
    MineStackObjectByMaterial(BUILDING, "mushroom_stem", "キノコの柄", Material.MUSHROOM_STEM),
    MineStackObjectByMaterial(BUILDING, "chain", "鎖", Material.CHAIN),
    MineStackObjectByMaterial(BUILDING, "glow_lichen", "ヒカリゴケ", Material.GLOW_LICHEN),
    MineStackObjectByMaterial(BUILDING, "cracked_nether_bricks", "ひび割れたネザーレンガ", Material.CRACKED_NETHER_BRICKS),
    MineStackObjectByMaterial(BUILDING, "chiseled_nether_bricks", "模様入りのネザーレンガ", Material.CHISELED_NETHER_BRICKS),
    MineStackObjectByMaterial(BUILDING, "warped_stairs", "歪んだ階段", Material.WARPED_STAIRS),
    MineStackObjectByMaterial(BUILDING, "quartz_bricks", "クォーツレンガ", Material.QUARTZ_BRICKS),
    MineStackObjectByMaterial(BUILDING, "cut_red_sandstone", "研がれた赤い砂岩", Material.CUT_RED_SANDSTONE),
    MineStackObjectByMaterial(BUILDING, "turtle_egg", "カメの卵", Material.TURTLE_EGG),
    MineStackObjectByMaterial(BUILDING, "blue_ice", "青氷", Material.BLUE_ICE),
    MineStackObjectByMaterial(BUILDING, "conduit", "コンジット", Material.CONDUIT),
    MineStackObjectByMaterial(BUILDING, "scaffolding", "足場", Material.SCAFFOLDING),
    MineStackObjectByMaterial(BUILDING, "honey_block", "ハチミツブロック", Material.HONEY_BLOCK),
    MineStackObjectByMaterial(BUILDING, "loom", "機織り機", Material.LOOM),
    MineStackObjectByMaterial(BUILDING, "composter", "コンポスター", Material.COMPOSTER),
    MineStackObjectByMaterial(BUILDING, "barrel", "樽", Material.BARREL),
    MineStackObjectByMaterial(BUILDING, "smoker", "燻製器", Material.SMOKER),
    MineStackObjectByMaterial(BUILDING, "blast_furnace", "溶鉱炉", Material.BLAST_FURNACE),
    MineStackObjectByMaterial(BUILDING, "cartography_table", "製図台", Material.CARTOGRAPHY_TABLE),
    MineStackObjectByMaterial(BUILDING, "fletching_table", "矢細工台", Material.FLETCHING_TABLE),
    MineStackObjectByMaterial(BUILDING, "grindstone", "砥石", Material.GRINDSTONE),
    MineStackObjectByMaterial(BUILDING, "smithing_table", "鍛冶台", Material.SMITHING_TABLE),
    MineStackObjectByMaterial(BUILDING, "stonecutter", "石切台", Material.STONECUTTER),
    MineStackObjectByMaterial(BUILDING, "bell", "鐘", Material.BELL),
    MineStackObjectByMaterial(BUILDING, "lantern", "ランタン", Material.LANTERN),
    MineStackObjectByMaterial(BUILDING, "soul_lantern", "魂のランタン", Material.SOUL_LANTERN),
    MineStackObjectByMaterial(BUILDING, "sweet_berries", "スイートベリー", Material.SWEET_BERRIES),
    MineStackObjectByMaterial(BUILDING, "glow_berries", "グロウベリー", Material.GLOW_BERRIES),
    MineStackObjectByMaterial(BUILDING, "campfire", "焚き火", Material.CAMPFIRE),
    MineStackObjectByMaterial(BUILDING, "soul_campfire", "魂の焚き火", Material.SOUL_CAMPFIRE),
    MineStackObjectByMaterial(BUILDING, "shroomlight", "シュルームライト", Material.SHROOMLIGHT),
    MineStackObjectByMaterial(BUILDING, "honeycomb", "ハニカム", Material.HONEYCOMB),
    MineStackObjectByMaterial(BUILDING, "bee_nest", "ミツバチの巣", Material.BEE_NEST),
    MineStackObjectByMaterial(BUILDING, "beehive", "養蜂箱", Material.BEEHIVE),
    MineStackObjectByMaterial(BUILDING, "honey_bottle", "ハチミツ入りの瓶", Material.HONEY_BOTTLE),
    MineStackObjectByMaterial(BUILDING, "honeycomb_block", "ハニカムブロック", Material.HONEYCOMB_BLOCK),
    MineStackObjectByMaterial(BUILDING, "lodestone", "ロードストーン", Material.LODESTONE),
    MineStackObjectByMaterial(BUILDING, "crying_obsidian", "泣く黒曜石", Material.CRYING_OBSIDIAN),
    MineStackObjectByMaterial(BUILDING, "blackstone", "ブラックストーン", Material.BLACKSTONE),
    MineStackObjectByMaterial(BUILDING, "gilded_blackstone", "きらめくブラックストーン", Material.GILDED_BLACKSTONE),
    MineStackObjectByMaterial(BUILDING, "polished_blackstone", "磨かれたブラックストーン", Material.POLISHED_BLACKSTONE),
    MineStackObjectByMaterial(BUILDING, "chiseled_polished_blackstone", "模様入りの磨かれたブラックストーン", Material.CHISELED_POLISHED_BLACKSTONE),
    MineStackObjectByMaterial(BUILDING, "polished_blackstone_bricks", "磨かれたブラックストーンレンガ", Material.POLISHED_BLACKSTONE_BRICKS),
    MineStackObjectByMaterial(BUILDING, "cracked_polished_blackstone_bricks", "ひび割れたブラックストーンレンガ", Material.CRACKED_POLISHED_BLACKSTONE_BRICKS),
    MineStackObjectByMaterial(BUILDING, "respawn_anchor", "リスポーンアンカー", Material.RESPAWN_ANCHOR),
    MineStackObjectByMaterial(BUILDING, "pointed_dripstone", "鍾乳石", Material.POINTED_DRIPSTONE),
    MineStackObjectByMaterial(BUILDING, "warped_fungus_on_a_stick", "歪んだキノコ付きの棒", Material.WARPED_FUNGUS_ON_A_STICK),
    MineStackObjectByMaterial(BUILDING, "jigsaw", "ジグソーブロック", Material.JIGSAW),
    MineStackObjectByMaterial(BUILDING, "powder_snow_bucket", "粉雪入りバケツ", Material.POWDER_SNOW_BUCKET),
    MineStackObjectByMaterial(BUILDING, "glow_ink_sac", "輝くイカスミ", Material.GLOW_INK_SAC),
    MineStackObjectByMaterial(BUILDING, "filled_map", "地図", Material.FILLED_MAP),
    MineStackObjectByMaterial(BUILDING, "fire_charge", "ファイヤーチャージ", Material.FIRE_CHARGE),
    MineStackObjectByMaterial(BUILDING, "writable_book", "本と羽根ペン", Material.WRITABLE_BOOK),
    MineStackObjectByMaterial(BUILDING, "written_book", "記入済みの本", Material.WRITTEN_BOOK),
    MineStackObjectByMaterial(BUILDING, "glow_item_frame", "輝く額縁", Material.GLOW_ITEM_FRAME),
    MineStackObjectByMaterial(BUILDING, "pumpkin_pie", "パンプキンパイ", Material.PUMPKIN_PIE),
    MineStackObjectByMaterial(BUILDING, "firework_star", "花火の星", Material.FIREWORK_STAR),
    MineStackObjectByMaterial(BUILDING, "leather_horse_armor", "革の馬鎧", Material.LEATHER_HORSE_ARMOR),
    MineStackObjectByMaterial(BUILDING, "knowledge_book", "知恵の本", Material.KNOWLEDGE_BOOK),
  ) ++ rightElems(
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(BUILDING, "log", "オークの原木", Material.OAK_LOG),
      List(
        MineStackObjectByMaterial(BUILDING, "log1", "マツの原木", Material.SPRUCE_LOG),
        MineStackObjectByMaterial(BUILDING, "log2", "シラカバの原木", Material.BIRCH_LOG),
        MineStackObjectByMaterial(BUILDING, "log3", "ジャングルの原木", Material.JUNGLE_LOG),
        MineStackObjectByMaterial(BUILDING, "log_2", "アカシアの原木", Material.ACACIA_LOG),
        MineStackObjectByMaterial(BUILDING, "log_21", "ダークオークの原木", Material.DARK_OAK_LOG),
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(BUILDING, "stripped_oak_log", "樹皮を剥いだオークの原木", Material.STRIPPED_OAK_LOG),
      List(
        MineStackObjectByMaterial(BUILDING, "stripped_spruce_log", "樹皮を剥いだトウヒの原木", Material.STRIPPED_SPRUCE_LOG),
        MineStackObjectByMaterial(BUILDING, "stripped_birch_log", "樹皮を剥いだシラカバの原木", Material.STRIPPED_BIRCH_LOG),
        MineStackObjectByMaterial(BUILDING, "stripped_jungle_log", "樹皮を剥いだジャングルの原木", Material.STRIPPED_JUNGLE_LOG),
        MineStackObjectByMaterial(BUILDING, "stripped_acacia_log", "樹皮を剥いだアカシアの原木", Material.STRIPPED_ACACIA_LOG),
        MineStackObjectByMaterial(BUILDING, "stripped_dark_oak_log", "樹皮を剥いだダークオークの原木", Material.STRIPPED_DARK_OAK_LOG),
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(BUILDING, "stripped_oak_wood", "樹皮を剥いだオークの木", Material.STRIPPED_OAK_WOOD),
      List(
        MineStackObjectByMaterial(BUILDING, "stripped_spruce_wood", "樹皮を剥いだトウヒの木", Material.STRIPPED_SPRUCE_WOOD),
        MineStackObjectByMaterial(BUILDING, "stripped_birch_wood", "樹皮を剥いだシラカバの木", Material.STRIPPED_BIRCH_WOOD),
        MineStackObjectByMaterial(BUILDING, "stripped_jungle_wood", "樹皮を剥いだジャングルの木", Material.STRIPPED_JUNGLE_WOOD),
        MineStackObjectByMaterial(BUILDING, "stripped_acacia_wood", "樹皮を剥いだアカシアの木", Material.STRIPPED_ACACIA_WOOD),
        MineStackObjectByMaterial(BUILDING, "stripped_dark_oak_wood", "樹皮を剥いだダークオークの木", Material.STRIPPED_DARK_OAK_WOOD),
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(BUILDING, "wood", "オークの木材", Material.OAK_WOOD),
      List(
        MineStackObjectByMaterial(BUILDING, "wood_1", "マツの木材", Material.SPRUCE_WOOD),
        MineStackObjectByMaterial(BUILDING, "wood_2", "シラカバの木材", Material.BIRCH_WOOD),
        MineStackObjectByMaterial(BUILDING, "wood_3", "ジャングルの木材", Material.JUNGLE_WOOD),
        MineStackObjectByMaterial(BUILDING, "wood_4", "アカシアの木材", Material.ACACIA_WOOD),
        MineStackObjectByMaterial(BUILDING, "wood_5", "ダークオークの木材", Material.DARK_OAK_WOOD),
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(BUILDING, "oak_planks", "オークの板材", Material.OAK_PLANKS),
      List(
        MineStackObjectByMaterial(BUILDING, "spruce_planks", "トウヒの板材", Material.SPRUCE_PLANKS),
        MineStackObjectByMaterial(BUILDING, "birch_planks", "シラカバの板材", Material.BIRCH_PLANKS),
        MineStackObjectByMaterial(BUILDING, "jungle_planks", "ジャングルの板材", Material.JUNGLE_PLANKS),
        MineStackObjectByMaterial(BUILDING, "acacia_planks", "アカシアの板材", Material.ACACIA_PLANKS),
        MineStackObjectByMaterial(BUILDING, "dark_oak_planks", "ダークオークの板材", Material.DARK_OAK_PLANKS),
        MineStackObjectByMaterial(BUILDING, "crimson_planks", "真紅の板材", Material.CRIMSON_PLANKS),
        MineStackObjectByMaterial(BUILDING, "warped_planks", "歪んだ板材", Material.WARPED_PLANKS),
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(BUILDING, "wood_step0", "オークの木材ハーフブロック", Material.OAK_SLAB),
      List(
        MineStackObjectByMaterial(BUILDING, "wood_step1", "マツの木材ハーフブロック", Material.SPRUCE_SLAB),
        MineStackObjectByMaterial(BUILDING, "wood_step2", "シラカバの木材ハーフブロック", Material.BIRCH_SLAB),
        MineStackObjectByMaterial(BUILDING, "wood_step3", "ジャングルの木材ハーフブロック", Material.JUNGLE_SLAB),
        MineStackObjectByMaterial(BUILDING, "wood_step4", "アカシアの木材ハーフブロック", Material.ACACIA_SLAB),
        MineStackObjectByMaterial(BUILDING, "wood_step5", "ダークオークの木材ハーフブロック", Material.DARK_OAK_SLAB),
        MineStackObjectByMaterial(BUILDING, "step3", "丸石ハーフブロック", Material.COBBLESTONE_SLAB),
        MineStackObjectByMaterial(BUILDING, "step0", "石ハーフブロック", Material.STONE_SLAB),
        MineStackObjectByMaterial(BUILDING, "step5", "石レンガハーフブロック", Material.STONE_BRICK_SLAB),
        MineStackObjectByMaterial(BUILDING, "step1", "砂岩ハーフブロック", Material.SANDSTONE_SLAB),
        MineStackObjectByMaterial(BUILDING, "stone_slab20", "赤い砂岩ハーフブロック", Material.RED_SANDSTONE_SLAB),
        MineStackObjectByMaterial(BUILDING, "step4", "レンガハーフブロック", Material.BRICK_SLAB),
        MineStackObjectByMaterial(BUILDING, "step7", "ネザー水晶ハーフブロック", Material.QUARTZ_SLAB),
        MineStackObjectByMaterial(BUILDING, "step6", "ネザーレンガハーフブロック", Material.NETHER_BRICK_SLAB),
        MineStackObjectByMaterial(BUILDING, "purpur_slab", "プルプァハーフブロック", Material.PURPUR_SLAB),
        MineStackObjectByMaterial(BUILDING, "waxed_cut_copper_slab", "錆止めされた切り込み入りの銅のハーフブロック", Material.WAXED_CUT_COPPER_SLAB),
        MineStackObjectByMaterial(BUILDING, "waxed_exposed_cut_copper_slab", "錆止めされた風化した切り込み入りの銅のハーフブロック", Material.WAXED_EXPOSED_CUT_COPPER_SLAB),
        MineStackObjectByMaterial(BUILDING, "waxed_weathered_cut_copper_slab", "錆止めされた錆びた切り込み入りの銅のハーフブロック", Material.WAXED_WEATHERED_CUT_COPPER_SLAB),
        MineStackObjectByMaterial(BUILDING, "waxed_oxidized_cut_copper_slab", "錆止めされた酸化した切り込み入りの銅のハーフブロック", Material.WAXED_OXIDIZED_CUT_COPPER_SLAB),
        MineStackObjectByMaterial(BUILDING, "crimson_slab", "真紅のハーフブロック", Material.CRIMSON_SLAB),
        MineStackObjectByMaterial(BUILDING, "warped_slab", "歪んだハーフブロック", Material.WARPED_SLAB),
        MineStackObjectByMaterial(BUILDING, "smooth_stone_slab", "滑らかな石のハーフブロック", Material.SMOOTH_STONE_SLAB),
        MineStackObjectByMaterial(BUILDING, "cut_sandstone_slab", "研がれた砂岩のハーフブロック", Material.CUT_SANDSTONE_SLAB),
        MineStackObjectByMaterial(BUILDING, "petrified_oak_slab", "石化したオークのハーフブロック", Material.PETRIFIED_OAK_SLAB),
        MineStackObjectByMaterial(BUILDING, "cut_red_sandstone_slab", "研がれた赤い砂岩のハーフブロック", Material.CUT_RED_SANDSTONE_SLAB),
        MineStackObjectByMaterial(BUILDING, "prismarine_slab", "プリズマリンのハーフブロック", Material.PRISMARINE_SLAB),
        MineStackObjectByMaterial(BUILDING, "prismarine_brick_slab", "プリズマリンレンガのハーフブロック", Material.PRISMARINE_BRICK_SLAB),
        MineStackObjectByMaterial(BUILDING, "dark_prismarine_slab", "ダークプリズマリンのハーフブロック", Material.DARK_PRISMARINE_SLAB),
        MineStackObjectByMaterial(BUILDING, "polished_granite_slab", "磨かれた花崗岩のハーフブロック", Material.POLISHED_GRANITE_SLAB),
        MineStackObjectByMaterial(BUILDING, "smooth_red_sandstone_slab", "滑らかな赤い砂岩のハーフブロック", Material.SMOOTH_RED_SANDSTONE_SLAB),
        MineStackObjectByMaterial(BUILDING, "mossy_stone_brick_slab", "苔むした石レンガのハーフブロック", Material.MOSSY_STONE_BRICK_SLAB),
        MineStackObjectByMaterial(BUILDING, "polished_diorite_slab", "磨かれた閃緑岩のハーフブロック", Material.POLISHED_DIORITE_SLAB),
        MineStackObjectByMaterial(BUILDING, "mossy_cobblestone_slab", "苔むした丸石のハーフブロック", Material.MOSSY_COBBLESTONE_SLAB),
        MineStackObjectByMaterial(BUILDING, "end_stone_brick_slab", "エンドストーンレンガのハーフブロック", Material.END_STONE_BRICK_SLAB),
        MineStackObjectByMaterial(BUILDING, "smooth_sandstone_slab", "滑らかな砂岩のハーフブロック", Material.SMOOTH_SANDSTONE_SLAB),
        MineStackObjectByMaterial(BUILDING, "smooth_quartz_slab", "滑らかなクォーツのハーフブロック", Material.SMOOTH_QUARTZ_SLAB),
        MineStackObjectByMaterial(BUILDING, "granite_slab", "花崗岩のハーフブロック", Material.GRANITE_SLAB),
        MineStackObjectByMaterial(BUILDING, "andesite_slab", "安山岩のハーフブロック", Material.ANDESITE_SLAB),
        MineStackObjectByMaterial(BUILDING, "red_nether_brick_slab", "赤いネザーレンガのハーフブロック", Material.RED_NETHER_BRICK_SLAB),
        MineStackObjectByMaterial(BUILDING, "polished_andesite_slab", "磨かれた安山岩のハーフブロック", Material.POLISHED_ANDESITE_SLAB),
        MineStackObjectByMaterial(BUILDING, "diorite_slab", "閃緑岩のハーフブロック", Material.DIORITE_SLAB),
        MineStackObjectByMaterial(BUILDING, "cobbled_deepslate_slab", "深層岩の丸石のハーフブロック", Material.COBBLED_DEEPSLATE_SLAB),
        MineStackObjectByMaterial(BUILDING, "polished_deepslate_slab", "磨かれた深層岩のハーフブロック", Material.POLISHED_DEEPSLATE_SLAB),
        MineStackObjectByMaterial(BUILDING, "deepslate_brick_slab", "深層岩レンガのハーフブロック", Material.DEEPSLATE_BRICK_SLAB),
        MineStackObjectByMaterial(BUILDING, "deepslate_tile_slab", "深層岩タイルのハーフブロック", Material.DEEPSLATE_TILE_SLAB),
        MineStackObjectByMaterial(BUILDING, "blackstone_slab", "ブラックストーンのハーフブロック", Material.BLACKSTONE_SLAB),
        MineStackObjectByMaterial(BUILDING, "polished_blackstone_slab", "磨かれたブラックストーンのハーフブロック", Material.POLISHED_BLACKSTONE_SLAB),
        MineStackObjectByMaterial(BUILDING, "polished_blackstone_brick_slab", "磨かれたブラックストーンレンガのハーフブロック", Material.POLISHED_BLACKSTONE_BRICK_SLAB),
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(BUILDING, "oak_stairs", "オークの木の階段", Material.OAK_STAIRS),
      List(
        MineStackObjectByMaterial(BUILDING, "spruce_stairs", "マツの木の階段", Material.SPRUCE_STAIRS),
        MineStackObjectByMaterial(BUILDING, "birch_stairs", "シラカバの木の階段", Material.BIRCH_STAIRS),
        MineStackObjectByMaterial(BUILDING, "jungle_stairs", "ジャングルの木の階段", Material.JUNGLE_STAIRS),
        MineStackObjectByMaterial(BUILDING, "acacia_stairs", "アカシアの木の階段", Material.ACACIA_STAIRS),
        MineStackObjectByMaterial(BUILDING, "dark_oak_stairs", "ダークオークの木の階段", Material.DARK_OAK_STAIRS),
        MineStackObjectByMaterial(BUILDING, "stone_stairs", "丸石の階段", Material.COBBLESTONE_STAIRS),
        MineStackObjectByMaterial(BUILDING, "smooth_stairs", "石レンガの階段", Material.STONE_BRICK_STAIRS),
        MineStackObjectByMaterial(BUILDING, "standstone_stairs", "砂岩の階段", Material.SANDSTONE_STAIRS),
        MineStackObjectByMaterial(BUILDING, "red_sandstone_stairs", "赤い砂岩の階段", Material.RED_SANDSTONE_STAIRS),
        MineStackObjectByMaterial(BUILDING, "brick_stairs", "レンガの階段", Material.BRICK_STAIRS),
        MineStackObjectByMaterial(BUILDING, "quartz_stairs", "ネザー水晶の階段", Material.QUARTZ_STAIRS),
        MineStackObjectByMaterial(BUILDING, "nether_brick_stairs", "ネザーレンガの階段", Material.NETHER_BRICK_STAIRS),
        MineStackObjectByMaterial(BUILDING, "purpur_stairs", "プルプァの階段", Material.PURPUR_STAIRS),
        MineStackObjectByMaterial(BUILDING, "waxed_cut_copper_stairs", "錆止めされた切り込み入りの銅の階段", Material.WAXED_CUT_COPPER_STAIRS),
        MineStackObjectByMaterial(BUILDING, "waxed_exposed_cut_copper_stairs", "錆止めされた風化した切り込み入りの銅の階段", Material.WAXED_EXPOSED_CUT_COPPER_STAIRS),
        MineStackObjectByMaterial(BUILDING, "waxed_weathered_cut_copper_stairs", "錆止めされた錆びた切り込み入りの銅の階段", Material.WAXED_WEATHERED_CUT_COPPER_STAIRS),
        MineStackObjectByMaterial(BUILDING, "waxed_oxidized_cut_copper_stairs", "錆止めされた酸化した切り込み入りの銅の階段", Material.WAXED_OXIDIZED_CUT_COPPER_STAIRS),
        MineStackObjectByMaterial(BUILDING, "crimson_stairs", "真紅の階段", Material.CRIMSON_STAIRS),
        MineStackObjectByMaterial(BUILDING, "prismarine_stairs", "プリズマリンの階段", Material.PRISMARINE_STAIRS),
        MineStackObjectByMaterial(BUILDING, "prismarine_brick_stairs", "プリズマリンレンガの階段", Material.PRISMARINE_BRICK_STAIRS),
        MineStackObjectByMaterial(BUILDING, "dark_prismarine_stairs", "ダークプリズマリンの階段", Material.DARK_PRISMARINE_STAIRS),
        MineStackObjectByMaterial(BUILDING, "polished_granite_stairs", "磨かれた花崗岩の階段", Material.POLISHED_GRANITE_STAIRS),
        MineStackObjectByMaterial(BUILDING, "smooth_red_sandstone_stairs", "滑らかな赤い砂岩の階段", Material.SMOOTH_RED_SANDSTONE_STAIRS),
        MineStackObjectByMaterial(BUILDING, "mossy_stone_brick_stairs", "苔むした石レンガの階段", Material.MOSSY_STONE_BRICK_STAIRS),
        MineStackObjectByMaterial(BUILDING, "polished_diorite_stairs", "磨かれた閃緑岩の階段", Material.POLISHED_DIORITE_STAIRS),
        MineStackObjectByMaterial(BUILDING, "mossy_cobblestone_stairs", "苔むした丸石の階段", Material.MOSSY_COBBLESTONE_STAIRS),
        MineStackObjectByMaterial(BUILDING, "end_stone_brick_stairs", "エンドストーンレンガの階段", Material.END_STONE_BRICK_STAIRS),
        MineStackObjectByMaterial(BUILDING, "stone_stairs", "石の階段", Material.STONE_STAIRS),
        MineStackObjectByMaterial(BUILDING, "smooth_sandstone_stairs", "滑らかな砂岩の階段", Material.SMOOTH_SANDSTONE_STAIRS),
        MineStackObjectByMaterial(BUILDING, "smooth_quartz_stairs", "滑らかなクォーツの階段", Material.SMOOTH_QUARTZ_STAIRS),
        MineStackObjectByMaterial(BUILDING, "granite_stairs", "花崗岩の階段", Material.GRANITE_STAIRS),
        MineStackObjectByMaterial(BUILDING, "andesite_stairs", "安山岩の階段", Material.ANDESITE_STAIRS),
        MineStackObjectByMaterial(BUILDING, "red_nether_brick_stairs", "赤いネザーレンガの階段", Material.RED_NETHER_BRICK_STAIRS),
        MineStackObjectByMaterial(BUILDING, "polished_andesite_stairs", "磨かれた安山岩の階段", Material.POLISHED_ANDESITE_STAIRS),
        MineStackObjectByMaterial(BUILDING, "diorite_stairs", "閃緑岩の階段", Material.DIORITE_STAIRS),
        MineStackObjectByMaterial(BUILDING, "cobbled_deepslate_stairs", "深層岩の丸石の階段", Material.COBBLED_DEEPSLATE_STAIRS),
        MineStackObjectByMaterial(BUILDING, "polished_deepslate_stairs", "磨かれた深層岩の階段", Material.POLISHED_DEEPSLATE_STAIRS),
        MineStackObjectByMaterial(BUILDING, "deepslate_brick_stairs", "深層岩レンガの階段", Material.DEEPSLATE_BRICK_STAIRS),
        MineStackObjectByMaterial(BUILDING, "deepslate_tile_stairs", "深層岩タイルの階段", Material.DEEPSLATE_TILE_STAIRS),
        MineStackObjectByMaterial(BUILDING, "blackstone_stairs", "ブラックストーンの階段", Material.BLACKSTONE_STAIRS),
        MineStackObjectByMaterial(BUILDING, "polished_blackstone_stairs", "磨かれたブラックストーンの階段", Material.POLISHED_BLACKSTONE_STAIRS),
        MineStackObjectByMaterial(BUILDING, "polished_blackstone_brick_stairs", "磨かれたブラックストーンレンガの階段", Material.POLISHED_BLACKSTONE_BRICK_STAIRS),
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(BUILDING, "brick_wall", "レンガの塀", Material.BRICK_WALL),
      List(
        MineStackObjectByMaterial(BUILDING, "prismarine_wall", "プリズマリンの塀", Material.PRISMARINE_WALL),
        MineStackObjectByMaterial(BUILDING, "red_sandstone_wall", "赤い砂岩の塀", Material.RED_SANDSTONE_WALL),
        MineStackObjectByMaterial(BUILDING, "mossy_stone_brick_wall", "苔むした石レンガの塀", Material.MOSSY_STONE_BRICK_WALL),
        MineStackObjectByMaterial(BUILDING, "granite_wall", "花崗岩の塀", Material.GRANITE_WALL),
        MineStackObjectByMaterial(BUILDING, "stone_brick_wall", "石レンガの塀", Material.STONE_BRICK_WALL),
        MineStackObjectByMaterial(BUILDING, "nether_brick_wall", "ネザーレンガの塀", Material.NETHER_BRICK_WALL),
        MineStackObjectByMaterial(BUILDING, "andesite_wall", "安山岩の塀", Material.ANDESITE_WALL),
        MineStackObjectByMaterial(BUILDING, "red_nether_brick_wall", "赤いネザーレンガの塀", Material.RED_NETHER_BRICK_WALL),
        MineStackObjectByMaterial(BUILDING, "sandstone_wall", "砂岩の塀", Material.SANDSTONE_WALL),
        MineStackObjectByMaterial(BUILDING, "end_stone_brick_wall", "エンドストーンレンガの塀", Material.END_STONE_BRICK_WALL),
        MineStackObjectByMaterial(BUILDING, "diorite_wall", "閃緑岩の塀", Material.DIORITE_WALL),
        MineStackObjectByMaterial(BUILDING, "blackstone_wall", "ブラックストーンの塀", Material.BLACKSTONE_WALL),
        MineStackObjectByMaterial(BUILDING, "polished_blackstone_wall", "磨かれたブラックストーンの塀", Material.POLISHED_BLACKSTONE_WALL),
        MineStackObjectByMaterial(BUILDING, "polished_blackstone_brick_wall", "磨かれたブラックストーンレンガの塀", Material.POLISHED_BLACKSTONE_BRICK_WALL),
        MineStackObjectByMaterial(BUILDING, "cobbled_deepslate_wall", "深層岩の丸石の塀", Material.COBBLED_DEEPSLATE_WALL),
        MineStackObjectByMaterial(BUILDING, "polished_deepslate_wall", "磨かれた深層岩の塀", Material.POLISHED_DEEPSLATE_WALL),
        MineStackObjectByMaterial(BUILDING, "deepslate_brick_wall", "深層岩レンガの塀", Material.DEEPSLATE_BRICK_WALL),
        MineStackObjectByMaterial(BUILDING, "deepslate_tile_wall", "深層岩タイルの塀", Material.DEEPSLATE_TILE_WALL),
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(BUILDING, "fence", "オークのフェンス", Material.OAK_FENCE),
      List(
        MineStackObjectByMaterial(BUILDING, "spruce_fence", "マツのフェンス", Material.SPRUCE_FENCE),
        MineStackObjectByMaterial(BUILDING, "birch_fence", "シラカバのフェンス", Material.BIRCH_FENCE),
        MineStackObjectByMaterial(BUILDING, "jungle_fence", "ジャングルのフェンス", Material.JUNGLE_FENCE),
        MineStackObjectByMaterial(BUILDING, "acacia_fence", "アカシアのフェンス", Material.ACACIA_FENCE),
        MineStackObjectByMaterial(BUILDING, "dark_oak_fence", "ダークオークのフェンス", Material.DARK_OAK_FENCE),
        MineStackObjectByMaterial(BUILDING, "nether_brick_fence", "ネザーレンガのフェンス", Material.NETHER_BRICK_FENCE),
        MineStackObjectByMaterial(BUILDING, "crimson_fence", "真紅のフェンス", Material.CRIMSON_FENCE),
        MineStackObjectByMaterial(BUILDING, "warped_fence", "歪んだフェンス", Material.WARPED_FENCE),
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(BUILDING, "dead_tube_coral_fan", "死んだクダウチワサンゴ", Material.DEAD_TUBE_CORAL_FAN),
      List(
        MineStackObjectByMaterial(BUILDING, "dead_brain_coral_fan", "死んだノウウチワサンゴ", Material.DEAD_BRAIN_CORAL_FAN),
        MineStackObjectByMaterial(BUILDING, "dead_bubble_coral_fan", "死んだミズタマウチワサンゴ", Material.DEAD_BUBBLE_CORAL_FAN),
        MineStackObjectByMaterial(BUILDING, "dead_fire_coral_fan", "死んだミレポラウチワサンゴ", Material.DEAD_FIRE_CORAL_FAN),
        MineStackObjectByMaterial(BUILDING, "dead_horn_coral_fan", "死んだシカツノウチワサンゴ", Material.DEAD_HORN_CORAL_FAN),
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(BUILDING, "tube_coral_block", "クダサンゴブロック", Material.TUBE_CORAL_BLOCK),
      List(
        MineStackObjectByMaterial(BUILDING, "brain_coral_block", "ノウサンゴブロック", Material.BRAIN_CORAL_BLOCK),
        MineStackObjectByMaterial(BUILDING, "bubble_coral_block", "ミズタマサンゴブロック", Material.BUBBLE_CORAL_BLOCK),
        MineStackObjectByMaterial(BUILDING, "fire_coral_block", "ミレポラサンゴブロック", Material.FIRE_CORAL_BLOCK),
        MineStackObjectByMaterial(BUILDING, "horn_coral_block", "シカツノサンゴブロック", Material.HORN_CORAL_BLOCK),
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(BUILDING, "dead_tube_coral_block", "死んだクダサンゴブロック", Material.DEAD_TUBE_CORAL_BLOCK),
      List(
        MineStackObjectByMaterial(BUILDING, "dead_brain_coral_block", "死んだノウサンゴブロック", Material.DEAD_BRAIN_CORAL_BLOCK),
        MineStackObjectByMaterial(BUILDING, "dead_bubble_coral_block", "死んだミズタマサンゴブロック", Material.DEAD_BUBBLE_CORAL_BLOCK),
        MineStackObjectByMaterial(BUILDING, "dead_fire_coral_block", "死んだミレポラサンゴブロック", Material.DEAD_FIRE_CORAL_BLOCK),
        MineStackObjectByMaterial(BUILDING, "dead_horn_coral_block", "死んだシカツノサンゴブロック", Material.DEAD_HORN_CORAL_BLOCK),
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(BUILDING, "tube_coral", "クダサンゴ", Material.TUBE_CORAL),
      List(
        MineStackObjectByMaterial(BUILDING, "brain_coral", "ノウサンゴ", Material.BRAIN_CORAL),
        MineStackObjectByMaterial(BUILDING, "bubble_coral", "ミズタマサンゴ", Material.BUBBLE_CORAL),
        MineStackObjectByMaterial(BUILDING, "fire_coral", "ミレポラサンゴ", Material.FIRE_CORAL),
        MineStackObjectByMaterial(BUILDING, "horn_coral", "シカツノサンゴ", Material.HORN_CORAL),
        MineStackObjectByMaterial(BUILDING, "dead_brain_coral", "死んだノウサンゴ", Material.DEAD_BRAIN_CORAL),
        MineStackObjectByMaterial(BUILDING, "dead_bubble_coral", "死んだミズタマサンゴ", Material.DEAD_BUBBLE_CORAL),
        MineStackObjectByMaterial(BUILDING, "dead_fire_coral", "死んだミレポラサンゴ", Material.DEAD_FIRE_CORAL),
        MineStackObjectByMaterial(BUILDING, "dead_horn_coral", "死んだシカツノサンゴ", Material.DEAD_HORN_CORAL),
        MineStackObjectByMaterial(BUILDING, "dead_tube_coral", "死んだクダサンゴ", Material.DEAD_TUBE_CORAL),
        MineStackObjectByMaterial(BUILDING, "tube_coral_fan", "クダウチワサンゴ", Material.TUBE_CORAL_FAN),
        MineStackObjectByMaterial(BUILDING, "brain_coral_fan", "ノウウチワサンゴ", Material.BRAIN_CORAL_FAN),
        MineStackObjectByMaterial(BUILDING, "bubble_coral_fan", "ミズタマウチワサンゴ", Material.BUBBLE_CORAL_FAN),
        MineStackObjectByMaterial(BUILDING, "fire_coral_fan", "ミレポラウチワサンゴ", Material.FIRE_CORAL_FAN),
        MineStackObjectByMaterial(BUILDING, "horn_coral_fan", "シカツノウチワサンゴ", Material.HORN_CORAL_FAN),
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(BUILDING, "flower_banner_pattern", "旗の模様", Material.FLOWER_BANNER_PATTERN),
      List(
        MineStackObjectByMaterial(BUILDING, "creeper_banner_pattern", "旗の模様", Material.CREEPER_BANNER_PATTERN),
        MineStackObjectByMaterial(BUILDING, "skull_banner_pattern", "旗の模様", Material.SKULL_BANNER_PATTERN),
        MineStackObjectByMaterial(BUILDING, "mojang_banner_pattern", "旗の模様", Material.MOJANG_BANNER_PATTERN),
        MineStackObjectByMaterial(BUILDING, "globe_banner_pattern", "旗の模様", Material.GLOBE_BANNER_PATTERN),
        MineStackObjectByMaterial(BUILDING, "piglin_banner_pattern", "旗の模様", Material.PIGLIN_BANNER_PATTERN),
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(BUILDING, "white_banner", "白色の旗", Material.WHITE_BANNER),
      List(
        MineStackObjectByMaterial(BUILDING, "orange_banner", "橙色の旗", Material.ORANGE_BANNER),
        MineStackObjectByMaterial(BUILDING, "magenta_banner", "赤紫色の旗", Material.MAGENTA_BANNER),
        MineStackObjectByMaterial(BUILDING, "light_blue_banner", "空色の旗", Material.LIGHT_BLUE_BANNER),
        MineStackObjectByMaterial(BUILDING, "yellow_banner", "黄色の旗", Material.YELLOW_BANNER),
        MineStackObjectByMaterial(BUILDING, "lime_banner", "黄緑色の旗", Material.LIME_BANNER),
        MineStackObjectByMaterial(BUILDING, "pink_banner", "桃色の旗", Material.PINK_BANNER),
        MineStackObjectByMaterial(BUILDING, "gray_banner", "灰色の旗", Material.GRAY_BANNER),
        MineStackObjectByMaterial(BUILDING, "light_gray_banner", "薄灰色の旗", Material.LIGHT_GRAY_BANNER),
        MineStackObjectByMaterial(BUILDING, "cyan_banner", "青緑色の旗", Material.CYAN_BANNER),
        MineStackObjectByMaterial(BUILDING, "purple_banner", "紫色の旗", Material.PURPLE_BANNER),
        MineStackObjectByMaterial(BUILDING, "blue_banner", "青色の旗", Material.BLUE_BANNER),
        MineStackObjectByMaterial(BUILDING, "brown_banner", "茶色の旗", Material.BROWN_BANNER),
        MineStackObjectByMaterial(BUILDING, "green_banner", "緑色の旗", Material.GREEN_BANNER),
        MineStackObjectByMaterial(BUILDING, "red_banner", "赤色の旗", Material.RED_BANNER),
        MineStackObjectByMaterial(BUILDING, "black_banner", "黒色の旗", Material.BLACK_BANNER),
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(BUILDING, "sign", "看板", Material.OAK_SIGN),
      List(
        MineStackObjectByMaterial(BUILDING, "spruce_sign", "トウヒの看板", Material.SPRUCE_SIGN),
        MineStackObjectByMaterial(BUILDING, "birch_sign", "シラカバの看板", Material.BIRCH_SIGN),
        MineStackObjectByMaterial(BUILDING, "jungle_sign", "ジャングルの看板", Material.JUNGLE_SIGN),
        MineStackObjectByMaterial(BUILDING, "acacia_sign", "アカシアの看板", Material.ACACIA_SIGN),
        MineStackObjectByMaterial(BUILDING, "dark_oak_sign", "ダークオークの看板", Material.DARK_OAK_SIGN),
        MineStackObjectByMaterial(BUILDING, "crimson_sign", "真紅の看板", Material.CRIMSON_SIGN),
        MineStackObjectByMaterial(BUILDING, "warped_sign", "歪んだ看板", Material.WARPED_SIGN),
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(BUILDING, "candle", "ろうそく", Material.CANDLE),
      List(
        MineStackObjectByMaterial(BUILDING, "white_candle", "白色のろうそく", Material.WHITE_CANDLE),
        MineStackObjectByMaterial(BUILDING, "orange_candle", "橙色のろうそく", Material.ORANGE_CANDLE),
        MineStackObjectByMaterial(BUILDING, "magenta_candle", "赤紫色のろうそく", Material.MAGENTA_CANDLE),
        MineStackObjectByMaterial(BUILDING, "light_blue_candle", "空色のろうそく", Material.LIGHT_BLUE_CANDLE),
        MineStackObjectByMaterial(BUILDING, "yellow_candle", "黄色のろうそく", Material.YELLOW_CANDLE),
        MineStackObjectByMaterial(BUILDING, "lime_candle", "黄緑色のろうそく", Material.LIME_CANDLE),
        MineStackObjectByMaterial(BUILDING, "pink_candle", "桃色のろうそく", Material.PINK_CANDLE),
        MineStackObjectByMaterial(BUILDING, "gray_candle", "灰色のろうそく", Material.GRAY_CANDLE),
        MineStackObjectByMaterial(BUILDING, "light_gray_candle", "薄灰色のろうそく", Material.LIGHT_GRAY_CANDLE),
        MineStackObjectByMaterial(BUILDING, "cyan_candle", "青緑色のろうそく", Material.CYAN_CANDLE),
        MineStackObjectByMaterial(BUILDING, "purple_candle", "紫色のろうそく", Material.PURPLE_CANDLE),
        MineStackObjectByMaterial(BUILDING, "blue_candle", "青色のろうそく", Material.BLUE_CANDLE),
        MineStackObjectByMaterial(BUILDING, "brown_candle", "茶色のろうそく", Material.BROWN_CANDLE),
        MineStackObjectByMaterial(BUILDING, "green_candle", "緑色のろうそく", Material.GREEN_CANDLE),
        MineStackObjectByMaterial(BUILDING, "red_candle", "赤色のろうそく", Material.RED_CANDLE),
        MineStackObjectByMaterial(BUILDING, "black_candle", "黒色のろうそく", Material.BLACK_CANDLE),
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(BUILDING, "bed", "白色のベッド", Material.WHITE_BED),
      List(
        MineStackObjectByMaterial(BUILDING, "bed_1", "橙色のベッド", Material.ORANGE_BED),
        MineStackObjectByMaterial(BUILDING, "bed_2", "赤紫色のベッド", Material.MAGENTA_BED),
        MineStackObjectByMaterial(BUILDING, "bed_3", "空色のベッド", Material.LIGHT_BLUE_BED),
        MineStackObjectByMaterial(BUILDING, "bed_4", "黄色のベッド", Material.YELLOW_BED),
        MineStackObjectByMaterial(BUILDING, "bed_5", "黄緑色のベッド", Material.LIME_BED),
        MineStackObjectByMaterial(BUILDING, "bed_6", "桃色のベッド", Material.PINK_BED),
        MineStackObjectByMaterial(BUILDING, "bed_7", "灰色のベッド", Material.GRAY_BED),
        MineStackObjectByMaterial(BUILDING, "bed_8", "薄灰色のベッド", Material.LIGHT_GRAY_BED),
        MineStackObjectByMaterial(BUILDING, "bed_9", "青緑色のベッド", Material.CYAN_BED),
        MineStackObjectByMaterial(BUILDING, "bed_10", "紫色のベッド", Material.PURPLE_BED),
        MineStackObjectByMaterial(BUILDING, "bed_11", "青色のベッド", Material.BLUE_BED),
        MineStackObjectByMaterial(BUILDING, "bed_12", "茶色のベッド", Material.BROWN_BED),
        MineStackObjectByMaterial(BUILDING, "bed_13", "緑色のベッド", Material.GREEN_BED),
        MineStackObjectByMaterial(BUILDING, "bed_14", "赤色のベッド", Material.RED_BED),
        MineStackObjectByMaterial(BUILDING, "bed_15", "黒色のベッド", Material.BLACK_BED)
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(BUILDING, "hard_clay", "テラコッタ", Material.TERRACOTTA),
      List(
        MineStackObjectByMaterial(BUILDING, "stained_clay", "白色のテラコッタ", Material.WHITE_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING, "stained_clay1", "橙色のテラコッタ", Material.ORANGE_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING, "stained_clay2", "赤紫色のテラコッタ", Material.MAGENTA_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING, "stained_clay3", "空色のテラコッタ", Material.LIGHT_BLUE_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING, "stained_clay4", "黄色のテラコッタ", Material.YELLOW_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING, "stained_clay5", "黄緑色のテラコッタ", Material.LIME_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING, "stained_clay6", "桃色のテラコッタ", Material.PINK_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING, "stained_clay7", "灰色のテラコッタ", Material.GRAY_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING, "stained_clay8", "薄灰色のテラコッタ", Material.LIGHT_GRAY_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING, "stained_clay9", "青緑色のテラコッタ", Material.CYAN_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING, "stained_clay10", "紫色のテラコッタ", Material.PURPLE_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING, "stained_clay11", "青色のテラコッタ", Material.BLUE_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING, "stained_clay12", "茶色のテラコッタ", Material.BROWN_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING, "stained_clay13", "緑色のテラコッタ", Material.GREEN_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING, "stained_clay14", "赤色のテラコッタ", Material.RED_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING, "stained_clay15", "黒色のテラコッタ", Material.BLACK_TERRACOTTA)
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(BUILDING, "concrete", "白色のコンクリート", Material.WHITE_CONCRETE),
      List(
        MineStackObjectByMaterial(BUILDING, "concrete1", "橙色のコンクリート", Material.ORANGE_CONCRETE),
        MineStackObjectByMaterial(BUILDING, "concrete2", "赤紫色のコンクリート", Material.MAGENTA_CONCRETE),
        MineStackObjectByMaterial(BUILDING, "concrete3", "空色のコンクリート", Material.LIGHT_BLUE_CONCRETE),
        MineStackObjectByMaterial(BUILDING, "concrete4", "黄色のコンクリート", Material.YELLOW_CONCRETE),
        MineStackObjectByMaterial(BUILDING, "concrete5", "黄緑色のコンクリート", Material.LIME_CONCRETE),
        MineStackObjectByMaterial(BUILDING, "concrete6", "桃色のコンクリート", Material.PINK_CONCRETE),
        MineStackObjectByMaterial(BUILDING, "concrete7", "灰色のコンクリート", Material.GRAY_CONCRETE),
        MineStackObjectByMaterial(BUILDING, "concrete8", "薄灰色のコンクリート", Material.LIGHT_GRAY_CONCRETE),
        MineStackObjectByMaterial(BUILDING, "concrete9", "青緑色のコンクリート", Material.CYAN_CONCRETE),
        MineStackObjectByMaterial(BUILDING, "concrete10", "紫色のコンクリート", Material.PURPLE_CONCRETE),
        MineStackObjectByMaterial(BUILDING, "concrete11", "青色のコンクリート", Material.BLUE_CONCRETE),
        MineStackObjectByMaterial(BUILDING, "concrete12", "茶色のコンクリート", Material.BROWN_CONCRETE),
        MineStackObjectByMaterial(BUILDING, "concrete13", "緑色のコンクリート", Material.GREEN_CONCRETE),
        MineStackObjectByMaterial(BUILDING, "concrete14", "赤色のコンクリート", Material.RED_CONCRETE),
        MineStackObjectByMaterial(BUILDING, "concrete15", "黒色のコンクリート", Material.BLACK_CONCRETE)
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(BUILDING, "concrete_powder", "白色のコンクリートパウダー", Material.WHITE_CONCRETE_POWDER),
      List(
        MineStackObjectByMaterial(BUILDING, "concrete_powder1", "橙色のコンクリートパウダー", Material.ORANGE_CONCRETE_POWDER),
        MineStackObjectByMaterial(BUILDING, "concrete_powder2", "赤紫色のコンクリートパウダー", Material.MAGENTA_CONCRETE_POWDER),
        MineStackObjectByMaterial(BUILDING, "concrete_powder3", "空色のコンクリートパウダー", Material.LIGHT_BLUE_CONCRETE_POWDER),
        MineStackObjectByMaterial(BUILDING, "concrete_powder4", "黄色のコンクリートパウダー", Material.YELLOW_CONCRETE_POWDER),
        MineStackObjectByMaterial(BUILDING, "concrete_powder5", "黄緑色のコンクリートパウダー", Material.LIME_CONCRETE_POWDER),
        MineStackObjectByMaterial(BUILDING,"concrete_powder6","桃色のコンクリートパウダー",Material.PINK_CONCRETE_POWDER),
        MineStackObjectByMaterial(BUILDING,"concrete_powder7","灰色のコンクリートパウダー",Material.GRAY_CONCRETE_POWDER),
        MineStackObjectByMaterial(BUILDING,"concrete_powder8","薄灰色のコンクリートパウダー",Material.LIGHT_GRAY_CONCRETE_POWDER),
        MineStackObjectByMaterial(BUILDING,"concrete_powder9","青緑色のコンクリートパウダー",Material.CYAN_CONCRETE_POWDER),
        MineStackObjectByMaterial(BUILDING,"concrete_powder10","紫色のコンクリートパウダー",Material.PURPLE_CONCRETE_POWDER),
        MineStackObjectByMaterial(BUILDING,"concrete_powder11","青色のコンクリートパウダー",Material.BLUE_CONCRETE_POWDER),
        MineStackObjectByMaterial(BUILDING,"concrete_powder12","茶色のコンクリートパウダー",Material.BROWN_CONCRETE_POWDER),
        MineStackObjectByMaterial(BUILDING,"concrete_powder13","緑色のコンクリートパウダー",Material.GREEN_CONCRETE_POWDER),
        MineStackObjectByMaterial(BUILDING,"concrete_powder14","赤色のコンクリートパウダー",Material.RED_CONCRETE_POWDER),
        MineStackObjectByMaterial(BUILDING,"concrete_powder15","黒色のコンクリートパウダー",Material.BLACK_CONCRETE_POWDER)
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(BUILDING,"white_glazed_terracotta","白色の彩釉テラコッタ",Material.WHITE_GLAZED_TERRACOTTA),
      List(
        MineStackObjectByMaterial(BUILDING,"orange_glazed_terracotta","橙色の彩釉テラコッタ",Material.ORANGE_GLAZED_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING,"magenta_glazed_terracotta","赤紫色の彩釉テラコッタ",Material.MAGENTA_GLAZED_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING,"light_blue_glazed_terracotta","空色の彩釉テラコッタ",Material.LIGHT_BLUE_GLAZED_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING,"yellow_glazed_terracotta","黄色の彩釉テラコッタ",Material.YELLOW_GLAZED_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING,"lime_glazed_terracotta","黄緑色の彩釉テラコッタ",Material.LIME_GLAZED_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING,"pink_glazed_terracotta","桃色の彩釉テラコッタ",Material.PINK_GLAZED_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING,"gray_glazed_terracotta","灰色の彩釉テラコッタ",Material.GRAY_GLAZED_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING,"silver_glazed_terracotta","薄灰色の彩釉テラコッタ",Material.LIGHT_GRAY_GLAZED_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING,"cyan_glazed_terracotta","青緑色の彩釉テラコッタ",Material.CYAN_GLAZED_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING,"purple_glazed_terracotta","紫色の彩釉テラコッタ",Material.PURPLE_GLAZED_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING,"blue_glazed_terracotta","青色の彩釉テラコッタ",Material.BLUE_GLAZED_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING,"brown_glazed_terracotta","茶色の彩釉テラコッタ",Material.BROWN_GLAZED_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING,"green_glazed_terracotta","緑色の彩釉テラコッタ",Material.GREEN_GLAZED_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING,"red_glazed_terracotta","赤色の彩釉テラコッタ",Material.RED_GLAZED_TERRACOTTA),
        MineStackObjectByMaterial(BUILDING,"black_glazed_terracotta","黒色の彩釉テラコッタ",Material.BLACK_GLAZED_TERRACOTTA)
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(BUILDING, "wool_0", "白色の羊毛", Material.WHITE_WOOL),
      List(
        MineStackObjectByMaterial(BUILDING, "wool_1", "橙色の羊毛", Material.ORANGE_WOOL),
        MineStackObjectByMaterial(BUILDING, "wool_2", "赤紫色の羊毛", Material.MAGENTA_WOOL),
        MineStackObjectByMaterial(BUILDING, "wool_3", "空色の羊毛", Material.LIGHT_BLUE_WOOL),
        MineStackObjectByMaterial(BUILDING, "wool_4", "黄色の羊毛", Material.YELLOW_WOOL),
        MineStackObjectByMaterial(BUILDING, "wool_5", "黄緑色の羊毛", Material.LIME_WOOL),
        MineStackObjectByMaterial(BUILDING, "wool_6", "桃色の羊毛", Material.PINK_WOOL),
        MineStackObjectByMaterial(BUILDING, "wool_7", "灰色の羊毛", Material.GRAY_WOOL),
        MineStackObjectByMaterial(BUILDING, "wool_8", "薄灰色の羊毛", Material.LIGHT_GRAY_WOOL),
        MineStackObjectByMaterial(BUILDING, "wool_9", "青緑色の羊毛", Material.CYAN_WOOL),
        MineStackObjectByMaterial(BUILDING, "wool_10", "紫色の羊毛", Material.PURPLE_WOOL),
        MineStackObjectByMaterial(BUILDING, "wool_11", "青色の羊毛", Material.BLUE_WOOL),
        MineStackObjectByMaterial(BUILDING, "wool_12", "茶色の羊毛", Material.BROWN_WOOL),
        MineStackObjectByMaterial(BUILDING, "wool_13", "緑色の羊毛", Material.GREEN_WOOL),
        MineStackObjectByMaterial(BUILDING, "wool_14", "赤色の羊毛", Material.RED_WOOL),
        MineStackObjectByMaterial(BUILDING, "wool_15", "黒色の羊毛", Material.BLACK_WOOL)
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(BUILDING, "carpet_0", "白色のカーペット", Material.WHITE_CARPET),
      List(
        MineStackObjectByMaterial(BUILDING, "carpet_1", "橙色のカーペット", Material.ORANGE_CARPET),
        MineStackObjectByMaterial(BUILDING, "carpet_2", "赤紫色のカーペット", Material.MAGENTA_CARPET),
        MineStackObjectByMaterial(BUILDING, "carpet_3", "空色のカーペット", Material.LIGHT_BLUE_CARPET),
        MineStackObjectByMaterial(BUILDING, "carpet_4", "黄色のカーペット", Material.YELLOW_CARPET),
        MineStackObjectByMaterial(BUILDING, "carpet_5", "黄緑色のカーペット", Material.LIME_CARPET),
        MineStackObjectByMaterial(BUILDING, "carpet_6", "桃色のカーペット", Material.PINK_CARPET),
        MineStackObjectByMaterial(BUILDING, "carpet_7", "灰色のカーペット", Material.GRAY_CARPET),
        MineStackObjectByMaterial(BUILDING, "carpet_8", "薄灰色のカーペット", Material.LIGHT_GRAY_CARPET),
        MineStackObjectByMaterial(BUILDING, "carpet_9", "青緑色のカーペット", Material.CYAN_CARPET),
        MineStackObjectByMaterial(BUILDING, "carpet_10", "紫色のカーペット", Material.PURPLE_CARPET),
        MineStackObjectByMaterial(BUILDING, "carpet_11", "青色のカーペット", Material.BLUE_CARPET),
        MineStackObjectByMaterial(BUILDING, "carpet_12", "茶色のカーペット", Material.BROWN_CARPET),
        MineStackObjectByMaterial(BUILDING, "carpet_13", "緑色のカーペット", Material.GREEN_CARPET),
        MineStackObjectByMaterial(BUILDING, "carpet_14", "赤色のカーペット", Material.RED_CARPET),
        MineStackObjectByMaterial(BUILDING, "carpet_15", "黒色のカーペット", Material.BLACK_CARPET)
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(BUILDING, "glass", "ガラス",Material.GLASS),
      List(
        MineStackObjectByMaterial(BUILDING, "stained_glass_0", "白色の色付きガラス", Material.WHITE_STAINED_GLASS),
        MineStackObjectByMaterial(BUILDING, "stained_glass_1", "橙色の色付きガラス", Material.ORANGE_STAINED_GLASS),
        MineStackObjectByMaterial(BUILDING, "stained_glass_2", "赤紫色の色付きガラス", Material.MAGENTA_STAINED_GLASS),
        MineStackObjectByMaterial(BUILDING, "stained_glass_3", "空色の色付きガラス", Material.LIGHT_BLUE_STAINED_GLASS),
        MineStackObjectByMaterial(BUILDING, "stained_glass_4", "黄色の色付きガラス", Material.YELLOW_STAINED_GLASS),
        MineStackObjectByMaterial(BUILDING, "stained_glass_5", "黄緑色の色付きガラス", Material.LIME_STAINED_GLASS),
        MineStackObjectByMaterial(BUILDING, "stained_glass_6", "桃色の色付きガラス", Material.PINK_STAINED_GLASS),
        MineStackObjectByMaterial(BUILDING, "stained_glass_7", "灰色の色付きガラス", Material.GRAY_STAINED_GLASS),
        MineStackObjectByMaterial(BUILDING, "stained_glass_8", "薄灰色の色付きガラス", Material.LIGHT_GRAY_STAINED_GLASS),
        MineStackObjectByMaterial(BUILDING, "stained_glass_9", "青緑色の色付きガラス", Material.CYAN_STAINED_GLASS),
        MineStackObjectByMaterial(BUILDING, "stained_glass_10", "紫色の色付きガラス", Material.PURPLE_STAINED_GLASS),
        MineStackObjectByMaterial(BUILDING, "stained_glass_11", "青色の色付きガラス", Material.BLUE_STAINED_GLASS),
        MineStackObjectByMaterial(BUILDING, "stained_glass_12", "茶色の色付きガラス", Material.BROWN_STAINED_GLASS),
        MineStackObjectByMaterial(BUILDING, "stained_glass_13", "緑色の色付きガラス", Material.GREEN_STAINED_GLASS),
        MineStackObjectByMaterial(BUILDING, "stained_glass_14", "赤色の色付きガラス", Material.RED_STAINED_GLASS),
        MineStackObjectByMaterial(BUILDING, "stained_glass_15", "黒色の色付きガラス", Material.BLACK_STAINED_GLASS)
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(BUILDING, "glass_panel", "板ガラス", Material.GLASS_PANE),
      List(
          MineStackObjectByMaterial(BUILDING,"glass_panel_0","白色の色付きガラス板",Material.WHITE_STAINED_GLASS_PANE),
          MineStackObjectByMaterial(BUILDING,"glass_panel_1","橙色の色付きガラス板",Material.ORANGE_STAINED_GLASS_PANE),
          MineStackObjectByMaterial(BUILDING,"glass_panel_2","赤紫色の色付きガラス板",Material.MAGENTA_STAINED_GLASS_PANE),
          MineStackObjectByMaterial(BUILDING,"glass_panel_3","空色の色付きガラス板",Material.LIGHT_BLUE_STAINED_GLASS_PANE),
          MineStackObjectByMaterial(BUILDING,"glass_panel_4","黄色の色付きガラス板",Material.YELLOW_STAINED_GLASS_PANE),
          MineStackObjectByMaterial(BUILDING,"glass_panel_5","黄緑色の色付きガラス板",Material.LIME_STAINED_GLASS_PANE),
          MineStackObjectByMaterial(BUILDING,"glass_panel_6","桃色の色付きガラス板",Material.PINK_STAINED_GLASS_PANE),
          MineStackObjectByMaterial(BUILDING,"glass_panel_7","灰色の色付きガラス板",Material.GRAY_STAINED_GLASS_PANE),
          MineStackObjectByMaterial(BUILDING,"glass_panel_8","薄灰色の色付きガラス板",Material.LIGHT_GRAY_STAINED_GLASS_PANE),
          MineStackObjectByMaterial(BUILDING,"glass_panel_9","青緑色の色付きガラス板",Material.CYAN_STAINED_GLASS_PANE),
          MineStackObjectByMaterial(BUILDING,"glass_panel_10","紫色の色付きガラス板",Material.PURPLE_STAINED_GLASS_PANE),
          MineStackObjectByMaterial(BUILDING,"glass_panel_11","青色の色付きガラス板",Material.BLUE_STAINED_GLASS_PANE),
          MineStackObjectByMaterial(BUILDING,"glass_panel_12","茶色の色付きガラス板",Material.BROWN_STAINED_GLASS_PANE),
          MineStackObjectByMaterial(BUILDING,"glass_panel_13","緑色の色付きガラス板",Material.GREEN_STAINED_GLASS_PANE),
          MineStackObjectByMaterial(BUILDING,"glass_panel_14","赤色の色付きガラス板",Material.RED_STAINED_GLASS_PANE),
          MineStackObjectByMaterial(BUILDING,"glass_panel_15","黒色の色付きガラス板",Material.BLACK_STAINED_GLASS_PANE)
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(BUILDING, "dye_1", "赤色の染料", Material.RED_DYE),
      List(
        MineStackObjectByMaterial(BUILDING, "dye_2", "緑色の染料", Material.GREEN_DYE),
        MineStackObjectByMaterial(BUILDING, "dye_5", "紫色の染料", Material.PURPLE_DYE),
        MineStackObjectByMaterial(BUILDING, "dye_6", "青緑色の染料", Material.CYAN_DYE),
        MineStackObjectByMaterial(BUILDING, "dye_7", "薄灰色の染料", Material.LIGHT_GRAY_DYE),
        MineStackObjectByMaterial(BUILDING, "dye_8", "灰色の染料", Material.GRAY_DYE),
        MineStackObjectByMaterial(BUILDING, "dye_9", "桃色の染料", Material.PINK_DYE),
        MineStackObjectByMaterial(BUILDING, "dye_10", "黄緑色の染料", Material.LIME_DYE),
        MineStackObjectByMaterial(BUILDING, "dye_11", "黄色の染料", Material.YELLOW_DYE),
        MineStackObjectByMaterial(BUILDING, "dye_12", "空色の染料", Material.LIGHT_BLUE_DYE),
        MineStackObjectByMaterial(BUILDING, "dye_13", "赤紫色の染料", Material.MAGENTA_DYE),
        MineStackObjectByMaterial(BUILDING, "dye_14", "橙色の染料", Material.ORANGE_DYE),
        MineStackObjectByMaterial(BUILDING, "dye_15", "骨粉", Material.BONE_MEAL),
        MineStackObjectByMaterial(BUILDING, "ink_sack0", "イカスミ", Material.INK_SAC),
        MineStackObjectByMaterial(BUILDING, "white_dye", "白色の染料", Material.WHITE_DYE),
        MineStackObjectByMaterial(BUILDING, "blue_dye", "青色の染料", Material.BLUE_DYE),
        MineStackObjectByMaterial(BUILDING, "brown_dye", "茶色の染料", Material.BROWN_DYE),
        MineStackObjectByMaterial(BUILDING, "black_dye", "黒色の染料", Material.BLACK_DYE),
      )
    )
  )

  // レッドストーン系ブロック
  private val minestacklistrs: List[MineStackObjectGroup[ItemStack]] = leftElems(
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"redstone","レッドストーン",Material.REDSTONE),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"redstone_block","レッドストーンブロック",Material.REDSTONE_BLOCK),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "lever", "レバー", Material.LEVER),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"redstone_torch_on","レッドストーントーチ",Material.REDSTONE_TORCH),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"note_block","音符ブロック",Material.NOTE_BLOCK),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"redstone_lamp_off","レッドストーンランプ",Material.REDSTONE_LAMP),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"tripwire_hook","トリップワイヤーフック",Material.TRIPWIRE_HOOK),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "dropper", "ドロッパー", Material.DROPPER),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"piston_sticky_base","粘着ピストン",Material.STICKY_PISTON),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"piston_base","ピストン",Material.PISTON),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "tnt", "TNT", Material.TNT),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"trapped_chest","トラップチェスト",Material.TRAPPED_CHEST),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"daylight_detector","日照センサー",Material.DAYLIGHT_DETECTOR),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"diode","レッドストーンリピーター",Material.REPEATER),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"dispenser","ディスペンサー",Material.DISPENSER),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "hopper", "ホッパー", Material.HOPPER),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"redstone_comparator","レッドストーンコンパレーター",Material.COMPARATOR),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"powered_rail","パワードレール",Material.POWERED_RAIL),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"detector_rail","ディテクターレール",Material.DETECTOR_RAIL),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"activator_rail","アクティベーターレール",Material.ACTIVATOR_RAIL),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "saddle", "サドル", Material.SADDLE),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "minecart", "トロッコ", Material.MINECART),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"chest_minecart","チェスト付きトロッコ",Material.CHEST_MINECART),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"furnace_minecart","かまど付きトロッコ",Material.FURNACE_MINECART),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"hopper_minecart","ホッパー付きトロッコ",Material.HOPPER_MINECART),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"iron_horse_armor","鉄の馬鎧",Material.IRON_HORSE_ARMOR),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"golden_horse_armor","金の馬鎧",Material.GOLDEN_HORSE_ARMOR),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"diamond_horse_armor","ダイヤの馬鎧",Material.DIAMOND_HORSE_ARMOR),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "observer", "オブザーバー", Material.OBSERVER),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "lectern", "書見台", Material.LECTERN),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "target", "的", Material.TARGET),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "lightning_rod", "避雷針", Material.LIGHTNING_ROD),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "sculk_sensor", "スカルクセンサー", Material.SCULK_SENSOR),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "bundle", "バンドル", Material.BUNDLE),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "clock", "時計", Material.CLOCK),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "spyglass", "望遠鏡", Material.SPYGLASS),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "firework_rocket", "ロケット花火", Material.FIREWORK_ROCKET),
    MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "redstone_wire", "レッドストーンワイヤー", Material.REDSTONE_WIRE),
  ) ++ rightElems(
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"wood_button","木のボタン",Material.OAK_BUTTON),
      List(
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"stone_button","石のボタン",Material.STONE_BUTTON),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "spruce_button", "トウヒのボタン", Material.SPRUCE_BUTTON),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "birch_button", "シラカバのボタン", Material.BIRCH_BUTTON),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "jungle_button", "ジャングルのボタン", Material.JUNGLE_BUTTON),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "acacia_button", "アカシアのボタン", Material.ACACIA_BUTTON),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "dark_oak_button", "ダークオークのボタン", Material.DARK_OAK_BUTTON),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "polished_blackstone_button", "磨かれたブラックストーンのボタン", Material.POLISHED_BLACKSTONE_BUTTON),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "crimson_button", "真紅のボタン", Material.CRIMSON_BUTTON),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "warped_button", "歪んだボタン", Material.WARPED_BUTTON),
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"wood_door","オークのドア",Material.OAK_DOOR),
      List(
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"spruce_door_item","マツのドア",Material.SPRUCE_DOOR),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"birch_door_item","シラカバのドア",Material.BIRCH_DOOR),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"jungle_door_item","ジャングルのドア",Material.JUNGLE_DOOR),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"acacia_door_item","アカシアのドア",Material.ACACIA_DOOR),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"dark_oak_door_item","ダークオークのドア",Material.DARK_OAK_DOOR),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"iron_door","鉄のドア",Material.IRON_DOOR),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "crimson_door", "真紅のドア", Material.CRIMSON_DOOR),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "warped_door", "歪んだドア", Material.WARPED_DOOR),
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"trap_door","木のトラップドア",Material.OAK_TRAPDOOR),
      List(
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"iron_trapdoor","鉄のトラップドア",Material.IRON_TRAPDOOR),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "spruce_trapdoor", "トウヒのトラップドア", Material.SPRUCE_TRAPDOOR),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "birch_trapdoor", "シラカバのトラップドア", Material.BIRCH_TRAPDOOR),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "jungle_trapdoor", "ジャングルのトラップドア", Material.JUNGLE_TRAPDOOR),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "acacia_trapdoor", "アカシアのトラップドア", Material.ACACIA_TRAPDOOR),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "dark_oak_trapdoor", "ダークオークのトラップドア", Material.DARK_OAK_TRAPDOOR),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "crimson_trapdoor", "真紅のトラップドア", Material.CRIMSON_TRAPDOOR),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "warped_trapdoor", "歪んだトラップドア", Material.WARPED_TRAPDOOR),
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"fence_gate","オークのフェンスゲート",Material.OAK_FENCE_GATE),
      List(
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"spruce_fence_gate","マツのフェンスゲート",Material.SPRUCE_FENCE_GATE),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"birch_fence_gate","シラカバのフェンスゲート",Material.BIRCH_FENCE_GATE),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"jungle_fence_gate","ジャングルのフェンスゲート",Material.JUNGLE_FENCE_GATE),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"dark_oak_fence_gate","ダークオークのフェンスゲート",Material.DARK_OAK_FENCE_GATE),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"acacia_fence_gate","アカシアのフェンスゲート",Material.ACACIA_FENCE_GATE),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "crimson_fence_gate", "真紅のフェンスゲート", Material.CRIMSON_FENCE_GATE),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "warped_fence_gate", "歪んだフェンスゲート", Material.WARPED_FENCE_GATE),
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"wood_plate","木の感圧版",Material.OAK_PRESSURE_PLATE),
      List(
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"stone_plate","石の感圧版",Material.STONE_PRESSURE_PLATE),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "polished_blackstone_pressure_plate", "磨かれたブラックストーンの感圧板", Material.POLISHED_BLACKSTONE_PRESSURE_PLATE),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "spruce_pressure_plate", "トウヒの感圧板", Material.SPRUCE_PRESSURE_PLATE),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "birch_pressure_plate", "シラカバの感圧板", Material.BIRCH_PRESSURE_PLATE),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "jungle_pressure_plate", "ジャングルの感圧板", Material.JUNGLE_PRESSURE_PLATE),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "acacia_pressure_plate", "アカシアの感圧板", Material.ACACIA_PRESSURE_PLATE),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "dark_oak_pressure_plate", "ダークオークの感圧板", Material.DARK_OAK_PRESSURE_PLATE),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "crimson_pressure_plate", "真紅の感圧板", Material.CRIMSON_PRESSURE_PLATE),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "warped_pressure_plate", "歪んだ感圧板", Material.WARPED_PRESSURE_PLATE),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"gold_plate","重量感圧版 (軽) ",Material.LIGHT_WEIGHTED_PRESSURE_PLATE),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"iron_plate","重量感圧版 (重) ",Material.HEAVY_WEIGHTED_PRESSURE_PLATE),
      )
    ),
    MineStackObjectWithKindVariants(
      MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION, "boat", "オークのボート", Material.OAK_BOAT),
      List(
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"spruce_boat","マツのボート",Material.SPRUCE_BOAT),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"birch_boat","シラカバのボート",Material.BIRCH_BOAT),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"jungle_boat","ジャングルのボート",Material.JUNGLE_BOAT),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"acacia_boat","アカシアのボート",Material.ACACIA_BOAT),
        MineStackObjectByMaterial(REDSTONE_AND_TRANSPORTATION,"dark_oak_boat","ダークオークのボート",Material.DARK_OAK_BOAT),
      )
    )
  )

  /**
   * デフォルトでガチャの内容に含まれている景品。
   */
  private val minestackBuiltinGachaPrizes: List[MineStackObjectGroup[ItemStack]] = leftElems(
    MineStackObjectByItemStack(GACHA_PRIZES, "gachaimo", None, hasNameLore = true, gachaPrizeAPI.staticGachaPrizeFactory.gachaRingo),
    MineStackObjectByItemStack(GACHA_PRIZES, "exp_bottle", Some("エンチャントの瓶"), hasNameLore = false, new ItemStack(Material.EXPERIENCE_BOTTLE))
  )

  // @formatter:on

  import cats.implicits._

  private val gachaPrizesObjects: F[Ref[F, Vector[MineStackObject[ItemStack]]]] = for {
    gachaPrizes <- gachaPrizeAPI.gachaPrizesWhenGachaEventsIsNotHolding
    mineStackObjects <- Ref.of[F, Vector[MineStackObject[ItemStack]]](
      gachaPrizes.sortBy(_.id.id).map { gachaPrize =>
        MineStackObjectByItemStack(
          GACHA_PRIZES,
          s"gachadata0_${gachaPrize.id.id}",
          None,
          hasNameLore = true,
          gachaPrize.itemStack
        )
      }
    )
  } yield mineStackObjects

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
      case Right(group) => List(group.representative) ++ group.kindVariants
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

  override def findBySignedItemStacks(
    itemStacks: Vector[ItemStack],
    player: Player
  ): F[Vector[(ItemStack, Option[MineStackObject[ItemStack]])]] = {
    for {
      gachaPrizes <- gachaPrizeAPI.gachaPrizesWhenGachaEventsIsNotHolding
      mineStackObjects <- allMineStackObjects
    } yield {
      implicit val canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack] =
        gachaPrizeAPI.canBeSignedAsGachaPrize

      val signedItemStacks = gachaPrizes.map { gachaPrize =>
        gachaPrize.materializeWithOwnerSignature(player.getName) -> gachaPrize.itemStack
      }

      itemStacks.map { _itemStack =>
        signedItemStacks.find(_._1.isSimilar(_itemStack)) match {
          case Some((_, notSignedItemStack)) =>
            _itemStack -> mineStackObjects.find(_.itemStack.isSimilar(notSignedItemStack))
          case None =>
            mineStackObjects.find(_.itemStack.isSimilar(_itemStack)) match {
              case Some(value)
                  if value.category != GACHA_PRIZES || minestackBuiltinGachaPrizes.exists(
                    _.swap.contains(value)
                  ) =>
                _itemStack -> Some(value)
              case _ => _itemStack -> None
            }
        }
      }
    }
  }

  override protected implicit val F: Functor[F] = implicitly
}
