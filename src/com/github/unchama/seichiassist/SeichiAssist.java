package com.github.unchama.seichiassist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;


public class SeichiAssist extends JavaPlugin{
	public static SeichiAssist plugin;
	private HashMap<String, TabExecutor> commandlist;


	//起動するタスクリスト
	private List<BukkitTask> tasklist = new ArrayList<BukkitTask>();
	//playerに依存するデータマップ
	public static final HashMap<String,PlayerData> playermap = new HashMap<String,PlayerData>();
	//Gachadataに依存するデータリスト
	public static final List<GachaData> gachadatalist = new ArrayList<GachaData>();

	@Override
	public void onEnable(){
		plugin = this;
		//コンフィグ系の設定は全てConfig.javaに移動
		new Config(this);

		//コマンドの登録系の設定も全てCommands.javaに移動
		commandlist = new HashMap<String, TabExecutor>();
		commandlist.put("gacha",new gachaCommand(plugin));
		commandlist.put("seichi",new seichiCommand(plugin));
		commandlist.put("ef",new effectCommand(plugin));

		//リスナーの登録
		getServer().getPluginManager().registerEvents(new SeichiPlayerListener(), this);

		for(Player p : getServer().getOnlinePlayers()){
			playermap.put(p.getName().toLowerCase(), new PlayerData(p));
		}

		getLogger().info("SeichiPlugin is Enabled!");


		//一定時間おきに処理を実行するタスク
		//３０分おき
		//tasklist.add(new HalfHourTaskRunnable().runTaskTimer(this,100,1000));
		tasklist.add(new HalfHourTaskRunnable().runTaskTimer(this,100,36000));
		//１分おき
		tasklist.add(new MinuteTaskRunnable().runTaskTimer(this,0,1200));
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
		int i = 1;
		//プレイヤーのデータをセーブ
		for(String name : playermap.keySet()){
			PlayerData d = playermap.get(name);
			Config.config.set("player" + i,name);
			Config.config.set(name + "effectflag",d.effectflag);
			Config.config.set(name + "messageflag",d.messageflag);
			Config.config.set(name + "gachapoint",d.gachapoint);
			Config.config.set(name + "rank",d.rank);
			i++;
		}
		Config.config.set("playernum",i);
		getLogger().info("プレイヤーデータを保存しました。");

		i = 1;
		//ガチャのデータを保存
		for(GachaData gachadata : gachadatalist){
			Config.config.set("item"+ i,gachadata.itemstack);
			Config.config.set("amount"+ i,gachadata.amount);
			Config.config.set("probability"+ i,gachadata.probability);
			i++;
		}
		Config.config.set("gachanum",i);
		//configをsave
		saveConfig();
		getLogger().info("ガチャを保存しました．");
		getLogger().info("SeichiPlugin is Disabled!");
	}

}


