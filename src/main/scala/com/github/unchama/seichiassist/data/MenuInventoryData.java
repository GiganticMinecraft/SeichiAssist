package com.github.unchama.seichiassist.data;

import com.github.unchama.seichiassist.LevelThresholds;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.achievement.Nicknames;
import com.github.unchama.seichiassist.data.player.AchievementPoint;
import com.github.unchama.seichiassist.data.player.PlayerData;
import com.github.unchama.seichiassist.data.player.PlayerNickname;
import com.github.unchama.seichiassist.database.DatabaseGateway;
import com.github.unchama.seichiassist.activeskill.effect.ActiveSkillNormalEffect;
import com.github.unchama.seichiassist.activeskill.effect.ActiveSkillPremiumEffect;
import com.github.unchama.seichiassist.task.VotingFairyTask;
import com.github.unchama.seichiassist.util.AsyncInventorySetter;
import com.github.unchama.seichiassist.util.ItemMetaFactory;
import com.github.unchama.seichiassist.util.TypeConverter;
import com.github.unchama.seichiassist.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import scala.Option;
import scala.collection.mutable.HashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class MenuInventoryData {
    private MenuInventoryData() {
    }

    private static final HashMap<UUID, PlayerData> playermap = SeichiAssist.playermap();
    private static final DatabaseGateway databaseGateway = SeichiAssist.databaseGateway();

    //二つ名組合せシステム用
    /*
    * FIXME:
    *  実装がやばいので複数のプレイヤーが同時に開いたらぶっ壊れる。
    *  しかしながら、今日に至るまでバグが報告されたことは１度もない。まじでミラクル。
    */
    private static boolean pageFlag1;
    private static boolean pageFlag2;
    private static boolean negatedPageFlag3 = true;
    private static boolean negatedPageFlagS = true;
    private static int checkTitle1;
    private static int checkTitle2;
    private static int checkTitle3;
    private static int checkTitleS;
    private static int NoKeep;
    /**
     * (short) 3はダサいし、マジックコンスタントみたいだよね。
     */
    private static final short PLAYER_SKULL = 3;

    //投票特典受け取りボタン
    private static List<String> getVoteButtonLore(final PlayerData playerdata) {
        return Arrays.asList(
                ChatColor.RESET + "" + ChatColor.GRAY + "投票特典を受け取るには",
                ChatColor.RESET + "" + ChatColor.GRAY + "投票ページで投票した後",
                ChatColor.RESET + "" + ChatColor.GRAY + "このボタンをクリックします",
                ChatColor.RESET + "" + ChatColor.AQUA + "特典受取済投票回数：" + playerdata.p_givenvote(),
                ChatColor.RESET + "" + ChatColor.AQUA + "所有投票pt：" + playerdata.activeskilldata().effectpoint
        );
    }

    //ランキングリスト

    /**
     * 整地量
     * @param page ページ
     * @return メニュー
     */
    public static Inventory getRankingBySeichiAmount(final int page) {
        final int pageLimit = 14;
        final int lowerBound = 100;
        final Inventory inventory = getEmptyInventory(6, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "整地神ランキング");
        final ItemStack itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        itemstack.setDurability(PLAYER_SKULL);
        for (int count = 10 * page, count2 = 0; count < 10 + 10 * page; count++, count2++) {
            if (count >= SeichiAssist.ranklist().size()) {
                break;
            }

            final RankData rankdata = SeichiAssist.ranklist().apply(count);
            if (rankdata.totalbreaknum < (Long) LevelThresholds.levelExpThresholds().apply(lowerBound - 1)) { //レベル100相当の総整地量判定に変更
                break;
            }

            final List<String> lore = Arrays.asList(
                    ChatColor.RESET + "" + ChatColor.GREEN + "整地レベル:" + rankdata.level,
                    ChatColor.RESET + "" + ChatColor.GREEN + "総整地量:" + rankdata.totalbreaknum
            );

            final SkullMeta skullmeta = build(
                    ChatColor.YELLOW + "" + ChatColor.BOLD + "" + (count + 1) + "位:" + "" + ChatColor.WHITE + rankdata.name,
                    lore,
                    rankdata.name
            );
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, count2, itemstack.clone());
        }

        if (page != pageLimit) {
            // 整地神ランキング次ページ目を開く
            final SkullMeta skullMeta = build(
                    ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地神ランキング" + (page + 2) + "ページ目へ",
                    Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"), "MHF_ArrowDown"
            );
            itemstack.setItemMeta(skullMeta);
            AsyncInventorySetter.setItemAsync(inventory, 52, itemstack.clone());
        }

        // 1ページ目を開く
        {
            final String name;
            final List<String> lore;
            final String ign;
            if (page == 0) {
                name = ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ";
                lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
                ign = "MHF_ArrowLeft";
            } else {
                // 整地神ランキング前ページ目を開く;
                name = ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地神ランキング" + page + "ページ目へ";
                lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
                ign = "MHF_ArrowUp";
            }
            final SkullMeta skullmeta = build(name, lore, ign);
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, 45, itemstack.clone());
        }


        // 総整地量の表記
        {
            final List<String> lore = Arrays.asList(
                    ChatColor.RESET + "" + ChatColor.AQUA + "全プレイヤー総整地量:",
                    ChatColor.RESET + "" + ChatColor.AQUA + SeichiAssist.allplayerbreakblockint()
            );

            final SkullMeta skullmeta = build(
                    ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地鯖統計データ",
                    lore,
                    "unchama"
            );
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, 53, itemstack.clone());
        }

        return inventory;
    }

    /**
     * ログイン時間
     * @param page ページ
     * @return メニュー
     */
    public static Inventory getRankingByPlayingTime(final int page) {
        final int pageLimit = 14;
        final Inventory inventory = getEmptyInventory(6, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "ログイン神ランキング");
        final ItemStack itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        itemstack.setDurability(PLAYER_SKULL);
        final int rankStart = 10 * page;
        for (int rank = rankStart, inventoryIndex = 0; rank < rankStart + 10; rank++, inventoryIndex++) {
            if (rank >= SeichiAssist.ranklist_playtick().size()) {
                break;
            }
            final RankData rankdata = SeichiAssist.ranklist_playtick().apply(rank);

            final SkullMeta skullmeta = build(
                ChatColor.YELLOW + "" + ChatColor.BOLD + "" + (rank + 1) + "位:" + "" + ChatColor.WHITE + rankdata.name,
                Collections.singletonList(ChatColor.RESET + "" + ChatColor.GREEN + "総ログイン時間:" + TypeConverter.toTimeString(TypeConverter.toSecond(rankdata.playtick))),
                rankdata.name
            );
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, inventoryIndex, itemstack.clone());
        }

        if (page != pageLimit) {
            // 整地神ランキング次ページ目を開く
            final SkullMeta skullmeta = build(
                ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ログイン神ランキング" + (page + 2) + "ページ目へ",
                Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"),
                "MHF_ArrowDown"
            );
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, 52, itemstack.clone());
        }

        // 1ページ目を開く
        {
            final SkullMeta skullmeta;
            if (page == 0) {
                skullmeta = build(
                        ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ",
                        Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"),
                        "MHF_ArrowLeft"
                );
            } else {
                // 整地神ランキング前ページ目を開く;
                skullmeta = build(
                        ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ログイン神ランキング" + page + "ページ目へ",
                        Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"),
                        "MHF_ArrowUp"
                );
            }
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, 45, itemstack.clone());
        }

        return inventory;
    }

    /**
     * 投票回数
     * @param page ページ
     * @return メニュー
     */
    public static Inventory getRankingByVotingCount(final int page) {
        final int pageLimit = 14;
        final int lowerBound = 1;
        final Inventory inventory = getEmptyInventory(6, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "投票神ランキング");
        final ItemStack itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        itemstack.setDurability(PLAYER_SKULL);
        RankData rankdata;
        for (int count = 10 * page, count2 = 0; count < 10 + 10 * page; count++, count2++) {
            if (count >= SeichiAssist.ranklist_p_vote().size()) {
                break;
            }
            
            rankdata = SeichiAssist.ranklist_p_vote().apply(count);
            if (rankdata.p_vote < lowerBound) { //投票数0
                break;
            }

            final SkullMeta skullmeta = build(
                    ChatColor.YELLOW + "" + ChatColor.BOLD + "" + (count + 1) + "位:" + "" + ChatColor.WHITE + rankdata.name,
                    Collections.singletonList(ChatColor.RESET + "" + ChatColor.GREEN + "総投票回数:" + rankdata.p_vote),
                    rankdata.name
            );
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, count2, itemstack.clone());
        }

        if (page != pageLimit) {
            // 投票神ランキング次ページ目を開く
            final SkullMeta skullmeta = build(
                ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "投票神ランキング" + (page + 2) + "ページ目へ",
                Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"),
                "MHF_ArrowDown"
            );
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, 52, itemstack.clone());
        }

        // 1ページ目を開く
        {
            final SkullMeta skullmeta;
            if (page == 0) {
                skullmeta = build(
                        ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ",
                        Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"),
                        "MHF_ArrowLeft"
                );
            } else {
                // 整地神ランキング前ページ目を開く;
                skullmeta = build(
                        ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "投票神ランキング" + page + "ページ目へ",
                        Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"),
                        "MHF_ArrowUp"
                );
            }
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, 45, itemstack.clone());
        }
        return inventory;
    }

    /**
     * プレミアムエフェクトポイント
     * @param page ページ
     * @return メニュー
     */
    public static Inventory getRankingByPremiumEffectPoint(final int page) {
        final int pageLimit = 2;
        final int lowerBound = 1;
        final Inventory inventory = getEmptyInventory(6, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "寄付神ランキング");
        final ItemStack itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        itemstack.setDurability(PLAYER_SKULL);
        RankData rankdata;
        for (int rank = 50 * page, inventoryIndex = 0; rank < 50 + 50 * page; rank++, inventoryIndex++) {
            if (rank >= SeichiAssist.ranklist_premiumeffectpoint().size()) {
                break;
            }
            if (inventoryIndex == 45) {
                inventoryIndex = 47;
            }
            rankdata = SeichiAssist.ranklist_premiumeffectpoint().apply(rank);
            if (rankdata.premiumeffectpoint < lowerBound) { //寄付金額0
                break;
            }
            final SkullMeta skullmeta = build(
        ChatColor.YELLOW + "" + ChatColor.BOLD + "" + (rank + 1) + "位:" + "" + ChatColor.WHITE + rankdata.name,
                Collections.singletonList(ChatColor.RESET + "" + ChatColor.GREEN + "総寄付金額:" + rankdata.premiumeffectpoint * 100),
                rankdata.name
            );
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, inventoryIndex, itemstack.clone());
        }

        if (page != pageLimit) {
            // 整地神ランキング次ページ目を開く
            final SkullMeta skullmeta = build(
                ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "寄付神ランキング" + (page + 2) + "ページ目へ",
                Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"),
                "MHF_ArrowDown"
            );
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, 52, itemstack.clone());
        }

        // 1ページ目を開く
        {
            final SkullMeta skullmeta;
            if (page == 0) {
                skullmeta = build(
                        ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ",
                        Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"),
                        "MHF_ArrowLeft"
                );
            } else {
                // 整地神ランキング前ページ目を開く;
                skullmeta = build(
                        ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "寄付神ランキング" + page + "ページ目へ",
                        Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"),
                        "MHF_ArrowUp"
                );
            }
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, 45, itemstack.clone());
        }


        return inventory;
    }

    /**
     * エフェクト選択
     * @param p プレイヤー
     * @return メニュー
     */
    public static Inventory getActiveSkillEffectMenuData(final Player p) {
        final Player player = p.getPlayer();
        final UUID uuid = player.getUniqueId();
        final PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
            Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            return null;
        }

        final Inventory inventory = getEmptyInventory(6, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "整地スキルエフェクト選択");

        // 1ページ目を開く
        {
            final ItemStack itemstack = new ItemStack(Material.SKULL_ITEM, 1);
            final SkullMeta skullmeta = ItemMetaFactory.SKULL.getValue();
            itemstack.setDurability(PLAYER_SKULL);
            skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "スキルメニューへ");
            final List<String> lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
            skullmeta.setLore(lore);
            skullmeta.setOwner("MHF_ArrowLeft");
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, 45, itemstack.clone());
        }
        //1行目
        {
            final ItemStack itemstack = new ItemStack(Material.SKULL_ITEM, 1);
            final SkullMeta skullmeta = ItemMetaFactory.SKULL.getValue();
            itemstack.setDurability(PLAYER_SKULL);
            skullmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
            skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + playerdata.lowercaseName() + "のスキルエフェクトデータ");
            final List<String> lore = Arrays.asList(
                    ChatColor.RESET + "" + ChatColor.GREEN + "現在選択しているエフェクト：" + ActiveSkillNormalEffect.getNameByNum(playerdata.activeskilldata().effectnum),
                    ChatColor.RESET + "" + ChatColor.YELLOW + "使えるエフェクトポイント：" + playerdata.activeskilldata().effectpoint,
                    ChatColor.RESET + "" + ChatColor.DARK_GRAY + "※投票すると獲得出来ます",
                    ChatColor.RESET + "" + ChatColor.LIGHT_PURPLE + "使えるプレミアムポイント：" + playerdata.activeskilldata().premiumeffectpoint,
                    ChatColor.RESET + "" + ChatColor.DARK_GRAY + "※寄付をすると獲得できます"
            );
            skullmeta.setLore(lore);
            // この操作は安全; メニューを開けているのにUUIDがないなんてことがないから
            skullmeta.setOwningPlayer(Bukkit.getOfflinePlayer(playerdata.uuid())); //.setOwner(playerdata.name);
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, 0, itemstack.clone());
        }

        {
            final ItemStack itemstack = new ItemStack(Material.BOOK_AND_QUILL, 1);
            final ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BOOK_AND_QUILL);
            itemmeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "プレミアムエフェクト購入履歴");
            final List<String> lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで閲覧");
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            AsyncInventorySetter.setItemAsync(inventory, 2,  itemstack);
        }
        {
            final ItemStack itemstack = new ItemStack(Material.GLASS, 1);
            final ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GLASS);
            itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "エフェクトを使用しない");
            final List<String> lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            AsyncInventorySetter.setItemAsync(inventory, 1,  itemstack);
        }
        {
            final ActiveSkillNormalEffect[] effects = ActiveSkillNormalEffect.arrayValues();
            int i = 0;
            for (final ActiveSkillNormalEffect elem :
                    effects) {
                final ItemStack itemstack;
                final ItemMeta itemmeta;
                final List<String> lore;
                //プレイヤーがそのスキルを取得している場合の処理
                if (playerdata.activeskilldata().obtainedSkillEffects.contains(elem)) {
                    itemstack = new ItemStack(elem.material(), 1);
                    itemmeta = Bukkit.getItemFactory().getItemMeta(elem.material());
                    itemmeta.setDisplayName(elem.nameOnUI());
                    lore = Arrays.asList(
                            ChatColor.RESET + "" + ChatColor.GREEN + elem.explanation(),
                            ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット"
                    );
                }
                //プレイヤーがそのスキルをまだ取得していない場合の処理
                else {
                    itemstack = new ItemStack(Material.BEDROCK, 1);
                    itemmeta = ItemMetaFactory.BEDROCK.getValue();
                    itemmeta.setDisplayName(elem.nameOnUI());
                    lore = Arrays.asList(
                            ChatColor.RESET + "" + ChatColor.GREEN + elem.explanation(),
                            ChatColor.RESET + "" + ChatColor.YELLOW + "必要エフェクトポイント：" + elem.usePoint(),
                            ChatColor.RESET + "" + ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除"
                    );
                }
                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                AsyncInventorySetter.setItemAsync(inventory, i + 9,  itemstack);
                i++;
            }
        }
        {
            final ActiveSkillPremiumEffect[] effects = ActiveSkillPremiumEffect.arrayValues();
            int i = 0;
            for (final ActiveSkillPremiumEffect effect :
                    effects) {
                final ItemStack itemstack;
                final ItemMeta itemmeta;
                final List<String> lore;
                if (playerdata.activeskilldata().obtainedSkillPremiumEffects.contains(effect)) {
                    itemstack = new ItemStack(effect.material(), 1);
                    itemmeta = Bukkit.getItemFactory().getItemMeta(effect.material());
                    itemmeta.setDisplayName(ChatColor.UNDERLINE + "" + ChatColor.BOLD + ChatColor.stripColor(effect.desc()));
                    lore = Arrays.asList(
                            ChatColor.RESET + "" + ChatColor.GREEN + effect.explain(),
                            ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット"
                    );
                } else {
                    //プレイヤーがそのスキルをまだ取得していない場合の処理
                    itemstack = new ItemStack(Material.BEDROCK, 1);
                    itemmeta = ItemMetaFactory.BEDROCK.getValue();
                    itemmeta.setDisplayName(effect.desc());
                    lore = Arrays.asList(
                            ChatColor.RESET + "" + ChatColor.GREEN + effect.explain(),
                            ChatColor.RESET + "" + ChatColor.YELLOW + "必要プレミアムポイント：" + effect.usePoint(),
                            ChatColor.RESET + "" + ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除"
                    );
                }

                itemmeta.setLore(lore);
                itemstack.setItemMeta(itemmeta);
                AsyncInventorySetter.setItemAsync(inventory, i + 27,  itemstack);
                i++;
            }
        }
        return inventory;
    }

    /**
     * プレミア購入履歴表示
     * @param player プレイヤー
     * @return メニュー
     */
    public static Inventory getBuyRecordMenuData(final Player player) {
        final PlayerData playerdata = playermap.apply(player.getUniqueId());
        final Inventory inventory = getEmptyInventory(4, ChatColor.BLUE + "" + ChatColor.BOLD + "プレミアムエフェクト購入履歴");

        // 1ページ目を開く
        final ItemStack itemstack = new ItemStack(Material.SKULL_ITEM, 1);
        itemstack.setDurability(PLAYER_SKULL);
        final List<String> lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
        final SkullMeta skullmeta = build(
                ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "エフェクト選択メニューへ",
                lore,
                "MHF_ArrowLeft"
        );
        itemstack.setItemMeta(skullmeta);
        AsyncInventorySetter.setItemAsync(inventory, 27, itemstack.clone());

        databaseGateway.donateDataManipulator.loadDonateData(playerdata, inventory);

        return inventory;
    }

    /**
     * 二つ名組み合わせ
     * @param p プレイヤー
     * @return メニュー
     */
    public static Inventory setFreeTitleMainData(final Player p) {
        final Player player = p.getPlayer();
        final UUID uuid = player.getUniqueId();
        final PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
            Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            return null;
        }

        final Inventory inventory = getEmptyInventory(4, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "二つ名組合せシステム");

        //各ボタンの設定
        pageFlag1 = false;
        pageFlag2 = false;
        negatedPageFlag3 = true;
        negatedPageFlagS = true;
        checkTitle1 = 0;
        checkTitle2 = 0;
        checkTitle3 = 0;
        checkTitleS = 0;
        NoKeep = 0;

        //実績ポイントの最新情報反映ボタン
        {
            final ItemStack itemstack = new ItemStack(Material.EMERALD_ORE, 1);
            final ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.EMERALD_ORE);
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績ポイント 情報");
            final List<String> lore = Arrays.asList(
                    ChatColor.RESET + "" + ChatColor.GREEN + "クリックで情報を最新化",
                    ChatColor.RESET + "" + ChatColor.RED + "累計獲得量：" + playerdata.achievePoint().cumulativeTotal(),
                    ChatColor.RESET + "" + ChatColor.RED + "累計消費量：" + playerdata.achievePoint().used(),
                    ChatColor.RESET + "" + ChatColor.AQUA + "使用可能量：" + playerdata.achievePoint().left()
            );
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            AsyncInventorySetter.setItemAsync(inventory, 0,  itemstack);
        }
        //パーツショップ
        {
            final ItemStack itemstack = new ItemStack(Material.ITEM_FRAME, 1);
            final ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.ITEM_FRAME);
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績ポイントショップ");
            final List<String> lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.GREEN + "クリックで開きます");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            AsyncInventorySetter.setItemAsync(inventory, 9,  itemstack);
        }
        //エフェクトポイントからの変換ボタン
        {
            final ItemStack itemstack = new ItemStack(Material.EMERALD, 1);
            final ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.EMERALD);
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ポイント変換ボタン");
            final List<String> lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "JMS投票で手に入るポイントを",
                    ChatColor.RESET + "" + ChatColor.RED + "実績ポイントに変換できます。",
                    ChatColor.RESET + "" + ChatColor.YELLOW + "" + ChatColor.BOLD + "投票pt 10pt → 実績pt 3pt",
                    ChatColor.RESET + "" + ChatColor.AQUA + "クリックで変換を一回行います。",
                    ChatColor.RESET + "" + ChatColor.GREEN + "所有投票pt :" + playerdata.activeskilldata().effectpoint,
                    ChatColor.RESET + "" + ChatColor.GREEN + "所有実績pt :" + playerdata.achievePoint().left()
            );
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            AsyncInventorySetter.setItemAsync(inventory, 1,  itemstack);
        }
        {
            final PlayerNickname nickname = playerdata.settings().nickname();
            final String playerTitle = Nicknames.getTitleFor(nickname.id1(), nickname.id2(), nickname.id3());
            final ItemStack itemStack = build(
                    Material.BOOK,
                    ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "現在の二つ名の確認",
                    ChatColor.RESET + "" + ChatColor.RED + "「" + playerTitle + "」"
            );
            AsyncInventorySetter.setItemAsync(inventory, 4,  itemStack);
        }
        AsyncInventorySetter.setItemAsync(inventory, 11,  build(
                Material.WATER_BUCKET,
                ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "前パーツ選択画面",
                ChatColor.RESET + "" + ChatColor.RED + "クリックで移動します"
        ));
        AsyncInventorySetter.setItemAsync(inventory, 13,  build(
                Material.MILK_BUCKET,
                ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "中パーツ選択画面",
                ChatColor.RESET + "" + ChatColor.RED + "クリックで移動します"
        ));
        AsyncInventorySetter.setItemAsync(inventory, 15,  build(
                Material.LAVA_BUCKET,
                ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "後パーツ選択画面",
                ChatColor.RESET + "" + ChatColor.RED + "クリックで移動します"
        ));

        // 1ページ目を開く
        {
            final ItemStack itemstack = new ItemStack(Material.SKULL_ITEM, 1);
            itemstack.setDurability(PLAYER_SKULL);
            final List<String> lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
            final SkullMeta skullmeta = build(
                    ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績・二つ名メニューへ",
                    lore,
                    "MHF_ArrowLeft"
            );
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, 27, itemstack.clone());
        }
        return inventory;
    }

    /**
     * 二つ名 - 前パーツ
     * @param p プレイヤー
     * @return メニュー
     */
    public static Inventory setFreeTitle1Data(final Player p) {
        //プレイヤーを取得
        final Player player = p.getPlayer();
        //UUID取得
        final UUID uuid = player.getUniqueId();
        //プレイヤーデータ
        final PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
            Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            return null;
        }

        final Inventory inventory = getEmptyInventory(4, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "二つ名組合せ「前」");

        if (pageFlag1) {
            pageFlag1 = false;
        } else {
            checkTitle1 = 1000;
        }

        //各ボタンの設定
        //解禁済みの実績をチェック→前パーツがあるかをチェック→あればボタン配置
        int checkInv = 0;
        for (; checkTitle1 < 9900; checkTitle1++) {
            if (checkInv < 27) {
                if (playerdata.TitleFlags().contains(checkTitle1)) {
                    final Option<String> maybeHeadPart = Nicknames.getHeadPartFor(checkTitle1);
                    if (maybeHeadPart.nonEmpty()) {
                        final ItemStack itemstack = new ItemStack(Material.WATER_BUCKET, 1);
                        final ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.WATER_BUCKET);
                        itemmeta.setDisplayName(String.valueOf(checkTitle1));
                        final List<String> lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.RED + "前パーツ「" + maybeHeadPart.get() + "」");
                        itemmeta.setLore(lore);
                        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        itemstack.setItemMeta(itemmeta);
                        AsyncInventorySetter.setItemAsync(inventory, checkInv,  itemstack);

                        checkInv++;
                    }
                }

            } else if (checkInv == 27) {
                //次ページへのボタンを配置
                final ItemStack itemstack = new ItemStack(Material.SKULL_ITEM, 1);
                final List<String> lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
                final SkullMeta skullmeta = build(
                        ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "次ページへ",
                        lore,
                        "MHF_ArrowRight"
                );
                itemstack.setDurability(PLAYER_SKULL);
                itemstack.setItemMeta(skullmeta);
                AsyncInventorySetter.setItemAsync(inventory, 35, itemstack.clone());

                pageFlag1 = true;

                break;
            }
        }

        //パーツ未選択状態にするボタン
        {
            final ItemStack itemstack = new ItemStack(Material.GRASS, 1);
            final ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GRASS);
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "前パーツを未選択状態にする");
            final List<String> lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで実行");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            AsyncInventorySetter.setItemAsync(inventory, 31,  itemstack);
        }
        // 二つ名組合せメインページを開く
        {
            final ItemStack itemstack = new ItemStack(Material.BARRIER, 1);
            final ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BARRIER);
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "二つ名組合せメインメニューへ");
            final List<String> lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            AsyncInventorySetter.setItemAsync(inventory, 27,  itemstack);
        }
        return inventory;
    }

    /**
     * 二つ名 - 中パーツ
     * @param p プレイヤー
     * @return メニュー
     */
    public static Inventory setFreeTitle2Data(final Player p) {
        final Player player = p.getPlayer();
        final UUID uuid = player.getUniqueId();
        final PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
            Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            return null;
        }

        final Inventory inventory = getEmptyInventory(4, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "二つ名組合せ「中」");
        ItemStack itemstack;
        ItemMeta itemmeta;

        if (pageFlag2) {
            pageFlag2 = false;
        } else {
            checkTitle2 = 9900;
        }

        //各ボタンの設定
        //パーツがあるかをチェック→あればボタン配置
        int checkInv = 0;
        for (; checkTitle2 < 9999; checkTitle2++) {
            if (checkInv < 27) {
                final Option<String> maybeMiddlePart = Nicknames.getMiddlePartFor(checkTitle2);
                //一部の「隠し中パーツ」は取得しているかの確認
                if (9911 <= checkTitle2  /*&& checkTitle2 <= 9927*/) {
                    if (playerdata.TitleFlags().contains(checkTitle2)) {
                        if (maybeMiddlePart.nonEmpty()) {
                            itemstack = new ItemStack(Material.MILK_BUCKET, 1);
                            itemmeta = Bukkit.getItemFactory().getItemMeta(Material.MILK_BUCKET);
                            itemmeta.setDisplayName(String.valueOf(checkTitle2));
                            final List<String> lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.RED + "中パーツ「" + maybeMiddlePart.get() + "」");
                            itemmeta.setLore(lore);
                            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                            itemstack.setItemMeta(itemmeta);
                            AsyncInventorySetter.setItemAsync(inventory, checkInv,  itemstack);

                            checkInv++;
                        }
                    }
                } else if (maybeMiddlePart.nonEmpty()) {
                    itemstack = new ItemStack(Material.MILK_BUCKET, 1);
                    itemmeta = Bukkit.getItemFactory().getItemMeta(Material.MILK_BUCKET);
                    itemmeta.setDisplayName(String.valueOf(checkTitle2));
                    final List<String> lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.RED + "中パーツ「" + maybeMiddlePart.get() + "」");
                    itemmeta.setLore(lore);
                    itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    itemstack.setItemMeta(itemmeta);
                    AsyncInventorySetter.setItemAsync(inventory, checkInv,  itemstack);

                    checkInv++;
                }


            } else if (checkInv == 27) {
                //次ページへのボタンを配置
                itemstack = new ItemStack(Material.SKULL_ITEM, 1);
                itemstack.setDurability(PLAYER_SKULL);
                final List<String> lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
                final SkullMeta skullmeta = build(
                        ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "次ページへ",
                        lore,
                        "MHF_ArrowRight"
                );
                itemstack.setItemMeta(skullmeta);
                AsyncInventorySetter.setItemAsync(inventory, 35, itemstack.clone());

                pageFlag2 = true;

                break;
            }
        }


        //パーツ未選択状態にするボタン
        {
            itemstack = new ItemStack(Material.GRASS, 1);
            itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GRASS);
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "中パーツを未選択状態にする");
            final List<String> lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで実行");
            itemmeta.setLore(lore);
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemstack.setItemMeta(itemmeta);
            AsyncInventorySetter.setItemAsync(inventory, 31,  itemstack);
        }
        // 二つ名組合せメインページを開く
        {
            itemstack = new ItemStack(Material.BARRIER, 1);
            itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BARRIER);
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "二つ名組合せメインメニューへ");
            final List<String> lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            AsyncInventorySetter.setItemAsync(inventory, 27,  itemstack);
        }
        return inventory;
    }

    /**
     * 二つ名 - 後パーツ
     * @param p プレイヤー
     * @return メニュー
     */
    public static Inventory setFreeTitle3Data(final Player p) {
        //プレイヤーを取得
        final Player player = p.getPlayer();
        //UUID取得
        final UUID uuid = player.getUniqueId();
        //プレイヤーデータ
        final PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
            Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            return null;
        }

        final Inventory inventory = getEmptyInventory(4, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "二つ名組合せ「後」");

        final SkullMeta skullmeta;

        if (negatedPageFlag3) {
            checkTitle3 = 1000;
        }

        //各ボタンの設定
        //解禁済みの実績をチェック→前パーツがあるかをチェック→あればボタン配置
        int checkInv = 0;
        for (; checkTitle3 < 9900; checkTitle3++) {
            if (checkInv < 27) {
                if (playerdata.TitleFlags().contains(checkTitle3)) {
                    final Option<String> maybeTailPart = Nicknames.getTailPartFor(checkTitle3);
                    if (maybeTailPart.nonEmpty()) {
                        final ItemStack itemstack = new ItemStack(Material.LAVA_BUCKET, 1);
                        final ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.LAVA_BUCKET);
                        itemmeta.setDisplayName(String.valueOf(checkTitle3));
                        final List<String> lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.RED + "後パーツ「" + maybeTailPart.get() + "」");
                        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        itemmeta.setLore(lore);
                        itemstack.setItemMeta(itemmeta);
                        AsyncInventorySetter.setItemAsync(inventory, checkInv,  itemstack);

                        checkInv++;
                    }

                }
            } else if (checkInv == 27) {
                //次ページへのボタンを配置
                final ItemStack itemstack = new ItemStack(Material.SKULL_ITEM, 1);
                skullmeta = ItemMetaFactory.SKULL.getValue();
                itemstack.setDurability(PLAYER_SKULL);
                skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "次ページへ");
                final List<String> lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
                skullmeta.setLore(lore);
                skullmeta.setOwner("MHF_ArrowRight");
                itemstack.setItemMeta(skullmeta);
                AsyncInventorySetter.setItemAsync(inventory, 35, itemstack.clone());

                negatedPageFlag3 = false;

                break;
            }

        }

        //パーツ未選択状態にするボタン
        {
            final ItemStack itemstack = new ItemStack(Material.GRASS, 1);
            final ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GRASS);
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "後パーツを未選択状態にする");
            final List<String> lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで実行");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            AsyncInventorySetter.setItemAsync(inventory, 31,  itemstack);
        }

        // 二つ名組合せメインページを開く
        {
            final ItemStack itemstack = new ItemStack(Material.BARRIER, 1);
            final ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BARRIER);
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "二つ名組合せメインメニューへ");
            final List<String> lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            AsyncInventorySetter.setItemAsync(inventory, 27,  itemstack);
        }
        return inventory;
    }

    /**
     * 実績ポイントショップ
     * @param p プレイヤー
     * @return メニュー
     */
    public static Inventory setTitleShopData(final Player p) {
        //プレイヤーを取得
        final Player player = p.getPlayer();
        //UUID取得
        final UUID uuid = player.getUniqueId();
        //プレイヤーデータ
        final PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (playerdata == null) {
            player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
            Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
            return null;
        }

        final Inventory inventory = getEmptyInventory(4, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績ポイントショップ");

        //実績ポイントの最新情報反映ボタン
        {
            final List<String> lore;
            final ItemStack itemstack = new ItemStack(Material.EMERALD_ORE, 1);
            final ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.EMERALD_ORE);
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績ポイント 情報");
            {
                final AchievementPoint ap = playerdata.achievePoint();
                lore = Arrays.asList(
                        ChatColor.RESET + "" + ChatColor.GREEN + "クリックで情報を最新化",
                        ChatColor.RESET + "" + ChatColor.RED + "累計獲得量：" + ap.cumulativeTotal(),
                        ChatColor.RESET + "" + ChatColor.RED + "累計消費量：" + ap.used(),
                        ChatColor.RESET + "" + ChatColor.AQUA + "使用可能量：" + ap.left()
                );
            }
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            AsyncInventorySetter.setItemAsync(inventory, 0,  itemstack);
        }
        //おしながき
        if (playerdata.samepageflag()) {
            checkTitleS = NoKeep;
        } else if (negatedPageFlagS) {
            checkTitleS = 9801;
        }
        NoKeep = checkTitleS;
        playerdata.samepageflag_$eq(false);
        int inventoryIndex = 1;
        for (; checkTitleS <= 9832; checkTitleS++) {
            final List<String> lore;
            final ItemStack itemstack;
            final ItemMeta itemmeta;
            if (inventoryIndex < 27) {
                if (!playerdata.TitleFlags().contains(checkTitleS)) {
                    itemstack = new ItemStack(Material.BEDROCK, 1);
                    itemmeta = ItemMetaFactory.BEDROCK.getValue();
                    itemmeta.setDisplayName(String.valueOf(checkTitleS));
                    lore = Arrays.asList(
                            ChatColor.RESET + "" + ChatColor.RED + "前・後パーツ「" + Nicknames.getHeadPartFor(checkTitleS).getOrElse(() -> "") + "」",
                            ChatColor.RESET + "" + ChatColor.GREEN + "必要ポイント：20",
                            ChatColor.RESET + "" + ChatColor.AQUA + "クリックで購入できます"
                    );
                    itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    itemmeta.setLore(lore);
                    itemstack.setItemMeta(itemmeta);
                    AsyncInventorySetter.setItemAsync(inventory, inventoryIndex,  itemstack);
                    inventoryIndex++;
                }
            } else {
                //次ページへのボタンを配置
                itemstack = new ItemStack(Material.SKULL_ITEM, 1);
                itemstack.setDurability(PLAYER_SKULL);
                final SkullMeta skullmeta = ItemMetaFactory.SKULL.getValue();
                skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "次ページへ");
                lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
                skullmeta.setLore(lore);
                skullmeta.setOwner("MHF_ArrowRight");
                itemstack.setItemMeta(skullmeta);
                AsyncInventorySetter.setItemAsync(inventory, 35, itemstack.clone());

                negatedPageFlagS = false;
                break;
            }
        }
        checkTitleS = Math.max(checkTitleS, 9911);
        for (; checkTitleS <= 9935; ) {
            if (inventoryIndex < 27) {
                final List<String> lore;
                final ItemStack itemstack;
                final ItemMeta itemmeta;
                if (!playerdata.TitleFlags().contains(checkTitleS)) {
                    itemstack = new ItemStack(Material.BEDROCK, 1);
                    itemmeta = ItemMetaFactory.BEDROCK.getValue();
                    itemmeta.setDisplayName(String.valueOf(checkTitleS));
                    lore = Arrays.asList(
                            ChatColor.RESET + "" + ChatColor.RED + "中パーツ「" + Nicknames.getMiddlePartFor(checkTitleS).getOrElse(() -> "") + "」",
                            ChatColor.RESET + "" + ChatColor.GREEN + "必要ポイント：35",
                            ChatColor.RESET + "" + ChatColor.AQUA + "クリックで購入できます"
                    );
                    itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    itemmeta.setLore(lore);
                    itemstack.setItemMeta(itemmeta);
                    AsyncInventorySetter.setItemAsync(inventory, inventoryIndex,  itemstack);

                    inventoryIndex++;
                }
            } else {
                //次ページへのボタンを配置
                final ItemStack itemstack = new ItemStack(Material.SKULL_ITEM, 1);
                final SkullMeta skullmeta = ItemMetaFactory.SKULL.getValue();
                itemstack.setDurability(PLAYER_SKULL);
                skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "次ページへ");
                final List<String> lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
                skullmeta.setLore(lore);
                skullmeta.setOwner("MHF_ArrowRight");
                itemstack.setItemMeta(skullmeta);
                AsyncInventorySetter.setItemAsync(inventory, 35, itemstack.clone());

                negatedPageFlagS = false;

                break;
            }
            checkTitleS++;
        }


        // 二つ名組合せメインページを開く
        {
            final ItemStack itemstack = new ItemStack(Material.BARRIER, 1);
            final ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BARRIER);
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "二つ名組合せメインメニューへ");
            final List<String> lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
            itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            AsyncInventorySetter.setItemAsync(inventory, 27,  itemstack);
        }
        return inventory;
    }

    private static boolean validate(final Player p, final PlayerData playerdata) {
        if (playerdata == null) {
            Util.sendPlayerDataNullMessage(p);
            Bukkit.getLogger().warning(p.getName() + " -> PlayerData not found.");
            Bukkit.getLogger().warning("MenuInventoryData.menuData");
            return true;
        }
        return false;
    }

    /**
     * 投票妖精
     * @param p プレイヤー
     * @return メニュー
     */
    public static Inventory getVotingMenuData(final Player p) {
        //UUID取得
        final UUID uuid = p.getUniqueId();
        //プレイヤーデータ
        final PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (validate(p, playerdata)) return null;
        final Inventory inventory = getEmptyInventory(4, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "投票ptメニュー");

        //投票pt受け取り
        {
            final ItemStack itemstack = new ItemStack(Material.DIAMOND);
            final ItemMeta itemmeta = itemstack.getItemMeta();
            itemmeta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "クリックで投票特典を受け取れます");
            itemmeta.setLore(getVoteButtonLore(playerdata));
            itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
            itemstack.setItemMeta(itemmeta);
            AsyncInventorySetter.setItemAsync(inventory, 0,  itemstack);
        }
        // ver0.3.2 投票ページ表示
        {
            final ItemStack itemstack = new ItemStack(Material.BOOK_AND_QUILL, 1);
            final ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BOOK_AND_QUILL);
            itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "投票ページにアクセス");
            final List<String> lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + "投票すると様々な特典が！",
                    ChatColor.RESET + "" + ChatColor.GREEN + "1日1回投票出来ます",
                    ChatColor.RESET + "" + ChatColor.DARK_GRAY + "クリックするとチャット欄に",
                    ChatColor.RESET + "" + ChatColor.DARK_GRAY + "URLが表示されますので",
                    ChatColor.RESET + "" + ChatColor.DARK_GRAY + "Tキーを押してから",
                    ChatColor.RESET + "" + ChatColor.DARK_GRAY + "そのURLをクリックしてください"
            );
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            AsyncInventorySetter.setItemAsync(inventory, 9,  itemstack);
        }

        //棒メニューに戻る
        {
            final ItemStack itemstack = new ItemStack(Material.SKULL_ITEM, 1);
            itemstack.setDurability(PLAYER_SKULL);
            final List<String> lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
            final SkullMeta skullmeta = build(
                    ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ",
                    lore,
                    "MHF_ArrowLeft"
            );
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, 27, itemstack.clone());
        }

        //妖精召喚時間設定トグルボタン
        {
            final ItemStack itemStack = new ItemStack(Material.WATCH);
            final ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.WATCH);
            meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "マナ妖精 時間設定");
            final List<String> list = Arrays.asList(
                    ChatColor.RESET + "" + ChatColor.GREEN + "" + ChatColor.BOLD + "" + VotingFairyTask.dispToggleVFTime(playerdata.toggleVotingFairy()),
                    "",
                    ChatColor.RESET + "" + ChatColor.GRAY + "コスト",
                    ChatColor.RESET + "" + ChatColor.RED + "" + ChatColor.BOLD + "" + playerdata.toggleVotingFairy() * 2 + "投票pt",
                    "",
                    ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで切替"
            );
            meta.setLore(list);
            itemStack.setItemMeta(meta);
            AsyncInventorySetter.setItemAsync(inventory, 2,  itemStack);
        }

        //妖精契約設定トグル
        {
            final ItemStack itemStack = new ItemStack(Material.PAPER);
            itemStack.setItemMeta(VFPromiseMeta(playerdata));
            AsyncInventorySetter.setItemAsync(inventory, 11,  itemStack);
        }
        //妖精音トグル
        {
            final ItemStack itemStack = new ItemStack(Material.JUKEBOX);
            itemStack.setItemMeta(getVotingFairySoundsToggleMeta(playerdata.toggleVFSound()));
            AsyncInventorySetter.setItemAsync(inventory, 20,  itemStack);
        }

        //妖精召喚
        {
            final ItemStack itemStack = new ItemStack(Material.GHAST_TEAR);
            final ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "マナ妖精 召喚");
            final List<String> list = Arrays.asList(
                    ChatColor.RESET + "" + ChatColor.GRAY + "" + playerdata.toggleVotingFairy() * 2 + "投票ptを消費して",
                    ChatColor.RESET + "" + ChatColor.GRAY + "マナ妖精を呼びます",
                    ChatColor.RESET + "" + ChatColor.GRAY + "時間 : " + VotingFairyTask.dispToggleVFTime(playerdata.toggleVotingFairy()),
                    ChatColor.RESET + "" + ChatColor.DARK_RED + "Lv.10以上で解放"
            );
            meta.setLore(list);
            meta.addEnchant(Enchantment.DIG_SPEED, 100, false);
            itemStack.setItemMeta(meta);
            AsyncInventorySetter.setItemAsync(inventory, 4,  itemStack);
        }


        if (playerdata.usingVotingFairy()) {
            //妖精 時間確認
            {
                final ItemStack itemStack = new ItemStack(Material.COMPASS);
                final ItemMeta meta = itemStack.getItemMeta();
                meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "マナ妖精に時間を聞く");
                final List<String> list = Arrays.asList(
                        ChatColor.RESET + "" + ChatColor.GRAY + "妖精さんはいそがしい。",
                        ChatColor.GRAY + "帰っちゃう時間を教えてくれる"
                );
                meta.setLore(list);
                meta.addEnchant(Enchantment.DIG_SPEED, 100, false);
                itemStack.setItemMeta(meta);
                AsyncInventorySetter.setItemAsync(inventory, 13,  itemStack);
            }

            {
                final int prank = playerdata.calcPlayerApple();
                final ItemStack itemStack = new ItemStack(Material.GOLDEN_APPLE);
                final ItemMeta meta = itemStack.getItemMeta();
                meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "㊙ がちゃりんご情報 ㊙");
                final List<String> lores = new ArrayList<>(6 + 4 * 2 + 5);

                // 6
                lores.addAll(Arrays.asList(
                        ChatColor.RESET + "" + ChatColor.RED + "" + ChatColor.BOLD + "※ﾆﾝｹﾞﾝに見られないように気を付けること！",
                        ChatColor.RESET + "" + ChatColor.RED + "" + ChatColor.BOLD + "  毎日大妖精からデータを更新すること！",
                        "",
                        ChatColor.RESET + "" + ChatColor.GOLD + "" + ChatColor.BOLD + "昨日までにがちゃりんごを",
                        ChatColor.RESET + "" + ChatColor.GOLD + "" + ChatColor.BOLD + "たくさんくれたﾆﾝｹﾞﾝたち",
                        ChatColor.RESET + "" + ChatColor.DARK_GRAY + "召喚されたらラッキーだよ！"
                ));
                for (int count = 0; count < 4; count++) {
                    if (count >= SeichiAssist.ranklist_p_apple().size()) {
                        break;
                    }
                    final RankData rankdata = SeichiAssist.ranklist_p_apple().apply(count);
                    if (rankdata.p_apple == 0) {
                        break;
                    }
                    // 4 x 2 = 8
                    lores.add(ChatColor.GRAY + "たくさんくれたﾆﾝｹﾞﾝ第" + (count + 1) + "位！");
                    lores.add(ChatColor.GRAY + "なまえ：" + rankdata.name + " りんご：" + rankdata.p_apple + "個");
                }

                // 5
                lores.add(ChatColor.AQUA + "ぜーんぶで" + SeichiAssist.allplayergiveapplelong() + "個もらえた！");
                lores.add("");
                lores.add(ChatColor.GREEN + "↓呼び出したﾆﾝｹﾞﾝの情報↓");
                lores.add(ChatColor.GREEN + "今までに" + playerdata.p_apple() + "個もらった");
                lores.add(ChatColor.GREEN + "ﾆﾝｹﾞﾝの中では" + prank + "番目にたくさんくれる！");

                meta.setLore(lores);
                meta.addEnchant(Enchantment.DIG_SPEED, 100, false);
                itemStack.setItemMeta(meta);
                AsyncInventorySetter.setItemAsync(inventory, 6,  itemStack);
            }
        }


        return inventory;

    }

    /**
     * 投票妖精音切り替え
     * @param playSound trueなら鳴らす
     * @return ラベルがついたアイテム
     */
    private static ItemMeta getVotingFairySoundsToggleMeta(final boolean playSound) {
        final ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.JUKEBOX);
        final List<String> lore;
        itemmeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "マナ妖精の音トグル");
        if (playSound) {
            lore = Arrays.asList(
                    ChatColor.RESET + "" + ChatColor.GREEN + "現在音が鳴る設定になっています。",
                    ChatColor.RESET + "" + ChatColor.DARK_GRAY + "※この機能はデフォルトでONです。",
                    ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで切替"
            );
        } else {
            lore = Arrays.asList(
                    ChatColor.RESET + "" + ChatColor.RED + "現在音が鳴らない設定になっています。",
                    ChatColor.RESET + "" + ChatColor.DARK_GRAY + "※この機能はデフォルトでONです。",
                    ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで切替"
            );
            itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
        }
        itemmeta.setLore(lore);

        return itemmeta;
    }

    /**
     * 投票妖精戦略
     * @param playerdata プレイヤーの設定
     * @return ラベルが付いたアイテム
     */
    private static ItemMeta VFPromiseMeta(final PlayerData playerdata) {
        final ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.PAPER);
        itemmeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "妖精とのお約束");
        // n % 4 + 1 -> 1..4
        final List<List<String>> loreTable = Arrays.asList(
                Collections.emptyList(),
                Arrays.asList(
                        ChatColor.RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ガンガンたべるぞ",
                        ChatColor.RESET + "" + ChatColor.GRAY + "とにかく妖精さんにりんごを開放します。",
                        ChatColor.RESET + "" + ChatColor.GRAY + "めっちゃ喜ばれます。"
                ),
                Arrays.asList(
                        ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "バッチリたべよう",
                        ChatColor.RESET + "" + ChatColor.GRAY + "食べ過ぎないように注意しつつ",
                        ChatColor.RESET + "" + ChatColor.GRAY + "妖精さんにりんごを開放します。",
                        ChatColor.RESET + "" + ChatColor.GRAY + "喜ばれます。"
                ),
                Arrays.asList(
                        ChatColor.GREEN + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "リンゴだいじに",
                        ChatColor.RESET + "" + ChatColor.GRAY + "少しだけ妖精さんにりんごを開放します。",
                        ChatColor.RESET + "" + ChatColor.GRAY + "伝えると大抵落ち込みます。"
                ),
                Arrays.asList(
                        ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "リンゴつかうな",
                        ChatColor.RESET + "" + ChatColor.GRAY + "絶対にりんごを開放しません。",
                        ChatColor.RESET + "" + ChatColor.GRAY + ""
                )
        );
        final int strategy = playerdata.toggleGiveApple();
        final List<String> lore = loreTable.get(strategy);

        itemmeta.setLore(lore);
        return itemmeta;
    }

    /**
     * GiganticBerserk進化設定
     * @param p
     * @return メニュー
     */
    public static Inventory getGiganticBerserkBeforeEvolutionMenu(final Player p) {
        //UUID取得
        final UUID uuid = p.getUniqueId();
        //プレイヤーデータ
        final PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (validate(p, playerdata)) return null;
        final Inventory inventory = getEmptyInventory(6, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "スキルを進化させますか?");
        {
            // 色
            final byte[] table = {12, 15, 4, 0, 12};
            final ItemStack itemstack = new ItemStack(Material.STAINED_GLASS_PANE, 1, table[playerdata.giganticBerserk().stage()]);
            final ItemMeta itemmeta = itemstack.getItemMeta();
            itemmeta.setDisplayName(" ");
            itemstack.setItemMeta(itemmeta);

            for (final int i :
                    new int[]{6, 7, 14, 15, 16, 21, 22, 23, 24, 32, 41}) {
                AsyncInventorySetter.setItemAsync(inventory, i,  itemstack);
            }
        }
        {
            final ItemStack itemstack = new ItemStack(Material.STICK, 1);
            final ItemMeta itemmeta = itemstack.getItemMeta();
            itemmeta.setDisplayName(" ");
            itemstack.setItemMeta(itemmeta);

            for (final int i :
                    new int[]{30, 39, 40, 47}) {
                AsyncInventorySetter.setItemAsync(inventory, i,  itemstack);
            }
        }
        {
            final ItemStack itemstack = new ItemStack(Material.NETHER_STAR, 1);
            final ItemMeta itemmeta = itemstack.getItemMeta();
            itemmeta.setDisplayName(ChatColor.WHITE + "スキルを進化させる");
            final List<String> lore = Arrays.asList(
                    ChatColor.RESET + "" + ChatColor.GREEN + "進化することにより、スキルの秘めたる力を解放できますが",
                    ChatColor.RESET + "" + ChatColor.GREEN + "スキルは更に大量の魂を求めるようになり",
                    ChatColor.RESET + "" + ChatColor.GREEN + "レベル(回復確率)がリセットされます",
                    ChatColor.RESET + "" + ChatColor.RED + "本当に進化させますか?",
                    ChatColor.RESET + "" + ChatColor.DARK_RED + ChatColor.UNDERLINE + "クリックで進化させる"
            );
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            AsyncInventorySetter.setItemAsync(inventory, 31,  itemstack);
        }
        return inventory;
    }

    /**
     * GiganticBerserk進化設定
     * @param p
     * @return メニュー
     */
    public static Inventory getGiganticBerserkAfterEvolutionMenu(final Player p) {
        //UUID取得
        final UUID uuid = p.getUniqueId();
        //プレイヤーデータ
        final PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (validate(p, playerdata)) return null;
        final Inventory inventory = getEmptyInventory(6, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "スキルを進化させました");
        {
            final byte[] table = {12, 15, 4, 0, 3};
            final byte b = table[playerdata.giganticBerserk().stage()];

            final ItemStack itemstack = new ItemStack(Material.STAINED_GLASS_PANE, 1, b);

            final ItemMeta itemmeta = itemstack.getItemMeta();
            itemmeta.setDisplayName(" ");
            itemstack.setItemMeta(itemmeta);

            for (final int i :
                    new int[]{6, 7, 14, 15, 16, 21, 22, 23, 24, 32, 41}) {
                AsyncInventorySetter.setItemAsync(inventory, i,  itemstack);
            }
        }
        {
            final ItemStack itemstack = new ItemStack(Material.STICK, 1);
            final ItemMeta itemmeta = itemstack.getItemMeta();
            itemmeta.setDisplayName(" ");
            itemstack.setItemMeta(itemmeta);

            for (final int i :
                    new int[]{30, 39, 40, 47}) {
                AsyncInventorySetter.setItemAsync(inventory, i,  itemstack);
            }
        }

        {
            final ItemStack itemstack = new ItemStack(Material.NETHER_STAR, 1);
            final ItemMeta itemmeta = itemstack.getItemMeta();
            itemmeta.setDisplayName(ChatColor.WHITE + "スキルを進化させました！");
            final List<String> lore = Arrays.asList(
                    ChatColor.RESET + "" + ChatColor.GREEN + "スキルの秘めたる力を解放することで、マナ回復量が増加し",
                    ChatColor.RESET + "" + ChatColor.DARK_RED + "スキルはより魂を求めるようになりました"
            );
            itemmeta.setLore(lore);
            itemstack.setItemMeta(itemmeta);
            AsyncInventorySetter.setItemAsync(inventory, 31,  itemstack);
        }
        return inventory;
    }
    
    private static Inventory getEmptyInventory(final int rows, final String title) {
        return Bukkit.getServer().createInventory(null, rows * 9, title);
    }

    private static SkullMeta build(final String name, final List<String> lore, final String owner) {
        final SkullMeta ret = ItemMetaFactory.SKULL.getValue();
        ret.setDisplayName(name);
        ret.setOwner(owner);
        ret.setLore(lore);
        return ret;
    }

    private static ItemStack build(final Material mat, final String name, final String singleLore) {
        final ItemStack itemstack = new ItemStack(mat, 1);
        final ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(mat);
        itemmeta.setDisplayName(name);
        final List<String> lore = Collections.singletonList(singleLore);
        itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemmeta.setLore(lore);
        itemstack.setItemMeta(itemmeta);
        return itemstack;
    }
}
