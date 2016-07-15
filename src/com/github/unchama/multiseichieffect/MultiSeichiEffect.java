package com.github.unchama.multiseichieffect;


import java.util.ArrayList;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MultiSeichiEffect extends JavaPlugin implements Listener {
	
	//このクラス自身を表すインスタンス
	//public static SeichiPlugin instance;


	
	//タスクキル判定用ArrayList
	public static ArrayList<Player> tasks = new ArrayList<Player>();

	@Override
	public void onEnable(){

		//Configが"なかったら"コピーする
		saveDefaultConfig();

		//config.ymlを読み込む．読み出し方法はconf.getString("key")
		FileConfiguration conf=getConfig();

		//リスナーの登録
		getServer().getPluginManager().registerEvents(this, this);

		getLogger().info("SeichiPlugin is Enabled!");

	}

	@Override
	public void onDisable() {

		getLogger().info("SeichiPlugin is Disabled!");
	}

	//プレイヤーがjoinした時に実行
	@EventHandler
	public void onplayerJoinEvent(PlayerJoinEvent event){
		
		//誰がjoinしたのか取得しPlayer型の変数playerに格納
		Player player = event.getPlayer();
		
		//一定時間おきに処理を実行するインスタンスを立ち上げ、そのインスタンスに変数playerを代入
		new TestRunnable(player).runTaskTimer(this, 0, 1201);
		
		//タスクキル判定用に変数playerをArrayListに登録
		tasks.add(player);
		
		//ログ出力
		//getLogger().info("new runTaskTimer create Success!");
		//getLogger().info("Number of Running task : " + tasks.size());
	}

	/*
	//デバッグ用(runTaskTimerが残らずきちんと終了してることを確認する時に使った)
	@EventHandler
	public void onclick(BlockBreakEvent event){
		Player player = event.getPlayer();
		tasks.remove(player);
		getLogger().info("new runTaskTimer delete Success!");
		getLogger().info("Number of Running task : " + tasks.size());
	}
	*/

	//プレイヤーがleftした時に実行
	@EventHandler
	public void messageQuitEvent(PlayerQuitEvent event){
		
		//誰がleftしたのか取得しPlayer型の変数playerに格納
		Player player = event.getPlayer();
		
		//タスクキル判定用に変数playerをArrayListから削除
		tasks.remove(player);
		
		//ログ出力
		//getLogger().info("new runTaskTimer delete Success!");
		//getLogger().info("Number of Running task : " + tasks.size());
	}
	/*
	@EventHandler
	public void onPlayerRightClickEvent(PlayerInteractEvent event){
		Player player = event.getPlayer();
		Action action = event.getAction();
		ItemStack itemstack = event.getItem();
		itemstack.setAmount(1);
		Block b = Bukkit.getWorld("World").getBlockAt(43, 64, 249);
		
		player.sendMessage("ue-1");
		if(action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK) ){
			player.sendMessage("ue");
			if(((SkullMeta)event.getItem().getItemMeta()).getOwner().equals("unchama")){
				player.sendMessage("ue2");
				player.getInventory().removeItem(itemstack);//43 64 249 ~ 43 64 248
				//ItemStack[] gachachest = ((DoubleChest) b).getInventory().getContents();
				if(player.getInventory().firstEmpty()== -1){
					//player.getWorld().dropItemNaturally(player.getLocation(), gachachest[(int)Math.random()*53]);
					player.sendMessage("地べたに置いたわよ忘れるんじゃないよ");
				}else{
					//player.getInventory().addItem(gachachest[(int)Math.random()*53]);
					player.sendMessage("プレゼントフォーユー");
				}
			}
		}
	}
	*/
}

