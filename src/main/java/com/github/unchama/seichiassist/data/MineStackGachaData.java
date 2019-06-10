package com.github.unchama.seichiassist.data;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MineStackGachaData implements Cloneable {
	public final String objName;
	//アイテムデータ格納
	public final ItemStack itemStack;
	//取得確率格納
	public final double probability;
	//アイテム数
	public final int amount;
	//解放レベル(本来のレベルではないことに注意)
	public final int level;

	public MineStackGachaData(String objName, ItemStack itemStack, double probability, int amount, int level){
		this.objName = objName;
		this.itemStack = itemStack.clone();
		this.probability = probability;
		this.amount = amount;
		this.level = level;
	}

	public boolean itemStackEquals(ItemStack another) { //ItemStackとgashadataが同じならOK
		final List<String> lore = this.itemStack.getItemMeta().getLore();
		final List<String> anotherLore = another.getItemMeta().getLore();

		if(anotherLore.containsAll(lore)&&
				( this.itemStack.getItemMeta().getDisplayName().contains(another.getItemMeta().getDisplayName()) ||
				another.getItemMeta().getDisplayName().contains(this.itemStack.getItemMeta().getDisplayName()) ) ){
			//この時点で名前と内容が一致
			//盾、バナー用の模様判定
			if( (another.getType() == Material.SHIELD || (another.getType() == Material.BANNER) ) && this.itemStack.getType() == another.getType()){
				BlockStateMeta bs0 = (BlockStateMeta) another.getItemMeta();
				Banner b0 = (Banner) bs0.getBlockState();
				List<Pattern> p0 = b0.getPatterns();

				BlockStateMeta bs1 = (BlockStateMeta) this.itemStack.getItemMeta();
				Banner b1 = (Banner) bs1.getBlockState();
				List<Pattern> p1 = b1.getPatterns();

				return p0.containsAll(p1);
			}
			return true;
		}
		return false;
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

	public MineStackGachaData copy() {
		return new MineStackGachaData(objName, itemStack.clone(), probability, amount, level);
	}
}
