package com.github.unchama.seichiassist.util;

import com.github.unchama.seichiassist.util.external.CoreProtectWrapper;
import com.github.unchama.seichiassist.util.external.ExternalPlugins;
import com.github.unchama.seichiassist.ActiveSkill;
import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.Coordinate;
import com.github.unchama.seichiassist.data.MineStackGachaData;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.minestack.MineStackObj;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;


public final class BreakUtil {
	private BreakUtil() {

	}

	//他のプラグインの影響があってもブロックを破壊できるのか
	@SuppressWarnings("deprecation")
	public static boolean canBreak(Player player ,Block breakblock) {
		if(!player.isOnline() || breakblock == null){
			return false;
		}
		HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
		UUID uuid = player.getUniqueId();
		PlayerData playerdata = playermap.get(uuid);

		//壊されるブロックのMaterialを取得
		final Material material = breakblock.getType();

		//壊されるブロックがワールドガード範囲だった場合処理を終了
		if(!ExternalPlugins.getWorldGuard().canBuild(player, breakblock.getLocation())){
			if(playerdata.getDispworldguardlogflag()){
				player.sendMessage(ChatColor.RED + "ワールドガードで保護されています。");
			}
			return false;
		}

		if(!equalsIgnoreNameCaseWorld(player.getWorld().getName())){
			final CoreProtectWrapper wrapper = ExternalPlugins.getCoreProtectWrapper();
			if (wrapper == null) {
				Bukkit.getLogger().warning("CoreProtectにアクセスできませんでした。");
			} else {
				final boolean failure = !wrapper.queueBlockRemoval(player, breakblock);
				//もし失敗したらプレイヤーに報告し処理を終了
				if(failure) {
					player.sendMessage(ChatColor.RED + "coreprotectに保存できませんでした。管理者に報告してください。");
					return false;
				}
			}
		}

		if(material == Material.CHEST || material == Material.TRAPPED_CHEST){
			if(!playerdata.getChestflag()){
				player.sendMessage(ChatColor.RED + "スキルでのチェスト破壊は無効化されています");
				return false;
			}else if(!Util.isSeichiWorld(player)){
				player.sendMessage(ChatColor.RED + "スキルでのチェスト破壊は整地ワールドでのみ有効です");
				return false;
			}
		}

		return true;
	}
	private static boolean equalsIgnoreNameCaseWorld(String name) {
		List<String> ignoreworldlist = SeichiAssist.ignoreWorldlist;
		return ignoreworldlist.stream().anyMatch(s -> name.equalsIgnoreCase(s.toLowerCase()));
	}
	//ブロックを破壊する処理、ドロップも含む、統計増加も含む
	public static void breakBlock(Player player, Block breakblock, Location centerofblock, ItemStack tool, boolean stepflag) {

		Material material = breakblock.getType();
		if(!SeichiAssist.materiallist.contains(material)){
			return;
		}

		ItemStack itemstack = dropItemOnTool(breakblock,tool);

		//農地か草の道の場合土をドロップ
		if(material == Material.GRASS_PATH || material == Material.SOIL) {
			// DIRT, amount = 1
			itemstack = new ItemStack(Material.DIRT);
		}

		if(material == Material.MOB_SPAWNER) {
			itemstack = null;
		}

		if(material == Material.GLOWING_REDSTONE_ORE) {
			material = Material.REDSTONE_ORE;
		}

		if(material == Material.AIR) {
			return;
		}

		if(itemstack != null) {
			//アイテムをドロップさせる
			if(!addItemtoMineStack(player,itemstack)){
				breakblock.getWorld().dropItemNaturally(centerofblock,itemstack);
			}
		}

		//ブロックを空気に変える
		breakblock.setType(Material.AIR);

		if(stepflag) {
			//あたかもプレイヤーが壊したかのようなエフェクトを表示させる、壊した時の音を再生させる
			breakblock.getWorld().playEffect(breakblock.getLocation(), Effect.STEP_SOUND, material);
		}

		//プレイヤーの統計を１増やす
		if(material != Material.GRASS_PATH && material != Material.SOIL && material != Material.MOB_SPAWNER){
			player.incrementStatistic(Statistic.MINE_BLOCK, material);
		}

	}

