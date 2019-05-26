package com.github.unchama.seichiassist;

import com.github.unchama.seichiassist.effect.arrow.ArrowMagicTask;
import com.github.unchama.seichiassist.effect.breaking.MagicTask;
import com.github.unchama.seichiassist.data.Coordinate;
import com.github.unchama.seichiassist.data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public enum ActiveSkillPremiumEffect {
    MAGIC(1, "ef_magic", ChatColor.RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "マジック", "鶏が出る手品", 10, Material.RED_ROSE),
    ;

    SeichiAssist plugin = SeichiAssist.instance;

    private int num;
    private String sql_name;
    private String name;
    private String explain;
    private int usepoint;
    private Material material;

    ActiveSkillPremiumEffect(int num, String sql_name, String name, String explain, int usepoint, Material material) {
        this.num = num;
        this.sql_name = sql_name;
        this.name = name;
        this.explain = explain;
        this.usepoint = usepoint;
        this.material = material;
    }

    public int getNum() {
        return this.num;
    }

    public String getsqlName() {
        return this.sql_name;
    }

    public String getName() {
        return this.name;
    }

    public String getExplain() {
        return this.explain;
    }

    public int getUsePoint() {
        return this.usepoint;
    }

    public Material getMaterial() {
        return this.material;
    }

    //プレイヤーが所持しているかどうか
    public Boolean isObtained(Map<Integer, Boolean> flagmap) {
        return flagmap.get(getNum());
    }

    //獲得させる処理
    public void setObtained(Map<Integer, Boolean> flagmap) {
        flagmap.put(getNum(), true);
    }

    //エフェクトの実行処理分岐 範囲破壊と複数範囲破壊
    public void runBreakEffect(Player player, PlayerData playerdata, ItemStack tool, List<Block> breaklist, Coordinate start, Coordinate end, Location standard) {
        switch (this.toString()) {
            case "MAGIC":
                if (SeichiAssist.DEBUG) {
                    new MagicTask(player, playerdata, tool, breaklist, start, end, standard).runTaskTimer(plugin, 0, 100);
                } else {
                    new MagicTask(player, playerdata, tool, breaklist, start, end, standard).runTaskTimer(plugin, 0, 10);
                }
                break;
            default:
                break;
        }
    }

    //エフェクトの実行処理分岐
    public void runArrowEffect(Player player) {
        switch (this.toString()) {
            case "MAGIC":
                new ArrowMagicTask(player).runTaskTimer(plugin, 0, 1);
                break;
            default:
                break;
        }
    }

    public static @Nullable ActiveSkillPremiumEffect fromSqlName(String sqlName) {
        return Arrays
            .stream(ActiveSkillPremiumEffect.values())
            .filter((effect) -> sqlName.equals(effect.sql_name))
            .findFirst()
            .orElse(null);
    }
}
