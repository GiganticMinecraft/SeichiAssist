package com.github.unchama.seichiassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.github.unchama.seichiassist.bungee.BungeeReceiver;
import com.github.unchama.seichiassist.commands.AchieveCommand;
import com.github.unchama.seichiassist.commands.EventCommand;
import com.github.unchama.seichiassist.commands.GiganticFeverCommand;
import com.github.unchama.seichiassist.commands.HalfBlockProtectCommand;
import com.github.unchama.seichiassist.commands.contributeCommand;
import com.github.unchama.seichiassist.commands.effectCommand;
import com.github.unchama.seichiassist.commands.gachaCommand;
import com.github.unchama.seichiassist.commands.lastquitCommand;
import com.github.unchama.seichiassist.commands.levelCommand;
import com.github.unchama.seichiassist.commands.mebiusCommand;
import com.github.unchama.seichiassist.commands.rmpCommand;
import com.github.unchama.seichiassist.commands.seichiCommand;
import com.github.unchama.seichiassist.commands.shareinvCommand;
import com.github.unchama.seichiassist.commands.stickCommand;
import com.github.unchama.seichiassist.commands.subHomeCommand;
import com.github.unchama.seichiassist.data.GachaData;
import com.github.unchama.seichiassist.data.MineStackGachaData;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.RankData;
import com.github.unchama.seichiassist.listener.EntityListener;
import com.github.unchama.seichiassist.listener.GachaItemListener;
import com.github.unchama.seichiassist.listener.MebiusListener;
import com.github.unchama.seichiassist.listener.PlayerBlockBreakListener;
import com.github.unchama.seichiassist.listener.PlayerChatEventListener;
import com.github.unchama.seichiassist.listener.PlayerClickListener;
import com.github.unchama.seichiassist.listener.PlayerDeathEventListener;
import com.github.unchama.seichiassist.listener.PlayerInventoryListener;
import com.github.unchama.seichiassist.listener.PlayerJoinListener;
import com.github.unchama.seichiassist.listener.PlayerPickupItemListener;
import com.github.unchama.seichiassist.listener.PlayerQuitListener;
import com.github.unchama.seichiassist.listener.RegionInventoryListener;
import com.github.unchama.seichiassist.listener.WorldRegenListener;
import com.github.unchama.seichiassist.listener.newyearevent.NewYearsEvent;
import com.github.unchama.seichiassist.minestack.MineStackObj;
import com.github.unchama.seichiassist.minestack.objects.MineStackBuildObj;
import com.github.unchama.seichiassist.minestack.objects.MineStackDropObj;
import com.github.unchama.seichiassist.minestack.objects.MineStackFarmObj;
import com.github.unchama.seichiassist.minestack.objects.MineStackGachaObj;
import com.github.unchama.seichiassist.minestack.objects.MineStackMineObj;
import com.github.unchama.seichiassist.minestack.objects.MineStackRsObj;
import com.github.unchama.seichiassist.task.HalfHourTaskRunnable;
import com.github.unchama.seichiassist.task.MinuteTaskRunnable;
import com.github.unchama.seichiassist.task.PlayerDataBackupTaskRunnable;
import com.github.unchama.seichiassist.task.PlayerDataSaveTaskRunnable;
import com.github.unchama.seichiassist.util.Util;


public class SeichiAssist extends JavaPlugin{

	public static SeichiAssist plugin;
	//デバッグフラグ(デバッグモード使用時はここで変更するのではなくconfig.ymlの設定値を変更すること！)
	public static Boolean DEBUG = false;
	//ガチャシステムのメンテナンスフラグ
	public static Boolean gachamente = false;

	public static final String PLAYERDATA_TABLENAME = "playerdata";
	public static final String GACHADATA_TABLENAME = "gachadata";
	public static final String DONATEDATA_TABLENAME = "donatedata";

	//MineStack用ガチャデータを作成する
	public static final String MINESTACK_GACHADATA_TABLENAME = "msgachadata";

	public static final String SEICHIWORLDNAME = "world_sw";
	public static final String DEBUGWORLDNAME = "world";

	private String pluginChannel = "BungeeCord";

	private HashMap<String, TabExecutor> commandlist;
	public static Sql sql;
	public static Config config;

	public static final int SUB_HOME_DATASIZE = 98;	//DB上でのサブホーム1つ辺りのデータサイズ　xyz各10*3+ワールド名64+区切り文字1*4

	public static final int VOTE_FAIRYTIME_DATASIZE = 17; //DB上での妖精を召喚した時間のデータサイズ　年4+月2+日2+時間2+分2+区切り文字1*5

	Random rand = new java.util.Random();
	//起動するタスクリスト
	private List<BukkitTask> tasklist = new ArrayList<BukkitTask>();

	//Gachadataに依存するデータリスト
	public static final List<GachaData> gachadatalist = new ArrayList<GachaData>();

	//(minestackに格納する)Gachadataに依存するデータリスト
	public static List<MineStackGachaData> msgachadatalist = new ArrayList<MineStackGachaData>();

	//Playerdataに依存するデータリスト
	public static final HashMap<UUID,PlayerData> playermap = new HashMap<UUID,PlayerData>();

	//総採掘量ランキング表示用データリスト
	public static final List<RankData> ranklist = new ArrayList<RankData>();

	//プレイ時間ランキング表示用データリスト
	public static final List<RankData> ranklist_playtick = new ArrayList<RankData>();

	//投票ポイント表示用データリスト
	public static final List<RankData> ranklist_p_vote = new ArrayList<RankData>();

	//プレミアムエフェクトポイント表示用データリスト
	public static final List<RankData> ranklist_premiumeffectpoint = new ArrayList<RankData>();

	//総採掘量表示用int
	public static long allplayerbreakblockint;

	//プラグインで出すエンティティの保存
	public static final List<Entity> entitylist = new ArrayList<Entity>();

	//プレイヤーがスキルで破壊するブロックリスト
	public static final List<Block> allblocklist = new ArrayList<Block>();


	//lvの閾値
	public static final List<Integer> levellist = new ArrayList<Integer>(Arrays.asList(
			0,15,49,106,198,//5
			333,705,1265,2105,3347,//10
			4589,5831,7073,8315,9557,//15
			11047,12835,14980,17554,20642,//20
			24347,28793,34128,40530,48212,//25
			57430,68491,81764,97691,116803,//30
			135915,155027,174139,193251,212363,//35
			235297,262817,295841,335469,383022,//40
			434379,489844,549746,614440,684309,//45
			759767,841261,929274,1024328,1126986,//50

			/*
			新経験値テーブル
			51-60→125,000
			61-70→175,000
			71-80→220,000
			81-90→280,000
			91-99→360,000
			99-100→800,000
			 */

			1250000,1375000,1500000,1625000,1750000,//55
			1875000,2000000,2125000,2250000,2375000,//60
			2550000,2725000,2900000,3075000,3250000,//65
			3425000,3600000,3775000,3950000,4125000,//70
			4345000,4565000,4785000,5005000,5225000,//75
			5445000,5665000,5885000,6105000,6325000,//80
			6605000,6885000,7165000,7445000,7725000,//85
			8005000,8285000,8565000,8845000,9125000,//90
			9485000,9845000,10205000,10565000,10925000,//95
			11285000,11645000,12005000,12365000,13165000,//100

			/*
			新経験値テーブル(仮)
			100-110→450,000
			110-120→490,000
			120-130→540,000
			130-140→590,000
			140-150→660,000
			150-160→740,000
			160-170→820,000
			170-180→920,000
			180-190→1,000,000
			190-199→1,150,000
			199-200→1,500,000
			 */

			13615000,14065000,14515000,14965000,15415000,//105
			15865000,16315000,16765000,17215000,17665000,//110
			18155000,18645000,19135000,19625000,20115000,//115
			20605000,21095000,21585000,22075000,22565000,//120
			23105000,23645000,24185000,24725000,25265000,//125
			25805000,26345000,26885000,27425000,27965000,//130
			28555000,29145000,29735000,30325000,30915000,//135
			31505000,32095000,32685000,33275000,33865000,//140
			34525000,35185000,35845000,36505000,37165000,//145
			37825000,38485000,39145000,39805000,40465000,//150
			41205000,41945000,42685000,43425000,44165000,//155
			44905000,45645000,46385000,47125000,47865000,//160
			48685000,49505000,50325000,51145000,51965000,//165
			52785000,53605000,54425000,55245000,56065000,//170
			56985000,57905000,58825000,59745000,60665000,//175
			61585000,62505000,63425000,64345000,65265000,//180
			66265000,67265000,68265000,69265000,70265000,//185
			71265000,72265000,73265000,74265000,75265000,//190
			76415000,77565000,78715000,79865000,81015000,//195
			82165000,83315000,84465000,85615000,87115000//200
			));

