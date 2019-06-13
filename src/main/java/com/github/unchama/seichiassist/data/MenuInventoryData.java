package com.github.unchama.seichiassist.data;

import com.github.unchama.seasonalevents.events.valentine.Valentine;
import com.github.unchama.seichiassist.*;
import com.github.unchama.seichiassist.database.DatabaseGateway;
import com.github.unchama.seichiassist.minestack.HistoryData;
import com.github.unchama.seichiassist.minestack.MineStackObj;
import com.github.unchama.seichiassist.util.exp.ExperienceManager;
import com.github.unchama.seichiassist.util.external.ExternalPlugins;
import com.github.unchama.seichiassist.util.TypeConverter;
import com.github.unchama.seichiassist.util.Util;
import com.github.unchama.seichiassist.util.external.WorldGuard;
import com.github.unchama.seichiassist.util.AsyncInventorySetter;
import com.github.unchama.seichiassist.task.GiganticBerserkTask;
import com.github.unchama.seichiassist.task.VotingFairyTask;
import com.github.unchama.util.collection.ImmutableListFactory;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
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

import java.util.*;

public class MenuInventoryData {
	private static HashMap<UUID, PlayerData> playermap = SeichiAssist.playermap;
	private static DatabaseGateway databaseGateway = SeichiAssist.databaseGateway;
	private static WorldGuardPlugin Wg = ExternalPlugins.getWorldGuard();

	//二つ名組合せシステム用
	private static boolean nextpageflag1 = false ;
	private static boolean nextpageflag2 = false ;
	private static boolean nextpageflag3 = false ;
	private static boolean nextpageflagS = false ;
	private static int checkTitle1 = 0 ;
	private static int checkTitle2 = 0 ;
	private static int checkTitle3 = 0 ;
	private static int checkTitleS = 0 ;
	private static int NoKeep = 0;

	//1ページ目メニュー
	public static Inventory getMenuData(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if (sendWarningToLogger(p, playerdata)) return null;

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "木の棒メニュー");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore = new ArrayList<>();



