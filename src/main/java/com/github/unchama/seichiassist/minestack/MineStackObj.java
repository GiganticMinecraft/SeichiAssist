package com.github.unchama.seichiassist.minestack;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MineStackObj {
	private String objName;
	private String japaneseName;
	private int level;
	private int gachaType;
	private int stackType;
	private ItemStack itemStack;
	private boolean nameLoreFlag;

	public MineStackObj(String objName, String japaneseName,
						int level, Material material, int durability,
						boolean nameLoreFlag, int gachaType, int stackType){
		this.objName = objName;
		this.japaneseName = japaneseName;
		this.level = level;
		this.nameLoreFlag = nameLoreFlag;
		this.gachaType = gachaType;
		this.itemStack = new ItemStack(material, 1, (short) durability);
		this.stackType = stackType;
	}

	public MineStackObj(String objName, int level, ItemStack itemStack, boolean nameLoreFlag, int gachaType, int stackType){
		this.objName = objName;
		this.japaneseName = itemStack.getItemMeta().getDisplayName();
		this.level = level;
		this.nameLoreFlag = nameLoreFlag;
		this.gachaType = gachaType;
		this.itemStack = itemStack.clone();
		this.stackType = stackType;
	}

	public String getMineStackObjName(){
		return objName;
	}
	public String getJapaneseName(){
		return japaneseName;
	}
	public int getLevel(){
		return level;
	}
	public Material getMaterial(){
		return itemStack.getType();
	}
	public int getDurability(){
		return itemStack.getDurability();
	}
	public boolean getNameLoreFlag(){
		return nameLoreFlag;
	}
	public int getGachaType(){
		return gachaType;
	}

	public ItemStack getItemStack(){
		return itemStack;
	}

	public int getStackType(){
		return stackType;
	}

}
