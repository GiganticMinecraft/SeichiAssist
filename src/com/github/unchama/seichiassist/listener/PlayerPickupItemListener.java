package com.github.unchama.seichiassist.listener;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.*;
import org.bukkit.enchantments.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.MineStackGachaData;
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
		if(SeichiAssist.DEBUG){
			player.sendMessage(ChatColor.RED + "pick:" + itemstack.toString());
			player.sendMessage(ChatColor.RED + "pickDurability:" + itemstack.getDurability());
		}
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


		int i=0;
		for(i=0; i<SeichiAssist.minestacklist.size(); i++){
			if(material.equals(SeichiAssist.minestacklist.get(i).getMaterial()) &&
				itemstack.getDurability() == SeichiAssist.minestacklist.get(i).getDurability()){
				//この時点でIDとサブIDが一致している
				if(!SeichiAssist.minestacklist.get(i).getNameloreflag() && (!itemstack.getItemMeta().hasLore() && !itemstack.getItemMeta().hasDisplayName() ) ){//名前と説明文が無いアイテム
					if (playerdata.level < config.getMineStacklevel(SeichiAssist.minestacklist.get(i).getLevel())) {
						//レベルを満たしていない
						return;
					} else {
						playerdata.minestack.addNum(i, amount);
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
							MineStackGachaData g = SeichiAssist.msgachadatalist.get(SeichiAssist.minestacklist.get(i).getGachatype());
							String name = playerdata.name; //プレイヤーのネームを見る
							//player.sendMessage("Debug A");
							if(g.probability<0.1){ //カタログギフト券を除く(名前があるアイテム)
								if(!Util.ItemStackContainsOwnerName(itemstack, name)){
									//所有者の名前が無ければreturn
									//player.sendMessage("Debug B");
									return;
								}
							}
							//ItemStack itemstack_temp = Util.ItemStackResetName(itemstack);//名前を消しておく

							//GachaData.
							if(!g.compareonly(itemstack)){ //この1行で対応可能？
								//player.sendMessage("Debug C");
								//gachadata.itemstack.isSimilar(itemstack)でスタックサイズ以外が一致しているか判定可能
								//continue; //アイテムの中身が違う
							} else { //中身が同じ場合のみここに入る
								//player.sendMessage("Debug D");
								if(playerdata.level < config.getMineStacklevel(SeichiAssist.minestacklist.get(i).getLevel())){
									//レベルを満たしていない
									return;
								} else {
									playerdata.minestack.addNum(i, amount);
									break;
								}
							}

						}
				}
			}
		}
		if(i==SeichiAssist.minestacklist.size()){
			return;
		}

		event.setCancelled(true);
		player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
		item.remove();
	}
}
