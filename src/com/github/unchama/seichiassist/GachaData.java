package com.github.unchama.seichiassist;

import org.bukkit.inventory.ItemStack;

public class GachaData {
	//アイテムデータ格納
	ItemStack itemstack;
	//取得確率格納
	double probability;
	//アイテム数
	int amount;
	GachaData(){
		itemstack = null;
		probability = 0.0;
		amount = 0;
	}
	GachaData(ItemStack _itemstack,double _probability,int _amount){
		itemstack = _itemstack;
		probability = _probability;
		amount = _amount;
	}
}
