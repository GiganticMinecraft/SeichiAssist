package com.github.unchama.seichiassist.data;

import com.github.unchama.seichiassist.minestack.MineStackObj;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class MineStack {

	private HashMap<MineStackObj, Long> objectCountMap = new HashMap<>();

	public MineStack(){ }

	public long getStackedAmountOf(@NotNull MineStackObj mineStackObj) {
		if (objectCountMap.containsKey(mineStackObj)) {
			return objectCountMap.get(mineStackObj);
		}
		return 0;
	}

	/**
	 * 指定されたマインスタックオブジェクトのスタック数をセットする。
	 *
	 * これは初期化時にのみ呼ぶべきであり、増減をさせたい場合は
	 * {@link MineStack#addStackedAmountOf(MineStackObj, long)} と
	 * {@link MineStack#subtractStackedAmountOf(MineStackObj, long)} を呼べ。
	 */
	public void setStackedAmountOf(@NotNull MineStackObj mineStackObj, long to) {
		objectCountMap.put(mineStackObj, to);
	}

	/**
	 * 指定されたマインスタックオブジェクトのスタック数を増加させる。
	 */
	public void addStackedAmountOf(@NotNull MineStackObj mineStackObj, long by) {
		final long currentAmount = getStackedAmountOf(mineStackObj);
		setStackedAmountOf(mineStackObj, currentAmount + by);
	}

	/**
	 * 指定されたマインスタックオブジェクトのスタック数を減少させる。
	 */
	public void subtractStackedAmountOf(@NotNull MineStackObj mineStackObj, long by) {
		addStackedAmountOf(mineStackObj, -by);
	}

}
