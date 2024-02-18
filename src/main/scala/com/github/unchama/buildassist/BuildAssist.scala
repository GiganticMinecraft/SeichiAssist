package com.github.unchama.buildassist

import cats.effect.{ConcurrentEffect, IO, SyncIO}
import com.github.unchama.buildassist.listener._
import com.github.unchama.buildassist.menu.BuildAssistMenuRouter
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.listener.BuildMainMenuOpener
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountData
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.managedfly.ManagedFlyApi
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.seichiassist.subsystems.playerheadskin.PlayerHeadSkinAPI
import com.github.unchama.seichiassist.{DefaultEffectEnvironment, subsystems}
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.{Bukkit, Material}

import java.util
import java.util.UUID
import scala.collection.mutable

class BuildAssist(plugin: Plugin)(
  implicit flyApi: ManagedFlyApi[SyncIO, Player],
  buildCountAPI: subsystems.buildcount.BuildCountAPI[IO, SyncIO, Player],
  manaApi: ManaApi[IO, SyncIO, Player],
  mineStackAPI: MineStackAPI[IO, Player, ItemStack],
  ioConcurrentEffect: ConcurrentEffect[IO],
  playerHeadSkinAPI: PlayerHeadSkinAPI[IO, Player]
) {

  // TODO この辺のフィールドを整理する

  /**
   * 永続化されない、プレーヤーのセッション内でのみ有効な一時データを管理するMap。 [[TemporaryDataInitializer]] によって初期化、削除される。
   */
  val temporaryData: mutable.HashMap[UUID, TemporaryMutableBuildAssistPlayerData] =
    mutable.HashMap()

  val buildAmountDataRepository
    : KeyedDataRepository[Player, ReadOnlyRef[SyncIO, BuildAmountData]] =
    buildCountAPI.playerBuildAmountRepository

  {
    BuildAssist.plugin = plugin
    BuildAssist.instance = this
  }

  def onEnable(): Unit = {
    implicit val menuRouter: BuildAssistMenuRouter[IO] = {
      import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{
        layoutPreparationContext,
        onMainThread
      }

      BuildAssistMenuRouter.apply
    }

    // コンフィグ系の設定は全てConfig.javaに移動
    BuildAssist.config = new BuildAssistConfig(plugin)
    BuildAssist.config.loadConfig()

    import buildCountAPI._
    import menuRouter._

    implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment

    val listeners = List(
      new BuildMainMenuOpener(),
      new PlayerInventoryListener(),
      new TemporaryDataInitializer(this.temporaryData),
      new BlockLineUpTriggerListener[SyncIO],
      new TilingSkillTriggerListener[IO, SyncIO]
    )

    listeners.foreach { listener =>
      Bukkit.getServer.getPluginManager.registerEvents(listener, plugin)
    }

    plugin.getLogger.info("BuildAssist is Enabled!")
  }

}

object BuildAssist {
  var instance: BuildAssist = _

