package com.github.unchama.seichiassist.data;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.util.StaticGachaPrizeFactory;
import com.github.unchama.seichiassist.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GachaData {
	//アイテムデータ格納
	public ItemStack itemStack;
	//取得確率格納
	public double probability;
	//アイテム数
	public int amount;

	public GachaData(ItemStack itemStack, double probability, int amount){
		this.itemStack = itemStack.clone();
		this.probability = probability;
		this.amount = amount;
	}

	public GachaData(GachaData gachadata) {
		this.itemStack = gachadata.itemStack.clone();
		this.probability = gachadata.probability;
		this.amount = gachadata.amount;
	}

	public static GachaData runGacha() {
		double sum = 1.0;
		double rand = Math.random();

		for (GachaData gachadata : SeichiAssist.gachadatalist) {
			sum -= gachadata.probability;
			if (sum <= rand) {
				return gachadata.copy();
			}
		}
		return new GachaData(StaticGachaPrizeFactory.getGachaRingo(),1.0,1);
	}
	public boolean compare(ItemStack m,String name) {
		List<String> mlore,lore;
		lore = this.itemStack.getItemMeta().getLore();
		mlore = m.getItemMeta().getLore();
		if(mlore.containsAll(lore)&&this.itemStack.getItemMeta().getDisplayName().equals(m.getItemMeta().getDisplayName())){
			int index = Util.loreIndexOf(mlore, "所有者");
			if(index >= 0){
				//保有者であれば交換
				//保有者でなければ交換できない
				return mlore.get(index).toLowerCase().contains(name);
			}//所有者の記載がなければ交換できる。
			else{
				return true;
			}
		}
		return false;
	}

	public boolean compareonly(ItemStack m) { //ItemStackとgashadataが同じならOK
		List<String> mlore,lore;
		lore = this.itemStack.getItemMeta().getLore();
		mlore = m.getItemMeta().getLore();
		return mlore.containsAll(lore) && this.itemStack.getItemMeta().getDisplayName().equals(m.getItemMeta().getDisplayName());
	}

	public void addname(String name) {
		ItemMeta meta = this.itemStack.getItemMeta();
		List<String> lore;
		if(meta.hasLore()){
			lore = meta.getLore();
		}else{
			lore = new ArrayList<>();
		}
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "所有者：" + name);
		meta.setLore(lore);
		this.itemStack.setItemMeta(meta);
	}

	public GachaData copy() {
		return new GachaData(this.itemStack.clone(), probability, amount);
	}
}
