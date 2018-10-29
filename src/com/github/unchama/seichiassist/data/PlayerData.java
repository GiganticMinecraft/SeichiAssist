package com.github.unchama.seichiassist.data;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.event.SeichiLevelUpEvent;
import com.github.unchama.seichiassist.minestack.MineStackHistoryData;
import com.github.unchama.seichiassist.minestack.MineStackObj;
import com.github.unchama.seichiassist.task.MebiusTaskRunnable;
import com.github.unchama.seichiassist.util.ExperienceManager;
import com.github.unchama.seichiassist.util.Util;
import com.github.unchama.seichiassist.util.Util.DirectionType;


public class PlayerData {
	static Config config = SeichiAssist.config;
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
	//現在のスターレベル
	public long starlevel;
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
	//全体メッセージ非表示トグル
	public boolean everymessageflag;
	//ワールドガード保護ログ表示トグル
	public boolean dispworldguardlogflag;
	//複数種類破壊トグル
	public boolean multipleidbreakflag;

	//チェスト破壊トグル
	public boolean chestflag;

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

	public int ChainVote;

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
	public int selectHomeNum;
	public int setHomeNameNum;
	public String[] subhome_name = new String[SeichiAssist.config.getSubHomeMax()];
	public boolean isSubHomeNameChange;

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
	public int titlepage ; //実績メニュー用汎用ページ指定
	public boolean samepageflag ;//実績ショップ用


	//建築LV
	private int build_lv;
	//設置ブロック数
	private BigDecimal build_count;
	//設置ブロックサーバー統合フラグ
	private byte build_count_flg;

	// 1周年記念
	public boolean anniversary;

	//ハーフブロック破壊抑制用
	private boolean halfBreakFlag;

	//グリッド式保護関連
	private int aheadUnit;
	private int behindUnit;
	private int rightUnit;
	private int leftUnit;
	private boolean canCreateRegion;
	private int unitPerClick;
	private Map<Integer, GridTemplate> templateMap;

	//投票妖精関連
	public boolean canVotingFairyUse;
	public Calendar VotingFairyStartTime;
	public Calendar VotingFairyEndTime;
	public int hasVotingFairyMana;
	public int VotingFairyRecoveryValue;
	public int giveApple;

	//貢献度pt
	public int added_mana;
	public int contribute_point;

	//正月イベント用
	public boolean hasNewYearSobaGive;
	public int newYearBagAmount;

	//バレンタインイベント用
	public boolean hasChocoGave;

	//MineStackの履歴
	public MineStackHistoryData hisotryData;
	//MineStack検索機能使用中かどうか
	public boolean isSearching;
	//MineStack検索保存用Map
	public Map<Integer, MineStackObj> indexMap;

