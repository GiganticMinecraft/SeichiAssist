package com.github.unchama.seichiassist.minestack;

import org.bukkit.*;

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

    public List<MineStackObj> getObjs() {
        List<MineStackObj> result = new ArrayList<>();
        for (HistoryData data : historyList) {
            result.add(data.obj);
        }
        return result;
    }

    /**
     * 重複チェック
     * @param index MineStackListの要素の番号
     * @return true: 重複 / false: 重複していない
     */
    private boolean isDuplicated(int index) {
        //要素の番号はサーバ起動時から変化しないので,要素の番号だけで重複を判断してもかまわない。
        for (HistoryData data : historyList) {
            if (data.index == index) {
                return true;
            }
        }
        //ここまで来たら要素としてないので重複はありえない。
        return false;
    }
}
