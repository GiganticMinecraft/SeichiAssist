package com.github.unchama.seichiassist.data;

public class RankData {
    public String name;
    /* 整地Lv */
    public final int level;
    /* 整地量 */
    public final long totalbreaknum;
    /* プレイ時間 (ティック) */
    public long playtick;
    /* 投票回数 */
    public int p_vote;
    /* ??? */
    public int p_apple;

    public RankData() {
        name = "";
        level = 1;
        totalbreaknum = 0;
    }
}