  // 範囲設置ブロックの対象リスト
  val materiallist: java.util.Set[Material] = util
    .EnumSet
    .of(
      Material.STONE // 石
      ,
      Material.GRASS // 草
      ,
      Material.DIRT // 土
      ,
      Material.COBBLESTONE // 丸石
      ,
      Material.ACACIA_WOOD,
      Material.BIRCH_WOOD,
      Material.DARK_OAK_WOOD,
      Material.JUNGLE_WOOD,
      Material.OAK_WOOD,
      Material.SAND // 砂
      ,
      Material.GRAVEL // 砂利
      ,
      Material.GOLD_ORE // 金鉱石
      ,
      Material.IRON_ORE // 鉄鉱石
      ,
      Material.COAL_ORE // 石炭鉱石
      ,
      Material.ACACIA_LOG,
      Material.BIRCH_LOG,
      Material.DARK_OAK_LOG,
      Material.JUNGLE_LOG,
      Material.OAK_LOG,
      Material.GLASS // ガラス
      ,
      Material.LAPIS_ORE // ラピス鉱石
      ,
      Material.LAPIS_BLOCK // ラピスB
      ,
      Material.SANDSTONE, // 砂岩
      Material.BLACK_WOOL,
      Material.BLUE_WOOL,
      Material.BROWN_WOOL,
      Material.CYAN_WOOL,
      Material.GRAY_WOOL,
      Material.GREEN_WOOL,
      Material.LIGHT_BLUE_WOOL,
      Material.LIGHT_GRAY_WOOL,
      Material.LIME_WOOL,
      Material.MAGENTA_WOOL,
      Material.ORANGE_WOOL,
      Material.PINK_WOOL,
      Material.PURPLE_WOOL,
      Material.RED_WOOL,
      Material.WHITE_WOOL,
      Material.YELLOW_WOOL,
      Material.GOLD_BLOCK // 金B
      ,
      Material.IRON_BLOCK // 鉄B
      ,
      Material.BRICK // レンガB
      ,
      Material.BOOKSHELF // 本棚
      ,
      Material.MOSSY_COBBLESTONE // 苔石
      ,
      Material.OBSIDIAN // 黒曜石
      ,
      Material.DIAMOND_ORE // ダイヤ鉱石
      ,
      Material.DIAMOND_BLOCK // ダイヤB
      ,
      Material.REDSTONE_ORE // 赤鉱石
      ,
      Material.ICE // 氷
      ,
      Material.SNOW_BLOCK // 雪B
      ,
      Material.CLAY // 粘土B
      ,
      Material.NETHERRACK // ネザーラック
      ,
      Material.SOUL_SAND // ソウルサンド
      ,
      Material.GLOWSTONE,
      Material.BLACK_STAINED_GLASS,
      Material.BLUE_STAINED_GLASS,
      Material.BROWN_STAINED_GLASS,
      Material.CYAN_STAINED_GLASS,
      Material.GRAY_STAINED_GLASS,
      Material.GREEN_STAINED_GLASS,
      Material.LIGHT_BLUE_STAINED_GLASS,
      Material.LIGHT_GRAY_STAINED_GLASS,
      Material.LIME_STAINED_GLASS,
      Material.MAGENTA_STAINED_GLASS,
      Material.ORANGE_STAINED_GLASS,
      Material.PINK_STAINED_GLASS,
      Material.PURPLE_STAINED_GLASS,
      Material.RED_STAINED_GLASS,
      Material.WHITE_STAINED_GLASS,
      Material.YELLOW_STAINED_GLASS,
      Material.STONE_BRICKS // 石レンガ
      ,
      Material.MYCELIUM // 菌糸
      ,
      Material.NETHER_BRICK // ネザーレンガ
      ,
      Material.END_STONE // エンドストーン
      ,
      Material.EMERALD_ORE // エメ鉱石
      ,
      Material.EMERALD_BLOCK // エメB
      ,
      Material.COBBLESTONE_WALL // 丸石の壁
      ,
      Material.NETHER_QUARTZ_ORE // 水晶鉱石
      ,
      Material.QUARTZ_BLOCK // 水晶B
      ,
      Material.BLACK_TERRACOTTA,
      Material.BLUE_TERRACOTTA,
      Material.BROWN_TERRACOTTA,
      Material.CYAN_TERRACOTTA,
      Material.GRAY_TERRACOTTA,
      Material.GREEN_TERRACOTTA,
      Material.LIGHT_BLUE_TERRACOTTA,
      Material.LIGHT_GRAY_TERRACOTTA,
      Material.LIME_TERRACOTTA,
      Material.MAGENTA_TERRACOTTA,
      Material.ORANGE_TERRACOTTA,
      Material.PINK_TERRACOTTA,
      Material.PURPLE_TERRACOTTA,
      Material.RED_TERRACOTTA,
      Material.WHITE_TERRACOTTA,
      Material.YELLOW_TERRACOTTA,
      Material.PRISMARINE // プリズマリン
      ,
      Material.SEA_LANTERN, // シーランタン
      Material.BLACK_GLAZED_TERRACOTTA,
      Material.BLUE_GLAZED_TERRACOTTA,
      Material.BROWN_GLAZED_TERRACOTTA,
      Material.CYAN_GLAZED_TERRACOTTA,
      Material.GRAY_GLAZED_TERRACOTTA,
      Material.GREEN_GLAZED_TERRACOTTA,
      Material.LIGHT_BLUE_GLAZED_TERRACOTTA,
      Material.LIGHT_GRAY_GLAZED_TERRACOTTA,
      Material.LIME_GLAZED_TERRACOTTA,
      Material.MAGENTA_GLAZED_TERRACOTTA,
      Material.ORANGE_GLAZED_TERRACOTTA,
      Material.PINK_GLAZED_TERRACOTTA,
      Material.PURPLE_GLAZED_TERRACOTTA,
      Material.RED_GLAZED_TERRACOTTA,
      Material.WHITE_GLAZED_TERRACOTTA,
      Material.YELLOW_GLAZED_TERRACOTTA,
      Material.COAL_BLOCK // 石炭B
      ,
      Material.PACKED_ICE // 氷塊
      ,
      Material.RED_SANDSTONE // 赤い砂岩
      ,
      Material.PURPUR_BLOCK // プルパーブ
      ,
      Material.PURPUR_PILLAR // 柱状プルパーブ
      ,
      Material.END_STONE_BRICKS // エンドレンガB
      ,
      Material.RED_NETHER_BRICKS // 赤ネザーレンガB
      ,
      Material.BONE_BLOCK // 骨B
      ,
      Material.NETHER_WART_BLOCK, // ネザーウォートB
      Material.BLACK_CONCRETE,
      Material.BLUE_CONCRETE,
      Material.BROWN_CONCRETE,
      Material.CYAN_CONCRETE,
      Material.GRAY_CONCRETE,
      Material.GREEN_CONCRETE,
      Material.LIGHT_BLUE_CONCRETE,
      Material.LIGHT_GRAY_CONCRETE,
      Material.LIME_CONCRETE,
      Material.MAGENTA_CONCRETE,
      Material.ORANGE_CONCRETE,
      Material.PINK_CONCRETE,
      Material.PURPLE_CONCRETE,
      Material.RED_CONCRETE,
      Material.WHITE_CONCRETE,
      Material.YELLOW_CONCRETE,
      Material.BLACK_CONCRETE_POWDER,
      Material.BLUE_CONCRETE_POWDER,
      Material.BROWN_CONCRETE_POWDER,
      Material.CYAN_CONCRETE_POWDER,
      Material.GRAY_CONCRETE_POWDER,
      Material.GREEN_CONCRETE_POWDER,
      Material.LIGHT_BLUE_CONCRETE_POWDER,
      Material.LIGHT_GRAY_CONCRETE_POWDER,
      Material.LIME_CONCRETE_POWDER,
      Material.MAGENTA_CONCRETE_POWDER,
      Material.ORANGE_CONCRETE_POWDER,
      Material.PINK_CONCRETE_POWDER,
      Material.PURPLE_CONCRETE_POWDER,
      Material.RED_CONCRETE_POWDER,
      Material.WHITE_CONCRETE_POWDER,
      Material.YELLOW_CONCRETE_POWDER,
      Material.ACACIA_STAIRS,
      Material.ACACIA_FENCE,
      Material.ACACIA_FENCE_GATE,
      Material.BIRCH_STAIRS,
      Material.BIRCH_FENCE,
      Material.BIRCH_FENCE_GATE,
      Material.BONE_BLOCK,
      Material.BOOKSHELF,
      Material.BRICK,
      Material.BRICK_STAIRS,
      Material.CACTUS,
      Material.CHEST,
      Material.DARK_OAK_STAIRS,
      Material.DARK_OAK_FENCE,
      Material.DARK_OAK_FENCE_GATE,
      Material.END_STONE_BRICKS,
      Material.FURNACE,
      Material.GLOWSTONE,
      Material.JACK_O_LANTERN,
      Material.JUKEBOX,
      Material.JUNGLE_FENCE,
      Material.JUNGLE_FENCE_GATE,
      Material.STRIPPED_JUNGLE_WOOD,
      Material.LADDER,
      Material.OAK_LEAVES,
      Material.BIRCH_LEAVES,
      Material.JUNGLE_LEAVES,
      Material.ACACIA_LEAVES,
      Material.DARK_OAK_LEAVES,
      Material.SPRUCE_LEAVES,
      Material.ACACIA_LOG,
      Material.BIRCH_LOG,
      Material.DARK_OAK_LOG,
      Material.JUNGLE_LOG,
      Material.OAK_LOG,
      Material.NETHER_BRICK,
      Material.NETHER_BRICK_STAIRS,
      Material.NETHER_WART_BLOCK,
      Material.RED_NETHER_BRICKS,
      Material.OBSIDIAN,
      Material.PACKED_ICE,
      Material.PRISMARINE,
      Material.PUMPKIN,
      Material.PURPUR_BLOCK,
      Material.PURPUR_SLAB,
      Material.PURPUR_STAIRS,
      Material.PURPUR_PILLAR,
      Material.QUARTZ_BLOCK,
      Material.QUARTZ_STAIRS,
      Material.QUARTZ,
      Material.SANDSTONE,
      Material.SANDSTONE_STAIRS,
      Material.SEA_LANTERN,
      Material.SLIME_BLOCK,
      Material.SMOOTH_STONE,
      Material.CHISELED_STONE_BRICKS,
      Material.CRACKED_STONE_BRICKS,
      Material.INFESTED_STONE_BRICKS,
      Material.MOSSY_STONE_BRICKS,
      Material.STONE_BRICK_STAIRS,
      Material.SNOW_BLOCK,
      Material.SPRUCE_FENCE,
      Material.SPRUCE_FENCE_GATE,
      Material.STRIPPED_ACACIA_WOOD,
      Material.STRIPPED_BIRCH_WOOD,
      Material.STRIPPED_DARK_OAK_WOOD,
      Material.STRIPPED_JUNGLE_WOOD,
      Material.STRIPPED_OAK_WOOD,
      Material.OAK_FENCE,
      Material.ACACIA_FENCE,
      Material.BIRCH_FENCE,
      Material.DARK_OAK_FENCE,
      Material.JUNGLE_FENCE,
      Material.OAK_FENCE_GATE,
      Material.ACACIA_FENCE_GATE,
      Material.BIRCH_FENCE_GATE,
      Material.DARK_OAK_FENCE_GATE,
      Material.JUNGLE_FENCE_GATE,
      Material.OAK_FENCE_GATE,
      Material.BLACK_STAINED_GLASS_PANE,
      Material.BLUE_STAINED_GLASS_PANE,
      Material.BROWN_STAINED_GLASS_PANE,
      Material.CYAN_STAINED_GLASS_PANE,
      Material.GRAY_STAINED_GLASS_PANE,
      Material.GREEN_STAINED_GLASS_PANE,
      Material.LIGHT_BLUE_STAINED_GLASS_PANE,
      Material.LIGHT_GRAY_STAINED_GLASS_PANE,
      Material.LIME_STAINED_GLASS_PANE,
      Material.MAGENTA_STAINED_GLASS_PANE,
      Material.ORANGE_STAINED_GLASS_PANE,
      Material.PINK_STAINED_GLASS_PANE,
      Material.PURPLE_STAINED_GLASS_PANE,
      Material.RED_STAINED_GLASS_PANE,
      Material.WHITE_STAINED_GLASS_PANE,
      Material.YELLOW_STAINED_GLASS_PANE,
      Material.ACACIA_SLAB,
      Material.BIRCH_SLAB,
      Material.DARK_OAK_SLAB,
      Material.JUNGLE_SLAB,
      Material.OAK_SLAB,
      Material.STONE,
      Material.STONE_SLAB,
      Material.TORCH,
      Material.BLACK_CARPET,
      Material.BLUE_CARPET,
      Material.BROWN_CARPET,
      Material.CYAN_CARPET,
      Material.GRAY_CARPET,
      Material.GREEN_CARPET,
      Material.LIGHT_BLUE_CARPET,
      Material.LIGHT_GRAY_CARPET,
      Material.LIME_CARPET,
      Material.MAGENTA_CARPET,
      Material.ORANGE_CARPET,
      Material.PINK_CARPET,
      Material.PURPLE_CARPET,
      Material.RED_CARPET,
      Material.WHITE_CARPET,
      Material.YELLOW_CARPET,
      Material.CRAFTING_TABLE
    )

