package com.github.unchama.seichiassist.minestack;

/**
 * @author karayuu
 */
public class HistoryData {
    public int index;
    public MineStackObj obj;

    HistoryData(int index, MineStackObj obj) {
        this.index = index;
        this.obj = obj;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof HistoryData)) {
            return false;
        }

        HistoryData data = (HistoryData) object;
        return this.index == data.index;
    }
}
