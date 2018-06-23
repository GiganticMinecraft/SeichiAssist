package com.github.unchama.seichiassist.minestack;

import com.github.unchama.seichiassist.minestack.objects.*;
import org.bukkit.*;

/**
 * Created by karayuu on 2018/06/20
 */
public enum MineStackObjs {
    /* ※objNameは変更禁止！SQLのカラムで使用しているため、スペルミス等を修正するとデータが無くなってしまいます！ */
    GRASS(new MineStackMineObj("grass","草ブロック",1,Material.GRASS,0)),
    COBBLESTONE(new MineStackMineObj("cobblestone","丸石",1,Material.COBBLESTONE, 0)),
    STONE(new MineStackMineObj("stone","石",1,Material.STONE,0)),
    GRANITE(new MineStackMineObj("granite","花崗岩",1,Material.STONE,1)),
    DIORITE(new MineStackMineObj("diorite","閃緑岩",1,Material.STONE,3)),
    ANDESITE(new MineStackMineObj("andesite","安山岩",1,Material.STONE,5)),
    GRAVEL(new MineStackMineObj("gravel","砂利",1,Material.GRAVEL,0)),
    SAND(new MineStackMineObj("sand","砂",1,Material.SAND,0)),
    SANDSTONE(new MineStackMineObj("sandstone","砂岩",1,Material.SANDSTONE,0)),
    NETHERRACK(new MineStackMineObj("netherrack","ネザーラック",1,Material.NETHERRACK,0)),
    SOUL_SAND(new MineStackMineObj("soul_sand","ソウルサンド",1,Material.SOUL_SAND,0)),
    COAL(new MineStackMineObj("coal","石炭",1,Material.COAL,0)),
    CHARCOAL(new MineStackMineObj("coal_1", "木炭",1,  Material.COAL, 1)),
    COAL_ORE(new MineStackMineObj("coal_ore","石炭鉱石",1,Material.COAL_ORE,0)),
    END_STONE(new MineStackMineObj("ender_stone","エンドストーン",1,Material.ENDER_STONE,0)),
    IRON_ORE(new MineStackMineObj("iron_ore","鉄鉱石",1,Material.IRON_ORE,0)),
    OBSIDIAN(new MineStackMineObj("obsidian","黒曜石",1,Material.OBSIDIAN,0)),
    PACKED_ICE(new MineStackMineObj("packed_ice","氷塊",1,Material.PACKED_ICE, 0)),
    QUARTZ(new MineStackMineObj("quartz","ネザー水晶",1,Material.QUARTZ,0)),
    QUARTZ_ORE(new MineStackMineObj("quartz_ore","ネザー水晶鉱石",1,Material.QUARTZ_ORE,0)),
    MAGMA(new MineStackMineObj("magma","マグマブロック",1,Material.MAGMA,0)),
    GOLD_ORE(new MineStackMineObj("gold_ore","金鉱石",1,Material.GOLD_ORE,0)),
    GLOWSTONE(new MineStackMineObj("glowstone","グロウストーン",1,Material.GLOWSTONE,0)),
    REDSTONE_ORE(new MineStackMineObj("redstone_ore","レッドストーン鉱石",1,Material.REDSTONE_ORE,0)),
    LAPIS_LAZULI(new MineStackMineObj("lapis_lazuli","ラピスラズリ",1,Material.INK_SACK,4)),
    LAPIS_ORE(new MineStackMineObj("lapis_ore","ラピスラズリ鉱石",1,Material.LAPIS_ORE,0)),
    DIAMOND(new MineStackMineObj("diamond","ダイヤモンド",1,Material.DIAMOND,0)),
    DIAMOND_ORE(new MineStackMineObj("diamond_ore","ダイヤモンド鉱石",1,Material.DIAMOND_ORE,0)),
    EMERALD(new MineStackMineObj("emerald","エメラルド",1,Material.EMERALD,0)),
    EMERALD_ORE(new MineStackMineObj("emerald_ore","エメラルド鉱石",1,Material.EMERALD_ORE,0)),
    LAPIS_BLOCK(new MineStackMineObj("lapis_block", "ラピスラズリブロック", 1, Material.LAPIS_BLOCK, 0)),
    IRON_BLOCK(new MineStackMineObj("iron_block", "鉄ブロック", 1, Material.IRON_BLOCK, 0)),
    GOLD_BLOCK(new MineStackMineObj("gold_block", "金ブロック", 1, Material.GOLD_BLOCK, 0)),
    DIAMOND_BLOCK(new MineStackMineObj("diamond_block", "ダイヤモンドブロック", 1, Material.DIAMOND_BLOCK, 0)),
    RED_SAND(new MineStackMineObj("red_sand","赤い砂",1,Material.SAND,1)),
    RED_SANDSTONE(new MineStackMineObj("red_sandstone","赤い砂岩",1,Material.RED_SANDSTONE,0)),
    HARDENED_CLAY(new MineStackMineObj("hard_clay","堅焼き粘土",1,Material.HARD_CLAY,0)),
    WHITE_STAINED_HARDENED_CLAY(new MineStackMineObj("stained_clay","白色の堅焼き粘土",1,Material.STAINED_CLAY,0)),
    ORANGE_STAINED_HARDENED_CLAY(new MineStackMineObj("stained_clay1","橙色の堅焼き粘土",1,Material.STAINED_CLAY,1)),
    YELLOW_STAINED_HARDENED_CLAY(new MineStackMineObj("stained_clay4","黄色の堅焼き粘土",1,Material.STAINED_CLAY,4)),
    LIGHT_GRAY_STAINED_HARDENED_CLAY(new MineStackMineObj("stained_clay8","薄灰色の堅焼き粘土",1,Material.STAINED_CLAY,8)),



    ;

    public final MineStackObj obj;
    MineStackObjs(MineStackObj obj) {
        this.obj = obj;
    }
}
