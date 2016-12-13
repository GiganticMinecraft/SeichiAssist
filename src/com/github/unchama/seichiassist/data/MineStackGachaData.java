package com.github.unchama.seichiassist.data;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.unchama.seichiassist.util.Util;

public class MineStackGachaData {
	public String obj_name;
	//アイテムデータ格納
	public ItemStack itemstack;
	//取得確率格納
	public double probability;
	//アイテム数
	public int amount;
	public MineStackGachaData(){
		itemstack = null;
		probability = 0.0;
		amount = 0;
	}
	public MineStackGachaData(String _obj_name, ItemStack _itemstack,double _probability,int _amount){
		obj_name = _obj_name;
		itemstack = _itemstack.clone();
		probability = _probability;
		amount = _amount;
	}

	public MineStackGachaData(MineStackGachaData gachadata) {
		this.obj_name = gachadata.obj_name;
		this.itemstack = gachadata.itemstack.clone();
		this.probability = gachadata.probability;
		this.amount = gachadata.amount;
	}
	
	public boolean compare(ItemStack m,String name) {
		List<String> mlore,lore;
		lore = this.itemstack.getItemMeta().getLore();
		mlore = m.getItemMeta().getLore();
		if(mlore.containsAll(lore)&&this.itemstack.getItemMeta().getDisplayName().equals(m.getItemMeta().getDisplayName())){
			int index = Util.LoreContains(mlore, "所有者");
			if(index >= 0){
				//保有者であれば交換
				if(mlore.get(index).toLowerCase().contains(name)){
					return true;
				}//保有者でなければ交換できない
				else{
					return false;
				}
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
		if(mlore.containsAll(lore)&&this.itemstack.getItemMeta().getDisplayName().equals(m.getItemMeta().getDisplayName())){
			return true;
		}
		return false;
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