	public static final List<Integer> GBlevellist = new ArrayList<Integer>(Arrays.asList(
			20,30,40,40,50,50,60,70,80,100,
			100,110,120,130,140,150,160,170,180,200,
			250,270,300,320,350,370,400,420,450,500,
			500,600,700,800,900,1000,1100,1200,1300,1500,
			2000,3000,4000,5000,6000,7000,8000,9000,10000
			));

	private static final List<MineStackObj> minestacklistmine = new ArrayList<MineStackObj>(Arrays.asList(

			new MineStackMineObj("dirt","土",1,Material.DIRT,0)
			,new MineStackMineObj("grass","草ブロック",1,Material.GRASS,0)
			,new MineStackMineObj("cobblestone","丸石",1,Material.COBBLESTONE, 0)
			,new MineStackMineObj("stone","石",1,Material.STONE,0)
			,new MineStackMineObj("granite","花崗岩",1,Material.STONE,1)
			,new MineStackMineObj("diorite","閃緑岩",1,Material.STONE,3)
			,new MineStackMineObj("andesite","安山岩",1,Material.STONE,5)

			,new MineStackMineObj("gravel","砂利",1,Material.GRAVEL,0)
			,new MineStackMineObj("sand","砂",1,Material.SAND,0)
			,new MineStackMineObj("sandstone","砂岩",1,Material.SANDSTONE,0)

			,new MineStackMineObj("netherrack","ネザーラック",1,Material.NETHERRACK,0)
			,new MineStackMineObj("soul_sand","ソウルサンド",1,Material.SOUL_SAND,0)
			,new MineStackMineObj("coal","石炭",1,Material.COAL,0)

			//2018.6追加分
			,new MineStackMineObj("coal_1", "木炭",1,  Material.COAL, 1)
			,new MineStackMineObj("coal_ore","石炭鉱石",1,Material.COAL_ORE,0)
			,new MineStackMineObj("ender_stone","エンドストーン",1,Material.ENDER_STONE,0)
			,new MineStackMineObj("iron_ore","鉄鉱石",1,Material.IRON_ORE,0)
			,new MineStackMineObj("obsidian","黒曜石",1,Material.OBSIDIAN,0)
			,new MineStackMineObj("packed_ice","氷塊",1,Material.PACKED_ICE, 0)
			,new MineStackMineObj("quartz","ネザー水晶",1,Material.QUARTZ,0)
			,new MineStackMineObj("quartz_ore","ネザー水晶鉱石",1,Material.QUARTZ_ORE,0)
			,new MineStackMineObj("magma","マグマブロック",1,Material.MAGMA,0)
			,new MineStackMineObj("gold_ore","金鉱石",1,Material.GOLD_ORE,0)
			,new MineStackMineObj("glowstone","グロウストーン",1,Material.GLOWSTONE,0)

			,new MineStackMineObj("redstone_ore","レッドストーン鉱石",1,Material.REDSTONE_ORE,0)
			,new MineStackMineObj("lapis_lazuli","ラピスラズリ",1,Material.INK_SACK,4)
			,new MineStackMineObj("lapis_ore","ラピスラズリ鉱石",1,Material.LAPIS_ORE,0)
			,new MineStackMineObj("diamond","ダイヤモンド",1,Material.DIAMOND,0)
			,new MineStackMineObj("diamond_ore","ダイヤモンド鉱石",1,Material.DIAMOND_ORE,0)
			,new MineStackMineObj("emerald","エメラルド",1,Material.EMERALD,0)
			,new MineStackMineObj("emerald_ore","エメラルド鉱石",1,Material.EMERALD_ORE,0)

			//2018.6追加分
			,new MineStackMineObj("lapis_block", "ラピスラズリブロック", 1, Material.LAPIS_BLOCK, 0)
			,new MineStackMineObj("iron_block", "鉄ブロック", 1, Material.IRON_BLOCK, 0)
			,new MineStackMineObj("gold_block", "金ブロック", 1, Material.GOLD_BLOCK, 0)
			,new MineStackMineObj("diamond_block", "ダイヤモンドブロック", 1, Material.DIAMOND_BLOCK, 0)


			,new MineStackMineObj("red_sand","赤い砂",1,Material.SAND,1)
			,new MineStackMineObj("red_sandstone","赤い砂岩",1,Material.RED_SANDSTONE,0)
			,new MineStackMineObj("hard_clay","堅焼き粘土",1,Material.HARD_CLAY,0)

			,new MineStackMineObj("stained_clay","白色の堅焼き粘土",1,Material.STAINED_CLAY,0)
			,new MineStackMineObj("stained_clay1","橙色の堅焼き粘土",1,Material.STAINED_CLAY,1)
			,new MineStackMineObj("stained_clay4","黄色の堅焼き粘土",1,Material.STAINED_CLAY,4)
			,new MineStackMineObj("stained_clay8","薄灰色の堅焼き粘土",1,Material.STAINED_CLAY,8)
			,new MineStackMineObj("stained_clay12","茶色の堅焼き粘土",1,Material.STAINED_CLAY,12)
			,new MineStackMineObj("stained_clay14","赤色の堅焼き粘土",1,Material.STAINED_CLAY,14)
			,new MineStackMineObj("clay","粘土(ブロック)",1,Material.CLAY,0)
			,new MineStackMineObj("mossy_cobblestone","苔石",1,Material.MOSSY_COBBLESTONE,0)
			,new MineStackMineObj("ice","氷",1,Material.ICE,0)
			,new MineStackMineObj("dirt1","粗い土",1,Material.DIRT,1)
			,new MineStackMineObj("dirt2","ポドゾル",1,Material.DIRT,2)

			,new MineStackMineObj("snow_block","雪",1,Material.SNOW_BLOCK,0)
			,new MineStackMineObj("mycel","菌糸",1,Material.MYCEL,0)

			,new MineStackMineObj("bone_block","骨ブロック",1,Material.BONE_BLOCK,0)

			//2018.6追加分
			,new MineStackMineObj("sponge", "スポンジ", 1, Material.SPONGE, 0)
			,new MineStackMineObj("wet_sponge", "濡れたスポンジ", 1, Material.SPONGE, 1)

	));

	private static final List<MineStackObj> minestacklistdrop = new ArrayList<MineStackObj>(Arrays.asList(
			//以下モンスター+動物ドロップ
			new MineStackDropObj("ender_pearl","エンダーパール",1,Material.ENDER_PEARL,0)
			,new MineStackDropObj("slime_ball","スライムボール",1,Material.SLIME_BALL,0)
			,new MineStackDropObj("rotten_flesh","腐った肉",1,Material.ROTTEN_FLESH,0)
			,new MineStackDropObj("bone","骨",1,Material.BONE,0)
			,new MineStackDropObj("sulphur","火薬",1,Material.SULPHUR,0)
			,new MineStackDropObj("arrow","矢",1,Material.ARROW,0)
			,new MineStackDropObj("spider_eye","蜘蛛の目",1,Material.SPIDER_EYE,0)

			,new MineStackDropObj("string","糸",1,Material.STRING,0)

			,new MineStackDropObj("egg","卵",1,Material.EGG,0)
			,new MineStackDropObj("pork","生の豚肉",1,Material.PORK,0)
			,new MineStackDropObj("raw_chicken","生の鶏肉",1,Material.RAW_CHICKEN,0)
			,new MineStackDropObj("mutton","生の羊肉",1,Material.MUTTON,0)

			//2018.6追加分
			,new MineStackDropObj("cooked_mutton", "焼いた羊肉", 1, Material.COOKED_MUTTON, 0)

			,new MineStackDropObj("raw_beef","生の牛肉",1,Material.RAW_BEEF,0)

			//2018.6追加分
			,new MineStackDropObj("cooked_beaf", "ステーキ", 1, Material.COOKED_BEEF, 0)
			,new MineStackDropObj("rabbit", "生の兎肉", 1, Material.RABBIT, 0)
			,new MineStackDropObj("cooked_rabbit", "焼き兎肉", 1, Material.COOKED_RABBIT, 0)

			,new MineStackDropObj("raw_fish0","生魚",1,Material.RAW_FISH,0)

			//2018.6追加分
			,new MineStackDropObj("cooked_fish0", "焼き魚",1, Material.COOKED_FISH, 0)

			,new MineStackDropObj("raw_fish1","生鮭",1,Material.RAW_FISH,1)

			//2018.6追加分
			,new MineStackDropObj("cooked_fish1", "焼き鮭", 1,Material.COOKED_FISH, 1)

			,new MineStackDropObj("raw_fish2","クマノミ",1,Material.RAW_FISH,2)
			,new MineStackDropObj("raw_fish3","フグ",1,Material.RAW_FISH,3)
			,new MineStackDropObj("blaze_rod","ブレイズロッド",1,Material.BLAZE_ROD,0)
			,new MineStackDropObj("ghast_tear","ガストの涙",1,Material.GHAST_TEAR,0)
			,new MineStackDropObj("magma_cream","マグマクリーム",1,Material.MAGMA_CREAM,0)
			,new MineStackDropObj("prismarine_shard","プリズマリンの欠片",1,Material.PRISMARINE_SHARD,0)
			,new MineStackDropObj("prismarine_crystals","プリズマリンクリスタル",1,Material.PRISMARINE_CRYSTALS,0)
			,new MineStackDropObj("feather","羽",1,Material.FEATHER,0)
			,new MineStackDropObj("ink_sack0","イカスミ",1,Material.INK_SACK,0)
			,new MineStackDropObj("leather","革",1,Material.LEATHER,0)
			,new MineStackDropObj("rabbit_hide","ウサギの皮",1,Material.RABBIT_HIDE,0)
			,new MineStackDropObj("rabbit_foot","ウサギの足",1,Material.RABBIT_FOOT,0)

			//2018.6追加分
			,new MineStackDropObj("name_tag", "名札", 1, Material.NAME_TAG, 0)
			,new MineStackDropObj("lead", "リード", 1, Material.LEASH, 0)
	));

