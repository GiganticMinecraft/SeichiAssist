package com.github.unchama.seichiassist.listener;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.Util;

public class PlayerPickupItemListener implements Listener {
	SeichiAssist plugin = SeichiAssist.plugin;
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	private Config config = SeichiAssist.config;
	@EventHandler
	public void MineStackEvent(PlayerPickupItemEvent event){
		//実行したプレイヤーを取得
		Player player = event.getPlayer();
		//もしサバイバルでなければ処理を終了
		if(!player.getGameMode().equals(GameMode.SURVIVAL)){
			return;
		}
		UUID uuid = player.getUniqueId();
		PlayerData playerdata = playermap.get(uuid);
		//念のためエラー分岐
		if(playerdata == null){
			return;
		}
		//レベルが足りない場合処理終了
		if(playerdata.level < config.getMineStacklevel(1)){
			return;
		}
		//minestackflagがfalseの時は処理を終了
		if(!playerdata.minestackflag){
			return;
		}

		Item item = event.getItem();
		ItemStack itemstack = item.getItemStack();
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

		//ここにガチャアイテム(ItemStack型)判定を作成するかも
		/*
		if(playerdata!=null){
			String name = playerdata.name;
		    for(GachaData gachadata : gachadatalist){
		        if(!gachadata.itemstack.hasItemMeta()){
		             continue;
		        }else if(!gachadata.itemstack.getItemMeta().hasLore()){
		             continue;
		        }
		        //ガチャ景品リストにある商品の場合(Lore=説明文と表示名で判別),無い場合はアイテム返却
		        if(gachadata.compare(itemstack,name)){
		        	if(SeichiAssist.DEBUG){
		        		player.sendMessage(gachadata.itemstack.getItemMeta().getDisplayName());
		            }
		        if(gachadata.itemstack.getItemMeta().getLore().equals(itemstack.getItemMeta().getLore())
		          &&gachadata.itemstack.getItemMeta().getDisplayName().equals(itemstack.getItemMeta().getDisplayName())){
		            }
		        }
		    }
		}
		*/


		for(int i=0; i<SeichiAssist.minestacklist.size(); i++){
			if(material.equals(SeichiAssist.minestacklist.get(i).getMaterial()) &&
				itemstack.getDurability() == SeichiAssist.minestacklist.get(i).getDurability()){
				//この時点でIDとサブIDが一致している
				if(SeichiAssist.minestacklist.get(i).getNameloreflag()==false){//名前と説明文が無いアイテム
					if(playerdata.level < config.getMineStacklevel(SeichiAssist.minestacklist.get(i).getLevel())){
						//レベルを満たしていない
						return;
					} else {
						playerdata.minestack.addNum(i, amount);
						break;
					}
				} else {
					//名前・説明文付き
					ItemMeta meta = itemstack.getItemMeta();
					if(meta==null || meta.getDisplayName()==null || meta.getLore()== null){
						return;
					}
					//この時点で名前と説明文がある
						if(SeichiAssist.minestacklist.get(i).getGachatype()==-1){ //ガチャ以外のアイテム(がちゃりんご)
							if( !(meta.getDisplayName().equals(Util.getGachaimoName()))
								|| !(meta.getLore().equals(Util.getGachaimoLore())) ){
								return;
							}
							if(playerdata.level < config.getMineStacklevel(SeichiAssist.minestacklist.get(i).getLevel())){
								//レベルを満たしていない
								return;
							} else {
								playerdata.minestack.addNum(i, amount);
								break;
							}
						} else {
							//ガチャ品
						}
				}
			}
		}

		//Material型判定
		/*
		switch(material){
			case DIRT:
				if(playerdata.level < v1){
					return;
				}
				if(itemstack.getDurability() == 0){
					playerdata.minestack.dirt += amount;
					break;
				}
				if(playerdata.level < v26){
					return;
				}
				if(itemstack.getDurability() == 1){
					playerdata.minestack.dirt1 += amount;
				} else if(itemstack.getDurability() == 2){
					playerdata.minestack.dirt2 += amount;
				} else {
					return;
				}
				break;
			case GRASS:
				if(playerdata.level < v1){
					return;
				}
				playerdata.minestack.grass += amount;
				break;
			case GRAVEL:
				if(playerdata.level < v5){
					return;
				}
				playerdata.minestack.gravel += amount;
				break;
			case COBBLESTONE:
				if(playerdata.level < v2){
					return;
				}
				playerdata.minestack.cobblestone += amount;
				break;
			case STONE:
				if(playerdata.level < v2){
					return;
				}
				if(itemstack.getDurability() == 0){
					playerdata.minestack.stone += amount;
					break;
				}
				if(playerdata.level < v3){
					return;
				}
				if(itemstack.getDurability() == 1){
					playerdata.minestack.granite += amount;
				} else if(itemstack.getDurability() == 3){
					playerdata.minestack.diorite += amount;
				} else if(itemstack.getDurability() == 5){
					playerdata.minestack.andesite += amount;
				} else {
					return;
				}
				break;
			case SAND:
				if(playerdata.level < v5){
					return;
				}
				if(itemstack.getDurability() == 0){
					playerdata.minestack.sand += amount;
					break;
				}
				if(playerdata.level < v20){
					return;
				}
				if(itemstack.getDurability() == 1){
					playerdata.minestack.red_sand += amount;
				} else {
					return;
				}
				break;
			case PACKED_ICE:
				if(playerdata.level < v10){
					return;
				}
				playerdata.minestack.packed_ice += amount;
				break;
			case SANDSTONE:
				if(playerdata.level < v5 || itemstack.getDurability() != 0){
					return;
				}
				playerdata.minestack.sandstone += amount;
				break;
			case RED_SANDSTONE: //追加
				if(playerdata.level < v20 || itemstack.getDurability() != 0){
					return;
				}
				playerdata.minestack.red_sandstone += amount;
				break;
			case CLAY:
				if(playerdata.level < v23){
					return;
				}
				playerdata.minestack.clay += amount;
				break;
			case NETHERRACK:
				if(playerdata.level < v6){
					return;
				}
				playerdata.minestack.netherrack += amount;
				break;
			case SOUL_SAND:
				if(playerdata.level < v6){
					return;
				}
				playerdata.minestack.soul_sand += amount;
				break;
			case MAGMA:
				if(playerdata.level < v12){
					return;
				}
				playerdata.minestack.magma += amount;
				break;
			case ENDER_STONE:
				if(playerdata.level < v8){
					return;
				}
				playerdata.minestack.ender_stone += amount;
				break;
			case OBSIDIAN:
				if(playerdata.level < v9){
					return;
				}
				playerdata.minestack.obsidian += amount;
				break;
			case GLOWSTONE:
				if(playerdata.level < v13){
					return;
				}
				playerdata.minestack.glowstone += amount;
				break;
			case COAL:
				if(playerdata.level < v7 || itemstack.getDurability() != 0){
					return;
				}
				playerdata.minestack.coal += amount;
				break;
			case COAL_ORE:
				if(playerdata.level < v7){
					return;
				}
				playerdata.minestack.coal_ore += amount;
				break;
			case IRON_ORE:
				if(playerdata.level < v9){
					return;
				}
				playerdata.minestack.iron_ore += amount;
				break;
			case QUARTZ:
				if(playerdata.level < v11){
					return;
				}
				playerdata.minestack.quartz += amount;
				break;
			case QUARTZ_ORE:
				if(playerdata.level < v11){
					return;
				}
				playerdata.minestack.quartz_ore += amount;
				break;
			case GOLD_ORE:
				if(playerdata.level < v13){
					return;
				}
				playerdata.minestack.gold_ore += amount;
				break;

			case LOG:
				if(playerdata.level < v4){
					return;
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
					return;
				}
				break;
			case LOG_2:
				if(playerdata.level < v4){
					return;
				}
				if(itemstack.getDurability() == 0){
					playerdata.minestack.log_2 += amount;
				} else if(itemstack.getDurability() == 1){
					playerdata.minestack.log_21 += amount;
				} else {
					return;
				}
				break;
			case WOOD:
				if(playerdata.level < v14){
					return;
				}
				if(itemstack.getDurability() == 0){
					playerdata.minestack.wood += amount;
					break;
				}
				if(playerdata.level < v27){
					return;
				}
				if(itemstack.getDurability() == 5){
					playerdata.minestack.wood5 += amount;
				} else {
					return;
				}
				break;
			case FENCE:
				if(playerdata.level < v14){
					return;
				}
				playerdata.minestack.fence += amount;
				break;
			case HARD_CLAY:
				if(playerdata.level < v21){
					return;
				}
				playerdata.minestack.hard_clay += amount;
				break;
			case STAINED_CLAY:
				if(playerdata.level < v22){
					return;
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
					return;
				}
				break;
			case INK_SACK:
				if(playerdata.level < v16 || itemstack.getDurability() != 4){
					return;
				}
				playerdata.minestack.lapis_lazuli += amount;
				break;
			case LAPIS_ORE:
				if(playerdata.level < v16){
					return;
				}
				playerdata.minestack.lapis_ore += amount;
				break;
			case EMERALD:
				if(playerdata.level < v18){
					return;
				}
				playerdata.minestack.emerald += amount;
				break;
			case EMERALD_ORE:
				if(playerdata.level < v18){
					return;
				}
				playerdata.minestack.emerald_ore += amount;
				break;
			case REDSTONE:
				if(playerdata.level < v15){
					return;
				}
				playerdata.minestack.redstone += amount;
				break;
			case REDSTONE_ORE:
				if(playerdata.level < v15){
					return;
				}
				playerdata.minestack.redstone_ore += amount;
				break;
			case DIAMOND:
				if(playerdata.level < v17){
					return;
				}
				playerdata.minestack.diamond += amount;
				break;
			case DIAMOND_ORE:
				if(playerdata.level < v17){
					return;
				}
				playerdata.minestack.diamond_ore += amount;
				break;

			case MYCEL:
				if(playerdata.level < v33){
					return;
				}
				playerdata.minestack.mycel += amount;
				break;
			case SNOW_BLOCK:
				if(playerdata.level < v31){
					return;
				}
				playerdata.minestack.snow_block += amount;
				break;
			case ICE:
				if(playerdata.level < v25){
					return;
				}
				playerdata.minestack.ice += amount;
				break;
			case DARK_OAK_FENCE:
				if(playerdata.level < v27){
					return;
				}
				playerdata.minestack.dark_oak_fence += amount;
				break;
			case MOSSY_COBBLESTONE:
				if(playerdata.level < v24){
					return;
				}
				playerdata.minestack.mossy_cobblestone += amount;
				break;
			case RAILS:
				if(playerdata.level < v29){
					return;
				}
				playerdata.minestack.rails += amount;
				break;
			case EXP_BOTTLE:
				if(playerdata.level < v19){
					return;
				}
				playerdata.minestack.exp_bottle += amount;
				break;
			case HUGE_MUSHROOM_1:
				if(playerdata.level < v32){
					return;
				}
				playerdata.minestack.huge_mushroom_1 += amount;
				break;
			case HUGE_MUSHROOM_2:
				if(playerdata.level < v32){
					return;
				}
				playerdata.minestack.huge_mushroom_2 += amount;
				break;
			case WEB:
				if(playerdata.level < v28){
					return;
				}
				playerdata.minestack.web += amount;
				break;
			case STRING:
				if(playerdata.level < v28){
					return;
				}
				playerdata.minestack.string += amount;
				break;
			case SAPLING:
				if(playerdata.level < v34){
					return;
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
					return;
				}
				break;
			case LEAVES:
				if(playerdata.level < v30){
					return;
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
					return;
				}
				break;
			case LEAVES_2:
				if(playerdata.level < v30){
					return;
				}
				if(itemstack.getDurability() == 0){
					playerdata.minestack.leaves_2 += amount;
				} else if(itemstack.getDurability() == 1){
					playerdata.minestack.leaves_21 += amount;
				} else {
					return;
				}
				break;
			case GOLDEN_APPLE: //追加
				if(playerdata.level < v19 || itemstack.getDurability() != 0){
					return;
				}
				ItemMeta meta = itemstack.getItemMeta();
				if(meta==null || meta.getDisplayName()==null || meta.getLore()== null){
					return;
				}
				if( !(meta.getDisplayName().equals(Util.getGachaimoName()))
						|| !(meta.getLore().equals(Util.getGachaimoLore())) ){
					return;
				}
				playerdata.minestack.gachaimo += amount;
				break;

			default:
				return;
		}
		*/
		event.setCancelled(true);
		player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
		item.remove();
	}
}