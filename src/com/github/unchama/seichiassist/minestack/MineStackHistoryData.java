package com.github.unchama.seichiassist.minestack;

import java.util.*;

/**
 * Created by karayuu on 2018/06/13
 */
public final class MineStackHistoryData {
    private List<HistoryData> historyList = new ArrayList<>();

    /**
     * 履歴に追加します。ただし、データの保存可能な最大値を超えていた場合、先頭から削除されます。
     */
    public void add(int index, MineStackObj obj) {
        final int MAX = 27;
        HistoryData data = new HistoryData(index, obj);
        if (historyList.contains(data)) {
            historyList.remove(data);
            historyList.add(data);
            return;
        }
        if (historyList.size() >= MAX) {
            historyList.remove(0);
        }
        historyList.add(data);
    }

    /**
     * 履歴のListを返します。
     */
    public List<HistoryData> getHistoryList() {
        return historyList;
    }
}