	public static boolean addItemtoMineStack(Player player, ItemStack itemstack) {
		SeichiAssist plugin = SeichiAssist.instance;
		HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
		Config config = SeichiAssist.config;
		//もしサバイバルでなければ処理を終了
		if(player.getGameMode() != GameMode.SURVIVAL){
			return false;
		}
		if(SeichiAssist.DEBUG){
			player.sendMessage(ChatColor.RED + "minestackAdd:" + itemstack.toString());
			player.sendMessage(ChatColor.RED + "mineDurability:" + itemstack.getDurability());
		}
		UUID uuid = player.getUniqueId();
		PlayerData playerdata = playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			player.sendMessage(ChatColor.RED + "playerdataがありません。管理者に報告してください");
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "SeichiAssist[PickupItem処理]でエラー発生");
			plugin.getLogger().warning(player.getName() + "のplayerdataがありません。開発者に報告してください");
			return false;
		}
		//レベルが足りない場合処理終了
		if(playerdata.getLevel() < config.getMineStacklevel(1)){
			return false;
		}
		//minestackflagがfalseの時は処理を終了
		if(!playerdata.getMinestackflag()){
			return false;
		}

		int amount = itemstack.getAmount();
		Material material = itemstack.getType();


		//線路・キノコなどの、拾った時と壊した時とでサブIDが違う場合の処理
		//拾った時のサブIDに合わせる
		if(itemstack.getType() == Material.RAILS
			|| itemstack.getType() == Material.HUGE_MUSHROOM_1
			|| itemstack.getType() == Material.HUGE_MUSHROOM_2){

			itemstack.setDurability((short)0);
		}

		int i;
		for(i=0; i<SeichiAssist.minestacklist.size(); i++){
			final MineStackObj mineStackObj = SeichiAssist.minestacklist.get(i);
			if(material == mineStackObj.getMaterial() &&
				itemstack.getDurability() == mineStackObj.getDurability()){
				//この時点でIDとサブIDが一致している
				if(!mineStackObj.getNameloreflag() && (!itemstack.getItemMeta().hasLore() && !itemstack.getItemMeta().hasDisplayName() ) ){//名前と説明文が無いアイテム
					if(playerdata.getLevel() < config.getMineStacklevel(mineStackObj.getLevel())){
						//レベルを満たしていない
						return false;
					} else {
						playerdata.getMinestack().addStackedAmountOf(mineStackObj, amount);
						break;
					}
				} else if(mineStackObj.getNameloreflag() && itemstack.getItemMeta().hasDisplayName() && itemstack.getItemMeta().hasLore()){
					//名前・説明文付き
					ItemMeta meta = itemstack.getItemMeta();

					//この時点で名前と説明文がある
					if(mineStackObj.getGachatype()==-1){ //ガチャ以外のアイテム(がちゃりんご)
						if( !(meta.getDisplayName().equals(StaticGachaPrizeFactory.getGachaRingoName()))
							|| !(meta.getLore().equals(StaticGachaPrizeFactory.getGachaRingoLore())) ){
							return false;
						}
						if(playerdata.getLevel() < config.getMineStacklevel(mineStackObj.getLevel())){
							//レベルを満たしていない
							return false;
						} else {
							playerdata.getMinestack().addStackedAmountOf(mineStackObj, amount);
							break;
						}
					} else {
						//ガチャ品
						MineStackGachaData g = SeichiAssist.msgachadatalist.get(mineStackObj.getGachatype());
						String name = playerdata.getName(); //プレイヤーのネームを見る
						if(g.getProbability() <0.1){ //カタログギフト券を除く(名前があるアイテム)
							if(!Util.ItemStackContainsOwnerName(itemstack, name)){
								//所有者の名前が無ければreturn
								return false;
							}
						}

						if (g.itemStackEquals(itemstack)) { //中身が同じ場合のみここに入る
							if(playerdata.getLevel() < config.getMineStacklevel(mineStackObj.getLevel())){
								//レベルを満たしていない
								return false;
							} else {
								playerdata.getMinestack().addStackedAmountOf(mineStackObj, amount);
								//delete_flag=true;
								break;
							}
						}
					}
				}
			}
		}
		return i != SeichiAssist.minestacklist.size();

	}
	@SuppressWarnings("deprecation")
	public static ItemStack dropItemOnTool(Block breakblock, ItemStack tool) {
		ItemStack dropitem = null;
		Material dropmaterial;
		Material breakmaterial = breakblock.getType();
		int fortunelevel = tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
		double rand = Math.random();
		int bonus = (int) (rand * ((fortunelevel + 2)) - 1);
		if (bonus <= 1) {
			bonus = 1;
		}
		byte b = breakblock.getData();
		byte b_tree = b;
		b_tree &= 0x03;
		b &= 0x0F;

		int silktouch = tool.getEnchantmentLevel(Enchantment.SILK_TOUCH);
		if(silktouch > 0){
			//シルクタッチの処理
			switch(breakmaterial){
			case GLOWING_REDSTONE_ORE:
				dropmaterial = Material.REDSTONE_ORE;
				dropitem = new ItemStack(dropmaterial);
				break;
			case LOG:
			case LOG_2:
			case LEAVES:
			case LEAVES_2:
				dropitem = new ItemStack(breakmaterial,1,b_tree);
				break;
			case MONSTER_EGGS:
				dropmaterial = Material.STONE;
				dropitem = new ItemStack(dropmaterial);
				break;
			default:
				dropitem = new ItemStack(breakmaterial,1,b);
				break;
			}

		}else if(fortunelevel > 0 && SeichiAssist.luckmateriallist.contains(breakmaterial)){
			//幸運の処理
			switch(breakmaterial){
				case COAL_ORE:
					dropmaterial = Material.COAL;
					dropitem = new ItemStack(dropmaterial,bonus);
					break;
				case DIAMOND_ORE:
					dropmaterial = Material.DIAMOND;
					dropitem = new ItemStack(dropmaterial,bonus);
					break;
				case LAPIS_ORE:
					Dye dye = new Dye();
					dye.setColor(DyeColor.BLUE);

					bonus *= (rand * 4) + 4;
					dropitem = dye.toItemStack(bonus);
					break;
				case EMERALD_ORE:
					dropmaterial = Material.EMERALD;
					dropitem = new ItemStack(dropmaterial,bonus);
					break;
				case REDSTONE_ORE:
					dropmaterial = Material.REDSTONE;
					bonus *= rand + 4;
					dropitem = new ItemStack(dropmaterial,bonus);
					break;
				case GLOWING_REDSTONE_ORE:
					dropmaterial = Material.REDSTONE;
					bonus *= rand + 4;
					dropitem = new ItemStack(dropmaterial,bonus);
					break;
				case QUARTZ_ORE:
					dropmaterial = Material.QUARTZ;
					dropitem = new ItemStack(dropmaterial,bonus);
					break;
				default:
					break;
			}
		}else{
			//シルク幸運なしの処理
			switch(breakmaterial){
				case COAL_ORE:
					dropmaterial = Material.COAL;
					dropitem = new ItemStack(dropmaterial);
					break;
				case DIAMOND_ORE:
					dropmaterial = Material.DIAMOND;
					dropitem = new ItemStack(dropmaterial);
					break;
				case LAPIS_ORE:
					Dye dye = new Dye();
					dye.setColor(DyeColor.BLUE);
					dropitem = dye.toItemStack((int) ((rand*4) + 4));
					break;
				case EMERALD_ORE:
					dropmaterial = Material.EMERALD;
					dropitem = new ItemStack(dropmaterial);
					break;
				case REDSTONE_ORE:
					dropmaterial = Material.REDSTONE;
					dropitem = new ItemStack(dropmaterial,(int) (rand+4));
					break;
				case GLOWING_REDSTONE_ORE:
					dropmaterial = Material.REDSTONE;
					dropitem = new ItemStack(dropmaterial,(int) (rand+4));
					break;
				case QUARTZ_ORE:
					dropmaterial = Material.QUARTZ;
					dropitem = new ItemStack(dropmaterial);
					break;
				case STONE:
					//Material.STONEの処理
					if(breakblock.getData() == 0x00){
						//焼き石の処理
						dropmaterial = Material.COBBLESTONE;
						dropitem = new ItemStack(dropmaterial);
					}else{
						//他の石の処理
						dropitem = new ItemStack(breakmaterial,1,b);
					}
					break;
				case GRASS:
					//芝生の処理
					dropmaterial = Material.DIRT;
					dropitem = new ItemStack(dropmaterial);
					break;
				case GRAVEL:
					double p;
					switch(fortunelevel){
					case 1:
						p = 0.14;
						break;
					case 2:
						p = 0.25;
						break;
					case 3:
						p = 1.00;
						break;
					default :
						p = 0.1;
						break;
					}
					if(p>rand){
						dropmaterial = Material.FLINT;
					}else{
						dropmaterial = Material.GRAVEL;
					}
					dropitem = new ItemStack(dropmaterial,bonus);
					break;
				case LEAVES:
				case LEAVES_2:
					dropitem = null;
					break;
				case CLAY:
					dropmaterial = Material.CLAY_BALL;
					dropitem = new ItemStack(dropmaterial,4);
					break;
				case MONSTER_EGGS:
					Location loc = breakblock.getLocation();
					breakblock.getWorld().spawnEntity(loc, EntityType.SILVERFISH);
					dropitem = null;
					break;
				case LOG:
				case LOG_2:
					dropitem = new ItemStack(breakmaterial,1,b_tree);
					break;
				default:
					//breakblcokのままのアイテムスタックを保存
					dropitem = new ItemStack(breakmaterial,1,b);
					break;
			}
		}
		return dropitem;
	}

	public static double calcManaDrop(PlayerData playerdata) {
		//０～１のランダムな値を取得
		double rand = Math.random();
		//10%の確率で経験値付与
		if(rand < 0.1){
			//Lv8未満は獲得経験値ゼロ、それ以上はレベルに応じて経験値付与
			if(playerdata.getLevel() < 8 || playerdata.getActiveskilldata().skillcanbreakflag == false){
				return 0;
			}else if (playerdata.getLevel() < 18){
				return SeichiAssist.config.getDropExplevel(1);
			}else if (playerdata.getLevel() < 28){
				return SeichiAssist.config.getDropExplevel(2);
			}else if (playerdata.getLevel() < 38){
				return SeichiAssist.config.getDropExplevel(3);
			}else if (playerdata.getLevel() < 48){
				return SeichiAssist.config.getDropExplevel(4);
			}else if (playerdata.getLevel() < 58){
				return SeichiAssist.config.getDropExplevel(5);
			}else if (playerdata.getLevel() < 68){
				return SeichiAssist.config.getDropExplevel(6);
			}else if (playerdata.getLevel() < 78){
				return SeichiAssist.config.getDropExplevel(7);
			}else if (playerdata.getLevel() < 88){
				return SeichiAssist.config.getDropExplevel(8);
			}else if (playerdata.getLevel() < 98){
				return SeichiAssist.config.getDropExplevel(9);
			}else{
				return SeichiAssist.config.getDropExplevel(10);
			}
		}else{
			return 0;
		}
	}
	//num回だけ耐久を減らす処理
	public static short calcDurability(int enchantmentLevel,int num) {
		Random rand = new Random();
        double probability = 1.0 / (enchantmentLevel + 1.0);

        short durability = (short) IntStream.range(0, num)
                .filter(i -> probability > rand.nextDouble())
                .count();
		return durability;
	}

	public static String getCardinalDirection(Entity entity) {
		double rotation = (entity.getLocation().getYaw() + 180) % 360;
		Location loc = entity.getLocation();
		float pitch = loc.getPitch();
		if (rotation < 0) {
		rotation += 360.0;
		}

		if(pitch <= -30){
			return "U";
		}else if(pitch >= 25){
			return "D";
		}else if (0 <= rotation && rotation < 45.0) {
			return "N";
		}else if (45.0 <= rotation && rotation < 135.0) {
			return "E";
		}else if (135.0 <= rotation && rotation < 225.0) {
			return "S";
		}else if (225.0 <= rotation && rotation < 315.0) {
			return "W";
		}else if (315.0 <= rotation && rotation < 360.0) {
		return "N";
		} else {
		return null;
		}
	}

	public static boolean BlockEqualsMaterialList(final Block block){
		return SeichiAssist.materiallist.contains(block.getType());
	}

	/**
	 * @param player	破壊プレイヤー
	 * @param block		手動破壊対象またはアサルト/遠距離の指定座標
	 * @param isAssault	true:	アサルトアーマーによる破壊
	 * 					false:	アクティブスキルまたは手動による破壊
	 * @return 重力値（破壊範囲の上に積まれているブロック数）
	 */
	public static int getGravity(Player player, Block block, boolean isAssault) {
		/** OPENHEIGHTマス以上のtransparentmateriallistブロックの連続により、地上判定とする。 */
		final int OPENHEIGHT = 3;

		// 1. 重力値を適用すべきか判定
		// 整地ワールド判定
		if (!Util.isSeichiWorld(player)) {
			return 0;
		}

		// 2. 破壊要因判定
		/** 該当プレイヤーのPlayerData */
		PlayerData playerdata = SeichiAssist.playermap.get(player.getUniqueId());
		/** ActiveSkillのリスト */
		ActiveSkill[] skilllist = ActiveSkill.values();
		/** 重力値の計算を始めるY座標 */
		int startY;
		// Activeスキルの場合
		if (!isAssault) {
			/** 破壊要因スキルタイプ */
			int breakSkillType = playerdata.getActiveskilldata().skilltype;
			/** 破壊要因スキルレベル */
			int breakSkillLevel = playerdata.getActiveskilldata().skillnum;
			/** 破壊スキル使用判定 */
			boolean isBreakSkill = (breakSkillType > 0) && (playerdata.getActiveskilldata().mineflagnum > 0);
			// 重力値を計算開始するBlockのために、startY(blockのY方向offset値)を計算
			// 破壊スキルが選択されていなければ初期座標は破壊ブロックと同値
			if (!isBreakSkill) {
				startY = 0;
			}
			// 遠距離スキルの場合向きに依らずblock中心の横範囲となる
			else if (breakSkillType == ActiveSkill.ARROW.gettypenum()) {
				/** 選択中のスキルの破壊範囲 */
				Coordinate skillBreakArea = skilllist[breakSkillType - 1].getBreakLength(breakSkillLevel);
				// 破壊ブロックの高さ＋破壊範囲の高さ－2（2段目が手動破壊対象となるため）
				startY = skillBreakArea.y - 2;
			}
			// 単範囲/複数範囲破壊スキルの場合
			else {
				/** 該当プレイヤーが向いている方向 */
				String dir = BreakUtil.getCardinalDirection(player);
				// 下向きによる発動
				if (dir.equals("D")) {
					// block＝破壊範囲の最上層ブロックにつき、startは0
					startY = 0;
				}
				// 上向きによる発動
				else if (dir.equals("U")) {
					/** 選択中のスキルの破壊範囲 */
					Coordinate skillBreakArea = skilllist[breakSkillType - 1].getBreakLength(breakSkillLevel);
					// block＝破壊範囲の最下層ブロックにつき、startは破壊範囲の高さ
					startY = skillBreakArea.y;
				}
				// 横向きによる発動のうち、デュアルorトリアルのmineflagnumが1(上破壊)
				else if ((breakSkillLevel == 1 || breakSkillLevel == 2) && playerdata.getActiveskilldata().mineflagnum == 1) {
					// 破壊ブロックの1マス上が破壊されるので、startは2段目から
					startY = 1;
				}
				// その他横向き発動時
				else {
					/** 選択中のスキルの破壊範囲 */
					Coordinate skillBreakArea = skilllist[breakSkillType - 1].getBreakLength(breakSkillLevel);
					// 破壊ブロックの高さ＋破壊範囲の高さ－2（2段目が手動破壊対象となるため）
					startY = skillBreakArea.y - 2;
				}
			}
		}
		// Assaultスキルの場合
		else {
			/** 破壊要因スキルタイプ */
			int breakSkillType = playerdata.getActiveskilldata().assaulttype;
			/** 破壊要因スキルレベル */
			int breakSkillLevel = playerdata.getActiveskilldata().assaultnum;
			/** 選択中のスキルの破壊範囲 */
			Coordinate skillBreakArea = skilllist[breakSkillType - 1].getBreakLength(breakSkillLevel);
			// アサルトアーマーの場合
			if (breakSkillType == ActiveSkill.ARMOR.gettypenum()) {
				// スキル高さ - 足位置で1 - blockが1段目なので1
				startY = skillBreakArea.y - 2;
			}
			// その他のアサルトスキルの場合
			else {
				// 高さはスキル/2の切り上げ…blockが1段目なので-1してプラマイゼロ
				startY = (skillBreakArea.y - 1) / 2;
			}
		}

		// 3. 重力値計算
		/** OPENHEIGHTに達したかの計測カウンタ */
		int openCount = 0;
		/** 重力値 */
		int gravity = 0;
		/** 最大ループ数 */
		final int YMAX = 255;
		for (int checkPointer = 1; checkPointer < YMAX; checkPointer++) {
			/** 確認対象ブロック */
			Block target = block.getRelative(0, startY + checkPointer, 0);
			// 対象ブロックが地上判定ブロックの場合
			if (SeichiAssist.transparentmateriallist.contains(target.getType())) {
				// カウンタを加算
				openCount++;
				if (openCount >= OPENHEIGHT) {
					break;
				}
			} else {
				// カウンタをクリア
				openCount = 0;
				// 重力値を加算(水をは2倍にする)
				if (target.getType() == Material.WATER) {
					gravity += 2;
				} else {
					gravity++;
				}
			}
		}

		return gravity;
	}

	@SuppressWarnings("deprecation")
	public static boolean logRemove(final Player player, final Block removedBlock) {
		final CoreProtectWrapper wrapper = ExternalPlugins.getCoreProtectWrapper();
		if (wrapper == null) {
			player.sendMessage(ChatColor.RED + "error:coreprotectに保存できませんでした。管理者に報告してください。");
			return false;
		}

		final boolean failure = !wrapper.queueBlockRemoval(player, removedBlock);

		//もし失敗したらプレイヤーに報告し処理を終了
		if(failure){
			player.sendMessage(ChatColor.RED + "error:coreprotectに保存できませんでした。管理者に報告してください。");
			return false;
		}
		return true;
	}

}
