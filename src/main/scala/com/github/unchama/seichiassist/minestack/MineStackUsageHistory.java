package com.github.unchama.seichiassist.minestack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karayuu on 2018/06/13
 */
public final class MineStackUsageHistory {
    private static final int LIST_MAX_SIZE = 27;

    public List<MineStackObj> usageHistory = new ArrayList<>();

    /**
     * 履歴に追加します。ただし、データの保存可能な最大値を超えていた場合、先頭から削除されます。
     */
    public void add(MineStackObj obj) {
        usageHistory.remove(obj);
        usageHistory.add(obj);
        if (usageHistory.size() > LIST_MAX_SIZE) {
            usageHistory.remove(0);
        }
    }
}
