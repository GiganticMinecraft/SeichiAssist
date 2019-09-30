package com.github.unchama.seichiassist.data;

import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.player.PlayerData;
import com.github.unchama.seichiassist.util.Util;
import com.github.unchama.seichiassist.util.Util.DirectionType;
import com.github.unchama.seichiassist.util.external.ExternalPlugins;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import scala.Int;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 保護関連メニュー
 *
 * @author karayuu
 */
public class RegionMenuData {
    static WorldGuardPlugin Wg = ExternalPlugins.getWorldGuard();
    static WorldEditPlugin We = ExternalPlugins.getWorldEdit();
    static Config config = SeichiAssist.seichiAssistConfig();
    static NumberFormat nfNum = NumberFormat.getNumberInstance();

    /**
     * グリッド式保護メニュを開きます。
     *
     * @param player
     * @return
     */
    public static Inventory getGridWorldGuardMenu(Player player) {
        PlayerData playerData = SeichiAssist.playermap().getOrElse(player.getUniqueId(), () -> null);
        scala.collection.immutable.Map<DirectionType, Object> unitMap = playerData.unitMap();
        Map<DirectionType, String> directionMap = getPlayerDirectionString(player);

        Inventory gridInv = Bukkit.createInventory(null, InventoryType.DISPENSER,
                ChatColor.LIGHT_PURPLE + "グリッド式保護設定メニュー");

        //0マス目
        List<String> lore0 = new ArrayList<>();
        lore0.add(ChatColor.GREEN + "現在のユニット指定量");
        lore0.add(ChatColor.AQUA + "" + playerData.unitPerClick() + ChatColor.GREEN + "ユニット(" +
                ChatColor.AQUA + playerData.unitPerClick() * 15 + ChatColor.GREEN + "ブロック)/1クリック");
        lore0.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "クリックで変更");
        ItemStack menuicon0 = Util.getMenuIcon(Material.STAINED_GLASS_PANE, 1, 0,
                ChatColor.GREEN + "拡張単位の変更", lore0, true);
        gridInv.setItem(0, menuicon0);

