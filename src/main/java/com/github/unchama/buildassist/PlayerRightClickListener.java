package com.github.unchama.buildassist;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import com.github.unchama.seichiassist.MineStackObjectList;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.minestack.MineStackObj;

public class PlayerRightClickListener implements Listener  {
	HashMap<UUID, PlayerData> playermap = BuildAssist.playermap;

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerMenuUIEvent(final PlayerInteractEvent event){
		//プレイヤーを取得
		final Player player = event.getPlayer();
		//UUID取得
		final UUID uuid = player.getUniqueId();
		//ワールドデータを取得
		final World playerworld = player.getWorld();
		//プレイヤーが起こしたアクションを取得
		final Action action = event.getAction();
		//アクションを起こした手を取得
		final EquipmentSlot equipmentslot = event.getHand();
		//プレイヤーデータ
		final PlayerData playerdata = BuildAssist.playermap.get(uuid);
		final com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);

		//プレイヤーデータが無い場合は処理終了
		if(playerdata == null){
			return;
		}

		if(action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK){
			//左クリックの処理
			if(player.getInventory().getItemInMainHand().getType() == Material.STICK){
				//メインハンドに棒を持っているときの処理

				//オフハンドのアクション実行時処理を終了
				if(equipmentslot == EquipmentSlot.OFF_HAND){
					return;
				}
				//開く音を再生
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1.0f, 0.1f);
				player.openInventory(MenuInventoryData.getMenuData(player));
			}else if(player.isSneaking()){

				//プレイヤーインベントリを取得
				final PlayerInventory inventory = player.getInventory();
				final ItemStack offhanditem = inventory.getItemInOffHand();

				//オフハンドにブロックがあるか
				final boolean offhandtoolflag = BuildAssist.materiallist.contains(offhanditem.getType());


				//場合分け
				if(offhandtoolflag){
					//スキルフラグON以外のときは終了
					if(!playerdata.ZoneSetSkillFlag){
						return;
					}
					//オフハンドの時

					//Location playerloc = player.getLocation();
					//Block block = player.getWorld().getBlockAt(playerloc.getBlockX(), playerloc.getBlockY() -1 , playerloc.getBlockZ());

					//プレイヤーの足の座標を取得
					final int playerlocx = player.getLocation().getBlockX();
					final int playerlocy = player.getLocation().getBlockY();
					final int playerlocz = player.getLocation().getBlockZ();

					/*Coordinate start,end;
					Block placelocblock;

					start = new Coordinate(-3,-4,-3);
					end = new Coordinate(3,4,3);


					for(int x = start.x ; x < end.x ; x++){
						for(int z = start.z ; z < end.z ; z++){
							for(int y = start.y ; y < end.y; y++){
								placelocblock = block.getRelative(x, y, z);

							}
						}
					}
					*/

					//スキルの範囲設定用
					final int AREAint = playerdata.AREAint ;
					final int SEARCHint = AREAint + 1 ;
					final int AREAintB = (AREAint * 2)+ 1 ;
					final int SEARCHintB = (SEARCHint * 2)+ 1;


					//同ブロック探索(7*6*7)の開始座標を計算
					int searchX = playerlocx - SEARCHint ;
					int searchY = playerlocy - 4 ;
					int searchZ = playerlocz - SEARCHint ;


					//同上(Y座標記録)
					int Y1 = 256 ;
					int Y2 = 256 ;

					//スキル発動条件を満たすか
					boolean SetReady = false ;

					//
					int block_cnt = 0;

					//MineStack No.用
					int no = -1;

					//オフハンドアイテムと、範囲内のブロックに一致する物があるかどうか判別
					//同じ物がない場合・同じ物が3か所以上のY軸で存在する場合→SetReady = false
					for(;searchY < playerlocy + 2 ;){
						if(offhanditem.getType() == player.getWorld().getBlockAt(searchX,searchY,searchZ).getType()&&
							offhanditem.getData().getData() == player.getWorld().getBlockAt(searchX,searchY,searchZ).getData()){

							if(Y1 == searchY || Y1 == 256){
								Y1 = searchY ;
								SetReady = true ;
							}else if(Y2 == searchY || Y2 == 256){
								Y2 = searchY ;
							}else {
								SetReady = false ;
								player.sendMessage(ChatColor.RED + "範囲内に「オフハンドと同じブロック」が多すぎます。(Y軸2つ分以内にして下さい)");
								break;
							}
						}
						searchX ++ ;

						if(searchX > playerlocx + SEARCHint){
						searchX = searchX - SEARCHintB ;
						searchZ ++ ;
							if(searchZ > playerlocz + SEARCHint){
								searchZ = searchZ - SEARCHintB ;
								searchY ++ ;
							}

						}
					}

					if(Y1 == 256){
						player.sendMessage(ChatColor.RED + "範囲内に「オフハンドと同じブロック」を設置してください。(基準になります)");
						SetReady = false ;
					}

					//上の処理で「スキル条件を満たしていない」と判断された場合、処理終了
					if(!SetReady){
						player.sendMessage(ChatColor.RED + "発動条件が満たされませんでした。");
					}

					if(SetReady){
				        //実際に範囲内にブロックを設置する処理
						//設置範囲の基準となる座標
						int setblockX = playerlocx - AREAint ;
						final int setblockY = Y1 ;
						int setblockZ = playerlocz - AREAint ;
						int setunder;

						int searchedInv = 9 ;

						ItemStack ItemInInv;
						int ItemInInvAmount = 0 ;

						final Location WGloc = new Location(playerworld,0,0,0)  ;


						for(;setblockZ < playerlocz + SEARCHint ;){
							//ブロック設置座標のブロック判別
							final Material m1 = player.getWorld().getBlockAt(setblockX,setblockY,setblockZ).getType();
							final Set<Material> s1 = EnumSet.of(
									Material.AIR, Material.SNOW, Material.LONG_GRASS, Material.DEAD_BUSH, Material.YELLOW_FLOWER,
									Material.RED_ROSE, Material.RED_MUSHROOM, Material.BROWN_MUSHROOM
									);
							if(s1.contains(m1)) {
								setunder = 1;
								if(playerdata.zsSkillDirtFlag){
                                    for(;setunder < 5;){
                                        //設置対象の[setunder]分の下のブロックが空気かどうか
                                        final Material m2 = player.getWorld().getBlockAt(setblockX,(setblockY - setunder),setblockZ).getType();
                                        final Set<Material> s2 = EnumSet.of(Material.AIR, Material.LAVA, Material.STATIONARY_LAVA, Material.WATER, Material.STATIONARY_WATER);
                                        if(s2.contains(m2)){
                                            WGloc.setX(setblockX);
                                            WGloc.setY(setblockY - setunder);
                                            WGloc.setZ(setblockZ);
                                            //他人の保護がかかっている場合は処理を終了
                                            if(!Util.getWorldGuard().canBuild(player, WGloc)){
                                                player.sendMessage(ChatColor.RED + "付近に誰かの保護がかかっているようです" ) ;
                                            }else {
                                                //保護のない場合、土を設置する処理
                                                player.getWorld().getBlockAt(setblockX,(setblockY - setunder),setblockZ).setType(Material.DIRT);
                                            }
                                        }
                                        setunder ++;
                                    }
								}

								//他人の保護がかかっている場合は処理を終了
								WGloc.setX(setblockX);
								WGloc.setY(setblockY);
								WGloc.setZ(setblockZ);
								if(!Util.getWorldGuard().canBuild(player, WGloc)){
									player.sendMessage(ChatColor.RED + "付近に誰かの保護がかかっているようです" ) ;
									break;
								}else {
									//ここでMineStackの処理。flagがtrueならInvに関係なしにここに持ってくる
									if(playerdata.zs_minestack_flag)minestack:{//label指定は基本的に禁じ手だが、今回は後付けなので使わせてもらう。(解読性向上のため、1箇所のみの利用)
										//no:設置するブロック・max:設置できる最大量
										final List<MineStackObj> i1 = MineStackObjectList.INSTANCE.getMinestacklist();
										no = IntStream.range(0, MineStackObjectList.INSTANCE.getMinestacklist().size())
												.filter(cnt ->
														offhanditem.getType() == i1.get(cnt).getMaterial() &&
														offhanditem.getData().getData() == i1.get(cnt).getDurability()
												)
												.findFirst()
												.orElse(no);
										if(no > 0){
											//設置するブロックがMineStackに登録済み
											//1引く
											final MineStackObj mineStackObj = MineStackObjectList.INSTANCE.getMinestacklist().get(no);
											if(playerdata_s.getMinestack().getStackedAmountOf(mineStackObj) > 0){
												//player.sendMessage("MineStackよりブロック消費");
												//player.sendMessage("MineStackブロック残量(前):" + playerdata_s.getMinestack().getNum(no));
												playerdata_s.getMinestack().subtractStackedAmountOf(mineStackObj, 1);
												//player.sendMessage("MineStackブロック残量(後):" + playerdata_s.getMinestack().getNum(no));

												//設置処理
												player.getWorld().getBlockAt(setblockX,setblockY,setblockZ).setType(offhanditem.getType());
												player.getWorld().getBlockAt(setblockX,setblockY,setblockZ).setData(offhanditem.getData().getData());

												//ブロックカウント
												block_cnt++;

												//あとの設定
												setblockX ++ ;

												if(setblockX > playerlocx + AREAint){
													setblockX = setblockX - AREAintB ;
													setblockZ ++ ;
												}
												continue;
											}else{
												//player.sendMessage("MineStackのブロックがありません。インベントリより消費します。");
												break minestack;//minestack処理はなかったことにして次のfor分に飛ぶ。(label:minestackだけから抜ける)
											}
										}
									}


								//インベントリの左上から一つずつ確認する。
								//※一度「該当アイテムなし」と判断したスロットは次回以降スキップする様に組んであるゾ
									for(; searchedInv < 36 ;){
										//該当スロットのアイテムデータ取得
										ItemInInv = player.getInventory().getItem(searchedInv) ;
                                        if (ItemInInv != null) {
                                            ItemInInvAmount = ItemInInv.getAmount();
                                        }
                                        //スロットのアイテムが空白だった場合の処理(エラー回避のため)
										if(ItemInInv == null ){
											//確認したスロットが空気だった場合に次スロットへ移動
											if(searchedInv == 35){
												searchedInv = 0 ;
											}else if(searchedInv == 8 ){
												searchedInv = 36 ;
												player.sendMessage(ChatColor.RED + "アイテムが不足しています！" ) ;
											}else {
												searchedInv ++ ;
											}
											//スロットアイテムがオフハンドと一致した場合
										}else if(ItemInInv.getType() == offhanditem.getType() ){
											//数量以外のデータ(各種メタ)が一致するかどうか検知(仮)
											final ItemStack ItemInInvCheck = ItemInInv ;
											final ItemStack offhandCheck = offhanditem ;
											ItemInInvCheck.setAmount(1);
											offhandCheck.setAmount(1);

											if(!(ItemInInvCheck.equals(offhandCheck))){
												if(searchedInv == 35){
													searchedInv = 0 ;
												}else if(searchedInv == 8 ){
													searchedInv = 36 ;
													player.sendMessage(ChatColor.RED + "アイテムが不足しています!" ) ;
												}else {
													searchedInv ++ ;
												}
											}else {
												//取得したインベントリデータから数量を1ひき、インベントリに反映する
												if(ItemInInvAmount == 1) {
													ItemInInv.setType(Material.AIR);
													ItemInInv.setAmount(1);
												}else{
													ItemInInv.setAmount(ItemInInvAmount - 1) ;
												}
												player.getInventory().setItem(searchedInv, ItemInInv);
												//ブロックを設置する
												player.getWorld().getBlockAt(setblockX,setblockY,setblockZ).setType(offhanditem.getType());
												player.getWorld().getBlockAt(setblockX,setblockY,setblockZ).setData(offhanditem.getData().getData());

												block_cnt++;	//設置数カウント
												break;

											}
										}else {
											//確認したスロットが違うアイテムだった場合に、次のスロットへと対象を移す
											if(searchedInv == 35){
												searchedInv = 0 ;
											}else if(searchedInv == 8) {
												searchedInv = 36 ;
												player.sendMessage(ChatColor.RED + "アイテムが不足しています!" ) ;
											}else {
												searchedInv ++;
											}
										}
									}
								}
							}
							if(searchedInv == 36) {
								break;
							}

							setblockX++;

							if(setblockX > playerlocx + AREAint) {
								setblockX = setblockX - AREAintB;
								setblockZ ++;
							}
						}
					}
					//終了ログがうるさいので無くす
					//player.sendMessage(ChatColor.RED + "敷き詰めスキル：処理終了" ) ;

					if(Util.inTrackedWorld(player)){
						Util.addBuild1MinAmount(player, new BigDecimal(block_cnt * BuildAssist.config.getBlockCountMag()));	//設置した数を足す
					}
                }
            }
		}
	}
}
