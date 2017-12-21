package com.github.unchama.seichiassist.data;

import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.util.Util;
import com.github.unchama.seichiassist.util.Util.DirectionType;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.*;
import java.util.*;

/**
 * 保護関連メニュー
 *
 * @author karayuu
 */
public class RegionMenuData {
    static WorldGuardPlugin Wg = Util.getWorldGuard();
    static WorldEditPlugin We = Util.getWorldEdit();
    static Config config = SeichiAssist.config;
    static NumberFormat nfNum = NumberFormat.getNumberInstance();

    /**
     * 保護メニューを取得します。
     * @param player プレイヤー
     * @return 保護メニューインベントリ
     */
    public static Inventory getRegionMenuData(Player player) {
        Inventory inventory = Bukkit.createInventory(null, InventoryType.HOPPER, ChatColor.BLACK + "保護メニュー");

        //0マス目
        List<String> lore0 = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで召喚"
                , ChatColor.RESET + "" + ChatColor.DARK_GRAY + "※インベントリを空けておこう"
                , ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "保護のかけ方"
                , ChatColor.RESET + "" +  ChatColor.GREEN + "①召喚された斧を手に持ちます"
                , ChatColor.RESET + "" +  ChatColor.GREEN + "②保護したい領域の一方の角を" + ChatColor.YELLOW + "左" + ChatColor.GREEN + "クリック"
                , ChatColor.RESET + "" +  ChatColor.GREEN + "③もう一方の対角線上の角を" + ChatColor.RED + "右" + ChatColor.GREEN + "クリック"
                , ChatColor.RESET + "" +  ChatColor.GREEN + "④メニューの" + ChatColor.RESET + "" +  ChatColor.YELLOW + "金の斧" + ChatColor.RESET + "" +  ChatColor.GREEN + "をクリック"
                , ChatColor.RESET + "" + ChatColor.DARK_GRAY + "command->[//wand]");
        ItemStack menuicon0 = Util.getMenuIcon(Material.WOOD_AXE, 1
                , ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "保護設定用の木の斧を召喚"
                , lore0, true);
        inventory.setItem(0, menuicon0);

