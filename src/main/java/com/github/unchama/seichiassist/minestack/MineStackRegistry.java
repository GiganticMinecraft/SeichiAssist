package com.github.unchama.seichiassist.minestack;

import com.github.unchama.seichiassist.minestack.objects.MineStackBuildObj;
import com.github.unchama.seichiassist.minestack.objects.MineStackDropObj;
import com.github.unchama.seichiassist.minestack.objects.MineStackFarmObj;
import com.github.unchama.seichiassist.minestack.objects.MineStackGachaObj;
import com.github.unchama.seichiassist.minestack.objects.MineStackMineObj;
import com.github.unchama.seichiassist.minestack.objects.MineStackRsObj;
import com.github.unchama.seichiassist.util.SetFactory;

import java.util.Collections;
import java.util.Set;

/**
 * MineStack種別の管理を一元化するクラス。
 * このクラスでMineStackの一覧を取得したときに返されるCollectionは、特記がない限りイミュータブルである。
 */
public final class MineStackRegistry {
	private static Set<MineStackRsObj> rs = SetFactory.of();
	private static Set<MineStackBuildObj> build = SetFactory.of();
	private static Set<MineStackDropObj> drop = SetFactory.of();
	private static Set<MineStackFarmObj> farm = SetFactory.of();
	private static Set<MineStackGachaObj> gacha = SetFactory.of();
	private static Set<MineStackMineObj> mining = SetFactory.of();

	private MineStackRegistry() {
		
	}
	
	// build
	public static void addBuildingMaterial(final MineStackBuildObj buildObj) {
		build.add(buildObj);
	}

	public static Set<MineStackBuildObj> getBuildingMaterials() {
		return Collections.unmodifiableSet(build);
	}
	
	// drop
	public static void addDropMaterial(final MineStackDropObj dropObj) {
		drop.add(dropObj);
	}
	
	public static Set<MineStackDropObj> getDropMaterials() {
		return Collections.unmodifiableSet(drop);
	}
	
	// farm
	public static void addFarmMaterial(final MineStackFarmObj farmObj) {
		farm.add(farmObj);
	}

	public static Set<MineStackFarmObj> getFarmMaterials() {
		return Collections.unmodifiableSet(farm);
	}
	
	// gacha
	public static void addGachaMaterial(final MineStackGachaObj gachaObj) {
		gacha.add(gachaObj);
	}

	public static Set<MineStackGachaObj> getGachaMaterials() {
		return Collections.unmodifiableSet(gacha);
	}

	// mine
	public static void addMiningMaterial(final MineStackMineObj mineObj) {
		mining.add(mineObj);
	}

	public static Set<MineStackMineObj> getMiningMaterials() {
		return Collections.unmodifiableSet(mining);
	}
	
	// redstone
	public static void addRedstoneMaterial(final MineStackRsObj rsObj) {
		rs.add(rsObj);
	}

	public static Set<MineStackRsObj> getRedstoneMaterials() {
		return Collections.unmodifiableSet(rs);
	}

}
