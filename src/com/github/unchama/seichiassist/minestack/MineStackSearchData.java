package com.github.unchama.seichiassist.minestack;

import com.github.unchama.seichiassist.*;

/**
 * Created by karayuu on 2018/06/20
 */
public class MineStackSearchData {
    private int index;
    private MineStackObj obj;
    private String jpName;
    private String enName;
    private String katakana;

    MineStackSearchData(MineStackObj obj, String enName, String katakana) {
        this.index = SeichiAssist.minestacklist.indexOf(obj);
        this.obj = obj;
        this.jpName = obj.getJapaneseName();
        this.enName = enName;
        this.katakana = katakana;
    }
}
