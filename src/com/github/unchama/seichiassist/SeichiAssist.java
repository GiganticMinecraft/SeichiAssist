package com.github.unchama.seichiassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.github.unchama.seichiassist.commands.effectCommand;
import com.github.unchama.seichiassist.commands.gachaCommand;
import com.github.unchama.seichiassist.commands.levelCommand;
import com.github.unchama.seichiassist.commands.seichiCommand;
import com.github.unchama.seichiassist.data.GachaData;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.listener.PlayerBlockBreakListener;
import com.github.unchama.seichiassist.listener.PlayerJoinListener;
import com.github.unchama.seichiassist.listener.PlayerRightClickListener;
import com.github.unchama.seichiassist.task.HalfHourTaskRunnable;
import com.github.unchama.seichiassist.task.MinuteTaskRunnable;


public class SeichiAssist extends JavaPlugin{
	public static SeichiAssist plugin;
	public static Boolean DEBUG = true;

	private HashMap<String, TabExecutor> commandlist;
	public Sql sql;
	public static Config config;


	Random rand = new java.util.Random();
	//起動するタスクリスト
	private List<BukkitTask> tasklist = new ArrayList<BukkitTask>();

	//Gachadataに依存するデータリスト
	public static final List<GachaData> gachadatalist = new ArrayList<GachaData>();

	//Playerdataに依存するデータリスト
	public static final HashMap<UUID,PlayerData> playermap = new HashMap<UUID,PlayerData>();

	//ranklvの閾値
	public static final List<Integer> levellist = new ArrayList<Integer>(Arrays.asList(
			0,15,49,106,198,333,
			705,1265,2105,3347,4589,
			5831,7073,8315,9557,11047,
			12835,14980,17554,20642,24347,
			28793,34128,40530,48212,57430,
			68491,81764,97691,116803,135915,//30
			155027,174139,193251,212363,235297,
			262817,295841,335469,383022,434379,
			489844,549746,614440,684309,759767,
			841261,929274,1024328,1126986,1237856,
			1357595,1486913,1626576,1777412,1940314,
			2116248,2306256,2511464,2733088,2954712,//60
			3176336,3397960,3619584,3841208,4080561,
			4339062));
	public static final List<Material> materiallist = new ArrayList<Material>(Arrays.asList(
			Material.STONE,Material.NETHERRACK,Material.NETHER_BRICK,Material.DIRT
			,Material.GRAVEL,Material.LOG,Material.LOG_2,Material.GRASS
			,Material.COAL_ORE,Material.IRON_ORE,Material.GOLD_ORE,Material.DIAMOND_ORE
			,Material.LAPIS_ORE,Material.EMERALD_ORE,Material.REDSTONE_ORE,Material.SAND
			,Material.SANDSTONE,Material.QUARTZ_ORE,Material.END_BRICKS,Material.ENDER_STONE
			,Material.ICE,Material.PACKED_ICE,Material.OBSIDIAN
			));
	public static final List<Material> breakmateriallist = new ArrayList<Material>(Arrays.asList(
			Material.DIAMOND_PICKAXE,Material.DIAMOND_AXE,Material.DIAMOND_SPADE
			));
	public static final List<Material> cancelledmateriallist = new ArrayList<Material>(Arrays.asList(
			Material.CHEST,Material.ENDER_CHEST,Material.TRAPPED_CHEST,Material.ANVIL,Material.ARMOR_STAND
			,Material.BEACON,Material.BIRCH_DOOR,Material.BIRCH_FENCE_GATE,Material.BIRCH_WOOD_STAIRS
			,Material.BOAT,Material.FURNACE,Material.WORKBENCH,Material.HOPPER,Material.MINECART
			));


	@Override
	public void onEnable(){

		plugin = this;

		//コンフィグ系の設定は全てConfig.javaに移動
		config = new Config(this);
		config.loadConfig();
		config.loadGachaData();

		//MySQL系の設定はすべてSql.javaに移動
		sql = new Sql(this,config.getURL(), config.getDB(), config.getTable(), config.getID(), config.getPW());
		if(!sql.connect()){
			getLogger().info("データベース接続に失敗しました。");
		}
		//コマンドの登録
		commandlist = new HashMap<String, TabExecutor>();
		commandlist.put("gacha",new gachaCommand(plugin));
		commandlist.put("seichi",new seichiCommand(plugin));
		commandlist.put("ef",new effectCommand(plugin));
		commandlist.put("level",new levelCommand(plugin));

		//リスナーの登録
		getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerRightClickListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerBlockBreakListener(), this);

		//オンラインの全てのプレイヤーを処理
		for(Player p : getServer().getOnlinePlayers()){
			UUID uuid = p.getUniqueId();
			PlayerData playerdata = new PlayerData(p);
			//名前を取得
			String name = Util.getName(p);
			//データに行を追加
			sql.insertname(name, uuid);
			//プレイヤーマップにプレイヤーを追加
			playermap.put(p.getUniqueId(),playerdata);
			//データの初期設定
			playerdata.updata();
			playerdata.giveSorryForBug();
		}

		getLogger().info("SeichiPlugin is Enabled!");


		//一定時間おきに処理を実行するタスク
		//３０分おき
		if(DEBUG){
			tasklist.add(new HalfHourTaskRunnable().runTaskTimer(this,100,500));
		}else{
			tasklist.add(new HalfHourTaskRunnable().runTaskTimer(this,100,36000));
		}
		//１分おき
		if(DEBUG){
			tasklist.add(new MinuteTaskRunnable().runTaskTimer(this,0,300));
		}else{
			tasklist.add(new MinuteTaskRunnable().runTaskTimer(this,0,1200));
		}
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		return commandlist.get(cmd.getName()).onCommand(sender, cmd, label, args);
	}

	@Override
	public void onDisable(){
		//全てのタスクをキャンセル
		for(BukkitTask task:tasklist){
			task.cancel();
		}
		config.saveGachaData();
		getLogger().info("ガチャを保存しました．");

		sql.disconnect();

		//configをsave
		saveConfig();
		getLogger().info("SeichiPlugin is Disabled!");
	}


}