	private static final List<MineStackObj> minestacklistfarm = new ArrayList<MineStackObj>(Arrays.asList(
			//以下採掘で入手可能な農業系ブロック
			new MineStackFarmObj("log","オークの原木",1,Material.LOG,0)
			,new MineStackFarmObj("log1","マツの原木",1,Material.LOG,1)
			,new MineStackFarmObj("log2","シラカバの原木",1,Material.LOG,2)
			,new MineStackFarmObj("log3","ジャングルの原木",1,Material.LOG,3)
			,new MineStackFarmObj("log_2","アカシアの原木",1,Material.LOG_2,0)
			,new MineStackFarmObj("log_21","ダークオークの原木",1,Material.LOG_2,1)

			,new MineStackFarmObj("seeds","種",1,Material.SEEDS,0)
			,new MineStackFarmObj("apple","リンゴ",1,Material.APPLE,0)
			,new MineStackFarmObj("long_grass1","草",1,Material.LONG_GRASS,1)
			,new MineStackFarmObj("long_grass2","シダ",1,Material.LONG_GRASS,2)
			,new MineStackFarmObj("dead_bush","枯れ木",1,Material.DEAD_BUSH,0)
			,new MineStackFarmObj("cactus","サボテン",1,Material.CACTUS,0)
			,new MineStackFarmObj("vine","ツタ",1,Material.VINE,0)
			,new MineStackFarmObj("water_lily","スイレンの葉",1,Material.WATER_LILY,0)
			,new MineStackFarmObj("yellow_flower","タンポポ",1,Material.YELLOW_FLOWER,0)
			,new MineStackFarmObj("red_rose0","ポピー",1,Material.RED_ROSE,0)

			,new MineStackFarmObj("red_rose1","ヒスイラン",1,Material.RED_ROSE,1)
			,new MineStackFarmObj("red_rose2","アリウム",1,Material.RED_ROSE,2)
			,new MineStackFarmObj("red_rose3","ヒナソウ",1,Material.RED_ROSE,3)
			,new MineStackFarmObj("red_rose4","赤色のチューリップ",1,Material.RED_ROSE,4)
			,new MineStackFarmObj("red_rose5","橙色のチューリップ",1,Material.RED_ROSE,5)
			,new MineStackFarmObj("red_rose6","白色のチューリップ",1,Material.RED_ROSE,6)
			,new MineStackFarmObj("red_rose7","桃色のチューリップ",1,Material.RED_ROSE,7)
			,new MineStackFarmObj("red_rose8","フランスギク",1,Material.RED_ROSE,8)

			,new MineStackFarmObj("leaves","オークの葉",1,Material.LEAVES,0)
			,new MineStackFarmObj("leaves1","マツの葉",1,Material.LEAVES,1)
			,new MineStackFarmObj("leaves2","シラカバの葉",1,Material.LEAVES,2)
			,new MineStackFarmObj("leaves3","ジャングルの葉",1,Material.LEAVES,3)
			,new MineStackFarmObj("leaves_2","アカシアの葉",1,Material.LEAVES_2,0)
			,new MineStackFarmObj("leaves_21","ダークオークの葉",1,Material.LEAVES_2,1)

			,new MineStackFarmObj("double_plant0","ヒマワリ",1,Material.DOUBLE_PLANT,0)
			,new MineStackFarmObj("double_plant1","ライラック",1,Material.DOUBLE_PLANT,1)
			,new MineStackFarmObj("double_plant2","高い草",1,Material.DOUBLE_PLANT,2)
			,new MineStackFarmObj("double_plant3","大きなシダ",1,Material.DOUBLE_PLANT,3)
			,new MineStackFarmObj("double_plant4","バラの低木",1,Material.DOUBLE_PLANT,4)
			,new MineStackFarmObj("double_plant5","ボタン",1,Material.DOUBLE_PLANT,5)
			,new MineStackFarmObj("sugar_cane","サトウキビ",1,Material.SUGAR_CANE,0)
			,new MineStackFarmObj("pumpkin","カボチャ",1,Material.PUMPKIN,0)
			,new MineStackFarmObj("ink_sack3","カカオ豆",1,Material.INK_SACK,3)

			,new MineStackFarmObj("huge_mushroom_1","キノコ",1,Material.HUGE_MUSHROOM_1,0)
			,new MineStackFarmObj("huge_mushroom_2","キノコ",1,Material.HUGE_MUSHROOM_2,0)

			,new MineStackFarmObj("melon","スイカ",1,Material.MELON,0)
			,new MineStackFarmObj("melon_block","スイカ",1,Material.MELON_BLOCK,0)
			,new MineStackFarmObj("brown_mushroom","キノコ",1,Material.BROWN_MUSHROOM,0)
			,new MineStackFarmObj("red_mushroom","キノコ",1,Material.RED_MUSHROOM,0)

			,new MineStackFarmObj("sapling","オークの苗木",1,Material.SAPLING,0)
			,new MineStackFarmObj("sapling1","マツの苗木",1,Material.SAPLING,1)
			,new MineStackFarmObj("sapling2","シラカバの苗木",1,Material.SAPLING,2)
			,new MineStackFarmObj("sapling3","ジャングルの苗木",1,Material.SAPLING,3)
			,new MineStackFarmObj("sapling4","アカシアの苗木",1,Material.SAPLING,4)
			,new MineStackFarmObj("sapling5","ダークオークの苗木",1,Material.SAPLING,5)

			,new MineStackFarmObj("beetroot","ビートルート",1,Material.BEETROOT,0)
			,new MineStackFarmObj("beetroot_seeds","ビートルートの種",1,Material.BEETROOT_SEEDS,0)
			,new MineStackFarmObj("carrot_item","ニンジン",1,Material.CARROT_ITEM,0)
			,new MineStackFarmObj("potato_item","ジャガイモ",1,Material.POTATO_ITEM,0)
			,new MineStackFarmObj("wheat","小麦",1,Material.WHEAT,0)
			,new MineStackFarmObj("pumpkin_seeds","カボチャの種",1,Material.PUMPKIN_SEEDS,0)
			,new MineStackFarmObj("melon_seeds","スイカの種",1,Material.MELON_SEEDS,0)
			,new MineStackFarmObj("nether_stalk","ネザーウォート",1,Material.NETHER_STALK,0)
			//2018.6追加分
			,new MineStackFarmObj("chorus_fruit", "コーラスフルーツ", 1, Material.CHORUS_FRUIT, 0)
	));