		int prank = playerdata.calcPlayerRank(player);
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + playerdata.getName() + "の統計データ");
		lore.clear();
		if(playerdata.getStarlevel() <= 0){
			lore.add(ChatColor.RESET + "" +  ChatColor.AQUA + "整地レベル:" + playerdata.getLevel());
		}else{
			lore.add(ChatColor.RESET + "" +  ChatColor.AQUA + "整地レベル:" + playerdata.getLevel() + "☆" + playerdata.getStarlevel());
		}
		if(playerdata.getLevel() < LevelThresholds.INSTANCE.getLevelExpThresholds().size()){
			lore.add(ChatColor.RESET + "" +  ChatColor.AQUA + "次のレベルまで:" + (LevelThresholds.INSTANCE.getLevelExpThresholds().get(playerdata.getLevel()) - playerdata.getTotalbreaknum()));
		}
		//整地ワールド外では整地数が反映されない
		if(!Util.isSeichiWorld(p)){
			lore.add(ChatColor.RESET + "" +  ChatColor.RED + "整地ワールド以外では");
			lore.add(ChatColor.RESET + "" +  ChatColor.RED + "整地量とガチャ券は増えません");
		}
		lore.addAll(Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "パッシブスキル効果："
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "1ブロック整地ごとに"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "10%の確率で"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + playerdata.dispPassiveExp() + "のマナを獲得"
				, ChatColor.RESET + "" +  ChatColor.AQUA + "総整地量:" + playerdata.getTotalbreaknum()
				, ChatColor.RESET + "" +  ChatColor.GOLD + "ランキング：" + prank + "位" + ChatColor.RESET + "" +  ChatColor.GRAY + "(" + SeichiAssist.ranklist.size() +"人中)"
				));
		if(prank > 1){
			RankData rankdata = SeichiAssist.ranklist.get(prank-2);
			lore.add(ChatColor.RESET + "" +  ChatColor.AQUA + (prank-1) + "位("+ rankdata.name +")との差：" + (rankdata.totalbreaknum - playerdata.getTotalbreaknum()));
		}
		lore.add(ChatColor.RESET + "" +  ChatColor.GRAY + "総ログイン時間：" + TypeConverter.toTimeString(TypeConverter.toSecond(playerdata.getPlaytick())));
		lore.add(ChatColor.RESET + "" +  ChatColor.GRAY + "通算ログイン日数：" + playerdata.getTotalJoin() + "日");
		lore.add(ChatColor.RESET + "" +  ChatColor.GRAY + "連続ログイン日数：" + playerdata.getChainJoin() + "日");
		if(playerdata.getChainVote() > 0){
			lore.add(ChatColor.RESET + "" +  ChatColor.GRAY + "連続投票日数：" + playerdata.getChainVote() + "日");
		}
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "※1分毎に更新");
		lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "統計データは");
		lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "各サバイバルサーバー間で");
		lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "共有されます");

		//ExpBar用ダミー
		lore.add("");
		lore.add("");
		lore.add("");

		skullmeta.setLore(lore);
		skullmeta.setOwningPlayer(Bukkit.getOfflinePlayer(playerdata.getUuid()));
		//ExpBarトグル追加
		itemstack.setItemMeta(dispExpBarToggleMeta(playerdata,skullmeta));
		AsyncInventorySetter.setItemAsync(inventory,0,itemstack.clone());

		//採掘速度上昇効果のトグルボタン
		itemstack = new ItemStack(Material.DIAMOND_PICKAXE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_PICKAXE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "採掘速度上昇効果");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemstack.setItemMeta(EFButtonMeta(playerdata,itemmeta));
		inventory.setItem(1,itemstack);

		// ver0.3.2 四次元ポケットOPEN
		itemstack = new ItemStack(Material.ENDER_PORTAL_FRAME,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.ENDER_PORTAL_FRAME);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "4次元ポケットを開く");
		lore.clear();
		if( playerdata.getLevel() < SeichiAssist.config.getPassivePortalInventorylevel()){
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "整地レベルが"+SeichiAssist.config.getPassivePortalInventorylevel()+ "以上必要です");
		}else{
			lore.add(ChatColor.RESET + "" +  ChatColor.GRAY + "ポケットサイズ:" + playerdata.getInventory().getSize() + "スタック");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックで開く");
		}
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "※4次元ポケットの中身は");
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "各サバイバルサーバー間で");
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "共有されます");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(21,itemstack);

		// どこでもエンダーチェスト
		itemstack = new ItemStack(Material.ENDER_CHEST,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.ENDER_CHEST);
		itemmeta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "どこでもエンダーチェスト");
		lore.clear();
		if( playerdata.getLevel() < SeichiAssist.config.getPassivePortalInventorylevel()){
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "整地レベルが"+SeichiAssist.config.getDokodemoEnderlevel()+ "以上必要です");
		}else{
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックで開く");
		}
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(22,itemstack);

		// MineStackを開く
		itemstack = new ItemStack(Material.CHEST,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.CHEST);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "MineStack機能");
		lore.clear();
		lore.addAll(Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "説明しよう!MineStackとは…"
				, ChatColor.RESET + "" + "主要アイテムを無限にスタック出来る!"
				, ChatColor.RESET + "" + "スタックしたアイテムは"
				, ChatColor.RESET + "" + "ここから取り出せるゾ!"
				));
		if( playerdata.getLevel() < SeichiAssist.config.getMineStacklevel(1)){
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "整地レベルが"+SeichiAssist.config.getMineStacklevel(1)+ "以上必要です");
		}else{
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックで開く");
		}
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "※スタックしたアイテムは");
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "各サバイバルサーバー間で");
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "共有されます");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(24,itemstack);

		/*
		 * ここまでadd.loreに変更済み
		 * 以下ボタンにadd.lore使う場合は追加行より上をすべてadd.loreに変更しないとエラー吐きます
		 */

		//スターレベル情報
		//次の☆までの残量計算(整地量)
		long LongSLB = playerdata.getStarlevel_Break();
		long NextStar_Break = ((LongSLB + 1) * 87115000) - playerdata.getTotalbreaknum();

		itemstack = new ItemStack(Material.GOLD_INGOT,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GOLD_INGOT);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "スターレベル情報" );
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.AQUA + ChatColor.BOLD + "整地量：☆" + playerdata.getStarlevel_Break()
							,ChatColor.RESET + "" +  ChatColor.AQUA +  "次の☆まで：あと" + NextStar_Break
							,ChatColor.RESET + "" +  ChatColor.GREEN + ChatColor.UNDERLINE + ChatColor.BOLD + "合計：☆" + playerdata.getStarlevel());
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(10,itemstack);

		// 2ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "2ページ目へ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowRight");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,35,itemstack.clone());

		// 整地神番付を開く
		itemstack = new ItemStack(Material.COOKIE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COOKIE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地神ランキングを見る");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "(整地レベル100以上のプレイヤーのみ表記されます)"
				,ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(32,itemstack);

		// 整地神番付を開く
		itemstack = new ItemStack(Material.COOKIE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COOKIE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ログイン神ランキングを見る");
		/*
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "(整地レベル100以上のプレイヤーのみ表記されます)"
				,ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く"
				);
		*/
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(33,itemstack);

		// 整地神番付を開く
		itemstack = new ItemStack(Material.COOKIE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COOKIE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "投票神ランキングを見る");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "(投票しているプレイヤーのみ表記されます)"
				,ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く"
				);
		/*
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く");
		*/
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(34,itemstack);

		//運営からの詫びガチャ配布ボタン
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.DARK_AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "運営からのガチャ券を受け取る");
		skullmeta.setLore(SorryGachaGetButtonLore(playerdata));
		skullmeta.setOwner("whitecat_haru");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,29,itemstack.clone());

		// 不要ガチャ景品交換システムを開く
		itemstack = new ItemStack(Material.NOTE_BLOCK,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.NOTE_BLOCK);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "不要ガチャ景品交換システム");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "不必要な当たり、大当たり景品を"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "ガチャ券と交換出来ます"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "出てきたインベントリ―に"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "交換したい景品を入れて"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "escキーを押してください"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "たまにアイテムが消失するから"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "大事なものはいれないでネ"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(16,itemstack);

		itemstack = new ItemStack(Material.BED,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BED);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームメニューを開く");
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "ホームポイントに関するメニュー"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(18,itemstack);

		itemstack = new ItemStack(Material.COMPASS,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COMPASS);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ランダムテレポート(β)");
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "整地ワールドで使うと、良さげな土地にワープします"
				, ChatColor.RESET + "" + ChatColor.GRAY + "βテスト中のため、謎挙動にご注意ください"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで発動"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "command->[/rtp]"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(19,itemstack);

		// fastcraftリンク
		itemstack = new ItemStack(Material.WORKBENCH,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.WORKBENCH);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "FastCraft機能");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く"
				, ChatColor.RESET + "" +  ChatColor.RED + "ただの作業台じゃないんです…"
				, ChatColor.RESET + "" +  ChatColor.YELLOW + "自動レシピ補完機能付きの"
				, ChatColor.RESET + "" +  ChatColor.YELLOW + "最強な作業台はこちら"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "command->[/fc craft]"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(23,itemstack);

		// ver0.9.24 サーバー間移動
		itemstack = new ItemStack(Material.NETHER_STAR,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.NETHER_STAR);
		itemmeta.setDisplayName(ChatColor.RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "サーバー間移動メニュー");
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "・各サバイバルサーバー"
				, ChatColor.RESET + "" + ChatColor.GRAY + "・各整地専用サーバー"
				, ChatColor.RESET + "" + ChatColor.GRAY + "・公共施設サーバー"
				, ChatColor.RESET + "" + ChatColor.GRAY + "間を移動する時に使います"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックして開く"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(7,itemstack);

		// ver0.3.2 /spawnコマンド実行
		itemstack = new ItemStack(Material.BEACON,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEACON);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "スポーンワールドへワープ");
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "・メインワールド"
				, ChatColor.RESET + "" + ChatColor.GRAY + "・整地ワールド"
				, ChatColor.RESET + "" + ChatColor.GRAY + "間を移動する時に使います"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックするとワープします"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "command->[/spawn]"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(8,itemstack);


		//実績画面
		itemstack = new ItemStack(Material.END_CRYSTAL,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.END_CRYSTAL);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績・二つ名システム");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GRAY + "様々な実績に挑んで、"
							, ChatColor.RESET + "" +  ChatColor.GRAY + "いろんな二つ名を手に入れよう！"
							, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで設定画面へ移動");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(9,itemstack);


		//パッシブスキルツリー
		itemstack = new ItemStack(Material.ENCHANTED_BOOK,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.ENCHANTED_BOOK);
		itemmeta.addEnchant(Enchantment.DURABILITY, 100, false);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "パッシブスキルブック");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GRAY + "整地に便利なスキルを使用できるゾ"
										, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでスキル一覧を開く");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(11,itemstack);


		//アクティブスキルツリー
		itemstack = new ItemStack(Material.ENCHANTED_BOOK,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.ENCHANTED_BOOK);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "アクティブスキルブック");
		//整地ワールド外では整地スキルが発動しない
		if(!Util.isSkillEnable(p)){
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "このワールドでは"
					,ChatColor.RESET + "" +  ChatColor.RED + "整地スキルを使えません");
		}else{
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GRAY + "整地に便利なスキルを使用できるゾ"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでスキル一覧を開く");
		}
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(13,itemstack);



		//ガチャ券受け取りボタン
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		itemstack.setDurability((short) 3);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		skullmeta.setDisplayName(ChatColor.DARK_AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地報酬ガチャ券を受け取る");
		skullmeta.setLore(GachaGetButtonLore(playerdata));
		skullmeta.setOwner("unchama");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,27,itemstack.clone());

		//ガチャ券受け取り方法選択ボタン
		itemstack = new ItemStack(Material.STONE_BUTTON,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STONE_BUTTON);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地報酬ガチャ券受け取り方法");
		if(playerdata.getGachaflag()){
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "毎分受け取ります"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変更"
					);
		}else {
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "後でまとめて受け取ります"
					, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変更"
					);
		}
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(28,itemstack);

		//鉱石・交換券変換システムを開く
		itemstack = new ItemStack(Material.DIAMOND_ORE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_ORE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "鉱石・交換券変換システム");
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + "不必要な各種鉱石を"
				, ChatColor.RESET + "" + ChatColor.DARK_RED + "交換券" + ChatColor.RESET + ChatColor.GREEN + "と交換できます"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "出てきたインベントリ―に"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "交換したい鉱石を入れて"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "escキーを押してください"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "たまにアイテムが消失するから"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "大事なものはいれないでネ"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(17,itemstack);

		//保護関連メニュー
		RegionManager manager = Wg.getRegionManager(player.getWorld());

		List<String> lore3 = Arrays.asList(ChatColor.DARK_GRAY + "土地の保護が行えます"
			, ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く"
			, ChatColor.GRAY + "保護作成上限：" + ChatColor.AQUA + WorldGuard.getMaxRegionCount(player, player.getWorld())
			, ChatColor.GRAY + "現在のあなたの保護作成数：" + ChatColor.AQUA + manager.getRegionCountOfPlayer(Wg.wrapPlayer(player))
		);
		ItemStack icon3 = Util.getMenuIcon(Material.DIAMOND_AXE, 1,
				ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "土地保護メニュー", lore3, true);
		inventory.setItem(3, icon3);

		//投票ptメニュー
		itemstack = new ItemStack(Material.DIAMOND,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND);
		itemmeta.addEnchant(Enchantment.DURABILITY, 100, false);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "投票ptメニュー");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.GREEN + "投票ptに関することはこちらから！");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(30,itemstack);

		//5個目に一時的にチョコをゲットできるボタンを作る。TODO:ましなやり方に変えたい
		if (!playerdata.getHasChocoGave()) {
			if (Valentine.isInEvent) {
				itemstack = new ItemStack(Material.TRAPPED_CHEST);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.CHEST);
				itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
				itemmeta.setDisplayName("プレゼントボックス");
				lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.RED + "<バレンタインイベント記念>",
						ChatColor.RESET + "" + ChatColor.AQUA + "記念品として",
						ChatColor.RESET + "" + ChatColor.GREEN + "チョコチップクッキー×64個",
						ChatColor.RESET + "" + ChatColor.AQUA + "を配布します。",
						ChatColor.RESET + "" + ChatColor.DARK_RED + ChatColor.UNDERLINE + ChatColor.BOLD + "クリックで受け取る");
				itemmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(5, itemstack);
			}
		}

		return inventory;
	}
	//2ページメニュー
	public static Inventory getMenuData2(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();

		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);

		//経験値変更用のクラスを設定
		ExperienceManager expman = new ExperienceManager(player);

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "木の棒メニュー");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore = new ArrayList<>();

		// 自分の頭召喚
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "自分の頭を召喚");
		lore.clear();
		lore.addAll(Arrays.asList(ChatColor.RESET + "" +  ChatColor.GRAY + "経験値10000を消費して"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "自分の頭を召喚します"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "装飾用にドウゾ！"
				));
		if(expman.hasExp(10000)){
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックで召喚");
		}else{
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "経験値が足りません");
		}
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_Villager");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,12,itemstack.clone());

		//全体通知のトグルボタン
		itemstack = new ItemStack(Material.JUKEBOX,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.JUKEBOX);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "全体通知切替");
		itemstack.setItemMeta(dispWinSoundToggleMeta(playerdata,itemmeta));
		inventory.setItem(13,itemstack);

		//死亡メッセージ表示のトグルボタン
		itemstack = new ItemStack(Material.FLINT_AND_STEEL,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.FLINT_AND_STEEL);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "死亡メッセージ表示切替");
		itemstack.setItemMeta(dispKillLogToggleMeta(playerdata,itemmeta));
		inventory.setItem(14,itemstack);

		//ワールドガード保護表示のトグルボタン
		itemstack = new ItemStack(Material.BARRIER,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BARRIER);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ワールドガード保護メッセージ表示切替");
		//itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemstack.setItemMeta(dispWorldGuardLogToggleMeta(playerdata,itemmeta));
		inventory.setItem(15,itemstack);


		// ver0.3.2 hubコマンド
		itemstack = new ItemStack(Material.NETHER_STAR,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.NETHER_STAR);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ロビーサーバーへ移動");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックすると移動します"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "command->[/hub]"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(8,itemstack);

		/*
		 * ここまでadd.loreに変更済み
		 * 以下ボタンにadd.lore使う場合は追加行より上をすべてadd.loreに変更しないとエラー吐きます
		 */

		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "1ページ目へ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,27,itemstack.clone());

		// ver0.3.2 wikiページ表示
		itemstack = new ItemStack(Material.BOOK,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BOOK);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "公式Wikiにアクセス");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "鯖内の「困った」は公式Wikiで解決！"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "クリックするとチャット欄に"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "URLが表示されますので"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "Tキーを押してから"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "そのURLをクリックしてください"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(0,itemstack);

		// ver0.3.2 運営方針とルールページを表示
		itemstack = new ItemStack(Material.PAPER,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.PAPER);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "運営方針とルールを確認");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "当鯖で遊ぶ前に確認してネ！"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "クリックするとチャット欄に"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "URLが表示されますので"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "Tキーを押してから"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "そのURLをクリックしてください"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(1,itemstack);

		// ver0.3.2 鯖Mapを表示
		itemstack = new ItemStack(Material.MAP,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.MAP);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "鯖Mapを見る");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "webブラウザから鯖Mapを閲覧出来ます"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "他人の居場所や保護の場所を確認出来ます"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "クリックするとチャット欄に"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "URLが表示されますので"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "Tキーを押してから"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "そのURLをクリックしてください"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(2,itemstack);

		// ver0.3.2 掲示板を表示
		itemstack = new ItemStack(Material.SIGN,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.SIGN);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "JapanMinecraftServerリンク");
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.DARK_GRAY + "クリックするとチャット欄に"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "URLが表示されますので"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "Tキーを押してから"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "そのURLをクリックしてください"
				);
		//現在掲示板は存在しないので、名前を変更して「JapanMinecraftServerリンク」とする
		//消去部分
		//Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "管理人へのお問い合わせは"
		//, ChatColor.RESET + "" +  ChatColor.GREEN + "掲示板に書き込みをｵﾈｶﾞｲｼﾅｽ"
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(3,itemstack);

		//椎名林檎変換システムを開く
		itemstack = new ItemStack(Material.GOLDEN_APPLE,1);
		itemstack.setDurability((short) 1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GOLDEN_APPLE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "GT景品→椎名林檎変換システム");
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + "不必要なGT大当り景品を"
				, ChatColor.RESET + "" + ChatColor.GOLD + "椎名林檎" + ChatColor.RESET + ChatColor.GREEN + "と交換できます"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "出てきたインベントリーに"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "交換したい景品を入れて"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "escキーを押してください"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "たまにアイテムが消失しますが"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "補償はしていません(ごめんなさい)"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "神に祈りながら交換しよう"
				, ChatColor.RESET + "現在の交換レート:GT景品1つにつき" + SeichiAssist.config.rateGiganticToRingo() + "個"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(35,itemstack);

		//限定タイタン変換システムを開く
		itemstack = new ItemStack(Material.DIAMOND_AXE,1);
		itemstack.setDurability((short) 1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_AXE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "限定タイタン修繕システム");
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + "不具合によりテクスチャが反映されなくなってしまった"
				, ChatColor.RESET + "" + ChatColor.GOLD + "ホワイトデーイベント限定タイタン" + ChatColor.RESET + ChatColor.GREEN + "を修繕できます"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "出てきたインベントリーに"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "修繕したいタイタンを入れて"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "escキーを押してください"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "たまにアイテムが消失しますが"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "補償はしていません(ごめんなさい)"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "神に祈りながら交換しよう"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く"
				);
		itemmeta.setLore(lore);
		itemmeta.spigot().setUnbreakable(true);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(34,itemstack);

		// インベントリ共有ボタン
		itemstack = new ItemStack(Material.TRAPPED_CHEST,1);
		itemstack.setItemMeta(dispShareInvMeta(playerdata));
		inventory.setItem(6,itemstack);

		//ゴミ箱
		itemstack = new ItemStack(Material.BUCKET,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BUCKET);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ゴミ箱を開く");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "不用品の大量処分にドウゾ！"
				, ChatColor.RESET + "" + ChatColor.RED + "復活しないので取扱注意"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで開く"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(30,itemstack);

		return inventory;
	}
	//パッシブスキルメニュー
	public static Inventory getPassiveSkillMenuData(Player p){

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "整地スキル切り替え");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore;

		//プレイヤーを取得
		Player player = p.getPlayer();

		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);

		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,27,itemstack.clone());

		//複数種類同時破壊スキルのトグルボタン
		itemstack = new ItemStack(Material.DIAMOND_PICKAXE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_PICKAXE);
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "複数種類同時破壊スキル切替");
		//itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(MultipleIDBlockBreakToggleMeta(playerdata,itemmeta));
		inventory.setItem(0,itemstack);

		//Chest破壊
		if(playerdata.getChestflag()){
			itemstack = new ItemStack(Material.DIAMOND_AXE,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_AXE);
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		}else {
			itemstack = new ItemStack(Material.CHEST,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.CHEST);
		}
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "チェスト破壊スキル切替");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(ChestBreakToggleMeta(playerdata,itemmeta));
		inventory.setItem(1,itemstack);

		//GiganticBerserk
		//10レベ未満のプレイヤーはスキル未解放
		if(playerdata.getLevel() < 10){
			itemstack = new ItemStack(Material.STICK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STICK);
		}else {
			switch(playerdata.getGBstage()){
			case 0:
				itemstack = new ItemStack(Material.WOOD_SWORD,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.WOOD_SWORD);
				break;
			case 1:
				itemstack = new ItemStack(Material.STONE_SWORD,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STONE_SWORD);
				break;
			case 2:
				itemstack = new ItemStack(Material.GOLD_SWORD,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GOLD_SWORD);
				break;
			case 3:
				itemstack = new ItemStack(Material.IRON_SWORD,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.IRON_SWORD);
				break;
			case 4:
				itemstack = new ItemStack(Material.DIAMOND_SWORD,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_SWORD);
				break;
			default:
				itemstack = new ItemStack(Material.STICK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.STICK);
			}
		}
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "Gigantic" + ChatColor.RED + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "Berserk");

		if (playerdata.isGBStageUp() || (playerdata.getGBstage() == 4 && playerdata.getGBlevel() == 9)){
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		}
		itemmeta.setLore(lore);
		itemstack.setItemMeta(GiganticBerserkMeta(playerdata,itemmeta));
		inventory.setItem(9,itemstack);

		return inventory;
	}

	// 採掘速度トグルボタン
	public static ItemMeta EFButtonMeta(PlayerData playerdata,ItemMeta itemmeta){
		List<String> lore = new ArrayList<>();
		if (playerdata.getEffectflag() == 0) {
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "現在有効です(無制限)");
			lore.add(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで127制限");
		} else if (playerdata.getEffectflag() == 1) {
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "現在有効です" + ChatColor.YELLOW + "(127制限)");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで200制限");
		} else if(playerdata.getEffectflag() == 2) {
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "現在有効です" + ChatColor.YELLOW + "(200制限)");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで400制限");
		} else if(playerdata.getEffectflag() == 3) {
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "現在有効です" + ChatColor.YELLOW + "(400制限)");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで600制限");
		} else if(playerdata.getEffectflag() == 4) {
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "現在有効です" + ChatColor.YELLOW + "(600制限)");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでOFF");
		} else {
			itemmeta.removeEnchant(Enchantment.DIG_SPEED);
			lore.add(ChatColor.RESET + "" +  ChatColor.RED + "現在OFFです");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックで無制限");
		}
		lore.addAll(
				Arrays.asList(ChatColor.RESET + "" +  ChatColor.GRAY + "採掘速度上昇効果とは"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "接続人数と1分間の採掘量に応じて"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "採掘速度が変化するシステムです"
				, ChatColor.RESET + "" +  ChatColor.GOLD + "現在の採掘速度上昇Lv：" + (playerdata.getMinespeedlv() +1)
				, ChatColor.RESET + "" +  ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "上昇量の内訳"
				));
		for(EffectData ed : playerdata.getEffectdatalist()){
			lore.add(ChatColor.RESET + "" +  ChatColor.RED + "" + ed.EDtoString(ed.id,ed.duration,ed.amplifier));
		}
		itemmeta.setLore(lore);
		return itemmeta;
	}
	// ガチャ券受け取りボタン
	public static List<String> GachaGetButtonLore(PlayerData playerdata){
		List<String> lore;
		int gachaget = playerdata.getGachapoint() /1000;
		if(gachaget != 0){
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.AQUA + "未獲得ガチャ券：" + gachaget + "枚"
			, ChatColor.RESET + "" +  ChatColor.AQUA + "次のガチャ券まで:" + (1000 - playerdata.getGachapoint() %1000) + "ブロック");
		}else{
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "獲得できるガチャ券はありません"
			, ChatColor.RESET + "" +  ChatColor.AQUA + "次のガチャ券まで:" + (1000 - playerdata.getGachapoint() %1000) + "ブロック");
		}
		return lore;
	}
	//運営ガチャ券受け取りボタン
	public static List<String> SorryGachaGetButtonLore(PlayerData playerdata){
		List<String> lore = new ArrayList<>();
		lore.addAll(Arrays.asList(ChatColor.RESET + "" +  ChatColor.GRAY + "運営からのガチャ券を受け取ります"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "以下の場合に配布されます"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "・各種不具合のお詫びとして"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "・イベント景品として"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "・各種謝礼として"));
		int gachaget = playerdata.getNumofsorryforbug();
		if(gachaget != 0){
			lore.add(ChatColor.RESET + "" +  ChatColor.AQUA + "未獲得ガチャ券：" + gachaget + "枚");
		}else{
			lore.add(ChatColor.RESET + "" +  ChatColor.RED + "獲得できるガチャ券はありません");
		}
		return lore;
	}

	//投票特典受け取りボタン
	public static List<String> VoteGetButtonLore(PlayerData playerdata){
		List<String> lore = new ArrayList<>();
		lore.addAll(Arrays.asList(ChatColor.RESET + "" +  ChatColor.GRAY + "投票特典を受け取るには"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "投票ページで投票した後"
				, ChatColor.RESET + "" +  ChatColor.GRAY + "このボタンをクリックします"));
		lore.add(ChatColor.RESET + "" +  ChatColor.AQUA + "特典受取済投票回数：" + playerdata.getP_givenvote());
		lore.add(ChatColor.RESET + "" +  ChatColor.AQUA + "所有投票pt：" + playerdata.getActiveskilldata().effectpoint);
		return lore;
	}

	//Minestackメインページ
	public static Inventory getMineStackMainMenu(Player p) {
		PlayerData pd = SeichiAssist.playermap.get(p.getUniqueId());

		Inventory inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "MineStackメインメニュー");
		ItemStack itemstack = new ItemStack(Material.SKULL_ITEM,1);
		ItemMeta itemmeta;
		List<String> lore = new ArrayList<>();

		itemstack = new ItemStack(Material.IRON_PICKAXE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.IRON_PICKAXE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "対象ブロック自動スタック機能");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemstack.setItemMeta(MineStackToggleMeta(pd,itemmeta));
		inventory.setItem(0,itemstack);

		itemstack = new ItemStack(Material.DIAMOND_ORE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_ORE);
		//itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "鉱石系アイテム");
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(1,itemstack);


		itemstack = new ItemStack(Material.ENDER_PEARL,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.ENDER_PEARL);
		//itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ドロップ系アイテム");
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(2,itemstack);


		itemstack = new ItemStack(Material.SEEDS,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.SEEDS);
		//itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "農業・食料系アイテム");
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(3,itemstack);


		itemstack = new ItemStack(Material.SMOOTH_BRICK,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.SMOOTH_BRICK);
		//itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "建築系アイテム");
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(4,itemstack);


		itemstack = new ItemStack(Material.REDSTONE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.REDSTONE);
		//itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "レッドストーン・移動系アイテム");
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(5,itemstack);


		itemstack = new ItemStack(Material.GOLDEN_APPLE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GOLDEN_APPLE);
		//itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemmeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ガチャ品");
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(6,itemstack);


		itemstack = new ItemStack(Material.COMPASS);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COMPASS);
		itemmeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "アイテム検索");
		List<String> lore2 = Arrays.asList(ChatColor.GRAY + "MineStack内のアイテムを検索できます."
				,ChatColor.GRAY + "クリックした後チャット欄に"
				,ChatColor.GRAY + "アイテム名を" + ChatColor.RED + "" + ChatColor.UNDERLINE + "日本語で"
				,ChatColor.GRAY + "入力してください."
				,ChatColor.RED + "" + ChatColor.UNDERLINE + "未実装ナリよ");
		itemmeta.setLore(lore2);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(8, itemstack);

		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		itemstack.setDurability((short) 3);
		SkullMeta skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);

		// 1ページ目を開く
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ");
		lore.clear();
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,45,itemstack.clone());

		List<HistoryData> history = pd.getHisotryData().getHistoryList();
		int slot = 18;
		for (HistoryData data : history) {

			MineStackObj obj = data.obj;
			final long objectAmount = pd.getMinestack().getStackedAmountOf(obj);
			if (obj.getItemStack() == null) {
				setMineStackButton(inventory, objectAmount, new ItemStack(obj.getMaterial(), 1, (short)obj.getDurability()), SeichiAssist.config.getMineStacklevel(obj.getLevel()), slot, obj.getJapaneseName());
			} else {
				setMineStackButton(inventory, objectAmount, obj.getItemStack(), SeichiAssist.config.getMineStacklevel(obj.getLevel()), slot, obj.getJapaneseName());
			}
			slot++;
		}
		return inventory;
	}

	//Minestack全ページ(切り替え式)
	public static Inventory getMineStackMenu(Player p, int page, int stack_type){
		int minestack_stacktype_size=0;
		for(int i = 0; i< MineStackObjectList.INSTANCE.getMinestacklist().size(); i++){
			if(MineStackObjectList.INSTANCE.getMinestacklist().get(i).getStacktype()==stack_type){
				minestack_stacktype_size++;
			}
		}

		//現在の最大ページ数を取得(1ページ=0,2ページ=1,...)
		int maxpage = (minestack_stacktype_size + 1) / 45;
		if((minestack_stacktype_size + 1) % 45 == 0){
			maxpage--;
		}

		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);

		//インベントリ作成
		//Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "MineStack");
		Inventory inventory = null;
		if(stack_type==0){
			inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "鉱石系MineStack");
		} else if(stack_type==1){
			inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "ドロップ系MineStack");
		} else if(stack_type==2){
			inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "農業・食料系MineStack");
		} else if(stack_type==3){
			inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "建築系MineStack");
		} else if(stack_type==4){
			inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "レッドストーン・移動系MineStack");
		} else if(stack_type==5){
			inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "ガチャ系MineStack");
		}

		ItemStack itemstack;
		ItemMeta itemmeta;

		if(page==0){
		//MineStack機能のトグルボタン
		itemstack = new ItemStack(Material.IRON_PICKAXE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.IRON_PICKAXE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "対象ブロック自動スタック機能");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemstack.setItemMeta(MineStackToggleMeta(playerdata,itemmeta));
		inventory.setItem(0,itemstack);
		}

		//1から

		int start=0;
		if(page==0){
			start=1;
		}
		int max;
		if(page==maxpage){
			max = (minestack_stacktype_size+1)%45;
			if(max == 0){
				max = 45;
			}
		} else {
			max = 45;
		}

		int i=start;
		int ii_temp=0;
		int iii=0;
		int ii = start + page*45 - 1;
		while(ii_temp<ii){
			if(MineStackObjectList.INSTANCE.getMinestacklist().get(iii).getStacktype()!=stack_type){//対象外
				iii++;
			} else {
				iii++;
				ii_temp++;
			}
		}

		while(i<max){
			while(MineStackObjectList.INSTANCE.getMinestacklist().get(iii).getStacktype()!=stack_type){
				iii++;
			}
			//この時点で「stack_typeのii番目」のインデックスになっている
			MineStackObj msobj = MineStackObjectList.INSTANCE.getMinestacklist().get(iii);
			final long objectAmount = playerdata.getMinestack().getStackedAmountOf(msobj);
			if(msobj.getItemStack()==null){
				setMineStackButton(inventory, objectAmount, new ItemStack(msobj.getMaterial(), 1, (short)msobj.getDurability()),  SeichiAssist.config.getMineStacklevel(msobj.getLevel()), i, msobj.getJapaneseName());
				iii++;
			} else {
				setMineStackButton(inventory, objectAmount, msobj.getItemStack(), SeichiAssist.config.getMineStacklevel(msobj.getLevel()), i, msobj.getJapaneseName());
				iii++;
			}
			i++;
		}

		/*
		for(int i=start; i<max; i++){ //minestackbuttonのインベントリの位置
			  int ii = i + page*45 - 1; //minestacklistのindex
			MineStackObj msobj = SeichiAssist.minestacklist.get(ii);
			if(msobj.getItemStack()==null){
				setMineStackButton(inventory, playerdata.minestack.getNum(ii), new ItemStack(msobj.getMaterial(), 1, (short)msobj.getDurability()),  SeichiAssist.config.getMineStacklevel(msobj.getLevel()), i, msobj.getJapaneseName());
			} else {
				setMineStackButton(inventory, playerdata.minestack.getNum(ii), msobj.getItemStack(), SeichiAssist.config.getMineStacklevel(msobj.getLevel()), i, msobj.getJapaneseName());
			}
		}
		*/

		//44まで

		if(page==0){
			// メインページ1ページ目を開く
			itemstack = new ItemStack(Material.SKULL_ITEM,1);
			SkullMeta skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
			itemstack.setDurability((short) 3);
			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "MineStackメインメニューへ");
			List<String> lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
			skullmeta.setLore(lore);
			skullmeta.setOwner("MHF_ArrowLeft");
			itemstack.setItemMeta(skullmeta);
			AsyncInventorySetter.setItemAsync(inventory,45,itemstack.clone());
		} else if(page>=1){
			// MineStackの前のページを開く
			itemstack = new ItemStack(Material.SKULL_ITEM,1);
			SkullMeta skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
			itemstack.setDurability((short) 3);
			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "MineStack" + page + "ページ目へ");
			List<String> lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
			skullmeta.setLore(lore);
			skullmeta.setOwner("MHF_ArrowUp");
			itemstack.setItemMeta(skullmeta);
			AsyncInventorySetter.setItemAsync(inventory,45,itemstack.clone());
		}

		if(maxpage>=1 && page!=maxpage){ //3ページ以降があって現在は最大ページ目でない
			//MineStackの次のページを開く
			itemstack = new ItemStack(Material.SKULL_ITEM,1);
			SkullMeta skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
			itemstack.setDurability((short) 3);
			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "MineStack" + (page+2) + "ページ目へ");
			List<String> lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
			skullmeta.setLore(lore);
			skullmeta.setOwner("MHF_ArrowDown");
			itemstack.setItemMeta(skullmeta);
			AsyncInventorySetter.setItemAsync(inventory,53,itemstack.clone());
		}

		return inventory;
	}

	// MineStackトグルボタン
	public static ItemMeta MineStackToggleMeta(PlayerData playerdata,ItemMeta itemmeta){
		List<String> lore = new ArrayList<>();
		if(playerdata.getMinestackflag()){
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 1, false);
			itemmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "現在ONです");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでOFF");
		}else{
			itemmeta.removeEnchant(Enchantment.DIG_SPEED);
			lore.add(ChatColor.RESET + "" +  ChatColor.RED + "現在OFFです");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックでON");
		}
		itemmeta.setLore(lore);
		return itemmeta;
	}
	//MineStackボタン作成 Material版
	public static Inventory setMineStackButton(Inventory inv, long minestack,Material type,int level,int set){
		ItemStack itemstack = new ItemStack(type,1);
		ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(type);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + type.toString());
		List<String> lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + minestack +"個"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "Lv" + level + "以上でスタック可能"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで1スタック取り出し");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inv.setItem(set,itemstack);
		return inv;
	}
	//MineStackボタン作成 Material版名前付き
	public static Inventory setMineStackButton(Inventory inv, long minestack,Material type,int level,int set,String name){
		ItemStack itemstack = new ItemStack(type,1);
		ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(type);
		if(name!=null){
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + name);
		} else {
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + type.toString());
		}
		List<String> lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + minestack +"個"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "Lv" + level + "以上でスタック可能"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで1スタック取り出し");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inv.setItem(set,itemstack);
		return inv;
	}
	//MineStackボタン作成 ItemStack版
	public static Inventory setMineStackButton(Inventory inv, long minestack, ItemStack itemstack,int level,int set){
		itemstack.setAmount(1);
		ItemMeta itemmeta = itemstack.getItemMeta();
		//itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + itemmeta.getDisplayName());
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + itemstack.getType().toString());
		List<String> lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + minestack +"個"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "Lv" + level + "以上でスタック可能"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで1スタック取り出し");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inv.setItem(set,itemstack);
		return inv;
	}
	//MineStackボタン作成 ItemStack版名前付き
	public static Inventory setMineStackButton(Inventory inv, long minestack,ItemStack itemstack,int level,int set, String name){
		itemstack.setAmount(1);
		ItemMeta itemmeta = itemstack.getItemMeta();
		if(name!=null){
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + name);
		} else {
			//itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + itemmeta.getDisplayName());
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + itemstack.getType().toString());
		}
		List<String> lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + minestack +"個"
				, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "Lv" + level + "以上でスタック可能"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで1スタック取り出し");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inv.setItem(set,itemstack);
		return inv;
	}
	// 全体通知音消音トグルボタン
	public static ItemMeta dispWinSoundToggleMeta(PlayerData playerdata,ItemMeta itemmeta){
		List<String> lore = new ArrayList<>();
		if(playerdata.getEverysoundflag() && playerdata.getEverymessageflag()){
			itemmeta.removeEnchant(Enchantment.DIG_SPEED);
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "全体通知音:消音しない");
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "全体メッセージ:表示する");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変更");
		}else if(!playerdata.getEverysoundflag() && playerdata.getEverymessageflag()){
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			lore.add(ChatColor.RESET + "" +  ChatColor.RED + "全体通知音:消音する");
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "全体メッセージ:表示する");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変更");
		}else {
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			lore.add(ChatColor.RESET + "" +  ChatColor.RED + "全体通知音:消音する");
			lore.add(ChatColor.RESET + "" +  ChatColor.RED + "全体メッセージ:表示しない");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで変更");
		}
		itemmeta.setLore(lore);
		return itemmeta;
	}
	// 死亡メッセージ表示トグルボタン
	public static ItemMeta dispKillLogToggleMeta(PlayerData playerdata,ItemMeta itemmeta){
		List<String> lore = new ArrayList<>();
		if(playerdata.getDispkilllogflag()){
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "表示する");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで隠す");
		}else{
			itemmeta.removeEnchant(Enchantment.DIG_SPEED);
			lore.add(ChatColor.RESET + "" +  ChatColor.RED + "隠す");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックで表示する");
		}
		itemmeta.setLore(lore);
		return itemmeta;
	}

	// ワールドガード保護メッセージ表示トグルボタン(追加)
	public static ItemMeta dispWorldGuardLogToggleMeta(PlayerData playerdata,ItemMeta itemmeta){
		List<String> lore = new ArrayList<>();
		if(playerdata.getDispworldguardlogflag()){
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);

			lore.add(ChatColor.RESET + "" +  ChatColor.GRAY + "スキル使用時のワールドガード保護警告メッセージ");
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "表示する");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで隠す");
		}else{
			itemmeta.removeEnchant(Enchantment.DIG_SPEED);
			lore.add(ChatColor.RESET + "" +  ChatColor.GRAY + "スキル使用時のワールドガード保護警告メッセージ");
			lore.add(ChatColor.RESET + "" +  ChatColor.RED + "隠す");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックで表示する");
		}
		itemmeta.setLore(lore);
		return itemmeta;
	}

	// 複数種類ブロック同時破壊トグルボタン(追加)
	public static ItemMeta MultipleIDBlockBreakToggleMeta(PlayerData playerdata,ItemMeta itemmeta){
		List<String> lore = new ArrayList<>();
		if(playerdata.getMultipleidbreakflag()){
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "複数種類ブロック同時破壊");
			lore.add(ChatColor.RESET + "" +  ChatColor.GRAY + "ブロックに対応するツールを無視してスキルで");
			lore.add(ChatColor.RESET + "" +  ChatColor.GRAY + "破壊可能な全種類のブロックを同時に破壊します");
			lore.add(ChatColor.RESET + "" + ChatColor.DARK_RED + "整地ワールドではON/OFFに関わらず同時破壊されます");
					//ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：0秒"
			if(playerdata.getLevel() >=SeichiAssist.config.getMultipleIDBlockBreaklevel()){
				lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：" + SeichiAssist.config.getMultipleIDBlockBreaklevel());
					//	ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：***"
					//	ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット"
				lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "ON");
				lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでOFF");
			} else {
				lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "必要整地レベル：" + SeichiAssist.config.getMultipleIDBlockBreaklevel());
				lore.add(ChatColor.RESET + "" +  ChatColor.RED + "整地レベルが足りません");
				if(SeichiAssist.DEBUG){
					lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "ON");
				}
			}

		}else{
			itemmeta.removeEnchant(Enchantment.DIG_SPEED);
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "複数種類ブロック同時破壊");
			lore.add(ChatColor.RESET + "" +  ChatColor.GRAY + "ブロックに対応するツールを無視してスキルで");
			lore.add(ChatColor.RESET + "" +  ChatColor.GRAY + "破壊可能な全種類のブロックを同時に破壊します");
			lore.add(ChatColor.RESET + "" + ChatColor.DARK_RED + "整地ワールドではON/OFFに関わらず同時破壊されます");
			//ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "クールダウン：0秒"
			if(playerdata.getLevel() >=SeichiAssist.config.getMultipleIDBlockBreaklevel()){
				lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "必要整地レベル：" + SeichiAssist.config.getMultipleIDBlockBreaklevel());
				//	ChatColor.RESET + "" +  ChatColor.BLUE + "消費マナ：***"
				//	ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット"
				lore.add(ChatColor.RESET + "" +  ChatColor.RED + "OFF");
				lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックでON");
			} else {
				lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "必要整地レベル：" + SeichiAssist.config.getMultipleIDBlockBreaklevel());
				lore.add(ChatColor.RESET + "" +  ChatColor.RED + "整地レベルが足りません");
				if(SeichiAssist.DEBUG){
					lore.add(ChatColor.RESET + "" +  ChatColor.RED + "OFF");
				}
			}
		}
		itemmeta.setLore(lore);
		return itemmeta;
	}

	// GiganticBerserk Meta
	public static ItemMeta GiganticBerserkMeta(PlayerData playerdata, ItemMeta itemmeta){
		List<String> lore = new ArrayList<>();

		int n = (playerdata.getGBstage() * 10) + playerdata.getGBlevel();
		GiganticBerserkTask GBTR = new GiganticBerserkTask();

		if(playerdata.getLevel() < 10){
			lore.add(ChatColor.WHITE + "このパッシブスキルは");
			lore.add(ChatColor.WHITE + "整地レベルが10以上になると解放されます");
		}
		else {
			lore.add(ChatColor.RED + "敵MOBを倒した時");
			lore.add(ChatColor.RED + "その魂を吸収しマナへと変換するスキル");
			lore.add(ChatColor.DARK_GRAY + "※成功率は高くなく");
			lore.add(ChatColor.DARK_GRAY + "整地中でなければその効果を発揮しない");
			lore.add("");
			lore.add(ChatColor.DARK_GRAY + "実装は試験的であり、変更される場合があります");
			if(playerdata.getGBstage() == 4 && playerdata.getGBlevel() == 9){
				lore.add(ChatColor.GRAY + "MOBの魂を極限まで吸収し最大限の力を発揮する");
			}else {
				lore.add(ChatColor.GRAY + "MOBの魂を" + LevelThresholds.INSTANCE.getGiganticBerserkLevelList().get(n) + "回吸収すると更なる力が得られる");
				lore.add(ChatColor.GRAY + "" + playerdata.getGBexp() + "/" + LevelThresholds.INSTANCE.getGiganticBerserkLevelList().get(n));
			}
			lore.add(ChatColor.GRAY + "現在" + (playerdata.getGBlevel() + 1) + "レベル,回復率 " + (int)(100 * GBTR.getProb(playerdata)) + ".0%");

			if (playerdata.isGBStageUp()){
				lore.add("");
				lore.add(ChatColor.DARK_RED + "沢山の魂を吸収したことで");
				lore.add(ChatColor.DARK_RED + "スキルの秘めたる力を解放できそうだ…！");
				lore.add(ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで解放する");
			}

		}

		itemmeta.setLore(lore);
		return itemmeta;
	}

	public static ItemMeta ChestBreakToggleMeta(PlayerData playerdata, ItemMeta itemmeta){
		List<String> lore = new ArrayList<>();

		lore.add(ChatColor.GREEN + "スキルでチェストを破壊するスキル");

		if(playerdata.getChestflag()){
			lore.add(ChatColor.RED + "整地ワールドでのみ発動中(デフォルト)");
			lore.add("");
			lore.add(ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックで切り替え");
		}else{
			lore.add(ChatColor.RED + "発動しません");
			lore.add("");
			lore.add(ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックで切り替え");
		}
		itemmeta.setLore(lore);
		return itemmeta;
	}

	// PvPトグルボタン
	public static ItemMeta dispPvPToggleMeta(PlayerData playerdata,ItemMeta itemmeta){
		List<String> lore = new ArrayList<>();
		if(playerdata.getPvpflag()){
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "ON(ONの相手とPvPが可能)");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでOFF");
		}else{
			itemmeta.removeEnchant(Enchantment.DIG_SPEED);
			lore.add(ChatColor.RESET + "" +  ChatColor.RED + "OFF(全てのPvPを無効化)");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックでON");
		}
		itemmeta.setLore(lore);
		return itemmeta;
	}

	// ExpBarトグルボタン
	public static SkullMeta dispExpBarToggleMeta(PlayerData playerdata,SkullMeta skullmeta){
		List<String> lore = skullmeta.getLore();
		if(playerdata.getExpbar().isVisible()){
			lore.set(lore.size() - 2, ChatColor.RESET + "" +  ChatColor.GREEN + "整地量バーを表示");
			lore.set(lore.size() - 1, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで非表示");
		}else{
			lore.set(lore.size() - 2, ChatColor.RESET + "" +  ChatColor.RED + "整地量バーを非表示");
			lore.set(lore.size() - 1, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "クリックで表示");
		}
		skullmeta.setLore(lore);
		return skullmeta;
	}

	// インベントリ共有トグルボタン
	public static ItemMeta dispShareInvMeta(PlayerData playerdata){
		ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.TRAPPED_CHEST);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "インベントリ共有");
		List<String> lore = new ArrayList<>();
		lore.addAll(Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "現在の装備・アイテムを移動します。"
				, ChatColor.RESET + "" + "サーバー間のアイテム移動にご利用ください。"
				, ""
				));

		if(playerdata.getContentsPresentInSharedInventory()){
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "収納中");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでアイテムを取り出します。");
			lore.add(ChatColor.RESET + "" + ChatColor.RED + "現在の装備・アイテムが空であることを確認してください。");
		}else{
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "非収納中");
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでアイテムを収納します。");
		}
		itemmeta.setLore(lore);
		return itemmeta;
	}

	//ランキングリスト
	public static Inventory getRankingList(Player p, int page){
//		int maxpage=2;
		int maxpage=14;
		final int MIN_LEVEL = 100;
		Inventory inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "整地神ランキング");
		ItemStack itemstack = new ItemStack(Material.SKULL_ITEM,1);
		SkullMeta skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		List<String> lore = new ArrayList<>();
		itemstack.setDurability((short) 3);
		RankData rankdata;
