package com.github.unchama.seichiassist.data;

import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.player.PlayerData;
import com.github.unchama.seichiassist.util.MenuIcon;
import com.github.unchama.seichiassist.util.MenuIcon;
import com.github.unchama.seichiassist.util.RelativeDirection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import scala.jdk.CollectionConverters;

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
    private static final Config config = SeichiAssist.seichiAssistConfig();
    private static final NumberFormat nfNum = NumberFormat.getNumberInstance();

    /**
     * グリッド式保護メニュを開きます。
     *
     * @param player
     * @return
     */
    public static Inventory getGridWorldGuardMenu(Player player) {
        PlayerData playerData = SeichiAssist.playermap().getOrElse(player.getUniqueId(), () -> null);
        scala.collection.immutable.Map<RelativeDirection, Object> unitMap = playerData.unitMap();
        Map<RelativeDirection, String> directionMap = getPlayerDirectionString(player);

        Inventory gridInv = Bukkit.createInventory(null, InventoryType.DISPENSER,
                ChatColor.LIGHT_PURPLE + "グリッド式保護設定メニュー");

        //0マス目
        List<String> lore0 = new ArrayList<>();
        lore0.add(ChatColor.GREEN + "現在のユニット指定量");
        lore0.add(ChatColor.AQUA + "" + playerData.unitPerClick() + ChatColor.GREEN + "ユニット(" +
                ChatColor.AQUA + playerData.unitPerClick() * 15 + ChatColor.GREEN + "ブロック)/1クリック");
        lore0.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "クリックで変更");
        ItemStack menuicon0 = MenuIcon.getMenuIcon(Material.WHITE_STAINED_GLASS_PANE, 1, 0,
                ChatColor.GREEN + "拡張単位の変更", CollectionConverters.ListHasAsScala(lore0).asScala().toList(), true);
        gridInv.setItem(0, menuicon0);

        //1マス目
        List<String> lore1 = getGridLore(directionMap.get(RelativeDirection.ahead()), unitMap.getOrElse(RelativeDirection.ahead(), () -> null));
        if (!playerData.canGridExtend(RelativeDirection.ahead(), player.getWorld().getName())) {
            lore1.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "これ以上拡張できません");
        } else if (!playerData.canGridReduce(RelativeDirection.ahead())) {
            lore1.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "これ以上縮小できません");
        }
        ItemStack menuicon1 = MenuIcon.getMenuIcon(Material.RED_STAINED_GLASS_PANE, 1, 14,
                ChatColor.DARK_GREEN + "前に" + playerData.unitPerClick() + "ユニット増やす/減らす", CollectionConverters.ListHasAsScala(lore1).asScala().toList(), true);
        gridInv.setItem(1, menuicon1);

        //2マス目
        List<String> lore2 = new ArrayList<>();
        lore2.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "クリックで開く");
        ItemStack menuicon2 = MenuIcon.getMenuIcon(Material.CHEST, 1, ChatColor.GREEN + "設定保存メニュー",
                CollectionConverters.ListHasAsScala(lore2).asScala().toList(), true);
        gridInv.setItem(2, menuicon2);

        //3マス目
        List<String> lore3 = getGridLore(directionMap.get(RelativeDirection.left()), unitMap.getOrElse(RelativeDirection.left(), () -> null));
        if (!playerData.canGridExtend(RelativeDirection.left(), player.getWorld().getName())) {
            lore3.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "これ以上拡張できません");
        } else if (!playerData.canGridReduce(RelativeDirection.left())) {
            lore3.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "これ以上縮小できません");
        }
        ItemStack menuicon3 = MenuIcon.getMenuIcon(Material.PURPLE_STAINED_GLASS_PANE, 1, 10,
                ChatColor.DARK_GREEN + "左に" + playerData.unitPerClick() + "ユニット増やす/減らす", CollectionConverters.ListHasAsScala(lore3).asScala().toList(), true);
        gridInv.setItem(3, menuicon3);

        //4マス目
        List<String> lore4 = new ArrayList<>();
        lore4.add(ChatColor.GRAY + "現在の設定");
        lore4.add(ChatColor.GRAY + "前方向：" + ChatColor.AQUA + unitMap.getOrElse(RelativeDirection.ahead(), () -> null) + ChatColor.GRAY + "ユニット"
                + "(" + ChatColor.AQUA + nfNum.format((Integer) unitMap.apply(RelativeDirection.ahead()) * 15) + ChatColor.GRAY + "ブロック)");
        lore4.add(ChatColor.GRAY + "後ろ方向：" + ChatColor.AQUA + unitMap.getOrElse(RelativeDirection.behind(), () -> null) + ChatColor.GRAY + "ユニット"
                + "(" + ChatColor.AQUA + nfNum.format((Integer) unitMap.apply(RelativeDirection.behind()) * 15) + ChatColor.GRAY + "ブロック)");
        lore4.add(ChatColor.GRAY + "右方向：" + ChatColor.AQUA + unitMap.getOrElse(RelativeDirection.right(), () -> null) + ChatColor.GRAY + "ユニット"
                + "(" + ChatColor.AQUA + nfNum.format((Integer) unitMap.apply(RelativeDirection.right()) * 15) + ChatColor.GRAY + "ブロック)");
        lore4.add(ChatColor.GRAY + "左方向：" + ChatColor.AQUA + unitMap.getOrElse(RelativeDirection.left(), () -> null) + ChatColor.GRAY + "ユニット"
                + "(" + ChatColor.AQUA + nfNum.format((Integer) unitMap.apply(RelativeDirection.left()) * 15) + ChatColor.GRAY + "ブロック)");
        lore4.add(ChatColor.GRAY + "保護ユニット数：" + ChatColor.AQUA + playerData.gridChunkAmount());
        lore4.add(ChatColor.GRAY + "保護ユニット上限値：" + ChatColor.RED + config.getGridLimitPerWorld(player.getWorld().getName()));
        ItemStack menuicon4 = MenuIcon.getMenuIcon(Material.BLUE_STAINED_GLASS_PANE, 1, 11, ChatColor.DARK_GREEN + "設定",
                CollectionConverters.ListHasAsScala(lore4).asScala().toList(), true);
        gridInv.setItem(4, menuicon4);

        //5マス目
        List<String> lore5 = getGridLore(directionMap.get(RelativeDirection.right()), unitMap.getOrElse(RelativeDirection.right(), () -> null));
        if (!playerData.canGridExtend(RelativeDirection.right(), player.getWorld().getName())) {
            lore5.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "これ以上拡張できません");
        } else if (!playerData.canGridReduce(RelativeDirection.right())) {
            lore5.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "これ以上縮小できません");
        }
        ItemStack menuicon5 = MenuIcon.getMenuIcon(Material.LIME_STAINED_GLASS_PANE, 1, 5,
                ChatColor.DARK_GREEN + "右に" + playerData.unitPerClick() + "ユニット増やす/減らす", CollectionConverters.ListHasAsScala(lore5).asScala().toList(), true);
        gridInv.setItem(5, menuicon5);

        //6マス目
        List<String> lore6 = new ArrayList<>();
        lore6.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "取扱注意！！");
        ItemStack menuicon6 = MenuIcon.getMenuIcon(Material.YELLOW_STAINED_GLASS_PANE, 1, 4, ChatColor.RED + "全設定リセット",
                CollectionConverters.ListHasAsScala(lore6).asScala().toList(), true);
        gridInv.setItem(6, menuicon6);

        //7マス目
        List<String> lore7 = getGridLore(directionMap.get(RelativeDirection.behind()), unitMap.getOrElse(RelativeDirection.behind(), () -> null));
        if (!playerData.canGridExtend(RelativeDirection.behind(), player.getWorld().getName())) {
            lore7.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "これ以上拡張できません");
        } else if (!playerData.canGridReduce(RelativeDirection.behind())) {
            lore7.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "これ以上縮小できません");
        }
        ItemStack menuicon7 = MenuIcon.getMenuIcon(Material.GREEN_STAINED_GLASS_PANE, 1, 13,
                ChatColor.DARK_GREEN + "後ろに" + playerData.unitPerClick() + "ユニット増やす/減らす", CollectionConverters.ListHasAsScala(lore7).asScala().toList(), true);
        gridInv.setItem(7, menuicon7);

        //8マス目
        if (!config.isGridProtectionEnabled(player.getWorld())) {
            List<String> lore8 = new ArrayList<>();
            lore8.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "このワールドでは保護を作成できません");
            ItemStack menuicon8 = MenuIcon.getMenuIcon(Material.RED_WOOL, 1, 14, ChatColor.RED + "保護作成",
                    CollectionConverters.ListHasAsScala(lore8).asScala().toList(), true);
            gridInv.setItem(8, menuicon8);
        } else if (!playerData.canCreateRegion()) {
            List<String> lore8 = new ArrayList<>();
            lore8.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "以下の原因により保護を作成できません");
            lore8.add(ChatColor.RED + "・保護の範囲が他の保護と重複している");
            lore8.add(ChatColor.RED + "・保護の作成上限に達している");
            ItemStack menuicon8 = MenuIcon.getMenuIcon(Material.RED_WOOL, 1, 14, ChatColor.RED + "保護作成",
                    CollectionConverters.ListHasAsScala(lore8).asScala().toList(), true);
            gridInv.setItem(8, menuicon8);
        } else {
            List<String> lore8 = new ArrayList<>();
            lore8.add(ChatColor.DARK_GREEN + "保護作成可能です");
            lore8.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "クリックで作成");
            ItemStack menuicon8 = MenuIcon.getMenuIcon(Material.LIGHT_BLUE_WOOL, 1, 11, ChatColor.GREEN + "保護作成",
                    CollectionConverters.ListHasAsScala(lore8).asScala().toList(), true);
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

    private static Map<RelativeDirection, String> getPlayerDirectionString(Player player) {
        double rotation = (player.getLocation().getYaw() + 180) % 360;
        HashMap<RelativeDirection, String> directionMap = new HashMap<>();

        if (rotation < 0) {
            rotation += 360;
        }

        //0,360:south 90:west 180:north 270:east
        if (0.0 <= rotation && rotation < 45.0) {
            //前が北(North)
            directionMap.put(RelativeDirection.behind(), "南(South)");
            directionMap.put(RelativeDirection.ahead(), "北(North)");
            directionMap.put(RelativeDirection.left(), "西(West)");
            directionMap.put(RelativeDirection.right(), "東(East)");
        } else if (45.0 <= rotation && rotation < 135.0) {
            //前が東(East)
            directionMap.put(RelativeDirection.right(), "南(South)");
            directionMap.put(RelativeDirection.left(), "北(North)");
            directionMap.put(RelativeDirection.behind(), "西(West)");
            directionMap.put(RelativeDirection.ahead(), "東(East)");
        } else if (135.0 <= rotation && rotation < 225.0) {
            //前が南(South)
            directionMap.put(RelativeDirection.ahead(), "南(South)");
            directionMap.put(RelativeDirection.behind(), "北(North)");
            directionMap.put(RelativeDirection.right(), "西(West)");
            directionMap.put(RelativeDirection.left(), "東(East)");
        } else if (225.0 <= rotation && rotation < 315.0) {
            //前が西(West)
            directionMap.put(RelativeDirection.left(), "南(South)");
            directionMap.put(RelativeDirection.right(), "北(North)");
            directionMap.put(RelativeDirection.ahead(), "西(West)");
            directionMap.put(RelativeDirection.behind(), "東(East)");
        } else if (315.0 <= rotation && rotation < 360.0) {
            //前が北(North)
            directionMap.put(RelativeDirection.behind(), "南(South)");
            directionMap.put(RelativeDirection.ahead(), "北(North)");
            directionMap.put(RelativeDirection.left(), "西(West)");
            directionMap.put(RelativeDirection.right(), "東(East)");
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
        ItemStack retIcon = MenuIcon.getMenuIcon(Material.BARRIER, 1, ChatColor.RED + "グリッド式保護メニューに戻る",
                CollectionConverters.ListHasAsScala(lore).asScala().toList(), true);
        inv.setItem(getAisleAmount() * 9, retIcon);

        return inv;
    }

    /**
     * テンプレートメニュー用。
     *
     * @return グリッド式保護テンプレート保存メニューの縦の数
     */
    private static int getAisleAmount() {
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
            return MenuIcon.getMenuIcon(Material.CHEST, 1,
                    ChatColor.GREEN + "テンプレNo." + (i + 1) + "(設定済)", CollectionConverters.ListHasAsScala(lore).asScala().toList(), true);
        } else {
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GREEN + "未設定");
            lore.add(ChatColor.RED + "左クリックで現在の設定を保存");
            return MenuIcon.getMenuIcon(Material.PAPER, 1,
                    ChatColor.RED + "テンプレNo." + (i + 1), CollectionConverters.ListHasAsScala(lore).asScala().toList(), true);
        }
    }
}
