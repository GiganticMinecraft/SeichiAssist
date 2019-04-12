package com.github.unchama.seichiassist.minestack.objects;

import com.github.unchama.seichiassist.minestack.*;
import org.bukkit.*;

/**
 * Created by karayuu on 2018/06/04
 */
public final class MineStackRsObj extends MineStackObj {
    public MineStackRsObj(String objname, String japanesename, int level, Material material, int durability) {
        super(objname, japanesename, level, material, durability, false, -1, 4);
    }
}