	private static final List<MineStackObj> minestacklistbuild = new ArrayList<>(Arrays.asList(

			//以下建築系ブロック
			new MineStackBuildObj("step0","石ハーフブロック",1,Material.STEP,0)
			,new MineStackBuildObj("step3","丸石ハーフブロック",1,Material.STEP,3)

			,new MineStackBuildObj("polished_granite"	,"磨かれた花崗岩"	,1,Material.STONE,2)
			,new MineStackBuildObj("polished_diorite"	,"磨かれた閃緑岩"	,1,Material.STONE,4)
			,new MineStackBuildObj("polished_andesite"	,"磨かれた安山岩"	,1,Material.STONE,6)

			,new MineStackBuildObj("wood_step0","オークの木材ハーフブロック",1,Material.WOOD_STEP,0)
			,new MineStackBuildObj("wood_step1","マツの木材ハーフブロック",1,Material.WOOD_STEP,1)
			,new MineStackBuildObj("wood_step2","シラカバの木材ハーフブロック",1,Material.WOOD_STEP,2)
			,new MineStackBuildObj("wood_step3","ジャングルの木材ハーフブロック",1,Material.WOOD_STEP,3)
			,new MineStackBuildObj("wood_step4","アカシアの木材ハーフブロック",1,Material.WOOD_STEP,4)
			,new MineStackBuildObj("wood_step5","ダークオークの木材ハーフブロック",1,Material.WOOD_STEP,5)

			,new MineStackBuildObj("flint","火打石",1,Material.FLINT,0)
			,new MineStackBuildObj("step1","砂岩ハーフブロック",1,Material.STEP,1)
			,new MineStackBuildObj("glass"				,"ガラス"			,1,Material.GLASS,0)

			,new MineStackBuildObj("iron_ingot","鉄インゴット",1,Material.IRON_INGOT,0)

			,new MineStackBuildObj("nether_brick","ネザーレンガ(ブロック)",1,Material.NETHER_BRICK,0)
			,new MineStackBuildObj("nether_brick_fence","ネザーレンガのフェンス",1,Material.NETHER_FENCE,0)
			,new MineStackBuildObj("nether_brick_stairs","ネザーレンガの階段",1,Material.NETHER_BRICK_STAIRS,0)

			,new MineStackBuildObj("step6","ネザーレンガハーフブロック",1,Material.STEP,6)

			,new MineStackBuildObj("torch","松明",1,Material.TORCH,0)
			,new MineStackBuildObj("jack_o_lantern","ジャック・オ・ランタン",1,Material.JACK_O_LANTERN,0)
			,new MineStackBuildObj("nether_brick_item"	,"ネザーレンガ"		,1,Material.NETHER_BRICK_ITEM,0)

			,new MineStackBuildObj("step7","ネザー水晶ハーフブロック",1,Material.STEP,7)
			,new MineStackBuildObj("quartz_block"		,"ネザー水晶ブロック"	,1,Material.QUARTZ_BLOCK,0)

			//2018.6追加分
			,new MineStackBuildObj("quartz_block1", "模様入りネザー水晶ブロック", 1, Material.QUARTZ_BLOCK, 1)
			,new MineStackBuildObj("quartz_block2", "柱状ネザー水晶ブロック", 1, Material.QUARTZ_BLOCK, 2)

			,new MineStackBuildObj("end_bricks","エンドストーンレンガ",1,Material.END_BRICKS,0)

			,new MineStackBuildObj("purpur_block","プルパーブロック",1,Material.PURPUR_BLOCK,0)
			,new MineStackBuildObj("purpur_pillar","柱状プルパーブロック",1,Material.PURPUR_PILLAR,0)
			,new MineStackBuildObj("purpur_stairs","プルパーの階段",1,Material.PURPUR_STAIRS,0)

			,new MineStackBuildObj("purpur_slab","プルパーハーフブロック",1,Material.PURPUR_SLAB,0)

			,new MineStackBuildObj("gold_ingot","金インゴット",1,Material.GOLD_INGOT,0)

			,new MineStackBuildObj("snow_ball","雪玉",1,Material.SNOW_BALL,0)

			,new MineStackBuildObj("wood","オークの木材",1,Material.WOOD,0)
			//2018.6追加分
			,new MineStackBuildObj("wood_1", "マツの木材", 1, Material.WOOD, 1)
			,new MineStackBuildObj("wood_2", "シラカバの木材", 1, Material.WOOD, 2)
			,new MineStackBuildObj("wood_3", "ジャングルの木材", 1, Material.WOOD, 3)
			,new MineStackBuildObj("wood_4", "アカシアの木材", 1, Material.WOOD, 4)
			,new MineStackBuildObj("wood_5", "ダークオークの木材", 1, Material.WOOD, 5)
			,new MineStackBuildObj("oak_stairs", "オークの木の階段", 1, Material.WOOD_STAIRS, 0)
			,new MineStackBuildObj("stone_stairs", "丸石の階段", 1, Material.COBBLESTONE_STAIRS, 0)
			,new MineStackBuildObj("brick_stairs", "レンガの階段", 1, Material.BRICK_STAIRS, 0)
			,new MineStackBuildObj("standstone_stairs", "砂岩の階段", 1, Material.SANDSTONE_STAIRS, 0)
			,new MineStackBuildObj("spruce_stairs", "マツの木の階段", 1, Material.SPRUCE_WOOD_STAIRS, 0)
			,new MineStackBuildObj("birch_stairs", "シラカバの木の階段", 1, Material.BIRCH_WOOD_STAIRS, 0)
			,new MineStackBuildObj("jungle_stairs", "ジャングルの木の階段", 1, Material.JUNGLE_WOOD_STAIRS, 0)
			,new MineStackBuildObj("quartz_stairs", "ネザー水晶の階段", 1, Material.QUARTZ_STAIRS, 0)
			,new MineStackBuildObj("acacia_stairs", "アカシアの木の階段", 1, Material.ACACIA_STAIRS, 0)
			,new MineStackBuildObj("red_sandstone_stairs", "赤い砂岩の階段",1 ,Material.RED_SANDSTONE_STAIRS, 0)

			,new MineStackBuildObj("fence","オークのフェンス",1,Material.FENCE,0)
			,new MineStackBuildObj("dark_oak_fence","ダークオークのフェンス",1,Material.DARK_OAK_FENCE,0)
			//2018.6追加分
			,new MineStackBuildObj("spruce_fence", "マツのフェンス", 1, Material.SPRUCE_FENCE, 0)
			,new MineStackBuildObj("birch_fence", "シラカバのフェンス", 1, Material.BIRCH_FENCE, 0)
			,new MineStackBuildObj("jungle_fence", "ジャングルのフェンス", 1, Material.JUNGLE_FENCE, 0)
			,new MineStackBuildObj("acacia_fence", "アカシアのフェンス", 1, Material.ACACIA_FENCE, 0)
			,new MineStackBuildObj("cobblestone_wall_0", "丸石の壁", 1, Material.COBBLE_WALL, 0)
			,new MineStackBuildObj("cobblestone_wall_1", "苔石の壁", 1, Material.COBBLE_WALL, 1)
			,new MineStackBuildObj("bookshelf", "本棚", 1, Material.BOOKSHELF, 0)

			,new MineStackBuildObj("bucket","バケツ",1,Material.BUCKET,0)
			,new MineStackBuildObj("water_bucket","水入りバケツ",1,Material.WATER_BUCKET,0)
			,new MineStackBuildObj("lava_bucket","溶岩入りバケツ",1,Material.LAVA_BUCKET,0)
			,new MineStackBuildObj("milk_bucket","牛乳",1,Material.MILK_BUCKET,0)

			,new MineStackBuildObj("stone_slab20","赤い砂岩ハーフブロック",1,Material.STONE_SLAB2,0)

			,new MineStackBuildObj("prismarine0","プリズマリン",1,Material.PRISMARINE,0)
			,new MineStackBuildObj("prismarine1","プリズマリンレンガ",1,Material.PRISMARINE,1)
			,new MineStackBuildObj("prismarine2","ダークプリズマリン",1,Material.PRISMARINE,2)
			,new MineStackBuildObj("sea_lantern","シーランタン",1,Material.SEA_LANTERN,0)

			//2018.6追加
			,new MineStackBuildObj("end_rod", "エンドロッド", 1, Material.END_ROD, 0)

			,new MineStackBuildObj("step4","レンガハーフブロック",1,Material.STEP,4)
			,new MineStackBuildObj("clay_ball"			,"粘土"			,1,Material.CLAY_BALL,0)
			,new MineStackBuildObj("brick"				,"レンガ(ブロック)"	,1,Material.BRICK,0)
			,new MineStackBuildObj("brick_item"			,"レンガ"			,1,Material.CLAY_BRICK,0)

			,new MineStackBuildObj("web","クモの巣",1,Material.WEB,0)
			,new MineStackBuildObj("rails","レール",1,Material.RAILS,0)

			,new MineStackBuildObj("smooth_brick0","石レンガ",1,Material.SMOOTH_BRICK,0)
			,new MineStackBuildObj("smooth_brick1","苔石レンガ",1,Material.SMOOTH_BRICK,1)
			,new MineStackBuildObj("smooth_brick2","ひびの入った石レンガ",1,Material.SMOOTH_BRICK,2)
			,new MineStackBuildObj("smooth_brick3","模様入り石レンガ", 1,Material.SMOOTH_BRICK,3)

			,new MineStackBuildObj("step5","石レンガハーフブロック",1,Material.STEP,5)

			,new MineStackBuildObj("red_nether_brick"	,"赤いネザーレンガ"	,1,Material.RED_NETHER_BRICK,0)

			//2018.6追加分
			,new MineStackBuildObj("furnace", "かまど", 1, Material.FURNACE, 0)
			,new MineStackBuildObj("chest", "チェスト", 1, Material.CHEST, 0)
			,new MineStackBuildObj("bed", "ベッド", 1, Material.BED, 0)

			//2018.6追加
			,new MineStackBuildObj("wool_0", "羊毛", 1, Material.WOOL, 0)
			,new MineStackBuildObj("wool_1", "橙色の羊毛", 1, Material.WOOL, 1)
			,new MineStackBuildObj("wool_2", "赤紫色の羊毛", 1, Material.WOOL, 2)
			,new MineStackBuildObj("wool_3", "空色の羊毛", 1, Material.WOOL, 3)
			,new MineStackBuildObj("wool_4", "黄色の羊毛", 1, Material.WOOL, 4)
			,new MineStackBuildObj("wool_5", "黄緑色の羊毛", 1, Material.WOOL, 5)
			,new MineStackBuildObj("wool_6", "桃色の羊毛", 1, Material.WOOL, 6)
			,new MineStackBuildObj("wool_7", "灰色の羊毛", 1, Material.WOOL, 7)
			,new MineStackBuildObj("wool_8", "薄灰色の羊毛", 1, Material.WOOL, 8)
			,new MineStackBuildObj("wool_9", "水色の羊毛", 1, Material.WOOL, 9)
			,new MineStackBuildObj("wool_10", "紫色の羊毛", 1, Material.WOOL, 10)
			,new MineStackBuildObj("wool_11", "青色の羊毛", 1, Material.WOOL, 11)
			,new MineStackBuildObj("wool_12", "茶色の羊毛", 1, Material.WOOL, 12)
			,new MineStackBuildObj("wool_13", "緑色の羊毛", 1, Material.WOOL, 13)
			,new MineStackBuildObj("wool_14", "赤色の羊毛", 1, Material.WOOL, 14)
			,new MineStackBuildObj("wool_15", "黒色の羊毛", 1, Material.WOOL, 15)
			,new MineStackBuildObj("carpet_0", "カーペット", 1, Material.CARPET, 0)
			,new MineStackBuildObj("carpet_1", "橙色のカーペット", 1, Material.CARPET, 1)
			,new MineStackBuildObj("carpet_2", "赤紫色のカーペット", 1, Material.CARPET, 2)
			,new MineStackBuildObj("carpet_3", "空色のカーペット", 1, Material.CARPET, 3)
			,new MineStackBuildObj("carpet_4", "黄色のカーペット", 1, Material.CARPET, 4)
			,new MineStackBuildObj("carpet_5", "黄緑色のカーペット", 1, Material.CARPET, 5)
			,new MineStackBuildObj("carpet_6", "桃色のカーペット", 1, Material.CARPET, 6)
			,new MineStackBuildObj("carpet_7", "灰色のカーペット", 1, Material.CARPET, 7)
			,new MineStackBuildObj("carpet_8", "薄灰色のカーペット", 1, Material.CARPET, 8)
			,new MineStackBuildObj("carpet_9", "水色のカーペット", 1, Material.CARPET, 9)
			,new MineStackBuildObj("carpet_10", "紫色のカーペット", 1, Material.CARPET, 10)
			,new MineStackBuildObj("carpet_11", "青色のカーペット", 1, Material.CARPET, 11)
			,new MineStackBuildObj("carpet_12", "茶色のカーペット", 1, Material.CARPET, 12)
			,new MineStackBuildObj("carpet_13", "緑色のカーペット", 1, Material.CARPET, 13)
			,new MineStackBuildObj("carpet_14", "赤色のカーペット", 1, Material.CARPET, 14)
			,new MineStackBuildObj("carpet_15", "黒色のカーペット", 1, Material.CARPET, 15)
			,new MineStackBuildObj("stained_glass_0", "白色の色付きガラス", 1, Material.STAINED_GLASS, 0)
			,new MineStackBuildObj("stained_glass_1", "橙色の色付きガラス", 1, Material.STAINED_GLASS, 1)
			,new MineStackBuildObj("stained_glass_2", "赤紫色の色付きガラス", 1, Material.STAINED_GLASS, 2)
			,new MineStackBuildObj("stained_glass_3", "空色の色付きガラス", 1, Material.STAINED_GLASS, 3)
			,new MineStackBuildObj("stained_glass_4", "黄色の色付きガラス", 1, Material.STAINED_GLASS, 4)
			,new MineStackBuildObj("stained_glass_5", "黄緑色の色付きガラス", 1, Material.STAINED_GLASS, 5)
			,new MineStackBuildObj("stained_glass_6", "桃色の色付きガラス", 1, Material.STAINED_GLASS, 6)
			,new MineStackBuildObj("stained_glass_7", "灰色の色付きガラス", 1, Material.STAINED_GLASS, 7)
			,new MineStackBuildObj("stained_glass_8", "薄灰色の色付きガラス", 1, Material.STAINED_GLASS, 8)
			,new MineStackBuildObj("stained_glass_9", "水色の色付きガラス", 1, Material.STAINED_GLASS, 9)
			,new MineStackBuildObj("stained_glass_10", "紫色の色付きガラス", 1, Material.STAINED_GLASS, 10)
			,new MineStackBuildObj("stained_glass_11", "青色の色付きガラス", 1, Material.STAINED_GLASS, 11)
			,new MineStackBuildObj("stained_glass_12", "茶色の色付きガラス", 1, Material.STAINED_GLASS, 12)
			,new MineStackBuildObj("stained_glass_13", "緑色の色付きガラス", 1, Material.STAINED_GLASS, 13)
			,new MineStackBuildObj("stained_glass_14", "赤色の色付きガラス", 1, Material.STAINED_GLASS, 14)
			,new MineStackBuildObj("stained_glass_15", "黒色の色付きガラス", 1, Material.STAINED_GLASS, 15)
			,new MineStackBuildObj("glass_panel", "板ガラス", 1, Material.THIN_GLASS, 0)
			,new MineStackBuildObj("glass_panel_0", "白色の色付きガラス版", 1, Material.STAINED_GLASS_PANE, 0)
			,new MineStackBuildObj("glass_panel_1", "橙色の色付きガラス版", 1, Material.STAINED_GLASS_PANE, 1)
			,new MineStackBuildObj("glass_panel_2", "赤紫色の色付きガラス版", 1, Material.STAINED_GLASS_PANE, 2)
			,new MineStackBuildObj("glass_panel_3", "空色の色付きガラス版", 1, Material.STAINED_GLASS_PANE, 3)
			,new MineStackBuildObj("glass_panel_4", "黄色の色付きガラス版", 1, Material.STAINED_GLASS_PANE, 4)
			,new MineStackBuildObj("glass_panel_5", "黄緑色の色付きガラス版", 1, Material.STAINED_GLASS_PANE, 5)
			,new MineStackBuildObj("glass_panel_6", "桃色の色付きガラス版", 1, Material.STAINED_GLASS_PANE, 6)
			,new MineStackBuildObj("glass_panel_7", "灰色の色付きガラス版", 1, Material.STAINED_GLASS_PANE, 7)
			,new MineStackBuildObj("glass_panel_8", "薄灰色の色付きガラス版", 1, Material.STAINED_GLASS_PANE, 8)
			,new MineStackBuildObj("glass_panel_9", "水色の色付きガラス版", 1, Material.STAINED_GLASS_PANE, 9)
			,new MineStackBuildObj("glass_panel_10", "紫色の色付きガラス版", 1, Material.STAINED_GLASS_PANE, 10)
			,new MineStackBuildObj("glass_panel_11", "青色の色付きガラス版", 1, Material.STAINED_GLASS_PANE, 11)
			,new MineStackBuildObj("glass_panel_12", "茶色の色付きガラス版", 1, Material.STAINED_GLASS_PANE, 12)
			,new MineStackBuildObj("glass_panel_13", "緑色の色付きガラス版", 1, Material.STAINED_GLASS_PANE, 13)
			,new MineStackBuildObj("glass_panel_14", "赤色の色付きガラス版", 1, Material.STAINED_GLASS_PANE, 14)
			,new MineStackBuildObj("glass_panel_15", "黒色の色付きガラス版", 1, Material.STAINED_GLASS_PANE, 15)
			,new MineStackBuildObj("dye_1", "赤色の染料", 1, Material.INK_SACK, 1)
			,new MineStackBuildObj("dye_2", "緑色の染料", 1, Material.INK_SACK, 2)
			,new MineStackBuildObj("dye_5", "紫色の染料", 1, Material.INK_SACK, 5)
			,new MineStackBuildObj("dye_6", "水色の染料", 1, Material.INK_SACK, 6)
			,new MineStackBuildObj("dye_7", "薄灰色の染料", 1, Material.INK_SACK, 7)
			,new MineStackBuildObj("dye_8", "灰色の染料", 1, Material.INK_SACK, 8)
			,new MineStackBuildObj("dye_9", "桃色の染料", 1, Material.INK_SACK, 9)
			,new MineStackBuildObj("dye_10", "黄緑色の染料", 1, Material.INK_SACK, 10)
			,new MineStackBuildObj("dye_11", "黄色の染料", 1, Material.INK_SACK, 11)
			,new MineStackBuildObj("dye_12", "空色の染料",1, Material.INK_SACK, 12)
			,new MineStackBuildObj("dye_13", "赤紫色の染料", 1, Material.INK_SACK, 13)
			,new MineStackBuildObj("dye_14", "橙色の染料", 1, Material.INK_SACK, 14)
			,new MineStackBuildObj("dye_15", "骨粉", 1, Material.INK_SACK, 15)


	));