        //1マス目
        List<String> lore1 = getGridLore(directionMap.get(DirectionType.AHEAD$.MODULE$), unitMap.getOrElse(DirectionType.AHEAD$.MODULE$, () -> null));
        if (!playerData.canGridExtend(DirectionType.AHEAD$.MODULE$, player.getWorld().getName())) {
            lore1.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "これ以上拡張できません");
        } else if (!playerData.canGridReduce(DirectionType.AHEAD$.MODULE$)) {
            lore1.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "これ以上縮小できません");
        }
        ItemStack menuicon1 = Util.getMenuIcon(Material.STAINED_GLASS_PANE, 1, 14,
                ChatColor.DARK_GREEN + "前に" + playerData.unitPerClick() + "ユニット増やす/減らす", lore1, true);
        gridInv.setItem(1, menuicon1);

        //2マス目
        List<String> lore2 = new ArrayList<>();
        lore2.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "クリックで開く");
        ItemStack menuicon2 = Util.getMenuIcon(Material.CHEST, 1, ChatColor.GREEN + "設定保存メニュー",
                lore2, true);
        gridInv.setItem(2, menuicon2);

        //3マス目
        List<String> lore3 = getGridLore(directionMap.get(DirectionType.LEFT$.MODULE$), unitMap.getOrElse(DirectionType.LEFT$.MODULE$, () -> null));
        if (!playerData.canGridExtend(DirectionType.LEFT$.MODULE$, player.getWorld().getName())) {
            lore3.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "これ以上拡張できません");
        } else if (!playerData.canGridReduce(DirectionType.LEFT$.MODULE$)) {
            lore3.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "これ以上縮小できません");
        }
        ItemStack menuicon3 = Util.getMenuIcon(Material.STAINED_GLASS_PANE, 1, 10,
                ChatColor.DARK_GREEN + "左に" + playerData.unitPerClick() + "ユニット増やす/減らす", lore3, true);
        gridInv.setItem(3, menuicon3);

        //4マス目
        List<String> lore4 = new ArrayList<>();
        lore4.add(ChatColor.GRAY + "現在の設定");
        lore4.add(ChatColor.GRAY + "前方向：" + ChatColor.AQUA + unitMap.getOrElse(DirectionType.AHEAD$.MODULE$, () -> null) + ChatColor.GRAY + "ユニット"
                + "(" + ChatColor.AQUA + nfNum.format(((Int)unitMap.apply(DirectionType.AHEAD$.MODULE$)).toInt() * 15) + ChatColor.GRAY + "ブロック)");
        lore4.add(ChatColor.GRAY + "後ろ方向：" + ChatColor.AQUA + unitMap.getOrElse(DirectionType.BEHIND$.MODULE$, () -> null) + ChatColor.GRAY + "ユニット"
                + "(" + ChatColor.AQUA + nfNum.format(((Int)unitMap.apply(DirectionType.BEHIND$.MODULE$)).toInt() * 15) + ChatColor.GRAY + "ブロック)");
        lore4.add(ChatColor.GRAY + "右方向：" + ChatColor.AQUA + unitMap.getOrElse(DirectionType.RIGHT$.MODULE$, () -> null) + ChatColor.GRAY + "ユニット"
                + "(" + ChatColor.AQUA + nfNum.format(((Int)unitMap.apply(DirectionType.RIGHT$.MODULE$)).toInt() * 15) + ChatColor.GRAY + "ブロック)");
        lore4.add(ChatColor.GRAY + "左方向：" + ChatColor.AQUA + unitMap.getOrElse(DirectionType.LEFT$.MODULE$, () -> null) + ChatColor.GRAY + "ユニット"
                + "(" + ChatColor.AQUA + nfNum.format(((Int)unitMap.apply(DirectionType.LEFT$.MODULE$)).toInt() * 15) + ChatColor.GRAY + "ブロック)");
        lore4.add(ChatColor.GRAY + "保護ユニット数：" + ChatColor.AQUA + playerData.gridChunkAmount());
        lore4.add(ChatColor.GRAY + "保護ユニット上限値：" + ChatColor.RED + config.getGridLimitPerWorld(player.getWorld().getName()));
        ItemStack menuicon4 = Util.getMenuIcon(Material.STAINED_GLASS_PANE, 1, 11, ChatColor.DARK_GREEN + "設定",
                lore4, true);
        gridInv.setItem(4, menuicon4);

        //5マス目
        List<String> lore5 = getGridLore(directionMap.get(DirectionType.RIGHT$.MODULE$), unitMap.getOrElse(DirectionType.RIGHT$.MODULE$, () -> null));
        if (!playerData.canGridExtend(DirectionType.RIGHT$.MODULE$, player.getWorld().getName())) {
            lore5.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "これ以上拡張できません");
        } else if (!playerData.canGridReduce(DirectionType.RIGHT$.MODULE$)) {
            lore5.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "これ以上縮小できません");
        }
        ItemStack menuicon5 = Util.getMenuIcon(Material.STAINED_GLASS_PANE, 1, 5,
                ChatColor.DARK_GREEN + "右に" + playerData.unitPerClick() + "ユニット増やす/減らす", lore5, true);
        gridInv.setItem(5, menuicon5);

        //6マス目
        List<String> lore6 = new ArrayList<>();
        lore6.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "取扱注意！！");
        ItemStack menuicon6 = Util.getMenuIcon(Material.STAINED_GLASS_PANE, 1, 4, ChatColor.RED + "全設定リセット",
                lore6, true);
        gridInv.setItem(6, menuicon6);

        //7マス目
        List<String> lore7 = getGridLore(directionMap.get(DirectionType.BEHIND$.MODULE$), unitMap.getOrElse(DirectionType.BEHIND$.MODULE$, () -> null));
        if (!playerData.canGridExtend(DirectionType.BEHIND$.MODULE$, player.getWorld().getName())) {
            lore7.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "これ以上拡張できません");
        } else if (!playerData.canGridReduce(DirectionType.BEHIND$.MODULE$)) {
            lore7.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "これ以上縮小できません");
        }
        ItemStack menuicon7 = Util.getMenuIcon(Material.STAINED_GLASS_PANE, 1, 13,
                ChatColor.DARK_GREEN + "後ろに" + playerData.unitPerClick() + "ユニット増やす/減らす", lore7, true);
        gridInv.setItem(7, menuicon7);

        //8マス目
        if (!config.isGridProtectEnable(player)) {
            List<String> lore8 = new ArrayList<>();
            lore8.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "このワールドでは保護を作成できません");
            ItemStack menuicon8 = Util.getMenuIcon(Material.WOOL, 1, 14, ChatColor.RED + "保護作成",
                    lore8, true);
            gridInv.setItem(8, menuicon8);
        } else if (!playerData.canCreateRegion()) {
            List<String> lore8 = new ArrayList<>();
            lore8.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "以下の原因により保護を作成できません");
            lore8.add(ChatColor.RED + "・保護の範囲が他の保護と重複している");
            lore8.add(ChatColor.RED + "・保護の作成上限に達している");
            ItemStack menuicon8 = Util.getMenuIcon(Material.WOOL, 1, 14, ChatColor.RED + "保護作成",
                    lore8, true);
            gridInv.setItem(8, menuicon8);
        } else {
            List<String> lore8 = new ArrayList<>();
            lore8.add(ChatColor.DARK_GREEN + "保護作成可能です");
            lore8.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "クリックで作成");
            ItemStack menuicon8 = Util.getMenuIcon(Material.WOOL, 1, 11, ChatColor.GREEN + "保護作成",
                    lore8, true);
            gridInv.setItem(8, menuicon8);
        }
        return gridInv;
    }

    private static List<String> getGridLore(String direction, int unit) {
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "左クリックで増加");
        lore.add(ChatColor.RESET + "" + ChatColor.RED + "右クリックで減少");
        lore.add(ChatColor.RESET + "" + ChatColor.GRAY + ChatColor.GRAY + "---------------");
        lore.add(ChatColor.GRAY + "方向：" + ChatColor.AQUA + direction);
        lore.add(ChatColor.GRAY + "現在の指定方向ユニット数：" + ChatColor.AQUA + unit
                + ChatColor.GRAY + "(" + ChatColor.AQUA + nfNum.format(unit * 15) + ChatColor.GRAY + "ブロック)");

        return lore;
    }

    private static Map<DirectionType, String> getPlayerDirectionString(Player player) {
        double rotation = (player.getLocation().getYaw() + 180) % 360;
        HashMap<DirectionType, String> directionMap = new HashMap<>();

        if (rotation < 0) {
            rotation += 360;
        }

        //0,360:south 90:west 180:north 270:east
        if (0.0 <= rotation && rotation < 45.0) {
            //前が北(North)
            directionMap.put(DirectionType.BEHIND$.MODULE$, "南(South)");
            directionMap.put(DirectionType.AHEAD$.MODULE$, "北(North)");
            directionMap.put(DirectionType.LEFT$.MODULE$, "西(West)");
            directionMap.put(DirectionType.RIGHT$.MODULE$, "東(East)");
        } else if (45.0 <= rotation && rotation < 135.0) {
            //前が東(East)
            directionMap.put(DirectionType.RIGHT$.MODULE$, "南(South)");
            directionMap.put(DirectionType.LEFT$.MODULE$, "北(North)");
            directionMap.put(DirectionType.BEHIND$.MODULE$, "西(West)");
            directionMap.put(DirectionType.AHEAD$.MODULE$, "東(East)");
        } else if (135.0 <= rotation && rotation < 225.0) {
            //前が南(South)
            directionMap.put(DirectionType.AHEAD$.MODULE$, "南(South)");
            directionMap.put(DirectionType.BEHIND$.MODULE$, "北(North)");
            directionMap.put(DirectionType.RIGHT$.MODULE$, "西(West)");
            directionMap.put(DirectionType.LEFT$.MODULE$, "東(East)");
        } else if (225.0 <= rotation && rotation < 315.0) {
            //前が西(West)
            directionMap.put(DirectionType.LEFT$.MODULE$, "南(South)");
            directionMap.put(DirectionType.RIGHT$.MODULE$, "北(North)");
            directionMap.put(DirectionType.AHEAD$.MODULE$, "西(West)");
            directionMap.put(DirectionType.BEHIND$.MODULE$, "東(East)");
        } else if (315.0 <= rotation && rotation < 360.0) {
            //前が北(North)
            directionMap.put(DirectionType.BEHIND$.MODULE$, "南(South)");
            directionMap.put(DirectionType.AHEAD$.MODULE$, "北(North)");
            directionMap.put(DirectionType.LEFT$.MODULE$, "西(West)");
            directionMap.put(DirectionType.RIGHT$.MODULE$, "東(East)");
        }
        return directionMap;
    }

    /**
     * グリッド式保護設定保存メニューを取得します。
     *
     * @param player プレイヤー
     * @return グリッド式保護・設定保存Inventory
     */
    public static Inventory getGridTemplateInventory(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9 * (getAisleAmount() + 1), ChatColor.LIGHT_PURPLE + "グリッド式保護・設定保存");

        for (int i = 0; i <= config.getTemplateKeepAmount() - 1; i++) {
            inv.setItem(i, getGridtempMenuicon(i, player));
        }

        //戻るボタン
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "" + "クリックで戻る");
        ItemStack retIcon = Util.getMenuIcon(Material.BARRIER, 1, ChatColor.RED + "グリッド式保護メニューに戻る",
                lore, true);
        inv.setItem(getAisleAmount() * 9, retIcon);

        return inv;
    }

    /**
     * テンプレートメニュー用。
     *
     * @return グリッド式保護テンプレート保存メニューの縦の数
     */
    public static int getAisleAmount() {
        return config.getTemplateKeepAmount() / 9 + 1;
    }

    /**
     * テンプレートメニュー用。メニューアイコン作成
     *
     * @param i      アイコン番号
     * @param player プレイヤー
     * @return メニューアイコン
     */
    private static ItemStack getGridtempMenuicon(int i, Player player) {
        PlayerData playerData = SeichiAssist.playermap().getOrElse(player.getUniqueId(), () -> null);
        @Nullable GridTemplate template = playerData.templateMap().getOrElse(i, () -> null);

        if (template != null) {
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GREEN + "設定内容");
            lore.add(ChatColor.GRAY + "前方向：" + ChatColor.AQUA + template.getAheadAmount() + ChatColor.GRAY + "ユニット");
            lore.add(ChatColor.GRAY + "後ろ方向：" + ChatColor.AQUA + template.getBehindAmount() + ChatColor.GRAY + "ユニット");
            lore.add(ChatColor.GRAY + "右方向：" + ChatColor.AQUA + template.getRightAmount() + ChatColor.GRAY + "ユニット");
            lore.add(ChatColor.GRAY + "左方向：" + ChatColor.AQUA + template.getLeftAmount() + ChatColor.GRAY + "ユニット");
            lore.add(ChatColor.GREEN + "左クリックで設定を読み込み");
            lore.add(ChatColor.RED + "右クリックで現在の設定で上書き");
            return Util.getMenuIcon(Material.CHEST, 1,
                    ChatColor.GREEN + "テンプレNo." + (i + 1) + "(設定済)", lore, true);
        } else {
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GREEN + "未設定");
            lore.add(ChatColor.RED + "左クリックで現在の設定を保存");
            return Util.getMenuIcon(Material.PAPER, 1,
                    ChatColor.RED + "テンプレNo." + (i + 1), lore, true);
        }
    }
}
