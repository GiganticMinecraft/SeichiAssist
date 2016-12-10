package com.github.unchama.seichiassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

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

import com.github.unchama.seichiassist.commands.effectCommand;
import com.github.unchama.seichiassist.commands.gachaCommand;
import com.github.unchama.seichiassist.commands.lastquitCommand;
import com.github.unchama.seichiassist.commands.levelCommand;
import com.github.unchama.seichiassist.commands.seichiCommand;
import com.github.unchama.seichiassist.data.GachaData;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.RankData;
import com.github.unchama.seichiassist.listener.EntityListener;
import com.github.unchama.seichiassist.listener.GachaItemListener;
import com.github.unchama.seichiassist.listener.PlayerBlockBreakListener;
import com.github.unchama.seichiassist.listener.PlayerClickListener;
import com.github.unchama.seichiassist.listener.PlayerDeathEventListener;
import com.github.unchama.seichiassist.listener.PlayerInventoryListener;
import com.github.unchama.seichiassist.listener.PlayerJoinListener;
import com.github.unchama.seichiassist.listener.PlayerPickupItemListener;
import com.github.unchama.seichiassist.listener.PlayerQuitListener;
import com.github.unchama.seichiassist.task.HalfHourTaskRunnable;
import com.github.unchama.seichiassist.task.MinuteTaskRunnable;
import com.github.unchama.seichiassist.task.PlayerDataBackupTaskRunnable;
import com.github.unchama.seichiassist.task.PlayerDataSaveTaskRunnable;
import com.github.unchama.seichiassist.util.Util;


public class SeichiAssist extends JavaPlugin{

	public static SeichiAssist plugin;
	//デバッグフラグ
	public static Boolean DEBUG = false;
	//ガチャシステムのメンテナンスフラグ
	public static Boolean gachamente = false;

	public static final String PLAYERDATA_TABLENAME = "playerdata";
	public static final String GACHADATA_TABLENAME = "gachadata";
	public static final String DONATEDATA_TABLENAME = "donatedata";

	public static final String SEICHIWORLDNAME = "world_sw";
	public static final String DEBUGWORLDNAME = "world";

	private HashMap<String, TabExecutor> commandlist;
	public static Sql sql;
	public static Config config;

	public static final int SUB_HOME_DATASIZE = 98;	//DB上でのサブホーム1つ辺りのデータサイズ　xyz各10*3+ワールド名64+区切り文字1*4

	Random rand = new java.util.Random();
	//起動するタスクリスト
	private List<BukkitTask> tasklist = new ArrayList<BukkitTask>();

	//Gachadataに依存するデータリスト
	public static final List<GachaData> gachadatalist = new ArrayList<GachaData>();

	//Playerdataに依存するデータリスト
	public static final HashMap<UUID,PlayerData> playermap = new HashMap<UUID,PlayerData>();

	//総採掘量ランキング表示用データリスト
	public static final List<RankData> ranklist = new ArrayList<RankData>();

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
			25805000,26345000,26885000,27245000,27965000,//130
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

