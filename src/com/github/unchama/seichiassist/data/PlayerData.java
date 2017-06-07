package com.github.unchama.seichiassist.data;

import java.util.ArrayList;
import java.util.BitSet;
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
import com.github.unchama.seichiassist.task.MebiusTaskRunnable;
import com.github.unchama.seichiassist.util.ExperienceManager;
import com.github.unchama.seichiassist.util.Util;




public class PlayerData {
	//読み込み済みフラグ
	public boolean loaded = false;
	//プレイヤー名
	public String name;
	//UUID
	public UUID uuid;
	//エフェクトのフラグ
	public int effectflag;
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
	//全体通知音消音トグル
	public boolean everysoundflag;
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
	public long totalbreaknum;
	//整地量バー
	public ExpBar expbar;
	//合計経験値
	public int totalexp;
	//経験値マネージャ
	public ExperienceManager expmanager;
	//合計経験値統合済みフラグ
	public byte expmarge;
	//各統計値差分計算用配列
	private List<Integer> staticdata;
	//特典受け取り済み投票数
	public int p_givenvote;
	//投票受け取りボタン連打防止用
	public boolean votecooldownflag;

	//連続・通算ログイン用
	public String lastcheckdate ;
	public int ChainJoin ;
	public int TotalJoin ;

	//アクティブスキル関連データ
	public ActiveSkillData activeskilldata;

	//MebiusTask
	public MebiusTaskRunnable mebius;

	//ガチャボタン連打防止用
	public boolean gachacooldownflag;

	//インベントリ共有トグル
	public boolean shareinv;
	//インベントリ共有ボタン連打防止用
	public boolean shareinvcooldownflag;

	//サブのホームポイント
	private Location[] sub_home = new Location[SeichiAssist.config.getSubHomeMax()];

	//LV・二つ名表示切替用
	public boolean displayTypeLv;
	//表示二つ名の指定用
	public int displayTitle1No;
	public int displayTitle2No;
	public int displayTitle3No;
	//二つ名解禁フラグ保存用
	public BitSet TitleFlags;
	//二つ名関連用にp_vote(投票数)を引っ張る。(予期せぬエラー回避のため名前を複雑化)
	public int p_vote_forT ;
	//二つ名配布予約NOの保存
	public int giveachvNo;
	//実績ポイント用
	public int achvPointMAX ;//累計取得量
	public int achvPointUSE ;//消費量
	public int achvPoint ;//現在の残量
	public int achvChangenum ;//投票ptからの変換回数
	public boolean samepageflag ;//実績ショップ用


	//建築LV
	private int build_lv;
	//設置ブロック数
	private int build_count;
	//設置ブロックサーバー統合フラグ
	private byte build_count_flg;

	// 1周年記念
	public boolean anniversary;

	public PlayerData(Player player){
		//初期値を設定
		this.loaded = false;
		this.name = Util.getName(player);
		this.uuid = player.getUniqueId();
		this.effectflag = 0;
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
		this.mebius = new MebiusTaskRunnable(this);
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
		this.expbar = new ExpBar(this, player);
		this.expmanager = new ExperienceManager(player);
		this.p_givenvote = 0;
		this.votecooldownflag = true;
		this.gachacooldownflag = true;
		this.shareinvcooldownflag = true;

		this.displayTypeLv = true;
		this.displayTitle1No = 0 ;
		this.displayTitle2No = 0 ;
		this.displayTitle3No = 0 ;
		this.TitleFlags = new BitSet(10000);
		this.TitleFlags.set(1);
		this.p_vote_forT = 0 ;
		this.giveachvNo = 0 ;


		for (int x = 0 ; x < SeichiAssist.config.getSubHomeMax() ; x++){
//			this.sub_home[x] = new Location(null, 0, 0, 0);
			this.sub_home[x] = null;
		}
		this.build_lv = 1;
		this.build_count = 0;
		this.build_count_flg = 0;
		this.anniversary = false;
	}