  // 直列設置ブロックの対象リスト
  val materiallist2: java.util.Set[Material] = util
    .EnumSet
    .of(
      Material.STONE // 石
      ,
      Material.GRASS // 草
      ,
      Material.DIRT // 土
      ,
      Material.COBBLESTONE // 丸石
      ,
      Material.ACACIA_WOOD,
      Material.BIRCH_WOOD,
      Material.DARK_OAK_WOOD,
      Material.JUNGLE_WOOD,
      Material.OAK_WOOD,
      Material.SAND // 砂
      ,
      Material.GRAVEL // 砂利
      ,
      Material.GOLD_ORE // 金鉱石
      ,
      Material.IRON_ORE // 鉄鉱石
      ,
      Material.COAL_ORE // 石炭鉱石
      ,
      Material.ACACIA_LOG,
      Material.BIRCH_LOG,
      Material.DARK_OAK_LOG,
      Material.JUNGLE_LOG,
      Material.OAK_LOG,
      Material.GLASS // ガラス
      ,
      Material.LAPIS_ORE // ラピス鉱石
      ,
      Material.LAPIS_BLOCK // ラピスB
      ,
      Material.SANDSTONE // 砂岩
      ,
      Material.BLACK_WOOL,
      Material.BLUE_WOOL,
      Material.BROWN_WOOL,
      Material.CYAN_WOOL,
      Material.GRAY_WOOL,
      Material.GREEN_WOOL,
      Material.LIGHT_BLUE_WOOL,
      Material.LIGHT_GRAY_WOOL,
      Material.LIME_WOOL,
      Material.MAGENTA_WOOL,
      Material.ORANGE_WOOL,
      Material.PINK_WOOL,
      Material.PURPLE_WOOL,
      Material.RED_WOOL,
      Material.WHITE_WOOL,
      Material.YELLOW_WOOL,
      Material.GOLD_BLOCK // 金B
      ,
      Material.IRON_BLOCK // 鉄B
      ,
      Material.BRICK // レンガB
      ,
      Material.BOOKSHELF // 本棚
      ,
      Material.MOSSY_COBBLESTONE // 苔石
      ,
      Material.OBSIDIAN // 黒曜石
      ,
      Material.DIAMOND_ORE // ダイヤ鉱石
      ,
      Material.DIAMOND_BLOCK // ダイヤB
      ,
      Material.REDSTONE_ORE // 赤鉱石
      ,
      Material.ICE // 氷
      ,
      Material.SNOW_BLOCK // 雪B
      ,
      Material.CLAY // 粘土B
      ,
      Material.NETHERRACK // ネザーラック
      ,
      Material.SOUL_SAND // ソウルサンド
      ,
      Material.GLOWSTONE // グロウストーン
      ,
      Material.BLACK_STAINED_GLASS,
      Material.BLUE_STAINED_GLASS,
      Material.BROWN_STAINED_GLASS,
      Material.CYAN_STAINED_GLASS,
      Material.GRAY_STAINED_GLASS,
      Material.GREEN_STAINED_GLASS,
      Material.LIGHT_BLUE_STAINED_GLASS,
      Material.LIGHT_GRAY_STAINED_GLASS,
      Material.LIME_STAINED_GLASS,
      Material.MAGENTA_STAINED_GLASS,
      Material.ORANGE_STAINED_GLASS,
      Material.PINK_STAINED_GLASS,
      Material.PURPLE_STAINED_GLASS,
      Material.RED_STAINED_GLASS,
      Material.WHITE_STAINED_GLASS,
      Material.YELLOW_STAINED_GLASS,
      Material.STONE_BRICKS // 石レンガ
      ,
      Material.MYCELIUM // 菌糸
      ,
      Material.NETHER_BRICK // ネザーレンガ
      ,
      Material.END_STONE // エンドストーン
      ,
      Material.EMERALD_ORE // エメ鉱石
      ,
      Material.EMERALD_BLOCK // エメB
      ,
      Material.COBBLESTONE_WALL // 丸石の壁
      ,
      Material.NETHER_QUARTZ_ORE // 水晶鉱石
      ,
      Material.QUARTZ_BLOCK // 水晶B
      ,
      Material.BLACK_TERRACOTTA,
      Material.BLUE_TERRACOTTA,
      Material.BROWN_TERRACOTTA,
      Material.CYAN_TERRACOTTA,
      Material.GRAY_TERRACOTTA,
      Material.GREEN_TERRACOTTA,
      Material.LIGHT_BLUE_TERRACOTTA,
      Material.LIGHT_GRAY_TERRACOTTA,
      Material.LIME_TERRACOTTA,
      Material.MAGENTA_TERRACOTTA,
      Material.ORANGE_TERRACOTTA,
      Material.PINK_TERRACOTTA,
      Material.PURPLE_TERRACOTTA,
      Material.RED_TERRACOTTA,
      Material.WHITE_TERRACOTTA,
      Material.YELLOW_TERRACOTTA,
      Material.PRISMARINE // プリズマリン
      ,
      Material.SEA_LANTERN // シーランタン
      ,
      Material.BLACK_GLAZED_TERRACOTTA,
      Material.BLUE_GLAZED_TERRACOTTA,
      Material.BROWN_GLAZED_TERRACOTTA,
      Material.CYAN_GLAZED_TERRACOTTA,
      Material.GRAY_GLAZED_TERRACOTTA,
      Material.GREEN_GLAZED_TERRACOTTA,
      Material.LIGHT_BLUE_GLAZED_TERRACOTTA,
      Material.LIGHT_GRAY_GLAZED_TERRACOTTA,
      Material.LIME_GLAZED_TERRACOTTA,
      Material.MAGENTA_GLAZED_TERRACOTTA,
      Material.ORANGE_GLAZED_TERRACOTTA,
      Material.PINK_GLAZED_TERRACOTTA,
      Material.PURPLE_GLAZED_TERRACOTTA,
      Material.RED_GLAZED_TERRACOTTA,
      Material.WHITE_GLAZED_TERRACOTTA,
      Material.YELLOW_GLAZED_TERRACOTTA,
      Material.COAL_BLOCK // 石炭B
      ,
      Material.PACKED_ICE // 氷塊
      ,
      Material.RED_SANDSTONE // 赤い砂岩
      ,
      Material.PURPUR_BLOCK // プルパーブ
      ,
      Material.PURPUR_PILLAR // 柱状プルパーブ
      ,
      Material.END_STONE_BRICKS // エンドレンガB
      ,
      Material.RED_NETHER_BRICKS // 赤ネザーレンガB
      ,
      Material.BONE_BLOCK // 骨B
      ,
      Material.OAK_FENCE,
      Material.ACACIA_FENCE,
      Material.BIRCH_FENCE,
      Material.DARK_OAK_FENCE,
      Material.JUNGLE_FENCE,
      Material.IRON_BARS // 鉄フェンス
      ,
      Material.BLACK_STAINED_GLASS,
      Material.BLUE_STAINED_GLASS,
      Material.BROWN_STAINED_GLASS,
      Material.CYAN_STAINED_GLASS,
      Material.GRAY_STAINED_GLASS,
      Material.GREEN_STAINED_GLASS,
      Material.LIGHT_BLUE_STAINED_GLASS,
      Material.LIGHT_GRAY_STAINED_GLASS,
      Material.LIME_STAINED_GLASS,
      Material.MAGENTA_STAINED_GLASS,
      Material.ORANGE_STAINED_GLASS,
      Material.PINK_STAINED_GLASS,
      Material.PURPLE_STAINED_GLASS,
      Material.RED_STAINED_GLASS,
      Material.WHITE_STAINED_GLASS,
      Material.YELLOW_STAINED_GLASS,
      Material.NETHER_BRICK_FENCE // ネザーフェンス
      ,
      Material.BLACK_STAINED_GLASS_PANE,
      Material.BLUE_STAINED_GLASS_PANE,
      Material.BROWN_STAINED_GLASS_PANE,
      Material.CYAN_STAINED_GLASS_PANE,
      Material.GRAY_STAINED_GLASS_PANE,
      Material.GREEN_STAINED_GLASS_PANE,
      Material.LIGHT_BLUE_STAINED_GLASS_PANE,
      Material.LIGHT_GRAY_STAINED_GLASS_PANE,
      Material.LIME_STAINED_GLASS_PANE,
      Material.MAGENTA_STAINED_GLASS_PANE,
      Material.ORANGE_STAINED_GLASS_PANE,
      Material.PINK_STAINED_GLASS_PANE,
      Material.PURPLE_STAINED_GLASS_PANE,
      Material.RED_STAINED_GLASS_PANE,
      Material.WHITE_STAINED_GLASS_PANE,
      Material.YELLOW_STAINED_GLASS_PANE,
      Material.SLIME_BLOCK // スライムB
      ,
      Material.SPRUCE_FENCE // 松フェンス
      ,
      Material.BIRCH_FENCE // 白樺フェンス
      ,
      Material.JUNGLE_FENCE // ジャングルフェンス
      ,
      Material.DARK_OAK_FENCE // ダークオークフェンス
      ,
      Material.ACACIA_FENCE // アカシアフェンス
      ,
      Material.NETHER_WART_BLOCK, // ネザーウォートB
      Material.BLACK_CONCRETE,
      Material.BLUE_CONCRETE,
      Material.BROWN_CONCRETE,
      Material.CYAN_CONCRETE,
      Material.GRAY_CONCRETE,
      Material.GREEN_CONCRETE,
      Material.LIGHT_BLUE_CONCRETE,
      Material.LIGHT_GRAY_CONCRETE,
      Material.LIME_CONCRETE,
      Material.MAGENTA_CONCRETE,
      Material.ORANGE_CONCRETE,
      Material.PINK_CONCRETE,
      Material.PURPLE_CONCRETE,
      Material.RED_CONCRETE,
      Material.WHITE_CONCRETE,
      Material.YELLOW_CONCRETE,
      Material.BLACK_CONCRETE_POWDER,
      Material.BLUE_CONCRETE_POWDER,
      Material.BROWN_CONCRETE_POWDER,
      Material.CYAN_CONCRETE_POWDER,
      Material.GRAY_CONCRETE_POWDER,
      Material.GREEN_CONCRETE_POWDER,
      Material.LIGHT_BLUE_CONCRETE_POWDER,
      Material.LIGHT_GRAY_CONCRETE_POWDER,
      Material.LIME_CONCRETE_POWDER,
      Material.MAGENTA_CONCRETE_POWDER,
      Material.ORANGE_CONCRETE_POWDER,
      Material.PINK_CONCRETE_POWDER,
      Material.PURPLE_CONCRETE_POWDER,
      Material.RED_CONCRETE_POWDER,
      Material.WHITE_CONCRETE_POWDER,
      Material.YELLOW_CONCRETE_POWDER,
      Material.OAK_LEAVES,
      Material.BIRCH_LEAVES,
      Material.JUNGLE_LEAVES,
      Material.ACACIA_LEAVES,
      Material.DARK_OAK_LEAVES,
      Material.SPRUCE_LEAVES
    )

