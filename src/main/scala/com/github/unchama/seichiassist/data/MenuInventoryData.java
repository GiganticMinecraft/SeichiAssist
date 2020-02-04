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
import scala.Function0;
import scala.Option;
import scala.collection.mutable.HashMap;
import scala.collection.mutable.Map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class MenuInventoryData {
    private MenuInventoryData() {
    }

    private static final HashMap<UUID, PlayerData> playermap = SeichiAssist.playermap();
    private static final DatabaseGateway databaseGateway = SeichiAssist.databaseGateway();

    // 実際には60人も入ることは無いのでは？
    private static final Map<UUID, Boolean> finishedHeadPageBuild = new HashMap<>(60, 0.75);
    private static final Map<UUID, Boolean> finishedMiddlePageBuild = new HashMap<>(60, 0.75);
    private static final Map<UUID, Boolean> finishedTailPageBuild = new HashMap<>(60, 0.75);
    private static final Map<UUID, Boolean> finishedShopPageBuild = new HashMap<>(60, 0.75);

    // 実際には60人も入ることは無いのでは？
    private static final Map<UUID, Integer> headPartIndex = new HashMap<>(60, 0.75);
    private static final Map<UUID, Integer> middlePartIndex = new HashMap<>(60, 0.75);
    private static final Map<UUID, Integer> tailPartIndex = new HashMap<>(60, 0.75);
    private static final Map<UUID, Integer> shopIndex = new HashMap<>(60, 0.75);
    private static final Map<UUID, Integer> taihiIndex = new HashMap<>(60, 0.75);

    private static final List<List<String>> loreTable = Arrays.asList(
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

    /**
     * (short) 3はダサいし、マジックコンスタントみたいだよね。
     */
    private static final short PLAYER_SKULL = 3;

    /**
     * ラムダをいちいち正確に打つのは退屈で疲れる作業だし、かといってメソッドでカプセル化するメリットもない。
     * 加えて、明示的に「まとめる」ことでJVMに対して最適化のヒントとしても使える。
     */
    private static final Function0<Boolean> FALSE = () -> false;

    private static final Consumer<SkullMeta> DIG100 = (meta) -> meta.addEnchant(Enchantment.DIG_SPEED, 100, false);

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

    /**
     * 整地量
     * @param page ページ
     * @return メニュー
     */
    public static Inventory getRankingBySeichiAmount(final int page) {
        final int pageLimit = 14;
        final int lowerBound = 100;
        final Inventory inventory = getEmptyInventory(6, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "整地神ランキング");
        final ItemStack itemstack = new ItemStack(Material.SKULL_ITEM, 1, PLAYER_SKULL);
        int invIndex = 0;
        for (int rank = 10 * page; rank < 10 + 10 * page; rank++) {
            if (rank >= SeichiAssist.ranklist().size()) {
                break;
            }

            final RankData rankdata = SeichiAssist.ranklist().apply(rank);
            if (rankdata.totalbreaknum < (Long) LevelThresholds.levelExpThresholds().apply(lowerBound - 1)) { //レベル100相当の総整地量判定に変更
                break;
            }

            final List<String> lore = Arrays.asList(
                    ChatColor.RESET + "" + ChatColor.GREEN + "整地レベル:" + rankdata.level,
                    ChatColor.RESET + "" + ChatColor.GREEN + "総整地量:" + rankdata.totalbreaknum
            );

            final SkullMeta skullmeta = buildSkullMeta(
                    ChatColor.YELLOW + "" + ChatColor.BOLD + "" + (rank + 1) + "位:" + "" + ChatColor.WHITE + rankdata.name,
                    lore,
                    rankdata.name
            );
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, invIndex, itemstack.clone());
            invIndex++;
        }

        if (page != pageLimit) {
            // 整地神ランキング次ページ目を開く
            final SkullMeta skullMeta = buildSkullMeta(
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
                // 整地神ランキング前ページ目を開く
                name = ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地神ランキング" + page + "ページ目へ";
                lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
                ign = "MHF_ArrowUp";
            }
            final SkullMeta skullmeta = buildSkullMeta(name, lore, ign);
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, 45, itemstack.clone());
        }


        // 総整地量の表記
        {
            final List<String> lore = Arrays.asList(
                    ChatColor.RESET + "" + ChatColor.AQUA + "全プレイヤー総整地量:",
                    ChatColor.RESET + "" + ChatColor.AQUA + SeichiAssist.allplayerbreakblockint()
            );

            final SkullMeta skullmeta = buildSkullMeta(
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
        final ItemStack itemstack = new ItemStack(Material.SKULL_ITEM, 1, PLAYER_SKULL);
        final int rankStart = 10 * page;
        int invIndex = 0;
        for (int rank = rankStart; rank < rankStart + 10; rank++) {
            if (rank >= SeichiAssist.ranklist_playtick().size()) {
                break;
            }
            final RankData rankdata = SeichiAssist.ranklist_playtick().apply(rank);

            final SkullMeta skullmeta = buildSkullMeta(
                ChatColor.YELLOW + "" + ChatColor.BOLD + "" + (rank + 1) + "位:" + "" + ChatColor.WHITE + rankdata.name,
                Collections.singletonList(ChatColor.RESET + "" + ChatColor.GREEN + "総ログイン時間:" + TypeConverter.toTimeString(TypeConverter.toSecond(rankdata.playtick))),
                rankdata.name
            );
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, invIndex, itemstack.clone());
            invIndex++;
        }

        if (page != pageLimit) {
            // 整地神ランキング次ページ目を開く
            final SkullMeta skullmeta = buildSkullMeta(
                ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ログイン神ランキング" + (page + 2) + "ページ目へ",
                Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"),
                "MHF_ArrowDown"
            );
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, 52, itemstack.clone());
        }

        // 前のページ / ホームへ
        {
            final SkullMeta skullmeta;
            if (page == 0) {
                skullmeta = buildSkullMeta(
                        ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ",
                        Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"),
                        "MHF_ArrowLeft"
                );
            } else {
                // 整地神ランキング前ページを開く;
                skullmeta = buildSkullMeta(
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
        final Inventory inventory = getEmptyInventory(6, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "投票神ランキング");
        final ItemStack itemstack = new ItemStack(Material.SKULL_ITEM, 1, PLAYER_SKULL);
        for (int voteRank = 10 * page, invIndex = 0; voteRank < 10 + 10 * page; voteRank++, invIndex++) {
            if (voteRank >= SeichiAssist.ranklist_p_vote().size()) {
                break;
            }

            final RankData rankdata = SeichiAssist.ranklist_p_vote().apply(voteRank);
            if (rankdata.p_vote == 0) {
                break;
            }

            final SkullMeta skullmeta = buildSkullMeta(
                    ChatColor.YELLOW + "" + ChatColor.BOLD + "" + (voteRank + 1) + "位:" + "" + ChatColor.WHITE + rankdata.name,
                    Collections.singletonList(ChatColor.RESET + "" + ChatColor.GREEN + "総投票回数:" + rankdata.p_vote),
                    rankdata.name
            );
            itemstack.setItemMeta(skullmeta);
            AsyncInventorySetter.setItemAsync(inventory, invIndex, itemstack.clone());
        }

        if (page != pageLimit) {
            // 投票神ランキング次ページ目を開く
            final SkullMeta skullmeta = buildSkullMeta(
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
                skullmeta = buildSkullMeta(
                        ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ",
                        Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"),
                        "MHF_ArrowLeft"
                );
            } else {
                // 投票神ランキング前ページを開く;
                skullmeta = buildSkullMeta(
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
        final ItemStack itemstack = new ItemStack(Material.SKULL_ITEM, 1, PLAYER_SKULL);
        for (int donationRank = 50 * page, invIndex = 0; donationRank < 50 + 50 * page; donationRank++, invIndex++) {
            if (donationRank >= SeichiAssist.ranklist_premiumeffectpoint().size()) {
                break;
            }
            final RankData rankdata = SeichiAssist.ranklist_premiumeffectpoint().apply(donationRank);
            if (rankdata.premiumeffectpoint < lowerBound) { //寄付金額0
                break;
            }
            final SkullMeta skullmeta = buildSkullMeta(
        ChatColor.YELLOW + "" + ChatColor.BOLD + "" + (donationRank + 1) + "位:" + "" + ChatColor.WHITE + rankdata.name,
                Collections.singletonList(ChatColor.RESET + "" + ChatColor.GREEN + "総寄付金額:" + rankdata.premiumeffectpoint * 100),
                rankdata.name
            );
            itemstack.setItemMeta(skullmeta);
            final int finalInventoryIndex;
            if (invIndex == 45) {
                finalInventoryIndex = 47;
            } else {
                finalInventoryIndex = invIndex;
            }
            AsyncInventorySetter.setItemAsync(inventory, finalInventoryIndex, itemstack.clone());
        }

        if (page != pageLimit) {
            // 整地神ランキング次ページ目を開く
            final SkullMeta skullmeta = buildSkullMeta(
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
                skullmeta = buildSkullMeta(
                        ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ",
                        Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"),
                        "MHF_ArrowLeft"
                );
            } else {
                // 寄付神ランキング前ページ目を開く;
                skullmeta = buildSkullMeta(
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
        final UUID uuid = p.getUniqueId();
        final PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (validate(p, playerdata, "整地スキルエフェクト選択")) return null;
        final Inventory inventory = getEmptyInventory(6, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "整地スキルエフェクト選択");

        // 1ページ目を開く
        {
            final ItemStack itemstack = buildPlayerSkull(
                    ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "スキルメニューへ",
                    ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動",
                    "MHF_ArrowLeft"
            );
            AsyncInventorySetter.setItemAsync(inventory, 45, itemstack.clone());
        }
        //1行目
        {
            final List<String> lore = Arrays.asList(
                    ChatColor.RESET + "" + ChatColor.GREEN + "現在選択しているエフェクト：" + ActiveSkillNormalEffect.getNameByNum(playerdata.activeskilldata().effectnum),
                    ChatColor.RESET + "" + ChatColor.YELLOW + "使えるエフェクトポイント：" + playerdata.activeskilldata().effectpoint,
                    ChatColor.RESET + "" + ChatColor.DARK_GRAY + "※投票すると獲得出来ます",
                    ChatColor.RESET + "" + ChatColor.LIGHT_PURPLE + "使えるプレミアムポイント：" + playerdata.activeskilldata().premiumeffectpoint,
                    ChatColor.RESET + "" + ChatColor.DARK_GRAY + "※寄付をすると獲得できます"
            );
            final ItemStack itemstack = buildPlayerSkull(
                    ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + playerdata.lowercaseName() + "のスキルエフェクトデータ",
                    lore,
                    // この操作は安全; メニューを開けているのにUUIDがないなんてことがないから
                    Bukkit.getOfflinePlayer(playerdata.uuid()).getName(),
                    DIG100
            );
            AsyncInventorySetter.setItemAsync(inventory, 0, itemstack.clone());
        }

        {
            final ItemStack itemstack = build(
                    Material.BOOK_AND_QUILL,
                    ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "プレミアムエフェクト購入履歴",
                    ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで閲覧"
            );
            AsyncInventorySetter.setItemAsync(inventory, 2,  itemstack);
        }

        {
            final ItemStack itemstack = build(
                    Material.GLASS,
                    ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "エフェクトを使用しない",
                    ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット",
                    (meta) -> meta.addEnchant(Enchantment.DIG_SPEED, 100, false)
            );
            AsyncInventorySetter.setItemAsync(inventory, 1,  itemstack);
        }

        {
            final ActiveSkillNormalEffect[] effects = ActiveSkillNormalEffect.arrayValues();
            int i = 0;
            for (final ActiveSkillNormalEffect elem :
                    effects) {
                final ItemStack itemstack;
                //プレイヤーがそのスキルを取得している場合の処理
                if (playerdata.activeskilldata().obtainedSkillEffects.contains(elem)) {
                    itemstack = build(
                            elem.material(),
                            elem.nameOnUI(),
                            Arrays.asList(
                                    ChatColor.RESET + "" + ChatColor.GREEN + elem.explanation(),
                                    ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット"
                            )
                    );
                }
                //プレイヤーがそのスキルをまだ取得していない場合の処理
                else {
                    itemstack = build(
                            Material.BEDROCK,
                            null,
                            Arrays.asList(
                                    ChatColor.RESET + "" + ChatColor.GREEN + elem.explanation(),
                                    ChatColor.RESET + "" + ChatColor.YELLOW + "必要エフェクトポイント：" + elem.usePoint(),
                                    ChatColor.RESET + "" + ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除"
                            )
                    );
                }
                AsyncInventorySetter.setItemAsync(inventory, i + 9,  itemstack);
                i++;
            }
        }
        {
            final ActiveSkillPremiumEffect[] effects = ActiveSkillPremiumEffect.arrayValues();
            int i = 27;
            for (final ActiveSkillPremiumEffect effect :
                    effects) {
                final ItemStack itemstack;
                if (playerdata.activeskilldata().obtainedSkillPremiumEffects.contains(effect)) {
                    itemstack = build(
                            effect.material(),
                            ChatColor.UNDERLINE + "" + ChatColor.BOLD + ChatColor.stripColor(effect.desc()),
                            Arrays.asList(
                                    ChatColor.RESET + "" + ChatColor.GREEN + effect.explain(),
                                    ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット"
                            )
                    );
                } else {
                    //プレイヤーがそのスキルをまだ取得していない場合の処理
                    itemstack = build(
                            Material.BEDROCK,
                            effect.desc(),
                            Arrays.asList(
                                    ChatColor.RESET + "" + ChatColor.GREEN + effect.explain(),
                                    ChatColor.RESET + "" + ChatColor.YELLOW + "必要プレミアムポイント：" + effect.usePoint(),
                                    ChatColor.RESET + "" + ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除"
                            )
                    );
                }

                AsyncInventorySetter.setItemAsync(inventory, i,  itemstack);
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
        final ItemStack itemstack = buildPlayerSkull(
                null,
                ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動",
                "MHF_ArrowLeft"
        );
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
        final UUID uuid = p.getUniqueId();
        final PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (validate(p, playerdata, "二つ名組み合わせ")) return null;
        final Inventory inventory = getEmptyInventory(4, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "二つ名組合せシステム");

        //各ボタンの設定
        finishedHeadPageBuild.put(uuid, false);
        finishedMiddlePageBuild.put(uuid, false);
        finishedTailPageBuild.put(uuid, false);
        finishedShopPageBuild.put(uuid, false);
        headPartIndex.put(uuid, 0);
        middlePartIndex.put(uuid, 0);
        tailPartIndex.put(uuid, 0);
        shopIndex.put(uuid, 0);
        taihiIndex.put(uuid, 0);

        //実績ポイントの最新情報反映ボタン
        {
            // dynamic button
            final List<String> lore = Arrays.asList(
                    ChatColor.RESET + "" + ChatColor.GREEN + "クリックで情報を最新化",
                    ChatColor.RESET + "" + ChatColor.RED + "累計獲得量：" + playerdata.achievePoint().cumulativeTotal(),
                    ChatColor.RESET + "" + ChatColor.RED + "累計消費量：" + playerdata.achievePoint().used(),
                    ChatColor.RESET + "" + ChatColor.AQUA + "使用可能量：" + playerdata.achievePoint().left()
            );

            final ItemStack itemstack = build(
                    Material.EMERALD_ORE,
                    ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績ポイント 情報",
                    lore
            );

            AsyncInventorySetter.setItemAsync(inventory, 0,  itemstack);
        }
        //パーツショップ
        {
            // const button
            final ItemStack itemstack = build(
                    Material.ITEM_FRAME,
                    ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績ポイントショップ",
                    ChatColor.RESET + "" + ChatColor.GREEN + "クリックで開きます"
            );
            AsyncInventorySetter.setItemAsync(inventory, 9,  itemstack);
        }
        //エフェクトポイントからの変換ボタン
        {
            // dynamic button
            final List<String> lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "JMS投票で手に入るポイントを",
                    ChatColor.RESET + "" + ChatColor.RED + "実績ポイントに変換できます。",
                    ChatColor.RESET + "" + ChatColor.YELLOW + "" + ChatColor.BOLD + "投票pt 10pt → 実績pt 3pt",
                    ChatColor.RESET + "" + ChatColor.AQUA + "クリックで変換を一回行います。",
                    ChatColor.RESET + "" + ChatColor.GREEN + "所有投票pt :" + playerdata.activeskilldata().effectpoint,
                    ChatColor.RESET + "" + ChatColor.GREEN + "所有実績pt :" + playerdata.achievePoint().left()
            );

            final ItemStack itemstack = build(
                    Material.EMERALD,
                    ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ポイント変換ボタン",
                    lore
            );
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

        {
            // const button
            final ItemStack toHeadSelection = build(
                    Material.WATER_BUCKET,
                    ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "前パーツ選択画面",
                    ChatColor.RESET + "" + ChatColor.RED + "クリックで移動します"
            );

            // const button
            final ItemStack toMiddleSelection = build(
                    Material.MILK_BUCKET,
                    ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "中パーツ選択画面",
                    ChatColor.RESET + "" + ChatColor.RED + "クリックで移動します"
            );

            // const button
            final ItemStack toTailSelection = build(
                    Material.LAVA_BUCKET,
                    ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "後パーツ選択画面",
                    ChatColor.RESET + "" + ChatColor.RED + "クリックで移動します"
            );
            AsyncInventorySetter.setItemAsync(inventory, 11, toHeadSelection);
            AsyncInventorySetter.setItemAsync(inventory, 13, toMiddleSelection);
            AsyncInventorySetter.setItemAsync(inventory, 15, toTailSelection);
        }

        // 1ページ目を開く
        {
            // const Button
            final ItemStack itemstack = buildPlayerSkull(
                    ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績・二つ名メニューへ",
                    ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動",
                    "MHF_ArrowLeft"
            );
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
        final UUID uuid = p.getUniqueId();
        final PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (validate(p, playerdata, "二つ名/前パーツ")) return null;

        final Inventory inventory = getEmptyInventory(4, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "二つ名組合せ「前」");

        if (finishedHeadPageBuild.getOrElse(uuid, () -> false)) {
            finishedHeadPageBuild.put(uuid, false);
        } else {
            headPartIndex.put(uuid, 1000);
        }

        //各ボタンの設定
        //解禁済みの実績をチェック→前パーツがあるかをチェック→あればボタン配置
        int inventoryIndex = 0;
        for (int i = headPartIndex.get(uuid).get(); i < 9900; i++) {
            if (inventoryIndex < 27) {
                if (playerdata.TitleFlags().contains(i)) {
                    final Option<String> maybeHeadPart = Nicknames.getHeadPartFor(i);
                    if (maybeHeadPart.nonEmpty()) {
                        final ItemStack itemstack = build(
                                Material.WATER_BUCKET,
                                Integer.toString(i),
                                ChatColor.RESET + "" + ChatColor.RED + "前パーツ「" + maybeHeadPart.get() + "」"
                        );
                        final ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.WATER_BUCKET);
                        AsyncInventorySetter.setItemAsync(inventory, inventoryIndex,  itemstack);

                        inventoryIndex++;
                    }
                }

            } else if (inventoryIndex == 27) {
                //次ページへのボタンを配置
                final ItemStack itemstack = buildPlayerSkull(
                        ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "次ページへ",
                        ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動",
                        "MHF_ArrowRight"
                );
                finishedHeadPageBuild.put(uuid, true);
                break;
            }
        }

        //パーツ未選択状態にするボタン
        {
            // Pure Button
            final ItemStack itemstack = build(
                    Material.GRASS,
                    ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "前パーツを未選択状態にする",
                    ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで実行");
            AsyncInventorySetter.setItemAsync(inventory, 31,  itemstack);
        }
        // 二つ名組合せメインページを開く
        {
            // Pure Button
            final ItemStack itemstack = build(
                    Material.BARRIER,
                    ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "二つ名組合せメインメニューへ",
                    ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
            AsyncInventorySetter.setItemAsync(inventory, 27,  itemstack);
        }
        return inventory;
    }

    private static boolean validate(final Player destination, final PlayerData pd, final String operation) {
        if (pd == null) {
            destination.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[" + operation + "]でエラー発生");
            Bukkit.getLogger().warning(destination + "のplayerdataがありません。開発者に報告してください");
            return true;
        }
        return false;
    }
    /**
     * 二つ名 - 中パーツ
     * @param p プレイヤー
     * @return メニュー
     */
    public static Inventory setFreeTitle2Data(final Player p) {
        final UUID uuid = p.getUniqueId();
        final PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (validate(p, playerdata, "二つ名/中パーツ")) return null;

        final Inventory inventory = getEmptyInventory(4, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "二つ名組合せ「中」");


        if (finishedMiddlePageBuild.getOrElse(uuid, FALSE)) {
            finishedMiddlePageBuild.put(uuid, false);
        } else {
            middlePartIndex.put(uuid, 9900);
        }

        //各ボタンの設定
        //パーツがあるかをチェック→あればボタン配置
        int inventoryIndex = 0;
        for (int i = middlePartIndex.get(uuid).get(); i < 9999; i++) {
            if (inventoryIndex < 27) {
                final Option<String> maybeMiddlePart = Nicknames.getMiddlePartFor(i);
                //一部の「隠し中パーツ」は取得しているかの確認
                if (9911 <= i
                        && playerdata.TitleFlags().contains(i)
                        && maybeMiddlePart.nonEmpty()
                    || maybeMiddlePart.nonEmpty()) {
                        final ItemStack itemstack = build(
                                Material.MILK_BUCKET,
                                Integer.toString(i),
                                ChatColor.RESET + "" + ChatColor.RED + "中パーツ「" + maybeMiddlePart.get() + "」"
                        );

                        AsyncInventorySetter.setItemAsync(inventory, inventoryIndex, itemstack);
                        inventoryIndex++;
                }
            } else if (inventoryIndex == 27) {
                //次ページへのボタンを配置
                final List<String> lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
                final ItemStack itemstack = buildPlayerSkull(
                        ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "次ページへ",
                        lore,
                        "MHF_ArrowRight"
                );
                AsyncInventorySetter.setItemAsync(inventory, 35, itemstack.clone());
                finishedMiddlePageBuild.put(uuid, true);
                break;
            }
        }
        
        //パーツ未選択状態にするボタン
        {
            final ItemStack itemstack = build(
                    Material.GRASS,
                    ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "中パーツを未選択状態にする",
                    ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで実行");
            AsyncInventorySetter.setItemAsync(inventory, 31,  itemstack);
        }

        // 二つ名組合せメインページを開くボタン
        {
            final ItemStack itemstack = build(
                    Material.BARRIER,
                    ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "二つ名組合せメインメニューへ",
                    ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
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
        final UUID uuid = p.getUniqueId();
        final PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (validate(p, playerdata, "二つ名/後パーツ")) return null;
        final Inventory inventory = getEmptyInventory(4, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "二つ名組合せ「後」");

        if (!finishedTailPageBuild.getOrElse(uuid, FALSE)) {
            tailPartIndex.put(uuid, 1000);
        }

        //各ボタンの設定
        //解禁済みの実績をチェック→後パーツがあるかをチェック→あればボタン配置
        int inventoryIndex = 0;
        for (int i = tailPartIndex.get(uuid).get(); i < 9900; i++) {
            if (inventoryIndex < 27) {
                if (playerdata.TitleFlags().contains(i)) {
                    final Option<String> maybeTailPart = Nicknames.getTailPartFor(i);
                    if (maybeTailPart.nonEmpty()) {
                        final ItemStack itemstack = build(
                                Material.LAVA_BUCKET,
                                Integer.toString(i),
                                ChatColor.RESET + "" + ChatColor.RED + "後パーツ「" + maybeTailPart.get() + "」");
                        AsyncInventorySetter.setItemAsync(inventory, inventoryIndex,  itemstack);
                        inventoryIndex++;
                    }
                }
            } else if (inventoryIndex == 27) {
                //次ページへのボタンを配置
                final ItemStack itemstack = buildPlayerSkull(
                        ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "次ページへ",
                        Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"),
                        "MHF_ArrowRight"
                );
                AsyncInventorySetter.setItemAsync(inventory, 35, itemstack.clone());
                finishedTailPageBuild.put(uuid, true);
                break;
            }
        }

        //パーツ未選択状態にするボタン
        {
            final ItemStack itemstack = build(
                    Material.GRASS,
                    ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "後パーツを未選択状態にする",
                    ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで実行"
            );
            AsyncInventorySetter.setItemAsync(inventory, 31,  itemstack);
        }

        // 二つ名組合せメインページを開くボタン
        {
            final ItemStack itemstack = build(
                    Material.BARRIER,
                    ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "二つ名組合せメインメニューへ",
                    ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
            );
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
        final UUID uuid = p.getUniqueId();
        //プレイヤーデータ
        final PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (validate(p, playerdata, "実績ポイントショップ")) return null;
        final Inventory inventory = getEmptyInventory(4, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績ポイントショップ");

        //実績ポイントの最新情報反映ボタン
        {
            final AchievementPoint ap = playerdata.achievePoint();
            // dynamic button
            final List<String> lore = Arrays.asList(
                    ChatColor.RESET + "" + ChatColor.GREEN + "クリックで情報を最新化",
                    ChatColor.RESET + "" + ChatColor.RED + "累計獲得量：" + ap.cumulativeTotal(),
                    ChatColor.RESET + "" + ChatColor.RED + "累計消費量：" + ap.used(),
                    ChatColor.RESET + "" + ChatColor.AQUA + "使用可能量：" + ap.left()
            );
            final ItemStack itemstack = build(
                    Material.EMERALD_ORE,
                    ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績ポイント 情報",
                    lore
            );
            AsyncInventorySetter.setItemAsync(inventory, 0,  itemstack);
        }

        //おしながき
        if (playerdata.samepageflag()) {
            shopIndex.put(uuid, taihiIndex.get(uuid).get());
        } else if (!finishedShopPageBuild.getOrElse(uuid, FALSE)) {
            shopIndex.put(uuid, 9801);
        }

        taihiIndex.put(uuid, shopIndex.get(uuid).get());
        playerdata.samepageflag_$eq(false);
        int inventoryIndex = 1;
        int forNextI = 0;
        for (int i = shopIndex.get(uuid).get(); i <= 9833; i++) {
            final List<String> lore;
            final ItemStack itemstack;
            if (inventoryIndex < 27) {
                if (!playerdata.TitleFlags().contains(i)) {
                    lore = Arrays.asList(
                            ChatColor.RESET + "" + ChatColor.RED + "前・後パーツ「" + Nicknames.getHeadPartFor(i).getOrElse(() -> "") + "」",
                            ChatColor.RESET + "" + ChatColor.GREEN + "必要ポイント：20",
                            ChatColor.RESET + "" + ChatColor.AQUA + "クリックで購入できます"
                    );

                    itemstack = build(
                            Material.BEDROCK,
                            Integer.toString(i),
                            lore
                    );
                    AsyncInventorySetter.setItemAsync(inventory, inventoryIndex,  itemstack);
                    inventoryIndex++;
                }
            } else {
                //次ページへのボタンを配置
                itemstack = buildPlayerSkull(
                        ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "次ページへ",
                        ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動",
                        "MHF_ArrowRight"
                );
                AsyncInventorySetter.setItemAsync(inventory, 35, itemstack.clone());

                finishedShopPageBuild.put(uuid, true);
                forNextI = i;
                break;
            }
        }

        // SAFE: putしているのでキーがないなんてことはない
        shopIndex.put(uuid, Math.max(forNextI, 9911));
        for (int i = shopIndex.get(uuid).get(); i <= 9938; i++) {
            if (inventoryIndex < 27) {
                if (!playerdata.TitleFlags().contains(i)) {
                    final List<String> lore = Arrays.asList(
                            ChatColor.RESET + "" + ChatColor.RED + "中パーツ「" + Nicknames.getMiddlePartFor(i).getOrElse(() -> "") + "」",
                            ChatColor.RESET + "" + ChatColor.GREEN + "必要ポイント：35",
                            ChatColor.RESET + "" + ChatColor.AQUA + "クリックで購入できます"
                    );

                    final ItemStack itemstack = build(
                            Material.BEDROCK,
                            Integer.toString(i),
                            lore
                    );

                    AsyncInventorySetter.setItemAsync(inventory, inventoryIndex,  itemstack);
                    inventoryIndex++;
                }
            } else {
                //次ページへ遷移するボタン
                // const button
                final ItemStack itemstack = buildPlayerSkull(
                        ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "次ページへ",
                        ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動",
                        "MHF_ArrowRight"
                );

                AsyncInventorySetter.setItemAsync(inventory, 35, itemstack);
                finishedShopPageBuild.put(uuid, true);
                break;
            }
        }

        // 二つ名組合せメインページを開くボタン
        {
            final ItemStack itemstack = build(
                    Material.BARRIER,
                    ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "二つ名組合せメインメニューへ",
                    ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
            );
            AsyncInventorySetter.setItemAsync(inventory, 27,  itemstack);
        }
        return inventory;
    }

    /**
     * 投票妖精メニュー
     * @param p プレイヤー
     * @return メニュー
     */
    public static Inventory getVotingMenuData(final Player p) {
        //UUID取得
        final UUID uuid = p.getUniqueId();
        //プレイヤーデータ
        final PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (validate(p, playerdata, "投票妖精")) return null;
        final Inventory inventory = getEmptyInventory(4, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "投票ptメニュー");

        //投票pt受け取り
        {
            // dynamic button
            final ItemStack itemstack = build(
                    Material.DIAMOND,
                    ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "クリックで投票特典を受け取れます",
                    getVoteButtonLore(playerdata),
                    DIG100
            );
            AsyncInventorySetter.setItemAsync(inventory, 0,  itemstack);
        }

        // ver0.3.2 投票ページ表示
        // const button
        {
            final List<String> lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + "投票すると様々な特典が！",
                    ChatColor.RESET + "" + ChatColor.GREEN + "1日1回投票出来ます",
                    ChatColor.RESET + "" + ChatColor.DARK_GRAY + "クリックするとチャット欄に",
                    ChatColor.RESET + "" + ChatColor.DARK_GRAY + "URLが表示されますので",
                    ChatColor.RESET + "" + ChatColor.DARK_GRAY + "Tキーを押してから",
                    ChatColor.RESET + "" + ChatColor.DARK_GRAY + "そのURLをクリックしてください"
            );

            final ItemStack itemstack = build(
                    Material.BOOK_AND_QUILL,
                    ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "投票ページにアクセス",
                    lore
            );
            AsyncInventorySetter.setItemAsync(inventory, 9,  itemstack);
        }

        //棒メニューに戻る
        {
            // const button
            final List<String> lore = Collections.singletonList(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
            final ItemStack itemstack = buildPlayerSkull(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ", lore, "MHF_ArrowLeft");
            AsyncInventorySetter.setItemAsync(inventory, 27, itemstack.clone());
        }

        //妖精召喚時間設定トグルボタン
        {
            // const button
            final List<String> list = Arrays.asList(
                    ChatColor.RESET + "" + ChatColor.GREEN + "" + ChatColor.BOLD + "" + VotingFairyTask.dispToggleVFTime(playerdata.toggleVotingFairy()),
                    "",
                    ChatColor.RESET + "" + ChatColor.GRAY + "コスト",
                    ChatColor.RESET + "" + ChatColor.RED + "" + ChatColor.BOLD + "" + playerdata.toggleVotingFairy() * 2 + "投票pt",
                    "",
                    ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで切替"
            );

            final ItemStack itemStack = build(
                    Material.WATCH,
                    ChatColor.AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "マナ妖精 時間設定",
                    list
            );

            AsyncInventorySetter.setItemAsync(inventory, 2,  itemStack);
        }

        //妖精契約設定トグル
        {
            final ItemStack itemStack = new ItemStack(Material.PAPER);
            itemStack.setItemMeta(getVotingFairyContractMeta(playerdata));
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
            final List<String> lore = Arrays.asList(
                    ChatColor.RESET + "" + ChatColor.GRAY + "" + playerdata.toggleVotingFairy() * 2 + "投票ptを消費して",
                    ChatColor.RESET + "" + ChatColor.GRAY + "マナ妖精を呼びます",
                    ChatColor.RESET + "" + ChatColor.GRAY + "時間 : " + VotingFairyTask.dispToggleVFTime(playerdata.toggleVotingFairy()),
                    ChatColor.RESET + "" + ChatColor.DARK_RED + "Lv.10以上で解放"
            );

            // dynamic button
            final ItemStack itemStack = build(
                    Material.GHAST_TEAR,
                    ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "マナ妖精 召喚",
                    lore,
                    DIG100
            );

            AsyncInventorySetter.setItemAsync(inventory, 4,  itemStack);
        }


        if (playerdata.usingVotingFairy()) {
            //妖精 時間確認
            {
                // const button
                final List<String> lore = Arrays.asList(
                        ChatColor.RESET + "" + ChatColor.GRAY + "妖精さんはいそがしい。",
                        ChatColor.GRAY + "帰っちゃう時間を教えてくれる"
                );

                final ItemStack itemStack = build(
                        Material.COMPASS,
                        ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "マナ妖精に時間を聞く",
                        lore,
                        DIG100
                );

                AsyncInventorySetter.setItemAsync(inventory, 13,  itemStack);
            }

            {
                // dynamic button
                final int yourRank = playerdata.calcPlayerApple();
                final List<String> lore = new ArrayList<>(6 + 2 * 4 + 5);

                // 6
                lore.addAll(Arrays.asList(
                        ChatColor.RESET + "" + ChatColor.RED + "" + ChatColor.BOLD + "※ﾆﾝｹﾞﾝに見られないように気を付けること！",
                        ChatColor.RESET + "" + ChatColor.RED + "" + ChatColor.BOLD + "  毎日大妖精からデータを更新すること！",
                        "",
                        ChatColor.RESET + "" + ChatColor.GOLD + "" + ChatColor.BOLD + "昨日までにがちゃりんごを",
                        ChatColor.RESET + "" + ChatColor.GOLD + "" + ChatColor.BOLD + "たくさんくれたﾆﾝｹﾞﾝたち",
                        ChatColor.RESET + "" + ChatColor.DARK_GRAY + "召喚されたらラッキーだよ！"
                ));
                for (int rank = 0; rank <= 3; rank++) {
                    if (rank >= SeichiAssist.ranklist_p_apple().size()) {
                        break;
                    }
                    final RankData rankdata = SeichiAssist.ranklist_p_apple().apply(rank);
                    if (rankdata.p_apple == 0) {
                        break;
                    }
                    // 2 x 4 = 8
                    lore.add(ChatColor.GRAY + "たくさんくれたﾆﾝｹﾞﾝ第" + (rank + 1) + "位！");
                    lore.add(ChatColor.GRAY + "なまえ：" + rankdata.name + " りんご：" + rankdata.p_apple + "個");
                }

                // 5
                lore.add(ChatColor.AQUA + "ぜーんぶで" + SeichiAssist.allplayergiveapplelong() + "個もらえた！");
                lore.add("");
                lore.add(ChatColor.GREEN + "↓呼び出したﾆﾝｹﾞﾝの情報↓");
                lore.add(ChatColor.GREEN + "今までに" + playerdata.p_apple() + "個もらった");
                lore.add(ChatColor.GREEN + "ﾆﾝｹﾞﾝの中では" + yourRank + "番目にたくさんくれる！");

                final ItemStack itemStack = build(
                        Material.GOLDEN_APPLE,
                        ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "㊙ がちゃりんご情報 ㊙",
                        lore,
                        DIG100
                );
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
    private static ItemMeta getVotingFairyContractMeta(final PlayerData playerdata) {
        final ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.PAPER);
        itemmeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "妖精とのお約束");
        // n % 4 + 1 -> 1..4
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
        if (validate(p, playerdata, "Gigantic進化前確認")) return null;
        final Inventory inventory = getEmptyInventory(6, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "スキルを進化させますか?");
        {
            // 色
            final byte[] table = {12, 15, 4, 0, 3};
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
            final ItemStack itemstack = build(Material.STICK, " ", (String) null);
            for (final int i :
                    new int[]{30, 39, 40, 47}) {
                AsyncInventorySetter.setItemAsync(inventory, i,  itemstack);
            }
        }

        {
            // const button
            final List<String> lore = Arrays.asList(
                    ChatColor.RESET + "" + ChatColor.GREEN + "進化することにより、スキルの秘めたる力を解放できますが",
                    ChatColor.RESET + "" + ChatColor.GREEN + "スキルは更に大量の魂を求めるようになり",
                    ChatColor.RESET + "" + ChatColor.GREEN + "レベル(回復確率)がリセットされます",
                    ChatColor.RESET + "" + ChatColor.RED + "本当に進化させますか?",
                    ChatColor.RESET + "" + ChatColor.DARK_RED + ChatColor.UNDERLINE + "クリックで進化させる"
            );

            final ItemStack itemstack = build(
                    Material.NETHER_STAR,
                    ChatColor.WHITE + "スキルを進化させる",
                    lore
            );

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
        if (validate(p, playerdata, "GiganticBerserk進化後画面")) return null;
        final Inventory inventory = getEmptyInventory(6, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "スキルを進化させました");
        {
            final byte[] table = {12, 15, 4, 0, 3, 12};
            final byte b = table[playerdata.giganticBerserk().stage()];

            final ItemStack itemstack = new ItemStack(Material.STAINED_GLASS_PANE, 1, b);

            final ItemMeta itemmeta = itemstack.getItemMeta();
            if (playerdata.giganticBerserk().stage() >= 4) {
                itemmeta.addEnchant(Enchantment.DAMAGE_ALL,1,true);
                itemmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            itemmeta.setDisplayName(" ");
            itemstack.setItemMeta(itemmeta);

            for (final int i :
                    new int[]{6, 7, 14, 15, 16, 21, 22, 23, 24, 32, 41}) {
                AsyncInventorySetter.setItemAsync(inventory, i,  itemstack);
            }
        }
        {
            // const button
            final ItemStack itemstack = build(Material.STICK, " ", (String) null);

            for (final int i :
                    new int[]{30, 39, 40, 47}) {
                AsyncInventorySetter.setItemAsync(inventory, i,  itemstack);
            }
        }

        {
            final ItemStack itemstack = build(
                    Material.NETHER_STAR,
                    ChatColor.WHITE + "スキルを進化させました！",
                    Arrays.asList(
                            ChatColor.RESET + "" + ChatColor.GREEN + "スキルの秘めたる力を解放することで、マナ回復量が増加し",
                            ChatColor.RESET + "" + ChatColor.DARK_RED + "スキルはより魂を求めるようになりました"
                    )
            );
            AsyncInventorySetter.setItemAsync(inventory, 31,  itemstack);
        }
        return inventory;
    }
    
    private static Inventory getEmptyInventory(final int rows, final String title) {
        return Bukkit.getServer().createInventory(null, rows * 9, title);
    }

    private static SkullMeta buildSkullMeta(final String name, final List<String> lore, final String owner) {
        final SkullMeta ret = ItemMetaFactory.SKULL.getValue();
        ret.setDisplayName(name);
        ret.setOwner(owner);
        ret.setLore(lore);
        return ret;
    }

    private static ItemStack build(final Material mat, final String name, final String singleLore) {
        return build(mat, name, singleLore, nullConsumer());
    }

    private static <T extends ItemMeta> ItemStack build(final Material mat, final String name, final String singleLineLore, final Consumer<T> modify) {
        return build(mat, name, Collections.singletonList(singleLineLore), modify);
    }

    private static ItemStack build(final Material mat, final String name, final List<String> lore) {
        return build(mat, name, lore, nullConsumer());
    }

    private static <T extends ItemMeta> ItemStack build(final Material mat, final String name, final List<String> lore, final Consumer<T> modify) {
        final ItemStack temp = new ItemStack(mat);
        // 自己責任。
        @SuppressWarnings("unchecked")
        final T meta = (T) temp.getItemMeta();
        if (name != null) {
            meta.setDisplayName(name);
        }

        if (lore != null) {
            meta.setLore(lore);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        modify.accept(meta);
        temp.setItemMeta(meta);
        return temp;
    }

    private static ItemStack buildPlayerSkull(final String name, final String lore, final String owner) {
        return buildPlayerSkull(name, Collections.singletonList(lore), owner);
    }

    private static ItemStack buildPlayerSkull(final String name, final List<String> lore, final String owner) {
        return buildPlayerSkull(name, lore, owner, nullConsumer());
    }

    private static ItemStack buildPlayerSkull(final String name, final List<String> lore, final String owner, final Consumer<SkullMeta> modify) {
        final ItemStack ret = new ItemStack(Material.SKULL_ITEM, 1, PLAYER_SKULL);
        final SkullMeta sm = ItemMetaFactory.SKULL.getValue();
        if (name != null) {
            sm.setDisplayName(name);
        }

        if (lore != null) {
            sm.setLore(lore);
        }
        sm.setOwner(owner);
        modify.accept(sm);
        ret.setItemMeta(sm);
        return ret;
    }
    
    private static <T> Consumer<T> nullConsumer() {
        return (nothing) -> {};
    }
}
