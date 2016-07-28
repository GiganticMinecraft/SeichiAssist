package com.github.unchama.seichiassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
import com.github.unchama.seichiassist.listener.SeichiPlayerListener;
import com.github.unchama.seichiassist.task.HalfHourTaskRunnable;
import com.github.unchama.seichiassist.task.MinuteTaskRunnable;


public class SeichiAssist extends JavaPlugin{
	public static SeichiAssist plugin;
	private HashMap<String, TabExecutor> commandlist;
	public static Boolean DEBUG = false;


	//起動するタスクリスト
	private List<BukkitTask> tasklist = new ArrayList<BukkitTask>();

	//playerに依存するデータマップ
	public static final HashMap<String,PlayerData> playermap = new HashMap<String,PlayerData>();

	//Gachadataに依存するデータリスト
	public static final List<GachaData> gachadatalist = new ArrayList<GachaData>();

	//ranklvの閾値
	public static final List<Integer> levellist = new ArrayList<Integer>(Arrays.asList(
			15,49,106,198,333,
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

	//パッシブスキルが獲得できるレベルのリスト
	public static final List<Integer> passiveskillgetlevel = Arrays.asList(3);

	//アクティブスキルが獲得できるレベルのリスト
	public static final List<Integer> activeskillgetlevel = Arrays.asList(4);

	@Override
	public void onEnable(){

		plugin = this;

		//コンフィグ系の設定は全てConfig.javaに移動
		new Config(this);

		//コマンドの登録
		commandlist = new HashMap<String, TabExecutor>();
		commandlist.put("gacha",new gachaCommand(plugin));
		commandlist.put("seichi",new seichiCommand(plugin));
		commandlist.put("ef",new effectCommand(plugin));
		commandlist.put("level",new levelCommand(plugin));

		//リスナーの登録
		getServer().getPluginManager().registerEvents(new SeichiPlayerListener(), this);

		//オンラインの全てのプレイヤーを処理
		for(Player p : getServer().getOnlinePlayers()){
			//名前を取得
			String name = Util.getName(p);

			//保存データに一致する名前があるか検索
			if(!playermap.containsKey(name)){
				//ないときは新しく作成
				playermap.put(name, new PlayerData());
			}

			//プレイヤーの保存データplayerdataを参照
			PlayerData playerdata = playermap.get(name);

			//更新したいデータを更新
			playerdata.updata(p);
			playerdata.giveSorryForBug(p);

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

		Config.savePlayerData();
		getLogger().info("プレイヤーデータを保存しました。");

		Config.saveGachaData();
		getLogger().info("ガチャを保存しました．");

		//configをsave
		saveConfig();
		getLogger().info("SeichiPlugin is Disabled!");
	}

}


