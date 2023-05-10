package com.github.unchama.seichiassist.data;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.achievement.Nicknames;
import com.github.unchama.seichiassist.data.player.AchievementPoint;
import com.github.unchama.seichiassist.data.player.PlayerData;
import com.github.unchama.seichiassist.util.AsyncInventorySetter;
import com.github.unchama.seichiassist.util.ItemMetaFactory;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class MenuInventoryData {
    private MenuInventoryData() {
    }

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

    /**
     * (short) 3はダサいし、マジックコンスタントみたいだよね。
     */
    private static final short PLAYER_SKULL = 3;

    /**
     * ラムダをいちいち正確に打つのは退屈で疲れる作業だし、かといってメソッドでカプセル化するメリットもない。
     * 加えて、明示的に「まとめる」ことでJVMに対して最適化のヒントとしても使える。
     */
    private static final Function0<Boolean> FALSE = () -> false;


    private static final ItemStack toMoveNicknameMenu = build(
            Material.BARRIER,
            ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "二つ名組合せメインメニューへ",
            ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
    );

    public enum MenuType {
        HEAD("" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "二つ名組合せ「前」"),
        MIDDLE("" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "二つ名組合せ「中」"),
        TAIL("" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "二つ名組合せ「後」"),
        SHOP("" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "実績ポイントショップ"),
        COMBINE("" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "二つ名組合せシステム");

        public final String invName;
        MenuType(String invName) {
            this.invName = invName;
        }
    }

    public static void setHeadingIndex(UUID uuid, MenuType k, int index) {
        switch (k) {
            case HEAD:
                headPartIndex.put(uuid, index);
                break;
            case MIDDLE:
                middlePartIndex.put(uuid, index);
                break;
            case TAIL:
                tailPartIndex.put(uuid, index);
                break;
            case SHOP:
                shopIndex.put(uuid, index);
                break;
        }
    }

    public static Option<Integer> getHeadingIndex(UUID uuid, MenuType k) {
        switch (k) {
            case HEAD:
                return headPartIndex.get(uuid);
            case MIDDLE:
                return middlePartIndex.get(uuid);
            case TAIL:
                return tailPartIndex.get(uuid);
            case SHOP:
                return shopIndex.get(uuid);
        }
        throw new AssertionError("This statement shouldn't be reached!");
    }

    /**
     * 二つ名 - 前パーツ
     * @param p プレイヤー
     * @return メニュー
     */
    public static Inventory computeHeadPartCustomMenu(final Player p) {
        final UUID uuid = p.getUniqueId();
        final PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (isError(p, playerdata, "二つ名/前パーツ")) return null;

        final Inventory inventory = getEmptyInventory(4, MenuType.HEAD.invName);

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
                // 統一性のために右下へ
                AsyncInventorySetter.setItemAsync(inventory, 35,  itemstack);
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
        // 二つ名組合せメインページボタン
        {
            // Pure Button
            AsyncInventorySetter.setItemAsync(inventory, 27, toMoveNicknameMenu);
        }
        return inventory;
    }

    private static boolean isError(final Player destination, final PlayerData pd, final String operation) {
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
    public static Inventory computeMiddlePartCustomMenu(final Player p) {
        final UUID uuid = p.getUniqueId();
        final PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (isError(p, playerdata, "二つ名/中パーツ")) return null;

        final Inventory inventory = getEmptyInventory(4, MenuType.MIDDLE.invName);


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
            AsyncInventorySetter.setItemAsync(inventory, 27, toMoveNicknameMenu);
        }
        return inventory;
    }

    /**
     * 二つ名 - 後パーツ
     * @param p プレイヤー
     * @return メニュー
     */
    public static Inventory computeTailPartCustomMenu(final Player p) {
        final UUID uuid = p.getUniqueId();
        final PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (isError(p, playerdata, "二つ名/後パーツ")) return null;
        final Inventory inventory = getEmptyInventory(4, MenuType.TAIL.invName);

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
            AsyncInventorySetter.setItemAsync(inventory, 27, toMoveNicknameMenu);
        }
        return inventory;
    }

    /**
     * 実績ポイントショップ
     * @param p プレイヤー
     * @return メニュー
     */
    public static Inventory computePartsShopMenu(final Player p) {
        //プレイヤーを取得
        final UUID uuid = p.getUniqueId();
        //プレイヤーデータ
        final PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
        //念のためエラー分岐
        if (isError(p, playerdata, "実績ポイントショップ")) return null;
        final Inventory inventory = getEmptyInventory(4, MenuType.SHOP.invName);

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
            AsyncInventorySetter.setItemAsync(inventory, 27, toMoveNicknameMenu);
        }
        return inventory;
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
        if (isError(p, playerdata, "Gigantic進化前確認")) return null;
        final Inventory inventory = getEmptyInventory(6, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "スキルを進化させますか?");
        {
            // 色
            final byte[] table = {12, 15, 4, 0, 3};
            final ItemStack itemstack = new ItemStack(Material.STAINED_GLASS_PANE, 1, table[playerdata.giganticBerserk().stage()]);
            final ItemMeta itemmeta = itemstack.getItemMeta();
            itemmeta.setDisplayName(" ");
            itemstack.setItemMeta(itemmeta);
            placeGiganticBerserkGlass(inventory, itemstack);
        }

        placeGiganticBerserkShape(inventory);

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
        if (isError(p, playerdata, "GiganticBerserk進化後画面")) return null;
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

            placeGiganticBerserkGlass(inventory, itemstack);
        }

        placeGiganticBerserkShape(inventory);

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

    private static ItemStack build(final Material mat, final String name, final String singleLore) {
        return build(mat, name, singleLore, nullConsumer());
    }

    private static <T extends ItemMeta> ItemStack build(final Material mat, final String name, final String singleLineLore, final Consumer<? super T> modify) {
        return build(mat, name, Collections.singletonList(singleLineLore), modify);
    }

    private static ItemStack build(final Material mat, final String name, final List<String> lore) {
        return build(mat, name, lore, nullConsumer());
    }

    private static <T extends ItemMeta> ItemStack build(final Material mat, final String name, final List<String> lore, final Consumer<? super T> modify) {
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

    private static ItemStack buildPlayerSkull(final String name, final List<String> lore, final String owner, final Consumer<? super SkullMeta> modify) {
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
        return nothing -> {};
    }

    private static void placeGiganticBerserkShape(final Inventory inv) {
        final ItemStack itemstack = build(Material.STICK, " ", (String) null);
        for (final int i :
                new int[]{30, 39, 40, 47}) {
            AsyncInventorySetter.setItemAsync(inv, i,  itemstack);
        }
    }

    private static void placeGiganticBerserkGlass(final Inventory inv, final ItemStack itemstack) {
        for (final int i :
                new int[]{6, 7, 14, 15, 16, 21, 22, 23, 24, 32, 41}) {
            AsyncInventorySetter.setItemAsync(inv, i, itemstack);
        }
    }
}
