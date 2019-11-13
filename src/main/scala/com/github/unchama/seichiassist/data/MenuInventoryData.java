package com.github.unchama.seichiassist.data;

import com.github.unchama.itemstackbuilder.IconItemStackBuilder;
import com.github.unchama.seichiassist.*;
import com.github.unchama.seichiassist.data.player.PlayerData;
import com.github.unchama.seichiassist.database.DatabaseGateway;
import com.github.unchama.seichiassist.task.VotingFairyTask;
import com.github.unchama.seichiassist.util.AsyncInventorySetter;
import com.github.unchama.seichiassist.util.ItemMetaFactory;
import com.github.unchama.seichiassist.util.TypeConverter;
import com.github.unchama.seichiassist.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import scala.collection.mutable.HashMap;

import java.util.*;

public class MenuInventoryData {
    private static HashMap<UUID, PlayerData> playermap = SeichiAssist.playermap();
    private static DatabaseGateway databaseGateway = SeichiAssist.databaseGateway();

    //二つ名組合せシステム用
    private static boolean nextpageflag1 = false;
    private static boolean nextpageflag2 = false;
    private static boolean nextpageflag3 = false;
    private static boolean nextpageflagS = false;
    private static int checkTitle1 = 0;
    private static int checkTitle2 = 0;
    private static int checkTitle3 = 0;
    private static int checkTitleS = 0;
    private static int NoKeep = 0;

    //パッシブスキルメニュー
    public static Inventory getPassiveSkillMenuData(Player p) {

        Inventory inventory = Bukkit.getServer().createInventory(null, 4 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "整地スキル切り替え");
        ItemStack itemstack;
        ItemMeta itemmeta;
        SkullMeta skullmeta;
        List<String> lore;

        //プレイヤーを取得
        Player player = p.getPlayer();

        //UUID取得
        UUID uuid = player.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);

        // 1ページ目を開く
        itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        skullmeta = ItemMetaFactory.SKULL.getValue();
        itemstack.setDurability((short) 3);
        skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
        skullmeta.setLore(lore);
        skullmeta.setOwner("MHF_ArrowLeft");
        itemstack.setItemMeta(skullmeta);
        AsyncInventorySetter.setItemAsync(inventory, 27, itemstack.clone());

