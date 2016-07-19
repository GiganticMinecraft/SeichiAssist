package com.github.unchama.multiseichieffect;

import static com.github.unchama.multiseichieffect.Util.*;

import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class Gacha{
	private Player player;
	private Config config;

	private int point;
	private int last_point;
	private int interval;
	private ItemStack skull;

	Gacha(Player _player, Config _config){
		player = _player;
		config = _config;
		point = 0;
		last_point = 0;
		interval = config.getGachaPresentInterval();
		skull = getskull();
	}
	public void setPoint(int minute_point) {
		point += minute_point;
	}

	public void presentticket() {
		if(point >= interval){
			point -= interval;
			dropItem(player,skull);
			player.sendMessage(ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "が下に落ちました。右クリックで使えるゾ");
			/*
			if(!isPlayerContainItem(player,skull) && isPlayerInventryEmpty(player)){
				dropItem(player,skull);
				player.sendMessage("あなたの"+ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "地べたに置いたわよ忘れるんじゃないよ");
			}else{
				addItem(player,skull);
				player.sendMessage(ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "プレゼントフォーユー");
			}
			*/
		}else if(last_point != point){
			player.sendMessage("あと" + ChatColor.AQUA + (1000 - point) + ChatColor.WHITE + "ブロック整地すると" + ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "獲得ダヨ");
		}else{
			//１ブロックも掘ってなかったら煽る
			player.sendMessage("あ  く  し  ろ  は  た  ら  け");
		}
	}
	public void setLastPoint() {
		last_point = point;

	}
	public static ItemStack getskull(){
		ItemStack skull;
		SkullMeta skullmeta;
		skull = new ItemStack(Material.SKULL_ITEM, 1);
		skullmeta = (SkullMeta) skull.getItemMeta();
		skull.setDurability((short) 3);
		skullmeta.setDisplayName("ガチャ券");
		skullmeta.setOwner("unchama");
		skull.setItemMeta(skullmeta);
		return skull;
	}
	public static ItemStack runGacha() {
		double sum = 1.0;
		double rand = 0.0;
		//for (Entry<ItemStack, Double> item : MultiSeichiEffect.gachaitem.entrySet()) {
		//    sum += item.getValue();
		//}
		rand = Math.random();

		for (Entry<ItemStack, Double> item : MultiSeichiEffect.gachaitem.entrySet()) {
		    sum -= item.getValue();
		    if (sum <= rand) {
                return item.getKey();
            }
		}
		return new ItemStack(Material.BAKED_POTATO,1);
	}



}