	public static final List<MineStackObj> minestacklist = new ArrayList<MineStackObj>(Arrays.asList(

			new MineStackObj("dirt","土",1,Material.DIRT,0,false,-1)
			,new MineStackObj("grass","草ブロック",1,Material.GRASS,0,false,-1)
			,new MineStackObj("cobblestone","丸石",2,Material.COBBLESTONE,0,false,-1)
			,new MineStackObj("stone","石",2,Material.STONE,0,false,-1)
			,new MineStackObj("granite","花崗岩",3,Material.STONE,1,false,-1)
			,new MineStackObj("diorite","閃緑岩",3,Material.STONE,3,false,-1)
			,new MineStackObj("andesite","安山岩",3,Material.STONE,5,false,-1)

			,new MineStackObj("log","オークの原木",4,Material.LOG,0,false,-1)
			,new MineStackObj("log1","マツの原木",4,Material.LOG,1,false,-1)
			,new MineStackObj("log2","シラカバの原木",4,Material.LOG,2,false,-1)
			,new MineStackObj("log3","ジャングルの原木",4,Material.LOG,3,false,-1)
			,new MineStackObj("log_2","アカシアの原木",4,Material.LOG_2,0,false,-1)
			,new MineStackObj("log_21","ダークオークの原木",4,Material.LOG_2,1,false,-1)

			,new MineStackObj("gravel","砂利",5,Material.GRAVEL,0,false,-1)
			,new MineStackObj("sand","砂",5,Material.SAND,0,false,-1)

			,new MineStackObj("sandstone","砂岩",5,Material.SANDSTONE,0,false,-1)

			,new MineStackObj("netherrack","ネザーラック",6,Material.NETHERRACK,0,false,-1)
			,new MineStackObj("soul_sand","ソウルサンド",6,Material.SOUL_SAND,0,false,-1)
			,new MineStackObj("coal","石炭",7,Material.COAL,0,false,-1)
			,new MineStackObj("coal_ore","石炭鉱石",7,Material.COAL_ORE,0,false,-1)

			,new MineStackObj("ender_stone","エンドストーン",8,Material.ENDER_STONE,0,false,-1)
			,new MineStackObj("iron_ore","鉄鉱石",9,Material.IRON_ORE,0,false,-1)

			,new MineStackObj("obsidian","黒曜石",9,Material.OBSIDIAN,0,false,-1)

			,new MineStackObj("packed_ice","氷塊",10,Material.PACKED_ICE,0,false,-1)

			,new MineStackObj("quartz","ネザー水晶",11,Material.QUARTZ,0,false,-1)
			,new MineStackObj("quartz_ore","ネザー水晶鉱石",11,Material.QUARTZ_ORE,0,false,-1)

			,new MineStackObj("magma","マグマブロック",12,Material.MAGMA,0,false,-1)
			,new MineStackObj("gold_ore","金鉱石",13,Material.GOLD_ORE,0,false,-1)
			,new MineStackObj("glowstone","グロウストーン",13,Material.GLOWSTONE,0,false,-1)

			,new MineStackObj("wood","オークの木材",14,Material.WOOD,0,false,-1)
			,new MineStackObj("fence","オークのフェンス",14,Material.FENCE,0,false,-1)
			,new MineStackObj("redstone","レッドストーン",15,Material.REDSTONE,0,false,-1)
			,new MineStackObj("redstone_ore","レッドストーン鉱石",15,Material.REDSTONE_ORE,0,false,-1)

			,new MineStackObj("lapis_lazuli","ラピスラズリ",16,Material.INK_SACK,4,false,-1)
			,new MineStackObj("lapis_ore","ラピスラズリ鉱石",16,Material.LAPIS_ORE,0,false,-1)

			,new MineStackObj("diamond","ダイヤモンド",17,Material.DIAMOND,0,false,-1)
			,new MineStackObj("diamond_ore","ダイヤモンド鉱石",17,Material.DIAMOND_ORE,0,false,-1)

			,new MineStackObj("emerald","エメラルド",18,Material.EMERALD,0,false,-1)
			,new MineStackObj("emerald_ore","エメラルド鉱石",18,Material.EMERALD_ORE,0,false,-1)

			,new MineStackObj("gachaimo","がちゃりんご",19,Material.GOLDEN_APPLE,0,true,-1)
			,new MineStackObj("exp_bottle","エンチャントの瓶",19,Material.EXP_BOTTLE,0,false,-1)

			,new MineStackObj("red_sand","赤い砂",20,Material.SAND,1,false,-1)
			,new MineStackObj("red_sandstone","赤い砂岩",20,Material.RED_SANDSTONE,0,false,-1)

			,new MineStackObj("hard_clay","堅焼き粘土",21,Material.HARD_CLAY,0,false,-1)


			,new MineStackObj("stained_clay","白色の堅焼き粘土",22,Material.STAINED_CLAY,0,false,-1)

			,new MineStackObj("stained_clay1","橙色の堅焼き粘土",22,Material.STAINED_CLAY,1,false,-1)

			,new MineStackObj("stained_clay4","黄色の堅焼き粘土",22,Material.STAINED_CLAY,4,false,-1)
			,new MineStackObj("stained_clay8","薄灰色の堅焼き粘土",22,Material.STAINED_CLAY,8,false,-1)
			,new MineStackObj("stained_clay12","茶色の堅焼き粘土",22,Material.STAINED_CLAY,12,false,-1)
			,new MineStackObj("stained_clay14","赤色の堅焼き粘土",22,Material.STAINED_CLAY,14,false,-1)

			,new MineStackObj("clay","粘土",23,Material.CLAY,0,false,-1)

			,new MineStackObj("mossy_cobblestone","苔石",24,Material.MOSSY_COBBLESTONE,0,false,-1)
			,new MineStackObj("ice","氷",25,Material.ICE,0,false,-1)

			,new MineStackObj("dirt1","粗い土",26,Material.DIRT,1,false,-1)

			,new MineStackObj("dirt2","ポドゾル",26,Material.DIRT,2,false,-1)

			,new MineStackObj("wood5","ダークオークの木材",27,Material.WOOD,5,false,-1)
			,new MineStackObj("dark_oak_fence","ダークオークのフェンス",27,Material.DARK_OAK_FENCE,0,false,-1)
			,new MineStackObj("web","クモの巣",28,Material.WEB,0,false,-1)
			,new MineStackObj("string","糸",28,Material.STRING,0,false,-1)
			,new MineStackObj("rails","レール",29,Material.RAILS,0,false,-1)

			,new MineStackObj("leaves","オークの葉",30,Material.LEAVES,0,false,-1)

			,new MineStackObj("leaves1","マツの葉",30,Material.LEAVES,1,false,-1)
			,new MineStackObj("leaves2","シラカバの葉",30,Material.LEAVES,2,false,-1)
			,new MineStackObj("leaves3","ジャングルの葉",30,Material.LEAVES,3,false,-1)
			,new MineStackObj("leaves_2","アカシアの葉",30,Material.LEAVES_2,0,false,-1)
			,new MineStackObj("leaves_21","ダークオークの葉",30,Material.LEAVES_2,1,false,-1)

			,new MineStackObj("snow_block","雪",31,Material.SNOW_BLOCK,0,false,-1)
			,new MineStackObj("huge_mushroom_1","キノコ",32,Material.HUGE_MUSHROOM_1,0,false,-1)
			,new MineStackObj("huge_mushroom_2","キノコ",32,Material.HUGE_MUSHROOM_2,0,false,-1)

			,new MineStackObj("mycel","菌糸",33,Material.MYCEL,0,false,-1)

			,new MineStackObj("sapling","オークの苗木",34,Material.SAPLING,0,false,-1)
			,new MineStackObj("sapling1","マツの苗木",34,Material.SAPLING,1,false,-1)

			,new MineStackObj("sapling2","シラカバの苗木",34,Material.SAPLING,2,false,-1)
			,new MineStackObj("sapling3","ジャングルの苗木",34,Material.SAPLING,3,false,-1)
			,new MineStackObj("sapling4","アカシアの苗木",34,Material.SAPLING,4,false,-1)
			,new MineStackObj("sapling5","ダークオークの苗木",34,Material.SAPLING,5,false,-1)
			));

