package com.github.unchama.seichiassist.minestack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karayuu on 2018/06/13
 */
public final class MineStackHistoryData {
	private static final int LIST_MAX_SIZE = 27;

	@Deprecated
	private List<HistoryData> historyList = new ArrayList<>();

	private List<MineStackObj> usageHistory = new ArrayList<>();

	/**
	 * 履歴に追加します。ただし、データの保存可能な最大値を超えていた場合、先頭から削除されます。
	 */
	public void add(MineStackObj obj) {
		// ---------------------------
		// TODO remove this section
		// ---------------------------
		HistoryData data = new HistoryData(obj);
		if (historyList.contains(data)) {
			historyList.remove(data);
			historyList.add(data);
		}
		if (historyList.size() >= LIST_MAX_SIZE) {
			historyList.remove(0);
		}
		historyList.add(data);
		// ---------------------------

		usageHistory.remove(obj);
		usageHistory.add(obj);
		if (usageHistory.size() > LIST_MAX_SIZE) {
			usageHistory.remove(0);
		}
	}

	/**
	 * 履歴のListを返します。
	 */
	@Deprecated
	public List<HistoryData> getHistoryList() {
		return historyList;
	}

	public List<MineStackObj> getUsageHistory() {
		return usageHistory;
	}
}
