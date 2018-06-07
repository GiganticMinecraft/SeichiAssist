package com.github.unchama.seichiassist.minestack.objects;

import com.github.unchama.seichiassist.minestack.*;
import org.bukkit.*;

import java.util.*;

/**
 * Created by karayuu on 2018/06/04
 */
public class MineStackGachaObj extends MineStackObj {
    public MineStackGachaObj(String objname, String japanesename, int level, Material material, int durability) {
        super(objname, japanesename, level, material, durability, false, -1, 5);
    }

    public MineStackGachaObj(String objname, String japanesename, int level, Material material, int durability, List<String> lore) {
        super(objname, japanesename, level, material, durability, true, -1,  lore, 5);
    }
}