//		for(int count = 50*page,count2=0;count < 50+50*page;count++,count2++){
		for(int count = 10*page,count2=0;count < 10+10*page;count++,count2++){
			if(count >= SeichiAssist.ranklist.size()){
				break;
			}
//			if(count2==45){count2+=2;}
			rankdata = SeichiAssist.ranklist.get(count);
			if(rankdata.totalbreaknum< LevelThresholds.INSTANCE.getLevelExpThresholds().get(MIN_LEVEL-1)){ //レベル100相当の総整地量判定に変更
				break;
			}

			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "" + (count+1) +"位:" + "" + ChatColor.WHITE + rankdata.name);
			lore.clear();
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "整地レベル:" + rankdata.level);
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "総整地量:" + rankdata.totalbreaknum);

			skullmeta.setLore(lore);
			skullmeta.setOwner(rankdata.name);
			itemstack.setItemMeta(skullmeta);
			AsyncInventorySetter.setItemAsync(inventory,count2,itemstack.clone());
		}

		if(page!=maxpage){
		// 整地神ランキング次ページ目を開く
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地神ランキング"+(page+2)+"ページ目へ");
		lore.clear();
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowDown");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,52,itemstack.clone());
		}

		// 1ページ目を開く
		if(page==0){
			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ");
			lore.clear();
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
			skullmeta.setLore(lore);
			skullmeta.setOwner("MHF_ArrowLeft");
			itemstack.setItemMeta(skullmeta);
			AsyncInventorySetter.setItemAsync(inventory,45,itemstack.clone());
		} else {
			// 整地神ランキング前ページ目を開く;
			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地神ランキング"+page+"ページ目へ");
			lore.clear();
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
			skullmeta.setLore(lore);
			skullmeta.setOwner("MHF_ArrowUp");
			itemstack.setItemMeta(skullmeta);
			AsyncInventorySetter.setItemAsync(inventory,45,itemstack.clone());
		}

		// 総整地量の表記
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地鯖統計データ");
		lore.clear();
		lore.addAll(Arrays.asList(ChatColor.RESET + "" +  ChatColor.AQUA + "全プレイヤー総整地量:"
				,ChatColor.RESET + "" +  ChatColor.AQUA + SeichiAssist.allplayerbreakblockint
				));
		skullmeta.setLore(lore);
		skullmeta.setOwner("unchama");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,53,itemstack.clone());

		return inventory;
	}

	//ランキングリスト(ログイン時間)
	public static Inventory getRankingList_playtick(Player p, int page){
//		int maxpage=2;
		int maxpage=14;
		//final int MIN_LEVEL = 100;
		Inventory inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "ログイン神ランキング");
		ItemStack itemstack = new ItemStack(Material.SKULL_ITEM,1);
		SkullMeta skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		List<String> lore = new ArrayList<>();
		itemstack.setDurability((short) 3);
		RankData rankdata;
		for(int count = 10*page,count2=0;count < 10+10*page;count++,count2++){
			if(count >= SeichiAssist.ranklist_playtick.size()){
				break;
			}
			rankdata = SeichiAssist.ranklist_playtick.get(count);

			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "" + (count+1) +"位:" + "" + ChatColor.WHITE + rankdata.name);
			lore.clear();
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "総ログイン時間:" + TypeConverter.toTimeString(TypeConverter.toSecond(rankdata.playtick)));

			skullmeta.setLore(lore);
			skullmeta.setOwner(rankdata.name);
			itemstack.setItemMeta(skullmeta);
			AsyncInventorySetter.setItemAsync(inventory,count2,itemstack.clone());
		}

		if(page!=maxpage){
		// 整地神ランキング次ページ目を開く
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ログイン神ランキング"+(page+2)+"ページ目へ");
		lore.clear();
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowDown");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,52,itemstack.clone());
		}

		// 1ページ目を開く
		if(page==0){
			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ");
			lore.clear();
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
			skullmeta.setLore(lore);
			skullmeta.setOwner("MHF_ArrowLeft");
			itemstack.setItemMeta(skullmeta);
			AsyncInventorySetter.setItemAsync(inventory,45,itemstack.clone());
		} else {
			// 整地神ランキング前ページ目を開く;
			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ログイン神ランキング"+page+"ページ目へ");
			lore.clear();
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
			skullmeta.setLore(lore);
			skullmeta.setOwner("MHF_ArrowUp");
			itemstack.setItemMeta(skullmeta);
			AsyncInventorySetter.setItemAsync(inventory,45,itemstack.clone());
		}

		return inventory;
	}

	//ランキングリスト(投票回数)
	public static Inventory getRankingList_p_vote(Player p, int page){
//		int maxpage=2;
		int maxpage=14;
		final int MIN_LEVEL = 1;
		Inventory inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "投票神ランキング");
		ItemStack itemstack = new ItemStack(Material.SKULL_ITEM,1);
		SkullMeta skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		List<String> lore = new ArrayList<>();
		itemstack.setDurability((short) 3);
		RankData rankdata;
