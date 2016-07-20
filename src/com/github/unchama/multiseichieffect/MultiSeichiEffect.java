package com.github.unchama.multiseichieffect;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
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
	public static final HashMap<Player,Boolean> playerflag = new HashMap<Player,Boolean>();

	private boolean lock_rungacha;

	@Override
	public void onEnable(){
		instance = this;
		lock_rungacha = false;

		//Configが"なかったら"コピーする
		saveDefaultConfig();

		//config.ymlを読み込む．読み出し方法はconf.getString("key")
		config = new Config(getConfig());

		//コマンドの登録
		commands = new HashMap<String, TabExecutor>();
		commands.put("gacha", new gachaCommand(this,getConfig()));
		commands.put("seichi", new seichiCommand(this));
		commands.put("ef", new effectCommand(this));


		//リスナーの登録
		getServer().getPluginManager().registerEvents(this, this);

		//初回ガチャデータロード
		//gachaCommand.java見ればわかるけどとりあえず実装しただけだからコードまとめたりはそのうちやる
		gachaCommand.onEnableGachaLoad();
		getLogger().info("ガチャデータのLoadを完了しました。");

		getLogger().info("SeichiPlugin is Enabled!");

		//一定時間おきに処理を実行するインスタンスを立ち上げ、そのインスタンスに変数playerを代入
		allplayertask = new AllPlayerTaskRunnable(playermap,config).runTaskTimer(this,100,1200 * config.getNo1PlayerInterval()+1);

	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		return commands.get(cmd.getName()).onCommand(sender, cmd, label, args);
	}
	@Override
	public void onDisable() {
		getLogger().info("SeichiPlugin is Disabled!");
		saveConfig();
		allplayertask.cancel();
		//itemlistを保存したい。
	}

	//プレイヤーがjoinした時に実行
	@EventHandler
	public void onplayerJoinEvent(PlayerJoinEvent event){
		player = event.getPlayer();
		mineblock = new MineBlock(player);

		//誰がjoinしたのか取得しplayermapに格納
		playermap.put(player,mineblock);

		//flag設定をしていないプレイヤーを初期値TRUEにして追加
		if(!playerflag.containsKey(player)){
			playerflag.put(player, true);
		}

		//1分おきに処理を実行するインスタンスを立ち上げ、そのインスタンスに変数playerを代入
		new MinuteTaskRunnable(player,config).runTaskTimer(this,0,1201);
	}

	//プレイヤーがleftした時に実行
	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent event){
		Player player = event.getPlayer();

		//誰がleftしたのか取得しplayermapに格納
		playermap.remove(player);


	}

	//プレイヤーが右クリックした時に実行(ガチャを引く部分の処理)
	@EventHandler
	public void onPlayerRightClickEvent(PlayerInteractEvent event){
		Player player = event.getPlayer();
		Action action = event.getAction();
		ItemStack itemstack = event.getItem();
		ItemStack present;
		int amount = 0;
		Double probability = 0.0;
		if(action.equals(Action.RIGHT_CLICK_AIR)){
			if(itemstack.getType().equals(Material.SKULL_ITEM)){
				if(lock_rungacha){
					player.sendMessage("しばらく待ってからやり直してください");
					return;
				}
				if(gachaitem.isEmpty()){
					player.sendMessage("ガチャが設定されていません");
					return;
				}
				lock_rungacha = true;
				amount = player.getInventory().getItemInMainHand().getAmount();
				if (amount == 1) {
					// がちゃ券を1枚使うので、プレイヤーの手を素手にする
					player.getInventory().setItemInMainHand(null);
					} else {
					// プレイヤーが持っているガチャ券を1枚減らす
					player.getInventory().getItemInMainHand().setAmount(amount - 1);
					}
				//gacha実行
				present = Gacha.runGacha();

				probability = gachaitem.get(present);
				if(probability == null){
					probability = 1.0;
				}
				if(present.getAmount() == 0){
					present.setAmount(1);
				}

				Util.dropItem(player, present);
				//player.getWorld().dropItemNaturally(player.getLocation(),present);

				if(probability < 0.001){
					Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1, 2);
					player.sendMessage(ChatColor.YELLOW + "おめでとう！！！！！Gigantic☆大当たり！");
					Util.sendEveryMessage(ChatColor.GOLD + player.getDisplayName() + "がガチャでGigantic☆大当たり！\n" + ChatColor.AQUA + present.getItemMeta().getDisplayName() + "を引きました！おめでとうございます！");
				}else if(probability < 0.01){
					Util.sendEverySound(Sound.ENTITY_WITHER_SPAWN, (float) 0.8, 1);
					player.sendMessage(ChatColor.YELLOW + "おめでとう！！大当たり！");
					Util.sendEveryMessage(ChatColor.GOLD + player.getDisplayName() + "がガチャで大当たり！\n" + ChatColor.DARK_BLUE + present.getItemMeta().getDisplayName() + "を引きました！おめでとうございます！");
				}else if(probability < 0.1){
					player.sendMessage(ChatColor.YELLOW + "おめでとう！当たり！");
				}else if(probability <= 1.0){
					player.sendMessage(ChatColor.YELLOW + "はずれ！また遊んでね！");
				}else{
					player.sendMessage(ChatColor.RED + "不明なエラーが発生しました。");
				}
				player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, (float) 0.1);
				player.sendMessage(ChatColor.RED + "プレゼントが下に落ちました。");
				lock_rungacha = false;
			}
		}

	}

}

