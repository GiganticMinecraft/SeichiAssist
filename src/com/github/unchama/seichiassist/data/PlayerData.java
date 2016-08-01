package com.github.unchama.seichiassist.data;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.unchama.seichiassist.Level;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.Sql;
import com.github.unchama.seichiassist.Util;




public class PlayerData {
	Sql sql = SeichiAssist.plugin.sql;
	Player player;
	String name;

	/*
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
	*/
	//持ってるポーションエフェクト全てを格納する．
	public List<EffectData> effectdatalist;
	/*
	//現在のプレイヤーレベル
	public int level;
	//詫び券をあげる数
	public int numofsorryforbug;
	//採掘用アクティブスキルのフラグ
	public boolean activemineflag;
*/

	public PlayerData(Player p){
		player = p;
		effectdatalist = new ArrayList<EffectData>();
	}


	public void updata() {
		String name = Util.getName(player);
		//破壊量データ(before)を設定
		sql.insert("minutebefore",MineBlock.calcMineBlock(player),name);
		sql.insert("halfbefore",MineBlock.calcMineBlock(player),name);
		//プレイヤーのランクを計算し取得
		Level.updata(player);
		//アクティブスキルの使用可否
	}
	public void giveSorryForBug(){
		String name = Util.getName(player);
		//詫び券の配布
		ItemStack skull = Util.getskull();
		int numofsorryforbug = sql.selectint("numofsorryforbug", name);
		if( numofsorryforbug != 0){
			skull.setAmount(numofsorryforbug);
			Util.dropItem(player,skull);
			player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
			player.sendMessage("不具合のお詫びとして"+numofsorryforbug+ "枚の" + ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "がドロップしました。");
		}
		sql.insert("numofsorryforbug", 0, name);
	}

}