	public static final int minestacksize=minestacklist.size();
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
			"world_SW","world_SW_2","world_SW_nether","world_SW_the_end"
			));
	@Override
	public void onEnable(){
		plugin = this;

		//コンフィグ系の設定は全てConfig.javaに移動
		config = new Config(this);
		config.loadConfig();

		//MySQL系の設定はすべてSql.javaに移動
		sql = new Sql(this,config.getURL(), config.getDB(), config.getID(), config.getPW());
		if(!sql.connect()){
			getLogger().info("データベース初期処理にエラーが発生しました");
		}

		//mysqlからガチャデータ読み込み
		if(!sql.loadGachaData()){
			getLogger().info("ガチャデータのロードに失敗しました");
		}

		//コマンドの登録
		commandlist = new HashMap<String, TabExecutor>();
		commandlist.put("gacha",new gachaCommand(plugin));
		commandlist.put("seichi",new seichiCommand(plugin));
		commandlist.put("ef",new effectCommand(plugin));
		commandlist.put("level",new levelCommand(plugin));
		commandlist.put("lastquit",new lastquitCommand(plugin));

		//リスナーの登録
		getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerClickListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerBlockBreakListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerInventoryListener(), this);
		getServer().getPluginManager().registerEvents(new EntityListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerPickupItemListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerDeathEventListener(), this);
		getServer().getPluginManager().registerEvents(new GachaItemListener(), this);

		//オンラインの全てのプレイヤーを処理
		for(Player p : getServer().getOnlinePlayers()){
			//プレイヤーデータを生成
			sql.loadPlayerData(p);
		}

		//ランキングデータをセット
		if(!sql.setRanking()){
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


}


