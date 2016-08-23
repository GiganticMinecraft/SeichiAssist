package com.github.unchama.seichiassist.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.unchama.seichiassist.SeichiAssist;
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
	//ガチャ受け取りフラグ
	public boolean gachaflag;
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
	//採掘用アクティブスキルのフラグ 0:なし 1:上破壊 2:下破壊
	public int activemineflagnum;
	//拡張インベントリ
	public Inventory inventory;
	//アクティブスキル番号を格納
	public int activenum;
	//スキルクールダウン用フラグ
	public boolean skillcanbreakflag;
	//ワールドガード保護自動設定用
	public int rgnum;
	//ランキング算出用トータル破壊ブロック
	public int totalbreaknum;
	//スキル発動中だけtrueになるフラグ
	public boolean skillflag;


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
		gachaflag = true;
		minespeedlv = 0;
		lastminespeedlv = 0;
		effectdatalist = new ArrayList<EffectData>();
		level = 1;
		numofsorryforbug = 0;
		activemineflagnum = 0;
		inventory = SeichiAssist.plugin.getServer().createInventory(null, 9*3 ,"拡張インベントリ");
		activenum = 1;
		skillcanbreakflag = true;
		rgnum = 0;
		totalbreaknum = MineBlock.calcMineBlock(player);
		skillflag = false;
	}

	//プレイヤーデータを最新の状態に更新
	public void updata(Player player,int mines) {
		//破壊量データ(before)を設定
		minuteblock.before = mines;
		halfhourblock.before = mines;
		levelupdata(player,mines);
	}
	//詫び券の配布
	public void giveSorryForBug(Player player){
		ItemStack skull = Util.getskull(Util.getName(player));
		int count = 0;
		while(numofsorryforbug >= 1){
			numofsorryforbug -= 1;
			if(player.getInventory().contains(skull) || !Util.isPlayerInventryFill(player)){
				Util.addItem(player,skull);
			}else{
				Util.dropItem(player,skull);
			}
			count++;
		}
		//詫びガチャ関数初期化
		numofsorryforbug = 0;

		if(count > 0){
			player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
			player.sendMessage(ChatColor.GREEN + "不具合のお詫びとして"+count+ "枚の" + ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "がドロップしました");
		}


		/*
		String name = Util.getName(player);
		ItemStack skull = Util.getskull(name);
		if( numofsorryforbug != 0){
			skull.setAmount(numofsorryforbug);
			Util.dropItem(player,skull);
			player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
			player.sendMessage(ChatColor.GREEN + "不具合のお詫びとして"+numofsorryforbug+ "枚の" + ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "がドロップしました");
		}
		numofsorryforbug = 0;
		*/
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


	//オフラインかどうか
	public boolean isOffline() {
		return SeichiAssist.plugin.getServer().getPlayer(name) == null;
	}


	//レベルを更新
	public void levelupdata(Player player,int mines) {
		calcPlayerLevel(player,mines);
		setDisplayName(player);
	}


	//プレイヤーのレベルを指定された値に設定
	public void setLevel(int _level) {
		level = _level;
	}


	//表示される名前に整地レベルを追加
	public void setDisplayName(Player p) {
		String displayname = Util.getName(p);
		if(p.isOp()){
			//管理人の場合
			displayname = ChatColor.RED + "<管理人>" + name;
		}
		displayname =  "[ Lv" + level + " ]" + displayname + ChatColor.WHITE;

		p.setDisplayName(displayname);
		p.setPlayerListName(displayname);
	}


	//プレイヤーレベルを計算し、更新する。
	private void calcPlayerLevel(Player player,int mines){
		//現在のランクの次を取得
		int i = level + 1;
		//ランクが上がらなくなるまで処理
		while(SeichiAssist.levellist.get(i).intValue() <= mines && i <= SeichiAssist.levellist.size()){
			if(!SeichiAssist.DEBUG){
				//レベルアップ時のメッセージ
				player.sendMessage(ChatColor.GOLD+"ﾑﾑｯwwwwwwwﾚﾍﾞﾙｱｯﾌﾟwwwwwww【Lv("+(i-1)+")→Lv("+i+")】");
				//レベルアップ時の花火の打ち上げ
				Location loc = player.getLocation();
				Util.launchFireWorks(loc);
				String lvmessage = SeichiAssist.config.getLvMessage(i);
				if(!(lvmessage.isEmpty())){
					player.sendMessage(ChatColor.AQUA+lvmessage);
				}
			}
			i++;
		}
		level = i-1;
	}

	//現在の採掘量順位を表示する
	public static int calcPlayerRank(Player player){
		//ランク用関数
		int i = 0;
		int t = MineBlock.calcMineBlock(player);
		//ランクが上がらなくなるまで処理
		while(SeichiAssist.ranklist.get(i).intValue() > t){
			i++;
		}
		return i+1;
	}

}
