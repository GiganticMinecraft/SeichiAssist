package com.github.unchama.seichiassist;

import java.util.*;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;


public class SeichiAssist extends JavaPlugin{
	public static SeichiAssist plugin;


	//起動するタスクリスト
	private List<BukkitTask> tasklist = new ArrayList<BukkitTask>();
	//playerに依存するデータマップ
	public static final HashMap<Player,PlayerData> playermap = new HashMap<Player,PlayerData>();
	//Gachadataに依存するデータリスト
	public static final List<GachaData> gachadatalist = new ArrayList<GachaData>();

	@Override
	public void onEnable(){
		plugin = this;
		//コンフィグ系の設定は全てConfig.javaに移動
		new Config(this);

		//コマンドの登録系の設定も全てCommands.javaに移動
		new Commands(this);

		//リスナーの登録
		getServer().getPluginManager().registerEvents(new SeichiPlayerListener(), this);

		
		getLogger().info("SeichiPlugin is Enabled!");

		//一定時間おきに処理を実行するタスク
		//３０分おき
		tasklist.add(new HalfHourTaskRunnable().runTaskTimer(this,100,36000));
		//１分おき
		tasklist.add(new MinuteTaskRunnable().runTaskTimer(this,600,1200));

	}

	@Override
	public void onDisable() {
		//configをsave
		saveConfig();
		//全てのタスクをキャンセル
		for(BukkitTask task:tasklist){
			task.cancel();
		}
		int i = 1;
		//ガチャのデータを保存
		for(GachaData gachadata : gachadatalist){
			Config.config.set("item"+ i,gachadata.itemstack);
			Config.config.set("Amount"+ i,gachadata.amount);
			Config.config.set("probability"+ i,gachadata.probability);
			i++;
		}
		Config.config.set("num",i);
		getLogger().info("ガチャを保存しました．");
		getLogger().info("SeichiPlugin is Disabled!");
	}
	
}


