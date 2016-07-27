package com.github.unchama.seichiassist.data;

import org.bukkit.inventory.ItemStack;

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
		itemstack = _itemstack;
		probability = _probability;
		amount = _amount;
	}
}
