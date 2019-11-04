package com.github.unchama.seichiassist.data;

import com.github.unchama.seichiassist.util.Util;
import com.github.unchama.seichiassist.util.Util.DirectionType;

import java.util.Map;

/**
 * グリッド式保護設定(テンプレート)を保存するためのクラス
 *
 * @author karayuu
 * 2017/9/11
 */
public class GridTemplate {
    private int aheadAmount;
    private int behindAmount;
    private int rightAmount;
    private int leftAmount;

    //コンストラクタ
    public GridTemplate(int aheadAmount, int behindAmount, int rightAmount, int leftAmount) {
        this.aheadAmount = aheadAmount;
        this.behindAmount = behindAmount;
        this.rightAmount = rightAmount;
        this.leftAmount = leftAmount;

    }

    //ゲッター
    public int getAheadAmount() {
        return aheadAmount;
    }

    public int getBehindAmount() {
        return behindAmount;
    }

    public int getRightAmount() {
        return rightAmount;
    }

    public int getLeftAmount() {
        return leftAmount;
    }

    //セッター
    public void setChunkAmount(Map<DirectionType, Integer> setMap) {
        this.aheadAmount = setMap.get(Util.DirectionType$.MODULE$.ahead());
        this.behindAmount = setMap.get(Util.DirectionType$.MODULE$.behind());
        this.rightAmount = setMap.get(Util.DirectionType$.MODULE$.right());
        this.leftAmount = setMap.get(Util.DirectionType$.MODULE$.left());
    }

    /**
     * 空かどうか
     *
     * @return true: 空 / false: 空でない
     */
    public boolean isEmpty() {
        return this.aheadAmount == 0 && this.behindAmount == 0 && this.rightAmount == 0 && this.leftAmount == 0;
    }

    @Override
    public String toString() {
        return "前方向:" + this.aheadAmount + ",後ろ方向:"
                + this.behindAmount + ",右方向:" + this.rightAmount + ",左方向:" + this.leftAmount;
    }
}