        //1マス目
        ItemStack itemstack1 = new ItemStack(Material.GOLD_AXE,1);
        ItemMeta itemmeta1 = Bukkit.getItemFactory().getItemMeta(Material.GOLD_AXE);
        itemmeta1.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "保護の申請");
        itemmeta1.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        List<String> lore1 = new ArrayList<>();

        Selection selection = Util.getWorldEdit().getSelection(player);

        if(!player.hasPermission("worldguard.region.claim")){
            lore1.addAll(Arrays.asList(ChatColor.RED + "このワールドでは"
                    , ChatColor.RED + "保護を申請出来ません"
            ));
        }else if (selection == null) {
            lore1.addAll(Arrays.asList(ChatColor.RED + "範囲指定されてません"
                    , ChatColor.RED + "先に木の斧で2か所クリックしてネ"
            ));
        }else if(selection.getLength() < 10||selection.getWidth() < 10){
            lore1.addAll(Arrays.asList(ChatColor.RED + "選択された範囲が狭すぎます"
                    , ChatColor.RED + "1辺当たり最低10ブロック以上にしてネ"
            ));
        }else{
            itemmeta1.addEnchant(Enchantment.DIG_SPEED, 100, false);
            lore1.addAll(Arrays.asList(ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "範囲指定されています"
                    , ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックすると保護を申請します"
            ));
        }

        if(player.hasPermission("worldguard.region.claim")){
            PlayerData playerdata = SeichiAssist.playermap.get(player.getUniqueId());
            lore1.addAll(Arrays.asList(ChatColor.DARK_GRAY + "Y座標は自動で全範囲保護されます"
                    , ChatColor.RESET + "" + ChatColor.YELLOW + "" + "A new region has been claimed"
                    , ChatColor.RESET + "" + ChatColor.YELLOW + "" + "named '" + player.getName() + "_" + playerdata.rgnum + "'."
                    , ChatColor.RESET + "" + ChatColor.GRAY + "と出れば保護設定完了です"
                    , ChatColor.RESET + "" + ChatColor.RED + "赤色で別の英文が出た場合"
                    , ChatColor.RESET + "" + ChatColor.GRAY + "保護の設定に失敗しています"
                    , ChatColor.RESET + "" + ChatColor.GRAY + "・別の保護と被っていないか"
                    , ChatColor.RESET + "" + ChatColor.GRAY + "・保護数上限に達していないか"
                    , ChatColor.RESET + "" + ChatColor.GRAY + "確認してください"
            ));
        }
        itemmeta1.setLore(lore1);
        itemstack1.setItemMeta(itemmeta1);
        inventory.setItem(1,itemstack1);

        //2マス目
        List<String> lore2 =  Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで表示"
                , ChatColor.RESET + "" + ChatColor.GRAY + "今いるワールドで"
                , ChatColor.RESET + "" + ChatColor.GRAY + "あなたが保護している"
                , ChatColor.RESET + "" + ChatColor.GRAY + "土地の一覧を表示します"
                , ChatColor.RESET + "" + ChatColor.RED + "" + ChatColor.UNDERLINE + "/rg info 保護名"
                , ChatColor.RESET + "" + ChatColor.GRAY + "該当保護の詳細情報を表示"
                , ChatColor.RESET + "" + ChatColor.RED + "" + ChatColor.UNDERLINE + "/rg rem 保護名"
                , ChatColor.RESET + "" + ChatColor.GRAY + "該当保護を削除する"
                , ChatColor.RESET + "" + ChatColor.RED + "" + ChatColor.UNDERLINE + "/rg addmem 保護名 プレイヤー名"
                , ChatColor.RESET + "" + ChatColor.GRAY + "該当保護に指定メンバーを追加"
                , ChatColor.RESET + "" + ChatColor.RED + "" + ChatColor.UNDERLINE + "/rg removemember 保護名 プレイヤー名"
                , ChatColor.RESET + "" + ChatColor.GRAY + "該当保護の指定メンバーを削除"
                , ChatColor.RESET + "" + ChatColor.DARK_GRAY + "その他のコマンドはWikiを参照"
                , ChatColor.RESET + "" + ChatColor.DARK_GRAY + "command->[/rg list]"
        );
        ItemStack menuicon2 = Util.getMenuIcon(Material.STONE_AXE, 1,
                ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "保護一覧を表示", lore2, true);
        inventory.setItem(2, menuicon2);

        //3マス目
        List<String> lore3 = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く"
                , ChatColor.RESET + "" + ChatColor.RED + "保護の作成と管理が超簡単に！"
                , ChatColor.RESET + "" + ChatColor.RED + "クリックした場所によって挙動が変わります"
                , ChatColor.RESET + "" + ChatColor.YELLOW + "自分の所有する保護内なら…"
                , ChatColor.RESET + "" + ChatColor.GRAY + "保護の各種設定や削除が行えます"
                , ChatColor.RESET + "" + ChatColor.YELLOW + "それ以外なら…"
                , ChatColor.RESET + "" + ChatColor.GRAY + "新規保護の作成画面が表示されます"
                , ChatColor.RESET + "" + ChatColor.DARK_GRAY + "command->[/land]"
                );
        ItemStack menuicon3 = Util.getMenuIcon(Material.DIAMOND_AXE, 1,
                ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "RegionGUI機能", lore3, true);
        inventory.setItem(3, menuicon3);

        //4マス目
        List<String> lore4 = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く"
                , ChatColor.RESET + "" + ChatColor.RED + "グリッド式保護の作成ができます"
                , ChatColor.RESET + "" + ChatColor.YELLOW + "グリッド式保護とは…"
                , ChatColor.RESET + "" + ChatColor.GRAY + "保護をユニット単位で管理するシステムのこと"
                , ChatColor.RESET + "" + ChatColor.RED + "運営チームが個別に指定したワールドに関しては"
                , ChatColor.RESET + "" + ChatColor.RED + "このシステムのみでしか保護が作成できません"
                , ChatColor.RESET + "" + ChatColor.AQUA + "15ブロック＝1ユニットとして"
                , ChatColor.RESET + "" + ChatColor.AQUA + "保護が作成されます。"
        );
        ItemStack menuicon4 = Util.getMenuIcon(Material.IRON_AXE, 1,
                ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "グリッド式保護作成画面",
                lore4, true);
        inventory.setItem(4, menuicon4);


        return inventory;
    }

    /**
     * グリッド式保護メニュを開きます。
     * @param player
     * @return
     */
    public static Inventory getGridWorldGuardMenu(Player player) {
        PlayerData playerData = SeichiAssist.playermap.get(player.getUniqueId());
        Map<DirectionType, Integer> unitMap = playerData.getUnitMap();
        Map<DirectionType, String> directionMap = getPlayerDirectionString(player);

        Inventory gridInv = Bukkit.createInventory(null, InventoryType.DISPENSER,
                ChatColor.LIGHT_PURPLE + "グリッド式保護設定メニュー");

        //0マス目
        List<String> lore0 = new ArrayList<>();
        lore0.add(ChatColor.GREEN + "現在のユニット指定量");
        lore0.add(ChatColor.AQUA + "" + playerData.getUnitPerClick() + ChatColor.GREEN + "ユニット(" +
                ChatColor.AQUA + playerData.getUnitPerClick() * 15 + ChatColor.GREEN + "ブロック)/1クリック");
        lore0.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "クリックで変更");
        ItemStack menuicon0 = Util.getMenuIcon(Material.STAINED_GLASS_PANE, 1, 0,
                ChatColor.GREEN + "拡張単位の変更", lore0, true);
        gridInv.setItem(0, menuicon0);

        //1マス目
        List<String> lore1 = getGridLore(directionMap.get(DirectionType.AHEAD), unitMap.get(DirectionType.AHEAD));
        if (!playerData.canGridExtend(DirectionType.AHEAD)) {
            lore1.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "これ以上拡張できません");
        } else if (!playerData.canGridReduce(DirectionType.AHEAD)) {
            lore1.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "これ以上縮小できません");
        }
        ItemStack menuicon1 = Util.getMenuIcon(Material.STAINED_GLASS_PANE, 1, 14,
                ChatColor.DARK_GREEN + "前に" + playerData.getUnitPerClick() + "ユニット増やす/減らす", lore1, true);
        gridInv.setItem(1, menuicon1);

        //2マス目
        List<String> lore2 = new ArrayList<>();
        lore2.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "クリックで開く");
        ItemStack menuicon2 = Util.getMenuIcon(Material.CHEST, 1, ChatColor.GREEN + "設定保存メニュー",
                lore2, true);
        gridInv.setItem(2, menuicon2);

        //3マス目
        List<String> lore3 = getGridLore(directionMap.get(DirectionType.LEFT), unitMap.get(DirectionType.LEFT));
        if (!playerData.canGridExtend(DirectionType.LEFT)) {
            lore3.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "これ以上拡張できません");
        } else if (!playerData.canGridReduce(DirectionType.LEFT)) {
            lore3.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "これ以上縮小できません");
        }
        ItemStack menuicon3 = Util.getMenuIcon(Material.STAINED_GLASS_PANE, 1, 10,
                ChatColor.DARK_GREEN + "左に" + playerData.getUnitPerClick() + "ユニット増やす/減らす", lore3, true);
        gridInv.setItem(3, menuicon3);

        //4マス目
        List<String> lore4 = new ArrayList<>();
        lore4.add(ChatColor.GRAY + "現在の設定");
        lore4.add(ChatColor.GRAY + "前方向：" + ChatColor.AQUA + unitMap.get(DirectionType.AHEAD) + ChatColor.GRAY + "ユニット"
                + "(" + ChatColor.AQUA + nfNum.format(unitMap.get(DirectionType.AHEAD) * 15) + ChatColor.GRAY + "ブロック)");
        lore4.add(ChatColor.GRAY + "後ろ方向：" + ChatColor.AQUA + unitMap.get(DirectionType.BEHIND) + ChatColor.GRAY + "ユニット"
                + "(" + ChatColor.AQUA + nfNum.format(unitMap.get(DirectionType.BEHIND) * 15) + ChatColor.GRAY + "ブロック)");
        lore4.add(ChatColor.GRAY + "右方向：" + ChatColor.AQUA + unitMap.get(DirectionType.RIGHT) + ChatColor.GRAY + "ユニット"
                + "(" + ChatColor.AQUA + nfNum.format(unitMap.get(DirectionType.RIGHT) * 15) + ChatColor.GRAY + "ブロック)");
        lore4.add(ChatColor.GRAY + "左方向：" + ChatColor.AQUA + unitMap.get(DirectionType.LEFT) + ChatColor.GRAY + "ユニット"
                + "(" + ChatColor.AQUA + nfNum.format(unitMap.get(DirectionType.LEFT) * 15) + ChatColor.GRAY + "ブロック)");
        lore4.add(ChatColor.GRAY + "保護ユニット数：" + ChatColor.AQUA + playerData.getGridChunkAmount());
        lore4.add(ChatColor.GRAY + "保護ユニット上限値：" + ChatColor.RED + config.getGridLimit());
        ItemStack menuicon4 = Util.getMenuIcon(Material.STAINED_GLASS_PANE, 1, 11, ChatColor.DARK_GREEN + "設定",
                lore4, true);
        gridInv.setItem(4, menuicon4);

        //5マス目
        List<String> lore5 = getGridLore(directionMap.get(DirectionType.RIGHT), unitMap.get(DirectionType.RIGHT));
        if (!playerData.canGridExtend(DirectionType.RIGHT)) {
            lore5.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "これ以上拡張できません");
        } else if (!playerData.canGridReduce(DirectionType.RIGHT)) {
            lore5.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "これ以上縮小できません");
        }
        ItemStack menuicon5 = Util.getMenuIcon(Material.STAINED_GLASS_PANE, 1, 5,
                ChatColor.DARK_GREEN + "右に" + playerData.getUnitPerClick() + "ユニット増やす/減らす", lore5, true);
        gridInv.setItem(5, menuicon5);

        //6マス目
        List<String> lore6 = new ArrayList<>();
        lore6.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "取扱注意！！");
        ItemStack menuicon6 = Util.getMenuIcon(Material.STAINED_GLASS_PANE, 1, 4, ChatColor.RED + "全設定リセット",
                lore6, true);
        gridInv.setItem(6, menuicon6);

        //7マス目
        List<String> lore7 = getGridLore(directionMap.get(DirectionType.BEHIND), unitMap.get(DirectionType.BEHIND));
        if (!playerData.canGridExtend(DirectionType.BEHIND)) {
            lore7.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "これ以上拡張できません");
        } else if (!playerData.canGridReduce(DirectionType.BEHIND)) {
            lore7.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "これ以上縮小できません");
        }
        ItemStack menuicon7 = Util.getMenuIcon(Material.STAINED_GLASS_PANE, 1, 13,
                ChatColor.DARK_GREEN + "後ろに" + playerData.getUnitPerClick() + "ユニット増やす/減らす", lore7, true);
        gridInv.setItem(7, menuicon7);

        //8マス目
        if (!config.isGridProtectForce(player)) {
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
        lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "左クリックで増加");
        lore.add(ChatColor.RESET + "" +  ChatColor.RED + "右クリックで減少");
        lore.add(ChatColor.RESET + "" + ChatColor.GRAY + ChatColor.GRAY + "---------------");
        lore.add(ChatColor.GRAY + "方向：" + ChatColor.AQUA + direction);
        lore.add(ChatColor.GRAY + "現在の指定方向ユニット数：" + ChatColor.AQUA + unit
                + ChatColor.GRAY + "(" + ChatColor.AQUA + nfNum.format(unit * 15) + ChatColor.GRAY + "ブロック)");

        return lore;
    }

    private static Map<DirectionType, String> getPlayerDirectionString(Player player) {
        double rotation = (player.getLocation().getYaw() + 180) % 360;
        Map<DirectionType, String> directionMap = new HashMap<>();

        if (rotation < 0) {
            rotation += 360;
        }

        //0,360:south 90:west 180:north 270:east
        if (0.0 <= rotation && rotation < 45.0) {
            //前が北(North)
            directionMap.put(DirectionType.BEHIND, "南(South)");
            directionMap.put(DirectionType.AHEAD, "北(North)");
            directionMap.put(DirectionType.LEFT, "西(West)");
            directionMap.put(DirectionType.RIGHT, "東(East)");
        } else if (45.0 <= rotation && rotation < 135.0) {
            //前が東(East)
            directionMap.put(DirectionType.RIGHT, "南(South)");
            directionMap.put(DirectionType.LEFT, "北(North)");
            directionMap.put(DirectionType.BEHIND, "西(West)");
            directionMap.put(DirectionType.AHEAD, "東(East)");
        } else if (135.0 <= rotation && rotation < 225.0) {
            //前が南(South)
            directionMap.put(DirectionType.AHEAD, "南(South)");
            directionMap.put(DirectionType.BEHIND, "北(North)");
            directionMap.put(DirectionType.RIGHT, "西(West)");
            directionMap.put(DirectionType.LEFT, "東(East)");
        } else if (225.0 <= rotation && rotation < 315.0) {
            //前が西(West)
            directionMap.put(DirectionType.LEFT, "南(South)");
            directionMap.put(DirectionType.RIGHT, "北(North)");
            directionMap.put(DirectionType.AHEAD, "西(West)");
            directionMap.put(DirectionType.BEHIND, "東(East)");
        } else if (315.0 <= rotation && rotation < 360.0) {
            //前が北(North)
            directionMap.put(DirectionType.BEHIND, "南(South)");
            directionMap.put(DirectionType.AHEAD, "北(North)");
            directionMap.put(DirectionType.LEFT, "西(West)");
            directionMap.put(DirectionType.RIGHT, "東(East)");
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

        for (int i = 0; i <= config.getTemplateKeepAmount() - 1 ; i++) {
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
     * @param i アイコン番号
     * @param player プレイヤー
     *
     * @return メニューアイコン
     */
    private static ItemStack getGridtempMenuicon(int i, Player player) {
        PlayerData playerData = SeichiAssist.playermap.get(player.getUniqueId());
        Map<Integer, GridTemplate> templateMap = playerData.getTemplateMap();

        if (templateMap.get(i).isEmpty()) {
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GREEN + "未設定");
            lore.add(ChatColor.RED + "左クリックで現在の設定を保存");
            ItemStack menuIcon = Util.getMenuIcon(Material.PAPER, 1,
                    ChatColor.RED + "テンプレNo." + (i + 1) , lore, true);
            return menuIcon;
        } else {
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GREEN + "設定内容");
            lore.add(ChatColor.GRAY + "前方向：" + ChatColor.AQUA + templateMap.get(i).getAheadAmount() + ChatColor.GRAY + "ユニット");
            lore.add(ChatColor.GRAY + "後ろ方向：" + ChatColor.AQUA + templateMap.get(i).getBehindAmount() + ChatColor.GRAY + "ユニット");
            lore.add(ChatColor.GRAY + "右方向：" + ChatColor.AQUA + templateMap.get(i).getRightAmount() + ChatColor.GRAY + "ユニット");
            lore.add(ChatColor.GRAY + "左方向：" + ChatColor.AQUA + templateMap.get(i).getLeftAmount() + ChatColor.GRAY + "ユニット");
            lore.add(ChatColor.GREEN + "左クリックで設定を読み込み");
            lore.add(ChatColor.RED + "右クリックで現在の設定で上書き");
            ItemStack menuicon = Util.getMenuIcon(Material.CHEST, 1,
                    ChatColor.GREEN + "テンプレNo." + (i + 1) + "(設定済)", lore, true);
            return menuicon;
        }
    }
}
