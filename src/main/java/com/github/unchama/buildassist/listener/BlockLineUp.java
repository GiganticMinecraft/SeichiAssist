package com.github.unchama.buildassist.listener;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.github.unchama.buildassist.BuildAssist;
import com.github.unchama.buildassist.PlayerData;
import com.github.unchama.buildassist.Util;
import com.github.unchama.seichiassist.MineStackObjectList;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.github.unchama.buildassist.util.ExternalPlugins;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.minestack.MineStackObj;
import org.jetbrains.annotations.Nullable;

public class BlockLineUp implements Listener{
	@EventHandler
	public void onPlayerClick(final PlayerInteractEvent e){
		//プレイヤーを取得
		final Player player = e.getPlayer();
		//UUID取得
		final UUID uuid = player.getUniqueId();
		//プレイヤーが起こしたアクションを取得
		final Action action = e.getAction();
		//プレイヤーデータ
		final com.github.unchama.seichiassist.data.PlayerData playerdata_s = SeichiAssist.Companion.getPlayermap().get(uuid);
		final PlayerData playerdata = BuildAssist.Companion.getPlayermap().get(uuid);

		//プレイヤーデータが無い場合は処理終了
		if(playerdata == null){
			return;
		}

		//スキルOFFなら終了
		if(playerdata.line_up_flg == 0){
			return;
		}

		//スキル利用可能でないワールドの場合終了
		if(!Util.isSkillEnable(player)){
			return;
		}
		//左クリックの処理
		if(action.equals(Action.LEFT_CLICK_AIR) || action.equals(Action.LEFT_CLICK_BLOCK)){
			//プレイヤーインベントリを取得
			final PlayerInventory inventory = player.getInventory();
			//メインハンドとオフハンドを取得
			final ItemStack mainhanditem = inventory.getItemInMainHand();
			final ItemStack offhanditem = inventory.getItemInOffHand();

//			player.sendMessage(mainhanditem.getType().toString());
//			player.sendMessage(mainhanditem.getData().toString());
//			player.sendMessage(""+mainhanditem.getAmount());	//持ってる数

			//メインハンドにブロックがあるとき
			if(BuildAssist.Companion.getMateriallist2().contains(mainhanditem.getType()) || BuildAssist.Companion.getMaterial_slab2().contains(mainhanditem.getType())) {
				if(offhanditem.getType() != Material.STICK){//オフハンドに木の棒を持ってるときのみ発動する
					return;
				}

				final Location pl = player.getLocation();
				final Material m = mainhanditem.getType();
				byte d = mainhanditem.getData().getData();

				//仰角は下向きがプラスで上向きがマイナス
				//方角は南を0度として時計回りに360度、何故か偶にマイナスの値になる
				final float pitch = pl.getPitch();
				final float yaw = (pl.getYaw() + 360) % 360;
//				player.sendMessage("方角：" + Float.toString(yaw) + "　仰角：" + Float.toString(pitch));
//				player.sendMessage("マナ:" + playerdata_s.activeskilldata.mana.getMana() );
				int step_x = 0;
				int step_y = 0;
				int step_z = 0;
				//プレイヤーの足の座標を取得
				int px = pl.getBlockX();
//				int py = pl.getBlockY()+1;
				int py = (int)(pl.getY() + 1.6);
				int pz = pl.getBlockZ();
				int no = -1;		//マインスタックのNo.
				int double_mag = 1;//ハーフブロック重ね起きしたときフラグ
				//プレイヤーの向いてる方向を判定
				if (pitch > 45 ){//下
					step_y = -1;
//					py--;
					py = pl.getBlockY();
				}else if (pitch < -45 ){//上
					step_y = 1;
				}else{
					if(playerdata.line_up_flg == 2){//下設置設定の場合は一段下げる
						py--;
					}
					if (yaw > 315 || yaw < 45 ){//南
						step_z = 1;
					}else if(yaw < 135 ){//西
						step_x = -1;
					}else if(yaw < 225 ){//北
						step_z = -1;
					}else{//東
						step_x = 1;
					}
				}
				final double mana_mag = BuildAssist.Companion.getConfig().getblocklineupmana_mag();

				int max = mainhanditem.getAmount();//メインハンドのアイテム数を最大値に
				//マインスタック優先の場合最大値をマインスタックの数を足す
				if( playerdata.line_up_minestack_flg == 1 ) {
					final List<MineStackObj> t = MineStackObjectList.INSTANCE.getMinestacklist();
					for(int cnt = 0; cnt < t.size() ; cnt++){
						final MineStackObj mineStackObj = MineStackObjectList.INSTANCE.getMinestacklist().get(cnt);
						if(m == t.get(cnt).getMaterial() && d == t.get(cnt).getDurability()){
							max += playerdata_s.getMinestack().getStackedAmountOf(mineStackObj);
							no = cnt;
//							player.sendMessage("マインスタックNo.：" + no + "　max：" + max);
							break;
						}

					}
					/*
					//石ハーフ
					if (m == Material.STEP && d == 0){
						max += playerdata_s.minestack.getNum(Util.MineStackobjname_indexOf("step0"));
					}
					*/
				}
				//マナが途中で足りなくなる場合はマナの最大にする
				if ( playerdata_s.getActiveskilldata().mana.getMana()- (max) * mana_mag < 0.0 ){
					max = (int) (playerdata_s.getActiveskilldata().mana.getMana()/ mana_mag);
				}

				//手に持ってるのがハーフブロックの場合
				Material m2 = null;
				if(BuildAssist.Companion.getMaterial_slab2().contains(m)){
					if(playerdata.line_up_step_flg == 0) {
						d += 8;	//上設置設定の場合は上側のデータに書き換え
					} else if(playerdata.line_up_step_flg == 2) {
						final Map<Material, Material> mapping = new EnumMap<>(Material.class);
						mapping.put(Material.STONE_SLAB2, Material.DOUBLE_STONE_SLAB2);
						mapping.put(Material.PURPUR_SLAB, Material.PURPUR_DOUBLE_SLAB);
						mapping.put(Material.WOOD_STEP, Material.WOOD_DOUBLE_STEP);
						mapping.put(Material.STEP, Material.DOUBLE_STEP);
						final @Nullable Material m1 = mapping.get(m);
						m2 = m1 != null ? m1 : m;
						max /= 2;
						double_mag = 2;
					}
				}
//				player.sendMessage("max:" + max );
				//ループ数を64に制限
				if( max > 64 ){
					max = 64;
				}
				int v;
				for(v = 0 ; v < max ; v++) {//設置ループ
					px += step_x;
					py += step_y;
					pz += step_z;
					final Block b = pl.getWorld().getBlockAt(px , py , pz );

					//空気以外にぶつかったら設置終わり
					if (b.getType() != Material.AIR){
						if(!BuildAssist.Companion.getMaterial_destruction().contains(b.getType()) || playerdata.line_up_des_flg == 0){
							break;
						}
						final Collection<ItemStack> i = b.getDrops();

						if(i.iterator().hasNext()){
							b.getLocation().getWorld().dropItemNaturally(pl, i.iterator().next());
						}
					}

					//他人の保護がかかっている場合は設置終わり
					if(!ExternalPlugins.getWorldGuard().canBuild(player, b.getLocation())){
						break;
					}

					if (m2 != null) {
						pl.getWorld().getBlockAt(px , py , pz ).setType(m2);
					}
					pl.getWorld().getBlockAt(px , py , pz ).setData(d);		//ブロックのデータを設定

				}
				v *= double_mag;	//ハーフ2段重ねの場合は2倍
				//カウント対象ワールドの場合カウント値を足す
				if(Util.inTrackedWorld(player)){	//対象ワールドかチェック
					Util.addBuild1MinAmount(player, new BigDecimal(v * BuildAssist.Companion.getConfig().getBlockCountMag()));	//設置した数を足す
				}

				//マインスタック優先の場合マインスタックの数を減らす
				if( playerdata.line_up_minestack_flg == 1 && no > -1){
					final MineStackObj mineStackObj = MineStackObjectList.INSTANCE.getMinestacklist().get(no);


					//設置した数vを再計算(下のメインハンドの処理に使用する為)
					/*
					 * TODO 変数vの意味が以下の様に変わっているので可読性が宜しくない
					 * (設置した数 -> 設置した数のうち、MineStack上で足りなかったブロック数)
					 */
					final long num = playerdata_s.getMinestack().getStackedAmountOf(mineStackObj) - v;
					if( num < 0 ){ // minestack上の残数では足りない場合
						//minestackは0にする
						playerdata_s.getMinestack().subtractStackedAmountOf
							(mineStackObj , playerdata_s.getMinestack().getStackedAmountOf(mineStackObj));

						//minestack不足分をvへ代入
						v = (int)num * (-1);

					}else{ // minestack上の残数で足りる場合
						//minestack上から設置した数分引く
						playerdata_s.getMinestack().subtractStackedAmountOf(mineStackObj , v);

						//足りなかったブロックは0なのでvには0を代入
						v = 0;
					}
				}

				//アイテム数が0ならメインハンドのアイテムをクリア
				if (mainhanditem.getAmount() - v <= 0 ){
					inventory.setItemInMainHand(new ItemStack(Material.AIR,-1));//アイテム数が0になっても消えないので自前で消す
				}else{	//0じゃないなら設置した分を引く
					mainhanditem.setAmount(mainhanditem.getAmount() - v );

				}
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_PLACE, 1, 1);
			}
		}
	}
}