	public int GBstage;
	public int GBexp;
	public int GBlevel;
	public boolean isGBStageUp;


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
			//統計にないため除外
			if(m != Material.GRASS_PATH && m != Material.SOIL && m != Material.MOB_SPAWNER){
				staticdata.add(player.getStatistic(Statistic.MINE_BLOCK, m));
			}
		}
		this.activeskilldata = new ActiveSkillData();
		this.expbar = new ExpBar(this, player);
		this.expmanager = new ExperienceManager(player);
		this.p_givenvote = 0;
		this.votecooldownflag = true;
		this.gachacooldownflag = true;
		this.shareinvcooldownflag = true;
		this.chestflag = true;

		this.displayTypeLv = true;
		this.displayTitle1No = 0 ;
		this.displayTitle2No = 0 ;
		this.displayTitle3No = 0 ;
		this.TitleFlags = new BitSet(10000);
		this.TitleFlags.set(1);
		this.p_vote_forT = 0 ;
		this.giveachvNo = 0 ;
		this.titlepage = 1 ;

		for (int x = 0 ; x < SeichiAssist.config.getSubHomeMax() ; x++){
//			this.sub_home[x] = new Location(null, 0, 0, 0);
			this.sub_home[x] = null;
			this.subhome_name[x] = "サブホームポイント" + (x+1);
		}
		this.build_lv = 1;
		this.build_count = BigDecimal.ZERO;
		this.build_count_flg = 0;
		this.anniversary = false;

		this.halfBreakFlag = false;

		this.aheadUnit = 0;
		this.behindUnit = 0;
		this.rightUnit = 0;
		this.leftUnit = 0;
		this.canCreateRegion = true;
		this.unitPerClick = 1;
		this.templateMap = new HashMap<>();
		for (int i = 0; i <= config.getTemplateKeepAmount() - 1; i++) {
			this.templateMap.put(i, new GridTemplate(0, 0, 0, 0));
		}

		this.canVotingFairyUse = false;
		this.hasVotingFairyMana = 0;
		this.VotingFairyRecoveryValue = 0;
		this.giveApple = 0;
		this.VotingFairyStartTime = null;
		this.VotingFairyEndTime = null;

		this.added_mana = 0;
		this.contribute_point = 0;

		this.hasNewYearSobaGive = false;
		this.newYearBagAmount = 0;

		this.hasChocoGave = false;

		this.hisotryData = new MineStackHistoryData();
		this.isSearching = false;
		this.indexMap = new HashMap<>();

		this.ChainVote = 0;

		this.selectHomeNum = 0;
		this.setHomeNameNum = 0;
		this.isSubHomeNameChange = false;

		this.GBstage = 0;
		this.GBlevel = 0;
		this.GBexp = 0;
		this.isGBStageUp = false;
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
		//スターレベル用の計算
		starlevel = ( totalbreaknum / 87115000 ) - 1 ;

		//表示を追加する処理
		if(displayTitle1No == 0 && displayTitle2No == 0 && displayTitle3No == 0){
			if(starlevel <= 0){
				displayname =  "[ Lv" + level + " ]" + displayname + ChatColor.WHITE;
			}else{
				displayname =  "[Lv" + level + "☆" + starlevel + "]" + displayname + ChatColor.WHITE;
			}
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
			//レベルアップイベント着火
			Bukkit.getPluginManager().callEvent(new SeichiLevelUpEvent(p, this, i + 1));
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
			if(m != Material.GRASS_PATH && m != Material.SOIL && m != Material.MOB_SPAWNER){
				int getstat = p.getStatistic(Statistic.MINE_BLOCK, m);
				int getincrease = getstat - staticdata.get(i);
				sum += calcBlockExp(m,getincrease,p);
				if(SeichiAssist.DEBUG){
					p.sendMessage("calcの値:" + calcBlockExp(m,getincrease,p) + "(" + m + ")");
				}
				staticdata.set(i, getstat);
				i++;
			}
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

		//氷塊とマグマブロックの整地量を2倍
		case PACKED_ICE:
			result *= 2.0;
			break;

		case MAGMA:
			result *= 2.0;
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

	public void setSubHomeName(String s){
		byte[] bytes = null;
		if (s == null) {
		    //SQL初期化時(つまりサーバに導入時)必ず入力がカラムの内容がnullになるためその対策
		    return;
        }
		try {
			bytes = Hex.decodeHex(s.toCharArray());
		} catch (DecoderException e) {
			e.printStackTrace();
		}
		String str = new String(bytes,StandardCharsets.UTF_8);
		String[] strs = str.split(",");
		for (int i =0; i < strs.length; i++) {
			this.subhome_name[i] = strs[i];
		}
	}

	public String SubHomeNameToString(){
		byte[] sbyte = null;
		String str = String.join(",", this.subhome_name);
		sbyte = str.getBytes(StandardCharsets.UTF_8);
		String result = new String(Hex.encodeHex(sbyte));
		return result;
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
	public void build_count_set(BigDecimal count){
		build_count = count;
	}
	public BigDecimal build_count_get(){
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

	public boolean canBreakHalfBlock() {
		return this.halfBreakFlag;
	}

	public void toggleHalfBreakFlag() {
		halfBreakFlag = !halfBreakFlag;
	}

	public Map<DirectionType,Integer> getUnitMap() {
		Map<DirectionType, Integer> unitMap = new HashMap<>();

		unitMap.put(DirectionType.AHEAD, this.aheadUnit);
		unitMap.put(DirectionType.BEHIND, this.behindUnit);
		unitMap.put(DirectionType.RIGHT, this.rightUnit);
		unitMap.put(DirectionType.LEFT, this.leftUnit);

		return unitMap;
	}

	public int getGridChunkAmount() {
		return (this.aheadUnit + 1 + this.behindUnit) * (this.rightUnit + 1 + this.leftUnit);
	}

	/*
	public void setAheadChunk(int amount) {
		this.aheadChunk = amount;
	}

	public void setBehindChunk(int amount) {
		this.behindChunk = amount;
	}

	public void setRightChunk(int amount) {
		this.rightChunk = amount;
	}

	public void setLeftChunk(int amount) {
		this.leftChunk = amount;
	}
	*/

	public boolean canGridExtend(DirectionType directionType) {
		final int LIMIT = config.getGridLimit();
		Map<DirectionType, Integer> chunkMap = getUnitMap();

		//チャンクを拡大すると仮定する
		final int assumedAmoont = chunkMap.get(directionType) + this.unitPerClick;
		//合計チャンク再計算値
		int assumedUnitAmount = 0;
		//一応すべての拡張値を出しておく
		final int ahead = chunkMap.get(DirectionType.AHEAD);
		final int behind = chunkMap.get(DirectionType.BEHIND);
		final int right = chunkMap.get(DirectionType.RIGHT);
		final int left = chunkMap.get(DirectionType.LEFT);

		switch (directionType) {
			case AHEAD:
				assumedUnitAmount = (assumedAmoont + 1 + behind) * (right + 1 + left);
				break;
			case BEHIND:
				assumedUnitAmount = (ahead + 1 + assumedAmoont) * (right + 1 + left);
				break;
			case RIGHT:
				assumedUnitAmount = (ahead + 1 + behind) * (assumedAmoont + 1 + left);
				break;
			case LEFT:
				assumedUnitAmount = (ahead + 1 + behind) * (right + 1 + assumedAmoont);
				break;
			default:
				//ここに来ることはありえない
				Bukkit.getLogger().warning("グリッド式保護で予期せぬ動作[チャンク値仮定]。開発者に報告してください。");
		}

		return assumedUnitAmount <= LIMIT;

	}

	public boolean canGridReduce(DirectionType directionType) {
		Map<DirectionType, Integer> chunkMap = getUnitMap();

		//減らしたと仮定する
		final int assumedAmount = chunkMap.get(directionType) - unitPerClick;
		return assumedAmount >= 0;
	}

	public void setUnitAmount(DirectionType directionType, int amount) {
		switch (directionType) {
			case AHEAD:
				this.aheadUnit = amount;
				break;
			case BEHIND:
				this.behindUnit = amount;
				break;
			case RIGHT:
				this.rightUnit = amount;
				break;
			case LEFT:
				this.leftUnit = amount;
				break;
			default:
				//わざと何もしない
		}
	}

	public void addUnitAmount(DirectionType directionType, int addAmount) {
		switch (directionType) {
			case AHEAD:
				this.aheadUnit += addAmount;
				break;
			case BEHIND:
				this.behindUnit += addAmount;
				break;
			case RIGHT:
				this.rightUnit += addAmount;
				break;
			case LEFT:
				this.leftUnit += addAmount;
				break;
			default:
				//わざと何もしない
		}
	}

	public void setCanCreateRegion(boolean flag) {
		this.canCreateRegion = flag;
	}

	public boolean canCreateRegion() {
		return this.canCreateRegion;
	}

	public void toggleUnitPerGrid () {
		if (this.unitPerClick == 1) {
			this.unitPerClick = 10;
		} else if (this.unitPerClick == 10) {
			this.unitPerClick = 100;
		} else if (this.unitPerClick == 100) {
			this.unitPerClick = 1;
		}
	}

	public int getUnitPerClick() {
		return this.unitPerClick;
	}

	public void setTemplateMap(Map<Integer, GridTemplate> setMap) {
		this.templateMap = setMap;
	}

	public Map<Integer, GridTemplate> getTemplateMap() {
		return this.templateMap;
	}

	public String VotingFairyTimeToString(){
		Calendar cal = this.VotingFairyStartTime;
		String s = "";
		if (this.VotingFairyStartTime == null){
			//設定されてない場合
			s += ",,,,,";
		}else{
			//設定されてる場合
			Date date = cal.getTime();
			SimpleDateFormat format = new SimpleDateFormat("yyyy,MM,dd,HH,mm,");
			s += format.format(date);
		}
		return s;
	}

	public void SetVotingFairyTime(String str,Player p){
		String[] s = str.split(",", -1);
		if(s[0].length() > 0 && s[1].length() > 0 && s[2].length() > 0 && s[3].length() > 0 && s[4].length() > 0 ){
			Calendar startTime = new GregorianCalendar(Integer.parseInt(s[0]),Integer.parseInt(s[1])-1,Integer.parseInt(s[2]),Integer.parseInt(s[3]),Integer.parseInt(s[4]));
			Calendar EndTime = new GregorianCalendar(Integer.parseInt(s[0]),Integer.parseInt(s[1])-1,Integer.parseInt(s[2]),Integer.parseInt(s[3])+4,Integer.parseInt(s[4])+1);
			this.VotingFairyStartTime = startTime;
			this.VotingFairyEndTime = EndTime;
		}
	}

	public void isVotingFairy(Player p){
		//効果は継続しているか
			if( this.canVotingFairyUse == true && Util.isVotingFairyPeriod(this.VotingFairyStartTime, this.VotingFairyEndTime) == false ){
				this.canVotingFairyUse = false ;
				p.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "妖精は何処かへ行ってしまったようだ...");
			}
			else if(this.canVotingFairyUse == true){
				p.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "おかえり。" + p.getName() );
				if(this.hasVotingFairyMana > 0)
					p.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "僕はまだ君のマナを回復させられるよ" );
				else
					p.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "≪マナの妖精≫ " + ChatColor.RESET + "ガチャりんごがもう無いからまた渡してくれると嬉しいな" );
			}
	}

	public void isContribute(Player p,int addMana){
		Mana mana = new Mana();

		//負数(入力ミスによるやり直し中プレイヤーがオンラインだった場合)の時
		if(addMana < 0){
			p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "入力者のミスによって得た不正なマナを" + (-10*addMana) +"分減少させました.");
			p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "申し訳ございません.");
		}else{
			p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "運営からあなたの整地鯖への貢献報酬として");
			p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "マナの上限値が" + 10*addMana + "上昇しました．(永久)");
		}
		this.added_mana += addMana;

		mana.calcMaxMana(p, this.level);
	}
}
