package com.github.unchama.seichiassist.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

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
	//public MineBlock minuteblock;
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
	//拡張インベントリ
	public Inventory inventory;
	//ワールドガード保護自動設定用
	public int rgnum;

	//MineStack
	//public MineStack minestack;
	
	public MineStack minestack = new MineStack();
	//MineStackFlag
	public boolean minestackflag;
	//プレイ時間差分計算用int
	public int servertick;
	//プレイ時間
	public int playtick;
	//キルログ表示トグル
	public boolean dispkilllogflag;

	//ワールドガード保護ログ表示トグル
	public boolean dispworldguardlogflag;
	//複数種類破壊トグル
	public boolean multipleidbreakflag;

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
	//特典受け取り済み投票数
	public int p_givenvote;
	//投票受け取りボタン連打防止用
	public boolean votecooldownflag;

	//アクティブスキル関連データ
	public ActiveSkillData activeskilldata;

	//ガチャボタン連打防止用
	public boolean gachacooldownflag;

	//サブのホームポイント
	private Location[] sub_home = new Location[SeichiAssist.config.getSubHomeMax()];

	public PlayerData(Player player){
		//初期値を設定
		this.name = Util.getName(player);
		this.uuid = player.getUniqueId();
		this.effectflag = true;
		this.messageflag = false;
		//this.minuteblock = new MineBlock();
		this.halfhourblock = new MineBlock();
		this.gachapoint = 0;
		this.lastgachapoint = 0;
		this.gachaflag = true;
		this.minespeedlv = 0;
		this.lastminespeedlv = 0;
		this.effectdatalist = new ArrayList<EffectData>();
		this.level = 1;
		this.numofsorryforbug = 0;
		this.inventory = SeichiAssist.plugin.getServer().createInventory(null, 9*1 ,ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "4次元ポケット");
		this.rgnum = 0;
		this.minestack = new MineStack();
		this.minestackflag = true;
		this.servertick = player.getStatistic(org.bukkit.Statistic.PLAY_ONE_TICK);
		this.playtick = 0;
		this.dispkilllogflag = false;
		this.dispworldguardlogflag = true;
		this.multipleidbreakflag = false;
		this.pvpflag = false;
		this.loc = null;
		this.idletime = 0;
		this.staticdata = new ArrayList<Integer>();
		this.totalbreaknum = 0;
		for(Material m : SeichiAssist.materiallist){
			staticdata.add(player.getStatistic(Statistic.MINE_BLOCK, m));
		}
		this.activeskilldata = new ActiveSkillData();
		this.p_givenvote = 0;
		this.votecooldownflag = true;
		this.gachacooldownflag = true;

		for (int x = 0 ; x < SeichiAssist.config.getSubHomeMax() ; x++){
//			this.sub_home[x] = new Location(null, 0, 0, 0);
			this.sub_home[x] = null;
		}

	}

	//join時とonenable時、プレイヤーデータを最新の状態に更新
	public void updateonJoin(Player player) {
		//破壊量データ(before)を設定
		//minuteblock.before = totalbreaknum;
		halfhourblock.before = totalbreaknum;
		updataLevel(player);
		NotifySorryForBug(player);
		activeskilldata.updateonJoin(player, level);
	}


	//quit時とondisable時、プレイヤーデータを最新の状態に更新
	public void updateonQuit(Player player){
		//総整地量を更新
		calcMineBlock(player);
		//総プレイ時間更新
		calcPlayTick(player);

		activeskilldata.updateonQuit(player);
	}

	/*
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
	*/

	//詫びガチャの通知
	public void NotifySorryForBug(Player player){
		if(numofsorryforbug > 0){
			player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
			player.sendMessage(ChatColor.GREEN + "運営チームから"+numofsorryforbug+ "枚の" + ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "が届いています！\n木の棒メニューから受け取ってください");
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
	public void updataLevel(Player p) {
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
		//現在のランクを取得
		int i = level;
		//既にレベル上限に達していたら終了
		if(i >= SeichiAssist.levellist.size()){
			return;
		}
		//ランクが上がらなくなるまで処理
		while(SeichiAssist.levellist.get(i).intValue() <= totalbreaknum && (i+1) <= SeichiAssist.levellist.size()){

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
			if(activeskilldata.mana.isloaded()){
				//マナ最大値の更新
				activeskilldata.mana.LevelUp(p, i);
			}
			//レベル上限に達したら終了
			if(i >= SeichiAssist.levellist.size()){
				break;
			}
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
	public int calcMineBlock(Player p){
		int i = 0;
		double sum = 0.0;
		for(Material m : SeichiAssist.materiallist){
			int getstat = p.getStatistic(Statistic.MINE_BLOCK, m);
			int getincrease = getstat - staticdata.get(i);
			sum += calcBlockExp(m,getincrease,p);
			if(SeichiAssist.DEBUG){
				p.sendMessage("calcの値:" + calcBlockExp(m,getincrease,p) + "(" + m + ")");
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
		return x;
	}

	//ブロック別整地数反映量の調節
	private double calcBlockExp(Material m,int i,Player p){
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

		case NETHERRACK:
			//ネザーラックの重み分け
			result *= 0.2;
			break;
		/*
		case ENDER_STONE:
			//エンドストーンの重み分け
			result *= 0.5;
			break;
		*/

		default:
			break;
		}
		//整地ワールド外では整地数が反映されない
		if(!Util.isGainSeichiExp(p)){
			result *= 0.0;
		}
		return result;
	}

	//現在の採掘量順位を表示する
	public int calcPlayerRank(Player p){
		//ランク用関数
		int i = 0;
		int t = totalbreaknum;
		if(SeichiAssist.ranklist.size() == 0){
			return 1;
		}
		RankData rankdata = SeichiAssist.ranklist.get(i);
		//ランクが上がらなくなるまで処理
		while(rankdata.totalbreaknum > t){
			i++;
			rankdata = SeichiAssist.ranklist.get(i);
		}
		return i+1;
	}

	//パッシブスキルの獲得量表示
	public double dispPassiveExp() {
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
		if (level < 6){
			return 9*3;
		}else if (level < 16){
			return 9*3;
		}else if (level < 26){
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



	//サブホームの位置をセットする
	public void SetSubHome(Location l,int x){
		if(x >= 0 & x < SeichiAssist.config.getSubHomeMax() ){
			this.sub_home[x] = l;
		}
	}

	//サブホームの位置を読み込む
	public Location GetSubHome(int x){
		if(x >= 0 & x < SeichiAssist.config.getSubHomeMax() ){
			return this.sub_home[x];
		}else{
			return null;
		}
	}

	//文字列からサブデータを読み込む（DB用）
	public void SetSubHome(String str){
		String[] s = str.split(",", -1);
		for( int x = 0 ; x < SeichiAssist.config.getSubHomeMax() ; x++){
			if (s.length < x*4+3){
				break;
			}
//			if(s[x*4] != "" && s[x*4+1] != "" && s[x*4+2] != "" && s[x*4+3] != ""){	//未設定項目を飛ばす　何故かうまく動かない
			if(s[x*4].length() > 0 && s[x*4+1].length() > 0 && s[x*4+2].length() > 0 && s[x*4+3].length() > 0 ){

				Location l = new Location( Bukkit.getWorld(s[x*4+3]) , Integer.parseInt(s[x*4]) , Integer.parseInt(s[x*4+1]) , Integer.parseInt(s[x*4+2]) );
				this.sub_home[x] = l;
			}
		}
	}

	//文字列からサブデータを読み込む・デバッグ版（DB用）
	public void SetSubHome(String str , Player player){
		String[] s = str.split(",", -1);
		player.sendMessage(str );
		player.sendMessage("配列数" + s.length );
		for( int x = 0 ; x < SeichiAssist.config.getSubHomeMax() ; x++){
			if (s.length < x*4+3){
				break;
			}
			player.sendMessage("x:" + s[x*4] + " y:" +s[x*4+1]+ " z:" +s[x*4+2]+ " w:"+s[x*4+3] );
//			if(s[x*4] != "" && s[x*4+1] != "" && s[x*4+2] != "" && s[x*4+3] != ""){
			if(s[x*4].length() > 0 && s[x*4+1].length() > 0 && s[x*4+2].length() > 0 && s[x*4+3].length() > 0 ){
				player.sendMessage("読み込み");
				Location l = new Location( Bukkit.getWorld(s[x*4+3]) , Integer.parseInt(s[x*4]) , Integer.parseInt(s[x*4+1]) , Integer.parseInt(s[x*4+2]) );
				this.sub_home[x] = l;
			}
		}
	}

	//サブホームデータを文字列で返す（DB用）
	public String SubHomeToString(){
		String s = "";
		for( int x = 0 ; x < SeichiAssist.config.getSubHomeMax() ; x++){
			if (this.sub_home[x] == null){
				//設定されてない場合
				s += ",,,,";
			}else{
				//設定されてる場合
				s += String.valueOf( (int)sub_home[x].getX() ) +",";
				s += String.valueOf( (int)sub_home[x].getY() ) +",";
				s += String.valueOf( (int)sub_home[x].getZ() ) +",";
				s += sub_home[x].getWorld().getName() +",";
			}
		}
		return s;
	}

}
