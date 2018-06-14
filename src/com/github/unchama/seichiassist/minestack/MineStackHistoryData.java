package com.github.unchama.seichiassist.minestack;

import java.util.*;

/**
 * Created by karayuu on 2018/06/13
 */
public final class MineStackHistoryData {
    private Map<Integer, MineStackObj> historyMap = new HashMap<>();

    /**
     * 履歴に追加します。ただし、データの保存可能な最大値を超えていた場合、先頭から削除されます。
     */
    public void add(int index, MineStackObj obj) {
        final int MAX = 27;
        if (historyMap.containsValue(obj)) {
            historyMap.remove(index, obj);
            historyMap.put(index, obj);
            return;
        }
        if (historyMap.size() >= MAX) {
            historyMap = removeAtFirst(historyMap);
        }
        historyMap.put(index, obj);
    }

    /**
     * 履歴のMapを返します。
     */
    public Map<Integer, MineStackObj> getHistoryMap() {
        return historyMap;
    }

    private Map<Integer, MineStackObj> removeAtFirst(Map<Integer, MineStackObj> objMap) {
        Map<Integer, MineStackObj> result = new HashMap<>();

        boolean isFirst = true;
        for (Map.Entry<Integer, MineStackObj> entry : objMap.entrySet()) {
            if (isFirst) {
                isFirst = false;
            } else {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}
