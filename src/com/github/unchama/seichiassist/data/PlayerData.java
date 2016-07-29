package com.github.unchama.seichiassist.data;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.unchama.seichiassist.Level;
import com.github.unchama.seichiassist.Util;




public class PlayerData {
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
	//持ってるポーションエフェクト全てを格納する．
	public List<EffectData> effectdatalist;
	//現在のプレイヤーレベル
	public int level;
	//詫び券をあげる数
	public int numofsorryforbug;
	//採掘用アクティブスキルのフラグ
	public boolean activemineflag;
	//採掘で出る経験値が増える確率
	public double dropexpprobability;


	public PlayerData(){
		effectflag = true;
		messageflag = false;
		minuteblock = new MineBlock();
		halfhourblock = new MineBlock();
		effectdatalist = new ArrayList<EffectData>();
		gachapoint = 0;
		lastgachapoint = 0;
		minespeedlv = 0;
		minuteblock.before = 0;
		halfhourblock.before = 0;
		level = 1;
		minespeedlv = 0;
		minuteblock.before = 0;
		halfhourblock.before = 0;
		numofsorryforbug = 0;
		activemineflag = false;
		dropexpprobability = 0;

	}


	public void updata(Player p) {
		//破壊量データ(before)を設定
		minuteblock.before = MineBlock.calcMineBlock(p);
		halfhourblock.before = MineBlock.calcMineBlock(p);
		//プレイヤーのランクを計算し取得
		Level.updata(p);
	}
	public void giveSorryForBug(Player p){
		//詫び券の配布
				ItemStack skull = Util.getskull();
				if(numofsorryforbug != 0){
					skull.setAmount(numofsorryforbug);
					Util.dropItem(p,skull);
					p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
					p.sendMessage("不具合のお詫びとして"+numofsorryforbug+ "枚の" + ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "が" +numofsorryforbug+ "ドロップしました。");
				}
				numofsorryforbug = 0;
	}

}
