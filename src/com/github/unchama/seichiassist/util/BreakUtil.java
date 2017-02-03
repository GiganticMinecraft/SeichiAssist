package com.github.unchama.seichiassist.util;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.coreprotect.CoreProtectAPI;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;

import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.MineStackGachaData;
import com.github.unchama.seichiassist.data.PlayerData;

public class BreakUtil {
	//他のプラグインの影響があってもブロックを破壊できるのか
	@SuppressWarnings("deprecation")
	public static boolean canBreak(Player player ,Block breakblock) {
		if(!player.isOnline() || breakblock == null){
			return false;
		}
		HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
		UUID uuid = player.getUniqueId();
		PlayerData playerdata = playermap.get(uuid);

		//壊されるブロックの状態を取得
		BlockState blockstate = breakblock.getState();
		//壊されるブロックのデータを取得
		byte data = blockstate.getData().getData();


		//壊されるブロックがワールドガード範囲だった場合処理を終了
		//ここをオンオフ可能にする
		if(!Util.getWorldGuard().canBuild(player, breakblock.getLocation())){
			if(playerdata.dispworldguardlogflag){
				player.sendMessage(ChatColor.RED + "ワールドガードで保護されています。");
			}
			return false;
		}

		if(!equalignoreWorld(player.getWorld().getName())){
			//コアプロテクトのクラスを取得
			CoreProtectAPI CoreProtect = Util.getCoreProtect();
			//破壊ログを設定
			Boolean success = CoreProtect.logRemoval(player.getName(), breakblock.getLocation(), blockstate.getType(),data);
			//もし失敗したらプレイヤーに報告し処理を終了
			if(!success){
				player.sendMessage(ChatColor.RED + "coreprotectに保存できませんでした。管理者に報告してください。");
				return false;
			}
		}

		return true;
	}
	private static boolean equalignoreWorld(String name) {
		List<String> ignoreworldlist = SeichiAssist.ignoreWorldlist;
		for(String s : ignoreworldlist){
			if(name.equalsIgnoreCase(s.toLowerCase())){
				return true;
			}
		}
		return false;
	}
	//ブロックを破壊する処理、ドロップも含む、統計増加も含む
	public static void BreakBlock(Player player,Block breakblock,Location centerofblock,ItemStack tool,Boolean stepflag) {

		Material material = breakblock.getType();
		if(!SeichiAssist.materiallist.contains(material)){
			return;
		}

		ItemStack itemstack = dropItemOnTool(breakblock,tool);


		if(material.equals(Material.GLOWING_REDSTONE_ORE)){
			material = Material.REDSTONE_ORE;
		}
		if(material.equals(Material.AIR)){
			return;
		}

		if(itemstack != null){
			//アイテムをドロップさせる
			if(!addItemtoMineStack(player,itemstack)){
				HashMap<Integer,ItemStack> exceededItems = player.getInventory().addItem(itemstack);
				for(Integer i:exceededItems.keySet()){
					player.sendMessage(ChatColor.RED + "インベントリがいっぱいです");
					breakblock.getWorld().dropItemNaturally(centerofblock,exceededItems.get(i));
				}

			}
		}

		//ブロックを空気に変える
		breakblock.setType(Material.AIR);

		if(stepflag){
			//あたかもプレイヤーが壊したかのようなエフェクトを表示させる、壊した時の音を再生させる
			breakblock.getWorld().playEffect(breakblock.getLocation(), Effect.STEP_SOUND,material);
		}
		//プレイヤーの統計を１増やす
		player.incrementStatistic(Statistic.MINE_BLOCK, material);

	}

