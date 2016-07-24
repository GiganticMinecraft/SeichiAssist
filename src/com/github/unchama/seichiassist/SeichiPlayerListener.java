package com.github.unchama.seichiassist;

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
import org.bukkit.inventory.ItemStack;

public class SeichiPlayerListener implements Listener {
	HashMap<Player,PlayerData> playermap;

	//プレイヤーがjoinした時に実行
	@EventHandler
	public void onplayerJoinEvent(PlayerJoinEvent event){
		Player player = event.getPlayer();
		playermap = SeichiAssist.playermap;
		PlayerData playerdata;

		//ログインしたプレイヤーのデータが残っていなかった時にPlayerData作成
		if(!playermap.containsKey(player)){
			playermap.put(player, new PlayerData(player));
		}

		//playerのplayerdataを参照
		playerdata = playermap.get(player);

		//初見かどうかの判定
		if(player.hasPlayedBefore()){
			playerdata.firstjoinflag = true;
		}

	}


	//プレイヤーが右クリックした時に実行(ガチャを引く部分の処理)
	@EventHandler
	public void onPlayerRightClickEvent(PlayerInteractEvent event){
		Player player = event.getPlayer();
		Action action = event.getAction();
		ItemStack itemstack = event.getItem();
		GachaData present = new GachaData();
		int amount = 0;
		Double probability = 0.0;
		List<GachaData> gachadatalist = SeichiAssist.gachadatalist;

		if(action.equals(Action.RIGHT_CLICK_AIR)){
			if(itemstack.getType().equals(Material.SKULL_ITEM)){
				if(gachadatalist.isEmpty()){
					player.sendMessage("ガチャが設定されていません");
					return;
				}
				amount = player.getInventory().getItemInMainHand().getAmount();
				if (amount == 1) {
					// がちゃ券を1枚使うので、プレイヤーの手を素手にする
					player.getInventory().setItemInMainHand(null);
					} else {
					// プレイヤーが持っているガチャ券を1枚減らす
					player.getInventory().getItemInMainHand().setAmount(amount - 1);
					}
				//ガチャ実行
				present = runGacha();

				probability = present.probability;
				if(probability == null){
					probability = 1.0;
				}
				if(present.amount == 0){
					present.amount = 1;
				}
				String str = ChatColor.RED + "プレゼントが下に落ちました。";
				Util.dropItem(player, present.itemstack);
				if(probability < 0.001){
					Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1, 2);
					player.sendMessage(ChatColor.YELLOW + "おめでとう！！！！！Gigantic☆大当たり！" + str);
					Util.sendEveryMessage(ChatColor.GOLD + player.getDisplayName() + "がガチャでGigantic☆大当たり！\n" + ChatColor.AQUA + present.itemstack.getItemMeta().getDisplayName() + ChatColor.GOLD + "を引きました！おめでとうございます！");
				}else if(probability < 0.01){
					Util.sendEverySound(Sound.ENTITY_WITHER_SPAWN, (float) 0.8, 1);
					player.sendMessage(ChatColor.YELLOW + "おめでとう！！大当たり！" + str);
					Util.sendEveryMessage(ChatColor.GOLD + player.getDisplayName() + "がガチャで大当たり！\n" + ChatColor.DARK_BLUE + present.itemstack.getItemMeta().getDisplayName() + ChatColor.GOLD + "を引きました！おめでとうございます！");
				}else if(probability < 0.1){
					player.sendMessage(ChatColor.YELLOW + "おめでとう！当たり！" + str);
				}else if(probability <= 1.0){
					player.sendMessage(ChatColor.YELLOW + "はずれ！また遊んでね！" + str);
				}else{
					player.sendMessage(ChatColor.RED + "不明なエラーが発生しました。");
				}
				player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, (float) 0.1);
			}
		}

	}

	private GachaData runGacha() {
		double sum = 1.0;
		double rand = 0.0;

		rand = Math.random();

		for (GachaData gachadata : SeichiAssist.gachadatalist) {
		    sum -= gachadata.probability;
		    if (sum <= rand) {
                return gachadata;
            }
		}
		return new GachaData(new ItemStack(Material.BAKED_POTATO,1),1.0,1);
	}
}
