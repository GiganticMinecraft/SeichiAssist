package com.github.unchama.seichiassist.listener;

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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.Util;
import com.github.unchama.seichiassist.data.GachaData;
import com.github.unchama.seichiassist.data.PlayerData;

public class PlayerRightClickListener implements Listener {
	HashMap<String,PlayerData> playermap;
	//sqlを開く
	Sql sql = SeichiAssist.plugin.sql;

	//プレイヤーが右クリックした時に実行(ガチャを引く部分の処理)
	@EventHandler
	public void onPlayerRightClickGachaEvent(PlayerInteractEvent event){
		Player player = event.getPlayer();
		Action action = event.getAction();
		ItemStack itemstack = event.getItem();
		EquipmentSlot equipmentslot = event.getHand();

		GachaData present = new GachaData();
		int amount = 0;
		Double probability = 0.0;
		List<GachaData> gachadatalist = SeichiAssist.gachadatalist;


		if(action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)){
			if(event.getMaterial().equals(Material.SKULL_ITEM)){
				SkullMeta skullmeta = (SkullMeta) itemstack.getItemMeta();
				if(!skullmeta.hasOwner()){
					return;
				}
				if(skullmeta.getOwner().equals("unchama")){
					//設置をキャンセル
					event.setCancelled(true);
					if(equipmentslot.equals(EquipmentSlot.OFF_HAND)){
						return;
					}
					if(gachadatalist.isEmpty()){
						player.sendMessage("ガチャが設定されていません");
						return;
					}


					amount = itemstack.getAmount();
					if (amount == 1) {
						// がちゃ券を1枚使うので、プレイヤーの手を素手にする
						player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
					} else {
						// プレイヤーが持っているガチャ券を1枚減らす
						itemstack.setAmount(itemstack.getAmount()-1);
					}
					//ガチャ実行

					present = GachaData.runGacha();
					present.itemstack.setAmount(present.amount);
					probability = present.probability;
					String str = ChatColor.RED + "プレゼントがドロップしました。";
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
					}else{
						player.sendMessage(ChatColor.YELLOW + "はずれ！また遊んでね！" + str);
					}
					player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, (float) 0.1);
				}
			}
		}
	}
	@EventHandler
	public void onPlayerRightClickActiveSkillEvent(PlayerInteractEvent event){
		Player player = event.getPlayer();
		String name = Util.getName(player);
		Action action = event.getAction();
		EquipmentSlot equipmentslot = event.getHand();
		if(action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)){
			if(!player.isSneaking()){
				return;
			}else if(SeichiAssist.breakmateriallist.contains(player.getInventory().getItemInMainHand().getType())){

				if(equipmentslot.equals(EquipmentSlot.OFF_HAND)){
					//設置をキャンセル
					event.setCancelled(true);
					return;
				}
				if(action.equals(Action.RIGHT_CLICK_BLOCK)){
					Material cmaterial = event.getClickedBlock().getType();
					if(SeichiAssist.cancelledmateriallist.contains(cmaterial)){
						return;
					}
				}

				if(sql.selectint(name, "level") < SeichiAssist.config.getActiveMinelevel()){
					return;
				}
				boolean activemineflag = !sql.selectboolean(name, "activemineflag");
				if(activemineflag){
					player.sendMessage(ChatColor.GOLD + "デュアルブレイク:ON");
				}else{
					player.sendMessage(ChatColor.GOLD + "デュアルブレイク：OFF");
				}
				player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1, 1);
				sql.insert("activemineflag", activemineflag, name);
			}
		}
	}
	@EventHandler
	public void onPlayerRightClickEffectEvent(PlayerInteractEvent event){
		Player player = event.getPlayer();
		String name = Util.getName(player);
		Action action = event.getAction();
		EquipmentSlot equipmentslot = event.getHand();
		if(action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)){
			if(!player.isSneaking()){
				return;
			}else if(player.getInventory().getItemInMainHand().getType().equals(Material.AIR)){
				//設置をキャンセル
				event.setCancelled(true);
				if(equipmentslot.equals(EquipmentSlot.OFF_HAND) && action.equals(Action.RIGHT_CLICK_BLOCK)){
					return;
				}
				boolean effectflag = !sql.selectboolean(name, "effectflag");
				if (effectflag){
					player.sendMessage(ChatColor.GREEN + "採掘速度上昇効果:ON");
				}else{
					player.sendMessage(ChatColor.GREEN + "採掘速度上昇効果:OFF");
				}
				sql.insert("effectflag", effectflag, name);
			}
		}
	}

}
