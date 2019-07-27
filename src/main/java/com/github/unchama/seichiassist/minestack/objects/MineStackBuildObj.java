package com.github.unchama.seichiassist.minestack.objects;

import com.github.unchama.seichiassist.minestack.MineStackObj;
import com.github.unchama.seichiassist.minestack.MineStackObjectCategory;
import org.bukkit.Material;

/**
 * Created by karayuu on 2018/06/04
 */
public final class MineStackBuildObj extends MineStackObj {
	public MineStackBuildObj(String objname, String japanesename, int level, Material material, int durability) {
		super(objname, japanesename, level, material, durability, false, -1, MineStackObjectCategory.BUILDING);
	}
}
