package com.github.unchama.buildassist;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;


public class BuildAssist {
    public static Plugin plugin;
    static Boolean DEBUG = false;

    //起動するタスクリスト
    private List<BukkitTask> tasklist = new ArrayList<>();

    //総建築量ランキング表示用データリスト
    //public static final List<Integer> ranklist = new ArrayList<>();

    //Playerdataに依存するデータリスト
    public static final HashMap<UUID, PlayerData> playermap = new HashMap<>();
    private HashMap<String, TabExecutor> commandlist;
    public static BuildAssistConfig config;

    //lvの閾値
    static final List<Integer> levellist = Arrays.asList(
            0, 50, 100, 200, 300,
            450, 600, 900, 1200, 1600,        //10
            2000, 2500, 3000, 3600, 4300,
            5100, 6000, 7000, 8200, 9400,    //20
            10800, 12200, 13800, 15400, 17200,
            19000, 21000, 23000, 25250, 27500,    //30
            30000, 32500, 35500, 38500, 42000,
            45500, 49500, 54000, 59000, 64000,    //40
            70000, 76000, 83000, 90000, 98000,
            106000, 115000, 124000, 133000, 143000,//50
            153000, 163000, 174000, 185000, 196000,
            208000, 220000, 232000, 245000, 258000,//60
            271000, 285000, 299000, 313000, 328000,
            343000, 358000, 374000, 390000, 406000,//70
            423000, 440000, 457000, 475000, 493000,
            511000, 530000, 549000, 568000, 588000,//80
            608000, 628000, 648000, 668000, 688000,
            708000, 728000, 748000, 768000, 788000,//90
            808000, 828000, 848000, 868000, 888000,
            908000, 928000, 948000, 968000, 1000000,//100
            5000000
    );

    //範囲設置ブロックの対象リスト
    static final List<Material> materiallist = Arrays.asList(


            Material.STONE//石
            , Material.GRASS//草
            , Material.DIRT//土
            , Material.COBBLESTONE//丸石
            , Material.WOOD//木
            , Material.SAND//砂
            , Material.GRAVEL//砂利
            , Material.GOLD_ORE//金鉱石
            , Material.IRON_ORE//鉄鉱石
            , Material.COAL_ORE//石炭鉱石
            , Material.LOG//原木
            , Material.GLASS//ガラス
            , Material.LAPIS_ORE//ラピス鉱石
            , Material.LAPIS_BLOCK//ラピスB
            , Material.SANDSTONE//砂岩
            , Material.WOOL//羊毛
            , Material.GOLD_BLOCK//金B
            , Material.IRON_BLOCK//鉄B
            , Material.BRICK//レンガB
            , Material.BOOKSHELF//本棚
            , Material.MOSSY_COBBLESTONE//苔石
            , Material.OBSIDIAN//黒曜石
            , Material.DIAMOND_ORE//ダイヤ鉱石
            , Material.DIAMOND_BLOCK//ダイヤB
            , Material.REDSTONE_ORE//赤鉱石
            , Material.ICE//氷
            , Material.SNOW_BLOCK//雪B
            , Material.CLAY//粘土B
            , Material.NETHERRACK//ネザーラック
            , Material.SOUL_SAND//ソウルサンド
            , Material.GLOWSTONE//グロウストーン
            , Material.STAINED_GLASS//色付きガラス
            , Material.SMOOTH_BRICK//石レンガ
            , Material.MYCEL//菌糸
            , Material.NETHER_BRICK//ネザーレンガ
            , Material.ENDER_STONE//エンドストーン
            , Material.EMERALD_ORE//エメ鉱石
            , Material.EMERALD_BLOCK//エメB
            , Material.COBBLE_WALL//丸石の壁
            , Material.QUARTZ_ORE//水晶鉱石
            , Material.QUARTZ_BLOCK//水晶B
            , Material.STAINED_CLAY//色付き固焼き粘土
            , Material.LOG_2//原木2
            , Material.PRISMARINE//プリズマリン
            , Material.SEA_LANTERN//シーランタン
            , Material.HARD_CLAY//固焼き粘土
            , Material.COAL_BLOCK//石炭B
            , Material.PACKED_ICE//氷塊
            , Material.RED_SANDSTONE//赤い砂岩
            , Material.PURPUR_BLOCK//プルパーブ
            , Material.PURPUR_PILLAR//柱状プルパーブ
            , Material.END_BRICKS//エンドレンガB
            , Material.RED_NETHER_BRICK//赤ネザーレンガB
            , Material.BONE_BLOCK//骨B
            , Material.NETHER_WART_BLOCK//ネザーウォートB
            , Material.CONCRETE//コンクリート
            , Material.CONCRETE_POWDER//コンクリートパウダー


            , Material.ACACIA_STAIRS, Material.ACACIA_FENCE, Material.ACACIA_FENCE_GATE,
            Material.BIRCH_WOOD_STAIRS, Material.BIRCH_FENCE, Material.BIRCH_FENCE_GATE,
            Material.BONE_BLOCK, Material.BOOKSHELF,
            Material.BRICK, Material.BRICK_STAIRS,
            Material.CACTUS, Material.CHEST,
            Material.CLAY_BRICK,
            Material.DARK_OAK_STAIRS, Material.DARK_OAK_FENCE, Material.DARK_OAK_FENCE_GATE,
            Material.END_BRICKS,
            Material.FURNACE, Material.GLOWSTONE, Material.HARD_CLAY,
            Material.JACK_O_LANTERN, Material.JUKEBOX, Material.JUNGLE_FENCE, Material.JUNGLE_FENCE_GATE,
            Material.JUNGLE_WOOD_STAIRS, Material.LADDER, Material.LEAVES, Material.LEAVES_2,
            Material.LOG, Material.LOG_2, Material.NETHER_BRICK, Material.NETHER_BRICK_STAIRS,
            Material.NETHER_WART_BLOCK, Material.RED_NETHER_BRICK,
            Material.OBSIDIAN, Material.PACKED_ICE, Material.PRISMARINE,
            Material.PUMPKIN, Material.PURPUR_BLOCK, Material.PURPUR_SLAB,
            Material.PURPUR_STAIRS, Material.PURPUR_PILLAR,
            Material.QUARTZ_BLOCK, Material.QUARTZ_STAIRS, Material.QUARTZ,
            Material.SANDSTONE, Material.SANDSTONE_STAIRS, Material.SEA_LANTERN,
            Material.SLIME_BLOCK, Material.SMOOTH_BRICK, Material.SMOOTH_STAIRS,
            Material.SNOW_BLOCK, Material.SPRUCE_FENCE, Material.SPRUCE_FENCE_GATE,
            Material.SPRUCE_WOOD_STAIRS, Material.FENCE, Material.FENCE_GATE,
            Material.STAINED_CLAY, Material.STAINED_GLASS, Material.STAINED_GLASS_PANE,
            Material.STEP, Material.STONE, Material.STONE_SLAB2, Material.THIN_GLASS,
            Material.TORCH, Material.WOOD,
            Material.WOOD_STAIRS, Material.WOOD_STEP,
            Material.WOOL, Material.CARPET, Material.WORKBENCH
    );

