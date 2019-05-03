package com.github.unchama.seichiassist.listener;

import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.MineStackGachaData;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.minestack.MineStackObj;
import com.github.unchama.seichiassist.util.Util;
import org.bukkit.ChatColor;
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

import java.util.HashMap;
import java.util.UUID;

public class PlayerPickupItemListener implements Listener {
	SeichiAssist plugin = SeichiAssist.instance;
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

		int i;
		for(i=0; i<SeichiAssist.minestacklist.size(); i++){
			final MineStackObj mineStackObj = SeichiAssist.minestacklist.get(i);
			if(material.equals(mineStackObj.getMaterial()) &&
				itemstack.getDurability() == mineStackObj.getDurability()){
				//この時点でIDとサブIDが一致している
				if(!mineStackObj.getNameloreflag() && (!itemstack.getItemMeta().hasLore() && !itemstack.getItemMeta().hasDisplayName() ) ){//名前と説明文が無いアイテム
					if (playerdata.level < config.getMineStacklevel(mineStackObj.getLevel())) {
						//レベルを満たしていない
						return;
					} else {
						playerdata.minestack.addStackedAmountOf(mineStackObj, amount);
						break;
					}
				} else if(mineStackObj.getNameloreflag()==true && itemstack.getItemMeta().hasDisplayName() && itemstack.getItemMeta().hasLore()){
					//名前・説明文付き
					ItemMeta meta = itemstack.getItemMeta();
					//この時点で名前と説明文がある
						if(mineStackObj.getGachatype()==-1){ //ガチャ以外のアイテム(がちゃりんご)
							if( !(meta.getDisplayName().equals(Util.getGachaRingoName()))
								|| !(meta.getLore().equals(Util.getGachaRingoLore())) ){
								return;
							}
							if(playerdata.level < config.getMineStacklevel(mineStackObj.getLevel())){
								//レベルを満たしていない
								return;
							} else {
								playerdata.minestack.addStackedAmountOf(mineStackObj, amount);
								break;
							}
						} else {
							//ガチャ品
							MineStackGachaData g = SeichiAssist.msgachadatalist.get(mineStackObj.getGachatype());
							String name = playerdata.name; //プレイヤーのネームを見る
							if(g.probability<0.1){ //カタログギフト券を除く(名前があるアイテム)
								if(!Util.ItemStackContainsOwnerName(itemstack, name)){
									//所有者の名前が無ければreturn
									return;
								}
							}

							//GachaData.
							if (g.compareonly(itemstack)) { //中身が同じ場合のみここに入る
								//player.sendMessage("Debug D");
								if(playerdata.level < config.getMineStacklevel(mineStackObj.getLevel())){
									//レベルを満たしていない
									return;
								} else {
									playerdata.minestack.addStackedAmountOf(mineStackObj, amount);
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