        //複数種類同時破壊スキルのトグルボタン
        itemstack = new ItemStack(Material.DIAMOND_PICKAXE, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_PICKAXE);
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "複数種類同時破壊スキル切替");
        //itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.setLore(lore);
        itemstack.setItemMeta(MultipleIDBlockBreakToggleMeta(playerdata, itemmeta));
        inventory.setItem(0, itemstack);

        //Chest破壊
        if (playerdata.chestflag()) {
            itemstack = new ItemStack(Material.DIAMOND_AXE, 1);
            itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_AXE);
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        } else {
            itemstack = new ItemStack(Material.CHEST, 1);
            itemmeta = Bukkit.getItemFactory().getItemMeta(Material.CHEST);
        }
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "チェスト破壊スキル切替");
        itemmeta.setLore(lore);
        itemstack.setItemMeta(ChestBreakToggleMeta(playerdata, itemmeta));
        inventory.setItem(1, itemstack);

        //GiganticBerserk
        //10レベ未満のプレイヤーはスキル未解放
        if (playerdata.level() < 10) {
            itemstack = new ItemStack(Material.STICK, 1);
            itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STICK);
        } else {
            final Material m;
            switch (playerdata.giganticBerserk().stage()) {
                case 0:
                    m = Material.WOOD_SWORD;
                    break;
                case 1:
                    m = Material.STONE_SWORD;
                    break;
                case 2:
                    m = Material.GOLD_SWORD;
                    break;
                case 3:
                    m = Material.IRON_SWORD;
                    break;
                case 4:
                    m = Material.DIAMOND_SWORD;
                    break;
                default:
                    m = Material.STICK;
            }
            itemstack = new ItemStack(m, 1);
            itemmeta = Bukkit.getItemFactory().getItemMeta(m);
        }
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "Gigantic" + ChatColor.RED + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "Berserk");

        if (playerdata.giganticBerserk().canEvolve() || playerdata.giganticBerserk().reachedLimit()) {
            itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }
        itemmeta.setLore(lore);
        itemstack.setItemMeta(GiganticBerserkMeta(playerdata, itemmeta));
        inventory.setItem(9, itemstack);

        return inventory;
    }

    //投票特典受け取りボタン
    public static List<String> VoteGetButtonLore(PlayerData playerdata) {
        List<String> lore = new ArrayList<>();
        lore.addAll(Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "投票特典を受け取るには"
                , ChatColor.RESET + "" + ChatColor.GRAY + "投票ページで投票した後"
                , ChatColor.RESET + "" + ChatColor.GRAY + "このボタンをクリックします"));
        lore.add(ChatColor.RESET + "" + ChatColor.AQUA + "特典受取済投票回数：" + playerdata.p_givenvote());
        lore.add(ChatColor.RESET + "" + ChatColor.AQUA + "所有投票pt：" + playerdata.activeskilldata().effectpoint);
        return lore;
    }

    // 複数種類ブロック同時破壊トグルボタン(追加)
    public static ItemMeta MultipleIDBlockBreakToggleMeta(PlayerData playerdata, ItemMeta itemmeta) {
        List<String> lore = new ArrayList<>();
        if (playerdata.settings().multipleidbreakflag()) {
            itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
            lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "複数種類ブロック同時破壊");
            lore.add(ChatColor.RESET + "" + ChatColor.GRAY + "ブロックに対応するツールを無視してスキルで");
            lore.add(ChatColor.RESET + "" + ChatColor.GRAY + "破壊可能な全種類のブロックを同時に破壊します");
            lore.add(ChatColor.RESET + "" + ChatColor.DARK_RED + "整地ワールドではON/OFFに関わらず同時破壊されます");
            lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "ON");
            lore.add(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでOFF");
            if (SeichiAssist.DEBUG()) {
                lore.add(ChatColor.RESET + "" + ChatColor.RED + "ON");
            }
        } else {
            itemmeta.removeEnchant(Enchantment.DIG_SPEED);
            lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "複数種類ブロック同時破壊");
            lore.add(ChatColor.RESET + "" + ChatColor.GRAY + "ブロックに対応するツールを無視してスキルで");
            lore.add(ChatColor.RESET + "" + ChatColor.GRAY + "破壊可能な全種類のブロックを同時に破壊します");
            lore.add(ChatColor.RESET + "" + ChatColor.DARK_RED + "整地ワールドではON/OFFに関わらず同時破壊されます");
            lore.add(ChatColor.RESET + "" + ChatColor.RED + "OFF");
            lore.add(ChatColor.RESET + "" + ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックでON");
            if (SeichiAssist.DEBUG()) {
                lore.add(ChatColor.RESET + "" + ChatColor.RED + "OFF");
            }
        }
        itemmeta.setLore(lore);
        return itemmeta;
    }

    // GiganticBerserk Meta
    public static ItemMeta GiganticBerserkMeta(PlayerData playerdata, ItemMeta itemmeta) {
        List<String> lore = new ArrayList<>();

        int n = (playerdata.giganticBerserk().stage() * 10) + playerdata.giganticBerserk().level();

        if (playerdata.level() < 10) {
            lore.add(ChatColor.WHITE + "このパッシブスキルは");
            lore.add(ChatColor.WHITE + "整地レベルが10以上になると解放されます");
        } else {
            lore.add(ChatColor.RED + "敵MOBを倒した時");
            lore.add(ChatColor.RED + "その魂を吸収しマナへと変換するスキル");
            lore.add(ChatColor.DARK_GRAY + "※成功率は高くなく");
            lore.add(ChatColor.DARK_GRAY + "整地中でなければその効果を発揮しない");
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "実装は試験的であり、変更される場合があります");
            if (playerdata.giganticBerserk().reachedLimit()) {
                lore.add(ChatColor.GRAY + "MOBの魂を極限まで吸収し最大限の力を発揮する");
            } else {
                lore.add(ChatColor.GRAY + "MOBの魂を" + LevelThresholds.giganticBerserkLevelList().apply(n) + "回吸収すると更なる力が得られる");
                //exp
                lore.add(ChatColor.GRAY + "" + playerdata.giganticBerserk().exp() + "/" + LevelThresholds.giganticBerserkLevelList().apply(n));
            }
            //level
            lore.add(ChatColor.GRAY + "現在" + (playerdata.giganticBerserk().level() + 1) + "レベル,回復率 " + (100 * playerdata.giganticBerserk().manaRegenerationProbability()) + "%");

            if (playerdata.giganticBerserk().canEvolve()) {
                lore.add("");
                lore.add(ChatColor.DARK_RED + "沢山の魂を吸収したことで");
                lore.add(ChatColor.DARK_RED + "スキルの秘めたる力を解放できそうだ…！");
                lore.add(ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで解放する");
            }

        }

        itemmeta.setLore(lore);
        return itemmeta;
    }

    public static ItemMeta ChestBreakToggleMeta(PlayerData playerdata, ItemMeta itemmeta) {
        List<String> lore = new ArrayList<>();

        lore.add(ChatColor.GREEN + "スキルでチェストを破壊するスキル");

        if (playerdata.chestflag()) {
            lore.add(ChatColor.RED + "整地ワールドでのみ発動中(デフォルト)");
            lore.add("");
            lore.add(ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックで切り替え");
        } else {
            lore.add(ChatColor.RED + "発動しません");
            lore.add("");
            lore.add(ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックで切り替え");
        }
        itemmeta.setLore(lore);
        return itemmeta;
    }

    //ランキングリスト
    public static Inventory getRankingList(int page) {
//		int maxpage=2;
        int maxpage = 14;
        final int MIN_LEVEL = 100;
        Inventory inventory = Bukkit.getServer().createInventory(null, 6 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "整地神ランキング");
        ItemStack itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        SkullMeta skullmeta = ItemMetaFactory.SKULL.getValue();
        List<String> lore = new ArrayList<>();
        itemstack.setDurability((short) 3);
        RankData rankdata;
//		for(int count = 50*page,count2=0;count < 50+50*page;count++,count2++){
        for (int count = 10 * page, count2 = 0; count < 10 + 10 * page; count++, count2++) {
            if (count >= SeichiAssist.ranklist().size()) {
                break;
            }
//			if(count2==45){count2+=2;}
            rankdata = SeichiAssist.ranklist().apply(count);
            if (rankdata.totalbreaknum < (Long) LevelThresholds.levelExpThresholds().apply(MIN_LEVEL - 1)) { //レベル100相当の総整地量判定に変更
                break;
            }

            skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "" + (count + 1) + "位:" + "" + ChatColor.WHITE + rankdata.name);
            lore.clear();
            lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "整地レベル:" + rankdata.level);
            lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "総整地量:" + rankdata.totalbreaknum);

            skullmeta.setLore(lore);
            skullmeta.setOwner(rankdata.name);
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, count2, itemstack.clone());
        }

        if (page != maxpage) {
            // 整地神ランキング次ページ目を開く
            skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地神ランキング" + (page + 2) + "ページ目へ");
            lore.clear();
            lore.add(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
            );
            skullmeta.setLore(lore);
            skullmeta.setOwner("MHF_ArrowDown");
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, 52, itemstack.clone());
        }

        // 1ページ目を開く
        if (page == 0) {
            skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ");
            lore.clear();
            lore.add(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
            skullmeta.setLore(lore);
            skullmeta.setOwner("MHF_ArrowLeft");
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, 45, itemstack.clone());
        } else {
            // 整地神ランキング前ページ目を開く;
            skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地神ランキング" + page + "ページ目へ");
            lore.clear();
            lore.add(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
            skullmeta.setLore(lore);
            skullmeta.setOwner("MHF_ArrowUp");
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, 45, itemstack.clone());
        }

        // 総整地量の表記
        skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地鯖統計データ");
        lore.clear();
        lore.addAll(Arrays.asList(ChatColor.RESET + "" + ChatColor.AQUA + "全プレイヤー総整地量:"
                , ChatColor.RESET + "" + ChatColor.AQUA + SeichiAssist.allplayerbreakblockint()
        ));
        skullmeta.setLore(lore);
        skullmeta.setOwner("unchama");
        itemstack.setItemMeta(skullmeta);
        AsyncInventorySetter.setItemAsync(inventory, 53, itemstack.clone());

        return inventory;
    }

    //ランキングリスト(ログイン時間)
    public static Inventory getRankingList_playtick(int page) {
//		int maxpage=2;
        int maxpage = 14;
        //final int MIN_LEVEL = 100;
        Inventory inventory = Bukkit.getServer().createInventory(null, 6 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "ログイン神ランキング");
        ItemStack itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        SkullMeta skullmeta = ItemMetaFactory.SKULL.getValue();
        List<String> lore = new ArrayList<>();
        itemstack.setDurability((short) 3);
        RankData rankdata;
        for (int count = 10 * page, count2 = 0; count < 10 + 10 * page; count++, count2++) {
            if (count >= SeichiAssist.ranklist_playtick().size()) {
                break;
            }
            rankdata = SeichiAssist.ranklist_playtick().apply(count);

            skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "" + (count + 1) + "位:" + "" + ChatColor.WHITE + rankdata.name);
            lore.clear();
            lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "総ログイン時間:" + TypeConverter.toTimeString(TypeConverter.toSecond(rankdata.playtick)));

            skullmeta.setLore(lore);
            skullmeta.setOwner(rankdata.name);
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, count2, itemstack.clone());
        }

        if (page != maxpage) {
            // 整地神ランキング次ページ目を開く
            skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ログイン神ランキング" + (page + 2) + "ページ目へ");
            lore.clear();
            lore.add(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
            );
            skullmeta.setLore(lore);
            skullmeta.setOwner("MHF_ArrowDown");
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, 52, itemstack.clone());
        }

        // 1ページ目を開く
        if (page == 0) {
            skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ");
            lore.clear();
            lore.add(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
            skullmeta.setLore(lore);
            skullmeta.setOwner("MHF_ArrowLeft");
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, 45, itemstack.clone());
        } else {
            // 整地神ランキング前ページ目を開く;
            skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ログイン神ランキング" + page + "ページ目へ");
            lore.clear();
            lore.add(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
            skullmeta.setLore(lore);
            skullmeta.setOwner("MHF_ArrowUp");
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, 45, itemstack.clone());
        }

        return inventory;
    }

    //ランキングリスト(投票回数)
    public static Inventory getRankingList_p_vote(int page) {
//		int maxpage=2;
        int maxpage = 14;
        final int MIN_LEVEL = 1;
        Inventory inventory = Bukkit.getServer().createInventory(null, 6 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "投票神ランキング");
        ItemStack itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        SkullMeta skullmeta = ItemMetaFactory.SKULL.getValue();
        List<String> lore = new ArrayList<>();
        itemstack.setDurability((short) 3);
        RankData rankdata;
//		for(int count = 50*page,count2=0;count < 50+50*page;count++,count2++){
        for (int count = 10 * page, count2 = 0; count < 10 + 10 * page; count++, count2++) {
            if (count >= SeichiAssist.ranklist_p_vote().size()) {
                break;
            }
//			if(count2==45){count2+=2;}
            rankdata = SeichiAssist.ranklist_p_vote().apply(count);
            if (rankdata.p_vote < MIN_LEVEL) { //投票数0
                break;
            }

            skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "" + (count + 1) + "位:" + "" + ChatColor.WHITE + rankdata.name);
            lore.clear();
            //lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "整地レベル:" + rankdata.level);
            //lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "総整地量:" + rankdata.totalbreaknum);
            //lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "総ログイン時間:" + Util.toTimeString(Util.toSecond(rankdata.playtick)));
            lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "総投票回数:" + rankdata.p_vote);

            skullmeta.setLore(lore);
            skullmeta.setOwner(rankdata.name);
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, count2, itemstack.clone());
        }

        if (page != maxpage) {
            // 投票神ランキング次ページ目を開く
            skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "投票神ランキング" + (page + 2) + "ページ目へ");
            lore.clear();
            lore.add(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
            );
            skullmeta.setLore(lore);
            skullmeta.setOwner("MHF_ArrowDown");
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, 52, itemstack.clone());
        }

        // 1ページ目を開く
        if (page == 0) {
            skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ");
            lore.clear();
            lore.add(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
            skullmeta.setLore(lore);
            skullmeta.setOwner("MHF_ArrowLeft");
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, 45, itemstack.clone());
        } else {
            // 整地神ランキング前ページ目を開く;
            skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "投票神ランキング" + page + "ページ目へ");
            lore.clear();
            lore.add(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
            skullmeta.setLore(lore);
            skullmeta.setOwner("MHF_ArrowUp");
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, 45, itemstack.clone());
        }

        return inventory;
    }

    //ランキングリスト(プレミアムエフェクトポイント)
    public static Inventory getRankingList_premiumeffectpoint(int page) {
        int maxpage = 2;
        final int MIN_LEVEL = 1;
        Inventory inventory = Bukkit.getServer().createInventory(null, 6 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "寄付神ランキング");
        ItemStack itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        SkullMeta skullmeta = ItemMetaFactory.SKULL.getValue();
        List<String> lore = new ArrayList<>();
        itemstack.setDurability((short) 3);
        RankData rankdata;
        for (int count = 50 * page, count2 = 0; count < 50 + 50 * page; count++, count2++) {
            if (count >= SeichiAssist.ranklist_premiumeffectpoint().size()) {
                break;
            }
            if (count2 == 45) {
                count2 += 2;
            }
            rankdata = SeichiAssist.ranklist_premiumeffectpoint().apply(count);
            if (rankdata.premiumeffectpoint < MIN_LEVEL) { //寄付金額0
                break;
            }

            skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "" + (count + 1) + "位:" + "" + ChatColor.WHITE + rankdata.name);
            lore.clear();
            //lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "整地レベル:" + rankdata.level);
            //lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "総整地量:" + rankdata.totalbreaknum);
            //lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "総ログイン時間:" + Util.toTimeString(Util.toSecond(rankdata.playtick)));
            lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "総寄付金額:" + rankdata.premiumeffectpoint * 100);

            skullmeta.setLore(lore);
            skullmeta.setOwner(rankdata.name);
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, count2, itemstack.clone());
        }

        if (page != maxpage) {
            // 整地神ランキング次ページ目を開く
            skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "寄付神ランキング" + (page + 2) + "ページ目へ");
            lore.clear();
            lore.add(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
            );
            skullmeta.setLore(lore);
            skullmeta.setOwner("MHF_ArrowDown");
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, 52, itemstack.clone());
        }

        // 1ページ目を開く
        if (page == 0) {
            skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ");
            lore.clear();
            lore.add(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
            skullmeta.setLore(lore);
            skullmeta.setOwner("MHF_ArrowLeft");
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, 45, itemstack.clone());
        } else {
            // 整地神ランキング前ページ目を開く;
            skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "寄付神ランキング" + page + "ページ目へ");
            lore.clear();
            lore.add(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
            skullmeta.setLore(lore);
            skullmeta.setOwner("MHF_ArrowUp");
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, 45, itemstack.clone());
        }

        return inventory;
    }

    //エフェクト選択メニュー
    public static Inventory getActiveSkillEffectMenuData(Player p) {
        //プレイヤーを取得
        Player player = p.getPlayer();
        //UUID取得
        UUID uuid = player.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
            Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            return null;
        }

        Inventory inventory = Bukkit.getServer().createInventory(null, 6 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "整地スキルエフェクト選択");
        ItemStack itemstack;
        ItemMeta itemmeta;
        SkullMeta skullmeta;
        List<String> lore;

        // 1ページ目を開く
        itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        skullmeta = ItemMetaFactory.SKULL.getValue();
        itemstack.setDurability((short) 3);
        skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "スキルメニューへ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
        skullmeta.setLore(lore);
        skullmeta.setOwner("MHF_ArrowLeft");
        itemstack.setItemMeta(skullmeta);
        AsyncInventorySetter.setItemAsync(inventory, 45, itemstack.clone());

        //1行目

        itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        skullmeta = ItemMetaFactory.SKULL.getValue();
        itemstack.setDurability((short) 3);
        skullmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
        skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + playerdata.lowercaseName() + "のスキルエフェクトデータ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + "現在選択しているエフェクト：" + ActiveSkillEffect.getNameByNum(playerdata.activeskilldata().effectnum)
                , ChatColor.RESET + "" + ChatColor.YELLOW + "使えるエフェクトポイント：" + playerdata.activeskilldata().effectpoint
                , ChatColor.RESET + "" + ChatColor.DARK_GRAY + "※投票すると獲得出来ます"
                , ChatColor.RESET + "" + ChatColor.LIGHT_PURPLE + "使えるプレミアムポイント：" + playerdata.activeskilldata().premiumeffectpoint
                , ChatColor.RESET + "" + ChatColor.DARK_GRAY + "※寄付をすると獲得できます"

        );
        skullmeta.setLore(lore);
        // この操作は安全; メニューを開けているのにUUIDがないなんてことがないから
        skullmeta.setOwningPlayer(Bukkit.getOfflinePlayer(playerdata.uuid())); //.setOwner(playerdata.name);
        itemstack.setItemMeta(skullmeta);
        AsyncInventorySetter.setItemAsync(inventory, 0, itemstack.clone());


        itemstack = new ItemStack(Material.BOOK_AND_QUILL, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BOOK_AND_QUILL);
        itemmeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "プレミアムエフェクト購入履歴");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで閲覧");
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(2, itemstack);

        itemstack = new ItemStack(Material.GLASS, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GLASS);
        itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "エフェクトを使用しない");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(1, itemstack);


        ActiveSkillEffect[] skilleffect = ActiveSkillEffect.arrayValues();

        for (int i = 0; i < skilleffect.length; i++) {
            //プレイヤーがそのスキルを取得している場合の処理
            if (playerdata.activeskilldata().obtainedSkillEffects.contains(skilleffect[i])) {
                itemstack = new ItemStack(skilleffect[i].material(), 1);
                itemmeta = Bukkit.getItemFactory().getItemMeta(skilleffect[i].material());
                itemmeta.setDisplayName(skilleffect[i].nameOnUI());
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + skilleffect[i].explanation()
                        , ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット"
                );
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
            }
            //プレイヤーがそのスキルをまだ取得していない場合の処理
            else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(skilleffect[i].nameOnUI());
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + skilleffect[i].explanation()
                        , ChatColor.RESET + "" + ChatColor.YELLOW + "必要エフェクトポイント：" + skilleffect[i].usePoint()
                        , ChatColor.RESET + "" + ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
            }
            inventory.setItem(i + 9, itemstack);
        }

        ActiveSkillPremiumEffect[] premiumeffect = ActiveSkillPremiumEffect.arrayValues();
        for (int i = 0; i < premiumeffect.length; i++) {
            //プレイヤーがそのスキルを取得している場合の処理
            if (playerdata.activeskilldata().obtainedSkillPremiumEffects.contains(premiumeffect[i])) {
                itemstack = new ItemStack(premiumeffect[i].material(), 1);
                itemmeta = Bukkit.getItemFactory().getItemMeta(premiumeffect[i].material());
                itemmeta.setDisplayName(ChatColor.UNDERLINE + "" + ChatColor.BOLD + ChatColor.stripColor(premiumeffect[i].desc()));
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + premiumeffect[i].explain()
                        , ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット"
                );
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
            }
            //プレイヤーがそのスキルをまだ取得していない場合の処理
            else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(premiumeffect[i].desc());
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + premiumeffect[i].explain()
                        , ChatColor.RESET + "" + ChatColor.YELLOW + "必要プレミアムポイント：" + premiumeffect[i].usePoint()
                        , ChatColor.RESET + "" + ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
            }
            inventory.setItem(i + 27, itemstack);
        }

        return inventory;
    }

    //プレミア購入履歴表示
    public static Inventory getBuyRecordMenuData(Player player) {
        PlayerData playerdata = playermap.apply(player.getUniqueId());
        Inventory inventory = Bukkit.getServer().createInventory(null, 4 * 9, ChatColor.BLUE + "" + ChatColor.BOLD + "プレミアムエフェクト購入履歴");
        ItemStack itemstack;
        SkullMeta skullmeta;
        List<String> lore;

        // 1ページ目を開く
        itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        skullmeta = ItemMetaFactory.SKULL.getValue();
        itemstack.setDurability((short) 3);
        skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "エフェクト選択メニューへ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
        skullmeta.setLore(lore);
        skullmeta.setOwner("MHF_ArrowLeft");
        itemstack.setItemMeta(skullmeta);
        AsyncInventorySetter.setItemAsync(inventory, 27, itemstack.clone());

        databaseGateway.donateDataManipulator.loadDonateData(playerdata, inventory);

        return inventory;
    }

    //実績メニュー
    public static Inventory getTitleMenuData(Player p) {
        //プレイヤーを取得
        Player player = p.getPlayer();
        //UUID取得
        UUID uuid = player.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
            Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            return null;
        }

        Inventory inventory = Bukkit.getServer().createInventory(null, 4 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績・二つ名システム");
        ItemStack itemstack;
        ItemMeta itemmeta;
        SkullMeta skullmeta;
        List<String> lore;

        //表示切り替え(LV・二つ名)
        itemstack = new ItemStack(Material.REDSTONE_TORCH_ON, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.REDSTONE_TORCH_ON);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地レベルを表示");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "このボタンをクリックすると、"
                , ChatColor.RESET + "" + ChatColor.RED + "「整地LV」に表示を切り替えます。"
                , ChatColor.RESET + "" + ChatColor.YELLOW + "※反映されるまで最大1分ほどかかります。");
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(0, itemstack);

        //予約付与受け取りボタン
        if (!(playerdata.giveachvNo() == 0)) {
            itemstack = new ItemStack(Material.SKULL_ITEM, 1);
            skullmeta = ItemMetaFactory.SKULL.getValue();
            itemstack.setDurability((short) 3);
            skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "【実績付与システム】");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "運営チームからあなたへ、"
                    , ChatColor.RESET + "" + ChatColor.RED + "「二つ名」のプレゼントが届いています。"
                    , ChatColor.RESET + "" + ChatColor.YELLOW + "クリックすることで受け取れます！");
            skullmeta.setLore(lore);
            skullmeta.setOwner("MHF_Present2");
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, 1, itemstack.clone());
        }

        //二つ名組合せシステム画面へ移動
        itemstack = new ItemStack(Material.ANVIL, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.ANVIL);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "「二つ名組合せシステム」");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "設定画面を表示します。");
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(8, itemstack);

        //カテゴリ画面へ移動
        itemstack = new ItemStack(Material.GOLD_PICKAXE, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GOLD_PICKAXE);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "カテゴリ「整地」");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "以下の実績が含まれます。"
                , ChatColor.RESET + "" + ChatColor.AQUA + "「整地量」"
                , ChatColor.RESET + "" + ChatColor.AQUA + "「整地神ランキング」");
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(10, itemstack);

        itemstack = new ItemStack(Material.GLASS, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GLASS);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "カテゴリ「建築」");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.YELLOW + "今後実装予定のカテゴリです。");
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(12, itemstack);

        itemstack = new ItemStack(Material.COMPASS, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COMPASS);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "カテゴリ「ログイン」");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "以下の実績が含まれます。"
                , ChatColor.RESET + "" + ChatColor.AQUA + "「参加時間」"
                , ChatColor.RESET + "" + ChatColor.AQUA + "「通算ログイン」"
                , ChatColor.RESET + "" + ChatColor.AQUA + "「連続ログイン」"
                , ChatColor.RESET + "" + ChatColor.AQUA + "「記念日」");
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(14, itemstack);

        itemstack = new ItemStack(Material.BLAZE_POWDER, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BLAZE_POWDER);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "カテゴリ「やりこみ」");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "以下の実績が含まれます。"
                , ChatColor.RESET + "" + ChatColor.AQUA + "「MEBIUSブリーダー」"
                , ChatColor.RESET + "" + ChatColor.AQUA + "「スターレベル」");
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(16, itemstack);

        itemstack = new ItemStack(Material.EYE_OF_ENDER, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.EYE_OF_ENDER);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "カテゴリ「特殊」");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "以下の実績が含まれます。"
                , ChatColor.RESET + "" + ChatColor.AQUA + "「公式イベント」"
                , ChatColor.RESET + "" + ChatColor.AQUA + "「JMS投票数」"
                , ChatColor.RESET + "" + ChatColor.AQUA + "「極秘実績」");
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(22, itemstack);


        // 1ページ目を開く
        itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        skullmeta = ItemMetaFactory.SKULL.getValue();
        itemstack.setDurability((short) 3);
        skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
        skullmeta.setLore(lore);
        skullmeta.setOwner("MHF_ArrowLeft");
        itemstack.setItemMeta(skullmeta);
        AsyncInventorySetter.setItemAsync(inventory, 27, itemstack.clone());

        return inventory;
    }

    //二つ名組合せシステム・メニュー
    public static Inventory setFreeTitleMainData(Player p) {
        //プレイヤーを取得
        Player player = p.getPlayer();
        //UUID取得
        UUID uuid = player.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
            Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            return null;
        }

        Inventory inventory = Bukkit.getServer().createInventory(null, 4 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "二つ名組合せシステム");
        ItemStack itemstack;
        ItemMeta itemmeta;
        SkullMeta skullmeta;
        List<String> lore;

        //各ボタンの設定
        nextpageflag1 = false;
        nextpageflag2 = false;
        nextpageflag3 = false;
        nextpageflagS = false;
        checkTitle1 = 0;
        checkTitle2 = 0;
        checkTitle3 = 0;
        checkTitleS = 0;
        NoKeep = 0;

        //実績ポイントの最新情報反映ボタン
        itemstack = new ItemStack(Material.EMERALD_ORE, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.EMERALD_ORE);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績ポイント 情報");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + "クリックで情報を最新化"
                , ChatColor.RESET + "" + ChatColor.RED + "累計獲得量：" + playerdata.achievePoint().cumulativeTotal()
                , ChatColor.RESET + "" + ChatColor.RED + "累計消費量：" + playerdata.achievePoint().used()
                , ChatColor.RESET + "" + ChatColor.AQUA + "使用可能量：" + playerdata.achievePoint().left());
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(0, itemstack);

        //パーツショップ
        itemstack = new ItemStack(Material.ITEM_FRAME, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.ITEM_FRAME);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績ポイントショップ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + "クリックで開きます");
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(9, itemstack);

        //エフェクトポイントからの変換ボタン
        itemstack = new ItemStack(Material.EMERALD, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.EMERALD);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ポイント変換ボタン");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "JMS投票で手に入るポイントを"
                , ChatColor.RESET + "" + ChatColor.RED + "実績ポイントに変換できます。"
                , ChatColor.RESET + "" + ChatColor.YELLOW + "" + ChatColor.BOLD + "投票pt 10pt → 実績pt 3pt"
                , ChatColor.RESET + "" + ChatColor.AQUA + "クリックで変換を一回行います。"
                , ChatColor.RESET + "" + ChatColor.GREEN + "所有投票pt :" + playerdata.activeskilldata().effectpoint
                , ChatColor.RESET + "" + ChatColor.GREEN + "所有実績pt :" + playerdata.achievePoint().left());
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(1, itemstack);


        itemstack = new ItemStack(Material.BOOK, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BOOK);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "現在の二つ名の確認");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "「" + SeichiAssist.seichiAssistConfig().getTitle1(playerdata.settings().nickName().id1())
                + SeichiAssist.seichiAssistConfig().getTitle2(playerdata.settings().nickName().id2()) + SeichiAssist.seichiAssistConfig().getTitle3(playerdata.settings().nickName().id3()) + "」");
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(4, itemstack);

        itemstack = new ItemStack(Material.WATER_BUCKET, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.WATER_BUCKET);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "前パーツ選択画面");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "クリックで移動します");
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(11, itemstack);

        itemstack = new ItemStack(Material.MILK_BUCKET, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.MILK_BUCKET);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "中パーツ選択画面");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "クリックで移動します");
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(13, itemstack);

        itemstack = new ItemStack(Material.LAVA_BUCKET, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.LAVA_BUCKET);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "後パーツ選択画面");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "クリックで移動します");
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(15, itemstack);

        itemstack = new ItemStack(Material.WATER_BUCKET, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.WATER_BUCKET);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "前パーツ選択画面");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "クリックで移動します");
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(11, itemstack);


        // 1ページ目を開く
        itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        skullmeta = ItemMetaFactory.SKULL.getValue();
        itemstack.setDurability((short) 3);
        skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績・二つ名メニューへ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
        skullmeta.setLore(lore);
        skullmeta.setOwner("MHF_ArrowLeft");
        itemstack.setItemMeta(skullmeta);
        AsyncInventorySetter.setItemAsync(inventory, 27, itemstack.clone());

        return inventory;
    }

    //二つ名組合せ「前パーツ」
    public static Inventory setFreeTitle1Data(Player p) {
        //プレイヤーを取得
        Player player = p.getPlayer();
        //UUID取得
        UUID uuid = player.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
            Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            return null;
        }

        Inventory inventory = Bukkit.getServer().createInventory(null, 4 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "二つ名組合せ「前」");
        ItemStack itemstack;
        ItemMeta itemmeta;
        SkullMeta skullmeta;
        List<String> lore;


        if (nextpageflag1) {
            nextpageflag1 = false;
        } else {
            checkTitle1 = 1000;
        }

        //各ボタンの設定
        //解禁済みの実績をチェック→前パーツがあるかをチェック→あればボタン配置
        int checkInv = 0;
        for (; checkTitle1 < 9900; ) {
            if (checkInv < 27) {
                if (playerdata.TitleFlags().contains(checkTitle1)) {
                    if (SeichiAssist.seichiAssistConfig().getTitle1(checkTitle1) == null || SeichiAssist.seichiAssistConfig().getTitle1(checkTitle1).equals("")) {
                    } else {
                        itemstack = new ItemStack(Material.WATER_BUCKET, 1);
                        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.WATER_BUCKET);
                        itemmeta.setDisplayName(String.valueOf(checkTitle1));
                        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "前パーツ「" + SeichiAssist.seichiAssistConfig().getTitle1(checkTitle1) + "」");
                        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        itemmeta.setLore(lore);
                        itemstack.setItemMeta(itemmeta);
                        inventory.setItem(checkInv, itemstack);

                        checkInv++;
                    }

                }
            } else if (checkInv == 27) {
                //次ページへのボタンを配置
                itemstack = new ItemStack(Material.SKULL_ITEM, 1);
                skullmeta = ItemMetaFactory.SKULL.getValue();
                itemstack.setDurability((short) 3);
                skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "次ページへ");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
                skullmeta.setLore(lore);
                skullmeta.setOwner("MHF_ArrowRight");
                itemstack.setItemMeta(skullmeta);
                AsyncInventorySetter.setItemAsync(inventory, 35, itemstack.clone());

                nextpageflag1 = true;

                break;
            }
            checkTitle1++;
        }


        //パーツ未選択状態にするボタン
        itemstack = new ItemStack(Material.GRASS, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GRASS);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "前パーツを未選択状態にする");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで実行");
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(31, itemstack);

        // 二つ名組合せメインページを開く
        itemstack = new ItemStack(Material.BARRIER, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BARRIER);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "二つ名組合せメインメニューへ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(27, itemstack);

        return inventory;
    }

    //二つ名組合せ「中パーツ」
    public static Inventory setFreeTitle2Data(Player p) {
        //プレイヤーを取得
        Player player = p.getPlayer();
        //UUID取得
        UUID uuid = player.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
            Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            return null;
        }

        Inventory inventory = Bukkit.getServer().createInventory(null, 4 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "二つ名組合せ「中」");
        ItemStack itemstack;
        ItemMeta itemmeta;
        SkullMeta skullmeta;
        List<String> lore;


        if (nextpageflag2) {
            nextpageflag2 = false;
        } else {
            checkTitle2 = 9900;
        }

        //各ボタンの設定
        //パーツがあるかをチェック→あればボタン配置
        int checkInv = 0;
        for (; checkTitle2 < 9999; ) {
            if (checkInv < 27) {
                //一部の「隠し中パーツ」は取得しているかの確認
                if (9911 <= checkTitle2  /*&& checkTitle2 <= 9927*/) {
                    if (playerdata.TitleFlags().contains(checkTitle2)) {
                        if (SeichiAssist.seichiAssistConfig().getTitle2(checkTitle2) == null || SeichiAssist.seichiAssistConfig().getTitle2(checkTitle2).equals("")) {
                        } else {
                            itemstack = new ItemStack(Material.MILK_BUCKET, 1);
                            itemmeta = Bukkit.getItemFactory().getItemMeta(Material.MILK_BUCKET);
                            itemmeta.setDisplayName(String.valueOf(checkTitle2));
                            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "中パーツ「" + SeichiAssist.seichiAssistConfig().getTitle2(checkTitle2) + "」");
                            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                            itemmeta.setLore(lore);
                            itemstack.setItemMeta(itemmeta);
                            inventory.setItem(checkInv, itemstack);

                            checkInv++;
                        }
                    }
                } else if (SeichiAssist.seichiAssistConfig().getTitle2(checkTitle2) == null || SeichiAssist.seichiAssistConfig().getTitle2(checkTitle2).equals("")) {
                } else {
                    itemstack = new ItemStack(Material.MILK_BUCKET, 1);
                    itemmeta = Bukkit.getItemFactory().getItemMeta(Material.MILK_BUCKET);
                    itemmeta.setDisplayName(String.valueOf(checkTitle2));
                    lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "中パーツ「" + SeichiAssist.seichiAssistConfig().getTitle2(checkTitle2) + "」");
                    itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    itemmeta.setLore(lore);
                    itemstack.setItemMeta(itemmeta);
                    inventory.setItem(checkInv, itemstack);

                    checkInv++;
                }


            } else if (checkInv == 27) {
                //次ページへのボタンを配置
                itemstack = new ItemStack(Material.SKULL_ITEM, 1);
                skullmeta = ItemMetaFactory.SKULL.getValue();
                itemstack.setDurability((short) 3);
                skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "次ページへ");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
                skullmeta.setLore(lore);
                skullmeta.setOwner("MHF_ArrowRight");
                itemstack.setItemMeta(skullmeta);
                AsyncInventorySetter.setItemAsync(inventory, 35, itemstack.clone());

                break;
            }
            checkTitle2++;
        }


        //パーツ未選択状態にするボタン
        itemstack = new ItemStack(Material.GRASS, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GRASS);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "中パーツを未選択状態にする");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで実行");
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(31, itemstack);

        // 二つ名組合せメインページを開く
        itemstack = new ItemStack(Material.BARRIER, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BARRIER);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "二つ名組合せメインメニューへ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(27, itemstack);

        return inventory;
    }

    //二つ名組合せ「後パーツ」
    public static Inventory setFreeTitle3Data(Player p) {
        //プレイヤーを取得
        Player player = p.getPlayer();
        //UUID取得
        UUID uuid = player.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
            Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            return null;
        }

        Inventory inventory = Bukkit.getServer().createInventory(null, 4 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "二つ名組合せ「後」");
        ItemStack itemstack;
        ItemMeta itemmeta;
        SkullMeta skullmeta;
        List<String> lore;

        if (nextpageflag3) {
        } else {
            checkTitle3 = 1000;
        }

        //各ボタンの設定
        //解禁済みの実績をチェック→前パーツがあるかをチェック→あればボタン配置
        int checkInv = 0;
        for (; checkTitle3 < 9900; ) {
            if (checkInv < 27) {
                if (playerdata.TitleFlags().contains(checkTitle3)) {
                    if (SeichiAssist.seichiAssistConfig().getTitle3(checkTitle3) == null || SeichiAssist.seichiAssistConfig().getTitle3(checkTitle3).equals("")) {
                    } else {
                        itemstack = new ItemStack(Material.LAVA_BUCKET, 1);
                        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.LAVA_BUCKET);
                        itemmeta.setDisplayName(String.valueOf(checkTitle3));
                        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "後パーツ「" + SeichiAssist.seichiAssistConfig().getTitle3(checkTitle3) + "」");
                        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        itemmeta.setLore(lore);
                        itemstack.setItemMeta(itemmeta);
                        inventory.setItem(checkInv, itemstack);

                        checkInv++;
                    }

                }
            } else if (checkInv == 27) {
                //次ページへのボタンを配置
                itemstack = new ItemStack(Material.SKULL_ITEM, 1);
                skullmeta = ItemMetaFactory.SKULL.getValue();
                itemstack.setDurability((short) 3);
                skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "次ページへ");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
                skullmeta.setLore(lore);
                skullmeta.setOwner("MHF_ArrowRight");
                itemstack.setItemMeta(skullmeta);
                AsyncInventorySetter.setItemAsync(inventory, 35, itemstack.clone());

                nextpageflag3 = true;

                break;
            }
            checkTitle3++;
        }

        //パーツ未選択状態にするボタン
        itemstack = new ItemStack(Material.GRASS, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GRASS);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "後パーツを未選択状態にする");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで実行");
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(31, itemstack);


        // 二つ名組合せメインページを開く
        itemstack = new ItemStack(Material.BARRIER, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BARRIER);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "二つ名組合せメインメニューへ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(27, itemstack);

        return inventory;
    }


    //実績ポイントショップ
    public static Inventory setTitleShopData(Player p) {
        //プレイヤーを取得
        Player player = p.getPlayer();
        //UUID取得
        UUID uuid = player.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
            Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            return null;
        }

        Inventory inventory = Bukkit.getServer().createInventory(null, 4 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績ポイントショップ");
        ItemStack itemstack;
        ItemMeta itemmeta;
        SkullMeta skullmeta;
        List<String> lore;

        //実績ポイントの最新情報反映ボタン
        itemstack = new ItemStack(Material.EMERALD_ORE, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.EMERALD_ORE);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績ポイント 情報");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + "クリックで情報を最新化"
                , ChatColor.RESET + "" + ChatColor.RED + "累計獲得量：" + playerdata.achievePoint().cumulativeTotal()
                , ChatColor.RESET + "" + ChatColor.RED + "累計消費量：" + playerdata.achievePoint().used()
                , ChatColor.RESET + "" + ChatColor.AQUA + "使用可能量：" + playerdata.achievePoint().left());
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(0, itemstack);

        //おしながき
        if (playerdata.samepageflag()) {
            checkTitleS = NoKeep;
        } else if (!nextpageflagS) {
            checkTitleS = 9801;
        }
        NoKeep = checkTitleS;
        playerdata.samepageflag_$eq(false);
        int setInv = 1;
        for (; checkTitleS <= 9832; ) {
            if (setInv < 27) {
                if (!playerdata.TitleFlags().contains(checkTitleS)) {
                    itemstack = new ItemStack(Material.BEDROCK, 1);
                    itemmeta = ItemMetaFactory.BEDROCK.getValue();
                    itemmeta.setDisplayName(String.valueOf(checkTitleS));
                    lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "前・後パーツ「" + SeichiAssist.seichiAssistConfig().getTitle1(checkTitleS) + "」"
                            , ChatColor.RESET + "" + ChatColor.GREEN + "必要ポイント：20"
                            , ChatColor.RESET + "" + ChatColor.AQUA + "クリックで購入できます");
                    itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    itemmeta.setLore(lore);
                    itemstack.setItemMeta(itemmeta);
                    inventory.setItem(setInv, itemstack);

                    setInv++;
                }
            } else {
                //次ページへのボタンを配置
                itemstack = new ItemStack(Material.SKULL_ITEM, 1);
                skullmeta = ItemMetaFactory.SKULL.getValue();
                itemstack.setDurability((short) 3);
                skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "次ページへ");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
                skullmeta.setLore(lore);
                skullmeta.setOwner("MHF_ArrowRight");
                itemstack.setItemMeta(skullmeta);
                AsyncInventorySetter.setItemAsync(inventory, 35, itemstack.clone());

                nextpageflagS = true;

                break;
            }
            checkTitleS++;
        }
        if (checkTitleS < 9911) {
            checkTitleS = 9911;
        }
        for (; checkTitleS <= 9932; ) {
            if (setInv < 27) {
                if (!playerdata.TitleFlags().contains(checkTitleS)) {
                    itemstack = new ItemStack(Material.BEDROCK, 1);
                    itemmeta = ItemMetaFactory.BEDROCK.getValue();
                    itemmeta.setDisplayName(String.valueOf(checkTitleS));
                    lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "中パーツ「" + SeichiAssist.seichiAssistConfig().getTitle2(checkTitleS) + "」"
                            , ChatColor.RESET + "" + ChatColor.GREEN + "必要ポイント：35"
                            , ChatColor.RESET + "" + ChatColor.AQUA + "クリックで購入できます");
                    itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    itemmeta.setLore(lore);
                    itemstack.setItemMeta(itemmeta);
                    inventory.setItem(setInv, itemstack);

                    setInv++;
                }
            } else {
                //次ページへのボタンを配置
                itemstack = new ItemStack(Material.SKULL_ITEM, 1);
                skullmeta = ItemMetaFactory.SKULL.getValue();
                itemstack.setDurability((short) 3);
                skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "次ページへ");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
                skullmeta.setLore(lore);
                skullmeta.setOwner("MHF_ArrowRight");
                itemstack.setItemMeta(skullmeta);
                AsyncInventorySetter.setItemAsync(inventory, 35, itemstack.clone());

                nextpageflagS = true;

                break;
            }
            checkTitleS++;
        }


        // 二つ名組合せメインページを開く
        itemstack = new ItemStack(Material.BARRIER, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BARRIER);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "二つ名組合せメインメニューへ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(27, itemstack);

        return inventory;
    }

    //実績カテ「整地」
    public static Inventory getTitleSeichi(Player p) {
        //プレイヤーを取得
        Player player = p.getPlayer();
        //UUID取得
        UUID uuid = player.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
            Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            return null;
        }

        Inventory inventory = Bukkit.getServer().createInventory(null, 4 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "カテゴリ「整地」");
        ItemStack itemstack;
        ItemMeta itemmeta;
        SkullMeta skullmeta;
        List<String> lore;


        //ボタン情報
        itemstack = new ItemStack(Material.IRON_PICKAXE, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.IRON_PICKAXE);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「整地量」");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "獲得状況を表示します。");
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(12, itemstack);

        itemstack = new ItemStack(Material.DIAMOND_PICKAXE, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_PICKAXE);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「整地神ランキング」");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "獲得状況を表示します。");
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(14, itemstack);

        // 1ページ目を開く
        itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        skullmeta = ItemMetaFactory.SKULL.getValue();
        itemstack.setDurability((short) 3);
        skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績・二つ名メニューへ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
        skullmeta.setLore(lore);
        skullmeta.setOwner("MHF_ArrowLeft");
        itemstack.setItemMeta(skullmeta);
        AsyncInventorySetter.setItemAsync(inventory, 27, itemstack.clone());

        return inventory;
    }

    //実績カテ「建築」
    public static Inventory getTitleBuild(Player p) {
        //プレイヤーを取得
        Player player = p.getPlayer();
        //UUID取得
        UUID uuid = player.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
            Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            return null;
        }

        Inventory inventory = Bukkit.getServer().createInventory(null, 4 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "カテゴリ「建築」");
        ItemStack itemstack;
        ItemMeta itemmeta;
        SkullMeta skullmeta;
        List<String> lore;


        //ボタン情報
        // [ここにカテゴリを設定]

        // 1ページ目を開く
        itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        skullmeta = ItemMetaFactory.SKULL.getValue();
        itemstack.setDurability((short) 3);
        skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績・二つ名メニューへ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
        skullmeta.setLore(lore);
        skullmeta.setOwner("MHF_ArrowLeft");
        itemstack.setItemMeta(skullmeta);
        AsyncInventorySetter.setItemAsync(inventory, 27, itemstack.clone());

        return inventory;
    }

    //実績カテ「ログイン」
    public static Inventory getTitleLogin(Player p) {
        //プレイヤーを取得
        Player player = p.getPlayer();
        //UUID取得
        UUID uuid = player.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
            Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            return null;
        }

        Inventory inventory = Bukkit.getServer().createInventory(null, 4 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "カテゴリ「ログイン」");
        ItemStack itemstack;
        ItemMeta itemmeta;
        SkullMeta skullmeta;
        List<String> lore;


        //ボタン情報
        itemstack = new ItemStack(Material.COMPASS, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COMPASS);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「参加時間」");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "獲得状況を表示します。");
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(10, itemstack);

        itemstack = new ItemStack(Material.BOOK, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BOOK);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「通算ログイン」");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "獲得状況を表示します。");
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(12, itemstack);

        itemstack = new ItemStack(Material.BOOK_AND_QUILL, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BOOK_AND_QUILL);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「連続ログイン」");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "獲得状況を表示します。");
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(14, itemstack);

        itemstack = new ItemStack(Material.NETHER_STAR, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.NETHER_STAR);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「記念日」");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "獲得状況を表示します。");
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(16, itemstack);

        // 1ページ目を開く
        itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        skullmeta = ItemMetaFactory.SKULL.getValue();
        itemstack.setDurability((short) 3);
        skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績・二つ名メニューへ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
        skullmeta.setLore(lore);
        skullmeta.setOwner("MHF_ArrowLeft");
        itemstack.setItemMeta(skullmeta);
        AsyncInventorySetter.setItemAsync(inventory, 27, itemstack.clone());

        return inventory;
    }

    //実績カテ「やりこみ」
    public static Inventory getTitleSuperTry(Player p) {
        //プレイヤーを取得
        Player player = p.getPlayer();
        //UUID取得
        UUID uuid = player.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
            Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            return null;
        }

        Inventory inventory = Bukkit.getServer().createInventory(null, 4 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "カテゴリ「やりこみ」");
        ItemStack itemstack;
        ItemMeta itemmeta;
        SkullMeta skullmeta;
        List<String> lore;


        //ボタン情報
        itemstack = new ItemStack(Material.DIAMOND_HELMET, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_HELMET);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「MEBIUSブリーダー」");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "獲得状況を表示します。※未実装");
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(12, itemstack);

        itemstack = new ItemStack(Material.GOLD_INGOT, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GOLD_INGOT);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「スターレベル」");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "獲得状況を表示します。※未実装");
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(14, itemstack);

        // 1ページ目を開く
        itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        skullmeta = ItemMetaFactory.SKULL.getValue();
        itemstack.setDurability((short) 3);
        skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績・二つ名メニューへ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
        skullmeta.setLore(lore);
        skullmeta.setOwner("MHF_ArrowLeft");
        itemstack.setItemMeta(skullmeta);
        AsyncInventorySetter.setItemAsync(inventory, 27, itemstack.clone());

        return inventory;
    }

    //実績カテ「特殊」
    public static Inventory getTitleSpecial(Player p) {
        //プレイヤーを取得
        Player player = p.getPlayer();
        //UUID取得
        UUID uuid = player.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
            Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            return null;
        }

        Inventory inventory = Bukkit.getServer().createInventory(null, 4 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "カテゴリ「特殊」");
        ItemStack itemstack;
        ItemMeta itemmeta;
        SkullMeta skullmeta;
        List<String> lore;


        //ボタン情報
        itemstack = new ItemStack(Material.BLAZE_POWDER, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BLAZE_POWDER);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「公式イベント」");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "獲得状況を表示します。");
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(11, itemstack);

        itemstack = new ItemStack(Material.YELLOW_FLOWER, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.YELLOW_FLOWER);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「JMS投票数」");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "獲得状況を表示します。");
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(13, itemstack);

        itemstack = new ItemStack(Material.DIAMOND_BARDING, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BARDING);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「極秘任務」");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "獲得状況を表示します。"
                , ChatColor.RESET + "" + ChatColor.AQUA + "※獲得前の実績は表示されません。");
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(15, itemstack);

        // 1ページ目を開く
        itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        skullmeta = ItemMetaFactory.SKULL.getValue();
        itemstack.setDurability((short) 3);
        skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績・二つ名メニューへ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
        skullmeta.setLore(lore);
        skullmeta.setOwner("MHF_ArrowLeft");
        itemstack.setItemMeta(skullmeta);
        AsyncInventorySetter.setItemAsync(inventory, 27, itemstack.clone());

        return inventory;
    }

    //実績「整地神ランキング」
    public static Inventory getTitleRankData(Player p) {
        //プレイヤーを取得
        Player player = p.getPlayer();
        //UUID取得
        UUID uuid = player.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
            Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            return null;
        }

        Inventory inventory = Bukkit.getServer().createInventory(null, 4 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「整地神ランキング」");
        ItemStack itemstack;
        ItemMeta itemmeta;
        SkullMeta skullmeta;
        List<String> lore;


        //実績設定・解禁ボタン
        //1位
        if (playerdata.TitleFlags().contains(1001)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1001「" + SeichiAssist.seichiAssistConfig().getTitle1(1001) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：「整地神ランキング」1位達成"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(0, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1001「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：「整地神ランキング」1位達成"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(0, itemstack);
        }
        //5位
        if (playerdata.TitleFlags().contains(1002)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1002「" + SeichiAssist.seichiAssistConfig().getTitle1(1002) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：「整地神ランキング」5位達成"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(1, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1002「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：「整地神ランキング」5位達成"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(1, itemstack);
        }
        //27位
        if (playerdata.TitleFlags().contains(1003)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1003「" + SeichiAssist.seichiAssistConfig().getTitle1(1003) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：「整地神ランキング」27位達成"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(2, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1003「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：「整地神ランキング」27位達成"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(2, itemstack);
        }
        //50位
        if (playerdata.TitleFlags().contains(1004)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1004「" + SeichiAssist.seichiAssistConfig().getTitle1(1004) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：「整地神ランキング」50位達成"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(3, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1004「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：「整地神ランキング」50位達成"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(3, itemstack);
        }
        //100位
        if (playerdata.TitleFlags().contains(1010)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1010「" + SeichiAssist.seichiAssistConfig().getTitle1(1010) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：「整地神ランキング」100位達成"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(4, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1010「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：「整地神ランキング」100位達成"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(4, itemstack);
        }
        //250位
        if (playerdata.TitleFlags().contains(1011)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1011「" + SeichiAssist.seichiAssistConfig().getTitle1(1011)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9904) + SeichiAssist.seichiAssistConfig().getTitle3(1011) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：「整地神ランキング」250位達成"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(5, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1011「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：「整地神ランキング」250位達成"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(5, itemstack);
        }
        //500位
        if (playerdata.TitleFlags().contains(1012)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1012「" + SeichiAssist.seichiAssistConfig().getTitle1(1012)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9901) + SeichiAssist.seichiAssistConfig().getTitle3(1012) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：「整地神ランキング」500位達成"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(6, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1012「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：「整地神ランキング」500位達成"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(6, itemstack);
        }
        //750位
        if (playerdata.TitleFlags().contains(1005)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1005「" + SeichiAssist.seichiAssistConfig().getTitle1(1005)
                    + SeichiAssist.seichiAssistConfig().getTitle3(1005) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：「整地神ランキング」750位達成"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(7, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1005「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：「整地神ランキング」750位達成"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(7, itemstack);
        }
        //1000位
        if (playerdata.TitleFlags().contains(1006)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1006「" + SeichiAssist.seichiAssistConfig().getTitle1(1006) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：「整地神ランキング」1000位達成"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(8, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1006「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：「整地神ランキング」1000位達成"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(8, itemstack);
        }
        //2500位
        if (playerdata.TitleFlags().contains(1007)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1007「" + SeichiAssist.seichiAssistConfig().getTitle1(1007)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9904) + SeichiAssist.seichiAssistConfig().getTitle3(1007) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：「整地神ランキング」2500位達成"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(9, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1007「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：「整地神ランキング」2500位達成"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(9, itemstack);
        }
        //5000位
        if (playerdata.TitleFlags().contains(1008)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1008「" + SeichiAssist.seichiAssistConfig().getTitle1(1008)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9901) + SeichiAssist.seichiAssistConfig().getTitle3(1008) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：「整地神ランキング」5000位達成"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(10, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1008「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：「整地神ランキング」5000位達成"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(10, itemstack);
        }
        //10000位
        if (playerdata.TitleFlags().contains(1009)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1009「" + SeichiAssist.seichiAssistConfig().getTitle1(1009)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9909) + SeichiAssist.seichiAssistConfig().getTitle3(1009) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：「整地神ランキング」10000位達成"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(11, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1009「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：「整地神ランキング」10000位達成"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(11, itemstack);
        }


        // 1ページ目を開く
        itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        skullmeta = ItemMetaFactory.SKULL.getValue();
        itemstack.setDurability((short) 3);
        skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "「整地」カテゴリメニューへ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
        skullmeta.setLore(lore);
        skullmeta.setOwner("MHF_ArrowLeft");
        itemstack.setItemMeta(skullmeta);
        AsyncInventorySetter.setItemAsync(inventory, 27, itemstack.clone());

        return inventory;
    }


    //実績「整地量」
    public static Inventory getTitleAmountData(Player p) {
        //プレイヤーを取得
        Player player = p.getPlayer();
        //UUID取得
        UUID uuid = player.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
            Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            return null;
        }

        Inventory inventory = Bukkit.getServer().createInventory(null, 4 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「整地量」");
        ItemStack itemstack;
        ItemMeta itemmeta;
        SkullMeta skullmeta;
        List<String> lore;


        //実績設定・解禁ボタン
        //90億突破前は表示されない処理
        if (playerdata.TitleFlags().contains(3018)) {
            //100億
            if (playerdata.TitleFlags().contains(3019)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3019「" + SeichiAssist.seichiAssistConfig().getTitle1(3019) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 100億 を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(25, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3019「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が???を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(25, itemstack);
            }
        } else {
        }
        //80億突破前は表示されない処理
        if (playerdata.TitleFlags().contains(3017)) {
            //90億
            if (playerdata.TitleFlags().contains(3018)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3018「" + SeichiAssist.seichiAssistConfig().getTitle1(3018) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 90億 を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(24, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3018「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が???を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(24, itemstack);
            }
        } else {
        }
        //70億突破前は表示されない処理
        if (playerdata.TitleFlags().contains(3016)) {
            //80億
            if (playerdata.TitleFlags().contains(3017)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3017「" + SeichiAssist.seichiAssistConfig().getTitle1(3017) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 80億 を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(23, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3017「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が???を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(23, itemstack);
            }
        } else {
        }
        //60億突破前は表示されない処理
        if (playerdata.TitleFlags().contains(3015)) {
            //70億
            if (playerdata.TitleFlags().contains(3016)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3016「" + SeichiAssist.seichiAssistConfig().getTitle1(3016) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 70億 を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(22, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3016「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が???を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(22, itemstack);
            }
        } else {
        }
        //50億突破前は表示されない処理
        if (playerdata.TitleFlags().contains(3014)) {
            //60億
            if (playerdata.TitleFlags().contains(3015)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3015「" + SeichiAssist.seichiAssistConfig().getTitle1(3015) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 60億 を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(21, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3015「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が???を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(21, itemstack);
            }
        } else {
        }
        //int実績突破前は表示されない処理
        if (playerdata.TitleFlags().contains(3001)) {
            //50億
            if (playerdata.TitleFlags().contains(3014)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3014「" + SeichiAssist.seichiAssistConfig().getTitle1(3014)
                        + SeichiAssist.seichiAssistConfig().getTitle2(9909) + SeichiAssist.seichiAssistConfig().getTitle3(3014) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 50億 を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(20, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3014「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が???を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(20, itemstack);
            }
            //40億
            if (playerdata.TitleFlags().contains(3013)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3013「" + SeichiAssist.seichiAssistConfig().getTitle1(3013)
                        + SeichiAssist.seichiAssistConfig().getTitle2(9905) + SeichiAssist.seichiAssistConfig().getTitle3(3013) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 40億 を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(19, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3013「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が???を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(19, itemstack);
            }
            //30億
            if (playerdata.TitleFlags().contains(3012)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3012「" + SeichiAssist.seichiAssistConfig().getTitle1(3012) +
                        SeichiAssist.seichiAssistConfig().getTitle3(3012) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 30億 を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(18, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3012「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が???を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(18, itemstack);
            }
        } else {
        }
        //「10億」実績突破前は表示されない処理
        if (playerdata.TitleFlags().contains(3002)) {
            //int限界突破
            if (playerdata.TitleFlags().contains(3001)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3001「" + SeichiAssist.seichiAssistConfig().getTitle1(3001) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が「int型の壁」を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(9, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3001「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が???を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(9, itemstack);
            }
        } else {
        }
        //10億突破
        if (playerdata.TitleFlags().contains(3002)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3002「" + SeichiAssist.seichiAssistConfig().getTitle1(3002)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9905) + SeichiAssist.seichiAssistConfig().getTitle3(3002) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 10億 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(0, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3002「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 10億 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(0, itemstack);
        }
        //5億突破
        if (playerdata.TitleFlags().contains(3003)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3003「" + SeichiAssist.seichiAssistConfig().getTitle1(3003) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 5億 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(1, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3003「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 5億 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(1, itemstack);
        }
        //1億突破
        if (playerdata.TitleFlags().contains(3004)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3004「" + SeichiAssist.seichiAssistConfig().getTitle1(3004)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9902) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 1億 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(2, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3004「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 1億 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(2, itemstack);
        }
        //5000万突破
        if (playerdata.TitleFlags().contains(3005)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3005「" + SeichiAssist.seichiAssistConfig().getTitle1(3005)
                    + SeichiAssist.seichiAssistConfig().getTitle3(3005) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 5000万 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(3, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3005「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 5000万 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(3, itemstack);
        }
        //1000万突破
        if (playerdata.TitleFlags().contains(3006)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3006「" + SeichiAssist.seichiAssistConfig().getTitle1(3006) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 1000万 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(4, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3006「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 1000万 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(4, itemstack);
        }
        //500万突破
        if (playerdata.TitleFlags().contains(3007)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3007「" + SeichiAssist.seichiAssistConfig().getTitle1(3007)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9905) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 500万 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(5, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3007「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 500万 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(5, itemstack);
        }
        //100万突破
        if (playerdata.TitleFlags().contains(3008)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3008「" + SeichiAssist.seichiAssistConfig().getTitle1(3008) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 100万 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(6, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3008「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 100万 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(6, itemstack);
        }
        //50万突破
        if (playerdata.TitleFlags().contains(3009)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3009「" + SeichiAssist.seichiAssistConfig().getTitle1(3009)
                    + SeichiAssist.seichiAssistConfig().getTitle3(3009) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 50万 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(7, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3009「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 50万 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(7, itemstack);
        }
        //10万突破
        if (playerdata.TitleFlags().contains(3010)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3010「" + SeichiAssist.seichiAssistConfig().getTitle1(3010)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9909) + SeichiAssist.seichiAssistConfig().getTitle3(3010) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 10万 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(8, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3010「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 10万 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(8, itemstack);
        }
        //1万突破
        if (playerdata.TitleFlags().contains(3011)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3011「" + SeichiAssist.seichiAssistConfig().getTitle1(3011) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 1万 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(17, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3011「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：整地量が 1万 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(17, itemstack);
        }


        // 1ページ目を開く
        itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        skullmeta = ItemMetaFactory.SKULL.getValue();
        itemstack.setDurability((short) 3);
        skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "「整地」カテゴリメニューへ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
        skullmeta.setLore(lore);
        skullmeta.setOwner("MHF_ArrowLeft");
        itemstack.setItemMeta(skullmeta);
        AsyncInventorySetter.setItemAsync(inventory, 27, itemstack.clone());

        return inventory;
    }


    //実績「参加時間」
    public static Inventory getTitleTimeData(Player p) {
        //プレイヤーを取得
        Player player = p.getPlayer();
        //UUID取得
        UUID uuid = player.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
            Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            return null;
        }

        Inventory inventory = Bukkit.getServer().createInventory(null, 4 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「参加時間」");
        ItemStack itemstack;
        ItemMeta itemmeta;
        SkullMeta skullmeta;
        List<String> lore;


        //実績設定・解禁ボタン
        //「18000h」実績突破前は表示されない処理
        if (playerdata.TitleFlags().contains(4022)) {
            //20000h突破
            if (playerdata.TitleFlags().contains(4023)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4023「" + SeichiAssist.seichiAssistConfig().getTitle1(4023)
                        + SeichiAssist.seichiAssistConfig().getTitle3(4023) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 20000時間 を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(22, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4023「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が ??? を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(22, itemstack);
            }
        } else {
        }
        //「16000h」実績突破前は表示されない処理
        if (playerdata.TitleFlags().contains(4021)) {
            //18000h突破
            if (playerdata.TitleFlags().contains(4022)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4022「" + SeichiAssist.seichiAssistConfig().getTitle1(4022)
                        + SeichiAssist.seichiAssistConfig().getTitle2(9903) + SeichiAssist.seichiAssistConfig().getTitle3(4022) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 18000時間 を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(21, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4022「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が ??? を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(21, itemstack);
            }
        } else {
        }
        //「14000h」実績突破前は表示されない処理
        if (playerdata.TitleFlags().contains(4020)) {
            //16000h突破
            if (playerdata.TitleFlags().contains(4021)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4021「" + SeichiAssist.seichiAssistConfig().getTitle1(4021)
                        + SeichiAssist.seichiAssistConfig().getTitle3(4021) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 16000時間 を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(20, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4021「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が ??? を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(20, itemstack);
            }
        } else {
        }
        //「12000h」実績突破前は表示されない処理
        if (playerdata.TitleFlags().contains(4019)) {
            //14000h突破
            if (playerdata.TitleFlags().contains(4020)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4020「" + SeichiAssist.seichiAssistConfig().getTitle1(4020)
                        + SeichiAssist.seichiAssistConfig().getTitle3(4020) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 14000時間 を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(19, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4020「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が ??? を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(19, itemstack);
            }
        } else {
        }
        //「10000h」実績突破前は表示されない処理
        if (playerdata.TitleFlags().contains(4018)) {
            //12000h突破
            if (playerdata.TitleFlags().contains(4019)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4019「" + SeichiAssist.seichiAssistConfig().getTitle1(4019)
                        + SeichiAssist.seichiAssistConfig().getTitle3(4019) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 12000時間 を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(18, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4019「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が ??? を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(18, itemstack);
            }
        } else {
        }
        //「5000h」実績突破前は表示されない処理
        if (playerdata.TitleFlags().contains(4013)) {
            //10000h突破
            if (playerdata.TitleFlags().contains(4018)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4018「" + SeichiAssist.seichiAssistConfig().getTitle1(4018)
                        + SeichiAssist.seichiAssistConfig().getTitle3(4018) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 10000時間 を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(17, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4018「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が ??? を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(17, itemstack);
            }
            //9000h突破
            if (playerdata.TitleFlags().contains(4017)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4017「" + SeichiAssist.seichiAssistConfig().getTitle1(4017) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 9000時間 を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(16, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4017「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が ??? を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(16, itemstack);
            }
            //8000h突破
            if (playerdata.TitleFlags().contains(4016)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4016「" + SeichiAssist.seichiAssistConfig().getTitle1(4016)
                        + SeichiAssist.seichiAssistConfig().getTitle3(4016) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 8000時間 を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(15, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4016「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が ??? を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(15, itemstack);
            }
            //7000h突破
            if (playerdata.TitleFlags().contains(4015)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4015「" + SeichiAssist.seichiAssistConfig().getTitle1(4015) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 7000時間 を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(14, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4015「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が ??? を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(14, itemstack);
            }
            //6000h突破
            if (playerdata.TitleFlags().contains(4014)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4014「" + SeichiAssist.seichiAssistConfig().getTitle1(4014)
                        + SeichiAssist.seichiAssistConfig().getTitle2(9905) + SeichiAssist.seichiAssistConfig().getTitle3(4014) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 6000時間 を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(13, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4014「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が ??? を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(13, itemstack);
            }

        } else {
        }
        //「1000h」実績突破前は表示されない処理
        if (playerdata.TitleFlags().contains(4002)) {
            //5000h突破
            if (playerdata.TitleFlags().contains(4013)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4013「" + SeichiAssist.seichiAssistConfig().getTitle1(4013)
                        + SeichiAssist.seichiAssistConfig().getTitle3(4013) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 5000時間 を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(12, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4013「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が ??? を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(12, itemstack);
            }
            //4000h突破
            if (playerdata.TitleFlags().contains(4012)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4012「" + SeichiAssist.seichiAssistConfig().getTitle1(4012)
                        + SeichiAssist.seichiAssistConfig().getTitle3(4012) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 4000時間 を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(11, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4012「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が ??? を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(11, itemstack);
            }
            //3000h突破
            if (playerdata.TitleFlags().contains(4011)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4011「" + SeichiAssist.seichiAssistConfig().getTitle1(4011)
                        + SeichiAssist.seichiAssistConfig().getTitle2(9901) + SeichiAssist.seichiAssistConfig().getTitle3(4011) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 3000時間 を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(10, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4011「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が ??? を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(10, itemstack);
            }
            //2000h突破
            if (playerdata.TitleFlags().contains(4001)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4001「" + SeichiAssist.seichiAssistConfig().getTitle1(4001)
                        + SeichiAssist.seichiAssistConfig().getTitle2(9905) + SeichiAssist.seichiAssistConfig().getTitle3(4001) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 2000時間 を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(9, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4001「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が ??? を超える"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(9, itemstack);
            }

        } else {
        }
        //1000h突破
        if (playerdata.TitleFlags().contains(4002)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4002「" + SeichiAssist.seichiAssistConfig().getTitle1(4002)
                    + SeichiAssist.seichiAssistConfig().getTitle3(4002) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 1000時間 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(0, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4002「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 1000時間 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(0, itemstack);
        }
        //500h突破
        if (playerdata.TitleFlags().contains(4003)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4003「" + SeichiAssist.seichiAssistConfig().getTitle1(4003)
                    + SeichiAssist.seichiAssistConfig().getTitle3(4003) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 500時間 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(1, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4003「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 500時間 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(1, itemstack);
        }
        //250h突破
        if (playerdata.TitleFlags().contains(4004)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4004「" + SeichiAssist.seichiAssistConfig().getTitle1(4004)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9905) + SeichiAssist.seichiAssistConfig().getTitle3(4004) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 250時間 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(2, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4004「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 250時間 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(2, itemstack);
        }
        //100h突破
        if (playerdata.TitleFlags().contains(4005)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4005「" + SeichiAssist.seichiAssistConfig().getTitle1(4005)
                    + SeichiAssist.seichiAssistConfig().getTitle3(4005) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 100時間 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(3, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4005「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 100時間 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(3, itemstack);
        }
        //50h突破
        if (playerdata.TitleFlags().contains(4006)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4006「" + SeichiAssist.seichiAssistConfig().getTitle1(4006)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9905) + SeichiAssist.seichiAssistConfig().getTitle3(4006) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 50時間 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(4, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4006「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 50時間 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(4, itemstack);
        }
        //24h突破
        if (playerdata.TitleFlags().contains(4007)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4007「" + SeichiAssist.seichiAssistConfig().getTitle1(4007)
                    + SeichiAssist.seichiAssistConfig().getTitle3(4007) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 24時間 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(5, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4007「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 24時間 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(5, itemstack);
        }
        //10h突破
        if (playerdata.TitleFlags().contains(4008)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4008「" + SeichiAssist.seichiAssistConfig().getTitle1(4008)
                    + SeichiAssist.seichiAssistConfig().getTitle3(4008) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 10時間 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(6, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4008「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 10時間 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(6, itemstack);
        }
        //5h突破
        if (playerdata.TitleFlags().contains(4009)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4009「" + SeichiAssist.seichiAssistConfig().getTitle1(4009)
                    + SeichiAssist.seichiAssistConfig().getTitle3(4009) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 5時間 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(7, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4009「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 5時間 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(7, itemstack);
        }
        //1h突破
        if (playerdata.TitleFlags().contains(4010)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4010「" + SeichiAssist.seichiAssistConfig().getTitle1(4010)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9905) + SeichiAssist.seichiAssistConfig().getTitle3(4010) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 1時間 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(8, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4010「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：参加時間が 1時間 を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(8, itemstack);
        }
        if (playerdata.playTick() % 576000 >= 0 && playerdata.playTick() % 576000 <= 1199 && !(playerdata.TitleFlags().contains(8003))) {
            itemstack = new ItemStack(Material.EMERALD_BLOCK, 1);
            itemmeta = Bukkit.getItemFactory().getItemMeta(Material.EMERALD_BLOCK);
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "タイムカード、切りましょ？");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "※何かが起こります※");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(35, itemstack);
        }


        // 1ページ目を開く
        itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        skullmeta = ItemMetaFactory.SKULL.getValue();
        itemstack.setDurability((short) 3);
        skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "「ログイン」カテゴリメニューへ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
        skullmeta.setLore(lore);
        skullmeta.setOwner("MHF_ArrowLeft");
        itemstack.setItemMeta(skullmeta);
        AsyncInventorySetter.setItemAsync(inventory, 27, itemstack.clone());

        return inventory;
    }

    //実績「通算ログイン」
    public static Inventory getTitleJoinAmountData(Player p) {
        //プレイヤーを取得
        Player player = p.getPlayer();
        //UUID取得
        UUID uuid = player.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
            Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            return null;
        }

        Inventory inventory = Bukkit.getServer().createInventory(null, 4 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「通算ログイン」");
        ItemStack itemstack;
        ItemMeta itemmeta;
        SkullMeta skullmeta;
        List<String> lore;


        //実績設定・解禁ボタン
        //「1000日」実績突破前は表示されない処理
        if (playerdata.TitleFlags().contains(5119)) {
            if (playerdata.TitleFlags().contains(5120)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5120「" + SeichiAssist.seichiAssistConfig().getTitle1(5120)
                        + SeichiAssist.seichiAssistConfig().getTitle2(5120) + SeichiAssist.seichiAssistConfig().getTitle3(5120) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 1095日 に到達"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(19, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5120「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が ??? に到達"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(19, itemstack);
            }
        } else {
        }
        //「900日」実績突破前は表示されない処理
        if (playerdata.TitleFlags().contains(5118)) {
            if (playerdata.TitleFlags().contains(5119)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5119「" + SeichiAssist.seichiAssistConfig().getTitle1(5119)
                        + SeichiAssist.seichiAssistConfig().getTitle2(9905) + SeichiAssist.seichiAssistConfig().getTitle3(5119) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 1000日 に到達"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(18, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5119「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が ??? に到達"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(18, itemstack);
            }
        } else {
        }
        //「800日」実績突破前は表示されない処理
        if (playerdata.TitleFlags().contains(5117)) {
            if (playerdata.TitleFlags().contains(5118)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5118「" + SeichiAssist.seichiAssistConfig().getTitle1(5118)
                        + SeichiAssist.seichiAssistConfig().getTitle3(5118) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 900日 に到達"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(17, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5118「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が ??? に到達"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(17, itemstack);
            }
        } else {
        }
        //「730日」実績突破前は表示されない処理
        if (playerdata.TitleFlags().contains(5116)) {
            if (playerdata.TitleFlags().contains(5117)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5117「" + SeichiAssist.seichiAssistConfig().getTitle1(5117)
                        + SeichiAssist.seichiAssistConfig().getTitle3(5117) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 800日 に到達"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(16, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5117「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が ??? に到達"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(16, itemstack);
            }
        } else {
        }
        //「700日」実績突破前は表示されない処理
        if (playerdata.TitleFlags().contains(5115)) {
            if (playerdata.TitleFlags().contains(5116)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5116「" + SeichiAssist.seichiAssistConfig().getTitle1(5116)
                        + SeichiAssist.seichiAssistConfig().getTitle2(9905) + SeichiAssist.seichiAssistConfig().getTitle3(5116) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 730日 に到達"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(15, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5116「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が ??? に到達"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(15, itemstack);
            }
        } else {
        }
        //「600日」実績突破前は表示されない処理
        if (playerdata.TitleFlags().contains(5114)) {
            if (playerdata.TitleFlags().contains(5115)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5115「" + SeichiAssist.seichiAssistConfig().getTitle1(5115) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 700日 に到達"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(14, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5115「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が ??? に到達"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(14, itemstack);
            }
        } else {
        }
        //「500日」実績突破前は表示されない処理
        if (playerdata.TitleFlags().contains(5113)) {
            if (playerdata.TitleFlags().contains(5114)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5114「" + SeichiAssist.seichiAssistConfig().getTitle1(5114)
                        + SeichiAssist.seichiAssistConfig().getTitle3(5114) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 600日 に到達"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(13, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5114「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が ??? に到達"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(13, itemstack);
            }
        } else {
        }
        //「400日」実績突破前は表示されない処理
        if (playerdata.TitleFlags().contains(5112)) {
            if (playerdata.TitleFlags().contains(5113)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5113「" + SeichiAssist.seichiAssistConfig().getTitle1(5113)
                        + SeichiAssist.seichiAssistConfig().getTitle2(9905) + SeichiAssist.seichiAssistConfig().getTitle3(5113) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 500日 に到達"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(12, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5113「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が ??? に到達"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(12, itemstack);
            }
        } else {
        }
        //「365日」実績突破前は表示されない処理
        if (playerdata.TitleFlags().contains(5101)) {
            if (playerdata.TitleFlags().contains(5112)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5112「" + SeichiAssist.seichiAssistConfig().getTitle1(5112)
                        + SeichiAssist.seichiAssistConfig().getTitle3(5112) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 400日 に到達"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(11, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5112「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が ??? に到達"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(11, itemstack);
            }
        } else {
        }
        if (playerdata.TitleFlags().contains(5101)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5101「" + SeichiAssist.seichiAssistConfig().getTitle1(5101)
                    + SeichiAssist.seichiAssistConfig().getTitle3(5101) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 365日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(10, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5101「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 365日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(10, itemstack);
        }
        if (playerdata.TitleFlags().contains(5102)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5102「" + SeichiAssist.seichiAssistConfig().getTitle1(5102)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9907) + SeichiAssist.seichiAssistConfig().getTitle3(5102) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 300日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(9, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5102「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 300日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(9, itemstack);
        }
        if (playerdata.TitleFlags().contains(5103)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5103「" + SeichiAssist.seichiAssistConfig().getTitle1(5103)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9905) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 200日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(8, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5103「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 200日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(8, itemstack);
        }
        if (playerdata.TitleFlags().contains(5104)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5104「" + SeichiAssist.seichiAssistConfig().getTitle1(5104)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9905) + SeichiAssist.seichiAssistConfig().getTitle3(5104) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 100日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(7, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5104「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 100日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(7, itemstack);
        }
        if (playerdata.TitleFlags().contains(5105)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5105「" + SeichiAssist.seichiAssistConfig().getTitle1(5105)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9907) + SeichiAssist.seichiAssistConfig().getTitle3(5105) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 75日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(6, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5105「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 75日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(6, itemstack);
        }
        if (playerdata.TitleFlags().contains(5106)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5106「" + SeichiAssist.seichiAssistConfig().getTitle1(5106) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 50日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(5, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5106「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 50日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(5, itemstack);
        }
        if (playerdata.TitleFlags().contains(5107)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5107「" + SeichiAssist.seichiAssistConfig().getTitle1(5107)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9909) + SeichiAssist.seichiAssistConfig().getTitle3(5107) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 30日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(4, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5107「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 30日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(4, itemstack);
        }
        if (playerdata.TitleFlags().contains(5108)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5108「" + SeichiAssist.seichiAssistConfig().getTitle1(5108)
                    + SeichiAssist.seichiAssistConfig().getTitle3(5108) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 20日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(3, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5108「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 20日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(3, itemstack);
        }
        if (playerdata.TitleFlags().contains(5109)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5109「" + SeichiAssist.seichiAssistConfig().getTitle1(5109)
                    + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 10日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(2, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5109「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 10日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(2, itemstack);
        }
        if (playerdata.TitleFlags().contains(5110)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5110「" + SeichiAssist.seichiAssistConfig().getTitle1(5110) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 5日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(1, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5110「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 5日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(1, itemstack);
        }
        if (playerdata.TitleFlags().contains(5111)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5111「" + SeichiAssist.seichiAssistConfig().getTitle1(5111) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 2日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(0, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5111「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：通算ログイン日数が 2日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(0, itemstack);
        }

        // 1ページ目を開く
        itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        skullmeta = ItemMetaFactory.SKULL.getValue();
        itemstack.setDurability((short) 3);
        skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "「ログイン」カテゴリメニューへ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
        skullmeta.setLore(lore);
        skullmeta.setOwner("MHF_ArrowLeft");
        itemstack.setItemMeta(skullmeta);
        AsyncInventorySetter.setItemAsync(inventory, 27, itemstack.clone());

        return inventory;
    }

    //実績「連続ログイン」
    public static Inventory getTitleJoinChainData(Player p) {
        //プレイヤーを取得
        Player player = p.getPlayer();
        //UUID取得
        UUID uuid = player.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
            Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            return null;
        }

        Inventory inventory = Bukkit.getServer().createInventory(null, 4 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「連続ログイン」");
        ItemStack itemstack;
        ItemMeta itemmeta;
        SkullMeta skullmeta;
        List<String> lore;


        //実績設定・解禁ボタン
        if (playerdata.TitleFlags().contains(5001)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5001「" + SeichiAssist.seichiAssistConfig().getTitle1(5001)
                    + SeichiAssist.seichiAssistConfig().getTitle2(5001) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：連続ログイン日数が 100日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(0, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5001「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：連続ログイン日数が 100日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(0, itemstack);
        }
        if (playerdata.TitleFlags().contains(5002)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5002「" + SeichiAssist.seichiAssistConfig().getTitle1(5002)
                    + SeichiAssist.seichiAssistConfig().getTitle3(5002) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：連続ログイン日数が 50日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(1, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5002「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：連続ログイン日数が 50日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(1, itemstack);
        }
        if (playerdata.TitleFlags().contains(5003)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5003「" + SeichiAssist.seichiAssistConfig().getTitle1(5003) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：連続ログイン日数が 30日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(2, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5003「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：連続ログイン日数が 30日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(2, itemstack);
        }
        if (playerdata.TitleFlags().contains(5004)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5004「" + SeichiAssist.seichiAssistConfig().getTitle1(5004)
                    + SeichiAssist.seichiAssistConfig().getTitle3(5004) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：連続ログイン日数が 20日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(3, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5004「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：連続ログイン日数が 20日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(3, itemstack);
        }
        if (playerdata.TitleFlags().contains(5005)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5005「" + SeichiAssist.seichiAssistConfig().getTitle1(5005)
                    + SeichiAssist.seichiAssistConfig().getTitle3(5005) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：連続ログイン日数が 10日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(4, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5005「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：連続ログイン日数が 10日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(4, itemstack);
        }
        if (playerdata.TitleFlags().contains(5006)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5006「" + SeichiAssist.seichiAssistConfig().getTitle1(5006)
                    + SeichiAssist.seichiAssistConfig().getTitle3(5006) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：連続ログイン日数が 5日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(5, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5006「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：連続ログイン日数が 5日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(5, itemstack);
        }
        if (playerdata.TitleFlags().contains(5007)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5007「" + SeichiAssist.seichiAssistConfig().getTitle1(5007) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：連続ログイン日数が 3日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(6, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5007「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：連続ログイン日数が 3日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(6, itemstack);
        }
        if (playerdata.TitleFlags().contains(5008)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5008「" + SeichiAssist.seichiAssistConfig().getTitle1(5008)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9905) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：連続ログイン日数が 2日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(7, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5008「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：連続ログイン日数が 2日 に到達"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(7, itemstack);
        }

        // 1ページ目を開く
        itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        skullmeta = ItemMetaFactory.SKULL.getValue();
        itemstack.setDurability((short) 3);
        skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "「ログイン」カテゴリメニューへ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
        skullmeta.setLore(lore);
        skullmeta.setOwner("MHF_ArrowLeft");
        itemstack.setItemMeta(skullmeta);
        AsyncInventorySetter.setItemAsync(inventory, 27, itemstack.clone());

        return inventory;
    }


    //実績「JMS投票数」
    public static Inventory getTitleSupportData(Player p) {
        //プレイヤーを取得
        Player player = p.getPlayer();
        //UUID取得
        UUID uuid = player.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
            Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            return null;
        }

        Inventory inventory = Bukkit.getServer().createInventory(null, 4 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「JMS投票数」");
        ItemStack itemstack;
        ItemMeta itemmeta;
        SkullMeta skullmeta;
        List<String> lore;


        //実績設定・解禁ボタン
        //投票数365
        if (playerdata.TitleFlags().contains(6001)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6001「" + SeichiAssist.seichiAssistConfig().getTitle1(6001) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：JMS投票数が365を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(0, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6001「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：JMS投票数が365を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(0, itemstack);
        }
        //投票数200
        if (playerdata.TitleFlags().contains(6002)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6002「" + SeichiAssist.seichiAssistConfig().getTitle1(6002)
                    + SeichiAssist.seichiAssistConfig().getTitle3(6002) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：JMS投票数が200を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(1, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6002「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：JMS投票数が200を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(1, itemstack);
        }
        //投票数100
        if (playerdata.TitleFlags().contains(6003)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6003「" + SeichiAssist.seichiAssistConfig().getTitle1(6003) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：JMS投票数が100を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(2, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6003「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：JMS投票数が100を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(2, itemstack);
        }
        //投票数50
        if (playerdata.TitleFlags().contains(6004)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6004「" + SeichiAssist.seichiAssistConfig().getTitle1(6004)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9903) + SeichiAssist.seichiAssistConfig().getTitle3(6004) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：JMS投票数が50を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(3, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6004「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：JMS投票数が50を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(3, itemstack);
        }
        //投票数25
        if (playerdata.TitleFlags().contains(6005)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6005「" + SeichiAssist.seichiAssistConfig().getTitle1(6005)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9905) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：JMS投票数が25を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(4, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6005「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：JMS投票数が25を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(4, itemstack);
        }
        //投票数10
        if (playerdata.TitleFlags().contains(6006)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6006「" + SeichiAssist.seichiAssistConfig().getTitle1(6006)
                    + SeichiAssist.seichiAssistConfig().getTitle3(6006) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：JMS投票数が10を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(5, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6006「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：JMS投票数が10を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(5, itemstack);
        }
        //投票数5
        if (playerdata.TitleFlags().contains(6007)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6007「" + SeichiAssist.seichiAssistConfig().getTitle1(6007)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9902) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：JMS投票数が5を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(6, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6007「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：JMS投票数が5を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(6, itemstack);
        }
        //投票数1
        if (playerdata.TitleFlags().contains(6008)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6008「" + SeichiAssist.seichiAssistConfig().getTitle1(6008) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：JMS投票数が1を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(7, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6008「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：JMS投票数が1を超える"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(7, itemstack);
        }


        // 1ページ目を開く
        itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        skullmeta = ItemMetaFactory.SKULL.getValue();
        itemstack.setDurability((short) 3);
        skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "「特殊」カテゴリメニューへ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
        skullmeta.setLore(lore);
        skullmeta.setOwner("MHF_ArrowLeft");
        itemstack.setItemMeta(skullmeta);
        AsyncInventorySetter.setItemAsync(inventory, 27, itemstack.clone());

        return inventory;
    }

    //実績「公式イベント」
    public static Inventory getTitleEventData(Player p) {
        //プレイヤーを取得
        Player player = p.getPlayer();
        //UUID取得
        UUID uuid = player.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
            Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            return null;
        }

        Inventory inventory = Bukkit.getServer().createInventory(null, 4 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「公式イベント」");
        ItemStack itemstack;
        ItemMeta itemmeta;
        SkullMeta skullmeta;
        List<String> lore;


        //実績設定・解禁ボタン
        if (playerdata.TitleFlags().contains(7001)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7001「" + SeichiAssist.seichiAssistConfig().getTitle1(7001)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9901) + SeichiAssist.seichiAssistConfig().getTitle3(7001) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「整地大会」に参加する"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(0, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7001「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「整地大会」に参加する"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(0, itemstack);
        }
        if (playerdata.TitleFlags().contains(7002)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7002「" + SeichiAssist.seichiAssistConfig().getTitle1(7002)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9905) + SeichiAssist.seichiAssistConfig().getTitle3(7002) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「整地大会」にて総合優勝"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(1, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7002「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「整地大会」にて総合優勝"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(1, itemstack);
        }
        if (playerdata.TitleFlags().contains(7003)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7003「" + SeichiAssist.seichiAssistConfig().getTitle1(7003)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9905) + SeichiAssist.seichiAssistConfig().getTitle3(7003) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「建築コンペ」で最優秀賞獲得"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(2, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7003「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「建築コンペ」で最優秀賞獲得"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(2, itemstack);
        }
        if (playerdata.TitleFlags().contains(7004)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7004「" + SeichiAssist.seichiAssistConfig().getTitle2(7004) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「建築コンペ」で優秀賞獲得"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(3, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7004「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「建築コンペ」で優秀賞獲得"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(3, itemstack);
        }
        if (playerdata.TitleFlags().contains(7005)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7005「" + SeichiAssist.seichiAssistConfig().getTitle1(7005)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9902) + SeichiAssist.seichiAssistConfig().getTitle3(7005) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「建築コンペ」で佳作賞獲得"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(4, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7005「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「建築コンペ」で佳作賞獲得"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(4, itemstack);
        }
        if (playerdata.TitleFlags().contains(7006)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7006「" + SeichiAssist.seichiAssistConfig().getTitle1(7006)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9905) + SeichiAssist.seichiAssistConfig().getTitle3(7006) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「第一回建築コンペ」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "開催テーマは「桜」でした。"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(5, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7006「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「第一回建築コンペ」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "開催テーマは「桜」でした。"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(5, itemstack);
        }
        if (playerdata.TitleFlags().contains(7007)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7007「" + SeichiAssist.seichiAssistConfig().getTitle1(7007)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9905) + SeichiAssist.seichiAssistConfig().getTitle3(7007) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「第二回建築コンペ」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "開催テーマは「アスレチック」でした。"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(6, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7007「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「第二回建築コンペ」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "開催テーマは「アスレチック」でした。"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(6, itemstack);
        }
        //以下の二つはデータだけ先に実装しています。(使いそうだけど使うか分からない)
        //一応このままの状態でも解禁コマンドは使用可能。
        if (playerdata.TitleFlags().contains(7008)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7008「" + SeichiAssist.seichiAssistConfig().getTitle1(7008)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9905) + SeichiAssist.seichiAssistConfig().getTitle3(7008) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「GTテクスチャコンペ」で採用"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(7, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7008「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「GTテクスチャコンペ」で採用"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(7, itemstack);
        }
        if (playerdata.TitleFlags().contains(7009)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7009「" + SeichiAssist.seichiAssistConfig().getTitle1(7009)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9905) + SeichiAssist.seichiAssistConfig().getTitle3(7009) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「第二回整地大会」で優勝"
                    , ChatColor.RESET + "" + ChatColor.RED + "整地鯖一周年記念イベントでした！"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(8, itemstack);
        }

        if (playerdata.TitleFlags().contains(7010)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7010「" + SeichiAssist.seichiAssistConfig().getTitle1(7010)
                    + SeichiAssist.seichiAssistConfig().getTitle3(7010) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「第三回建築コンペ」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "開催テーマＡは「氷像(夏)」でした。"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(9, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7010「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「第三回建築コンペ」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "開催テーマＡは「氷像(夏)」でした。"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(9, itemstack);
        }
        if (playerdata.TitleFlags().contains(7011)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7011「" + SeichiAssist.seichiAssistConfig().getTitle1(7011)
                    + SeichiAssist.seichiAssistConfig().getTitle3(9905) + SeichiAssist.seichiAssistConfig().getTitle3(7011) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「第三回建築コンペ」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "開催テーマＢは「海岸建築(夏)」でした。"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(10, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7011「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「第三回建築コンペ」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "開催テーマＢは「海岸建築(夏)」でした。"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(10, itemstack);
        }
        if (playerdata.TitleFlags().contains(7012)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7012「" + SeichiAssist.seichiAssistConfig().getTitle1(7012)
                    + SeichiAssist.seichiAssistConfig().getTitle3(7012) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「第三回建築コンペ」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "開催テーマＣは「海上建築(夏)」でした。"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(11, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7012「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「第三回建築コンペ」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "開催テーマＣは「海上建築(夏)」でした。"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(11, itemstack);
        }
        if (playerdata.TitleFlags().contains(7013)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7013「" + SeichiAssist.seichiAssistConfig().getTitle1(7013) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「第四回建築コンペ」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "開催テーマＡは「和風建築」でした。"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(12, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7013「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「第四回建築コンペ」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "開催テーマＡは「和風建築」でした。"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(12, itemstack);
        }
        if (playerdata.TitleFlags().contains(7014)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7014「" + SeichiAssist.seichiAssistConfig().getTitle1(7014) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「第四回建築コンペ」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "開催テーマＢは「洋風建築」でした。"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(13, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7014「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「第四回建築コンペ」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "開催テーマＢは「洋風建築」でした。"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(13, itemstack);
        }
        if (playerdata.TitleFlags().contains(7015)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7015「" + SeichiAssist.seichiAssistConfig().getTitle1(7015)
                    + SeichiAssist.seichiAssistConfig().getTitle3(9904) + SeichiAssist.seichiAssistConfig().getTitle3(7015) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「第四回建築コンペ」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "開催テーマＣは「モダン建築」でした。"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(14, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7015「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「第四回建築コンペ」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "開催テーマＣは「モダン建築」でした。"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(14, itemstack);
        }
        if (playerdata.TitleFlags().contains(7016)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7016「" + SeichiAssist.seichiAssistConfig().getTitle1(7016)
                    + SeichiAssist.seichiAssistConfig().getTitle3(7016) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「第四回建築コンペ」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "開催テーマＤは「ファンタジー」でした。"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(15, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7016「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「第四回建築コンペ」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "開催テーマＤは「ファンタジー」でした。"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(15, itemstack);
        }
        if (playerdata.TitleFlags().contains(7017)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7017「" + SeichiAssist.seichiAssistConfig().getTitle1(7017)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9905) + SeichiAssist.seichiAssistConfig().getTitle3(7017) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "条件：グランプリ獲得"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(16, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7017「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "条件：グランプリ獲得"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(16, itemstack);
        }
        if (playerdata.TitleFlags().contains(7018)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7018「" + SeichiAssist.seichiAssistConfig().getTitle1(7018)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9904) + SeichiAssist.seichiAssistConfig().getTitle3(7018) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "条件：参加賞"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(17, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7018「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "条件：参加賞"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(17, itemstack);
        }
        if (playerdata.TitleFlags().contains(7019)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7019「" + SeichiAssist.seichiAssistConfig().getTitle1(7019)
                    + SeichiAssist.seichiAssistConfig().getTitle3(7019) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "条件：審査員賞(Yukki_XD)"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(18, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7019「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "条件：審査員賞(Yukki_XD)"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(18, itemstack);
        }
        if (playerdata.TitleFlags().contains(7020)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7020「" + SeichiAssist.seichiAssistConfig().getTitle1(7020)
                    + SeichiAssist.seichiAssistConfig().getTitle3(7020) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "条件：審査員賞(rukure2017)"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(19, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7020「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "条件：審査員賞(rukure2017)"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(19, itemstack);
        }
        if (playerdata.TitleFlags().contains(7021)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7021「" + SeichiAssist.seichiAssistConfig().getTitle1(7021)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9905) + SeichiAssist.seichiAssistConfig().getTitle3(7021) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "条件：審査員賞(Lucky3028)"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(20, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7021「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "条件：審査員賞(Lucky3028)"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(20, itemstack);
        }
        if (playerdata.TitleFlags().contains(7022)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7022「" + SeichiAssist.seichiAssistConfig().getTitle1(7022)
                    + SeichiAssist.seichiAssistConfig().getTitle3(7022) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "条件：審査員賞(tokuzi_)"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(21, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7022「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "条件：審査員賞(tokuzi_)"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(21, itemstack);
        }
        if (playerdata.TitleFlags().contains(7023)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7023「" + SeichiAssist.seichiAssistConfig().getTitle1(7023)
                    + SeichiAssist.seichiAssistConfig().getTitle3(9905) + SeichiAssist.seichiAssistConfig().getTitle3(7023) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "条件：審査員賞(kamikami46)"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(22, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7023「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "条件：審査員賞(kamikami46)"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(22, itemstack);
        }
        if (playerdata.TitleFlags().contains(7024)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7024「" + SeichiAssist.seichiAssistConfig().getTitle1(7024)
                    + SeichiAssist.seichiAssistConfig().getTitle3(7024) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "条件：審査員賞(CourageousLeaf)"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(23, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7024「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "条件：審査員賞(CourageousLeaf)"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(23, itemstack);
        }
        if (playerdata.TitleFlags().contains(7025)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7025「" + SeichiAssist.seichiAssistConfig().getTitle1(7025)
                    + SeichiAssist.seichiAssistConfig().getTitle3(9905) + SeichiAssist.seichiAssistConfig().getTitle3(7025) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "条件：審査員賞(nubasu)"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(24, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7025「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "条件：審査員賞(nubasu)"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(24, itemstack);
        }
        if (playerdata.TitleFlags().contains(7026)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7026「" + SeichiAssist.seichiAssistConfig().getTitle1(7026)
                    + SeichiAssist.seichiAssistConfig().getTitle3(9905) + SeichiAssist.seichiAssistConfig().getTitle3(7026) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "条件：審査員賞(tetsusan)"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(25, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7026「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "条件：審査員賞(tetsusan)"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(25, itemstack);
        }
        if (playerdata.TitleFlags().contains(7027)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7027「" + SeichiAssist.seichiAssistConfig().getTitle1(7027)
                    + SeichiAssist.seichiAssistConfig().getTitle3(7027) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "条件：審査員賞(tar0ss)"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(26, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7027「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "条件：審査員賞(tar0ss)"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(26, itemstack);
        }
        if (playerdata.TitleFlags().contains(7901)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7901「" + SeichiAssist.seichiAssistConfig().getTitle1(7901)
                    + SeichiAssist.seichiAssistConfig().getTitle2(7901) + SeichiAssist.seichiAssistConfig().getTitle3(7901) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：超会議2018で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(30, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7901「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：超会議2018で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(30, itemstack);
        }
        if (playerdata.TitleFlags().contains(7902)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7902「" + SeichiAssist.seichiAssistConfig().getTitle1(7902)
                    + SeichiAssist.seichiAssistConfig().getTitle3(7902) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：超会議2018で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(31, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7902「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：超会議2018で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(31, itemstack);
        }
        if (playerdata.TitleFlags().contains(7903)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7903「" + SeichiAssist.seichiAssistConfig().getTitle1(7903)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9905) + SeichiAssist.seichiAssistConfig().getTitle3(7903) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：超会議2018で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(32, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7903「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：超会議2018で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(32, itemstack);
        }
        if (playerdata.TitleFlags().contains(7904)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7904「" + SeichiAssist.seichiAssistConfig().getTitle1(7904)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9907) + SeichiAssist.seichiAssistConfig().getTitle3(7904) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：超会議2018で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(33, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7904「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：超会議2018で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(33, itemstack);
        }
        if (playerdata.TitleFlags().contains(7905)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7905「" + SeichiAssist.seichiAssistConfig().getTitle1(7905)
                    + SeichiAssist.seichiAssistConfig().getTitle3(7905) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：超会議2018で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(34, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7905「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：超会議2018で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(34, itemstack);
        }
        if (playerdata.TitleFlags().contains(7906)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7906「" + SeichiAssist.seichiAssistConfig().getTitle1(7906)
                    + SeichiAssist.seichiAssistConfig().getTitle3(7906) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：超会議2018で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(35, itemstack);
        } else {
            itemstack = new ItemStack(Material.BEDROCK, 1);
            itemmeta = ItemMetaFactory.BEDROCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7906「???」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：超会議2018で配布"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は配布解禁式です");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(35, itemstack);
        }

        // 1ページ目を開く
        itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        skullmeta = ItemMetaFactory.SKULL.getValue();
        itemstack.setDurability((short) 3);
        skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "「特殊」カテゴリメニューへ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
        skullmeta.setLore(lore);
        skullmeta.setOwner("MHF_ArrowLeft");
        itemstack.setItemMeta(skullmeta);
        AsyncInventorySetter.setItemAsync(inventory, 27, itemstack.clone());

        return inventory;
    }


    //実績「特殊」
    public static Inventory getTitleExtraData(Player p) {
        //プレイヤーを取得
        Player player = p.getPlayer();
        //UUID取得
        UUID uuid = player.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
            Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            return null;
        }

        Inventory inventory = Bukkit.getServer().createInventory(null, 4 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「記念日」");
        ItemStack itemstack;
        ItemMeta itemmeta;
        SkullMeta skullmeta;
        List<String> lore;


        //実績設定・解禁ボタン
        if (playerdata.titlepage() == 1) {
            if (playerdata.TitleFlags().contains(9001)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9001「" + SeichiAssist.seichiAssistConfig().getTitle1(9001) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とある始まりの日にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(0, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9001「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とある始まりの日にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(0, itemstack);
            }
            if (playerdata.TitleFlags().contains(9002)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9002「" + SeichiAssist.seichiAssistConfig().getTitle1(9002)
                        + SeichiAssist.seichiAssistConfig().getTitle3(9002) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とある聖夜の日にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(1, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9002「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とある聖夜の日にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(1, itemstack);
            }
            if (playerdata.TitleFlags().contains(9003)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9003「" + SeichiAssist.seichiAssistConfig().getTitle1(9003) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とある終わりの日にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(2, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9003「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とある終わりの日にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(2, itemstack);
            }
            if (playerdata.TitleFlags().contains(9004)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9004「" + SeichiAssist.seichiAssistConfig().getTitle1(9004)
                        + SeichiAssist.seichiAssistConfig().getTitle2(9004) + SeichiAssist.seichiAssistConfig().getTitle3(9004) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：1月にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(3, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9004「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：1月にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(3, itemstack);
            }
            if (playerdata.TitleFlags().contains(9005)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9005「" + SeichiAssist.seichiAssistConfig().getTitle1(9005)
                        + SeichiAssist.seichiAssistConfig().getTitle3(9005) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：2月にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(4, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9005「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：2月にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(4, itemstack);
            }
            if (playerdata.TitleFlags().contains(9006)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9006「" + SeichiAssist.seichiAssistConfig().getTitle1(9006) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とあるお豆の絨毯爆撃の日にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(5, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9006「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とあるお豆の絨毯爆撃の日にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(5, itemstack);
            }
            if (playerdata.TitleFlags().contains(9007)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9007「" + SeichiAssist.seichiAssistConfig().getTitle1(9007) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：建国記念日にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(6, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9007「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：建国記念日にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(6, itemstack);
            }
            if (playerdata.TitleFlags().contains(9008)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9008「" + SeichiAssist.seichiAssistConfig().getTitle1(9008)
                        + SeichiAssist.seichiAssistConfig().getTitle3(9008) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とあるカカオまみれの日にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(7, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9008「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とあるカカオまみれの日にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(7, itemstack);
            }
            if (playerdata.TitleFlags().contains(9009)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9009「" + SeichiAssist.seichiAssistConfig().getTitle1(9009) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：3月にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(8, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9009「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：3月にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(8, itemstack);
            }
            if (playerdata.TitleFlags().contains(9010)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9010「" + SeichiAssist.seichiAssistConfig().getTitle1(9010)
                        + SeichiAssist.seichiAssistConfig().getTitle2(9903) + SeichiAssist.seichiAssistConfig().getTitle3(9010) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とある女の子の日にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(9, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9010「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とある女の子の日にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(9, itemstack);
            }
            if (playerdata.TitleFlags().contains(9011)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9011「" + SeichiAssist.seichiAssistConfig().getTitle1(9011)
                        + SeichiAssist.seichiAssistConfig().getTitle3(9011) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：燃え尽きたカカオだらけの日にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(10, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9011「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：燃え尽きたカカオだらけの日にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(10, itemstack);
            }
            if (playerdata.TitleFlags().contains(9012)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9012「" + SeichiAssist.seichiAssistConfig().getTitle1(9012)
                        + SeichiAssist.seichiAssistConfig().getTitle3(9012) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：春分の日にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(11, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9012「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：春分の日にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(11, itemstack);
            }
            if (playerdata.TitleFlags().contains(9013)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9013「" + SeichiAssist.seichiAssistConfig().getTitle1(9013) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：4月にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(12, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9013「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：4月にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(12, itemstack);
            }
            if (playerdata.TitleFlags().contains(9014)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9014「" + SeichiAssist.seichiAssistConfig().getTitle2(9014) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とある嘘の日にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(13, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9014「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とある嘘の日にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(13, itemstack);
            }
            if (playerdata.TitleFlags().contains(9015)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9015「" + SeichiAssist.seichiAssistConfig().getTitle1(9015)
                        + SeichiAssist.seichiAssistConfig().getTitle3(9015) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とある良い子の日にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(14, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9015「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とある良い子の日にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(14, itemstack);
            }
            if (playerdata.TitleFlags().contains(9016)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9016「" + SeichiAssist.seichiAssistConfig().getTitle1(9016)
                        + SeichiAssist.seichiAssistConfig().getTitle2(9016) + SeichiAssist.seichiAssistConfig().getTitle3(9016) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とある掃除デーにプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(15, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9016「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とある掃除デーにプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(15, itemstack);
            }
            if (playerdata.TitleFlags().contains(9017)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9017「" + SeichiAssist.seichiAssistConfig().getTitle1(9017)
                        + SeichiAssist.seichiAssistConfig().getTitle3(9017) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：5月にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(16, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9017「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：5月にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(16, itemstack);
            }
            if (playerdata.TitleFlags().contains(9018)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9018「" + SeichiAssist.seichiAssistConfig().getTitle1(9018) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とある子供の日にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(17, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9018「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とある子供の日にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(17, itemstack);
            }
            if (playerdata.TitleFlags().contains(9019)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9019「" + SeichiAssist.seichiAssistConfig().getTitle1(9019)
                        + SeichiAssist.seichiAssistConfig().getTitle2(9901) + SeichiAssist.seichiAssistConfig().getTitle3(9019) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：端午の節句にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(18, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9019「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：端午の節句にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(18, itemstack);
            }
            if (playerdata.TitleFlags().contains(9020)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9020「" + SeichiAssist.seichiAssistConfig().getTitle1(9020)
                        + SeichiAssist.seichiAssistConfig().getTitle3(9020) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：母の日にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(19, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9020「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：母の日にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(19, itemstack);
            }
            if (playerdata.TitleFlags().contains(9021)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9021「" + SeichiAssist.seichiAssistConfig().getTitle1(9021)
                        + SeichiAssist.seichiAssistConfig().getTitle2(9901) + SeichiAssist.seichiAssistConfig().getTitle3(9021) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：6月にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(20, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9021「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：6月にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(20, itemstack);
            }
            if (playerdata.TitleFlags().contains(9022)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9022「" + SeichiAssist.seichiAssistConfig().getTitle1(9022)
                        + SeichiAssist.seichiAssistConfig().getTitle3(9022) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とある日記の日にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(21, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9022「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とある日記の日にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(21, itemstack);
            }
            if (playerdata.TitleFlags().contains(9023)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9023「" + SeichiAssist.seichiAssistConfig().getTitle1(9023)
                        + SeichiAssist.seichiAssistConfig().getTitle3(9023) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：父の日にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(22, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9023「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：父の日にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(22, itemstack);
            }
            if (playerdata.TitleFlags().contains(9024)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9024「" + SeichiAssist.seichiAssistConfig().getTitle1(9024)
                        + SeichiAssist.seichiAssistConfig().getTitle3(9024) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とある生誕の日にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(23, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9024「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とある生誕の日にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(23, itemstack);
            }
            if (playerdata.TitleFlags().contains(9025)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9025「" + SeichiAssist.seichiAssistConfig().getTitle1(9025)
                        + SeichiAssist.seichiAssistConfig().getTitle2(9025) + SeichiAssist.seichiAssistConfig().getTitle3(9025) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：7月にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(24, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9025「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：7月にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(24, itemstack);
            }
            if (playerdata.TitleFlags().contains(9026)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9026「" + SeichiAssist.seichiAssistConfig().getTitle1(9026)
                        + SeichiAssist.seichiAssistConfig().getTitle3(9026) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：七夕にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(25, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9026「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：七夕にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(25, itemstack);
            }
            if (playerdata.TitleFlags().contains(9027)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9027「" + SeichiAssist.seichiAssistConfig().getTitle1(9027)
                        + SeichiAssist.seichiAssistConfig().getTitle3(9027) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とある東京の日にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(26, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9027「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とある東京の日にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(26, itemstack);
            }
            if (playerdata.TitleFlags().contains(9028)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9028「" + SeichiAssist.seichiAssistConfig().getTitle1(9028)
                        + SeichiAssist.seichiAssistConfig().getTitle2(9028) + SeichiAssist.seichiAssistConfig().getTitle3(9028) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とある肉の日にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(29, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9028「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とある肉の日にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(29, itemstack);
            }
            if (playerdata.TitleFlags().contains(9029)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9029「" + SeichiAssist.seichiAssistConfig().getTitle1(9029)
                        + SeichiAssist.seichiAssistConfig().getTitle2(9029) + SeichiAssist.seichiAssistConfig().getTitle3(9029) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：8月にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(30, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9029「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：8月にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(30, itemstack);
            }
            if (playerdata.TitleFlags().contains(9030)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9030「" + SeichiAssist.seichiAssistConfig().getTitle1(9030)
                        + SeichiAssist.seichiAssistConfig().getTitle2(9905) + SeichiAssist.seichiAssistConfig().getTitle3(9030) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とあるバナナの日にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(31, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9030「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とあるバナナの日にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(31, itemstack);
            }
            if (playerdata.TitleFlags().contains(9031)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9031「" + SeichiAssist.seichiAssistConfig().getTitle1(9031)
                        + SeichiAssist.seichiAssistConfig().getTitle2(9908) + SeichiAssist.seichiAssistConfig().getTitle3(9031) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とあるJDの日にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(32, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9031「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とあるJDの日にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(32, itemstack);
            }
            if (playerdata.TitleFlags().contains(9032)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9032「" + SeichiAssist.seichiAssistConfig().getTitle1(9032)
                        + SeichiAssist.seichiAssistConfig().getTitle3(9032) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とある焼肉の日にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(33, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9032「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とある焼肉の日にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(33, itemstack);
            }
        } else if (playerdata.titlepage() == 2) {
            if (playerdata.TitleFlags().contains(9033)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9033「" + SeichiAssist.seichiAssistConfig().getTitle1(9033)
                        + SeichiAssist.seichiAssistConfig().getTitle2(9903) + SeichiAssist.seichiAssistConfig().getTitle3(9033) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：9月にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(0, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9033「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：9月にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(0, itemstack);
            }
            if (playerdata.TitleFlags().contains(9034)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9034「" + SeichiAssist.seichiAssistConfig().getTitle1(9034)
                        + SeichiAssist.seichiAssistConfig().getTitle3(9034) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とあるくじの日にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(1, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9034「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とあるくじの日にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(1, itemstack);
            }
            if (playerdata.TitleFlags().contains(9035)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9035「" + SeichiAssist.seichiAssistConfig().getTitle1(9035)
                        + SeichiAssist.seichiAssistConfig().getTitle2(9905) + SeichiAssist.seichiAssistConfig().getTitle3(9035) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とあるマラソンの日にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(2, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9035「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とあるマラソンの日にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(2, itemstack);
            }
            if (playerdata.TitleFlags().contains(9036)) {
                itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
                itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9036「" + SeichiAssist.seichiAssistConfig().getTitle1(9036)
                        + SeichiAssist.seichiAssistConfig().getTitle3(9036) + "」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とあるふぐの日にプレイ");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(3, itemstack);
            } else {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9036「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：とあるふぐの日にプレイ"
                        , ChatColor.RESET + "" + ChatColor.GREEN + "※クリックで実績に挑戦できます");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(3, itemstack);
            }
        }


        // 1ページ目を開く
        itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        skullmeta = ItemMetaFactory.SKULL.getValue();
        itemstack.setDurability((short) 3);
        skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "「ログイン」カテゴリメニューへ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
        skullmeta.setLore(lore);
        skullmeta.setOwner("MHF_ArrowLeft");
        itemstack.setItemMeta(skullmeta);
        AsyncInventorySetter.setItemAsync(inventory, 27, itemstack.clone());

        //次ページへのボタンを配置
        itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        skullmeta = ItemMetaFactory.SKULL.getValue();
        itemstack.setDurability((short) 3);
        skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "次ページへ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
        skullmeta.setLore(lore);
        skullmeta.setOwner("MHF_ArrowRight");
        itemstack.setItemMeta(skullmeta);
        AsyncInventorySetter.setItemAsync(inventory, 35, itemstack.clone());


        return inventory;
    }

    //実績「極秘任務」(隠し実績群)
    public static Inventory getTitleSecretData(Player p) {
        //プレイヤーを取得
        Player player = p.getPlayer();
        //UUID取得
        UUID uuid = player.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
            Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            return null;
        }

        Inventory inventory = Bukkit.getServer().createInventory(null, 4 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「極秘任務」");
        ItemStack itemstack;
        ItemMeta itemmeta;
        SkullMeta skullmeta;
        List<String> lore;


        //実績設定・解禁ボタン
        if (playerdata.TitleFlags().contains(8001)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No8001「" + SeichiAssist.seichiAssistConfig().getTitle1(8001)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9905) + SeichiAssist.seichiAssistConfig().getTitle3(8001) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：器を満たす奇跡の少女"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                    , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(0, itemstack);
        } else {
            if (Calendar.getInstance().get(Calendar.SECOND) == 0 &&
                    Calendar.getInstance().get(Calendar.MINUTE) == 0) {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No8001「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：器を満たす奇跡の少女"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(0, itemstack);
            }
        }
        if (playerdata.TitleFlags().contains(8002)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No8002「" + SeichiAssist.seichiAssistConfig().getTitle1(8002)
                    + SeichiAssist.seichiAssistConfig().getTitle2(9905) + SeichiAssist.seichiAssistConfig().getTitle3(8002) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：[[[[[[LuckyNumber]]]]]]"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                    , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(1, itemstack);
        } else {
            if (playerdata.totalbreaknum() % 1000000 == 0 && !(playerdata.totalbreaknum() == 0)) {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No8002「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：[[[[[[LuckyNumber]]]]]]"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は自動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(1, itemstack);
            }
        }
        if (playerdata.TitleFlags().contains(8003)) {
            itemstack = new ItemStack(Material.DIAMOND_BLOCK, 1);
            itemmeta = ItemMetaFactory.DIAMOND_BLOCK.getValue();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No8003「" + SeichiAssist.seichiAssistConfig().getTitle1(8003)
                    + SeichiAssist.seichiAssistConfig().getTitle3(8003) + "」");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：定時分働いたら記録を確認！"
                    , ChatColor.RESET + "" + ChatColor.RED + "※この実績は手動解禁式です。"
                    , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(2, itemstack);
        } else {
            if (playerdata.playTick() % 72000 >= 0 && playerdata.playTick() % 72000 <= 1199) {
                itemstack = new ItemStack(Material.BEDROCK, 1);
                itemmeta = ItemMetaFactory.BEDROCK.getValue();
                itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No8003「???」");
                lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "条件：定時分働いたら記録を確認！"
                        , ChatColor.RESET + "" + ChatColor.RED + "※この実績は手動解禁式です。"
                        , ChatColor.RESET + "" + ChatColor.AQUA + "こちらは【隠し実績】となります");
                itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                inventory.setItem(2, itemstack);
            }
        }


        // 1ページ目を開く
        itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        skullmeta = ItemMetaFactory.SKULL.getValue();
        itemstack.setDurability((short) 3);
        skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "「特殊」カテゴリメニューへ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
        skullmeta.setLore(lore);
        skullmeta.setOwner("MHF_ArrowLeft");
        itemstack.setItemMeta(skullmeta);
        AsyncInventorySetter.setItemAsync(inventory, 27, itemstack.clone());

        return inventory;
    }

    public static Inventory getServerSwitchMenu(Player p) {
        //UUID取得
        UUID uuid = p.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (sendWarningToLogger(p, playerdata)) return null;
        Inventory inventory = Bukkit.getServer().createInventory(null, 2 * 9, ChatColor.DARK_RED + "" + ChatColor.BOLD + "サーバーを選択してください");
        ItemStack itemstack;
        ItemMeta itemmeta;
        SkullMeta skullmeta;

        itemstack = new ItemStack(Material.DIAMOND_PICKAXE);
        itemmeta = itemstack.getItemMeta();
        itemmeta.setDisplayName(ChatColor.BOLD + "" + ChatColor.YELLOW + "アルカディアサーバー");
        List<String> lore1 = new ArrayList<>();
        lore1.add(ChatColor.GRAY + "旧第一サバイバルサーバー");
        itemmeta.setLore(lore1);
        itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
        itemstack.setItemMeta(itemmeta);

        inventory.setItem(0, itemstack);

        itemstack = new ItemStack(Material.DIAMOND_SPADE);
        itemmeta = itemstack.getItemMeta();
        itemmeta.setDisplayName(ChatColor.BOLD + "" + ChatColor.YELLOW + "エデンサーバー");
        List<String> lore2 = new ArrayList<>();
        lore2.add(ChatColor.GRAY + "旧第二サバイバルサーバー");
        itemmeta.setLore(lore2);
        itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
        itemstack.setItemMeta(itemmeta);

        inventory.setItem(1, itemstack);

        itemstack = new ItemStack(Material.DIAMOND_AXE);
        itemmeta = itemstack.getItemMeta();
        itemmeta.setDisplayName(ChatColor.BOLD + "" + ChatColor.YELLOW + "ヴァルハラサーバー");
        List<String> lore3 = new ArrayList<>();
        lore3.add(ChatColor.GRAY + "旧第三サバイバルサーバー");
        itemmeta.setLore(lore3);
        itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
        itemstack.setItemMeta(itemmeta);

        inventory.setItem(2, itemstack);

        inventory.setItem(
                7,
                new IconItemStackBuilder(Material.BRICK, (short) 0)
                        .title(ChatColor.BOLD + ChatColor.GREEN.toString() + "建築サーバー")
                        .enchanted()
                        .build()
        );

        itemstack = new ItemStack(Material.DIAMOND);
        itemmeta = itemstack.getItemMeta();
        itemmeta.setDisplayName(ChatColor.BOLD + "" + ChatColor.GREEN + "公共施設サーバー");
        itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
        itemstack.setItemMeta(itemmeta);

        inventory.setItem(8, itemstack);

        return inventory;
    }

    private static boolean sendWarningToLogger(Player p, PlayerData playerdata) {
        if (playerdata == null) {
            Util.sendPlayerDataNullMessage(p);
            Bukkit.getLogger().warning(p.getName() + " -> PlayerData not found.");
            Bukkit.getLogger().warning("MenuInventoryData.menuData");
            return true;
        }
        return false;
    }

    //投票メニュー
    public static Inventory getVotingMenuData(Player p) {

        //UUID取得
        UUID uuid = p.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (sendWarningToLogger(p, playerdata)) return null;
        Inventory inventory = Bukkit.getServer().createInventory(null, 4 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "投票ptメニュー");
        ItemStack itemstack;
        ItemMeta itemmeta;
        SkullMeta skullmeta;
        List<String> lore;

        //投票pt受け取り
        itemstack = new ItemStack(Material.DIAMOND);
        itemmeta = itemstack.getItemMeta();
        itemmeta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "クリックで投票特典を受け取れます");
        itemmeta.setLore(VoteGetButtonLore(playerdata));
        itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(0, itemstack);

        // ver0.3.2 投票ページ表示
        itemstack = new ItemStack(Material.BOOK_AND_QUILL, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BOOK_AND_QUILL);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "投票ページにアクセス");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + "投票すると様々な特典が！"
                , ChatColor.RESET + "" + ChatColor.GREEN + "1日1回投票出来ます"
                , ChatColor.RESET + "" + ChatColor.DARK_GRAY + "クリックするとチャット欄に"
                , ChatColor.RESET + "" + ChatColor.DARK_GRAY + "URLが表示されますので"
                , ChatColor.RESET + "" + ChatColor.DARK_GRAY + "Tキーを押してから"
                , ChatColor.RESET + "" + ChatColor.DARK_GRAY + "そのURLをクリックしてください"
        );
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(9, itemstack);

        //棒メニューに戻る
        itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        skullmeta = ItemMetaFactory.SKULL.getValue();
        itemstack.setDurability((short) 3);
        skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
        skullmeta.setLore(lore);
        skullmeta.setOwner("MHF_ArrowLeft");
        itemstack.setItemMeta(skullmeta);
        AsyncInventorySetter.setItemAsync(inventory, 27, itemstack.clone());

        //妖精召喚時間設定トグルボタン
        itemstack = new ItemStack(Material.WATCH);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.WATCH);
        itemmeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "マナ妖精 時間設定");
        lore = Arrays.asList(
                ChatColor.RESET + "" + ChatColor.GREEN + "" + ChatColor.BOLD + "" + VotingFairyTask.dispToggleVFTime(playerdata.toggleVotingFairy()),
                "",
                ChatColor.RESET + "" + ChatColor.GRAY + "コスト",
                ChatColor.RESET + "" + ChatColor.RED + "" + ChatColor.BOLD + "" + (playerdata.toggleVotingFairy() * 2) + "投票pt",
                "",
                ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで切替"
        );
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(2, itemstack);

        //妖精契約設定トグル
        itemstack = new ItemStack(Material.PAPER);
        itemstack.setItemMeta(VFPromiseMeta(playerdata));
        inventory.setItem(11, itemstack);

        //妖精音トグル
        itemstack = new ItemStack(Material.JUKEBOX);
        itemstack.setItemMeta(VFSoundToggleMeta(playerdata.toggleVFSound()));
        inventory.setItem(20, itemstack);


        //妖精召喚
        itemstack = new ItemStack(Material.GHAST_TEAR);
        itemmeta = itemstack.getItemMeta();
        itemmeta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "マナ妖精 召喚");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "" + (playerdata.toggleVotingFairy() * 2) + "投票ptを消費して"
                , ChatColor.RESET + "" + ChatColor.GRAY + "マナ妖精を呼びます"
                , ChatColor.RESET + "" + ChatColor.GRAY + "時間 : " + VotingFairyTask.dispToggleVFTime(playerdata.toggleVotingFairy())
                , ChatColor.RESET + "" + ChatColor.DARK_RED + "Lv.10以上で解放");
        itemmeta.setLore(lore);
        itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(4, itemstack);

        if (playerdata.usingVotingFairy()) {
            //妖精 時間確認
            itemstack = new ItemStack(Material.COMPASS);
            itemmeta = itemstack.getItemMeta();
            itemmeta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "マナ妖精に時間を聞く");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "妖精さんはいそがしい。", ChatColor.GRAY + "帰っちゃう時間を教えてくれる");
            itemmeta.setLore(lore);
            itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(13, itemstack);

            int prank = playerdata.calcPlayerApple();

            itemstack = new ItemStack(Material.GOLDEN_APPLE);
            itemmeta = itemstack.getItemMeta();
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "㊙ がちゃりんご情報 ㊙");
            List<String> lores = new ArrayList<>();
            lores.addAll(Arrays.asList(
                    ChatColor.RESET + "" + ChatColor.RED + "" + ChatColor.BOLD + "※ﾆﾝｹﾞﾝに見られないように気を付けること！"
                    , ChatColor.RESET + "" + ChatColor.RED + "" + ChatColor.BOLD + "  毎日大妖精からデータを更新すること！"
                    , ""

                    , ChatColor.RESET + "" + ChatColor.GOLD + "" + ChatColor.BOLD + "昨日までにがちゃりんごを"
                    , ChatColor.RESET + "" + ChatColor.GOLD + "" + ChatColor.BOLD + "たくさんくれたﾆﾝｹﾞﾝたち"
                    , ChatColor.RESET + "" + ChatColor.DARK_GRAY + "召喚されたらラッキーだよ！"
            ));
            RankData rankdata;
            for (int count = 0; count < 4; count++) {
                if (count >= SeichiAssist.ranklist_p_apple().size()) {
                    break;
                }
                rankdata = SeichiAssist.ranklist_p_apple().apply(count);
                if (rankdata.p_apple < 1) { //数0
                    break;
                }
                lores.add(ChatColor.GRAY + "たくさんくれたﾆﾝｹﾞﾝ第" + (count + 1) + "位！");
                lores.add(ChatColor.GRAY + "なまえ：" + rankdata.name + " りんご：" + rankdata.p_apple + "個");
            }

            lores.add(ChatColor.AQUA + "ぜーんぶで" + SeichiAssist.allplayergiveapplelong() + "個もらえた！");
            lores.add("");
            lores.add(ChatColor.GREEN + "↓呼び出したﾆﾝｹﾞﾝの情報↓");
            lores.add(ChatColor.GREEN + "今までに" + playerdata.p_apple() + "個もらった");
            lores.add(ChatColor.GREEN + "ﾆﾝｹﾞﾝの中では" + prank + "番目にたくさんくれる！");

            itemmeta.setLore(lores);
            itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(6, itemstack);
        }


        return inventory;

    }

    public static ItemMeta VFSoundToggleMeta(boolean bln) {
        ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.JUKEBOX);
        itemmeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "マナ妖精の音トグル");
        if (bln) {
            itemmeta.setLore(Arrays.asList(
                    ChatColor.RESET + "" + ChatColor.GREEN + "現在音が鳴る設定になっています。"
                    , ChatColor.RESET + "" + ChatColor.DARK_GRAY + "※この機能はデフォルトでONです。"
                    , ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで切替"
            ));
        } else {
            itemmeta.setLore(Arrays.asList(
                    ChatColor.RESET + "" + ChatColor.RED + "現在音が鳴らない設定になっています。"
                    , ChatColor.RESET + "" + ChatColor.DARK_GRAY + "※この機能はデフォルトでONです。"
                    , ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで切替"
            ));
            itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
        }


        return itemmeta;
    }

    public static ItemMeta VFPromiseMeta(PlayerData playerdata) {

        ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.PAPER);
        itemmeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "妖精とのお約束");

        if (playerdata.toggleGiveApple() == 1) {
            itemmeta.setLore(Arrays.asList(
                    ChatColor.RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ガンガンたべるぞ"
                    , ChatColor.RESET + "" + ChatColor.GRAY + "とにかく妖精さんにりんごを開放します。"
                    , ChatColor.RESET + "" + ChatColor.GRAY + "めっちゃ喜ばれます。"
            ));
        } else if (playerdata.toggleGiveApple() == 2) {
            itemmeta.setLore(Arrays.asList(
                    ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "バッチリたべよう"
                    , ChatColor.RESET + "" + ChatColor.GRAY + "食べ過ぎないように注意しつつ"
                    , ChatColor.RESET + "" + ChatColor.GRAY + "妖精さんにりんごを開放します。"
                    , ChatColor.RESET + "" + ChatColor.GRAY + "喜ばれます。"
            ));
        } else if (playerdata.toggleGiveApple() == 3) {
            itemmeta.setLore(Arrays.asList(
                    ChatColor.GREEN + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "リンゴだいじに"
                    , ChatColor.RESET + "" + ChatColor.GRAY + "少しだけ妖精さんにりんごを開放します。"
                    , ChatColor.RESET + "" + ChatColor.GRAY + "伝えると大抵落ち込みます。"
            ));
        } else if (playerdata.toggleGiveApple() == 4) {
            itemmeta.setLore(Arrays.asList(
                    ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "リンゴつかうな"
                    , ChatColor.RESET + "" + ChatColor.GRAY + "絶対にりんごを開放しません。"
                    , ChatColor.RESET + "" + ChatColor.GRAY + ""
            ));
        }
        return itemmeta;
    }

    public static Inventory getHomeMenuData(Player p) {
        //UUID取得
        UUID uuid = p.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (sendWarningToLogger(p, playerdata)) return null;
        Inventory inventory = Bukkit.getServer().createInventory(null, 3 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "ホームメニュー");
        ItemStack itemstack;
        ItemMeta itemmeta;
        List<String> lore;

        // ver0.3.2 homeコマンド
        itemstack = new ItemStack(Material.COMPASS, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COMPASS);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームポイントにワープ");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "あらかじめ設定した"
                , ChatColor.RESET + "" + ChatColor.GRAY + "ホームポイントにワープします"
                , ChatColor.RESET + "" + ChatColor.DARK_GRAY + "うまく機能しない時は"
                , ChatColor.RESET + "" + ChatColor.DARK_GRAY + "再接続してみてください"
                , ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでワープ"
                , ChatColor.RESET + "" + ChatColor.DARK_GRAY + "command->[/home]"
        );
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(0, itemstack);

        itemstack = new ItemStack(Material.BED, 1);
        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BED);
        itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームポイントを設定");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "現在位置をホームポイント"
                , ChatColor.RESET + "" + ChatColor.GRAY + "として設定します"
                , ChatColor.RESET + "" + ChatColor.DARK_GRAY + "※確認メニューが開きます"
                , ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで設定"
                , ChatColor.RESET + "" + ChatColor.DARK_GRAY + "command->[/sethome]"
        );
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(18, itemstack);

        for (int x = 0; x < SeichiAssist.seichiAssistConfig().getSubHomeMax(); x++) {
            //サブホームに移動ボタン
            itemstack = new ItemStack(Material.COMPASS, 1);
            itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COMPASS);
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "サブホームポイント" + (x + 1) + "にワープ");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "あらかじめ設定した"
                    , ChatColor.RESET + "" + ChatColor.GRAY + "サブホームポイント" + (x + 1) + "にワープします"
                    , ChatColor.RESET + "" + ChatColor.DARK_GRAY + "うまく機能しない時は"
                    , ChatColor.RESET + "" + ChatColor.DARK_GRAY + "再接続してみてください"
                    , ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでワープ"
                    , ChatColor.RESET + "" + ChatColor.DARK_GRAY + "command->[/subhome warp " + (x + 1) + "]"
            );
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(2 + x, itemstack);

            itemstack = new ItemStack(Material.PAPER);
            itemmeta = Bukkit.getItemFactory().getItemMeta(Material.PAPER);
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "サブホームポイント" + (x + 1) + "の情報");
            Location l = playerdata.getSubHomeLocation(x).getOrElse(() -> null);
            final List<String> subHomeLore;
            if (l != null) {
                final ManagedWorld world = ManagedWorld$.MODULE$.fromBukkitWorld(l.getWorld()).getOrElse(() -> null);
                final String worldName = world != null ? world.japaneseName() : l.getWorld().getName();

                subHomeLore = Arrays.asList(
                        ChatColor.RESET + "" + ChatColor.GRAY + "サブホームポイント" + (x + 1) + "は",
                        ChatColor.RESET + "" + ChatColor.GRAY + playerdata.getSubHomeName(x),
                        ChatColor.RESET + "" + ChatColor.GRAY + "と名付けられています",
                        ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで名称変更",
                        ChatColor.RESET + "" + ChatColor.DARK_GRAY + "command->[/subhome name " + (x + 1) + "]",
                        ChatColor.RESET + "" + ChatColor.GRAY + "" + worldName + " x:" + (int) l.getX() + " y:" + (int) l.getY() + " z:" + (int) l.getZ()
                );
            } else {
                subHomeLore = Arrays.asList(ChatColor.GRAY + "サブホームポイント" + (x + 1), ChatColor.GRAY + "ポイント未設定");
            }
            itemmeta.setLore(subHomeLore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(11 + x, itemstack);

            //サブホーム設定ボタン
            itemstack = new ItemStack(Material.BED, 1);
            itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BED);
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "サブホームポイント" + (x + 1) + "を設定");
            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "現在位置をサブホームポイント" + (x + 1)
                    , ChatColor.RESET + "" + ChatColor.GRAY + "として設定します"
                    , ChatColor.RESET + "" + ChatColor.DARK_GRAY + "※確認メニューが開きます"
                    , ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで設定"
                    , ChatColor.RESET + "" + ChatColor.DARK_GRAY + "command->[/subhome set " + (x + 1) + "]"
            );
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(20 + x, itemstack);
        }

        return inventory;
    }

    public static Inventory getCheckSetHomeMenuData(Player p) {
        //UUID取得
        UUID uuid = p.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        int n = playerdata.selectHomeNum();
        //念のためエラー分岐
        if (sendWarningToLogger(p, playerdata)) return null;
        Inventory inventory = Bukkit.getServer().createInventory(null, 3 * 9, ChatColor.RED + "" + ChatColor.BOLD + "ホームポイントを変更しますか?");
        ItemStack itemstack;
        ItemMeta itemmeta;
        List<String> lore;

        if (n >= 1) {
            itemstack = new ItemStack(Material.PAPER);
            itemmeta = itemstack.getItemMeta();
            itemmeta.setDisplayName(ChatColor.GREEN + "設定するサブホームポイントの情報");
            lore = Arrays.asList(
                    ChatColor.RESET + "" + ChatColor.GRAY + "No." + n,
                    ChatColor.RESET + "" + ChatColor.GRAY + "名称：" + playerdata.getSubHomeName(n - 1)
            );
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            inventory.setItem(4, itemstack);
        }

        itemstack = new ItemStack(Material.WOOL, 1, (byte) 5);
        itemmeta = itemstack.getItemMeta();
        itemmeta.setDisplayName(ChatColor.GREEN + "変更する");
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(11, itemstack);

        itemstack = new ItemStack(Material.WOOL, 1, (byte) 14);
        itemmeta = itemstack.getItemMeta();
        itemmeta.setDisplayName(ChatColor.RED + "変更しない");
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(15, itemstack);

        return inventory;
    }

    public static Inventory getGiganticBerserkEvolutionMenu(Player p) {
        //UUID取得
        UUID uuid = p.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (sendWarningToLogger(p, playerdata)) return null;
        Inventory inventory = Bukkit.getServer().createInventory(null, 6 * 9, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "スキルを進化させますか?");
        ItemStack itemstack;
        ItemMeta itemmeta;
        List<String> lore;

        // stage
        {
            final byte b;
            switch (playerdata.giganticBerserk().stage()) {
                case 0:
                    b = 12;
                    break;
                case 1:
                    b = 15;
                    break;
                case 2:
                    b = 4;
                    break;
                case 3:
                    b = 0;
                    break;
                default:
                    b = 12;
                    break;
            }
            itemstack = new ItemStack(Material.STAINED_GLASS_PANE, 1, b);
        }

        itemmeta = itemstack.getItemMeta();
        itemmeta.setDisplayName(" ");
        itemstack.setItemMeta(itemmeta);

        inventory.setItem(6, itemstack);
        inventory.setItem(7, itemstack);
        inventory.setItem(14, itemstack);
        inventory.setItem(15, itemstack);
        inventory.setItem(16, itemstack);
        inventory.setItem(21, itemstack);
        inventory.setItem(22, itemstack);
        inventory.setItem(23, itemstack);
        inventory.setItem(24, itemstack);
        inventory.setItem(32, itemstack);
        inventory.setItem(41, itemstack);

        itemstack = new ItemStack(Material.STICK, 1);
        itemmeta = itemstack.getItemMeta();
        itemmeta.setDisplayName(" ");
        itemstack.setItemMeta(itemmeta);

        inventory.setItem(30, itemstack);
        inventory.setItem(39, itemstack);
        inventory.setItem(40, itemstack);
        inventory.setItem(47, itemstack);


        itemstack = new ItemStack(Material.NETHER_STAR, 1);
        itemmeta = itemstack.getItemMeta();
        itemmeta.setDisplayName(ChatColor.WHITE + "スキルを進化させる");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + "進化することにより、スキルの秘めたる力を解放できますが"
                , ChatColor.RESET + "" + ChatColor.GREEN + "スキルは更に大量の魂を求めるようになり"
                , ChatColor.RESET + "" + ChatColor.GREEN + "レベル(回復確率)がリセットされます"
                , ChatColor.RESET + "" + ChatColor.RED + "本当に進化させますか?"
                , ChatColor.RESET + "" + ChatColor.DARK_RED + ChatColor.UNDERLINE + "クリックで進化させる"
        );
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(31, itemstack);

        return inventory;
    }

    public static Inventory getGiganticBerserkEvolution2Menu(Player p) {
        //UUID取得
        UUID uuid = p.getUniqueId();
        //プレイヤーデータ
        PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (sendWarningToLogger(p, playerdata)) return null;
        Inventory inventory = Bukkit.getServer().createInventory(null, 6 * 9, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "スキルを進化させました");
        ItemStack itemstack;
        ItemMeta itemmeta;
        List<String> lore;

        {
            final byte b;
            // stage
            switch (playerdata.giganticBerserk().stage()) {
                case 1:
                    b = 15;
                    break;
                case 2:
                    b = 4;
                    break;
                case 3:
                    b = 0;
                    break;
                case 4:
                    b = 3;
                    break;
                default:
                    b = 12;
                    break;
            }

            itemstack = new ItemStack(Material.STAINED_GLASS_PANE, 1, b);
        }

        itemmeta = itemstack.getItemMeta();
        itemmeta.setDisplayName(" ");
        itemstack.setItemMeta(itemmeta);

        inventory.setItem(6, itemstack);
        inventory.setItem(7, itemstack);
        inventory.setItem(14, itemstack);
        inventory.setItem(15, itemstack);
        inventory.setItem(16, itemstack);
        inventory.setItem(21, itemstack);
        inventory.setItem(22, itemstack);
        inventory.setItem(23, itemstack);
        inventory.setItem(24, itemstack);
        inventory.setItem(32, itemstack);
        inventory.setItem(41, itemstack);

        itemstack = new ItemStack(Material.STICK, 1);
        itemmeta = itemstack.getItemMeta();
        itemmeta.setDisplayName(" ");
        itemstack.setItemMeta(itemmeta);

        inventory.setItem(30, itemstack);
        inventory.setItem(39, itemstack);
        inventory.setItem(40, itemstack);
        inventory.setItem(47, itemstack);


        itemstack = new ItemStack(Material.NETHER_STAR, 1);
        itemmeta = itemstack.getItemMeta();
        itemmeta.setDisplayName(ChatColor.WHITE + "スキルを進化させました！");
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + "スキルの秘めたる力を解放することで、マナ回復量が増加し"
                , ChatColor.RESET + "" + ChatColor.DARK_RED + "スキルはより魂を求めるようになりました"
        );
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        inventory.setItem(31, itemstack);

        return inventory;
    }
}
