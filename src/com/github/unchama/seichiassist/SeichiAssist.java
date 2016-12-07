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


