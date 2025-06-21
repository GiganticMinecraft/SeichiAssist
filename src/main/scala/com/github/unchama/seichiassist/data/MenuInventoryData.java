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
	 * GiganticBerserk進化設定
	 * 
	 * @param p
	 * @return メニュー
	 */
	public static Inventory getGiganticBerserkBeforeEvolutionMenu(final Player p) {
		// UUID取得
		final UUID uuid = p.getUniqueId();
		// プレイヤーデータ
		final PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
		// 念のためエラー分岐
		if (isError(p, playerdata, "Gigantic進化前確認"))
			return null;
		final Inventory inventory = getEmptyInventory(6, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "スキルを進化させますか?");
		{
			// 色
			final Material[] table = { Material.ORANGE_STAINED_GLASS_PANE, Material.BLACK_STAINED_GLASS_PANE,
					Material.YELLOW_STAINED_GLASS_PANE, Material.WHITE_STAINED_GLASS_PANE,
					Material.LIGHT_BLUE_STAINED_GLASS_PANE };
			final ItemStack itemstack = new ItemStack(table[playerdata.giganticBerserk().stage()], 1);
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
					ChatColor.RESET + "" + ChatColor.DARK_RED + ChatColor.UNDERLINE + "クリックで進化させる");

			final ItemStack itemstack = build(Material.NETHER_STAR, ChatColor.WHITE + "スキルを進化させる", lore);

			AsyncInventorySetter.setItemAsync(inventory, 31, itemstack);
		}
		return inventory;
	}

	/**
	 * GiganticBerserk進化設定
	 * 
	 * @param p
	 * @return メニュー
	 */
	public static Inventory getGiganticBerserkAfterEvolutionMenu(final Player p) {
		// UUID取得
		final UUID uuid = p.getUniqueId();
		// プレイヤーデータ
		final PlayerData playerdata = SeichiAssist.playermap().apply(uuid);
		// 念のためエラー分岐
		if (isError(p, playerdata, "GiganticBerserk進化後画面"))
			return null;
		final Inventory inventory = getEmptyInventory(6, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "スキルを進化させました");
		{
			final Material[] table = { Material.BROWN_STAINED_GLASS_PANE, Material.BLACK_STAINED_GLASS_PANE,
					Material.YELLOW_STAINED_GLASS_PANE, Material.WHITE_STAINED_GLASS_PANE,
					Material.LIGHT_BLUE_STAINED_GLASS_PANE, Material.BROWN_STAINED_GLASS_PANE };

			final ItemStack itemstack = new ItemStack(table[playerdata.giganticBerserk().stage()], 1);

			final ItemMeta itemmeta = itemstack.getItemMeta();
			if (playerdata.giganticBerserk().stage() >= 4) {
				itemmeta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
				itemmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}
			itemmeta.setDisplayName(" ");
			itemstack.setItemMeta(itemmeta);

			placeGiganticBerserkGlass(inventory, itemstack);
		}

		placeGiganticBerserkShape(inventory);

		{
			final ItemStack itemstack = build(Material.NETHER_STAR, ChatColor.WHITE + "スキルを進化させました！",
					Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + "スキルの秘めたる力を解放することで、マナ回復量が増加し",
							ChatColor.RESET + "" + ChatColor.DARK_RED + "スキルはより魂を求めるようになりました"));
			AsyncInventorySetter.setItemAsync(inventory, 31, itemstack);
		}
		return inventory;
	}

	private static Inventory getEmptyInventory(final int rows, final String title) {
		return Bukkit.getServer().createInventory(null, rows * 9, title);
	}

	private static ItemStack build(final Material mat, final String name, final String singleLore) {
		return build(mat, name, singleLore, nullConsumer());
	}

	private static <T extends ItemMeta> ItemStack build(final Material mat, final String name,
			final String singleLineLore, final Consumer<? super T> modify) {
		return build(mat, name, Collections.singletonList(singleLineLore), modify);
	}

	private static ItemStack build(final Material mat, final String name, final List<String> lore) {
		return build(mat, name, lore, nullConsumer());
	}

	private static <T extends ItemMeta> ItemStack build(final Material mat, final String name, final List<String> lore,
			final Consumer<? super T> modify) {
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

	private static <T> Consumer<T> nullConsumer() {
		return nothing -> {
		};
	}

	private static void placeGiganticBerserkShape(final Inventory inv) {
		final ItemStack itemstack = build(Material.STICK, " ", (String) null);
		for (final int i : new int[] { 30, 39, 40, 47 }) {
			AsyncInventorySetter.setItemAsync(inv, i, itemstack);
		}
	}

	private static void placeGiganticBerserkGlass(final Inventory inv, final ItemStack itemstack) {
		for (final int i : new int[] { 6, 7, 14, 15, 16, 21, 22, 23, 24, 32, 41 }) {
			AsyncInventorySetter.setItemAsync(inv, i, itemstack);
		}
	}
}
