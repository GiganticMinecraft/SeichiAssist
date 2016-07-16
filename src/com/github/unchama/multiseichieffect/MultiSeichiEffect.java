package com.github.unchama.multiseichieffect;


import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class MultiSeichiEffect extends JavaPlugin implements Listener {

	//このクラス自身を表すインスタンス
	public static MultiSeichiEffect instance;

	private Player player;
	private Config config;
	private BukkitTask allplayertask;

	private MineBlock mineblock;
	//コマンドの一覧
	private HashMap<String, TabExecutor> commands;

	public static final HashMap<ItemStack,Double> gachaitem = new HashMap<ItemStack,Double>();
	public static final HashMap<Player,MineBlock> playermap = new HashMap<Player,MineBlock>();

	@Override
	public void onEnable(){
		instance = this;

		//Configが"なかったら"コピーする
		saveDefaultConfig();

		//config.ymlを読み込む．読み出し方法はconf.getString("key")
		config = new Config(getConfig());

		//コマンドの登録
		commands = new HashMap<String, TabExecutor>();
		commands.put("gacha", new gachaCommand(this));
		commands.put("seichi", new seichiCommand(this));


		//リスナーの登録
		getServer().getPluginManager().registerEvents(this, this);

		getLogger().info("SeichiPlugin is Enabled!");

		//一定時間おきに処理を実行するインスタンスを立ち上げ、そのインスタンスに変数playerを代入
		allplayertask = new HalfHourTaskRunnable(playermap,config).runTaskTimer(this,100,1200 * config.getNo1PlayerInterval()+1);

	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		return commands.get(cmd.getName()).onCommand(sender, cmd, label, args);
}
	@Override
	public void onDisable() {
		getLogger().info("SeichiPlugin is Disabled!");
		allplayertask.cancel();
	}

	//プレイヤーがjoinした時に実行
	@EventHandler
	public void onplayerJoinEvent(PlayerJoinEvent event){
		player = event.getPlayer();
		mineblock = new MineBlock(player);

		//誰がjoinしたのか取得しplayermapに格納
		playermap.put(player,mineblock);

		//1分おきに処理を実行するインスタンスを立ち上げ、そのインスタンスに変数playerを代入
		new MinuteTaskRunnable(player,config).runTaskTimer(this,0,1201);
;

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
	public void onPlayerQuitEvent(PlayerQuitEvent event){
		Player player = event.getPlayer();

		//誰がleftしたのか取得しplayermapに格納
		playermap.remove(player);



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
		ItemStack presentitem ;
		int amount = 0;
		Gacha gacha = new Gacha();
		if(action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK) ){
			if(event.getItem().getType().equals(gacha.getskull().getType())){
				amount = player.getItemOnCursor().getAmount();
				player.getItemOnCursor().setAmount(amount-1);//43 64 249 ~ 43 64 248
				presentitem = gacha.runGacha();
				if(player.getInventory().firstEmpty()== -1){
					player.getWorld().dropItemNaturally(player.getLocation(),presentitem);
					player.sendMessage("地べたに置いたわよ忘れるんじゃないよ");
				}else{
					player.getInventory().addItem(presentitem);
					player.sendMessage("プレゼントフォーユー");
				}
			}
		}
	}

	*/

}