	private static final List<MineStackObj> minestacklistrs = new ArrayList<>(Arrays.asList(

			//以下レッドストーン系ブロック
			new MineStackRsObj("redstone","レッドストーン",1,Material.REDSTONE,0)
			,new MineStackRsObj("stone_button","石のボタン",1,Material.STONE_BUTTON,0)
			,new MineStackRsObj("wood_button","木のボタン",1,Material.WOOD_BUTTON,0)
			,new MineStackRsObj("stone_plate","石の感圧版",1,Material.STONE_PLATE,0)
			,new MineStackRsObj("wood_plate","木の感圧版",1,Material.WOOD_PLATE,0)
			,new MineStackRsObj("fence_gate","オークのフェンスゲート",1,Material.FENCE_GATE,0)
			,new MineStackRsObj("spruce_fence_gate","マツのフェンスゲート",1,Material.SPRUCE_FENCE_GATE,0)
			,new MineStackRsObj("birch_fence_gate","シラカバのフェンスゲート",1,Material.BIRCH_FENCE_GATE,0)
			,new MineStackRsObj("jungle_fence_gate","ジャングルのフェンスゲート",1,Material.JUNGLE_FENCE_GATE,0)
			,new MineStackRsObj("dark_oak_fence_gate","ダークオークのフェンスゲート",1,Material.DARK_OAK_FENCE_GATE,0)
			,new MineStackRsObj("acacia_fence_gate","アカシアのフェンスゲート",1,Material.ACACIA_FENCE_GATE,0)
			,new MineStackRsObj("redstone_block","レッドストーンブロック",1,Material.REDSTONE_BLOCK,0)
			,new MineStackRsObj("lever","レバー",1,Material.LEVER,0)
			,new MineStackRsObj("redstone_torch_on","レッドストーントーチ",1,Material.REDSTONE_TORCH_ON,0)
			,new MineStackRsObj("trap_door","木のトラップドア",1,Material.TRAP_DOOR,0)
			,new MineStackRsObj("stone_button","鉄のトラップドア",1,Material.IRON_TRAPDOOR,0)
			,new MineStackRsObj("gold_plate","重量感圧版 (軽) ",1,Material.GOLD_PLATE,0)
			,new MineStackRsObj("iron_plate","重量感圧版 (重) ",1,Material.IRON_PLATE,0)
			,new MineStackRsObj("wood_door","オークのドア",1,Material.WOOD_DOOR,0)
			,new MineStackRsObj("spruce_door_item","マツのドア",1,Material.SPRUCE_DOOR_ITEM,0)
			,new MineStackRsObj("birch_door_item","シラカバのドア",1,Material.BIRCH_DOOR_ITEM,0)
			,new MineStackRsObj("jungle_door_item","ジャングルのドア",1,Material.JUNGLE_DOOR_ITEM,0)
			,new MineStackRsObj("acacia_door_item","アカシアのドア",1,Material.ACACIA_DOOR_ITEM,0)
			,new MineStackRsObj("dark_oak_door_item","ダークオークのドア",1,Material.DARK_OAK_DOOR_ITEM,0)
			,new MineStackRsObj("note_block","音符ブロック",1,Material.NOTE_BLOCK,0)
			,new MineStackRsObj("redstone_lamp_off","レッドストーンランプ",1,Material.REDSTONE_LAMP_OFF,0)
			,new MineStackRsObj("tripwire_hook","トリップワイヤーフック",1,Material.TRIPWIRE_HOOK,0)
			,new MineStackRsObj("dropper","ドロッパー",1,Material.DROPPER,0)
			,new MineStackRsObj("piston_sticky_base","粘着ピストン",1,Material.PISTON_STICKY_BASE,0)
			,new MineStackRsObj("piston_base","ピストン",1,Material.PISTON_BASE,0)
			,new MineStackRsObj("tnt","TNT",1,Material.TNT,0)
			,new MineStackRsObj("trapped_chest","トラップチェスト",1,Material.TRAPPED_CHEST,0)
			,new MineStackRsObj("daylight_detector","日照センサー",1,Material.DAYLIGHT_DETECTOR,0)
			,new MineStackRsObj("iron_door","鉄のドア",1,Material.IRON_DOOR,0)
			,new MineStackRsObj("diode","レッドストーンリピーター",1,Material.DIODE,0)
			,new MineStackRsObj("dispenser","ディスペンサー",1,Material.DISPENSER,0)
			,new MineStackRsObj("hopper","ホッパー",1,Material.HOPPER,0)
			,new MineStackRsObj("redstone_comparator","レッドストーンコンパレーター",1,Material.REDSTONE_COMPARATOR,0)

			//2018.6追加分
			,new MineStackRsObj("powered_rail", "パワードレール", 1, Material.POWERED_RAIL, 0)
			,new MineStackRsObj("detector_rail", "ディテクターレール", 1, Material.DETECTOR_RAIL, 0)
			,new MineStackRsObj("activator_rail", "アクティベーターレール", 1, Material.ACTIVATOR_RAIL, 0)
			,new MineStackRsObj("boat", "オークのボート", 1, Material.BOAT, 0)
			,new MineStackRsObj("spruce_boat", "マツのボート", 1, Material.BOAT_SPRUCE, 0)
			,new MineStackRsObj("birch_boat", "シラカバのボート", 1, Material.BOAT_BIRCH, 0)
			,new MineStackRsObj("jungle_boat", "ジャングルのボート", 1, Material.BOAT_JUNGLE, 0)
			,new MineStackRsObj("acacia_boat", "アカシアのボート", 1, Material.BOAT_ACACIA, 0)
			,new MineStackRsObj("dark_oak_boat", "ダークオークのボート", 1, Material.BOAT_DARK_OAK, 0)
			,new MineStackRsObj("saddle", "サドル", 1, Material.SADDLE, 0)
			,new MineStackRsObj("minecart", "トロッコ", 1, Material.MINECART, 0)
			,new MineStackRsObj("chest_minecart", "チェスト付きトロッコ", 1, Material.STORAGE_MINECART, 0)
			,new MineStackRsObj("furnace_minecart", "かまど付きトロッコ", 1, Material.POWERED_MINECART, 0)
			,new MineStackRsObj("hopper_minecart", "ホッパー付きトロッコ", 1, Material.HOPPER_MINECART, 0)
			,new MineStackRsObj("iron_horse_armor", "鉄の馬鎧", 1, Material.IRON_BARDING, 0)
			,new MineStackRsObj("golden_horse_armor", "金の馬鎧", 1, Material.GOLD_BARDING, 0)
			,new MineStackRsObj("diamond_horse_armor", "ダイヤの馬鎧", 1, Material.DIAMOND_BARDING, 0)
			,new MineStackRsObj("record_13", "レコード", 1, Material.GOLD_RECORD, 0)
			,new MineStackRsObj("record_cat", "レコード", 1, Material.GREEN_RECORD, 0)
			,new MineStackRsObj("record_blocks", "レコード", 1, Material.RECORD_3, 0)
			,new MineStackRsObj("record_chirp", "レコード", 1, Material.RECORD_4, 0)
			,new MineStackRsObj("record_far", "レコード", 1, Material.RECORD_5, 0)
			,new MineStackRsObj("record_mall", "レコード", 1, Material.RECORD_6, 0)
			,new MineStackRsObj("record_mellohi", "レコード", 1, Material.RECORD_7, 0)
			,new MineStackRsObj("record_stal", "レコード", 1, Material.RECORD_8, 0)
			,new MineStackRsObj("record_strad", "レコード", 1, Material.RECORD_9, 0)
			,new MineStackRsObj("record_ward", "レコード", 1, Material.RECORD_10, 0)
			,new MineStackRsObj("record_11", "レコード", 1, Material.RECORD_11, 0)
			,new MineStackRsObj("record_wait", "レコード", 1, Material.RECORD_12, 0)


	));