	//join時とonenable時、プレイヤーデータを最新の状態に更新
	public void updateonJoin(Player player) {
		//破壊量データ(before)を設定
		//minuteblock.before = totalbreaknum;
		halfhourblock.before = totalbreaknum;
		updataLevel(player);
		NotifySorryForBug(player);
		activeskilldata.updateonJoin(player, level);
		//サーバー保管経験値をクライアントに読み込み
		loadTotalExp();
	}


	//quit時とondisable時、プレイヤーデータを最新の状態に更新
	public void updateonQuit(Player player){
		//総整地量を更新
		calcMineBlock(player);
		//総プレイ時間更新
		calcPlayTick(player);

		activeskilldata.updateonQuit(player);
		expbar.remove();
		//クライアント経験値をサーバー保管
		saveTotalExp();
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
		expbar.calculate();
	}


	//プレイヤーのレベルを指定された値に設定
	public void setLevel(int _level) {
		level = _level;
	}

	//プレイヤーのレベルからレベルと総整地量を指定された値に設定
	/**
	 * @param _level
	 * レベル
	 *
	 * ※レベルと総整地量を変更します(取扱注意)
	 */
	public void setLevelandTotalbreaknum(int _level) {
		level = _level;
		totalbreaknum = SeichiAssist.levellist.get(_level-1);
	}


	//表示される名前に整地レベルor二つ名を追加
	public void setDisplayName(Player p) {
		String displayname = Util.getName(p);

		//表示を追加する処理
		if(displayTitle1No == 0 && displayTitle2No == 0 && displayTitle3No == 0){
			displayname =  "[ Lv" + level + " ]" + displayname + ChatColor.WHITE;
		} else {
			String displayTitle1 = SeichiAssist.config.getTitle1(displayTitle1No);
			String displayTitle2 = SeichiAssist.config.getTitle2(displayTitle2No);
			String displayTitle3 = SeichiAssist.config.getTitle3(displayTitle3No);
			displayname =  "[" + displayTitle1 + displayTitle2 + displayTitle3 + "]" + displayname + ChatColor.WHITE;
		}
		//放置時に色を変える
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
			result *= 1.0;
			break;

		case ENDER_STONE:
			//エンドストーンの重み分け
			result *= 1.0;
			break;


		default:
			break;
		}

		if(!Util.isSeichiWorld(p)){
			//整地ワールド外では整地数が反映されない
			result *= 0.0;
		}else if(p.getWorld().getName().equalsIgnoreCase("world_sw_zero")){
			//整地ワールドzeroでは整地量2.0倍
			result *= 2.0;
		}
		return result;
	}

	//現在の採掘量順位を表示する
	public int calcPlayerRank(Player p){
		//ランク用関数
		int i = 0;
		long t = totalbreaknum;
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

	//サブホームデータを文字列で返す（DB用）
	public String SubHomeToString(){
		String s = "";
		for( int x = 0 ; x < SeichiAssist.config.getSubHomeMax() ; x++){
			if (this.sub_home[x] == null || sub_home[x].getWorld() == null){
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

	public void build_count_flg_set(byte x){
		build_count_flg = x;
	}
	public byte build_count_flg_get(){
		return build_count_flg;
	}
	public void build_lv_set(int lv){
		build_lv = lv;
	}
	public int build_lv_get(){
		return build_lv;
	}
	public void build_count_set(int count){
		build_count = count;
	}
	public int build_count_get(){
		return build_count;
	}

	private void saveTotalExp() {
		totalexp = expmanager.getCurrentExp();
	}

	private void loadTotalExp() {
		int server_num = SeichiAssist.config.getServerNum();
		//経験値が統合されてない場合は統合する
		if (expmarge != 0x07 && server_num >= 1 && server_num <= 3) {
			if ((expmarge & (0x01 << (server_num - 1))) == 0 ) {
				if(expmarge == 0) {
					// 初回は加算じゃなくベースとして代入にする
					totalexp = expmanager.getCurrentExp();
				} else {
					totalexp += expmanager.getCurrentExp();
				}
				expmarge = (byte) (expmarge | (0x01 << (server_num - 1)));
			}
		}
		expmanager.setExp(totalexp);
	}
}