  // ハーフブロックとして扱うMaterial
  val material_slab2: java.util.Set[Material] = util
    .EnumSet
    .of(
      Material.RED_SANDSTONE_SLAB,
      Material.PURPUR_SLAB,
      Material.ACACIA_SLAB,
      Material.BIRCH_SLAB,
      Material.DARK_OAK_SLAB,
      Material.JUNGLE_SLAB,
      Material.OAK_SLAB,
      Material.STONE_SLAB,
      Material.STONE_BRICK_SLAB
    )

  val material_destruction: java.util.Set[Material] = util
    .EnumSet
    .of(
      Material.GRASS,
      Material.TALL_GRASS,
      Material.DEAD_BUSH // 枯れ木
      ,
      Material.DANDELION,
      Material.POPPY,
      Material.BLUE_ORCHID,
      Material.ALLIUM,
      Material.AZURE_BLUET,
      Material.RED_TULIP,
      Material.ORANGE_TULIP,
      Material.WHITE_TULIP,
      Material.PINK_TULIP,
      Material.OXEYE_DAISY,
      Material.BROWN_MUSHROOM // きのこ
      ,
      Material.RED_MUSHROOM // 赤きのこ
      ,
      Material.TORCH // 松明
      ,
      Material.SNOW // 雪
      ,
      Material.SUNFLOWER,
      Material.LILAC,
      Material.LARGE_FERN,
      Material.ROSE_BUSH,
      Material.PEONY,
      Material.WATER // 水
      ,
      Material.LAVA // 溶岩
      ,
      Material.VINE // ツタ
    )

  var plugin: Plugin = _
  var config: BuildAssistConfig = _
  val line_up_str: Seq[String] = Seq("OFF", "上側", "下側")
  val line_up_step_str: Seq[String] = Seq("上側", "下側", "両方")
  val line_up_off_on_str: Seq[String] = Seq("OFF", "ON")
}
