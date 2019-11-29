package com.github.unchama.seichiassist.data;

import com.github.unchama.itemstackbuilder.IconItemStackBuilder;
import com.github.unchama.seichiassist.*;
import com.github.unchama.seichiassist.achievement.NicknameParts;
import com.github.unchama.seichiassist.achievement.Nicknames;
import com.github.unchama.seichiassist.data.player.PlayerData;
import com.github.unchama.seichiassist.data.player.PlayerNickname;
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
import scala.Function0;
import scala.Option;
import scala.collection.mutable.HashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
        PlayerNickname nickname = playerdata.settings().nickname();
        String playerTitle = Nicknames.getTitleFor(nickname.id1(), nickname.id2(), nickname.id3());
        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "「" + playerTitle + "」");
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
                    Option<String> maybeHeadPart = Nicknames.getHeadPartFor(checkTitle1);
                    if (maybeHeadPart.nonEmpty()) {
                        itemstack = new ItemStack(Material.WATER_BUCKET, 1);
                        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.WATER_BUCKET);
                        itemmeta.setDisplayName(String.valueOf(checkTitle1));
                        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "前パーツ「" + maybeHeadPart.get() + "」");
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
                Option<String> maybeMiddlePart = Nicknames.getMiddlePartFor(checkTitle2);
                //一部の「隠し中パーツ」は取得しているかの確認
                if (9911 <= checkTitle2  /*&& checkTitle2 <= 9927*/) {
                    if (playerdata.TitleFlags().contains(checkTitle2)) {
                        if (maybeMiddlePart.nonEmpty()) {
                            itemstack = new ItemStack(Material.MILK_BUCKET, 1);
                            itemmeta = Bukkit.getItemFactory().getItemMeta(Material.MILK_BUCKET);
                            itemmeta.setDisplayName(String.valueOf(checkTitle2));
                            lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "中パーツ「" + maybeMiddlePart.get() + "」");
                            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                            itemmeta.setLore(lore);
                            itemstack.setItemMeta(itemmeta);
                            inventory.setItem(checkInv, itemstack);

                            checkInv++;
                        }
                    }
                } else if (Nicknames.getMiddlePartFor(checkTitle2).nonEmpty()) {
                    itemstack = new ItemStack(Material.MILK_BUCKET, 1);
                    itemmeta = Bukkit.getItemFactory().getItemMeta(Material.MILK_BUCKET);
                    itemmeta.setDisplayName(String.valueOf(checkTitle2));
                    lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "中パーツ「" + maybeMiddlePart.get() + "」");
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
                    Option<String> maybeTailPart = Nicknames.getTailPartFor(checkTitle3);
                    if (maybeTailPart.nonEmpty()) {
                        itemstack = new ItemStack(Material.LAVA_BUCKET, 1);
                        itemmeta = Bukkit.getItemFactory().getItemMeta(Material.LAVA_BUCKET);
                        itemmeta.setDisplayName(String.valueOf(checkTitle3));
                        lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "後パーツ「" + maybeTailPart.get() + "」");
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
                    lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "前・後パーツ「" + Nicknames.getHeadPartFor(checkTitleS).getOrElse(() -> "") + "」"
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
                    lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "中パーツ「" + Nicknames.getMiddlePartFor(checkTitleS).getOrElse(() -> "") + "」"
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
