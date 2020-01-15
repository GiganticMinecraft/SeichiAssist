package com.github.unchama.seichiassist.data;

public class RankData {
    public String name;
    /* 整地レベル */
    public int level;
    /* 整地量 */
    public long totalbreaknum;
    /* プレイ時間 (ティック) */
    public int playtick;
    /* 投票回数 */
    public int p_vote;
    /* プレミアムエフェクトポイント */
    public int premiumeffectpoint;
    /* ??? */
    public int p_apple;

    public RankData() {
        name = "";
        level = 1;
        totalbreaknum = 0;
    }
}