//		for(int count = 50*page,count2=0;count < 50+50*page;count++,count2++){
		for(int count = 10*page,count2=0;count < 10+10*page;count++,count2++){
			if(count >= SeichiAssist.ranklist_p_vote.size()){
				break;
			}
//			if(count2==45){count2+=2;}
			rankdata = SeichiAssist.ranklist_p_vote.get(count);
			if(rankdata.p_vote<MIN_LEVEL){ //投票数0
				break;
			}

			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "" + (count+1) +"位:" + "" + ChatColor.WHITE + rankdata.name);
			lore.clear();
			//lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "整地レベル:" + rankdata.level);
			//lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "総整地量:" + rankdata.totalbreaknum);
			//lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "総ログイン時間:" + Util.toTimeString(Util.toSecond(rankdata.playtick)));
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "総投票回数:" + rankdata.p_vote);

			skullmeta.setLore(lore);
			skullmeta.setOwner(rankdata.name);
			itemstack.setItemMeta(skullmeta);
			AsyncInventorySetter.setItemAsync(inventory,count2,itemstack.clone());
		}

		if(page!=maxpage){
		// 投票神ランキング次ページ目を開く
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "投票神ランキング"+(page+2)+"ページ目へ");
		lore.clear();
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowDown");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,52,itemstack.clone());
		}

		// 1ページ目を開く
		if(page==0){
			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ");
			lore.clear();
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
			skullmeta.setLore(lore);
			skullmeta.setOwner("MHF_ArrowLeft");
			itemstack.setItemMeta(skullmeta);
			AsyncInventorySetter.setItemAsync(inventory,45,itemstack.clone());
		} else {
			// 整地神ランキング前ページ目を開く;
			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "投票神ランキング"+page+"ページ目へ");
			lore.clear();
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
			skullmeta.setLore(lore);
			skullmeta.setOwner("MHF_ArrowUp");
			itemstack.setItemMeta(skullmeta);
			AsyncInventorySetter.setItemAsync(inventory,45,itemstack.clone());
		}

		return inventory;
	}

	//ランキングリスト(プレミアムエフェクトポイント)
	public static Inventory getRankingList_premiumeffectpoint(Player p, int page){
		int maxpage=2;
		final int MIN_LEVEL = 1;
		Inventory inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "寄付神ランキング");
		ItemStack itemstack = new ItemStack(Material.SKULL_ITEM,1);
		SkullMeta skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		List<String> lore = new ArrayList<>();
		itemstack.setDurability((short) 3);
		RankData rankdata;
		for(int count = 50*page,count2=0;count < 50+50*page;count++,count2++){
			if(count >= SeichiAssist.ranklist_premiumeffectpoint.size()){
				break;
			}
			if(count2==45){count2+=2;}
			rankdata = SeichiAssist.ranklist_premiumeffectpoint.get(count);
			if(rankdata.premiumeffectpoint<MIN_LEVEL){ //寄付金額0
				break;
			}

			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "" + (count+1) +"位:" + "" + ChatColor.WHITE + rankdata.name);
			lore.clear();
			//lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "整地レベル:" + rankdata.level);
			//lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "総整地量:" + rankdata.totalbreaknum);
			//lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "総ログイン時間:" + Util.toTimeString(Util.toSecond(rankdata.playtick)));
			lore.add(ChatColor.RESET + "" +  ChatColor.GREEN + "総寄付金額:" + rankdata.premiumeffectpoint*100);

			skullmeta.setLore(lore);
			skullmeta.setOwner(rankdata.name);
			itemstack.setItemMeta(skullmeta);
			AsyncInventorySetter.setItemAsync(inventory,count2,itemstack.clone());
		}

		if(page!=maxpage){
		// 整地神ランキング次ページ目を開く
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "寄付神ランキング"+(page+2)+"ページ目へ");
		lore.clear();
		lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動"
				);
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowDown");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,52,itemstack.clone());
		}

		// 1ページ目を開く
		if(page==0){
			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ");
			lore.clear();
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
			skullmeta.setLore(lore);
			skullmeta.setOwner("MHF_ArrowLeft");
			itemstack.setItemMeta(skullmeta);
			AsyncInventorySetter.setItemAsync(inventory,45,itemstack.clone());
		} else {
			// 整地神ランキング前ページ目を開く;
			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "寄付神ランキング"+page+"ページ目へ");
			lore.clear();
			lore.add(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
			skullmeta.setLore(lore);
			skullmeta.setOwner("MHF_ArrowUp");
			itemstack.setItemMeta(skullmeta);
			AsyncInventorySetter.setItemAsync(inventory,45,itemstack.clone());
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
				PlayerData playerdata = SeichiAssist.playermap.get(uuid);
				//念のためエラー分岐
				if(playerdata == null){
					player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
					Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
					Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
					return null;
				}

				Inventory inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "整地スキルエフェクト選択");
				ItemStack itemstack;
				ItemMeta itemmeta;
				SkullMeta skullmeta;
				List<String> lore;

				// 1ページ目を開く
				itemstack = new ItemStack(Material.SKULL_ITEM,1);
				skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
				itemstack.setDurability((short) 3);
				skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "スキルメニューへ");
				lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
				skullmeta.setLore(lore);
				skullmeta.setOwner("MHF_ArrowLeft");
				itemstack.setItemMeta(skullmeta);
				AsyncInventorySetter.setItemAsync(inventory,45,itemstack.clone());

				//1行目

				itemstack = new ItemStack(Material.SKULL_ITEM,1);
				skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
				itemstack.setDurability((short) 3);
				skullmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
				skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + playerdata.getName() + "のスキルエフェクトデータ");
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "現在選択しているエフェクト：" + ActiveSkillEffect.getNamebyNum(playerdata.getActiveskilldata().effectnum)
						, ChatColor.RESET + "" +  ChatColor.YELLOW + "使えるエフェクトポイント：" + playerdata.getActiveskilldata().effectpoint
						, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "※投票すると獲得出来ます"
						, ChatColor.RESET + "" +  ChatColor.LIGHT_PURPLE + "使えるプレミアムポイント：" + playerdata.getActiveskilldata().premiumeffectpoint
						, ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "※寄付をすると獲得できます"

						);
				skullmeta.setLore(lore);
				// この操作は安全; メニューを開けているのにUUIDがないなんてことがないから
				skullmeta.setOwningPlayer(Bukkit.getOfflinePlayer(playerdata.getUuid())); //.setOwner(playerdata.name);
				itemstack.setItemMeta(skullmeta);
				AsyncInventorySetter.setItemAsync(inventory,0,itemstack.clone());


				itemstack = new ItemStack(Material.BOOK_AND_QUILL,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BOOK_AND_QUILL);
				itemmeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "プレミアムエフェクト購入履歴");
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで閲覧");
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(2,itemstack);

				itemstack = new ItemStack(Material.GLASS,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GLASS);
				itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "エフェクトを使用しない");
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット");
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(1,itemstack);


				ActiveSkillEffect[] skilleffect = ActiveSkillEffect.values();

				for(int i = 0; i < skilleffect.length;i++){
					//プレイヤーがそのスキルを取得している場合の処理
					if (playerdata.getActiveskilldata().obtainedSkillEffects.contains(skilleffect[i])) {
						itemstack = new ItemStack(skilleffect[i].getMaterial(),1);
						itemmeta = Bukkit.getItemFactory().getItemMeta(skilleffect[i].getMaterial());
						itemmeta.setDisplayName(skilleffect[i].getName());
						lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + skilleffect[i].getExplain()
								, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット"
								);
						itemmeta.setLore(lore);
						itemstack.setItemMeta(itemmeta);
					}
					//プレイヤーがそのスキルをまだ取得していない場合の処理
					else{
						itemstack = new ItemStack(Material.BEDROCK,1);
						itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
						itemmeta.setDisplayName(skilleffect[i].getName());
						lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + skilleffect[i].getExplain()
								, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要エフェクトポイント：" + skilleffect[i].getUsePoint()
								, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
						itemmeta.setLore(lore);
						itemstack.setItemMeta(itemmeta);
					}
					inventory.setItem(i + 9,itemstack);
				}
				ActiveSkillPremiumEffect[] premiumeffect = ActiveSkillPremiumEffect.values();

				for(int i = 0; i < premiumeffect.length;i++){
					//プレイヤーがそのスキルを取得している場合の処理
					if (playerdata.getActiveskilldata().obtainedSkillPremiumEffects.contains(premiumeffect[i])) {
						itemstack = new ItemStack(premiumeffect[i].getMaterial(),1);
						itemmeta = Bukkit.getItemFactory().getItemMeta(premiumeffect[i].getMaterial());
						itemmeta.setDisplayName(ChatColor.UNDERLINE + "" + ChatColor.BOLD + ChatColor.stripColor(premiumeffect[i].getName()));
						lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + premiumeffect[i].getExplain()
								, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでセット"
								);
						itemmeta.setLore(lore);
						itemstack.setItemMeta(itemmeta);
					}
					//プレイヤーがそのスキルをまだ取得していない場合の処理
					else{
						itemstack = new ItemStack(Material.BEDROCK,1);
						itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
						itemmeta.setDisplayName(premiumeffect[i].getName());
						lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + premiumeffect[i].getExplain()
								, ChatColor.RESET + "" +  ChatColor.YELLOW + "必要プレミアムポイント：" + premiumeffect[i].getUsePoint()
								, ChatColor.RESET + "" +  ChatColor.AQUA + "" + ChatColor.UNDERLINE + "クリックで解除");
						itemmeta.setLore(lore);
						itemstack.setItemMeta(itemmeta);
					}
					inventory.setItem(i + 27,itemstack);
				}

		return inventory;
	}
	//プレミア購入履歴表示
	public static Inventory getBuyRecordMenuData(Player player) {
		PlayerData playerdata = playermap.get(player.getUniqueId());
		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.BLUE + "" + ChatColor.BOLD + "プレミアムエフェクト購入履歴");
		ItemStack itemstack;
		SkullMeta skullmeta;
		List<String> lore;

		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "エフェクト選択メニューへ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,27,itemstack.clone());

		databaseGateway.donateDataManipulator.loadDonateData(playerdata,inventory);

		return inventory;
	}

	//実績メニュー
	public static Inventory getTitleMenuData(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
			Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			return null;
		}

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績・二つ名システム");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore;

		//表示切り替え(LV・二つ名)
		itemstack = new ItemStack(Material.REDSTONE_TORCH_ON,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.REDSTONE_TORCH_ON);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "整地レベルを表示" );
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "このボタンをクリックすると、"
							,ChatColor.RESET + "" +  ChatColor.RED + "「整地LV」に表示を切り替えます。"
							,ChatColor.RESET + "" +  ChatColor.YELLOW + "※反映されるまで最大1分ほどかかります。");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(0,itemstack);

		//予約付与受け取りボタン
		if(!(playerdata.getGiveachvNo() == 0)){
			itemstack = new ItemStack(Material.SKULL_ITEM,1);
			skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
			itemstack.setDurability((short) 3);
			skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "【実績付与システム】");
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "運営チームからあなたへ、"
					,ChatColor.RESET + "" +  ChatColor.RED + "「二つ名」のプレゼントが届いています。"
					,ChatColor.RESET + "" +  ChatColor.YELLOW + "クリックすることで受け取れます！");
			skullmeta.setLore(lore);
			skullmeta.setOwner("MHF_Present2");
			itemstack.setItemMeta(skullmeta);
			AsyncInventorySetter.setItemAsync(inventory,1,itemstack.clone());
		}

		//二つ名組合せシステム画面へ移動
		itemstack = new ItemStack(Material.ANVIL,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.ANVIL);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "「二つ名組合せシステム」" );
		lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "設定画面を表示します。");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(8,itemstack);

		//カテゴリ画面へ移動
		itemstack = new ItemStack(Material.GOLD_PICKAXE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GOLD_PICKAXE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "カテゴリ「整地」" );
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "以下の実績が含まれます。"
							,ChatColor.RESET + "" +  ChatColor.AQUA + "「整地量」"
							,ChatColor.RESET + "" +  ChatColor.AQUA + "「整地神ランキング」");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(10,itemstack);

		itemstack = new ItemStack(Material.GLASS,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GLASS);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "カテゴリ「建築」" );
		lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.YELLOW + "今後実装予定のカテゴリです。");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(12,itemstack);

		itemstack = new ItemStack(Material.COMPASS,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COMPASS);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "カテゴリ「ログイン」" );
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "以下の実績が含まれます。"
							,ChatColor.RESET + "" +  ChatColor.AQUA + "「参加時間」"
							,ChatColor.RESET + "" +  ChatColor.AQUA + "「通算ログイン」"
							,ChatColor.RESET + "" +  ChatColor.AQUA + "「連続ログイン」"
							,ChatColor.RESET + "" +  ChatColor.AQUA + "「記念日」");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(14,itemstack);

		itemstack = new ItemStack(Material.BLAZE_POWDER,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BLAZE_POWDER);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "カテゴリ「やりこみ」" );
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "以下の実績が含まれます。"
							,ChatColor.RESET + "" +  ChatColor.AQUA + "「MEBIUSブリーダー」"
							,ChatColor.RESET + "" +  ChatColor.AQUA + "「スターレベル」");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(16,itemstack);

		itemstack = new ItemStack(Material.EYE_OF_ENDER,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.EYE_OF_ENDER);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "カテゴリ「特殊」" );
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "以下の実績が含まれます。"
							,ChatColor.RESET + "" +  ChatColor.AQUA + "「公式イベント」"
							,ChatColor.RESET + "" +  ChatColor.AQUA + "「JMS投票数」"
							,ChatColor.RESET + "" +  ChatColor.AQUA + "「極秘実績」");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(22,itemstack);


		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,27,itemstack.clone());

		return inventory;
	}

	//二つ名組合せシステム・メニュー
	public static Inventory setFreeTitleMainData(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
			Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			return null;
		}

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "二つ名組合せシステム");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore;

		//各ボタンの設定
		nextpageflag1 = false ;
		nextpageflag2 = false ;
		nextpageflag3 = false ;
		nextpageflagS = false ;
		checkTitle1 = 0 ;
		checkTitle2 = 0 ;
		checkTitle3 = 0 ;
		checkTitleS = 0 ;
		NoKeep = 0 ;

		//実績ポイントの最新情報反映ボタン
		itemstack = new ItemStack(Material.EMERALD_ORE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.EMERALD_ORE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績ポイント 情報" );
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "クリックで情報を最新化"
							,ChatColor.RESET + "" +  ChatColor.RED + "累計獲得量：" + (playerdata.getAchvPointMAX() + playerdata.getAchvChangenum() * 3)
							,ChatColor.RESET + "" +  ChatColor.RED + "累計消費量：" + playerdata.getAchvPointUSE()
							,ChatColor.RESET + "" +  ChatColor.AQUA + "使用可能量：" + playerdata.getAchvPoint());
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(0,itemstack);

		//パーツショップ
		itemstack = new ItemStack(Material.ITEM_FRAME,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.ITEM_FRAME);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績ポイントショップ" );
		lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.GREEN + "クリックで開きます");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(9,itemstack);

		//エフェクトポイントからの変換ボタン
		itemstack = new ItemStack(Material.EMERALD,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.EMERALD);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ポイント変換ボタン" );
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "JMS投票で手に入るポイントを"
							,ChatColor.RESET + "" +  ChatColor.RED + "実績ポイントに変換できます。"
							,ChatColor.RESET + "" +  ChatColor.YELLOW + "" + ChatColor.BOLD + "投票pt 10pt → 実績pt 3pt"
							,ChatColor.RESET + "" +  ChatColor.AQUA + "クリックで変換を一回行います。"
							,ChatColor.RESET + "" +  ChatColor.GREEN + "所有投票pt :" + playerdata.getActiveskilldata().effectpoint
							,ChatColor.RESET + "" +  ChatColor.GREEN + "所有実績pt :" + playerdata.getAchvPoint());
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(1,itemstack);


		itemstack = new ItemStack(Material.BOOK,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BOOK);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "現在の二つ名の確認" );
		lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "「" + SeichiAssist.config.getTitle1(playerdata.getDisplayTitle1No())
				+ SeichiAssist.config.getTitle2(playerdata.getDisplayTitle2No()) + SeichiAssist.config.getTitle3(playerdata.getDisplayTitle3No()) + "」");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(4,itemstack);

		itemstack = new ItemStack(Material.WATER_BUCKET,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.WATER_BUCKET);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "前パーツ選択画面" );
		lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "クリックで移動します");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(11,itemstack);

		itemstack = new ItemStack(Material.MILK_BUCKET,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.MILK_BUCKET);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "中パーツ選択画面" );
		lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "クリックで移動します");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(13,itemstack);

		itemstack = new ItemStack(Material.LAVA_BUCKET,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.LAVA_BUCKET);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "後パーツ選択画面" );
		lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "クリックで移動します");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(15,itemstack);

		itemstack = new ItemStack(Material.WATER_BUCKET,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.WATER_BUCKET);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "前パーツ選択画面" );
		lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "クリックで移動します");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(11,itemstack);


		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績・二つ名メニューへ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,27,itemstack.clone());

		return inventory;
	}

	//二つ名組合せ「前パーツ」
	public static Inventory setFreeTitle1Data(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
			Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			return null;
		}

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "二つ名組合せ「前」");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore;


		if(nextpageflag1){
			nextpageflag1 = false;
		}else {
			checkTitle1 = 1000;
		}

		//各ボタンの設定
		//解禁済みの実績をチェック→前パーツがあるかをチェック→あればボタン配置
		int checkInv = 0 ;
		for(;checkTitle1 < 9900 ;){
			if(checkInv < 27){
				if(playerdata.getTitleFlags().get(checkTitle1)){
					if(SeichiAssist.config.getTitle1(checkTitle1) == null || SeichiAssist.config.getTitle1(checkTitle1).equals("")){
					}else{
						itemstack = new ItemStack(Material.WATER_BUCKET,1);
						itemmeta = Bukkit.getItemFactory().getItemMeta(Material.WATER_BUCKET);
						itemmeta.setDisplayName(String.valueOf(checkTitle1));
						lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "前パーツ「" + SeichiAssist.config.getTitle1(checkTitle1) + "」");
						itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
						itemmeta.setLore(lore);
						itemstack.setItemMeta(itemmeta);
						inventory.setItem(checkInv,itemstack);

						checkInv ++ ;
					}

				}
			}else if(checkInv == 27){
				//次ページへのボタンを配置
				itemstack = new ItemStack(Material.SKULL_ITEM,1);
				skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
				itemstack.setDurability((short) 3);
				skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "次ページへ");
				lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
				skullmeta.setLore(lore);
				skullmeta.setOwner("MHF_ArrowRight");
				itemstack.setItemMeta(skullmeta);
				AsyncInventorySetter.setItemAsync(inventory,35,itemstack.clone());

				nextpageflag1 = true ;

				break;
			}
			checkTitle1 ++ ;
		}


		//パーツ未選択状態にするボタン
		itemstack = new ItemStack(Material.GRASS,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GRASS);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "前パーツを未選択状態にする");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで実行");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(31,itemstack);

		// 二つ名組合せメインページを開く
		itemstack = new ItemStack(Material.BARRIER,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BARRIER);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "二つ名組合せメインメニューへ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(27,itemstack);

		return inventory ;
	}

	//二つ名組合せ「中パーツ」
	public static Inventory setFreeTitle2Data(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
			Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			return null;
		}

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "二つ名組合せ「中」");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore;


		if(nextpageflag2){
			nextpageflag2 = false;
		}else {
			checkTitle2 = 9900;
		}

		//各ボタンの設定
		//パーツがあるかをチェック→あればボタン配置
		int checkInv = 0 ;
		for(;checkTitle2 < 9999 ;){
			if(checkInv < 27){
				//一部の「隠し中パーツ」は取得しているかの確認
				if(9911 <= checkTitle2  /*&& checkTitle2 <= 9927*/){
					if(playerdata.getTitleFlags().get(checkTitle2)){
						if(SeichiAssist.config.getTitle2(checkTitle2) == null || SeichiAssist.config.getTitle2(checkTitle2).equals("")){
						}else{
							itemstack = new ItemStack(Material.MILK_BUCKET,1);
							itemmeta = Bukkit.getItemFactory().getItemMeta(Material.MILK_BUCKET);
							itemmeta.setDisplayName(String.valueOf(checkTitle2));
							lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "中パーツ「" + SeichiAssist.config.getTitle2(checkTitle2) + "」");
							itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
							itemmeta.setLore(lore);
							itemstack.setItemMeta(itemmeta);
							inventory.setItem(checkInv,itemstack);

							checkInv ++ ;
						}
					}
				}else if(SeichiAssist.config.getTitle2(checkTitle2) == null || SeichiAssist.config.getTitle2(checkTitle2).equals("")){
				}else{
					itemstack = new ItemStack(Material.MILK_BUCKET,1);
					itemmeta = Bukkit.getItemFactory().getItemMeta(Material.MILK_BUCKET);
					itemmeta.setDisplayName(String.valueOf(checkTitle2));
					lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "中パーツ「" + SeichiAssist.config.getTitle2(checkTitle2) + "」");
					itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
					itemmeta.setLore(lore);
					itemstack.setItemMeta(itemmeta);
					inventory.setItem(checkInv,itemstack);

					checkInv ++ ;
				}


			}else if(checkInv == 27){
				//次ページへのボタンを配置
				itemstack = new ItemStack(Material.SKULL_ITEM,1);
				skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
				itemstack.setDurability((short) 3);
				skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "次ページへ");
				lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
				skullmeta.setLore(lore);
				skullmeta.setOwner("MHF_ArrowRight");
				itemstack.setItemMeta(skullmeta);
				AsyncInventorySetter.setItemAsync(inventory,35,itemstack.clone());

				break;
			}
			checkTitle2 ++ ;
		}


		//パーツ未選択状態にするボタン
		itemstack = new ItemStack(Material.GRASS,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GRASS);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "中パーツを未選択状態にする");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで実行");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(31,itemstack);

		// 二つ名組合せメインページを開く
		itemstack = new ItemStack(Material.BARRIER,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BARRIER);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "二つ名組合せメインメニューへ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(27,itemstack);

		return inventory ;
	}

	//二つ名組合せ「後パーツ」
	public static Inventory setFreeTitle3Data(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
			Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			return null;
		}

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "二つ名組合せ「後」");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore;

		if(nextpageflag3){
		}else {
			checkTitle3 = 1000;
		}

		//各ボタンの設定
		//解禁済みの実績をチェック→前パーツがあるかをチェック→あればボタン配置
		int checkInv = 0 ;
		for(;checkTitle3 < 9900 ;){
			if(checkInv < 27){
				if(playerdata.getTitleFlags().get(checkTitle3)){
					if(SeichiAssist.config.getTitle3(checkTitle3) == null || SeichiAssist.config.getTitle3(checkTitle3).equals("")){
					}else{
						itemstack = new ItemStack(Material.LAVA_BUCKET,1);
						itemmeta = Bukkit.getItemFactory().getItemMeta(Material.LAVA_BUCKET);
						itemmeta.setDisplayName(String.valueOf(checkTitle3));
						lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "後パーツ「" + SeichiAssist.config.getTitle3(checkTitle3) + "」");
						itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
						itemmeta.setLore(lore);
						itemstack.setItemMeta(itemmeta);
						inventory.setItem(checkInv,itemstack);

						checkInv ++ ;
					}

				}
			}else if(checkInv == 27){
				//次ページへのボタンを配置
				itemstack = new ItemStack(Material.SKULL_ITEM,1);
				skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
				itemstack.setDurability((short) 3);
				skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "次ページへ");
				lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
				skullmeta.setLore(lore);
				skullmeta.setOwner("MHF_ArrowRight");
				itemstack.setItemMeta(skullmeta);
				AsyncInventorySetter.setItemAsync(inventory,35,itemstack.clone());

				nextpageflag3 = true ;

				break;
			}
			checkTitle3 ++ ;
		}

		//パーツ未選択状態にするボタン
		itemstack = new ItemStack(Material.GRASS,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GRASS);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "後パーツを未選択状態にする");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで実行");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(31,itemstack);


		// 二つ名組合せメインページを開く
		itemstack = new ItemStack(Material.BARRIER,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BARRIER);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "二つ名組合せメインメニューへ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(27,itemstack);

		return inventory ;
	}


	//実績ポイントショップ
	public static Inventory setTitleShopData(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
			Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			return null;
		}

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績ポイントショップ");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore;

		//実績ポイントの最新情報反映ボタン
		itemstack = new ItemStack(Material.EMERALD_ORE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.EMERALD_ORE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績ポイント 情報" );
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "クリックで情報を最新化"
							,ChatColor.RESET + "" +  ChatColor.RED + "累計獲得量：" + (playerdata.getAchvPointMAX() + playerdata.getAchvChangenum() * 3)
							,ChatColor.RESET + "" +  ChatColor.RED + "累計消費量：" + playerdata.getAchvPointUSE()
							,ChatColor.RESET + "" +  ChatColor.AQUA + "使用可能量：" + playerdata.getAchvPoint());
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(0,itemstack);

		//おしながき
		if(playerdata.getSamepageflag()){
			checkTitleS = NoKeep ;
		}else if(!nextpageflagS){
			checkTitleS = 9801 ;
		}
		NoKeep = checkTitleS ;
		playerdata.setSamepageflag(false);
		int setInv = 1 ;
		for(;checkTitleS <= 9832;){
			if(setInv < 27){
				if(!playerdata.getTitleFlags().get(checkTitleS)){
					itemstack = new ItemStack(Material.BEDROCK,1);
					itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
					itemmeta.setDisplayName(String.valueOf(checkTitleS));
					lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "前・後パーツ「"+ SeichiAssist.config.getTitle1(checkTitleS) +"」"
										,ChatColor.RESET + "" +  ChatColor.GREEN + "必要ポイント：20"
										,ChatColor.RESET + "" +  ChatColor.AQUA + "クリックで購入できます");
					itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
					itemmeta.setLore(lore);
					itemstack.setItemMeta(itemmeta);
					inventory.setItem(setInv,itemstack);

					setInv ++ ;
				}
			}else {
				//次ページへのボタンを配置
				itemstack = new ItemStack(Material.SKULL_ITEM,1);
				skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
				itemstack.setDurability((short) 3);
				skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "次ページへ");
				lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
				skullmeta.setLore(lore);
				skullmeta.setOwner("MHF_ArrowRight");
				itemstack.setItemMeta(skullmeta);
				AsyncInventorySetter.setItemAsync(inventory,35,itemstack.clone());

				nextpageflagS = true ;

				break;
			}
			checkTitleS ++ ;
		}
		if(checkTitleS < 9911){
			checkTitleS = 9911 ;
		}
		for(;checkTitleS <= 9932;){
			if(setInv < 27){
				if(!playerdata.getTitleFlags().get(checkTitleS)){
					itemstack = new ItemStack(Material.BEDROCK,1);
					itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
					itemmeta.setDisplayName(String.valueOf(checkTitleS));
					lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "中パーツ「"+ SeichiAssist.config.getTitle2(checkTitleS) +"」"
										,ChatColor.RESET + "" +  ChatColor.GREEN + "必要ポイント：35"
										,ChatColor.RESET + "" +  ChatColor.AQUA + "クリックで購入できます");
					itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
					itemmeta.setLore(lore);
					itemstack.setItemMeta(itemmeta);
					inventory.setItem(setInv,itemstack);

					setInv ++ ;
				}
			}else {
				//次ページへのボタンを配置
				itemstack = new ItemStack(Material.SKULL_ITEM,1);
				skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
				itemstack.setDurability((short) 3);
				skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "次ページへ");
				lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
				skullmeta.setLore(lore);
				skullmeta.setOwner("MHF_ArrowRight");
				itemstack.setItemMeta(skullmeta);
				AsyncInventorySetter.setItemAsync(inventory,35,itemstack.clone());

				nextpageflagS = true ;

				break;
			}
			checkTitleS ++ ;
		}




		// 二つ名組合せメインページを開く
		itemstack = new ItemStack(Material.BARRIER,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BARRIER);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "二つ名組合せメインメニューへ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(27,itemstack);

		return inventory ;
	}

	//実績カテ「整地」
	public static Inventory getTitleSeichi(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
			Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			return null;
		}

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "カテゴリ「整地」");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore;


		//ボタン情報
		itemstack = new ItemStack(Material.IRON_PICKAXE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.IRON_PICKAXE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「整地量」" );
		lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "獲得状況を表示します。");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(12,itemstack);

		itemstack = new ItemStack(Material.DIAMOND_PICKAXE,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_PICKAXE);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「整地神ランキング」" );
		lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "獲得状況を表示します。");
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(14,itemstack);

		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績・二つ名メニューへ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,27,itemstack.clone());

		return inventory;
	}

	//実績カテ「建築」
	public static Inventory getTitleBuild(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
			Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			return null;
		}

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "カテゴリ「建築」");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore;


		//ボタン情報
		// [ここにカテゴリを設定]

		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績・二つ名メニューへ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,27,itemstack.clone());

		return inventory;
	}

	//実績カテ「ログイン」
	public static Inventory getTitleLogin(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
			Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			return null;
		}

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "カテゴリ「ログイン」");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore;


		//ボタン情報
		itemstack = new ItemStack(Material.COMPASS,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COMPASS);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「参加時間」" );
		lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "獲得状況を表示します。");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(10,itemstack);

		itemstack = new ItemStack(Material.BOOK,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BOOK);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「通算ログイン」" );
		lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "獲得状況を表示します。");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(12,itemstack);

		itemstack = new ItemStack(Material.BOOK_AND_QUILL,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BOOK_AND_QUILL);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「連続ログイン」" );
		lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "獲得状況を表示します。");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(14,itemstack);

		itemstack = new ItemStack(Material.NETHER_STAR,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.NETHER_STAR);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「記念日」" );
		lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "獲得状況を表示します。");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(16,itemstack);

		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績・二つ名メニューへ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,27,itemstack.clone());

		return inventory;
	}

	//実績カテ「やりこみ」
	public static Inventory getTitleSuperTry(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
			Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			return null;
		}

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "カテゴリ「やりこみ」");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore;


		//ボタン情報
		itemstack = new ItemStack(Material.DIAMOND_HELMET,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_HELMET);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「MEBIUSブリーダー」" );
		lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "獲得状況を表示します。※未実装");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(12,itemstack);

		itemstack = new ItemStack(Material.GOLD_INGOT,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.GOLD_INGOT);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「スターレベル」" );
		lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "獲得状況を表示します。※未実装");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(14,itemstack);

		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績・二つ名メニューへ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,27,itemstack.clone());

		return inventory;
	}

	//実績カテ「特殊」
	public static Inventory getTitleSpecial(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
			Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			return null;
		}

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "カテゴリ「特殊」");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore;


		//ボタン情報
		itemstack = new ItemStack(Material.BLAZE_POWDER,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BLAZE_POWDER);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「公式イベント」" );
		lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "獲得状況を表示します。");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(11,itemstack);

		itemstack = new ItemStack(Material.YELLOW_FLOWER,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.YELLOW_FLOWER);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「JMS投票数」" );
		lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "獲得状況を表示します。");
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(13,itemstack);

		itemstack = new ItemStack(Material.DIAMOND_BARDING,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BARDING);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績「極秘任務」" );
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "獲得状況を表示します。"
							,ChatColor.RESET + "" +  ChatColor.AQUA + "※獲得前の実績は表示されません。" );
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(15,itemstack);

		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "実績・二つ名メニューへ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,27,itemstack.clone());

		return inventory;
	}

	//実績「整地神ランキング」
	public static Inventory getTitleRankData(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
			Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			return null;
		}

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「整地神ランキング」");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore;


		//実績設定・解禁ボタン
		//1位
		if(playerdata.getTitleFlags().get(1001)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1001「"+ SeichiAssist.config.getTitle1(1001) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」1位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1001「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」1位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}
		//5位
		if(playerdata.getTitleFlags().get(1002)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1002「"+ SeichiAssist.config.getTitle1(1002) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」5位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1002「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」5位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}
		//27位
		if(playerdata.getTitleFlags().get(1003)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1003「"+ SeichiAssist.config.getTitle1(1003) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」27位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1003「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」27位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}
		//50位
		if(playerdata.getTitleFlags().get(1004)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1004「"+ SeichiAssist.config.getTitle1(1004) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」50位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(3,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1004「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」50位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(3,itemstack);
		}
		//100位
		if(playerdata.getTitleFlags().get(1010)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1010「"+ SeichiAssist.config.getTitle1(1010) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」100位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(4,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1010「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」100位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(4,itemstack);
		}
		//250位
		if(playerdata.getTitleFlags().get(1011)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1011「"+ SeichiAssist.config.getTitle1(1011)
					+ SeichiAssist.config.getTitle2(9904) + SeichiAssist.config.getTitle3(1011) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」250位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(5,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1011「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」250位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(5,itemstack);
		}
		//500位
		if(playerdata.getTitleFlags().get(1012)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1012「"+ SeichiAssist.config.getTitle1(1012)
					+ SeichiAssist.config.getTitle2(9901) + SeichiAssist.config.getTitle3(1012) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」500位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(6,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1012「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」500位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(6,itemstack);
		}
		//750位
		if(playerdata.getTitleFlags().get(1005)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1005「"+ SeichiAssist.config.getTitle1(1005)
					+ SeichiAssist.config.getTitle3(1005) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」750位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(7,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1005「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」750位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(7,itemstack);
		}
		//1000位
		if(playerdata.getTitleFlags().get(1006)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1006「"+ SeichiAssist.config.getTitle1(1006) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」1000位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(8,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1006「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」1000位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(8,itemstack);
		}
		//2500位
		if(playerdata.getTitleFlags().get(1007)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1007「"+ SeichiAssist.config.getTitle1(1007)
					+ SeichiAssist.config.getTitle2(9904) + SeichiAssist.config.getTitle3(1007) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」2500位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(9,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1007「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」2500位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(9,itemstack);
		}
		//5000位
		if(playerdata.getTitleFlags().get(1008)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1008「"+ SeichiAssist.config.getTitle1(1008)
					+ SeichiAssist.config.getTitle2(9901) + SeichiAssist.config.getTitle3(1008) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」5000位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(10,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1008「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」5000位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(10,itemstack);
		}
		//10000位
		if(playerdata.getTitleFlags().get(1009)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1009「"+ SeichiAssist.config.getTitle1(1009)
					+ SeichiAssist.config.getTitle2(9909) + SeichiAssist.config.getTitle3(1009) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」10000位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(11,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No1009「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：「整地神ランキング」10000位達成"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(11,itemstack);
		}



		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "「整地」カテゴリメニューへ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,27,itemstack.clone());

		return inventory;
	}


	//実績「整地量」
	public static Inventory getTitleAmountData(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
			Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			return null;
		}

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「整地量」");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore;


		//実績設定・解禁ボタン
		//90億突破前は表示されない処理
		if(playerdata.getTitleFlags().get(3018)){
			//100億
			if(playerdata.getTitleFlags().get(3019)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3019「"+ SeichiAssist.config.getTitle1(3019) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 100億 を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(25,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3019「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が???を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(25,itemstack);
			}
		}else{
		}
		//80億突破前は表示されない処理
		if(playerdata.getTitleFlags().get(3017)){
			//90億
			if(playerdata.getTitleFlags().get(3018)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3018「"+ SeichiAssist.config.getTitle1(3018) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 90億 を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(24,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3018「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が???を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(24,itemstack);
			}
		}else{
		}
		//70億突破前は表示されない処理
		if(playerdata.getTitleFlags().get(3016)){
			//80億
			if(playerdata.getTitleFlags().get(3017)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3017「"+ SeichiAssist.config.getTitle1(3017) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 80億 を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(23,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3017「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が???を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(23,itemstack);
			}
		}else{
		}
		//60億突破前は表示されない処理
		if(playerdata.getTitleFlags().get(3015)){
			//70億
			if(playerdata.getTitleFlags().get(3016)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3016「"+ SeichiAssist.config.getTitle1(3016) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 70億 を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(22,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3016「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が???を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(22,itemstack);
			}
		}else{
		}
		//50億突破前は表示されない処理
		if(playerdata.getTitleFlags().get(3014)){
			//60億
			if(playerdata.getTitleFlags().get(3015)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3015「"+ SeichiAssist.config.getTitle1(3015) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 60億 を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(21,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3015「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が???を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(21,itemstack);
			}
		}else{
		}
		//int実績突破前は表示されない処理
		if(playerdata.getTitleFlags().get(3001)){
			//50億
			if(playerdata.getTitleFlags().get(3014)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3014「"+ SeichiAssist.config.getTitle1(3014)
						+ SeichiAssist.config.getTitle2(9909) + SeichiAssist.config.getTitle3(3014) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 50億 を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(20,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3014「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が???を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(20,itemstack);
			}
			//40億
			if(playerdata.getTitleFlags().get(3013)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3013「"+ SeichiAssist.config.getTitle1(3013)
						+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(3013) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 40億 を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(19,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3013「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が???を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(19,itemstack);
			}
			//30億
			if(playerdata.getTitleFlags().get(3012)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3012「"+ SeichiAssist.config.getTitle1(3012) +
						SeichiAssist.config.getTitle3(3012) + "」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 30億 を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(18,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3012「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が???を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(18,itemstack);
			}
		}else{
		}
		//「10億」実績突破前は表示されない処理
		if(playerdata.getTitleFlags().get(3002)){
			//int限界突破
			if(playerdata.getTitleFlags().get(3001)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3001「"+ SeichiAssist.config.getTitle1(3001) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が「int型の壁」を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(9,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3001「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が???を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(9,itemstack);
			}
		}else{
		}
		//10億突破
		if(playerdata.getTitleFlags().get(3002)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3002「"+ SeichiAssist.config.getTitle1(3002)
					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(3002) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 10億 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3002「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 10億 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}
		//5億突破
		if(playerdata.getTitleFlags().get(3003)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3003「"+ SeichiAssist.config.getTitle1(3003) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 5億 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3003「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 5億 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}
		//1億突破
		if(playerdata.getTitleFlags().get(3004)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3004「"+ SeichiAssist.config.getTitle1(3004)
					+ SeichiAssist.config.getTitle2(9902) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 1億 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3004「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 1億 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}
		//5000万突破
		if(playerdata.getTitleFlags().get(3005)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3005「"+ SeichiAssist.config.getTitle1(3005)
					+ SeichiAssist.config.getTitle3(3005) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 5000万 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(3,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3005「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 5000万 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(3,itemstack);
		}
		//1000万突破
		if(playerdata.getTitleFlags().get(3006)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3006「"+ SeichiAssist.config.getTitle1(3006) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 1000万 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(4,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3006「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 1000万 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(4,itemstack);
		}
		//500万突破
		if(playerdata.getTitleFlags().get(3007)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3007「"+ SeichiAssist.config.getTitle1(3007)
					+ SeichiAssist.config.getTitle2(9905) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 500万 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(5,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3007「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 500万 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(5,itemstack);
		}
		//100万突破
		if(playerdata.getTitleFlags().get(3008)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3008「"+ SeichiAssist.config.getTitle1(3008) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 100万 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(6,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3008「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 100万 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(6,itemstack);
		}
		//50万突破
		if(playerdata.getTitleFlags().get(3009)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3009「"+ SeichiAssist.config.getTitle1(3009)
					+ SeichiAssist.config.getTitle3(3009) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 50万 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(7,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3009「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 50万 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(7,itemstack);
		}
		//10万突破
		if(playerdata.getTitleFlags().get(3010)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3010「"+ SeichiAssist.config.getTitle1(3010)
					+ SeichiAssist.config.getTitle2(9909) + SeichiAssist.config.getTitle3(3010) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 10万 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(8,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3010「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 10万 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(8,itemstack);
		}
		//1万突破
		if(playerdata.getTitleFlags().get(3011)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3011「"+ SeichiAssist.config.getTitle1(3011) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 10000 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(17,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No3011「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：整地量が 10000 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(17,itemstack);
		}


		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "「整地」カテゴリメニューへ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,27,itemstack.clone());

		return inventory;
	}



	//実績「参加時間」
	public static Inventory getTitleTimeData(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
			Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			return null;
		}

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「参加時間」");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore;


		//実績設定・解禁ボタン
		//「18000h」実績突破前は表示されない処理
		if(playerdata.getTitleFlags().get(4022)){
			//20000h突破
			if(playerdata.getTitleFlags().get(4023)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4023「"+ SeichiAssist.config.getTitle1(4023)
						+ SeichiAssist.config.getTitle3(4023) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 20000時間 を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(22,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4023「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が ??? を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(22,itemstack);
			}
		}else{
		}
		//「16000h」実績突破前は表示されない処理
		if(playerdata.getTitleFlags().get(4021)){
			//18000h突破
			if(playerdata.getTitleFlags().get(4022)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4022「"+ SeichiAssist.config.getTitle1(4022)
						+ SeichiAssist.config.getTitle2(9903) + SeichiAssist.config.getTitle3(4022) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 18000時間 を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(21,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4022「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が ??? を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(21,itemstack);
			}
		}else{
		}
		//「14000h」実績突破前は表示されない処理
		if(playerdata.getTitleFlags().get(4020)){
			//16000h突破
			if(playerdata.getTitleFlags().get(4021)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4021「"+ SeichiAssist.config.getTitle1(4021)
						+ SeichiAssist.config.getTitle3(4021) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 16000時間 を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(20,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4021「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が ??? を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(20,itemstack);
			}
		}else{
		}
		//「12000h」実績突破前は表示されない処理
		if(playerdata.getTitleFlags().get(4019)){
			//14000h突破
			if(playerdata.getTitleFlags().get(4020)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4020「"+ SeichiAssist.config.getTitle1(4020)
						+ SeichiAssist.config.getTitle3(4020) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 14000時間 を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(19,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4020「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が ??? を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(19,itemstack);
			}
		}else{
		}
		//「10000h」実績突破前は表示されない処理
		if(playerdata.getTitleFlags().get(4018)){
			//12000h突破
			if(playerdata.getTitleFlags().get(4019)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4019「"+ SeichiAssist.config.getTitle1(4019)
						+ SeichiAssist.config.getTitle3(4019) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 12000時間 を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(18,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4019「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が ??? を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(18,itemstack);
			}
		}else{
		}
		//「5000h」実績突破前は表示されない処理
		if(playerdata.getTitleFlags().get(4013)){
			//10000h突破
			if(playerdata.getTitleFlags().get(4018)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4018「"+ SeichiAssist.config.getTitle1(4018)
						+ SeichiAssist.config.getTitle3(4018) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 10000時間 を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(17,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4018「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が ??? を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(17,itemstack);
			}
			//9000h突破
			if(playerdata.getTitleFlags().get(4017)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4017「"+ SeichiAssist.config.getTitle1(4017) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 9000時間 を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(16,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4017「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が ??? を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(16,itemstack);
			}
			//8000h突破
			if(playerdata.getTitleFlags().get(4016)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4016「"+ SeichiAssist.config.getTitle1(4016)
						+ SeichiAssist.config.getTitle3(4016) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 8000時間 を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(15,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4016「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が ??? を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(15,itemstack);
			}
			//7000h突破
			if(playerdata.getTitleFlags().get(4015)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4015「"+ SeichiAssist.config.getTitle1(4015) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 7000時間 を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(14,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4015「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が ??? を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(14,itemstack);
			}
			//6000h突破
			if(playerdata.getTitleFlags().get(4014)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4014「"+ SeichiAssist.config.getTitle1(4014)
						+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(4014) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 6000時間 を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(13,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4014「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が ??? を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(13,itemstack);
			}

		}else{
		}
		//「1000h」実績突破前は表示されない処理
		if(playerdata.getTitleFlags().get(4002)){
			//5000h突破
			if(playerdata.getTitleFlags().get(4013)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4013「"+ SeichiAssist.config.getTitle1(4013)
						+ SeichiAssist.config.getTitle3(4013) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 5000時間 を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(12,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4013「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が ??? を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(12,itemstack);
			}
			//4000h突破
			if(playerdata.getTitleFlags().get(4012)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4012「"+ SeichiAssist.config.getTitle1(4012)
						+ SeichiAssist.config.getTitle3(4012) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 4000時間 を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(11,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4012「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が ??? を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(11,itemstack);
			}
			//3000h突破
			if(playerdata.getTitleFlags().get(4011)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4011「"+ SeichiAssist.config.getTitle1(4011)
						+ SeichiAssist.config.getTitle2(9901) + SeichiAssist.config.getTitle3(4011) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 3000時間 を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(10,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4011「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が ??? を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(10,itemstack);
			}
			//2000h突破
			if(playerdata.getTitleFlags().get(4001)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4001「"+ SeichiAssist.config.getTitle1(4001)
						+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(4001) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 2000時間 を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(9,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4001「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が ??? を超える"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(9,itemstack);
			}

		}else{
		}
		//1000h突破
		if(playerdata.getTitleFlags().get(4002)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4002「"+ SeichiAssist.config.getTitle1(4002)
					+ SeichiAssist.config.getTitle3(4002) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 1000時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4002「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 1000時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}
		//500h突破
		if(playerdata.getTitleFlags().get(4003)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4003「"+ SeichiAssist.config.getTitle1(4003)
					+ SeichiAssist.config.getTitle3(4003) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 500時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4003「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 500時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}
		//250h突破
		if(playerdata.getTitleFlags().get(4004)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4004「"+ SeichiAssist.config.getTitle1(4004)
					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(4004) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 250時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4004「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 250時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}
		//100h突破
		if(playerdata.getTitleFlags().get(4005)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4005「"+ SeichiAssist.config.getTitle1(4005)
					+ SeichiAssist.config.getTitle3(4005) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 100時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(3,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4005「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 100時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(3,itemstack);
		}
		//50h突破
		if(playerdata.getTitleFlags().get(4006)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4006「"+ SeichiAssist.config.getTitle1(4006)
					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(4006) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 50時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(4,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4006「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 50時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(4,itemstack);
		}
		//24h突破
		if(playerdata.getTitleFlags().get(4007)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4007「"+ SeichiAssist.config.getTitle1(4007)
					+ SeichiAssist.config.getTitle3(4007) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 24時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(5,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4007「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 24時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(5,itemstack);
		}
		//10h突破
		if(playerdata.getTitleFlags().get(4008)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4008「"+ SeichiAssist.config.getTitle1(4008)
					+ SeichiAssist.config.getTitle3(4008) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 10時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(6,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4008「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 10時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(6,itemstack);
		}
		//5h突破
		if(playerdata.getTitleFlags().get(4009)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4009「"+ SeichiAssist.config.getTitle1(4009)
					+ SeichiAssist.config.getTitle3(4009) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 5時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(7,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4009「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 5時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(7,itemstack);
		}
		//1h突破
		if(playerdata.getTitleFlags().get(4010)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4010「"+ SeichiAssist.config.getTitle1(4010)
					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(4010) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 1時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(8,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No4010「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：参加時間が 1時間 を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(8,itemstack);
		}
		if(playerdata.getPlaytick() % 576000 >= 0 && playerdata.getPlaytick() % 576000 <= 1199 && !(playerdata.getTitleFlags().get(8003))){
			itemstack = new ItemStack(Material.EMERALD_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.EMERALD_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "タイムカード、切りましょ？" );
			lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "※何かが起こります※");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(35,itemstack);
		}


		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "「ログイン」カテゴリメニューへ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,27,itemstack.clone());

		return inventory;
	}

	//実績「通算ログイン」
	public static Inventory getTitleJoinAmountData(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
			Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			return null;
		}

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「通算ログイン」");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore;


		//実績設定・解禁ボタン
		//「1000日」実績突破前は表示されない処理
		if(playerdata.getTitleFlags().get(5119)){
			if(playerdata.getTitleFlags().get(5120)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5120「"+ SeichiAssist.config.getTitle1(5120)
						+ SeichiAssist.config.getTitle2(5120) + SeichiAssist.config.getTitle3(5120) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 1095日 に到達"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(19,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5120「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が ??? に到達"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(19,itemstack);
			}
		}else{
		}
		//「900日」実績突破前は表示されない処理
		if(playerdata.getTitleFlags().get(5118)){
			if(playerdata.getTitleFlags().get(5119)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5119「"+ SeichiAssist.config.getTitle1(5119)
						+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(5119) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 1000日 に到達"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(18,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5119「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が ??? に到達"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(18,itemstack);
			}
		}else{
		}
		//「800日」実績突破前は表示されない処理
		if(playerdata.getTitleFlags().get(5117)){
			if(playerdata.getTitleFlags().get(5118)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5118「"+ SeichiAssist.config.getTitle1(5118)
						+ SeichiAssist.config.getTitle3(5118) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 900日 に到達"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(17,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5118「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が ??? に到達"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(17,itemstack);
			}
		}else{
		}
		//「730日」実績突破前は表示されない処理
		if(playerdata.getTitleFlags().get(5116)){
			if(playerdata.getTitleFlags().get(5117)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5117「"+ SeichiAssist.config.getTitle1(5117)
						+ SeichiAssist.config.getTitle3(5117) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 800日 に到達"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(16,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5117「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が ??? に到達"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(16,itemstack);
			}
		}else{
		}
		//「700日」実績突破前は表示されない処理
		if(playerdata.getTitleFlags().get(5115)){
			if(playerdata.getTitleFlags().get(5116)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5116「"+ SeichiAssist.config.getTitle1(5116)
						+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(5116) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 730日 に到達"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(15,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5116「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が ??? に到達"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(15,itemstack);
			}
		}else{
		}
		//「600日」実績突破前は表示されない処理
		if(playerdata.getTitleFlags().get(5114)){
			if(playerdata.getTitleFlags().get(5115)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5115「"+ SeichiAssist.config.getTitle1(5115) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 700日 に到達"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(14,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5115「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が ??? に到達"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(14,itemstack);
			}
		}else{
		}
		//「500日」実績突破前は表示されない処理
		if(playerdata.getTitleFlags().get(5113)){
			if(playerdata.getTitleFlags().get(5114)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5114「"+ SeichiAssist.config.getTitle1(5114)
						+ SeichiAssist.config.getTitle3(5114) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 600日 に到達"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(13,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5114「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が ??? に到達"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(13,itemstack);
			}
		}else{
		}
		//「400日」実績突破前は表示されない処理
		if(playerdata.getTitleFlags().get(5112)){
			if(playerdata.getTitleFlags().get(5113)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5113「"+ SeichiAssist.config.getTitle1(5113)
						+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(5113) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 500日 に到達"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(12,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5113「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が ??? に到達"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(12,itemstack);
			}
		}else{
		}
		//「365日」実績突破前は表示されない処理
		if(playerdata.getTitleFlags().get(5101)){
			if(playerdata.getTitleFlags().get(5112)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5112「"+ SeichiAssist.config.getTitle1(5112)
						 + SeichiAssist.config.getTitle3(5112) +"」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 400日 に到達"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(11,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5112「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が ??? に到達"
									,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
									,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(11,itemstack);
			}
		}else{
		}
		if(playerdata.getTitleFlags().get(5101)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5101「"+ SeichiAssist.config.getTitle1(5101)
					 + SeichiAssist.config.getTitle3(5101) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 365日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(10,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5101「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 365日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(10,itemstack);
		}
		if(playerdata.getTitleFlags().get(5102)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5102「"+ SeichiAssist.config.getTitle1(5102)
					 + SeichiAssist.config.getTitle2(9907) + SeichiAssist.config.getTitle3(5102) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 300日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(9,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5102「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 300日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(9,itemstack);
		}
		if(playerdata.getTitleFlags().get(5103)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5103「"+ SeichiAssist.config.getTitle1(5103)
					 + SeichiAssist.config.getTitle2(9905) + "」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 200日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(8,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5103「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 200日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(8,itemstack);
		}
		if(playerdata.getTitleFlags().get(5104)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5104「"+ SeichiAssist.config.getTitle1(5104)
					 + SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(5104) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 100日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(7,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5104「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 100日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(7,itemstack);
		}
		if(playerdata.getTitleFlags().get(5105)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5105「"+ SeichiAssist.config.getTitle1(5105)
					 + SeichiAssist.config.getTitle2(9907) + SeichiAssist.config.getTitle3(5105) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 75日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(6,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5105「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 75日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(6,itemstack);
		}
		if(playerdata.getTitleFlags().get(5106)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5106「"+ SeichiAssist.config.getTitle1(5106)+"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 50日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(5,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5106「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 50日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(5,itemstack);
		}
		if(playerdata.getTitleFlags().get(5107)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5107「"+ SeichiAssist.config.getTitle1(5107)
					 + SeichiAssist.config.getTitle2(9909) + SeichiAssist.config.getTitle3(5107) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 30日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(4,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5107「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 30日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(4,itemstack);
		}
		if(playerdata.getTitleFlags().get(5108)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5108「"+ SeichiAssist.config.getTitle1(5108)
					 + SeichiAssist.config.getTitle3(5108) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 20日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(3,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5108「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 20日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(3,itemstack);
		}
		if(playerdata.getTitleFlags().get(5109)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5109「"+ SeichiAssist.config.getTitle1(5109)
					 +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 10日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5109「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 10日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}
		if(playerdata.getTitleFlags().get(5110)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5110「"+ SeichiAssist.config.getTitle1(5110)+"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 5日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5110「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 5日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}
		if(playerdata.getTitleFlags().get(5111)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5111「"+ SeichiAssist.config.getTitle1(5111)+"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 2日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5111「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：通算ログイン日数が 2日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}

		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "「ログイン」カテゴリメニューへ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,27,itemstack.clone());

		return inventory;
	}

	//実績「連続ログイン」
	public static Inventory getTitleJoinChainData(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
			Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			return null;
		}

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「連続ログイン」");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore;


		//実績設定・解禁ボタン
		if(playerdata.getTitleFlags().get(5001)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5001「"+ SeichiAssist.config.getTitle1(5001)
					+ SeichiAssist.config.getTitle2(5001) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：連続ログイン日数が 100日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5001「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：連続ログイン日数が 100日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}
		if(playerdata.getTitleFlags().get(5002)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5002「"+ SeichiAssist.config.getTitle1(5002)
					+ SeichiAssist.config.getTitle3(5002) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：連続ログイン日数が 50日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5002「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：連続ログイン日数が 50日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}
		if(playerdata.getTitleFlags().get(5003)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5003「"+ SeichiAssist.config.getTitle1(5003)+"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：連続ログイン日数が 30日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5003「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：連続ログイン日数が 30日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}
		if(playerdata.getTitleFlags().get(5004)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5004「"+ SeichiAssist.config.getTitle1(5004)
					+ SeichiAssist.config.getTitle3(5004) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：連続ログイン日数が 20日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(3,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5004「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：連続ログイン日数が 20日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(3,itemstack);
		}
		if(playerdata.getTitleFlags().get(5005)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5005「"+ SeichiAssist.config.getTitle1(5005)
					+ SeichiAssist.config.getTitle3(5005) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：連続ログイン日数が 10日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(4,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5005「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：連続ログイン日数が 10日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(4,itemstack);
		}
		if(playerdata.getTitleFlags().get(5006)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5006「"+ SeichiAssist.config.getTitle1(5006)
					+ SeichiAssist.config.getTitle3(5006) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：連続ログイン日数が 5日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(5,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5006「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：連続ログイン日数が 5日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(5,itemstack);
		}
		if(playerdata.getTitleFlags().get(5007)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5007「"+ SeichiAssist.config.getTitle1(5007)+"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：連続ログイン日数が 3日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(6,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5007「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：連続ログイン日数が 3日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(6,itemstack);
		}
		if(playerdata.getTitleFlags().get(5008)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5008「"+ SeichiAssist.config.getTitle1(5008)
					 + SeichiAssist.config.getTitle2(9905) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：連続ログイン日数が 2日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(7,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No5008「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：連続ログイン日数が 2日 に到達"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(7,itemstack);
		}

		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "「ログイン」カテゴリメニューへ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,27,itemstack.clone());

		return inventory;
	}


	//実績「JMS投票数」
	public static Inventory getTitleSupportData(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
			Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			return null;
		}

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「JMS投票数」");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore;


		//実績設定・解禁ボタン
		//投票数365
		if(playerdata.getTitleFlags().get(6001)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6001「"+ SeichiAssist.config.getTitle1(6001) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が365を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6001「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が365を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}
		//投票数200
		if(playerdata.getTitleFlags().get(6002)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6002「"+ SeichiAssist.config.getTitle1(6002)
					+ SeichiAssist.config.getTitle3(6002) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が200を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6002「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が200を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}
		//投票数100
		if(playerdata.getTitleFlags().get(6003)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6003「"+ SeichiAssist.config.getTitle1(6003) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が100を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6003「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が100を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}
		//投票数50
		if(playerdata.getTitleFlags().get(6004)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6004「"+ SeichiAssist.config.getTitle1(6004)
					+ SeichiAssist.config.getTitle2(9903) + SeichiAssist.config.getTitle3(6004) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が50を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(3,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6004「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が50を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(3,itemstack);
		}
		//投票数25
		if(playerdata.getTitleFlags().get(6005)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6005「"+ SeichiAssist.config.getTitle1(6005)
					+ SeichiAssist.config.getTitle2(9905) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が25を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(4,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6005「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が25を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(4,itemstack);
		}
		//投票数10
		if(playerdata.getTitleFlags().get(6006)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6006「"+ SeichiAssist.config.getTitle1(6006)
					+ SeichiAssist.config.getTitle3(6006) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が10を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(5,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6006「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が10を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(5,itemstack);
		}
		//投票数5
		if(playerdata.getTitleFlags().get(6007)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6007「"+ SeichiAssist.config.getTitle1(6007)
					+ SeichiAssist.config.getTitle2(9902) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が5を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(6,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6007「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が5を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(6,itemstack);
		}
		//投票数1
		if(playerdata.getTitleFlags().get(6008)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6008「"+ SeichiAssist.config.getTitle1(6008) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が1を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(7,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No6008「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：JMS投票数が1を超える"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(7,itemstack);
		}


		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "「特殊」カテゴリメニューへ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,27,itemstack.clone());

		return inventory;
	}

	//実績「公式イベント」
	public static Inventory getTitleEventData(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
			Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			return null;
		}

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「公式イベント」");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore;


		//実績設定・解禁ボタン
		if(playerdata.getTitleFlags().get(7001)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7001「"+ SeichiAssist.config.getTitle1(7001)
					+ SeichiAssist.config.getTitle2(9901) + SeichiAssist.config.getTitle3(7001) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「整地大会」に参加する"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7001「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「整地大会」に参加する"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}
		if(playerdata.getTitleFlags().get(7002)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7002「"+ SeichiAssist.config.getTitle1(7002)
					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(7002) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「整地大会」にて総合優勝"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7002「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「整地大会」にて総合優勝"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}
		if(playerdata.getTitleFlags().get(7003)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7003「"+ SeichiAssist.config.getTitle1(7003)
					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(7003) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「建築コンペ」で最優秀賞獲得"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7003「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「建築コンペ」で最優秀賞獲得"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}
		if(playerdata.getTitleFlags().get(7004)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7004「"+ SeichiAssist.config.getTitle2(7004) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「建築コンペ」で優秀賞獲得"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(3,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7004「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「建築コンペ」で優秀賞獲得"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(3,itemstack);
		}
		if(playerdata.getTitleFlags().get(7005)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7005「"+ SeichiAssist.config.getTitle1(7005)
					+ SeichiAssist.config.getTitle2(9902) + SeichiAssist.config.getTitle3(7005) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「建築コンペ」で佳作賞獲得"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(4,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7005「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「建築コンペ」で佳作賞獲得"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(4,itemstack);
		}
		if(playerdata.getTitleFlags().get(7006)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7006「"+ SeichiAssist.config.getTitle1(7006)
					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(7006) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「第一回建築コンペ」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "開催テーマは「桜」でした。"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(5,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7006「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「第一回建築コンペ」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "開催テーマは「桜」でした。"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(5,itemstack);
		}
		if(playerdata.getTitleFlags().get(7007)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7007「"+ SeichiAssist.config.getTitle1(7007)
					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(7007) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「第二回建築コンペ」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "開催テーマは「アスレチック」でした。"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(6,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7007「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「第二回建築コンペ」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "開催テーマは「アスレチック」でした。"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(6,itemstack);
		}
		//以下の二つはデータだけ先に実装しています。(使いそうだけど使うか分からない)
		//一応このままの状態でも解禁コマンドは使用可能。
		if(playerdata.getTitleFlags().get(7008)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7008「"+ SeichiAssist.config.getTitle1(7008)
					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(7008) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「GTテクスチャコンペ」で採用"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(7,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7008「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「GTテクスチャコンペ」で採用"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(7,itemstack);
		}
		if(playerdata.getTitleFlags().get(7009)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7009「"+ SeichiAssist.config.getTitle1(7009)
					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(7009) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「第二回整地大会」で優勝"
								,ChatColor.RESET + "" +  ChatColor.RED + "整地鯖一周年記念イベントでした！"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(8,itemstack);
		}

		if(playerdata.getTitleFlags().get(7010)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7010「"+ SeichiAssist.config.getTitle1(7010)
					+ SeichiAssist.config.getTitle3(7010) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「第三回建築コンペ」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "開催テーマＡは「氷像(夏)」でした。"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(9,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7010「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「第三回建築コンペ」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "開催テーマＡは「氷像(夏)」でした。"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(9,itemstack);
		}
		if(playerdata.getTitleFlags().get(7011)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7011「"+ SeichiAssist.config.getTitle1(7011)
					+ SeichiAssist.config.getTitle3(9905) + SeichiAssist.config.getTitle3(7011) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「第三回建築コンペ」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "開催テーマＢは「海岸建築(夏)」でした。"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(10,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7011「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「第三回建築コンペ」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "開催テーマＢは「海岸建築(夏)」でした。"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(10,itemstack);
		}
		if(playerdata.getTitleFlags().get(7012)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7012「"+ SeichiAssist.config.getTitle1(7012)
					+ SeichiAssist.config.getTitle3(7012) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「第三回建築コンペ」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "開催テーマＣは「海上建築(夏)」でした。"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(11,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7012「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「第三回建築コンペ」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "開催テーマＣは「海上建築(夏)」でした。"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(11,itemstack);
		}
		if(playerdata.getTitleFlags().get(7013)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7013「"+ SeichiAssist.config.getTitle1(7013) + "」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「第四回建築コンペ」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "開催テーマＡは「和風建築」でした。"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(12,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7013「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「第四回建築コンペ」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "開催テーマＡは「和風建築」でした。"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(12,itemstack);
		}
		if(playerdata.getTitleFlags().get(7014)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7014「"+ SeichiAssist.config.getTitle1(7014) + "」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「第四回建築コンペ」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "開催テーマＢは「洋風建築」でした。"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(13,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7014「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「第四回建築コンペ」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "開催テーマＢは「洋風建築」でした。"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(13,itemstack);
		}
		if(playerdata.getTitleFlags().get(7015)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7015「"+ SeichiAssist.config.getTitle1(7015)
					+ SeichiAssist.config.getTitle3(9904) + SeichiAssist.config.getTitle3(7015) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「第四回建築コンペ」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "開催テーマＣは「モダン建築」でした。"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(14,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7015「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「第四回建築コンペ」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "開催テーマＣは「モダン建築」でした。"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(14,itemstack);
		}
		if(playerdata.getTitleFlags().get(7016)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7016「"+ SeichiAssist.config.getTitle1(7016)
					+ SeichiAssist.config.getTitle3(7016) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「第四回建築コンペ」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "開催テーマＤは「ファンタジー」でした。"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(15,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7016「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「第四回建築コンペ」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "開催テーマＤは「ファンタジー」でした。"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(15,itemstack);
		}
		if(playerdata.getTitleFlags().get(7017)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7017「"+ SeichiAssist.config.getTitle1(7017)
					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(7017) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "条件：グランプリ獲得"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(16,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7017「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "条件：グランプリ獲得"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(16,itemstack);
		}
		if(playerdata.getTitleFlags().get(7018)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7018「"+ SeichiAssist.config.getTitle1(7018)
					+ SeichiAssist.config.getTitle2(9904) + SeichiAssist.config.getTitle3(7018) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "条件：参加賞"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(17,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7018「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "条件：参加賞"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(17,itemstack);
		}
		if(playerdata.getTitleFlags().get(7019)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7019「"+ SeichiAssist.config.getTitle1(7019)
					+ SeichiAssist.config.getTitle3(7019) + "」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "条件：審査員賞(Yukki_XD)"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(18,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7019「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "条件：審査員賞(Yukki_XD)"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(18,itemstack);
		}
		if(playerdata.getTitleFlags().get(7020)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7020「"+ SeichiAssist.config.getTitle1(7020)
					+ SeichiAssist.config.getTitle3(7020) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "条件：審査員賞(rukure2017)"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(19,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7020「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "条件：審査員賞(rukure2017)"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(19,itemstack);
		}
		if(playerdata.getTitleFlags().get(7021)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7021「"+ SeichiAssist.config.getTitle1(7021)
					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(7021) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "条件：審査員賞(Lucky3028)"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(20,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7021「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "条件：審査員賞(Lucky3028)"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(20,itemstack);
		}
		if(playerdata.getTitleFlags().get(7022)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7022「"+ SeichiAssist.config.getTitle1(7022)
					+ SeichiAssist.config.getTitle3(7022) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "条件：審査員賞(tokuzi_)"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(21,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7022「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "条件：審査員賞(tokuzi_)"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(21,itemstack);
		}
		if(playerdata.getTitleFlags().get(7023)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7023「"+ SeichiAssist.config.getTitle1(7023)
					+ SeichiAssist.config.getTitle3(9905) + SeichiAssist.config.getTitle3(7023) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "条件：審査員賞(kamikami46)"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(22,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7023「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "条件：審査員賞(kamikami46)"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(22,itemstack);
		}
		if(playerdata.getTitleFlags().get(7024)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7024「"+ SeichiAssist.config.getTitle1(7024)
					+ SeichiAssist.config.getTitle3(7024) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "条件：審査員賞(CourageousLeaf)"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(23,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7024「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "条件：審査員賞(CourageousLeaf)"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(23,itemstack);
		}
		if(playerdata.getTitleFlags().get(7025)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7025「"+ SeichiAssist.config.getTitle1(7025)
					+ SeichiAssist.config.getTitle3(9905) + SeichiAssist.config.getTitle3(7025) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "条件：審査員賞(nubasu)"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(24,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7025「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "条件：審査員賞(nubasu)"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(24,itemstack);
		}
		if(playerdata.getTitleFlags().get(7026)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7026「"+ SeichiAssist.config.getTitle1(7026)
					+ SeichiAssist.config.getTitle3(9905) + SeichiAssist.config.getTitle3(7026) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "条件：審査員賞(tetsusan)"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(25,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7026「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "条件：審査員賞(tetsusan)"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(25,itemstack);
		}
		if(playerdata.getTitleFlags().get(7027)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7027「"+ SeichiAssist.config.getTitle1(7027)
					+ SeichiAssist.config.getTitle3(7027) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "条件：審査員賞(tar0ss)"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(26,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7027「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：公式イベント「イラストコンテスト」で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "条件：審査員賞(tar0ss)"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(26,itemstack);
		}
		if(playerdata.getTitleFlags().get(7901)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7901「"+ SeichiAssist.config.getTitle1(7901)
					+ SeichiAssist.config.getTitle2(7901) + SeichiAssist.config.getTitle3(7901) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：超会議2018で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(30,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7901「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：超会議2018で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(30,itemstack);
		}
		if(playerdata.getTitleFlags().get(7902)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7902「"+ SeichiAssist.config.getTitle1(7902)
					+ SeichiAssist.config.getTitle3(7902) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：超会議2018で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(31,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7902「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：超会議2018で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(31,itemstack);
		}
		if(playerdata.getTitleFlags().get(7903)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7903「"+ SeichiAssist.config.getTitle1(7903)
					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(7903) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：超会議2018で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(32,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7903「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：超会議2018で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(32,itemstack);
		}
		if(playerdata.getTitleFlags().get(7904)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7904「"+ SeichiAssist.config.getTitle1(7904)
					+ SeichiAssist.config.getTitle2(9907) + SeichiAssist.config.getTitle3(7904) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：超会議2018で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(33,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7904「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：超会議2018で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(33,itemstack);
		}
		if(playerdata.getTitleFlags().get(7905)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7905「"+ SeichiAssist.config.getTitle1(7905)
					+ SeichiAssist.config.getTitle3(7905) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：超会議2018で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(34,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7905「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：超会議2018で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(34,itemstack);
		}
		if(playerdata.getTitleFlags().get(7906)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7906「"+ SeichiAssist.config.getTitle1(7906)
					+ SeichiAssist.config.getTitle3(7906) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：超会議2018で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(35,itemstack);
		}else{
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No7906「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：超会議2018で配布"
								,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は配布解禁式です");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(35,itemstack);
		}

		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "「特殊」カテゴリメニューへ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,27,itemstack.clone());

		return inventory;
	}


	//実績「特殊」
	public static Inventory getTitleExtraData(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
			Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			return null;
		}

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「記念日」");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore;


		//実績設定・解禁ボタン
		if(playerdata.getTitlepage() == 1){
			if(playerdata.getTitleFlags().get(9001)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9001「"+ SeichiAssist.config.getTitle1(9001) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：とある始まりの日にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(0,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9001「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とある始まりの日にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(0,itemstack);
			}
			if(playerdata.getTitleFlags().get(9002)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9002「"+ SeichiAssist.config.getTitle1(9002)
						+ SeichiAssist.config.getTitle3(9002) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：とある聖夜の日にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(1,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9002「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とある聖夜の日にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(1,itemstack);
			}
			if(playerdata.getTitleFlags().get(9003)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9003「"+ SeichiAssist.config.getTitle1(9003) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：とある終わりの日にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(2,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9003「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とある終わりの日にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(2,itemstack);
			}
			if(playerdata.getTitleFlags().get(9004)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9004「"+ SeichiAssist.config.getTitle1(9004)
						+ SeichiAssist.config.getTitle2(9004) + SeichiAssist.config.getTitle3(9004) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：1月にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(3,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9004「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：1月にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(3,itemstack);
			}
			if(playerdata.getTitleFlags().get(9005)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9005「"+ SeichiAssist.config.getTitle1(9005)
						+ SeichiAssist.config.getTitle3(9005) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：2月にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(4,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9005「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：2月にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(4,itemstack);
			}
			if(playerdata.getTitleFlags().get(9006)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9006「"+ SeichiAssist.config.getTitle1(9006) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：とあるお豆の絨毯爆撃の日にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(5,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9006「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とあるお豆の絨毯爆撃の日にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(5,itemstack);
			}
			if(playerdata.getTitleFlags().get(9007)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9007「"+ SeichiAssist.config.getTitle1(9007) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：建国記念日にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(6,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9007「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：建国記念日にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(6,itemstack);
			}
			if(playerdata.getTitleFlags().get(9008)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9008「"+ SeichiAssist.config.getTitle1(9008)
						+ SeichiAssist.config.getTitle3(9008) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：とあるカカオまみれの日にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(7,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9008「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とあるカカオまみれの日にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(7,itemstack);
			}
			if(playerdata.getTitleFlags().get(9009)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9009「"+ SeichiAssist.config.getTitle1(9009) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：3月にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(8,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9009「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：3月にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(8,itemstack);
			}
			if(playerdata.getTitleFlags().get(9010)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9010「"+ SeichiAssist.config.getTitle1(9010)
						+ SeichiAssist.config.getTitle2(9903) + SeichiAssist.config.getTitle3(9010) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：とある女の子の日にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(9,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9010「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とある女の子の日にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(9,itemstack);
			}
			if(playerdata.getTitleFlags().get(9011)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9011「"+ SeichiAssist.config.getTitle1(9011)
						+ SeichiAssist.config.getTitle3(9011) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：燃え尽きたカカオだらけの日にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(10,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9011「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：燃え尽きたカカオだらけの日にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(10,itemstack);
			}
			if(playerdata.getTitleFlags().get(9012)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9012「"+ SeichiAssist.config.getTitle1(9012)
						+ SeichiAssist.config.getTitle3(9012) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：春分の日にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(11,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9012「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：春分の日にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(11,itemstack);
			}
			if(playerdata.getTitleFlags().get(9013)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9013「"+ SeichiAssist.config.getTitle1(9013) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：4月にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(12,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9013「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：4月にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(12,itemstack);
			}
			if(playerdata.getTitleFlags().get(9014)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9014「"+ SeichiAssist.config.getTitle2(9014) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：とある嘘の日にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(13,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9014「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とある嘘の日にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(13,itemstack);
			}
			if(playerdata.getTitleFlags().get(9015)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9015「"+ SeichiAssist.config.getTitle1(9015)
						+ SeichiAssist.config.getTitle3(9015) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：とある良い子の日にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(14,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9015「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とある良い子の日にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(14,itemstack);
			}
			if(playerdata.getTitleFlags().get(9016)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9016「"+ SeichiAssist.config.getTitle1(9016)
						+ SeichiAssist.config.getTitle2(9016) + SeichiAssist.config.getTitle3(9016) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：とある掃除デーにプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(15,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9016「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とある掃除デーにプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(15,itemstack);
			}
			if(playerdata.getTitleFlags().get(9017)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9017「"+ SeichiAssist.config.getTitle1(9017)
						+ SeichiAssist.config.getTitle3(9017) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：5月にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(16,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9017「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：5月にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(16,itemstack);
			}
			if(playerdata.getTitleFlags().get(9018)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9018「"+ SeichiAssist.config.getTitle1(9018) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：とある子供の日にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(17,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9018「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とある子供の日にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(17,itemstack);
			}
			if(playerdata.getTitleFlags().get(9019)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9019「"+ SeichiAssist.config.getTitle1(9019)
						+ SeichiAssist.config.getTitle2(9901) + SeichiAssist.config.getTitle3(9019) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：端午の節句にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(18,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9019「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：端午の節句にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(18,itemstack);
			}
			if(playerdata.getTitleFlags().get(9020)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9020「"+ SeichiAssist.config.getTitle1(9020)
						+ SeichiAssist.config.getTitle3(9020) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：母の日にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(19,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9020「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：母の日にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(19,itemstack);
			}
			if(playerdata.getTitleFlags().get(9021)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9021「"+ SeichiAssist.config.getTitle1(9021)
						+ SeichiAssist.config.getTitle2(9901) + SeichiAssist.config.getTitle3(9021) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：6月にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(20,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9021「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：6月にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(20,itemstack);
			}
			if(playerdata.getTitleFlags().get(9022)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9022「"+ SeichiAssist.config.getTitle1(9022)
						+ SeichiAssist.config.getTitle3(9022) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：とある日記の日にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(21,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9022「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とある日記の日にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(21,itemstack);
			}
			if(playerdata.getTitleFlags().get(9023)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9023「"+ SeichiAssist.config.getTitle1(9023)
						+ SeichiAssist.config.getTitle3(9023) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：父の日にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(22,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9023「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：父の日にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(22,itemstack);
			}
			if(playerdata.getTitleFlags().get(9024)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9024「"+ SeichiAssist.config.getTitle1(9024)
						+ SeichiAssist.config.getTitle3(9024) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：とある生誕の日にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(23,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9024「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とある生誕の日にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(23,itemstack);
			}
			if(playerdata.getTitleFlags().get(9025)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9025「"+ SeichiAssist.config.getTitle1(9025)
						+ SeichiAssist.config.getTitle2(9025) + SeichiAssist.config.getTitle3(9025) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：7月にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(24,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9025「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：7月にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(24,itemstack);
			}
			if(playerdata.getTitleFlags().get(9026)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9026「"+ SeichiAssist.config.getTitle1(9026)
						+ SeichiAssist.config.getTitle3(9026) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：七夕にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(25,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9026「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：七夕にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(25,itemstack);
			}
			if(playerdata.getTitleFlags().get(9027)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9027「"+ SeichiAssist.config.getTitle1(9027)
						+ SeichiAssist.config.getTitle3(9027) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：とある東京の日にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(26,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9027「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とある東京の日にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(26,itemstack);
			}
			if(playerdata.getTitleFlags().get(9028)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9028「"+ SeichiAssist.config.getTitle1(9028)
						+ SeichiAssist.config.getTitle2(9028) + SeichiAssist.config.getTitle3(9028) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：とある肉の日にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(29,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9028「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とある肉の日にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(29,itemstack);
			}
			if(playerdata.getTitleFlags().get(9029)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9029「"+ SeichiAssist.config.getTitle1(9029)
						+ SeichiAssist.config.getTitle2(9029) + SeichiAssist.config.getTitle3(9029) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：8月にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(30,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9029「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：8月にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(30,itemstack);
			}
			if(playerdata.getTitleFlags().get(9030)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9030「"+ SeichiAssist.config.getTitle1(9030)
						+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(9030) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：とあるバナナの日にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(31,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9030「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とあるバナナの日にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(31,itemstack);
			}
			if(playerdata.getTitleFlags().get(9031)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9031「"+ SeichiAssist.config.getTitle1(9031)
						+ SeichiAssist.config.getTitle2(9908) + SeichiAssist.config.getTitle3(9031) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：とあるJDの日にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(32,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9031「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とあるJDの日にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(32,itemstack);
			}
			if(playerdata.getTitleFlags().get(9032)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9032「"+ SeichiAssist.config.getTitle1(9032)
						+ SeichiAssist.config.getTitle3(9032) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：とある焼肉の日にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(33,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9032「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とある焼肉の日にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(33,itemstack);
			}
		}else if(playerdata.getTitlepage() == 2 ){
			if(playerdata.getTitleFlags().get(9033)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9033「"+ SeichiAssist.config.getTitle1(9033)
						+ SeichiAssist.config.getTitle2(9903) + SeichiAssist.config.getTitle3(9033) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：9月にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(0,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9033「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：9月にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(0,itemstack);
			}
			if(playerdata.getTitleFlags().get(9034)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9034「"+ SeichiAssist.config.getTitle1(9034)
						+ SeichiAssist.config.getTitle3(9034) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：とあるくじの日にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(1,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9034「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とあるくじの日にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(1,itemstack);
			}
			if(playerdata.getTitleFlags().get(9035)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9035「"+ SeichiAssist.config.getTitle1(9035)
						+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(9035) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：とあるマラソンの日にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(2,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9035「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とあるマラソンの日にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(2,itemstack);
			}
			if(playerdata.getTitleFlags().get(9036)){
				itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9036「"+ SeichiAssist.config.getTitle1(9036)
						+ SeichiAssist.config.getTitle3(9036) +"」" );
				lore = ImmutableListFactory.of(ChatColor.RESET + "" + ChatColor.RED + "条件：とあるふぐの日にプレイ");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(3,itemstack);
			}else{
				itemstack = new ItemStack(Material.BEDROCK,1);
				itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
				itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No9036「???」" );
				lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：とあるふぐの日にプレイ"
									,ChatColor.RESET + "" +  ChatColor.GREEN + "※クリックで実績に挑戦できます");
				itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				itemmeta.setLore(lore);
				itemstack.setItemMeta(itemmeta);
				inventory.setItem(3,itemstack);
			}
		}


		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "「ログイン」カテゴリメニューへ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,27,itemstack.clone());

		//次ページへのボタンを配置
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "次ページへ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowRight");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,35,itemstack.clone());


		return inventory;
	}

	//実績「極秘任務」(隠し実績群)
	public static Inventory getTitleSecretData(Player p){
		//プレイヤーを取得
		Player player = p.getPlayer();
		//UUID取得
		UUID uuid = player.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[木の棒メニューOPEN処理]でエラー発生");
			Bukkit.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			return null;
		}

		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "実績「極秘任務」");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore;


		//実績設定・解禁ボタン
		if(playerdata.getTitleFlags().get(8001)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No8001「"+ SeichiAssist.config.getTitle1(8001)
					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(8001) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：器を満たす奇跡の少女"
					,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
					,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
		}else{
			if(Calendar.getInstance().get(Calendar.SECOND)  == 0 &&
				Calendar.getInstance().get(Calendar.MINUTE)  == 0){
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No8001「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：器を満たす奇跡の少女"
					,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
					,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(0,itemstack);
			}
		}
		if(playerdata.getTitleFlags().get(8002)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No8002「"+ SeichiAssist.config.getTitle1(8002)
					+ SeichiAssist.config.getTitle2(9905) + SeichiAssist.config.getTitle3(8002) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：[[[[[[LuckyNumber]]]]]]"
					,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
					,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
		}else{
			if(playerdata.getTotalbreaknum() % 1000000 == 0 && !(playerdata.getTotalbreaknum() == 0)){
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No8002「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：[[[[[[LuckyNumber]]]]]]"
					,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は自動解禁式です。"
					,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(1,itemstack);
			}
		}
		if(playerdata.getTitleFlags().get(8003)){
			itemstack = new ItemStack(Material.DIAMOND_BLOCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_BLOCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No8003「"+ SeichiAssist.config.getTitle1(8003)
					+ SeichiAssist.config.getTitle3(8003) +"」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：定時分働いたら記録を確認！"
					,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は手動解禁式です。"
					,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
		}else{
			if(playerdata.getPlaytick() % 72000 >= 0 && playerdata.getPlaytick() % 72000 <= 1199){
			itemstack = new ItemStack(Material.BEDROCK,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BEDROCK);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "No8003「???」" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.RED + "条件：定時分働いたら記録を確認！"
					,ChatColor.RESET + "" +  ChatColor.RED + "※この実績は手動解禁式です。"
					,ChatColor.RESET + "" +  ChatColor.AQUA + "こちらは【隠し実績】となります");
			itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2,itemstack);
			}
		}


		// 1ページ目を開く
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "「特殊」カテゴリメニューへ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,27,itemstack.clone());

		return inventory;
	}
	public static Inventory getServerSwitchMenu(Player p) {
		//UUID取得
		UUID uuid = p.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if (sendWarningToLogger(p, playerdata)) return null;
		Inventory inventory = Bukkit.getServer().createInventory(null,2*9,ChatColor.DARK_RED + "" + ChatColor.BOLD + "サーバーを選択してください");
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

		inventory.setItem(0,itemstack);

		itemstack = new ItemStack(Material.DIAMOND_SPADE);
		itemmeta = itemstack.getItemMeta();
		itemmeta.setDisplayName(ChatColor.BOLD + "" + ChatColor.YELLOW + "エデンサーバー");
		List<String> lore2 = new ArrayList<>();
		lore2.add(ChatColor.GRAY + "旧第二サバイバルサーバー");
		itemmeta.setLore(lore2);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemstack.setItemMeta(itemmeta);

		inventory.setItem(1,itemstack);

		itemstack = new ItemStack(Material.DIAMOND_AXE);
		itemmeta = itemstack.getItemMeta();
		itemmeta.setDisplayName(ChatColor.BOLD + "" + ChatColor.YELLOW + "ヴァルハラサーバー");
		List<String> lore3 = new ArrayList<>();
		lore3.add(ChatColor.GRAY + "旧第三サバイバルサーバー");
		itemmeta.setLore(lore3);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemstack.setItemMeta(itemmeta);

		inventory.setItem(2,itemstack);

		itemstack = new ItemStack(Material.DIAMOND);
		itemmeta = itemstack.getItemMeta();
		itemmeta.setDisplayName(ChatColor.BOLD + "" + ChatColor.GREEN + "公共施設サーバー");
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemstack.setItemMeta(itemmeta);

		inventory.setItem(8,itemstack);

		return inventory;
	}

	private static boolean sendWarningToLogger(Player p, PlayerData playerdata) {
		if(playerdata == null){
			Util.sendPlayerDataNullMessage(p);
			Bukkit.getLogger().warning(p.getName() + " -> PlayerData not found.");
			Bukkit.getLogger().warning("MenuInventoryData.getMenuData");
			return true;
		}
		return false;
	}

	//投票メニュー
	public static Inventory getVotingMenuData(Player p){

		//UUID取得
		UUID uuid = p.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if (sendWarningToLogger(p, playerdata)) return null;
		Inventory inventory = Bukkit.getServer().createInventory(null,4*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "投票ptメニュー");
		ItemStack itemstack;
		ItemMeta itemmeta;
		SkullMeta skullmeta;
		List<String> lore;

		//投票pt受け取り
		itemstack = new ItemStack(Material.DIAMOND);
		itemmeta = itemstack.getItemMeta();
		itemmeta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "クリックで投票特典を受け取れます" );
		itemmeta.setLore(VoteGetButtonLore(playerdata));
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(0,itemstack);

		// ver0.3.2 投票ページ表示
		itemstack = new ItemStack(Material.BOOK_AND_QUILL,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BOOK_AND_QUILL);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "投票ページにアクセス");
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "投票すると様々な特典が！"
				, ChatColor.RESET + "" +  ChatColor.GREEN + "1日1回投票出来ます"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "クリックするとチャット欄に"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "URLが表示されますので"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "Tキーを押してから"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "そのURLをクリックしてください"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(9,itemstack);

		//棒メニューに戻る
		itemstack = new ItemStack(Material.SKULL_ITEM,1);
		skullmeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		itemstack.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームへ");
		lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで移動");
		skullmeta.setLore(lore);
		skullmeta.setOwner("MHF_ArrowLeft");
		itemstack.setItemMeta(skullmeta);
		AsyncInventorySetter.setItemAsync(inventory,27,itemstack.clone());

		//妖精召喚時間設定トグルボタン
		itemstack = new ItemStack(Material.WATCH);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.WATCH);
		itemmeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "マナ妖精 時間設定");
		lore = Arrays.asList(
				ChatColor.RESET + "" +  ChatColor.GREEN + "" +  ChatColor.BOLD + "" + VotingFairyTask.dispToggleVFTime(playerdata.getToggleVotingFairy()),
				"",
				ChatColor.RESET + "" +  ChatColor.GRAY + "コスト",
				ChatColor.RESET + "" +  ChatColor.RED + "" +  ChatColor.BOLD + "" + (playerdata.getToggleVotingFairy() * 2) + "投票pt",
				"",
				ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで切替"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(2,itemstack);

		//妖精契約設定トグル
		itemstack = new ItemStack(Material.PAPER);
		itemstack.setItemMeta(VFPromiseMeta(playerdata));
		inventory.setItem(11,itemstack);

		//妖精音トグル
		itemstack = new ItemStack(Material.JUKEBOX);
		itemstack.setItemMeta(VFSoundToggleMeta(playerdata.getToggleVFSound()));
		inventory.setItem(20,itemstack);


		//妖精召喚
		itemstack = new ItemStack(Material.GHAST_TEAR);
		itemmeta = itemstack.getItemMeta();
		itemmeta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "マナ妖精 召喚" );
		lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GRAY + "" + (playerdata.getToggleVotingFairy() * 2) + "投票ptを消費して"
				,ChatColor.RESET + "" +  ChatColor.GRAY + "マナ妖精を呼びます"
				,ChatColor.RESET + "" +  ChatColor.GRAY + "時間 : " + VotingFairyTask.dispToggleVFTime(playerdata.getToggleVotingFairy())
				,ChatColor.RESET + "" +  ChatColor.DARK_RED + "Lv.10以上で解放");
		itemmeta.setLore(lore);
		itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(4,itemstack);

		if(playerdata.getUsingVotingFairy()) {
			//妖精 時間確認
			itemstack = new ItemStack(Material.COMPASS);
			itemmeta = itemstack.getItemMeta();
			itemmeta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "マナ妖精に時間を聞く" );
			lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GRAY + "妖精さんはいそがしい。",ChatColor.GRAY + "帰っちゃう時間を教えてくれる" );
			itemmeta.setLore(lore);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(13,itemstack);

			int prank = playerdata.calcPlayerApple(p);

			itemstack = new ItemStack(Material.GOLDEN_APPLE);
			itemmeta = itemstack.getItemMeta();
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "㊙ がちゃりんご情報 ㊙" );
			List<String> lores = new ArrayList<>();
			lores.addAll(Arrays.asList(
					ChatColor.RESET + "" +  ChatColor.RED + "" + ChatColor.BOLD + "※ﾆﾝｹﾞﾝに見られないように気を付けること！"
					,ChatColor.RESET + "" + ChatColor.RED + "" + ChatColor.BOLD + "  毎日大妖精からデータを更新すること！"
					,""

					,ChatColor.RESET + "" +  ChatColor.GOLD + "" + ChatColor.BOLD + "昨日までにがちゃりんごを"
					,ChatColor.RESET + "" +  ChatColor.GOLD + "" + ChatColor.BOLD + "たくさんくれたﾆﾝｹﾞﾝたち"
					,ChatColor.RESET + "" +  ChatColor.DARK_GRAY + "召喚されたらラッキーだよ！"
					));
			RankData rankdata;
			for(int count = 0 ; count < 4 ; count++){
				if(count >= SeichiAssist.ranklist_p_apple.size()){
					break;
				}
				rankdata = SeichiAssist.ranklist_p_apple.get(count);
				if(rankdata.p_apple<1){ //数0
					break;
				}
				lores.add(ChatColor.GRAY + "たくさんくれたﾆﾝｹﾞﾝ第" + (count + 1) + "位！" );
				lores.add(ChatColor.GRAY + "なまえ：" + rankdata.name + " りんご：" + rankdata.p_apple + "個");
			}

			lores.add(ChatColor.AQUA + "ぜーんぶで" + SeichiAssist.allplayergiveapplelong + "個もらえた！");
			lores.add("");
			lores.add(ChatColor.GREEN + "↓呼び出したﾆﾝｹﾞﾝの情報↓");
			lores.add(ChatColor.GREEN + "今までに" + playerdata.getP_apple() + "個もらった");
			lores.add(ChatColor.GREEN + "ﾆﾝｹﾞﾝの中では" + prank + "番目にたくさんくれる！");

			itemmeta.setLore(lores);
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(6,itemstack);
		}


		return inventory;

	}
	public static ItemMeta VFSoundToggleMeta(boolean bln) {
		ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.JUKEBOX);
		itemmeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "マナ妖精の音トグル");
		if(bln) {
			itemmeta.setLore(Arrays.asList(
					ChatColor.RESET + "" +  ChatColor.GREEN + "現在音が鳴る設定になっています。"
					,ChatColor.RESET + "" + ChatColor.DARK_GRAY + "※この機能はデフォルトでONです。"
					,ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで切替"
					));
		}else {
			itemmeta.setLore(Arrays.asList(
					ChatColor.RESET + "" +  ChatColor.RED + "現在音が鳴らない設定になっています。"
					,ChatColor.RESET + "" + ChatColor.DARK_GRAY + "※この機能はデフォルトでONです。"
					,ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで切替"
					));
			itemmeta.addEnchant(Enchantment.DIG_SPEED, 100, false);
		}


		return itemmeta;
	}
	public static ItemMeta VFPromiseMeta(PlayerData playerdata){

		ItemMeta itemmeta = Bukkit.getItemFactory().getItemMeta(Material.PAPER);
		itemmeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "妖精とのお約束");

		if(playerdata.getToggleGiveApple() == 1) {
			itemmeta.setLore(Arrays.asList(
					ChatColor.RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ガンガンたべるぞ"
					,ChatColor.RESET + "" + ChatColor.GRAY + "とにかく妖精さんにりんごを開放します。"
					,ChatColor.RESET + "" + ChatColor.GRAY + "めっちゃ喜ばれます。"
					));
		}else if(playerdata.getToggleGiveApple() == 2) {
			itemmeta.setLore(Arrays.asList(
					ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "バッチリたべよう"
					,ChatColor.RESET + "" + ChatColor.GRAY + "食べ過ぎないように注意しつつ"
					,ChatColor.RESET + "" + ChatColor.GRAY + "妖精さんにりんごを開放します。"
					,ChatColor.RESET + "" + ChatColor.GRAY + "喜ばれます。"
					));
		}else if(playerdata.getToggleGiveApple() == 3) {
			itemmeta.setLore(Arrays.asList(
					ChatColor.GREEN + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "リンゴだいじに"
					,ChatColor.RESET + "" + ChatColor.GRAY + "少しだけ妖精さんにりんごを開放します。"
					,ChatColor.RESET + "" + ChatColor.GRAY + "伝えると大抵落ち込みます。"
					));
		}else if(playerdata.getToggleGiveApple() == 4) {
			itemmeta.setLore(Arrays.asList(
					ChatColor.BLUE + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "リンゴつかうな"
					,ChatColor.RESET + "" + ChatColor.GRAY + "絶対にりんごを開放しません。"
					,ChatColor.RESET + "" + ChatColor.GRAY + ""
					));
		}
		return itemmeta;
	}

	public static Inventory getHomeMenuData(Player p){
		//UUID取得
		UUID uuid = p.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if (sendWarningToLogger(p, playerdata)) return null;
		Inventory inventory = Bukkit.getServer().createInventory(null,3*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "ホームメニュー");
		ItemStack itemstack;
		ItemMeta itemmeta;
		List<String> lore;

		// ver0.3.2 homeコマンド
		itemstack = new ItemStack(Material.COMPASS,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COMPASS);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームポイントにワープ");
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "あらかじめ設定した"
				, ChatColor.RESET + "" + ChatColor.GRAY + "ホームポイントにワープします"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "うまく機能しない時は"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "再接続してみてください"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでワープ"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "command->[/home]"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(0,itemstack);

		itemstack = new ItemStack(Material.BED,1);
		itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BED);
		itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "ホームポイントを設定");
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "現在位置をホームポイント"
				, ChatColor.RESET + "" + ChatColor.GRAY + "として設定します"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "※確認メニューが開きます"
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで設定"
				, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "command->[/sethome]"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(18,itemstack);

		for(int x = 0 ; x < SeichiAssist.config.getSubHomeMax() ; x++){
			//サブホームに移動ボタン
			itemstack = new ItemStack(Material.COMPASS,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.COMPASS);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "サブホームポイント"+  (x+1) + "にワープ");
				lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "あらかじめ設定した"
						, ChatColor.RESET + "" + ChatColor.GRAY + "サブホームポイント" + (x+1) + "にワープします"
						, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "うまく機能しない時は"
						, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "再接続してみてください"
						, ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックでワープ"
						, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "command->[/subhome " + (x+1) + "]"
						);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(2+x,itemstack);

			itemstack = new ItemStack(Material.PAPER);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.PAPER);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "サブホームポイント" + (x+1) + "の情報");
			Location l = playerdata.getSubHomeLocation(x);
			final List<String> subHomeLore = !(l == null || l.getWorld() == null)
					?
					Arrays.asList(
							ChatColor.RESET + "" + ChatColor.GRAY + "サブホームポイント" + (x+1) + "は",
							ChatColor.RESET + "" + ChatColor.GRAY + playerdata.getSubHomeName(x),
							ChatColor.RESET + "" + ChatColor.GRAY + "と名付けられています",
							ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで名称変更",
							ChatColor.RESET + "" + ChatColor.DARK_GRAY + "command->[/subhome name " + (x+1) + "]",
							ChatColor.RESET + "" + ChatColor.GRAY + "" + Util.getWorldName(l.getWorld().getName()) + " x:" + (int)l.getX() + " y:" + (int)l.getY() + " z:" + (int)l.getZ())
					: Arrays.asList(ChatColor.GRAY + "サブホームポイント" + (x + 1), ChatColor.GRAY + "ポイント未設定");
			itemmeta.setLore(subHomeLore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(11+x,itemstack);

			//サブホーム設定ボタン
			itemstack = new ItemStack(Material.BED,1);
			itemmeta = Bukkit.getItemFactory().getItemMeta(Material.BED);
			itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "サブホームポイント" + (x+1) + "を設定");
			lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GRAY + "現在位置をサブホームポイント" + (x+1)
					, ChatColor.RESET + "" + ChatColor.GRAY + "として設定します"
					, ChatColor.RESET + "" + ChatColor.	DARK_GRAY + "※確認メニューが開きます"
					, ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.UNDERLINE + "クリックで設定"
					, ChatColor.RESET + "" + ChatColor.DARK_GRAY + "command->[/subhome set " + (x+1) + "]"
					);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(20+x,itemstack);
		}

		return inventory;
	}

	public static Inventory getCheckSetHomeMenuData(Player p){
		//UUID取得
		UUID uuid = p.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		int n = playerdata.getSelectHomeNum();
		//念のためエラー分岐
		if (sendWarningToLogger(p, playerdata)) return null;
		Inventory inventory = Bukkit.getServer().createInventory(null,3*9,ChatColor.RED + "" + ChatColor.BOLD + "ホームポイントを変更しますか?");
		ItemStack itemstack;
		ItemMeta itemmeta;
		List<String> lore;

		if (n >= 1){
			itemstack = new ItemStack(Material.PAPER);
			itemmeta = itemstack.getItemMeta();
			itemmeta.setDisplayName(ChatColor.GREEN + "設定するサブホームポイントの情報");
			lore = Arrays.asList(
					ChatColor.RESET + "" + ChatColor.GRAY + "No." + n,
					ChatColor.RESET + "" + ChatColor.GRAY + "名称：" + playerdata.getSubHomeName(n-1)
			);
			itemmeta.setLore(lore);
			itemstack.setItemMeta(itemmeta);
			inventory.setItem(4,itemstack);
		}

		itemstack = new ItemStack(Material.WOOL, 1, (byte)5);
		itemmeta = itemstack.getItemMeta();
		itemmeta.setDisplayName(ChatColor.GREEN + "変更する");
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(11,itemstack);

		itemstack = new ItemStack(Material.WOOL, 1, (byte)14);
		itemmeta = itemstack.getItemMeta();
		itemmeta.setDisplayName(ChatColor.RED + "変更しない");
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(15,itemstack);

		return inventory;
	}

	public static Inventory getGiganticBerserkEvolutionMenu(Player p) {
		//UUID取得
		UUID uuid = p.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if (sendWarningToLogger(p, playerdata)) return null;
		Inventory inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "スキルを進化させますか?");
		ItemStack itemstack;
		ItemMeta itemmeta;
		List<String> lore;

		switch(playerdata.getGBstage()){
		case 0:
			itemstack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte)12);
			break;
		case 1:
			itemstack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte)15);
			break;
		case 2:
			itemstack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte)4);
			break;
		case 3:
			itemstack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte)0);
			break;
		default:
			itemstack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte)12);
			break;
		}

		itemmeta = itemstack.getItemMeta();
		itemmeta.setDisplayName(" ");
		itemstack.setItemMeta(itemmeta);

		inventory.setItem(6,itemstack);
		inventory.setItem(7,itemstack);
		inventory.setItem(14,itemstack);
		inventory.setItem(15,itemstack);
		inventory.setItem(16,itemstack);
		inventory.setItem(21,itemstack);
		inventory.setItem(22,itemstack);
		inventory.setItem(23,itemstack);
		inventory.setItem(24,itemstack);
		inventory.setItem(32,itemstack);
		inventory.setItem(41,itemstack);

		itemstack = new ItemStack(Material.STICK, 1);
		itemmeta = itemstack.getItemMeta();
		itemmeta.setDisplayName(" ");
		itemstack.setItemMeta(itemmeta);

		inventory.setItem(30,itemstack);
		inventory.setItem(39,itemstack);
		inventory.setItem(40,itemstack);
		inventory.setItem(47,itemstack);


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
		inventory.setItem(31,itemstack);

		return inventory;
	}
	public static Inventory getGiganticBerserkEvolution2Menu(Player p) {
		//UUID取得
		UUID uuid = p.getUniqueId();
		//プレイヤーデータ
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		//念のためエラー分岐
		if (sendWarningToLogger(p, playerdata)) return null;
		Inventory inventory = Bukkit.getServer().createInventory(null,6*9,ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "スキルを進化させました");
		ItemStack itemstack;
		ItemMeta itemmeta;
		List<String> lore;

		switch(playerdata.getGBstage()){
		case 1:
			itemstack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte)15);
			break;
		case 2:
			itemstack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte)4);
			break;
		case 3:
			itemstack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte)0);
			break;
		case 4:
			itemstack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte)3);
			break;
		default:
			itemstack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte)12);
			break;
		}

		itemmeta = itemstack.getItemMeta();
		itemmeta.setDisplayName(" ");
		itemstack.setItemMeta(itemmeta);

		inventory.setItem(6,itemstack);
		inventory.setItem(7,itemstack);
		inventory.setItem(14,itemstack);
		inventory.setItem(15,itemstack);
		inventory.setItem(16,itemstack);
		inventory.setItem(21,itemstack);
		inventory.setItem(22,itemstack);
		inventory.setItem(23,itemstack);
		inventory.setItem(24,itemstack);
		inventory.setItem(32,itemstack);
		inventory.setItem(41,itemstack);

		itemstack = new ItemStack(Material.STICK, 1);
		itemmeta = itemstack.getItemMeta();
		itemmeta.setDisplayName(" ");
		itemstack.setItemMeta(itemmeta);

		inventory.setItem(30,itemstack);
		inventory.setItem(39,itemstack);
		inventory.setItem(40,itemstack);
		inventory.setItem(47,itemstack);


		itemstack = new ItemStack(Material.NETHER_STAR, 1);
		itemmeta = itemstack.getItemMeta();
		itemmeta.setDisplayName(ChatColor.WHITE + "スキルを進化させました！");
		lore = Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + "スキルの秘めたる力を解放することで、マナ回復量が増加し"
				, ChatColor.RESET + "" + ChatColor.DARK_RED + "スキルはより魂を求めるようになりました"
				);
		itemmeta.setLore(lore);
		itemstack.setItemMeta(itemmeta);
		inventory.setItem(31,itemstack);

		return inventory;
	}
}
