package com.github.unchama.seichiassist.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Statistic;
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
	//スキル発動中だけtrueになるフラグ
	public boolean skillflag;
	//MineStack
	public MineStack minestack;
	//MineStackFlag
	public boolean minestackflag;
	//プレイ時間差分計算用int
	public int servertick;
	//プレイ時間
	public int playtick;
	//キルログ表示トグル
	public boolean dispkilllogflag;
	//PvPトグル
	public boolean pvpflag;
	//現在座標
	public Location loc;
	//放置時間
	public int idletime;

	//トータル破壊ブロック
	public int totalbreaknum;
	//各統計値差分計算用配列
	private List<Integer> staticdata;



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
		inventory = SeichiAssist.plugin.getServer().createInventory(null, 9*1 ,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "4次元ポケット");
		activenum = 1;
		skillcanbreakflag = true;
		rgnum = 0;
		skillflag = false;
		minestack = new MineStack();
		minestackflag = true;
		servertick = player.getStatistic(org.bukkit.Statistic.PLAY_ONE_TICK);
		playtick = 0;
		dispkilllogflag = false;
		pvpflag = false;
		loc = null;
		idletime = 0;
		staticdata = new ArrayList<Integer>();

		totalbreaknum = 0;
		for(Material m : SeichiAssist.materiallist){
			staticdata.add(player.getStatistic(Statistic.MINE_BLOCK, m));
		}

	}

	//join時とonenable時、プレイヤーデータを最新の状態に更新
	public void UpdateonJoin(Player player) {
		//破壊量データ(before)を設定
		minuteblock.before = totalbreaknum;
		halfhourblock.before = totalbreaknum;
		levelupdata(player);
		NotifySorryForBug(player);
	}

	//quit時とondisable時、プレイヤーデータを最新の状態に更新
	public void UpdateonQuit(Player player){
		//総整地量を更新
		calcMineBlock(player);
		//総プレイ時間更新
		calcPlayTick(player);
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
			player.sendMessage(ChatColor.GREEN + "運営チームから"+count+ "枚の" + ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "を受け取りました");
		}
	}

	//詫びガチャの通知
	public void NotifySorryForBug(Player player){
		if(numofsorryforbug > 0){
			player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
			player.sendMessage(ChatColor.GREEN + "運営チームから"+numofsorryforbug+ "枚の" + ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "が届いています！\n木の棒メニューから受け取ってください。");
		}
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



	//オフラインかどうか
	public boolean isOffline() {
		return SeichiAssist.plugin.getServer().getPlayer(uuid) == null;
	}


	//レベルを更新
	public void levelupdata(Player p) {
		calcPlayerLevel(p);
		setDisplayName(p);
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
			if(idletime >= 10){
				displayname = ChatColor.DARK_GRAY + "<管理人>" + name;
			}else if(idletime >= 3){
				displayname = ChatColor.GRAY + "<管理人>" + name;
			}else{
				displayname = ChatColor.RED + "<管理人>" + name;
			}
		}
		displayname =  "[ Lv" + level + " ]" + displayname + ChatColor.WHITE;

		if(idletime >= 10){
			displayname = ChatColor.DARK_GRAY + displayname;
		}else if(idletime >= 3){
			displayname = ChatColor.GRAY + displayname;
		}

		p.setDisplayName(displayname);
		p.setPlayerListName(displayname);
	}


	//プレイヤーレベルを計算し、更新する。
	private void calcPlayerLevel(Player p){
		//現在のランクの次を取得
		int i = level;
		//ランクが上がらなくなるまで処理
		while(SeichiAssist.levellist.get(i).intValue() <= totalbreaknum && i <= SeichiAssist.levellist.size()){

			//レベルアップ時のメッセージ
			p.sendMessage(ChatColor.GOLD+"ﾑﾑｯwwwwwwwﾚﾍﾞﾙｱｯﾌﾟwwwwwww【Lv("+(i)+")→Lv("+(i+1)+")】");
			//レベルアップ時の花火の打ち上げ
			Location loc = p.getLocation();
			Util.launchFireWorks(loc);
			String lvmessage = SeichiAssist.config.getLvMessage(i+1);
			if(!(lvmessage.isEmpty())){
				p.sendMessage(ChatColor.AQUA+lvmessage);
			}

			i++;
		}
		level = i;
	}

	//総プレイ時間を更新する
	public void calcPlayTick(Player p){
		int getservertick = p.getStatistic(org.bukkit.Statistic.PLAY_ONE_TICK);
		int getincrease = getservertick - servertick;
		servertick = getservertick;
		if(SeichiAssist.DEBUG){
			p.sendMessage("総プレイ時間に追加したtick:" + getincrease);
		}
		playtick += getincrease;
	}

	//総破壊ブロック数を更新する
	public void calcMineBlock(Player p){
		int i = 0;
		double sum = 0.0;
		for(Material m : SeichiAssist.materiallist){
			int getstat = p.getStatistic(Statistic.MINE_BLOCK, m);
			int getincrease = getstat - staticdata.get(i);
			sum += calcBlockExp(m,getincrease);
			if(SeichiAssist.DEBUG){
				p.sendMessage("calcの値:" + calcBlockExp(m,getincrease) + "(" + m + ")");
			}
			staticdata.set(i, getstat);
			i++;
		}
		//double値を四捨五入
		int x = (int)( sum < 0.0 ? sum-0.5 : sum+0.5 );
		if(SeichiAssist.DEBUG){
			p.sendMessage("整地量に追加した値:" + x);
		}
		totalbreaknum += x;
	}

	//ブロック別整地数反映量の調節
	private double calcBlockExp(Material m,int i){
		double result = (double)i;
		//ブロック別重み分け
		switch(m){
		case DIRT:
			//DIRTとGRASSは二重カウントされているので半分に
			result *= 0.5;
			break;
		case GRASS:
			//DIRTとGRASSは二重カウントされているので半分に
			result *= 0.5;
			break;
		default:
			break;
		}
		return result;
	}

	//現在の採掘量順位を表示する
	public int calcPlayerRank(Player p){
		//ランク用関数
		int i = 0;
		int t = totalbreaknum;
		RankData rankdata = SeichiAssist.ranklist.get(i);
		//ランクが上がらなくなるまで処理
		while(rankdata.totalbreaknum > t){
			i++;
			rankdata = SeichiAssist.ranklist.get(i);
		}
		return i+1;
	}

	//パッシブスキルの獲得量表示
	public int dispPassiveExp() {
		if(level < 8){
			return 0;
		}else if (level < 18){
			return SeichiAssist.config.getDropExplevel(1);
		}else if (level < 28){
			return SeichiAssist.config.getDropExplevel(2);
		}else if (level < 38){
			return SeichiAssist.config.getDropExplevel(3);
		}else if (level < 48){
			return SeichiAssist.config.getDropExplevel(4);
		}else if (level < 58){
			return SeichiAssist.config.getDropExplevel(5);
		}else if (level < 68){
			return SeichiAssist.config.getDropExplevel(6);
		}else if (level < 78){
			return SeichiAssist.config.getDropExplevel(7);
		}else if (level < 88){
			return SeichiAssist.config.getDropExplevel(8);
		}else if (level < 98){
			return SeichiAssist.config.getDropExplevel(9);
		}else{
			return SeichiAssist.config.getDropExplevel(10);
		}
	}
	//四次元ポケットのサイズを取得
	public int getPocketSize() {
		if (level < 26){
			return 9*3;
		}else if (level < 36){
			return 9*3;
		}else if (level < 46){
			return 9*3;
		}else if (level < 56){
			return 9*4;
		}else if (level < 66){
			return 9*5;
		}else{
			return 9*6;
		}
	}
}
