package com.github.unchama.seichiassist.data;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.util.Util;

public class GachaData {
	//アイテムデータ格納
	public ItemStack itemstack;
	//取得確率格納
	public double probability;
	//アイテム数
	public int amount;
	public GachaData(){
		itemstack = null;
		probability = 0.0;
		amount = 0;
	}
	public GachaData(ItemStack _itemstack,double _probability,int _amount){
		itemstack = _itemstack.clone();
		probability = _probability;
		amount = _amount;
	}

	public GachaData(GachaData gachadata) {
		this.itemstack = gachadata.itemstack.clone();
		this.probability = gachadata.probability;
		this.amount = gachadata.amount;
	}
	public static GachaData runGacha() {
		double sum = 1.0;
		double rand = 0.0;

		rand = Math.random();

		for (GachaData gachadata : SeichiAssist.gachadatalist) {
		    sum -= gachadata.probability;
		    if (sum <= rand) {
                return new GachaData(gachadata);
            }
		}
		return new GachaData(Util.getGachaimo(),1.0,1);
	}
	public boolean compare(ItemStack m,String name) {
		List<String> mlore,lore;
		lore = this.itemstack.getItemMeta().getLore();
		mlore = m.getItemMeta().getLore();
		if(mlore.containsAll(lore)&&this.itemstack.getItemMeta().getDisplayName().equals(m.getItemMeta().getDisplayName())){
			int index = Util.LoreContains(mlore, "所有者");
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
		lore = this.itemstack.getItemMeta().getLore();
		mlore = m.getItemMeta().getLore();
        return mlore.containsAll(lore) && this.itemstack.getItemMeta().getDisplayName().equals(m.getItemMeta().getDisplayName());
    }

	public void addname(String name) {
		ItemMeta meta = this.itemstack.getItemMeta();
		List<String> lore;
		if(meta.hasLore()){
			lore = meta.getLore();
		}else{
			lore = new ArrayList<String>();
		}
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "所有者：" + name);
		meta.setLore(lore);
		this.itemstack.setItemMeta(meta);
	}
}