    //ハーフブロックまとめ
    public static final List<Material> material_slab = Arrays.asList(
            Material.STONE_SLAB2, Material.PURPUR_SLAB, Material.WOOD_STEP, Material.STEP
    );


    //直列設置ブロックの対象リスト
    static final List<Material> materiallist2 = Arrays.asList(
            Material.STONE//石
            , Material.GRASS//草
            , Material.DIRT//土
            , Material.COBBLESTONE//丸石
            , Material.WOOD//木
            , Material.SAND//砂
            , Material.GRAVEL//砂利
            , Material.GOLD_ORE//金鉱石
            , Material.IRON_ORE//鉄鉱石
            , Material.COAL_ORE//石炭鉱石
            , Material.LOG//原木
            , Material.GLASS//ガラス
            , Material.LAPIS_ORE//ラピス鉱石
            , Material.LAPIS_BLOCK//ラピスB
            , Material.SANDSTONE//砂岩
            , Material.WOOL//羊毛
            , Material.GOLD_BLOCK//金B
            , Material.IRON_BLOCK//鉄B
            , Material.BRICK//レンガB
            , Material.BOOKSHELF//本棚
            , Material.MOSSY_COBBLESTONE//苔石
            , Material.OBSIDIAN//黒曜石
            , Material.DIAMOND_ORE//ダイヤ鉱石
            , Material.DIAMOND_BLOCK//ダイヤB
            , Material.REDSTONE_ORE//赤鉱石
            , Material.ICE//氷
            , Material.SNOW_BLOCK//雪B
            , Material.CLAY//粘土B
            , Material.NETHERRACK//ネザーラック
            , Material.SOUL_SAND//ソウルサンド
            , Material.GLOWSTONE//グロウストーン
            , Material.STAINED_GLASS//色付きガラス
            , Material.SMOOTH_BRICK//石レンガ
            , Material.MYCEL//菌糸
            , Material.NETHER_BRICK//ネザーレンガ
            , Material.ENDER_STONE//エンドストーン
            , Material.EMERALD_ORE//エメ鉱石
            , Material.EMERALD_BLOCK//エメB
            , Material.COBBLE_WALL//丸石の壁
            , Material.QUARTZ_ORE//水晶鉱石
            , Material.QUARTZ_BLOCK//水晶B
            , Material.STAINED_CLAY//色付き固焼き粘土
            , Material.LOG_2//原木2
            , Material.PRISMARINE//プリズマリン
            , Material.SEA_LANTERN//シーランタン
            , Material.HARD_CLAY//固焼き粘土
            , Material.COAL_BLOCK//石炭B
            , Material.PACKED_ICE//氷塊
            , Material.RED_SANDSTONE//赤い砂岩
            , Material.PURPUR_BLOCK//プルパーブ
            , Material.PURPUR_PILLAR//柱状プルパーブ
            , Material.END_BRICKS//エンドレンガB
            , Material.RED_NETHER_BRICK//赤ネザーレンガB
            , Material.BONE_BLOCK//骨B

//			,Material.LEAVES//葉		設置した葉が時間経過で消えるので除外
            , Material.FENCE//オークフェンス
            , Material.IRON_FENCE//鉄フェンス
            , Material.THIN_GLASS//板ガラス
            , Material.NETHER_FENCE//ネザーフェンス
            , Material.STAINED_GLASS_PANE//色付き板ガラス
//			,Material.LEAVES_2//葉2		設置した葉が時間経過で消えるので除外
            , Material.SLIME_BLOCK//スライムB
//			,Material.CARPET//カーペット
            , Material.SPRUCE_FENCE//松フェンス
            , Material.BIRCH_FENCE//白樺フェンス
            , Material.JUNGLE_FENCE//ジャングルフェンス
            , Material.DARK_OAK_FENCE//ダークオークフェンス
            , Material.ACACIA_FENCE//アカシアフェンス
//			,Material.RAILS//レール

            , Material.NETHER_WART_BLOCK//ネザーウォートB
            , Material.CONCRETE//コンクリート
            , Material.CONCRETE_POWDER//コンクリートパウダー
/*
			,Material.WHITE_GLAZED_TERRACOTTA//彩釉テラコッタ	ブロックの向きがあるので除外
			,Material.ORANGE_GLAZED_TERRACOTTA
			,Material.MAGENTA_GLAZED_TERRACOTTA
			,Material.LIGHT_BLUE_GLAZED_TERRACOTTA
			,Material.YELLOW_GLAZED_TERRACOTTA
			,Material.LIME_GLAZED_TERRACOTTA
			,Material.PINK_GLAZED_TERRACOTTA
			,Material.GRAY_GLAZED_TERRACOTTA
			,Material.SILVER_GLAZED_TERRACOTTA
			,Material.CYAN_GLAZED_TERRACOTTA
			,Material.PURPLE_GLAZED_TERRACOTTA
			,Material.BLUE_GLAZED_TERRACOTTA
			,Material.BROWN_GLAZED_TERRACOTTA
			,Material.GREEN_GLAZED_TERRACOTTA
			,Material.RED_GLAZED_TERRACOTTA
			,Material.BLACK_GLAZED_TERRACOTTA
*/
    );

