package com.github.unchama.multiseichieffect;

import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class SeichiPlayerListener implements Listener {
	
	
	//プレイヤーがjoinした時に実行
	@EventHandler
	public void onplayerJoinEvent(PlayerJoinEvent event){
		Player player = event.getPlayer();
		HashMap<Player,PlayerData> playermap = MultiSeichiEffect.playermap;
		PlayerData playerdata;
		
		//ログインしたプレイヤーのPlayerData作成
		if(!playermap.containsKey(player)){
			playermap.put(player, new PlayerData());
		}
		//playerのplayerdataを参照
		playerdata = playermap.get(player);
		
		//初見かどうかの判定
		if(player.hasPlayedBefore()){
			playerdata.firstjoinflag = true;
		}
		
		//オンラインプレイヤーフラグをONにする．
		playerdata.onlineflag = true;
		
		//1分おきに処理を実行するインスタンスを立ち上げ、そのインスタンスに変数playerを代入
		new MinuteTaskRunnable(player).runTaskTimer(this,0,1201);
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
				//がちゃ実行
				present = Gacha.runGacha();

				probability = gachaitem.get(present);
				if(probability == null){
					probability = 1.0;
				}
				if(present.getAmount() == 0){
					present.setAmount(1);
				}
				player.getWorld().dropItemNaturally(player.getLocation(),present);

				if(probability < 0.001){
					player.sendMessage(ChatColor.YELLOW + "おめでとう！！！！！Gigantic☆大当たり！");
					Util.sendEveryMessage(ChatColor.GOLD + player.getDisplayName() + "がガチャでGigantic☆大当たり" +  present.getItemMeta().getDisplayName() + ChatColor.GOLD + "を引きました！おめでとうございます！");
				}else if(probability < 0.01){
					player.sendMessage(ChatColor.YELLOW + "おめでとう！！大当たり！");
					Util.sendEveryMessage(ChatColor.GOLD + player.getDisplayName() + "がガチャで大当たり"  + present.getItemMeta().getDisplayName() +  ChatColor.GOLD + "を引きました！おめでとうございます！");
				}else if(probability < 0.1){
					player.sendMessage(ChatColor.YELLOW + "おめでとう！当たり！");
				}else if(probability < 1.0){
					player.sendMessage(ChatColor.YELLOW + "はずれ！また遊んでね！");
				}else{
					player.sendMessage(ChatColor.RED+ "不明なエラーが発生しました．管理者に報告してください．");
				}
				Util.dropItem(player, present);
				//player.getWorld().dropItemNaturally(player.getLocation(),present);
				String str = ChatColor.RED + "プレゼントが下に落ちました。";

				if(probability < 0.001){
					Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1, 2);
					player.sendMessage(ChatColor.YELLOW + "おめでとう！！！！！Gigantic☆大当たり！" + str);
					Util.sendEveryMessage(ChatColor.GOLD + player.getDisplayName() + "がガチャでGigantic☆大当たり！\n" + ChatColor.AQUA + present.getItemMeta().getDisplayName() + "を引きました！おめでとうございます！");
				}else if(probability < 0.01){
					Util.sendEverySound(Sound.ENTITY_WITHER_SPAWN, (float) 0.8, 1);
					player.sendMessage(ChatColor.YELLOW + "おめでとう！！大当たり！" + str);
					Util.sendEveryMessage(ChatColor.GOLD + player.getDisplayName() + "がガチャで大当たり！\n" + ChatColor.DARK_BLUE + present.getItemMeta().getDisplayName() + "を引きました！おめでとうございます！");
				}else if(probability < 0.1){
					player.sendMessage(ChatColor.YELLOW + "おめでとう！当たり！" + str);
				}else if(probability <= 1.0){
					player.sendMessage(ChatColor.YELLOW + "はずれ！また遊んでね！" + str);
				}else{
					player.sendMessage(ChatColor.RED + "不明なエラーが発生しました。");
				}
				player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, (float) 0.1);
				lock_rungacha = false;
			}
		}

	}

}

}
