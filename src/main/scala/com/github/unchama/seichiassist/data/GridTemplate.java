package com.github.unchama.seichiassist.data;

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

    @Override
    public String toString() {
        return "前方向:" + this.aheadAmount + ",後ろ方向:"
                + this.behindAmount + ",右方向:" + this.rightAmount + ",左方向:" + this.leftAmount;
    }
}