    static final List<Material> material_slab2 = Arrays.asList(
            Material.STONE_SLAB2    //赤砂岩
            , Material.PURPUR_SLAB    //プルパー
            , Material.WOOD_STEP        //木
            , Material.STEP            //石
    );

    static final List<Material> material_destruction = Arrays.asList(
            Material.LONG_GRASS            //草
            , Material.DEAD_BUSH            //枯れ木
            , Material.YELLOW_FLOWER        //タンポポ
            , Material.RED_ROSE            //花9種
            , Material.BROWN_MUSHROOM    //きのこ
            , Material.RED_MUSHROOM        //赤きのこ
            , Material.TORCH                //松明
            , Material.SNOW                //雪
            , Material.DOUBLE_PLANT        //高い花、草
            , Material.WATER                //水
            , Material.STATIONARY_WATER    //水
    );

    public static String[] line_up_str = {"OFF", "上側", "下側"};
    public static String[] line_up_step_str = {"上側", "下側", "両方"};
    public static String[] line_up_off_on_str = {"OFF", "ON"};

    public BuildAssist(Plugin plugin) {
        BuildAssist.plugin = plugin;
    }

    public void onEnable() {
        //コンフィグ系の設定は全てConfig.javaに移動
        config = new BuildAssistConfig(plugin);
        config.loadConfig();


        //コマンドの登録
        commandlist = new HashMap<>();
        commandlist.put("fly", new flyCommand(plugin));

        plugin.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new EntityListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerRightClickListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerInventoryListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new BlockLineUp(), plugin);        //クリックイベント登録
        plugin.getServer().getPluginManager().registerEvents(new PlayerQuitListener(), plugin);    //退出時
        plugin.getServer().getPluginManager().registerEvents(new BlockPlaceEventListener(), plugin);    //ブロックを置いた時


        for (Player p : plugin.getServer().getOnlinePlayers()) {
            UUID uuid = p.getUniqueId();

            PlayerData playerdata = new PlayerData(p);

            playerdata.updateLevel(p);

            playermap.put(uuid, playerdata);
        }
        plugin.getLogger().info("BuildAssist is Enabled!");

        tasklist.add(new MinuteTaskRunnable().runTaskTimer(plugin, 0, 1200));
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return commandlist.get(cmd.getName()).onCommand(sender, cmd, label, args);
    }


    public void onDisable() {
        for (BukkitTask task : this.tasklist) {
            task.cancel();
        }
    }

}