	public static boolean addItemtoMineStack(Player player, ItemStack itemstack) {
		SeichiAssist plugin = SeichiAssist.plugin;
		HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
		Config config = SeichiAssist.config;
		//もしサバイバルでなければ処理を終了
		if(!player.getGameMode().equals(GameMode.SURVIVAL)){
			return false;
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
		if(playerdata.level < config.getMineStacklevel(1)){
			return false;
		}
		//minestackflagがfalseの時は処理を終了
		if(!playerdata.minestackflag){
			return false;
		}

		int amount = itemstack.getAmount();
		Material material = itemstack.getType();

		/*
		int v1 = config.getMineStacklevel(1);
		int v2 = config.getMineStacklevel(2);
		int v3 = config.getMineStacklevel(3);
		int v4 = config.getMineStacklevel(4);
		int v5 = config.getMineStacklevel(5);
		int v6 = config.getMineStacklevel(6);
		int v7 = config.getMineStacklevel(7);
		int v8 = config.getMineStacklevel(8);
		int v9 = config.getMineStacklevel(9);
		int v10 = config.getMineStacklevel(10);
		int v11 = config.getMineStacklevel(11);//追加
		int v12 = config.getMineStacklevel(12);//追加
		int v13 = config.getMineStacklevel(13);//追加
		int v14 = config.getMineStacklevel(14);//追加
		int v15 = config.getMineStacklevel(15);//追加
		int v16 = config.getMineStacklevel(16);
		int v17 = config.getMineStacklevel(17);
		int v18 = config.getMineStacklevel(18);
		int v19 = config.getMineStacklevel(19);
		int v20 = config.getMineStacklevel(20);
		int v21 = config.getMineStacklevel(21);
		int v22 = config.getMineStacklevel(22);
		int v23 = config.getMineStacklevel(23);
		int v24 = config.getMineStacklevel(24);
		int v25 = config.getMineStacklevel(25);
		int v26 = config.getMineStacklevel(26);
		int v27 = config.getMineStacklevel(27);
		int v28 = config.getMineStacklevel(28);
		int v29 = config.getMineStacklevel(29);
		int v30 = config.getMineStacklevel(30);
		int v31 = config.getMineStacklevel(31);
		int v32 = config.getMineStacklevel(32);
		int v33 = config.getMineStacklevel(33);
		int v34 = config.getMineStacklevel(34);
		int v35 = config.getMineStacklevel(35);
		int v36 = config.getMineStacklevel(36);
		int v37 = config.getMineStacklevel(37);
		int v38 = config.getMineStacklevel(38);
		*/
		//boolean delete_flag=false;

		int i=0;
		for(i=0; i<SeichiAssist.minestacklist.size(); i++){
			if(material.equals(SeichiAssist.minestacklist.get(i).getMaterial()) &&
				itemstack.getDurability() == SeichiAssist.minestacklist.get(i).getDurability()){
				//この時点でIDとサブIDが一致している
				if(SeichiAssist.minestacklist.get(i).getNameloreflag()==false && (!itemstack.getItemMeta().hasLore() && !itemstack.getItemMeta().hasDisplayName() ) ){//名前と説明文が無いアイテム
					if(playerdata.level < config.getMineStacklevel(SeichiAssist.minestacklist.get(i).getLevel())){
						//レベルを満たしていない
						return false;
					} else {
						playerdata.minestack.addNum(i, amount);
						//delete_flag=true;
						break;
					}
				} else if(SeichiAssist.minestacklist.get(i).getNameloreflag()==true && itemstack.getItemMeta().hasDisplayName() && itemstack.getItemMeta().hasLore()){
					//名前・説明文付き
					ItemMeta meta = itemstack.getItemMeta();
					/*
					if(meta==null || meta.getDisplayName()==null || meta.getLore()== null){
						return;
					}
					*/
					//この時点で名前と説明文がある
						if(SeichiAssist.minestacklist.get(i).getGachatype()==-1){ //ガチャ以外のアイテム(がちゃりんご)
							if( !(meta.getDisplayName().equals(Util.getGachaimoName()))
								|| !(meta.getLore().equals(Util.getGachaimoLore())) ){
								return false;
							}
							if(playerdata.level < config.getMineStacklevel(SeichiAssist.minestacklist.get(i).getLevel())){
								//レベルを満たしていない
								return false;
							} else {
								playerdata.minestack.addNum(i, amount);
								break;
							}
						} else {
							//ガチャ品
							MineStackGachaData g = SeichiAssist.msgachadatalist.get(SeichiAssist.minestacklist.get(i).getGachatype());
							String name = playerdata.name; //プレイヤーのネームを見る
							if(g.probability<0.1){ //カタログギフト券を除く(名前があるアイテム)
								if(!Util.ItemStackContainsOwnerName(itemstack, name)){
									//所有者の名前が無ければreturn
									return false;
								}
							}
							//ItemStack itemstack_temp = Util.ItemStackResetName(itemstack);//名前を消しておく

							//GachaData.
							if(!g.compareonly(itemstack)){ //この1行で対応可能？
								//gachadata.itemstack.isSimilar(itemstack)でスタックサイズ以外が一致しているか判定可能
								//continue; //アイテムの中身が違う
							} else { //中身が同じ場合のみここに入る
								if(playerdata.level < config.getMineStacklevel(SeichiAssist.minestacklist.get(i).getLevel())){
									//レベルを満たしていない
									return false;
								} else {
									playerdata.minestack.addNum(i, amount);
									//delete_flag=true;
									break;
								}
							}

						}
				}
			}
		}
		if(i==SeichiAssist.minestacklist.size()){
			return false;
		}

		/*
		switch(material){
			case DIRT:
				if(playerdata.level < v1){
					return false;
				}
				if(itemstack.getDurability() == 0){
					playerdata.minestack.dirt += amount;
					break;
				}
				if(playerdata.level < v26){
					return false;
				}
				if(itemstack.getDurability() == 1){
					playerdata.minestack.dirt1 += amount;
				} else if(itemstack.getDurability() == 2){
					playerdata.minestack.dirt2 += amount;
				} else {
					return false;
				}
				break;
			case GRASS:
				if(playerdata.level < v1){
					return false;
				}
				playerdata.minestack.grass += amount;
				break;
			case GRAVEL:
				if(playerdata.level < v5){
					return false;
				}
				playerdata.minestack.gravel += amount;
				break;
			case COBBLESTONE:
				if(playerdata.level < v2){
					return false;
				}
				playerdata.minestack.cobblestone += amount;
				break;
			case STONE:
				if(playerdata.level < v2){
					return false;
				}
				if(itemstack.getDurability() == 0){
					playerdata.minestack.stone += amount;
					break;
				}
				if(playerdata.level < v3){
					return false;
				}
				if(itemstack.getDurability() == 1){
					playerdata.minestack.granite += amount;
				} else if(itemstack.getDurability() == 3){
					playerdata.minestack.diorite += amount;
				} else if(itemstack.getDurability() == 5){
					playerdata.minestack.andesite += amount;
				} else {
					return false;
				}
				break;
			case SAND:
				if(playerdata.level < v5){
					return false;
				}
				if(itemstack.getDurability() == 0){
					playerdata.minestack.sand += amount;
					break;
				}
				if(playerdata.level < v20){
					return false;
				}
				if(itemstack.getDurability() == 1){
					playerdata.minestack.red_sand += amount;
				} else {
					return false;
				}
				break;
			case PACKED_ICE:
				if(playerdata.level < v10){
					return false;
				}
				playerdata.minestack.packed_ice += amount;
				break;
			case SANDSTONE:
				if(playerdata.level < v5 || itemstack.getDurability() != 0){
					return false;
				}
				playerdata.minestack.sandstone += amount;
				break;
			case RED_SANDSTONE: //追加
				if(playerdata.level < v20 || itemstack.getDurability() != 0){
					return false;
				}
				playerdata.minestack.red_sandstone += amount;
				break;
			case CLAY:
				if(playerdata.level < v23){
					return false;
				}
				playerdata.minestack.clay += amount;
				break;
			case NETHERRACK:
				if(playerdata.level < v6){
					return false;
				}
				playerdata.minestack.netherrack += amount;
				break;
			case SOUL_SAND:
				if(playerdata.level < v6){
					return false;
				}
				playerdata.minestack.soul_sand += amount;
				break;
			case MAGMA:
				if(playerdata.level < v12){
					return false;
				}
				playerdata.minestack.magma += amount;
				break;
			case ENDER_STONE:
				if(playerdata.level < v8){
					return false;
				}
				playerdata.minestack.ender_stone += amount;
				break;
			case OBSIDIAN:
				if(playerdata.level < v9){
					return false;
				}
				playerdata.minestack.obsidian += amount;
				break;
			case GLOWSTONE:
				if(playerdata.level < v13){
					return false;
				}
				playerdata.minestack.glowstone += amount;
				break;
			case COAL:
				if(playerdata.level < v7 || itemstack.getDurability() != 0){
					return false;
				}
				playerdata.minestack.coal += amount;
				break;
			case COAL_ORE:
				if(playerdata.level < v7){
					return false;
				}
				playerdata.minestack.coal_ore += amount;
				break;
			case IRON_ORE:
				if(playerdata.level < v9){
					return false;
				}
				playerdata.minestack.iron_ore += amount;
				break;
			case QUARTZ:
				if(playerdata.level < v11){
					return false;
				}
				playerdata.minestack.quartz += amount;
				break;
			case QUARTZ_ORE:
				if(playerdata.level < v11){
					return false;
				}
				playerdata.minestack.quartz_ore += amount;
				break;
			case GOLD_ORE:
				if(playerdata.level < v13){
					return false;
				}
				playerdata.minestack.gold_ore += amount;
				break;

			case LOG:
				if(playerdata.level < v4){
					return false;
				}
				if(itemstack.getDurability() == 0){
					playerdata.minestack.log += amount;
				} else if(itemstack.getDurability() == 1){
					playerdata.minestack.log1 += amount;
				} else if(itemstack.getDurability() == 2){
					playerdata.minestack.log2 += amount;
				} else if(itemstack.getDurability() == 3){
					playerdata.minestack.log3 += amount;
				} else {
					return false;
				}
				break;
			case LOG_2:
				if(playerdata.level < v4){
					return false;
				}
				if(itemstack.getDurability() == 0){
					playerdata.minestack.log_2 += amount;
				} else if(itemstack.getDurability() == 1){
					playerdata.minestack.log_21 += amount;
				} else {
					return false;
				}
				break;
			case WOOD:
				if(playerdata.level < v14){
					return false;
				}
				if(itemstack.getDurability() == 0){
					playerdata.minestack.wood += amount;
					break;
				}
				if(playerdata.level < v27){
					return false;
				}
				if(itemstack.getDurability() == 5){
					playerdata.minestack.wood5 += amount;
				} else {
					return false;
				}
				break;
			case FENCE:
				if(playerdata.level < v14){
					return false;
				}
				playerdata.minestack.fence += amount;
				break;
			case HARD_CLAY:
				if(playerdata.level < v21){
					return false;
				}
				playerdata.minestack.hard_clay += amount;
				break;
			case STAINED_CLAY:
				if(playerdata.level < v22){
					return false;
				}
				if(itemstack.getDurability() == 0){
					playerdata.minestack.stained_clay += amount;
				} else if(itemstack.getDurability() == 1){
					playerdata.minestack.stained_clay1 += amount;
				} else if(itemstack.getDurability() == 4){
					playerdata.minestack.stained_clay4 += amount;
				} else if(itemstack.getDurability() == 8){
					playerdata.minestack.stained_clay8 += amount;
				} else if(itemstack.getDurability() == 12){
					playerdata.minestack.stained_clay12 += amount;
				} else if(itemstack.getDurability() == 14){
					playerdata.minestack.stained_clay14 += amount;
				} else {
					return false;
				}
				break;
			case INK_SACK:
				if(playerdata.level < v16 || itemstack.getDurability() != 4){
					return false;
				}
				playerdata.minestack.lapis_lazuli += amount;
				break;
			case LAPIS_ORE:
				if(playerdata.level < v16){
					return false;
				}
				playerdata.minestack.lapis_ore += amount;
				break;
			case EMERALD:
				if(playerdata.level < v18){
					return false;
				}
				playerdata.minestack.emerald += amount;
				break;
			case EMERALD_ORE:
				if(playerdata.level < v18){
					return false;
				}
				playerdata.minestack.emerald_ore += amount;
				break;
			case REDSTONE:
				if(playerdata.level < v15){
					return false;
				}
				playerdata.minestack.redstone += amount;
				break;
			case REDSTONE_ORE:
				if(playerdata.level < v15){
					return false;
				}
				playerdata.minestack.redstone_ore += amount;
				break;
			case DIAMOND:
				if(playerdata.level < v17){
					return false;
				}
				playerdata.minestack.diamond += amount;
				break;
			case DIAMOND_ORE:
				if(playerdata.level < v17){
					return false;
				}
				playerdata.minestack.diamond_ore += amount;
				break;

				case MYCEL:
					if(playerdata.level < v33){
						return false;
					}
					playerdata.minestack.mycel += amount;
					break;
				case SNOW_BLOCK:
					if(playerdata.level < v31){
						return false;
					}
					playerdata.minestack.snow_block += amount;
					break;
				case ICE:
					if(playerdata.level < v25){
						return false;
					}
					playerdata.minestack.ice += amount;
					break;
				case DARK_OAK_FENCE:
					if(playerdata.level < v27){
						return false;
					}
					playerdata.minestack.dark_oak_fence += amount;
					break;
				case MOSSY_COBBLESTONE:
					if(playerdata.level < v24){
						return false;
					}
					playerdata.minestack.mossy_cobblestone += amount;
					break;
				case RAILS:
					if(playerdata.level < v29){
						return false;
					}
					playerdata.minestack.rails += amount;
					break;
				case EXP_BOTTLE:
					if(playerdata.level < v19){
						return false;
					}
					playerdata.minestack.exp_bottle += amount;
					break;
				case HUGE_MUSHROOM_1:
					if(playerdata.level < v32){
						return false;
					}
					playerdata.minestack.huge_mushroom_1 += amount;
					break;
				case HUGE_MUSHROOM_2:
					if(playerdata.level < v32){
						return false;
					}
					playerdata.minestack.huge_mushroom_2 += amount;
					break;
				case WEB:
					if(playerdata.level < v28){
						return false;
					}
					playerdata.minestack.web += amount;
					break;
				case STRING:
					if(playerdata.level < v28){
						return false;
					}
					playerdata.minestack.string += amount;
					break;
				case SAPLING:
					if(playerdata.level < v34){
						return false;
					}
					if(itemstack.getDurability() == 0){
						playerdata.minestack.sapling += amount;
					} else if(itemstack.getDurability() == 1){
						playerdata.minestack.sapling1 += amount;
					} else if(itemstack.getDurability() == 2){
						playerdata.minestack.sapling2 += amount;
					} else if(itemstack.getDurability() == 3){
						playerdata.minestack.sapling3 += amount;
					} else if(itemstack.getDurability() == 4){
						playerdata.minestack.sapling4 += amount;
					} else if(itemstack.getDurability() == 5){
						playerdata.minestack.sapling5 += amount;
					} else {
						return false;
					}
					break;
				case LEAVES:
					if(playerdata.level < v30){
						return false;
					}
					if(itemstack.getDurability() == 0){
						playerdata.minestack.leaves += amount;
					} else if(itemstack.getDurability() == 1){
						playerdata.minestack.leaves1 += amount;
					} else if(itemstack.getDurability() == 2){
						playerdata.minestack.leaves2 += amount;
					} else if(itemstack.getDurability() == 3){
						playerdata.minestack.leaves3 += amount;
					} else {
						return false;
					}
					break;
				case LEAVES_2:
					if(playerdata.level < v30){
						return false;
					}
					if(itemstack.getDurability() == 0){
						playerdata.minestack.leaves_2 += amount;
					} else if(itemstack.getDurability() == 1){
						playerdata.minestack.leaves_21 += amount;
					} else {
						return false;
					}
					break;
				case GOLDEN_APPLE: //追加
					if(playerdata.level < v19 || itemstack.getDurability() != 0){
						return false;
					}
					ItemMeta meta = itemstack.getItemMeta();
					if(meta==null || meta.getDisplayName()==null || meta.getLore()== null){
						return false;
					}
					if( !(meta.getDisplayName().equals(Util.getGachaimoName()))
							|| !(meta.getLore().equals(Util.getGachaimoLore())) ){
						return false;
					}
					playerdata.minestack.gachaimo += amount;
					break;

			default:
				return false;
		}
		*/
		//player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, (float)0.1, (float)1);
		return true;

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
					double p = 0;
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
/*マナ追加のためいったん消えてもらおう
	//追加経験値の設定
	public static int calcExpDrop(PlayerData playerdata) {
		//０～１のランダムな値を取得
		double rand = Math.random();
		//10%の確率で経験値付与
		if(rand < 0.1){
			//Lv8未満は獲得経験値ゼロ、それ以上はレベルに応じて経験値付与
			if(playerdata.level < 8 || playerdata.activeskilldata.skillcanbreakflag == false){
				return 0;
			}else if (playerdata.level < 18){
				return SeichiAssist.config.getDropExplevel(1);
			}else if (playerdata.level < 28){
				return SeichiAssist.config.getDropExplevel(2);
			}else if (playerdata.level < 38){
				return SeichiAssist.config.getDropExplevel(3);
			}else if (playerdata.level < 48){
				return SeichiAssist.config.getDropExplevel(4);
			}else if (playerdata.level < 58){
				return SeichiAssist.config.getDropExplevel(5);
			}else if (playerdata.level < 68){
				return SeichiAssist.config.getDropExplevel(6);
			}else if (playerdata.level < 78){
				return SeichiAssist.config.getDropExplevel(7);
			}else if (playerdata.level < 88){
				return SeichiAssist.config.getDropExplevel(8);
			}else if (playerdata.level < 98){
				return SeichiAssist.config.getDropExplevel(9);
			}else{
				return SeichiAssist.config.getDropExplevel(10);
			}
		}else{
			return 0;
		}
	}
	*/
	public static double calcManaDrop(PlayerData playerdata) {
		//０～１のランダムな値を取得
		double rand = Math.random();
		//10%の確率で経験値付与
		if(rand < 0.1){
			//Lv8未満は獲得経験値ゼロ、それ以上はレベルに応じて経験値付与
			if(playerdata.level < 8 || playerdata.activeskilldata.skillcanbreakflag == false){
				return 0;
			}else if (playerdata.level < 18){
				return SeichiAssist.config.getDropExplevel(1);
			}else if (playerdata.level < 28){
				return SeichiAssist.config.getDropExplevel(2);
			}else if (playerdata.level < 38){
				return SeichiAssist.config.getDropExplevel(3);
			}else if (playerdata.level < 48){
				return SeichiAssist.config.getDropExplevel(4);
			}else if (playerdata.level < 58){
				return SeichiAssist.config.getDropExplevel(5);
			}else if (playerdata.level < 68){
				return SeichiAssist.config.getDropExplevel(6);
			}else if (playerdata.level < 78){
				return SeichiAssist.config.getDropExplevel(7);
			}else if (playerdata.level < 88){
				return SeichiAssist.config.getDropExplevel(8);
			}else if (playerdata.level < 98){
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
		short durability = 0;
		double probability = 1.0 / (enchantmentLevel + 1.0);

		for(int i = 0; i < num ; i++){
			if(probability >  rand.nextDouble() ){
				durability++;
			}
		}
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

	public static boolean BlockEqualsMaterialList(Block b){
		List<Material> m = SeichiAssist.materiallist;
		for(int i=0; i<m.size(); i++){
			if(b.getType().equals(m.get(i))){
				return true;
			}
		}
		return false;
	}

	public static double getGravity(Player player, Block block, int breakyloc, int weight) {
		int gravity = 2;
		while(!SeichiAssist.transparentmateriallist.contains(block.getRelative(0,gravity,0).getType())){
			gravity++;
		}
		gravity --;
		gravity -= breakyloc;
		gravity= gravity*weight + 1;
		if(gravity < 1)gravity = 1;
		return gravity;
	}
	@SuppressWarnings("deprecation")
	public static boolean logPlace(Player player, Block placeblock) {
		//設置するブロックの状態を取得
		BlockState blockstate = placeblock.getState();
		//設置するブロックのデータを取得
		byte data = blockstate.getData().getData();

		//コアプロテクトのクラスを取得
		CoreProtectAPI CoreProtect = Util.getCoreProtect();
		//破壊ログを設定
		Boolean success = CoreProtect.logRemoval(player.getName(), placeblock.getLocation(), blockstate.getType(),data);
		//もし失敗したらプレイヤーに報告し処理を終了
		if(!success){
			player.sendMessage(ChatColor.RED + "error:coreprotectに保存できませんでした。管理者に報告してください。");
			return false;
		}
		return true;
	}
	public static void addItemToPlayerDirectry(Player player,Block block,ItemStack tool){
		ItemStack dropItem = dropItemOnTool(block, tool);
		if(SeichiAssist.DEBUG){
			player.sendMessage(ChatColor.RED + block.toString());
			player.sendMessage(ChatColor.RED + dropItem.toString());

		}
		PlayerInventory inventory = player.getInventory();
		if(!addItemtoMineStack(player,dropItem)){
			HashMap<Integer,ItemStack> exceededItems = inventory.addItem(dropItem);
			for(Integer i:exceededItems.keySet()){
				player.sendMessage(ChatColor.RED + "インベントリがいっぱいです");
				block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5),exceededItems.get(i));
			}

		}
		player.incrementStatistic(Statistic.MINE_BLOCK, block.getType());
		block.setType(Material.AIR);


	}
}
