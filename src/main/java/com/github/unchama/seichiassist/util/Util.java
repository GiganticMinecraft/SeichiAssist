package com.github.unchama.seichiassist.util;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.minestack.MineStackObj;
import com.github.unchama.util.collection.ImmutableListFactory;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Difficulty;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public final class Util {
	// インスタンスを作成したところでメソッドが呼べるわけでもないので封印
	private Util() {

	}

	private static FireworkEffect.Type[] types = { FireworkEffect.Type.BALL,
		FireworkEffect.Type.BALL_LARGE, FireworkEffect.Type.BURST,
		FireworkEffect.Type.CREEPER, FireworkEffect.Type.STAR, };

	public static void sendPlayerDataNullMessage(Player player){
		player.sendMessage(ChatColor.RED + "初回ログイン時の読み込み中か、読み込みに失敗しています");
		player.sendMessage(ChatColor.RED + "再接続しても改善されない場合はお問い合わせフォームからお知らせ下さい");
	}

	//スキルの発動可否の処理(発動可能ならtrue、発動不可ならfalse)
	public static boolean isSkillEnable(Player player){
		//デバッグモード時は全ワールドでスキル使用を許可する(DEBUGWORLDNAME = worldの場合)
		String worldname = SeichiAssist.SEICHIWORLDNAME;
		if(SeichiAssist.DEBUG){
			worldname = SeichiAssist.DEBUGWORLDNAME;
		}

		//整地ワールドzeroではスキル発動不可
		if(player.getWorld().getName().equalsIgnoreCase("world_sw_zero")){
			return false;
		}

		//プレイヤーの場所が各種整地ワールド(world_SWで始まるワールド)または各種メインワールド(world)にいる場合
		return player.getWorld().getName().toLowerCase().startsWith(worldname)
				|| player.getWorld().getName().equalsIgnoreCase("world")
				|| player.getWorld().getName().equalsIgnoreCase("world_2")
				|| player.getWorld().getName().equalsIgnoreCase("world_nether")
				|| player.getWorld().getName().equalsIgnoreCase("world_the_end")
				|| player.getWorld().getName().equalsIgnoreCase("world_TT")
				|| player.getWorld().getName().equalsIgnoreCase("world_nether_TT")
				|| player.getWorld().getName().equalsIgnoreCase("world_the_end_TT");
		//それ以外のワールドの場合
	}

	//プレイヤーが整地ワールドにいるかどうかの判定処理(整地ワールド=true、それ以外=false)
	public static boolean isSeichiWorld(Player player){
		//デバッグモード時は全ワールドtrue(DEBUGWORLDNAME = worldの場合)
		String worldname = SeichiAssist.SEICHIWORLDNAME;
		if(SeichiAssist.DEBUG){
			worldname = SeichiAssist.DEBUGWORLDNAME;
		}
		//整地ワールドではtrue
		return player.getWorld().getName().toLowerCase().startsWith(worldname);

		//それ以外のワールドの場合
	}

	//ガチャ券アイテムスタック型の取得
	public static ItemStack getskull(String name){
		ItemStack skull;
		SkullMeta skullmeta;
		skull = new ItemStack(Material.SKULL_ITEM, 1);
		skullmeta = ItemMetaFactory.SKULL.getValue();
		skull.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "ガチャ券");
		List<String> lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.GREEN + "右クリックで使えます"
				, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "所有者:" + name);
		skullmeta.setLore(lore);
		skullmeta.setOwner("unchama");
		skull.setItemMeta(skullmeta);
		return skull;
	}

	//プレイヤーのインベントリがフルかどうか確認
	public static boolean isPlayerInventoryFull(Player player){
		return (player.getInventory().firstEmpty() == -1);
	}
	//指定されたアイテムを指定されたプレイヤーにドロップする
	public static void dropItem(Player player,ItemStack itemstack){
		player.getWorld().dropItemNaturally(player.getLocation(), itemstack);
	}
	//指定されたアイテムを指定されたプレイヤーインベントリに追加する
	public static void addItem(Player player,ItemStack itemstack){
		player.getInventory().addItem(itemstack);
	}

	/**
	 * プレイヤーに安全にアイテムを付与します。
	 *
	 * @param player 付与する対象プレイヤー
	 * @param itemStack 付与するアイテム
	 */
	public static void addItemToPlayerSafely(Player player, ItemStack itemStack) {
		if (isPlayerInventoryFull(player)) {
			dropItem(player, itemStack);
		} else {
			addItem(player, itemStack);
		}
	}

	public static void sendAdminMessage(String str){
		for ( Player player : Bukkit.getOnlinePlayers() ) {
			if(player.hasPermission("SeichiAssist.admin")){
				player.sendMessage(str);
			}
		}
	}


	public static void sendEveryMessage(String str){
		for ( Player player : Bukkit.getOnlinePlayers() ) {
			player.sendMessage(str);
		}
	}

	public static void sendEveryMessageWithoutIgnore(String str){
		for ( Player player : Bukkit.getOnlinePlayers() ) {
			if (SeichiAssist.playermap.get(player.getUniqueId()).getEverymessageflag()) {
				player.sendMessage(str);
			}
		}
	}

	/**
	 * json形式のチャットを送信する際に使用
	 */
	public static void sendEveryMessage(BaseComponent base){
		for ( Player player : Bukkit.getOnlinePlayers() ) {
			player.spigot().sendMessage(base);
		}
	}

	public static void sendEveryMessageWithoutIgnore(BaseComponent base){
		for ( Player player : Bukkit.getOnlinePlayers() ) {
			if (SeichiAssist.playermap.get(player.getUniqueId()).getEverymessageflag()) {
				player.spigot().sendMessage(base);
			}
		}
	}

	public static String getEnchantName(String vaname, int enchlevel){
		switch(vaname){
			case "PROTECTION_ENVIRONMENTAL":
				return "ダメージ軽減" + " " + getEnchantLevelRome(enchlevel);

			case "PROTECTION_FIRE":
				return "火炎耐性" + " " + getEnchantLevelRome(enchlevel);

			case "PROTECTION_FALL":
				return "落下耐性" + " " + getEnchantLevelRome(enchlevel);

			case "PROTECTION_EXPLOSIONS":
				return "爆発耐性" + " " + getEnchantLevelRome(enchlevel);

			case "PROTECTION_PROJECTILE":
				return "飛び道具耐性" + " " + getEnchantLevelRome(enchlevel);

			case "OXYGEN":
				return "水中呼吸" + " " + getEnchantLevelRome(enchlevel);

			case "WATER_WORKER":
				return "水中採掘";

			case "THORNS":
				return "棘の鎧" + " " + getEnchantLevelRome(enchlevel);

			case "DEPTH_STRIDER":
				return "水中歩行" + " " + getEnchantLevelRome(enchlevel);

			case "FROST_WALKER":
				return "氷渡り" + " " + getEnchantLevelRome(enchlevel);

			case "DAMAGE_ALL":
				return "ダメージ増加" + " " + getEnchantLevelRome(enchlevel);

			case "DAMAGE_UNDEAD":
				return "アンデッド特効" + " " + getEnchantLevelRome(enchlevel);

			case "DAMAGE_ARTHROPODS":
				return "虫特効" + " " + getEnchantLevelRome(enchlevel);

			case "KNOCKBACK":
				return "ノックバック" + " " + getEnchantLevelRome(enchlevel);

			case "FIRE_ASPECT":
				return "火属性" + " " + getEnchantLevelRome(enchlevel);

			case "LOOT_BONUS_MOBS":
				return "ドロップ増加" + " " + getEnchantLevelRome(enchlevel);

			case "DIG_SPEED":
				return "効率強化" + " " + getEnchantLevelRome(enchlevel);

			case "SILK_TOUCH":
				return "シルクタッチ";

			case "DURABILITY":
				return "耐久力" + " " + getEnchantLevelRome(enchlevel);

			case "LOOT_BONUS_BLOCKS":
				return "幸運" + " " + getEnchantLevelRome(enchlevel);

			case "ARROW_DAMAGE":
				return "射撃ダメージ増加" + " " + getEnchantLevelRome(enchlevel);

			case "ARROW_KNOCKBACK":
				return "パンチ" + " " + getEnchantLevelRome(enchlevel);

			case "ARROW_FIRE":
				return "フレイム";

			case "ARROW_INFINITE":
				return "無限";

			case "LUCK":
				return "宝釣り" + " " + getEnchantLevelRome(enchlevel);

			case "LURE":
				return "入れ食い" + " " + getEnchantLevelRome(enchlevel);

			case "MENDING":
				return "修繕";

			default:
				return vaname;
		}
	}

	private static String getEnchantLevelRome(int enchantlevel){
		switch(enchantlevel){
			case 1:
				return "Ⅰ";

			case 2:
				return "Ⅱ";

			case 3:
				return "Ⅲ";

			case 4:
				return "Ⅳ";

			case 5:
				return "Ⅴ";

			case 6:
				return "Ⅵ";

			case 7:
				return "Ⅶ";

			case 8:
				return "Ⅷ";

			case 9:
				return "Ⅸ";

			case 10:
				return "Ⅹ" ;

			default:
				return String.valueOf(enchantlevel);
		}

	}

	public static String getDescFormat(List<String> list)
	{
		return " " + String.join("\n", list) + "\n";
		//return list.toString().replaceAll(",", "\n").replaceAll("\\[", " ").replaceAll("]", "\n");
	}

	public static void sendEverySound(Sound kind, float a, float b){
		for ( Player player : Bukkit.getOnlinePlayers() ) {
			player.playSound(player.getLocation(), kind, a, b);
		}
	}
	public static void sendEverySoundWithoutIgnore(Sound kind, float a, float b){
		for ( Player player : Bukkit.getOnlinePlayers() ) {
			if (SeichiAssist.playermap.get(player.getUniqueId()).getEverysoundflag()) {
				player.playSound(player.getLocation(), kind, a, b);
			}
		}
	}

	//プレイヤーネームを格納（toLowerCaseで全て小文字にする。)
	public static String getName(Player p) {
		return p.getName().toLowerCase();
	}
	public static String getName(String name) {
		//小文字にしてるだけだよ
		return name.toLowerCase();
	}

	//指定された場所に花火を打ち上げる関数
	public static void launchFireWorks(Location loc) {
		// 花火を作る
		Firework firework = loc.getWorld().spawn(loc, Firework.class);

		// 花火の設定情報オブジェクトを取り出す
		FireworkMeta meta = firework.getFireworkMeta();
		Builder effect = FireworkEffect.builder();
		Random rand = new Random();

		// 形状をランダムに決める
		effect.with(types[rand.nextInt(types.length)]);

		// 基本の色を単色～5色以内でランダムに決める
		effect.withColor(getRandomColors(1 + rand.nextInt(5)));

		// 余韻の色を単色～3色以内でランダムに決める
		effect.withFade(getRandomColors(1 + rand.nextInt(3)));

		// 爆発後に点滅するかをランダムに決める
		effect.flicker(rand.nextBoolean());

		// 爆発後に尾を引くかをランダムに決める
		effect.trail(rand.nextBoolean());

		// 打ち上げ高さを1以上4以内でランダムに決める
		meta.setPower(1 + rand.nextInt(4));

		// 花火の設定情報を花火に設定
		meta.addEffect(effect.build());
		firework.setFireworkMeta(meta);

	}
	//カラーをランダムで決める
	public static Color[] getRandomColors(int length) {
		// 配列を作る
		Color[] colors;
		Random rand = new Random();
		// 配列の要素を順に処理していく
		// 24ビットカラーの範囲でランダムな色を決める
		colors = IntStream.range(0, length).mapToObj(n -> Color.fromBGR(rand.nextInt(1 << 24))).toArray(Color[]::new);

		// 配列を返す
		return colors;
	}

	//ガチャアイテムを含んでいるか調べる
	public static boolean containsGachaTicket(Player player) {
		org.bukkit.inventory.ItemStack[] inventory = player.getInventory().getStorageContents();
		Material material;
		SkullMeta skullmeta;
		for (final ItemStack itemStack : inventory) {
			material = itemStack.getType();
			if (material == Material.SKULL_ITEM) {
				skullmeta = (SkullMeta) itemStack.getItemMeta();
				if (skullmeta.hasOwner()) {
					if (skullmeta.getOwner().equals("unchama")) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * loreを捜査して、要素の中に{@code find}が含まれているかを調べる。
	 * @param lore 探される対象
	 * @param find 探す文字列
	 * @return 見つかった場合はその添字、見つからなかった場合は-1
	 */
	public static int loreIndexOf(List<String> lore, String find) {
		return IntStream.range(0, lore.size())
				.filter(i -> lore.get(i).contains(find))
				.findFirst()
				.orElse(-1);
	}
	public static boolean isGachaTicket(ItemStack itemstack) {
		if(itemstack.getType() != Material.SKULL_ITEM){
			return false;
		}
		SkullMeta skullmeta = (SkullMeta) itemstack.getItemMeta();

		//ownerがいない場合処理終了
		if(!skullmeta.hasOwner()){
			return false;
		}
		// オーナーがunchamaか？
		return skullmeta.getOwner().equals("unchama");
	}
	public static boolean removeItemfromPlayerInventory(PlayerInventory inventory,
			ItemStack itemstack, int count) {
		//持っているアイテムを減らす処理
		if (itemstack.getAmount() == count) {
			// アイテムをcount個使うので、プレイヤーの手を素手にする
			inventory.setItemInMainHand(new ItemStack(Material.AIR));
		}
		else if(itemstack.getAmount() > count){
			// プレイヤーが持っているアイテムをcount個減らす
			itemstack.setAmount(itemstack.getAmount()-count);
		}
		else return itemstack.getAmount() >= count;
		return true;
	}
	public static ItemStack getForBugskull(String name) {
		ItemStack skull;
		SkullMeta skullmeta;
		skull = new ItemStack(Material.SKULL_ITEM, 1);
		skullmeta = ItemMetaFactory.SKULL.getValue();
		skull.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "ガチャ券");
		List<String> lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.GREEN + "右クリックで使えます"
				, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "所有者：" + name
				, ChatColor.RESET + "" +  ChatColor.DARK_RED + "運営から不具合のお詫びです");
		skullmeta.setLore(lore);
		skullmeta.setOwner("unchama");
		skull.setItemMeta(skullmeta);
		return skull;
	}
	public static ItemStack getVoteskull(String name) {
		ItemStack skull;
		SkullMeta skullmeta;
		skull = new ItemStack(Material.SKULL_ITEM, 1);
		skullmeta = ItemMetaFactory.SKULL.getValue();
		skull.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "ガチャ券");
		List<String> lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.GREEN + "右クリックで使えます"
				, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "所有者：" + name
				,ChatColor.RESET + "" +  ChatColor.LIGHT_PURPLE + "投票ありがとナス♡");
		skullmeta.setLore(lore);
		skullmeta.setOwner("unchama");
		skull.setItemMeta(skullmeta);
		return skull;
	}
	public static ItemStack getExchangeskull(String name) {
		ItemStack skull;
		SkullMeta skullmeta;
		skull = new ItemStack(Material.SKULL_ITEM, 1);
		skullmeta = ItemMetaFactory.SKULL.getValue();
		skull.setDurability((short) 3);
		skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "ガチャ券");
		List<String> lore = ImmutableListFactory.of(ChatColor.RESET + "" +  ChatColor.GREEN + "右クリックで使えます"
				, ChatColor.RESET + "" +  ChatColor.DARK_GREEN + "所有者：" + name
				,ChatColor.RESET + "" +  ChatColor.GRAY + "ガチャ景品と交換しました。");
		skullmeta.setLore(lore);
		skullmeta.setOwner("unchama");
		skull.setItemMeta(skullmeta);
		return skull;
	}

	public static boolean ItemStackContainsOwnerName(ItemStack itemstack, String name) {

		ItemMeta meta = itemstack.getItemMeta();
		List<String> lore;
		if(meta.hasLore()){
			lore = meta.getLore();
		}else{
			lore = new ArrayList<>();
		}

		for (String s : lore) {
			if (s.contains("所有者：")) { //"所有者:がある"
				int idx = s.lastIndexOf("所有者：");
				idx += 4; //「所有者：」の右端(名前の左端)までidxを移動
				String temp = s.substring(idx);
				if (temp.equals(name)) {
					return true;
				}
			}
		}
		return false;
	}

	public static ItemStack ItemStackResetName(ItemStack itemstack) {

		ItemStack itemstack_temp = new ItemStack(itemstack);
		ItemMeta meta = itemstack_temp.getItemMeta();
		List<String> lore;
		if(meta != null){
			if(meta.hasLore()){
				lore = meta.getLore();

				int i;
				for(i=0; i<lore.size(); i++){
					if(lore.get(i).contains("所有者：")){ //"所有者:がある"
						break;
					}
				}
				if(i!=lore.size()){ //所有者表記が無かった場合を除く
					lore.remove(i);
					meta.setLore(lore);
				}
			}
		}
		return itemstack_temp;
	}

	public static int getMineStackTypeindex(int idx){
		int temp;
		int type = SeichiAssist.minestacklist.get(idx).getStacktype();
		temp = (int) IntStream.range(0, idx).filter(i -> SeichiAssist.minestacklist.get(i).getStacktype() == type).count();
		return temp;
	}

	/**
	 * GUIメニューアイコン作成用
	 * @author karayuu
	 *
	 * @param material メニューアイコンMaterial, not {@code null}
	 * @param amount メニューアイコンのアイテム個数
	 * @param displayName メニューアイコンのDisplayName, not {@code null}
	 * @param lore メニューアイコンのLore, not {@code null}
	 * @param isHideFlags 攻撃値・ダメージ値を隠すかどうか(true: 隠す / false: 隠さない)
	 * @throws IllegalArgumentException Material,DisplayName, Loreのいずれかが {@code null} の時
	 * @return ItemStack型のメニューアイコン
	 */
	public static ItemStack getMenuIcon(Material material, int amount,
										String displayName, List<String> lore, boolean isHideFlags) {
		if (material == null || displayName == null || lore == null) {
			throw new IllegalArgumentException("Material,DisplayName,LoreにNullは指定できません。");
		}
		ItemStack menuicon = new ItemStack(material, amount);
		ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(material);
		itemMeta.setDisplayName(displayName);
		itemMeta.setLore(lore);
		if (isHideFlags) {
			itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		}
		menuicon.setItemMeta(itemMeta);

		return menuicon;
	}

	/**
	 * GUIメニューアイコン作成用
	 * @author karayuu
	 *
	 * @param material メニューアイコンMaterial, not {@code null}
	 * @param amount メニューアイコンのアイテム個数
	 * @param durabity メニューアイコンのダメージ値
	 * @param displayName メニューアイコンのDisplayName, not {@code null}
	 * @param lore メニューアイコンのLore, not {@code null}
	 * @param isHideFlags 攻撃値・ダメージ値を隠すかどうか(true: 隠す / false: 隠さない)
	 * @throws IllegalArgumentException Material,DisplayName, Loreのいずれかが {@code null} の時
	 * @return ItemStack型のメニューアイコン
	 */
	public static ItemStack getMenuIcon(Material material, int amount, int durabity,
										String displayName, List<String> lore, boolean isHideFlags) {
		if (material == null || displayName == null || lore == null) {
			throw new IllegalArgumentException("Material,DisplayName,LoreにNullは指定できません。");
		}
		ItemStack menuicon = new ItemStack(material, amount, (short) durabity);
		ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(material);
		itemMeta.setDisplayName(displayName);
		itemMeta.setLore(lore);
		if (isHideFlags) {
			itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		}
		menuicon.setItemMeta(itemMeta);

		return menuicon;
	}

	/**
	 * PlayerDataでチャンク数をゲット・セットするためのenum
	 */
	public enum DirectionType {
		AHEAD,
		BEHIND,
		RIGHT,
		LEFT,
		;
	}

	/**
	 * PlayerDataなどで使用する方角関係のenum
	 */
	public enum Direction {
		NORTH,
		SOUTH,
		EAST,
		WEST,
		;
	}

	public static Direction getPlayerDirection(Player player) {
		double rotation = (player.getLocation().getYaw() + 180) % 360;

		if (rotation < 0) {
			rotation += 360;
		}

		//0,360:south 90:west 180:north 270:east
		if (0.0 <= rotation && rotation < 45.0) {
			//前が北(North)
			return Direction.NORTH;
		} else if (45.0 <= rotation && rotation < 135.0) {
			//前が東(East)
			return Direction.EAST;
		} else if (135.0 <= rotation && rotation < 225.0) {
			//前が南(South)
			return Direction.SOUTH;
		} else if (225.0 <= rotation && rotation < 315.0) {
			//前が西(West)
			return Direction.WEST;
		} else if (315.0 <= rotation && rotation < 360.0) {
			//前が北(North)
			return Direction.NORTH;
		}
		//ここに到達はありえない。
		return null;
	}

	public static String showTime(Calendar cal) {
		  Date date = cal.getTime();
		  SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		  return format.format(date);
	}

	public static String showHour(Calendar cal) {
		Date date = cal.getTime();
		  SimpleDateFormat format = new SimpleDateFormat("HH:mm");
		  return format.format(date);
	}

	public static String getTimeZone(Calendar cal) {
		Date date = cal.getTime();
		SimpleDateFormat format = new SimpleDateFormat("HH");
		int n = TypeConverter.toInt(format.format(date));
		return	4<=n && n<10 ? "morning"
				:10<=n && n<18 ? "day"
						:"night";
	}

	public static boolean isVotingFairyPeriod(Calendar start, Calendar end) {
		Calendar cur = Calendar.getInstance();
		return cur.after(start) && cur.before(end);
	}

	public static String getWorldName(String s){
		String worldname;
		switch(s){
		case "world_spawn":
			worldname = "スポーンワールド";
			break;
		case "world":
			worldname = "メインワールド";
			break;
		case "world_SW":
			worldname = "第一整地ワールド";
			break;
		case "world_SW_2":
			worldname = "第二整地ワールド";
			break;
		case "world_SW_3":
			worldname = "第三整地ワールド";
			break;
		case "world_SW_nether":
			worldname = "整地ネザー";
			break;
		case "world_SW_the_end":
			worldname = "整地エンド";
			break;
		default:
			worldname = s;
		}
		return worldname;
	}

	public static void setDifficulty(List<String> worldNameList, Difficulty difficulty) {
		for (String name : worldNameList) {
			World world = Bukkit.getWorld(name);
			if (world == null) {
				Bukkit.getLogger().warning(name + "という名前のワールドは存在しません。");
				continue;
			}
			world.setDifficulty(difficulty);
		}
	}

	/**
	 * 指定した名前のマインスタックオブジェクトを返す
	 */
	// TODO これはここにあるべきではない
	@Deprecated public static @Nullable MineStackObj findMineStackObjectByName(String name) {
		return SeichiAssist.minestacklist.stream()
				.filter(obj -> name.equals(obj.getMineStackObjName()))
				.findFirst().orElse(null);
	}

	public static boolean isEnemy(EntityType type) {
		switch(type){
		//通常世界MOB
		case CAVE_SPIDER:
			return true;
		case CREEPER:
			return true;
		case GUARDIAN:
			return true;
		case SILVERFISH:
			return true;
		case SKELETON:
			return true;
		case SLIME:
			return true;
		case SPIDER:
			return true;
		case WITCH:
			return true;
		case ZOMBIE:
			return true;
		//ネザーMOB
		case BLAZE:
			return true;
		case GHAST:
			return true;
		case MAGMA_CUBE:
			return true;
		case PIG_ZOMBIE:
			return true;
		//エンドMOB
		case ENDERMAN:
			return true;
		case ENDERMITE:
			return true;
		case SHULKER:
			return true;
		//敵MOB以外(エンドラ,ウィザーは除外)
		default:
			return false;
		}
	}

	public static boolean isMineHeadItem(ItemStack itemstack) {
		return itemstack.getType() == Material.CARROT_STICK &&
				loreIndexOf(itemstack.getItemMeta().getLore(), "頭を狩り取る形をしている...") >= 0;
	}

	public static ItemStack getSkullDataFromBlock(Block block) {
		//ブロックがskullじゃない場合石でも返しとく
		if(block.getType() != Material.SKULL) {return new ItemStack(Material.STONE);}

		Skull skull = (Skull) block.getState();
		ItemStack itemStack = new ItemStack(Material.SKULL_ITEM);

		//SkullTypeがプレイヤー以外の場合，SkullTypeだけ設定して終わり
		if(skull.getSkullType() != SkullType.PLAYER) {
			switch (skull.getSkullType()) {
			case CREEPER:
				itemStack.setDurability((short) SkullType.CREEPER.ordinal());
				break;
			case DRAGON:
				itemStack.setDurability((short) SkullType.DRAGON.ordinal());
				break;
			case SKELETON:
				itemStack.setDurability((short) SkullType.SKELETON.ordinal());
				break;
			case WITHER:
				itemStack.setDurability((short) SkullType.WITHER.ordinal());
				break;
			case ZOMBIE:
				itemStack.setDurability((short) SkullType.ZOMBIE.ordinal());
				break;
			default:
				break;
			}
			return itemStack;
		}

		//プレイヤーの頭の場合，ドロップアイテムからItemStackを取得．データ値をPLAYERにして返す
		Collection<ItemStack> drops = block.getDrops();
		for (final ItemStack drop : drops) {
			itemStack = drop;
		}

		itemStack.setDurability((short) SkullType.PLAYER.ordinal());
		return itemStack;
	}

	public static boolean isLimitedTitanItem(ItemStack itemstack) {
		return itemstack.getType() == Material.DIAMOND_AXE &&
				loreIndexOf(itemstack.getItemMeta().getLore(), "特別なタイタンをあなたに♡") >= 0;
	}

}
