package com.github.unchama.seichiassist.minestack;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.GachaData;
import com.github.unchama.seichiassist.data.MineStackGachaData;
import com.github.unchama.seichiassist.minestack.objects.MineStackBuildObj;
import com.github.unchama.seichiassist.minestack.objects.MineStackDropObj;
import com.github.unchama.seichiassist.minestack.objects.MineStackFarmObj;
import com.github.unchama.seichiassist.minestack.objects.MineStackGachaObj;
import com.github.unchama.seichiassist.minestack.objects.MineStackMineObj;
import com.github.unchama.seichiassist.minestack.objects.MineStackRsObj;
import com.github.unchama.seichiassist.util.SetFactory;
import org.apache.commons.lang.NotImplementedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
	private static List<GachaData> gachaData = new ArrayList<>();
	private static List<MineStackGachaData> msgds = new ArrayList<>();

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

	/**
	 * ガチャ景品をMineStackに格納できるものとして覚えさせる
	 * @param gachaObj 覚えさせるもの
	 */
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

	// SeichiAssistに準拠
	public static List<MineStackObj> getAllRegistered() {
		final Set<MineStackObj> ret = SetFactory.of();
		ret.addAll(rs);
		ret.addAll(build);
		ret.addAll(drop);
		ret.addAll(farm);
		ret.addAll(gacha);
		ret.addAll(mining);

		// Setなのでdistinctは不要
		return ret.parallelStream().collect(Collectors.toList());
	}

	public static void addGachaData(final GachaData gd) {
		gachaData.add(gd);
	}

	public static void removeGachaData(final GachaData gd) {
		gachaData.remove(gd);
	}

	/**
	 * 登録されたGachaDataの量のアップデート
	 * @param gd ガチャデータ
	 * @param newAmount 新しい量
	 */
	public static void updateGachaDataAmount(final GachaData gd, final int newAmount) {
		if (!gachaData.contains(gd)) {
			SeichiAssist.instance.getLogger().warning("" + gd + "は登録されていません");
			return;
		}

		gachaData.get(gachaData.indexOf(gd)).amount = newAmount;
	}

	/**
	 * 登録されたGachaDataの確率のアップデート
	 * @param gd ガチャデータ
	 * @param newProbability 新しい確率
	 */
	public static void updateGachaDataProbability(final GachaData gd, final double newProbability) {
		if (!gachaData.contains(gd)) {
			SeichiAssist.instance.getLogger().warning("" + gd + "は登録されていません");
			return;
		}

		gachaData.get(gachaData.indexOf(gd)).probability = newProbability;
	}

	public static void moveAndInsertGachaData(final GachaData gd, final int newIndex) {
		if (!gachaData.contains(gd)) {
			SeichiAssist.instance.getLogger().warning("" + gd + "は登録されていません");
			return;
		}
		gachaData.remove(gd);
		gachaData.add(newIndex-1,gd);
	}

	public static void discardGachaData() {
		gachaData.clear();
	}


	public static List<GachaData> getGachaDataes() {
		return Collections.unmodifiableList(gachaData);
	}

	public static void addGachaPrise(final MineStackGachaData msgd) {
		msgds.add(msgd);
	}

	public static void removeGachaPrise(final MineStackGachaData msgd) {
		msgds.remove(msgd);
	}

	public static List<MineStackGachaData> getGachaPrises() {
		return Collections.unmodifiableList(msgds);
	}

	public static void discardGachaPrises() {
		msgds.clear();
	}

	public static void saveGachaMaterials() {
		SeichiAssist.sql.saveGachaData(gachaData.parallelStream().collect(Collectors.toList()));
	}

	/**
	 * 絶対に失敗するメソッド。
	 * @param <T> 見かけ上返すクラス
	 * @return 絶対にない。
	 */
	@Deprecated
	private static <T> T alwaysFail() {
		throw new NotImplementedException();
	}

	/**
	 *
	 * @return ロードが成功したかどうか (true=成功)
	 */
	public static boolean loadGachaMaterials() {
		// 除去した差分を反映するため
		final List<MineStackGachaData> o = SeichiAssist.sql.getMineStackGachaDataL();
		if (o == null) return false;
		gacha.clear();
		// Streamは再利用できない
		gacha.addAll(o
				.parallelStream()
				.map(mineStackGachaData -> new MineStackGachaObj(mineStackGachaData.obj_name, null, mineStackGachaData.level, mineStackGachaData.itemstack.getType(), mineStackGachaData.itemstack.getDurability()))
				.collect(Collectors.toList())
		);
		gachaData.clear();
		gachaData.addAll(o
				.parallelStream()
				.map(mineStackGachaData -> new GachaData(mineStackGachaData.itemstack, mineStackGachaData.probability, mineStackGachaData.amount))
				.collect(Collectors.toList())
		);
		return true;
	}
}
