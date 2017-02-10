package com.github.unchama.seichiassist.listener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.Util;

public class MebiusListener implements Listener {
	// Instanceアクセス用
	public static MebiusListener me;
	// Tipsリスト
	private static List<String> tips = new ArrayList<String>();
	// 経験値瓶をボーナスするLv(EXPBONUS未満)
	private static final int EXPBONUS = 50;
	// デバッグフラグ
	private static boolean DEBUGENABLE = false;
	private static boolean debugFlg = false;

	// 起動時
	public MebiusListener() {
		me = this;
		if (SeichiAssist.config.getMebiusDebug() == 1) {
			// mebiusdebug=1の時はコマンドでトグル可能
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "メビウス帽子のdebugモードトグル機能：有効");
			DEBUGENABLE = true;
		} else {
			// debugmode=0の時はトグル不可能
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "メビウス帽子のdebugモードトグル機能：無効");
		}
		// Tipsリストを読み込む
		loadTips();
	}

	// リロード
	public static void reload() {
		tips.clear();
		loadTips();
	}

	// デバッグ
	public static void debug(Player player) {
		if (DEBUGENABLE) {
			if (debugFlg) {
				player.sendMessage("メビウス帽子：デバッグモードが解除されました。");
			} else {
				player.sendMessage("メビウス帽子：サーバー全体がデバッグモードになりました。");
			}
			debugFlg = !debugFlg;
		}
	}

	// デバッグgive
	public static void debugGive(Player player) {
		if (debugFlg) {
			give(player);
		}
	}

	// プレイヤーログアウト時
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		getPlayerData(player).mebius.cancel();
	}

	// 経験値瓶を投げた時
	// @EventHandler(priority = EventPriority.LOW) //
	// onPlayerRightClickExpBottleEventより先に呼び出す
	// public void onExpBottle(PlayerInteractEvent event) {
	// // PlayerのLvがEXPBONUS以上なら抜ける
	// if (getPlayerData(event.getPlayer()).level >= EXPBONUS) {
	// return;
	// }
	// // PlayerがMebiusを装備してなければ抜ける
	// if (!isEquip(event.getPlayer())) {
	// return;
	// }
	// // メインハンドにアイテムを持っていなければ抜ける
	// if (event.getPlayer().getInventory() == null ||
	// event.getPlayer().getInventory().getItemInMainHand() == null) {
	// return;
	// }
	// try {
	// // 経験値瓶を投げる時
	// if
	// (event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.EXP_BOTTLE)
	// && (event.getAction().equals(Action.RIGHT_CLICK_AIR) ||
	// event.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
	// int amount = 1;
	// // スニーク状態
	// if (event.getPlayer().isSneaking()) {
	// amount = event.getItem().getAmount();
	// }
	// // 投げる個数分追加で投げる
	// for (int cnt = 0; cnt < amount; cnt++) {
	// event.getPlayer().launchProjectile(ThrownExpBottle.class);
	// }
	// }
	// } catch (NullPointerException e) {
	// // 万が一NullPointerExceptionが発生した場合、処理無しでOK
	// }
	// }

	// Tipsを呼び出されたとき
	public static void callTips(Player player) {
		if (isEquip(player)) {
			int no = new Random().nextInt(tips.size() + 1);
			if (no == tips.size()) {
				// Talkを喋る
				getPlayerData(player).mebius.speak(getTalk(getIl(player.getInventory().getHelmet())));
			} else {
				// tipsの中身を設定
				getPlayerData(player).mebius.speak(tips.get(no));
			}
		}
	}

	// ダメージを受けた時
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {
		final List<String> breakmsgs = Arrays.asList(
				"いたた…もうすぐ壊れちゃいそうだ…",
				"もうダメかも…こんなところで、悔しいなぁ",
				"お願い、修繕して欲しいよ…",
				"ごめんね…これ以上は[str1]のこと、守ってあげられそうにないよ…",
				"もっと[str1]と、旅したかったなぁ",
				"まだ平気…壊れるまでは、[str1]のことを守るんだ…");
		final List<String> warnmsgs = Arrays.asList(
				"[str2]からの攻撃だ！気を付けて！",
				"お前なんか余裕なんだからなー！さあ[str1]、やっちゃえ！",
				"びっくりしたなー！人が休んでるときにー！",
				"もーなんで今攻撃してくるのさあああ！",
				"いったーいっ、今僕の小指踏んだなー！？",
				"いてっ！やめろよー！僕を怒らせたら怖いぞー！");

		// プレイヤーがダメージを受けた場合
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			// プレイヤーがMebiusを装備していない場合は除外
			if (!isEquip(player)) {
				return;
			}

			ItemStack mebius = player.getInventory().getHelmet();
			// 耐久無限じゃない場合
			if (!mebius.getItemMeta().spigot().isUnbreakable()) {
				// 耐久閾値を超えていたら破損警告
				short max = mebius.getType().getMaxDurability();
				short dur = mebius.getDurability();
				if (dur >= max - 10) {
					getPlayerData((Player) event.getEntity()).mebius.speak(getMessage(breakmsgs, player.getName(), ""));
				}
			}

			// モンスターからダメージを受けた場合
			if (event.getDamager() instanceof Monster) {
				Monster monster = (Monster) event.getDamager();
				// 対モンスターメッセージ
				getPlayerData((Player) event.getEntity()).mebius.speak(getMessage(warnmsgs, player.getName(), monster.getName()));
			}
		}
	}

	// 壊れたとき
	@EventHandler
	public void onBreak(PlayerItemBreakEvent event) {
		final List<String> msgs = Arrays.asList(
				"ここまでかぁっ…[str1]と一緒に旅したこと、すごく楽しかったなぁ…",
				"この先[str1]のこと、守ってあげられなくなっちゃった…ごめんね…",
				"僕、少しは[str1]の役に立てたかなぁ…もしそうだったら、嬉しいなぁ",
				"[str1]のおかげで最期まで防具としていられたんだぁ…使ってくれて、ありがとう。",
				"最期まで[str1]の頭にいれたことって、すごく幸せなことだよ",
				"もし生まれ変わっても、また[str1]と…");
		ItemStack item = event.getBrokenItem();
		// 壊れたアイテムがMEBIUSなら
		if (isMebius(item)) {
			Player player = event.getPlayer();
			getPlayerData(event.getPlayer()).mebius.speak(getMessage(msgs, player.getName(), ""));
			player.sendMessage(getName(item) + ChatColor.RESET + "が旅立ちました。");
			// エンドラが叫ぶ
			player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_DEATH, 1f, 0.1f);
		}
	}

	// モンスターを倒した時
	@EventHandler
	public void onKill(EntityDeathEvent event) {
		try {
			final List<String> msgs = Arrays.asList(
					"さすが[str1]！[str2]なんて敵じゃないね！",
					"僕にかかれば[str2]なんてこんなもんだよー！",
					"モンスターってなんで人間を襲うんだろう…？",
					"ねえ[str1]、今の僕のおかげだよね！ね？",
					"たまにはやられてみたいもんだねー、ふふん！",
					"[str2]なんて僕の力を出すまでもなかったね！");
			// プレイヤーがモンスターを倒した場合以外は除外
			if (!(event.getEntity() instanceof Monster) || !(event.getEntity().getKiller() instanceof Player)) {
				return;
			}
			Player player = (Player) event.getEntity().getKiller();
			Monster monster = (Monster) event.getEntity();
			// プレイヤーがMebiusを装備していない場合は除外
			if (!isEquip(player)) {
				return;
			}
			getPlayerData(player).mebius.speak(getMessage(msgs, player.getName(), monster.getName()));
		} catch (NullPointerException e) {
		}
	}

	// ブロックを破壊した時
	public static void onBlockBreak(BlockBreakEvent event) {
		final List<String> msgs = Arrays.asList(
				"ポコポコポコポコ…整地の音って、落ち着くねえ。",
				"頑張れー！頑張れー！そこをまっすぐ！左にも石があるよー！…うるさい？",
				"一生懸命掘ってると、いつの間にか無心になっちゃうよねえ…！",
				"なんだか眠たくなってきちゃったー、[str1]は平気ー？",
				"今日はどこまで掘るのかなー？",
				"[str1]と一緒に整地するの、楽しいねえ！");
		Player player = event.getPlayer();
		if (isEquip(player)) {
			getPlayerData(player).mebius.speak(getMessage(msgs, player.getName(), ""));
			// Lvup
			if (isLevelUp(player)) {
				levelUp(player);
			}
		}
		// 取得判定
		if (isDrop()) {
			discovery(player);
		}
	}

	// 金床配置時（クリック）
	@EventHandler
	public void onRename(InventoryClickEvent event) {
		// 金床を開いていない場合return
		if (!(event.getView().getTopInventory() instanceof AnvilInventory)) {
			return;
		}
		Inventory inv = event.getClickedInventory();
		if (inv instanceof AnvilInventory) {
			// mebiusを選択中
			ItemStack item = event.getCursor();
			if (isMebius(item)) {
				// mebiusを左枠に置いた場合はcancel
				int rawSlot = event.getRawSlot();
				if (rawSlot == event.getView().convertSlot(rawSlot) && rawSlot == 0) {
					event.setCancelled(true);
					event.getWhoClicked().sendMessage(ChatColor.RED + "MEBIUSへの命名は" + ChatColor.RESET + "/mebius naming <name>" + ChatColor.RED + "で行ってください。");
				}
			}
		} else {
			// mebiusをShiftクリックした場合
			if (event.getClick().isShiftClick() && isMebius(event.getCurrentItem())) {
				// 左枠が空いている場合はcancel
				if (event.getView().getTopInventory().getItem(0) == null) {
					event.setCancelled(true);
					event.getWhoClicked().sendMessage(ChatColor.RED + "MEBIUSへの命名は" + ChatColor.RESET + "/mebius naming <name>" + ChatColor.RED + "で行ってください。");
				}
			}
		}
	}

	// 金床配置時（ドラッグ）
	@EventHandler
	public void onDrag(InventoryDragEvent event) {
		// 金床じゃなければreturn
		Inventory inv = event.getInventory();
		if (!(inv instanceof AnvilInventory)) {
			return;
		}
		// mebiusを選択中じゃなければreturn
		ItemStack item = event.getOldCursor();
		if (!isMebius(item)) {
			return;
		}
		// mebiusを左枠に置いた場合はcancel
		for (Integer rawSlot : event.getRawSlots()) {
			if (rawSlot == event.getView().convertSlot(rawSlot) && rawSlot == 0) {
				event.setCancelled(true);
				event.getWhoClicked().sendMessage(ChatColor.RED + "MEBIUSへの命名は" + ChatColor.RESET + "/mebius naming <name>" + ChatColor.RED + "で行ってください。");
			}
		}
	}

	// Mebiusを装備しているか
	public static boolean isEquip(Player player) {
		try {
			return isMebius(player.getInventory().getHelmet());
		} catch (NullPointerException e) {
		}
		return false;
	}

	// MebiusのDisplayNameを設定
	public static boolean setName(Player player, String name) {
		if (isEquip(player)) {
			ItemStack mebius = player.getInventory().getHelmet();
			ItemMeta meta = mebius.getItemMeta();
			meta.setDisplayName(NAMEHEAD + name);
			player.sendMessage(getName(mebius) + ChatColor.RESET + "に命名しました。");
			mebius.setItemMeta(meta);
			player.getInventory().setHelmet(mebius);
			getPlayerData(player).mebius.speakForce("わーい、ありがとう！今日から僕は" + NAMEHEAD + name + ChatColor.RESET + "だ！");
			return true;
		}
		return false;
	}

	// MebiusのDisplayNameを取得
	public static String getName(ItemStack mebius) {
		try {
			if (isMebius(mebius)) {
				return mebius.getItemMeta().getDisplayName();
			}
		} catch (NullPointerException e) {
		}
		return NAMEHEAD + DEFNAME;
	}

	// Mebius用private
	// Mebius最大Lv
	private static final int LVMAX = 30;
	// 初期Name
	private static final String DEFNAME = "MEBIUS";
	// Mebius識別用の先頭Lore
	private static final List<String> LOREFIRST = Arrays.asList(
			ChatColor.RESET + "" + ChatColor.GRAY + "経験値瓶 効果2倍" + ChatColor.RED + "(整地レベル" + Integer.toString(EXPBONUS) + "未満限定)", "",
			ChatColor.RESET + "" + ChatColor.AQUA + "初心者をサポートする不思議なヘルメット。", "");
	private static final List<String> LOREFIRST2 = Arrays.asList(
			ChatColor.RESET + "", ChatColor.RESET + "" + ChatColor.AQUA + "初心者をサポートする不思議なヘルメット。", ChatColor.RESET + "" + ChatColor.AQUA + "整地により成長する。", "");
	private static final int LV = 4, TALK = 5, DEST = 6, OWNER = 8;
	private static final String NAMEHEAD = ChatColor.RESET + "" + ChatColor.GOLD + "" + ChatColor.BOLD + "";
	private static final String ILHEAD = ChatColor.RESET + "" + ChatColor.RED + "" + ChatColor.BOLD + "アイテムLv. ";
	private static final String TALKHEAD = ChatColor.RESET + "" + ChatColor.GOLD + "" + ChatColor.ITALIC + "";
	private static final String DESTHEAD = ChatColor.RESET + "" + ChatColor.GRAY + "" + ChatColor.ITALIC + "";
	private static final String OWNERHEAD = ChatColor.RESET + "" + ChatColor.DARK_GREEN + "所有者：";

	// PlayerData取得
	private static PlayerData getPlayerData(Player player) {
		return SeichiAssist.playermap.get(player.getUniqueId());
	}

	// ItemStackがMebiusか
	private static boolean isMebius(ItemStack item) {
		try {
			List<String> lore = item.getItemMeta().getLore();
			if (lore.containsAll(LOREFIRST2) || lore.containsAll(LOREFIRST)) {
				return true;
			}
		} catch (NullPointerException e) {
		}
		return false;
	}

	// 新規Mebius配布処理(初見配布時)
	public static void give(Player player) {
		ItemStack mebius = create(null, player);
		player.getInventory().setHelmet(mebius);
	}

	// 新規Mebius発見処理(採掘時)
	private static void discovery(Player player) {
		ItemStack mebius = create(null, player);
		player.sendMessage(ChatColor.RESET + "" + ChatColor.YELLOW + "" + ChatColor.BOLD + "おめでとうございます。採掘中にMEBIUSを発見しました。");
		player.sendMessage(ChatColor.RESET + "" + ChatColor.YELLOW + "" + ChatColor.BOLD + "MEBIUSはプレイヤーと共に成長するヘルメットです。");
		player.sendMessage(ChatColor.RESET + "" + ChatColor.YELLOW + "" + ChatColor.BOLD + "あなただけのMEBIUSを育てましょう！");
		Bukkit.getServer().getScheduler().runTaskLater(SeichiAssist.plugin, new Runnable() {
			public void run() {
				getPlayerData(player).mebius.speakForce("こんにちは、" + player.getName() + ChatColor.RESET + "。僕は" + getName(mebius) + ChatColor.RESET + "！これからよろしくね！");
			}
		}, 10);
		player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1f, 1f);
		if (!Util.isPlayerInventryFill(player)) {
			Util.addItem(player, mebius);
		} else {
			player.sendMessage(ChatColor.RESET + "" + ChatColor.RED + "" + ChatColor.BOLD + "所持しきれないためMEBIUSをドロップしました。");
			Util.dropItem(player, mebius);
		}
	}

	// MebiusのLvを取得
	private static int getIl(ItemStack mebius) {
		return Integer.parseInt(mebius.getItemMeta().getLore().get(LV).replace(ILHEAD, ""));
	}

	// MebiusのOwnerを取得
	private static String getOwner(ItemStack mebius) {
		return mebius.getItemMeta().getLore().get(OWNER).replaceFirst(OWNERHEAD, "");
	}

	// Mebiusレベルアップ確率テーブル
	private static final List<Integer> lvPer = Arrays.asList(
			Integer.valueOf(500), Integer.valueOf(500), Integer.valueOf(500), Integer.valueOf(500), Integer.valueOf(800), Integer.valueOf(800), Integer.valueOf(800), Integer.valueOf(800), Integer.valueOf(800), Integer.valueOf(1700),
			Integer.valueOf(1700), Integer.valueOf(1700), Integer.valueOf(1700), Integer.valueOf(1700), Integer.valueOf(1800), Integer.valueOf(1800), Integer.valueOf(1800), Integer.valueOf(1800), Integer.valueOf(1800), Integer.valueOf(2200),
			Integer.valueOf(2200), Integer.valueOf(2200), Integer.valueOf(2200), Integer.valueOf(2200), Integer.valueOf(2600), Integer.valueOf(2600), Integer.valueOf(2600), Integer.valueOf(2600), Integer.valueOf(3000), Integer.valueOf(3000));

	// MebiusLvアップ判定
	private static boolean isLevelUp(Player player) {
		int chk = new Random().nextInt(lvPer.get(getIl(player.getInventory().getHelmet()) - 1));
		if (debugFlg) {
			chk /= 100;
		}
		if (chk == 0) {
			return true;
		}
		return false;
	}

	// Mebiusドロップ率
	private static final int dropPer = 50000;

	// Mebiusドロップ判定
	private static boolean isDrop() {
		int chk = new Random().nextInt(dropPer);
		if (debugFlg) {
			chk /= 100;
		}
		if (chk == 0) {
			return true;
		}
		return false;
	}

	// Mebius更新処理
	private static void levelUp(Player player) {
		ItemStack mebius = player.getInventory().getHelmet();
		ItemMeta meta = mebius.getItemMeta();
		List<String> lore = meta.getLore();

		// 上限Lvチェック
		int level = getIl(mebius);
		if (level == LVMAX) {
			return;
		}

		// 所有者が異なる場合…名前変更でもNG
		if (!player.getName().toLowerCase().equals(getOwner(mebius))) {
			return;
		}

		// Level Up
		level += 1;
		// レベルアップ通知
		player.sendMessage(getName(mebius) + ChatColor.RESET + "がレベルアップしました。");
		// ItemStack更新レベルなら
		if (APPEARANCE.containsKey(level)) {
			// 新規アイテムに更新
			mebius = create(mebius, player);
		} else {
			// Lore更新
			updateTalkDest(lore, level);
			meta.setLore(lore);
			// エンチャント更新
			setEnchant(meta, level, player);
			mebius.setItemMeta(meta);
		}
		// 耐久を回復
		mebius.setDurability((short) 0);
		player.getInventory().setHelmet(mebius);
		// Talkを喋る
		getPlayerData(player).mebius.speakForce(getTalk(level));
	}

	// 見た目更新Lv
	private static final Map<Integer, Material> APPEARANCE = new LinkedHashMap<Integer, Material>() {
		{
			put(Integer.valueOf(1), Material.LEATHER_HELMET);
			put(Integer.valueOf(5), Material.GOLD_HELMET);
			put(Integer.valueOf(10), Material.CHAINMAIL_HELMET);
			put(Integer.valueOf(20), Material.IRON_HELMET);
			put(Integer.valueOf(25), Material.DIAMOND_HELMET);
		}
	};

	// 新しいMebiusのひな形を作る
	private static ItemStack create(ItemStack mebius, Player player) {
		String name = NAMEHEAD + DEFNAME;
		int level = 1;
		Map<Enchantment, Integer> ench = new LinkedHashMap<Enchantment, Integer>() {
			{
				put(Enchantment.DURABILITY, 3);
				put(Enchantment.MENDING, 1);
			}
		};
		// 新規作成じゃない場合、レベルを更新してMebiusの名前とエンチャントを覚えておく
		if (mebius != null) {
			level = getIl(mebius) + 1;
			name = mebius.getItemMeta().getDisplayName();
			ench = mebius.getItemMeta().getEnchants();
			// Mebiusの進化を通知する
			player.sendMessage(name + ChatColor.RESET + "の見た目が進化しました。");
		}
		mebius = new ItemStack(APPEARANCE.get(level));
		ItemMeta meta = Bukkit.getItemFactory().getItemMeta(APPEARANCE.get(level));
		meta.setDisplayName(name);
		// Lore生成
		List<String> lore = new ArrayList<String>(LOREFIRST2);
		lore.addAll(Arrays.asList(ILHEAD + Integer.toString(level), "", "", "", OWNERHEAD + player.getName().toLowerCase()));
		updateTalkDest(lore, level);
		meta.setLore(lore);
		// エンチャントを付与する
		for (Map.Entry<Enchantment, Integer> e : ench.entrySet()) {
			meta.removeEnchant(e.getKey());
			meta.addEnchant(e.getKey(), e.getValue(), true);
		}
		// フラグ設定
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		mebius.setItemMeta(meta);

		return mebius;
	}

	// レベル別Talk
	private static final List<List<String>> TALKDEST = Arrays.asList(
			Arrays.asList("こんにちは！これからよろしくねー！", "いつの間にか被っていた。"), Arrays.asList("僕のこと外さないでね？", "段々成長していくらしい。"), Arrays.asList("モンスターって怖いねえ…", "どこから喋っているのだろう。"),
			Arrays.asList("どこでもルールって大切だね。", "ちゃんと守らなきゃね。"), Arrays.asList("整地神様って知ってる？偉いんだよ！", "どうやら神様を知ってるみたい。"), Arrays.asList("知らないこと、いっぱい学びたいなぁ。", "どこに記憶しているんだろう。"),
			Arrays.asList("ゾンビっておいしいのかな？", "それだけはやめておけ。"), Arrays.asList("どこかに僕の兄弟が埋まってるんだー。", "採掘で手に入るのかな。"), Arrays.asList("…はっ！寝てないからね！？", "たまに静かだよね。"),
			Arrays.asList("スキルって気持ち良いよね！", "マナが吸い取られるけどね。"), Arrays.asList("メインワールドの探検しようよー！", "息抜きは大切だね。"), Arrays.asList("宿題も大切だよ？", "何の話をしてるんだろう…"),
			Arrays.asList("空を自由に飛びたいなー！", "はい、タケコプター！"), Arrays.asList("ジュースが飲みたいよー！", "どこから飲むつもりだろう。"), Arrays.asList("君の頭って落ち着くねぇ。", "君のお喋りにも慣れたなぁ。"),
			Arrays.asList("APOLLO様みたいになれるかな？", "どんな関係があるんだろう…"), Arrays.asList("僕って役に立つでしょー！", "静かならもっといいんだけどね。"), Arrays.asList("赤いりんごがあるらしいよ！？", "りんごは普通赤いんだよ。"),
			Arrays.asList("ヘルメット式電動耳掃除…", "何を怖いことを言っている…"), Arrays.asList("ここまで育つなんてね！", "立派になったもんだね。"), Arrays.asList("動きすぎると酔っちゃうよね。", "三半規管はあるのかな。"),
			Arrays.asList("僕は整地神様に生み出されたんだよ！", "整地神ってお喋りなのかな…"), Arrays.asList("君とドラゴンを倒す夢を見たよ…", "エンダードラゴンのことかな。"), Arrays.asList("君は僕が育てたと胸を張って言えるね！", "逆でしょう。"),
			Arrays.asList("ああー饅頭が怖いなあ！", "落語でも見た？あげないよ。"), Arrays.asList("僕にも手足があったらなー…！", "被れなくなるでしょ。"), Arrays.asList("このフィット感…着心地抜群だよね？", "もう少し静かだったらね。"),
			Arrays.asList("餃子っておいしいんだねえ！", "ニンニク臭がこもってるよ…"), Arrays.asList("君も立派になったねえ", "同じこと思ってたとこ。"), Arrays.asList("育ててくれてありがとう！", "ある意味、最強のヘルメット。"));

	// Talk更新
	private static void updateTalkDest(List<String> lore, int level) {
		for (int cnt = 0; cnt < LOREFIRST2.size(); cnt++) {
			lore.set(cnt, LOREFIRST2.get(cnt));
		}
		lore.set(LV, ILHEAD + Integer.toString(level));
		lore.set(TALK, TALKHEAD + "「" + TALKDEST.get(level - 1).get(TALK - TALK) + "」");
		lore.set(DEST, DESTHEAD + TALKDEST.get(level - 1).get(DEST - TALK));
	}

	// Talkを取得
	private static String getTalk(int level) {
		return TALKDEST.get(level - 1).get(TALK - TALK);
	}

	// エンチャント用インナークラス
	private static class Enchant {
		/** エンチャント実体 */
		public final Enchantment ench;
		/** 解放アイテムレベル */
		public final int open;
		/** 最大エンチャントレベル */
		public final int max;
		/** エンチャント名 */
		public final String name;

		public Enchant(Enchantment ench, int open, int max, String name) {
			this.ench = ench;
			this.open = open;
			this.max = max;
			this.name = name;
		}
	}

	// エンチャント別レベル制限リスト
	private static final List<Enchant> ENCHANT = new ArrayList<Enchant>(Arrays.asList(
			new Enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2, 10, "ダメージ軽減"),
			new Enchant(Enchantment.PROTECTION_FIRE, 6, 10, "火炎耐性"),
			new Enchant(Enchantment.PROTECTION_PROJECTILE, 6, 10, "飛び道具耐性"),
			new Enchant(Enchantment.PROTECTION_EXPLOSIONS, 6, 10, "爆発耐性"),
			new Enchant(Enchantment.OXYGEN, 15, 3, "水中呼吸"),
			new Enchant(Enchantment.WATER_WORKER, 15, 1, "水中採掘"),
			new Enchant(Enchantment.DURABILITY, 2, 10, "耐久力")));
	private static final String UNBREAK = ChatColor.RESET + "" + ChatColor.AQUA + "耐久無限";
	private static final List<String> ROMAN = Arrays.asList("", "", " II", " III", " IV", " V", " VI", " VII", " VIII", " IX", " X", " XI", " XII", " XIII", " XIV", " XV", " XVI", " XVII", " XVIII", " XIX", " XX");

	// エンチャント更新
	private static void setEnchant(ItemMeta meta, int level, Player player) {
		// LvMAXなら無限とLoreをセット
		if (level == LVMAX) {
			meta.spigot().setUnbreakable(true);
			List<String> lore = meta.getLore();
			lore.add(UNBREAK);
			meta.setLore(lore);
			player.sendMessage(ChatColor.RESET + "" + ChatColor.GREEN + "おめでとうございます。" + meta.getDisplayName() + ChatColor.RESET + "" + ChatColor.GREEN + "のレベルが最大になりました。");
			player.sendMessage(UNBREAK + ChatColor.RESET + "が付与されました。");
			return;
		}
		// その他はレベル別Enchantから設定
		Map<Enchantment, Integer> ench = new LinkedHashMap<Enchantment, Integer>();
		// エンチャントMapはimmutableなので移し替える
		for (Map.Entry<Enchantment, Integer> e : meta.getEnchants().entrySet()) {
			ench.put(e.getKey(), e.getValue());
		}
		Random rd = new Random();
		while (true) {
			Enchant ec = ENCHANT.get(rd.nextInt(ENCHANT.size()));
			// 解放レベル以上
			if (level >= ec.open) {
				int ecLv = 0;
				// 未取得または上り幅がある時
				if (!ench.containsKey(ec.ench) || (ecLv = ench.get(ec.ench)) < ec.max) {
					// エンチャントを加算
					ench.put(ec.ench, ecLv + 1);
					// メッセージを生成
					if (ecLv == 0) {
						player.sendMessage(ChatColor.GRAY + ec.name + ChatColor.RESET + "が付与されました。");
					} else {
						player.sendMessage(ChatColor.GRAY + ec.name + ROMAN.get(ecLv) + ChatColor.RESET + "が" + ChatColor.GRAY + ec.name + ROMAN.get(ecLv + 1) + ChatColor.RESET + "に強化されました。");
					}
					// 古いエンチャントを消去し新しいエンチャントを付与する
					for (Map.Entry<Enchantment, Integer> e : ench.entrySet()) {
						meta.removeEnchant(e.getKey());
						meta.addEnchant(e.getKey(), e.getValue(), true);
					}
					// ループ終了
					break;
				}
			}
		}
	}

	// Mebius Tips
	private static final List<String> MTIPS = Arrays.asList(
			"僕の名前は、/mebius naming <名前> コマンドで変更できるよ！<名前>の代わりに新しい名前を入れてね！",
			"僕は整地によって成長するんだー。アイテムレベル30まであるんだよ！",
			"僕たち兄弟のステータスはみんなバラバラなんだよー！",
			"僕たちはこの世界のどこかに埋まってるんだー。整地して僕の兄弟も見つけて欲しいな！");

	// webからTipsリスト読み込み
	private static void loadTips() {
		try {
			// HTTP通信でJSONデータを取得
			URL url = new URL("http://seichi.click/d/Tips");
			URLConnection urlCon = url.openConnection();
			// 403回避のためユーザーエージェントを登録
			urlCon.setRequestProperty("User-Agent", "Mebius");
			InputStream in = urlCon.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, "EUC-JP"));
			String line;
			// Tips先頭まで読み込み
			while ((line = reader.readLine()) != null) {
				if (line.contains("<ul id=\"content_block_2\" class=\"list-1\">")) {
					break;
				}
			}
			// Tipsを読み込み
			while ((line = reader.readLine()) != null) {
				if (line.contains("</ul>")) {
					break;
				} else {
					tips.add(line.replace("<li> ", "").replace("</li>", ""));
				}
			}
			reader.close();
			in.close();
		} catch (Exception e) {
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Tipsの読み込みに失敗");
		}
		tips.addAll(MTIPS);
	}

	// メッセージリストからランダムに取り出し、タグを置換する
	private static String getMessage(List<String> messages, String str1, String str2) {
		String msg = messages.get(new Random().nextInt(messages.size()));
		if (!str1.isEmpty()) {
			msg = msg.replace("[str1]", str1 + ChatColor.RESET);
		}
		if (!str2.isEmpty()) {
			msg = msg.replace("[str2]", str2 + ChatColor.RESET);
		}
		return msg;
	}
}
