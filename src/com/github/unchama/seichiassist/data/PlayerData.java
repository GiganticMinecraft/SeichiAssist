package com.github.unchama.seichiassist.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.util.Level;
import com.github.unchama.seichiassist.util.Util;




public class PlayerData {
	//プレイヤー名
	public String name;
	//UUID
	public UUID uuid;
	//エフェクトのフラグ
	public boolean effectflag;
	//内訳メッセージを出すフラグ
	public boolean messageflag;
	//1分間のデータを保存するincrease:１分間の採掘量
	public MineBlock minuteblock;
	//３０分間のデータを保存する．
	public MineBlock halfhourblock;
	//ガチャの基準となるポイント
	public int gachapoint;
	//最後のガチャポイントデータ
	public int lastgachapoint;
	//今回の採掘速度上昇レベルを格納
	public int minespeedlv;
	//前回の採掘速度上昇レベルを格納
	public int lastminespeedlv;
	//持ってるポーションエフェクト全てを格納する．
	public List<EffectData> effectdatalist;
	//現在のプレイヤーレベル
	public int level;
	//詫び券をあげる数
	public int numofsorryforbug;
	//採掘用アクティブスキルのフラグ
	public boolean activemineflag;


	public PlayerData(Player player){
		//初期値を設定
		name = Util.getName(player);
		uuid = player.getUniqueId();
		effectflag = true;
		messageflag = false;
		minuteblock = new MineBlock();
		halfhourblock = new MineBlock();
		gachapoint = 0;
		lastgachapoint = 0;
		minespeedlv = 0;
		lastminespeedlv = 0;
		effectdatalist = new ArrayList<EffectData>();
		level = 1;
		numofsorryforbug = 0;
		activemineflag = false;
		//統計量を取得
		int mines = MineBlock.calcMineBlock(player);
		updata(player,mines);
		giveSorryForBug(player);
	}

	//プレイヤーデータを最新の状態に更新
	public void updata(Player player,int mines) {
		//破壊量データ(before)を設定
		minuteblock.before = mines;
		halfhourblock.before = mines;
		//プレイヤーのランクを計算し取得
		Level.updata(player,mines);
	}
	//詫び券の配布
	public void giveSorryForBug(Player player){
		ItemStack skull = Util.getskull();
		if( numofsorryforbug != 0){
			skull.setAmount(numofsorryforbug);
			Util.dropItem(player,skull);
			player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
			player.sendMessage(ChatColor.GREEN + "不具合のお詫びとして"+numofsorryforbug+ "枚の" + ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "がドロップしました。");
		}
		numofsorryforbug = 0;
	}

	//エフェクトデータのdurationを60秒引く
	public void calcEffectData() {
		//tmplistを作成
		List<EffectData> tmplist = new ArrayList<EffectData>();
		//effectdatalistのdurationをすべて60秒（1200tick）引いてtmplistに格納
		for(EffectData ed : effectdatalist){
			ed.duration -= 1200;
			tmplist.add(ed);
		}
		//tmplistのdurationが3秒以下（60tick）のものはeffectdatalistから削除
		for(EffectData ed : tmplist){
			if(ed.duration <= 60){
				effectdatalist.remove(ed);
			}
		}
	}

	//プレイヤーネームを更新
	public void renewName(Player new_player) {
		//現在のplayername を取得
				String now_name = Util.getName(new_player);
				//UUIDは同じだがplayernameが異なっているとき
				if(!now_name.equals(name)){
					name = now_name;
				}
	}

	public boolean isOffline() {
		return SeichiAssist.plugin.getServer().getPlayer(name) == null;
	}

}