	private static List<MineStackObj> minestacklistgacha =  new ArrayList<>(Arrays.asList(

			//以下ガチャ系アイテム
			new MineStackGachaObj("gachaimo",Util.getGachaimoName(),1,Material.GOLDEN_APPLE,0,Util.getGachaimoLore())
			,new MineStackGachaObj("exp_bottle","エンチャントの瓶",1,Material.EXP_BOTTLE,0)

			//2018.6追加分

			//,new MineStackGachaObj("change_paper", ChatColor.DARK_RED + "" + ChatColor.BOLD + "交換券", 1, Material.PAPER, 0, -2,  Enchantment.PROTECTION_FIRE)

	));

	public static List<MineStackObj> minestacklistgacha1 = null;


	public static List<MineStackObj> minestacklist = null;

	//public static final int minestacksize=minestacklist.size();
	public static final boolean minestack_sql_enable=true; //ここは必ずtrue(falseのときはSQL初期設定+SQL入出力しない[デバッグ用])


	public static final List<Material> materiallist = new ArrayList<Material>(Arrays.asList(
			Material.STONE,Material.NETHERRACK,Material.NETHER_BRICK,Material.DIRT
			,Material.GRAVEL,Material.LOG,Material.LOG_2,Material.GRASS
			,Material.COAL_ORE,Material.IRON_ORE,Material.GOLD_ORE,Material.DIAMOND_ORE
			,Material.LAPIS_ORE,Material.EMERALD_ORE,Material.REDSTONE_ORE,Material.GLOWING_REDSTONE_ORE,Material.SAND
			,Material.SANDSTONE,Material.QUARTZ_ORE,Material.END_BRICKS,Material.ENDER_STONE
			,Material.ICE,Material.PACKED_ICE,Material.OBSIDIAN,Material.MAGMA,Material.SOUL_SAND,Material.LEAVES,Material.LEAVES_2
			,Material.CLAY,Material.STAINED_CLAY,Material.COBBLESTONE,Material.MOSSY_COBBLESTONE,Material.HARD_CLAY
			,Material.MONSTER_EGGS,Material.WEB,Material.WOOD,Material.FENCE,Material.DARK_OAK_FENCE,Material.RAILS //追加
			,Material.MYCEL,Material.SNOW_BLOCK,Material.HUGE_MUSHROOM_1,Material.HUGE_MUSHROOM_2,Material.BONE_BLOCK //追加
			,Material.PURPUR_BLOCK,Material.PURPUR_PILLAR,Material.SEA_LANTERN,Material.PRISMARINE //追加
			,Material.SMOOTH_BRICK,Material.GLOWSTONE //追加
			,Material.STAINED_GLASS,Material.STAINED_GLASS_PANE,Material.THIN_GLASS,Material.GLASS
			,Material.WOOD_STAIRS,Material.BIRCH_WOOD_STAIRS,Material.SPRUCE_WOOD_STAIRS,Material.ACACIA_STAIRS,Material.DARK_OAK_STAIRS
			,Material.BIRCH_FENCE,Material.SPRUCE_FENCE,Material.ACACIA_FENCE
			,Material.FENCE_GATE,Material.BIRCH_FENCE_GATE,Material.SPRUCE_FENCE_GATE,Material.ACACIA_FENCE_GATE,Material.DARK_OAK_FENCE_GATE
			,Material.COBBLESTONE_STAIRS,Material.SANDSTONE_STAIRS,Material.BRICK_STAIRS,Material.QUARTZ_STAIRS
			,Material.BOOKSHELF,Material.IRON_FENCE,Material.ICE,Material.WOOL,Material.GOLD_BLOCK
			,Material.END_ROD,Material.PUMPKIN,Material.MELON_BLOCK,Material.STONE_SLAB2,Material.SPONGE
			,Material.SOIL,Material.GRASS_PATH,Material.MOB_SPAWNER,Material.WORKBENCH,Material.FURNACE
			,Material.QUARTZ_BLOCK
			,Material.CHEST
			));
	public static final List<Material> luckmateriallist = new ArrayList<Material>(Arrays.asList(
			Material.COAL_ORE,Material.DIAMOND_ORE,Material.LAPIS_ORE,Material.EMERALD_ORE,
			Material.REDSTONE_ORE,Material.GLOWING_REDSTONE_ORE,Material.QUARTZ_ORE
			));
	public static final List<Material> breakmateriallist = new ArrayList<Material>(Arrays.asList(
			Material.DIAMOND_PICKAXE,Material.DIAMOND_AXE,Material.DIAMOND_SPADE,
			Material.WOOD_PICKAXE,						  Material.WOOD_SPADE,
			Material.IRON_PICKAXE,Material.IRON_AXE,Material.IRON_SPADE,
			Material.GOLD_PICKAXE,Material.GOLD_AXE,Material.GOLD_SPADE
			));
	public static final List<Material> cancelledmateriallist = new ArrayList<Material>(Arrays.asList(
			Material.CHEST,Material.ENDER_CHEST,Material.TRAPPED_CHEST,Material.ANVIL,Material.ARMOR_STAND
			,Material.BEACON,Material.BIRCH_DOOR,Material.BIRCH_FENCE_GATE,Material.BIRCH_WOOD_STAIRS
			,Material.BOAT,Material.FURNACE,Material.WORKBENCH,Material.HOPPER,Material.MINECART
			));

