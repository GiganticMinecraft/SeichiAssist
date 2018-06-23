package com.github.unchama.seichiassist.minestack;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.*;
import org.bukkit.inventory.ItemStack;

public class MineStackObj implements Comparable<MineStackObj>{

	private String objname;
	private String japanesename;
	private int level;
	private Material material;
	private int durability;
	private boolean nameloreflag;
	private int gachatype;
	private List<String> lore;
	private ItemStack itemstack;
	private int stacktype;
	private Enchantment needed_enchantment;

	public MineStackObj(String objname, String japanesename,
			int level, Material material, int durability,
			boolean nameloreflag, int gachatype, int stacktype){
		this.objname = objname;
		this.japanesename = japanesename;
		this.level = level;
		this.material = material;
		this.durability = durability;
		this.nameloreflag = nameloreflag;
		this.gachatype = gachatype;
		this.lore = null;
		this.itemstack = null;
		this.stacktype = stacktype;
	}

	public MineStackObj(String objname, String japanesename,
			int level, Material material, int durability,
			boolean nameloreflag, int gachatype, List<String> lore, int stacktype){
		this.objname = objname;
		this.japanesename = japanesename;
		this.level = level;
		this.material = material;
		this.durability = durability;
		this.nameloreflag = nameloreflag;
		this.gachatype = gachatype;
		this.lore = lore;
		this.itemstack = null;
		this.stacktype = stacktype;
	}

	public MineStackObj(String objname, int level, ItemStack itemstack, boolean nameloreflag, int gachatype, int stacktype){
		this.objname = objname;
		this.japanesename = itemstack.getItemMeta().getDisplayName();
		this.level = level;
		this.material = itemstack.getType();
		this.durability = itemstack.getDurability();
		this.nameloreflag = nameloreflag;
		this.gachatype = gachatype;
		this.lore = itemstack.getItemMeta().getLore();
		this.itemstack = itemstack.clone();
		this.stacktype = stacktype;
	}

	protected MineStackObj(String objname, String japanesename, int level, Material material, int durability,
						   boolean nameloreflag, int gachatype, int stacktype, Enchantment needed_enchantment) {
		this.objname = objname;
		this.japanesename = japanesename;
		this.level = level;
		this.material = material;
		this.durability = durability;
		this.nameloreflag = nameloreflag;
		this.gachatype = gachatype;
		this.stacktype = stacktype;
		this.needed_enchantment = needed_enchantment;
	}

	public String getMineStackObjName(){
		return objname;
	}
	public String getJapaneseName(){
		return japanesename;
	}
	public int getLevel(){
		return level;
	}
	public Material getMaterial(){
		return material;
	}
	public int getDurability(){
		return durability;
	}
	public boolean getNameloreflag(){
		return nameloreflag;
	}
	public int getGachatype(){
		return gachatype;
	}

	public List<String> getLore(){
		return lore;
	}

	public ItemStack getItemStack(){
		return itemstack;
	}

	public int getStacktype(){
		return stacktype;
	}

	public Enchantment getNeeded_enchantment() {
		return needed_enchantment;
	}

	@Override
	public int compareTo(MineStackObj o) {
		// TODO 自動生成されたメソッド・スタブ
		return this.level-o.level;
	}



}