	public static final Set<Material> transparentmateriallist = new HashSet<Material>(Arrays.asList(
			Material.BEDROCK,Material.AIR
			));
	public static final List<Material> gravitymateriallist = new ArrayList<Material>(Arrays.asList(
			Material.LOG, Material.LOG_2,Material.LEAVES,Material.LEAVES_2
			));
	//スキル破壊ブロック分のcoreprotectログ保存処理を除外するワールドリスト(coreprotectログデータ肥大化の軽減が目的)
	//スキル自体はメインワールドと各整地ワールドのみ(world_SWで始まるワールドのみ)で発動する(ここの設定は無視する)
	public static final List<String> ignoreWorldlist = new ArrayList<String>(Arrays.asList(
			"world_SW","world_SW_2","world_SW_3","world_SW_nether","world_SW_the_end"
			));

	//保護を掛けて整地するワールドのリスト
	public static final List<String> rgSeichiWorldlist = new ArrayList<String>(Arrays.asList(
			"world_SW_2","world_SW_nether","world_SW_the_end"
			));

	public static final List<String> seichiWorldList = new ArrayList<>(Arrays.asList(
	        "world_SW", "world_SW_2", "world_SW_3", "world_SW_nether", "world_SW_the_end"
    ));

	@Override
	public void onEnable(){
		plugin = this;

		//チャンネルを追加
		Bukkit.getMessenger().registerOutgoingPluginChannel(this,
				this.pluginChannel);

		//コンフィグ系の設定は全てConfig.javaに移動
		config = new Config(this);
		config.loadConfig();

		if(SeichiAssist.config.getDebugMode()==1){
			//debugmode=1の時は最初からデバッグモードで鯖を起動
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "seichiassistをデバッグモードで起動します");
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "コンソールから/seichi debugmode");
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "を実行するといつでもONOFFを切り替えられます");
			DEBUG = true;
		}else{
			//debugmode=0の時は/seichi debugmodeによる変更コマンドも使えない
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "seichiassistを通常モードで起動します");
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "デバッグモードを使用する場合は");
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "config.ymlの設定値を書き換えて再起動してください");
		}

		//MySQL系の設定はすべてSql.javaに移動
		sql = new Sql(this,config.getURL(), config.getDB(), config.getID(), config.getPW());
		if(!sql.connect()){
			getLogger().info("データベース初期処理にエラーが発生しました");
		}

		//mysqlからガチャデータ読み込み
		if(!sql.loadGachaData()){
			getLogger().info("ガチャデータのロードに失敗しました");
		} else { //ガチャデータを読み込んだ

		}

		//リスト結合(ガチャ品(ガチャリンゴなど)+ガチャ品(本体))
		minestacklistgacha1 = new ArrayList<MineStackObj>();

		//mysqlからMineStack用ガチャデータ読み込み
		if(!sql.loadMineStackGachaData()){
			getLogger().info("MineStack用ガチャデータのロードに失敗しました");
			//minestacklist.addAll(minestacklistbase);
		} else { //MineStack用ガチャデータを読み込んだ
			getLogger().info("MineStack用ガチャデータのロードに成功しました");
			minestacklistgacha1 = creategachaminestacklist();


			//minestacklist.addAll(minestacklistbase);
			minestacklistgacha.addAll(minestacklistgacha1);

			minestacklist = new ArrayList<MineStackObj>();
			minestacklist.addAll(minestacklistmine);
			minestacklist.addAll(minestacklistdrop);
			minestacklist.addAll(minestacklistfarm);
			minestacklist.addAll(minestacklistbuild);
			minestacklist.addAll(minestacklistrs);

			Collections.sort(minestacklistgacha);

			minestacklist.addAll(minestacklistgacha);

		}

		if(!sql.connect1()){
			getLogger().info("データベース初期処理にエラーが発生しました");
		}



		//

		//コマンドの登録
		commandlist = new HashMap<String, TabExecutor>();
		commandlist.put("gacha",new gachaCommand(plugin));
		commandlist.put("seichi",new seichiCommand(plugin));
		commandlist.put("ef",new effectCommand(plugin));
		commandlist.put("level",new levelCommand(plugin));
		commandlist.put("lastquit",new lastquitCommand(plugin));
		commandlist.put("stick",new stickCommand(plugin));
		commandlist.put("rmp",new rmpCommand(plugin));
		commandlist.put("shareinv",new shareinvCommand(plugin));
		commandlist.put("mebius",new mebiusCommand(plugin));
		commandlist.put("unlockachv", new AchieveCommand(plugin));
		commandlist.put("halfguard", new HalfBlockProtectCommand(plugin));
		commandlist.put("event", new EventCommand(plugin));
		commandlist.put("contribute", new contributeCommand(plugin));
		commandlist.put("subhome", new subHomeCommand(plugin));
		commandlist.put("gtfever", new GiganticFeverCommand());

		//リスナーの登録
		getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerClickListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerChatEventListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerBlockBreakListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerInventoryListener(), this);
		getServer().getPluginManager().registerEvents(new EntityListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerPickupItemListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerDeathEventListener(), this);
		getServer().getPluginManager().registerEvents(new GachaItemListener(), this);
		getServer().getPluginManager().registerEvents(new MebiusListener(), this);
		getServer().getPluginManager().registerEvents(new RegionInventoryListener(), this);
		getServer().getPluginManager().registerEvents(new WorldRegenListener(), this);
		//正月イベント用
		new NewYearsEvent(this);
		// マナ自動回復用リスナー…無効化中
		// getServer().getPluginManager().registerEvents(new PlayerMoveListener(), this);
		// BungeeCordとのI/F
		Bukkit.getMessenger().registerIncomingPluginChannel(this, "SeichiAssistBungee", new BungeeReceiver(this));
		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "SeichiAssistBungee");


		//オンラインの全てのプレイヤーを処理
		for(Player p : getServer().getOnlinePlayers()){
			//プレイヤーデータを生成
			sql.loadPlayerData(new PlayerData(p));
		}

		//ランキングデータをセット
		if(!sql.setRanking()){
			getLogger().info("ランキングデータの作成に失敗しました");
		}

		if(!sql.setRanking_playtick()){
			getLogger().info("ランキングデータの作成に失敗しました");
		}

		if(!sql.setRanking_p_vote()){
			getLogger().info("ランキングデータの作成に失敗しました");
		}

		if(!sql.setRanking_premiumeffectpoint()){
			getLogger().info("ランキングデータの作成に失敗しました");
		}

		//タスクスタート
		startTaskRunnable();

		getLogger().info("SeichiAssist is Enabled!");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		return commandlist.get(cmd.getName()).onCommand(sender, cmd, label, args);
	}

	@Override
	public void onDisable(){
		//全てのタスクをキャンセル
		stopAllTaskRunnable();

		//全てのエンティティを削除
		for(Entity e :entitylist){
			e.remove();
		}

		//全てのスキルで破壊されるブロックを強制破壊
		for(Block b : allblocklist){
			b.setType(Material.AIR);
		}

		//sqlコネクションチェック
		sql.checkConnection();
		for(Player p : getServer().getOnlinePlayers()){
			//UUIDを取得
			UUID uuid = p.getUniqueId();
			//プレイヤーデータ取得
			PlayerData playerdata = playermap.get(uuid);
			//念のためエラー分岐
			if(playerdata == null){
				p.sendMessage(ChatColor.RED + "playerdataの保存に失敗しました。管理者に報告してください");
				getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[Ondisable処理]でエラー発生");
				getLogger().warning(Util.getName(p)+ "のplayerdataの保存失敗。開発者に報告してください");
				continue;
			}
			//quit時とondisable時、プレイヤーデータを最新の状態に更新
			playerdata.updateonQuit(p);

			new PlayerDataSaveTaskRunnable(playerdata,true,true).run();
		}



		/*
		for(PlayerData playerdata : playermap.values()){
			if(!sql.savePlayerData(playerdata)){
				getLogger().info(playerdata.name + "のデータ保存に失敗しました");
			}
		}
		*/


		/* マルチサーバー対応の為コメントアウト
		if(!sql.saveGachaData()){
			getLogger().info("ガチャデータ保存に失敗しました");
		}
		*/

		//マルチサーバー対応の為コメントアウト
		/*
		if(!sql.saveMineStackGachaData()){
			getLogger().info("MineStack用ガチャデータ保存に失敗しました");
		}
		*/

		if(!sql.disconnect()){
			getLogger().info("データベース切断に失敗しました");
		}

		getLogger().info("SeichiAssist is Disabled!");
	}

	public void startTaskRunnable(){
		//一定時間おきに処理を実行するタスク
		if(DEBUG){
			tasklist.add(new HalfHourTaskRunnable().runTaskTimer(this,440,400));
		}else{
			tasklist.add(new HalfHourTaskRunnable().runTaskTimer(this,36400,36000));
		}

		if(DEBUG){
			tasklist.add(new MinuteTaskRunnable().runTaskTimer(this,0,200));
		}else{
			tasklist.add(new MinuteTaskRunnable().runTaskTimer(this,0,1200));
		}

		//非同期処理にしたいけど別ステートメントでsql文処理させるようにしてからじゃないとだめぽ
		if(DEBUG){
			tasklist.add(new PlayerDataBackupTaskRunnable().runTaskTimer(this,480,400));
		}else{
			tasklist.add(new PlayerDataBackupTaskRunnable().runTaskTimer(this,12800,12000));
		}
	}

	public void stopAllTaskRunnable(){
		for(BukkitTask task:tasklist){
			task.cancel();
		}
	}

	private static List<MineStackObj> creategachaminestacklist(){
		List<MineStackObj> minestacklist = new ArrayList<MineStackObj>();
		for(int i=0; i<SeichiAssist.msgachadatalist.size(); i++){
			MineStackGachaData g = SeichiAssist.msgachadatalist.get(i);
			//int levelsidx = 0;
			//System.out.println("Debug A");
				if(!g.itemstack.getType().equals(Material.EXP_BOTTLE)){ //経験値瓶だけはすでにリストにあるので除外
					/*
					minestacklist.add(new MineStackObj(g.obj_name,g.itemstack.getItemMeta().getDisplayName(),g.level,g.itemstack.getType(),g.itemstack.getDurability(),true,i,g.itemstack.getItemMeta().getLore()));
					*/
					minestacklist.add(new MineStackObj(g.obj_name,g.level,g.itemstack,true,i,5));
					//System.out.println("Debug C");


				}
		}
		return minestacklist;
	}


}


