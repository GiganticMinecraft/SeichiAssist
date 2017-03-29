package com.github.unchama.seichiassist;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.unchama.seichiassist.data.GachaData;
import com.github.unchama.seichiassist.data.MineStackGachaData;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.RankData;
import com.github.unchama.seichiassist.listener.MebiusListener;
import com.github.unchama.seichiassist.task.CoolDownTaskRunnable;
import com.github.unchama.seichiassist.task.LoadPlayerDataTaskRunnable;
import com.github.unchama.seichiassist.task.PlayerDataSaveTaskRunnable;
import com.github.unchama.seichiassist.task.PlayerDataUpdateOnJoinRunnable;
import com.github.unchama.seichiassist.util.BukkitSerialization;
import com.github.unchama.seichiassist.util.Util;

//MySQL操作関数
public class Sql{
	private final String url;
	private final String db;
	private final String id;
	private final String pw;
	public static Connection con = null;
	private Statement stmt = null;

	private ResultSet rs = null;

	public static String exc;
	private SeichiAssist plugin = SeichiAssist.plugin;
	private HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;

	//コンストラクタ
	Sql(SeichiAssist plugin ,String url, String db, String id, String pw){
		this.plugin = plugin;
		this.url = url;
		this.db = db;
		this.id = id;
		this.pw = pw;
	}

	/**
	 * 接続関数
	 *
	 * @param url 接続先url
	 * @param id ユーザーID
	 * @param pw ユーザーPW
	 * @param db データベースネーム
	 * @param table テーブルネーム
	 * @return
	 */
	public boolean connect(){
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			e.printStackTrace();
			plugin.getLogger().info("Mysqlドライバーのインスタンス生成に失敗しました");
			return false;
		}
		//sql鯖への接続とdb作成
		if(!connectMySQL()){
			plugin.getLogger().info("SQL接続に失敗しました");
			return false;
		}
		if(!createDB()){
			plugin.getLogger().info("データベース作成に失敗しました");
			return false;
		}
		/*
		if(!connectDB()){
			plugin.getLogger().info("データベース接続に失敗しました");
			return false;
		}
		*/
		/*
		if(!createPlayerDataTable(SeichiAssist.PLAYERDATA_TABLENAME)){
			plugin.getLogger().info("playerdataテーブル作成に失敗しました");
			return false;
		}
		*/

		if(!createGachaDataTable(SeichiAssist.GACHADATA_TABLENAME)){
			plugin.getLogger().info("gachadataテーブル作成に失敗しました");
			return false;
		}

		if(!createMineStackGachaDataTable(SeichiAssist.MINESTACK_GACHADATA_TABLENAME)){
			plugin.getLogger().info("MineStack用gachadataテーブル作成に失敗しました");
			return false;
		}

		if(!createDonateDataTable(SeichiAssist.DONATEDATA_TABLENAME)){
			plugin.getLogger().info("donatedataテーブル作成に失敗しました");
			return false;
		}


		return true;
	}

	public boolean connect1(){
		if(!createPlayerDataTable(SeichiAssist.PLAYERDATA_TABLENAME)){
			plugin.getLogger().info("playerdataテーブル作成に失敗しました");
			return false;
		}
		return true;
	}

	private boolean connectMySQL(){
		try {
			if(stmt != null && !stmt.isClosed()){
				stmt.close();
				con.close();
			}
			con = (Connection) DriverManager.getConnection(url, id, pw);
			stmt = con.createStatement();
	    } catch (SQLException e) {
	    	e.printStackTrace();
	    	return false;
		}
		return true;
	}

	//接続正常ならtrue、そうでなければ再接続試行後正常でtrue、だめならfalseを返す
	public boolean checkConnection(){
		try {
			if(con.isClosed()){
				plugin.getLogger().warning("sqlConnectionクローズを検出。再接続試行");
				con = (Connection) DriverManager.getConnection(url, id, pw);
			}
			if(stmt.isClosed()){
				plugin.getLogger().warning("sqlStatementクローズを検出。再接続試行");
				stmt = con.createStatement();
				//connectDB();
			}
	    } catch (SQLException e) {
	    	e.printStackTrace();
	    	//イクセプションった時に接続再試行
	    	plugin.getLogger().warning("sqlExceptionを検出。再接続試行");
	    	if(connectMySQL()){
	    		plugin.getLogger().info("sqlコネクション正常");
	    		return true;
	    	}else{
	    		plugin.getLogger().warning("sqlコネクション不良を検出");
	    		return false;
	    	}
		}
		//plugin.getLogger().info("sqlコネクション正常");
		return true;
	}

	/**
	 * コネクション切断処理
	 *
	 * @return 成否
	 */
	public boolean disconnect(){
	    if (con != null){
	    	try{
	    		stmt.close();
				con.close();
	    	}catch (SQLException e){
	    		e.printStackTrace();
	    		return false;
	    	}
	    }
	    return true;
	}

	//コマンド出力関数
	//@param command コマンド内容
	//@return 成否
	//@throws SQLException
	//private変更禁止！(処理がバッティングします)
	private boolean putCommand(String command){
		checkConnection();
		try {
			stmt.executeUpdate(command);
			return true;
		}catch (SQLException e) {
			java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
			exc = e.getMessage();
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * データベース作成
	 * 失敗時には変数excにエラーメッセージを格納
	 *
	 * @param table テーブル名
	 * @return 成否
	 */
	public boolean createDB(){
		if(db==null){
			return false;
		}
		String command;
		command = "CREATE DATABASE IF NOT EXISTS " + db
				+ " character set utf8 collate utf8_general_ci";
		return putCommand(command);
	}

	/*
	private boolean connectDB() {
		String command;
		command = "use " + db;
		return putCommand(command);
	}
	*/

	/**
	 * テーブル作成
	 * 失敗時には変数excにエラーメッセージを格納
	 *
	 * @param table テーブル名
	 * @return 成否
	 */
	public boolean createPlayerDataTable(String table){
		if(table==null){
			return false;
		}
		//テーブルが存在しないときテーブルを新規作成
		String command =
				"CREATE TABLE IF NOT EXISTS " + db + "." + table +
				"(name varchar(30) unique," +
				"uuid varchar(128) unique)";
		if(!putCommand(command)){
			return false;
		}
		//必要なcolumnを随時追加
		command =
				"alter table " + db + "." + table +
				" add column if not exists effectflag tinyint default 0" +
				",add column if not exists minestackflag boolean default true" +
				",add column if not exists messageflag boolean default false" +
				",add column if not exists activemineflagnum int default 0" +
				",add column if not exists assaultflag boolean default false" +
				",add column if not exists activeskilltype int default 0" +
				",add column if not exists activeskillnum int default 1" +
				",add column if not exists assaultskilltype int default 0" +
				",add column if not exists assaultskillnum int default 0" +
				",add column if not exists arrowskill int default 0" +
				",add column if not exists multiskill int default 0" +
				",add column if not exists breakskill int default 0" +
				",add column if not exists condenskill int default 0" +
				",add column if not exists effectnum int default 0" +
				",add column if not exists gachapoint int default 0" +
				",add column if not exists gachaflag boolean default true" +
				",add column if not exists level int default 1" +
				",add column if not exists numofsorryforbug int default 0" +
				",add column if not exists inventory blob default null" +
				",add column if not exists rgnum int default 0" +
				",add column if not exists totalbreaknum bigint default 0" +
				",add column if not exists lastquit datetime default null" +
				",add column if not exists displayTypeLv boolean default true" +
				",add column if not exists displayTitleNo int default 0" +
				",add column if not exists TitleFlags text default null" +
				",add column if not exists giveachvNo int default 0" ;

				/*
				",add column if not exists stack_dirt int default 0" +
				",add column if not exists stack_gravel int default 0" +
				",add column if not exists stack_cobblestone int default 0" +
				",add column if not exists stack_stone int default 0" +
				",add column if not exists stack_sand int default 0" +

				",add column if not exists stack_sandstone int default 0" +
				",add column if not exists stack_netherrack int default 0" +
				",add column if not exists stack_ender_stone int default 0" +
				",add column if not exists stack_obsidian int default 0" + //追加
				",add column if not exists stack_grass int default 0" +

				",add column if not exists stack_quartz int default 0" +
				",add column if not exists stack_quartz_ore int default 0" +
				",add column if not exists stack_soul_sand int default 0" +
				",add column if not exists stack_magma int default 0" +
				",add column if not exists stack_coal int default 0" +

				",add column if not exists stack_coal_ore int default 0" +
				",add column if not exists stack_iron_ore int default 0" +
				",add column if not exists stack_packed_ice int default 0" +
				",add column if not exists stack_gold_ore int default 0" +
				",add column if not exists stack_lapis_ore int default 0" +

				",add column if not exists stack_emerald_ore int default 0" +
				",add column if not exists stack_redstone_ore int default 0" +
				",add column if not exists stack_diamond_ore int default 0" +
				",add column if not exists stack_log int default 0" +
				",add column if not exists stack_log_2 int default 0" +

				",add column if not exists stack_wood int default 0" +
				",add column if not exists stack_hard_clay int default 0" +
				",add column if not exists stack_stained_clay int default 0" +
				",add column if not exists stack_fence int default 0" +
				",add column if not exists stack_lapis_lazuli int default 0" + //テスト

				",add column if not exists stack_granite int default 0" + //追加
				",add column if not exists stack_diorite int default 0" + //追加
				",add column if not exists stack_andesite int default 0" + //追加
				",add column if not exists stack_red_sand int default 0" + //追加
				",add column if not exists stack_red_sandstone int default 0" + //追加

				",add column if not exists stack_log1 int default 0" + //追加
				",add column if not exists stack_log2 int default 0" + //追加
				",add column if not exists stack_log3 int default 0" + //追加
				",add column if not exists stack_log_21 int default 0" + //追加
				",add column if not exists stack_stained_clay1 int default 0" + //追加

				",add column if not exists stack_stained_clay4 int default 0" + //追加
				",add column if not exists stack_stained_clay8 int default 0" + //追加
				",add column if not exists stack_stained_clay12 int default 0" + //追加
				",add column if not exists stack_stained_clay14 int default 0" + //追加
				",add column if not exists stack_emerald int default 0" + //追加

				",add column if not exists stack_redstone int default 0" + //追加
				",add column if not exists stack_diamond int default 0" + //追加
				",add column if not exists stack_clay int default 0" + //追加
				",add column if not exists stack_glowstone int default 0" + //追加
				",add column if not exists stack_dirt1 int default 0" + //

				",add column if not exists stack_dirt2 int default 0" + //
				",add column if not exists stack_mycel int default 0" + //
				",add column if not exists stack_snow_block int default 0" + //
				",add column if not exists stack_ice int default 0" + //
				",add column if not exists stack_dark_oak_fence int default 0" + //

				",add column if not exists stack_mossy_cobblestone int default 0" + //
				",add column if not exists stack_rails int default 0" + //
				",add column if not exists stack_exp_bottle int default 0" + //
				",add column if not exists stack_huge_mushroom_1 int default 0" + //
				",add column if not exists stack_huge_mushroom_2 int default 0" + //

				",add column if not exists stack_web int default 0" + //
				",add column if not exists stack_string int default 0" + //
				",add column if not exists stack_wood5 int default 0" + //
				",add column if not exists stack_sapling int default 0" + //
				",add column if not exists stack_sapling1 int default 0" + //

				",add column if not exists stack_sapling2 int default 0" + //
				",add column if not exists stack_sapling3 int default 0" + //
				",add column if not exists stack_sapling4 int default 0" + //
				",add column if not exists stack_sapling5 int default 0" + //
				",add column if not exists stack_leaves int default 0" + //

				",add column if not exists stack_leaves1 int default 0" + //
				",add column if not exists stack_leaves2 int default 0" + //
				",add column if not exists stack_leaves3 int default 0" + //
				",add column if not exists stack_leaves_2 int default 0" + //
				",add column if not exists stack_leaves_21 int default 0" + //

				",add column if not exists stack_gachaimo int default 0"; //
				*/

				//MineStack関連をすべてfor文に変更
				if(SeichiAssist.minestack_sql_enable==true){
					for(int i=0; i<SeichiAssist.minestacklist.size(); i++){
						command += ",add column if not exists stack_" + SeichiAssist.minestacklist.get(i).getMineStackObjName() + " int default 0";
					}
				}

				command +=
				",add column if not exists playtick int default 0" +
				",add column if not exists killlogflag boolean default false" +
				",add column if not exists worldguardlogflag boolean default true" +//

				",add column if not exists multipleidbreakflag boolean default false" +

				",add column if not exists pvpflag boolean default false" +
				",add column if not exists loginflag boolean default false" +
				",add column if not exists p_vote int default 0" +
				",add column if not exists p_givenvote int default 0" +
				",add column if not exists effectpoint int default 0" +
				",add column if not exists premiumeffectpoint int default 0" +
				",add column if not exists mana double default 0.0" +
				",add column if not exists expvisible boolean default true" +
				",add column if not exists totalexp int default 0" +
				",add column if not exists expmarge tinyint unsigned default 0" +
				",add column if not exists shareinv blob" +
				",add column if not exists everysound boolean default true" +

				",add column if not exists homepoint_" + SeichiAssist.config.getServerNum() + " varchar(" + SeichiAssist.config.getSubHomeMax() * SeichiAssist.SUB_HOME_DATASIZE + ") default ''"+

				//BuildAssistのデータ
				",add column if not exists build_lv int default 1" +//
				",add column if not exists build_count int default 0" +//
				",add column if not exists build_count_flg TINYINT UNSIGNED default 0" +//

				"";


		ActiveSkillEffect[] activeskilleffect = ActiveSkillEffect.values();
		for(int i = 0; i < activeskilleffect.length ; i++){
			command = command +
					",add column if not exists " + activeskilleffect[i].getsqlName() + " boolean default false";
		}
		ActiveSkillPremiumEffect[] premiumeffect = ActiveSkillPremiumEffect.values();
		for(int i = 0; i < premiumeffect.length ; i++){
			command = command +
					",add column if not exists " + premiumeffect[i].getsqlName() + " boolean default false";
		}
		return putCommand(command);
	}

	//ガチャデータテーブル作成
	public boolean createGachaDataTable(String table){
		if(table==null){
			return false;
		}
		//テーブルが存在しないときテーブルを新規作成
		String command =
				"CREATE TABLE IF NOT EXISTS " + db + "." + table +
				"(id int auto_increment unique,"
				+ "amount int(11))";
		if(!putCommand(command)){
			return false;
		}
		//必要なcolumnを随時追加
		command =
				"alter table " + db + "." + table +
				" add column if not exists probability double default 0.0" +
				",add column if not exists itemstack blob default null" +
				"";
		return putCommand(command);
	}

	//MineStack用ガチャデータテーブル作成
	public boolean createMineStackGachaDataTable(String table){
		if(table==null){
			return false;
		}
		//テーブルが存在しないときテーブルを新規作成
		String command =
				"CREATE TABLE IF NOT EXISTS " + db + "." + table +
				"(id int auto_increment unique,"
				+ "amount int(11))";
		if(!putCommand(command)){
			return false;
		}
		//必要なcolumnを随時追加
		command =
				"alter table " + db + "." + table +
				" add column if not exists probability double default 0.0" +
				",add column if not exists level int(11) default 0" +
				",add column if not exists obj_name tinytext default null" +
				",add column if not exists itemstack blob default null" +
				"";
		return putCommand(command);
	}

	public boolean createDonateDataTable(String table){
		if(table==null){
			return false;
		}
		//テーブルが存在しないときテーブルを新規作成
		String command =
				"CREATE TABLE IF NOT EXISTS " + db + "." + table +
				"(id int auto_increment unique)";
		if(!putCommand(command)){
			return false;
		}
		//必要なcolumnを随時追加
		command =
				"alter table " + db + "." + table +
				" add column if not exists playername varchar(20) default null" +
				",add column if not exists playeruuid varchar(128) default null" +
				",add column if not exists effectnum int default null" +
				",add column if not exists effectname varchar(20) default null" +
				",add column if not exists getpoint int default 0" +
				",add column if not exists usepoint int default 0" +
				",add column if not exists date datetime default null" +
				"";
		return putCommand(command);
	}
	//投票特典配布時の処理(p_givenvoteの値の更新もココ)
	public int compareVotePoint(Player player,final PlayerData playerdata){

		if(!playerdata.votecooldownflag){
			player.sendMessage(ChatColor.RED + "しばらく待ってからやり直してください");
			return 0;
		}else{
	        //連打による負荷防止の為クールダウン処理
	        new CoolDownTaskRunnable(player,true,false,false).runTaskLater(plugin,1200);
		}
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String struuid = playerdata.uuid.toString();
		int p_vote = 0;
		int p_givenvote = 0;
		String command = "select p_vote,p_givenvote from " + db + "."  + table
				+ " where uuid = '" + struuid + "'";
 		try{
			rs = stmt.executeQuery(command);
			while (rs.next()) {
				p_vote = rs.getInt("p_vote");
				p_givenvote = rs.getInt("p_givenvote");
				}
			rs.close();
		} catch (SQLException e) {
			java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
			exc = e.getMessage();
			e.printStackTrace();
			player.sendMessage(ChatColor.RED + "投票特典の受け取りに失敗しました");
			return 0;
		}
 		//比較して差があればその差の値を返す(同時にp_givenvoteも更新しておく)
 		if(p_vote > p_givenvote){
 			command = "update " + db + "." + table
 					+ " set p_givenvote = " + p_vote
 					+ " where uuid like '" + struuid + "'";
 			if(!putCommand(command)){
 				player.sendMessage(ChatColor.RED + "投票特典の受け取りに失敗しました");
 				return 0;
 			}

 			return p_vote - p_givenvote;
 		}
 		player.sendMessage(ChatColor.YELLOW + "投票特典は全て受け取り済みのようです");
		return 0;

	}

	//最新のnumofsorryforbug値を返してmysqlのnumofsorrybug値を初期化する処理
	public int givePlayerBug(Player player,final PlayerData playerdata){
		if(!playerdata.votecooldownflag){
			player.sendMessage(ChatColor.RED + "しばらく待ってからやり直してください");
			return 0;
		}else{
	        //連打による負荷防止の為クールダウン処理
	        new CoolDownTaskRunnable(player,true,false,false).runTaskLater(plugin,1200);
		}
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String struuid = playerdata.uuid.toString();
		int numofsorryforbug = 0;
		String command = "select numofsorryforbug from " + db + "." + table
				+ " where uuid = '" + struuid + "'";
		try{
			rs = stmt.executeQuery(command);
			while (rs.next()) {
				numofsorryforbug = rs.getInt("numofsorryforbug");
				}
			rs.close();
		} catch (SQLException e) {
			java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
			exc = e.getMessage();
			e.printStackTrace();
			player.sendMessage(ChatColor.RED + "ガチャ券の受け取りに失敗しました");
			return 0;
		}
 		//576より多い場合はその値を返す(同時にnumofsorryforbugから-576)
 		if(numofsorryforbug > 576){
 			command = "update " + db + "." + table
 					+ " set numofsorryforbug = numofsorryforbug - 576"
 					+ " where uuid like '" + struuid + "'";
 			if(!putCommand(command)){
 				player.sendMessage(ChatColor.RED + "ガチャ券の受け取りに失敗しました");
 				return 0;
 			}

 			return 576;
 		}//0より多い場合はその値を返す(同時にnumofsorryforbug初期化)
 		else if(numofsorryforbug > 0){
 			command = "update " + db + "." + table
 					+ " set numofsorryforbug = 0"
 					+ " where uuid like '" + struuid + "'";
 			if(!putCommand(command)){
 				player.sendMessage(ChatColor.RED + "ガチャ券の受け取りに失敗しました");
 				return 0;
 			}

 			return numofsorryforbug;
 		}
 		player.sendMessage(ChatColor.YELLOW + "ガチャ券は全て受け取り済みのようです");
		return 0;

	}

	//投票時にmysqlに投票ポイントを加算しておく処理
	public boolean addVotePoint(String name) {
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String command = "";

		command = "update " + db + "." + table
				+ " set"

				//1加算
				+ " p_vote = p_vote + 1"

				+ " where name like '" + name + "'";

		return putCommand(command);

	}

	//プレミアムエフェクトポイントを加算しておく処理
	public boolean addPremiumEffectPoint(String name,int num) {
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String command = "";

		command = "update " + db + "." + table
				+ " set"

				//引数で来たポイント数分加算
				+ " premiumeffectpoint = premiumeffectpoint + " + num

				+ " where name like '" + name + "'";

		return putCommand(command);
	}


	//指定されたプレイヤーにガチャ券を送信する
	public boolean addPlayerBug(String name,int num) {
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String command = "update " + db + "." + table
				+ " set numofsorryforbug = numofsorryforbug + " + num
				+ " where name like '" + name + "'";
		return putCommand(command);
	}


	public boolean loadPlayerData(final Player p) {
		String name = Util.getName(p);
		final UUID uuid = p.getUniqueId();
		final String struuid = uuid.toString().toLowerCase();
		String command = "";
		final String table = SeichiAssist.PLAYERDATA_TABLENAME;
 		int count = -1;
 		//uuidがsqlデータ内に存在するか検索
 		//command:
 		//select count(*) from playerdata where uuid = 'struuid'
 		command = "select count(*) as count from " + db + "." + table
 				+ " where uuid = '" + struuid + "'";
 		//sqlコネクションチェック(mysql接続が切れたときの為のフェイルセーフ機構(ダメならリログすれば直る))
 		checkConnection();
 		try{
			rs = stmt.executeQuery(command);
			while (rs.next()) {
				   count = rs.getInt("count");
				  }
			rs.close();
		} catch (SQLException e) {
			java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
			exc = e.getMessage();
			e.printStackTrace();
			return false;
		}

 		if(count == 0){
 			//uuidが存在しない時の処理

 			//新しくuuidとnameを設定し行を作成
 			//insert into playerdata (name,uuid) VALUES('unchima','UNCHAMA')
 			command = "insert into " + db + "." + table
 	 				+ " (name,uuid,loginflag) values('" + name
 	 				+ "','" + struuid+ "','1')";
 			if(!putCommand(command)){
 				return false;
 			}
 			//PlayerDataを新規作成
 			playermap.put(uuid, new PlayerData(p));
 			//初見さんにLv1メッセージを送信
 			p.sendMessage(SeichiAssist.config.getLvMessage(1));
 			//初見さんであることを全体告知
 			plugin.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "【初見キタ】" + p.getName() + "のプレイヤーデータ作成完了");
 			Util.sendEveryMessage(ChatColor.LIGHT_PURPLE+""+ChatColor.BOLD+name+"さんは初参加です。整地鯖へヨウコソ！" + ChatColor.RESET +" - " + ChatColor.YELLOW + ChatColor.UNDERLINE +  "http://seichi.click");
 			Util.sendEverySound(Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
 			//初見プレイヤーに木の棒、エリトラ、ピッケルを配布
 			p.getInventory().addItem(new ItemStack(Material.STICK));
 			p.getInventory().addItem(new ItemStack(Material.ELYTRA));
 			p.getInventory().addItem(new ItemStack(Material.DIAMOND_PICKAXE));
 			p.getInventory().addItem(new ItemStack(Material.DIAMOND_SPADE));
 			MebiusListener.give(p);
 			return true;

 		}else if(count == 1){
 			//uuidが存在するときの処理
 			if(SeichiAssist.DEBUG){
 				p.sendMessage("sqlにデータが保存されています。");
 			}
 			p.sendMessage(ChatColor.YELLOW + "プレイヤーデータを読み込んでいます。しばらくお待ちください…");
 			new LoadPlayerDataTaskRunnable(p).runTaskTimerAsynchronously(plugin, 20, 20);
 			new PlayerDataUpdateOnJoinRunnable(p).runTaskTimer(plugin, 30, 20);
 			return true;

 		}else{
 			//mysqlに該当するplayerdataが2個以上ある時エラーを吐く
 			Bukkit.getLogger().info(Util.getName(p) + "のplayerdata読込時に原因不明のエラー発生");
 			return false;
 		}
	}

	//ondisable"以外"の時のプレイヤーデータセーブ処理(loginflag折りません)
	public void savePlayerData(PlayerData playerdata){
		new PlayerDataSaveTaskRunnable(playerdata,false,false).runTaskAsynchronously(plugin);
	}

	//ondisable"以外"の時のプレイヤーデータセーブ処理(ログアウト時に使用、loginflag折ります)
	public void saveQuitPlayerData(PlayerData playerdata){
		new PlayerDataSaveTaskRunnable(playerdata,false,true).runTaskAsynchronously(plugin);
	}


	//loginflagのフラグ折る処理(ondisable時とquit時に実行させる)
	/*
	public boolean logoutPlayerData(PlayerData playerdata) {
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String struuid = playerdata.uuid.toString();
		String command = "";

		command = "update " + db + "." + table
				+ " set"

				//ログインフラグ折る
				+ " loginflag = false"

				+ " where uuid like '" + struuid + "'";

		return putCommand(command);

	}
	*/

	//ガチャデータロード
	public boolean loadGachaData(){
		String table = SeichiAssist.GACHADATA_TABLENAME;
		List<GachaData> gachadatalist = new ArrayList<GachaData>();
		//SELECT `totalbreaknum` FROM `playerdata` WHERE 1 ORDER BY `playerdata`.`totalbreaknum` DESC
		String command = "select * from " + db + "." + table;
 		try{
			rs = stmt.executeQuery(command);
			while (rs.next()) {
				GachaData gachadata = new GachaData();
				Inventory inventory = BukkitSerialization.fromBase64(rs.getString("itemstack").toString());
				gachadata.itemstack = (inventory.getItem(0));
				gachadata.amount = rs.getInt("amount");
				gachadata.probability = rs.getDouble("probability");
				gachadatalist.add(gachadata);
				  }
			rs.close();
		} catch (SQLException | IOException e) {
			java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
			exc = e.getMessage();
			e.printStackTrace();
			return false;
		}
 		SeichiAssist.gachadatalist.clear();
 		SeichiAssist.gachadatalist.addAll(gachadatalist);
 		return true;

	}

	//MineStack用ガチャデータロード
	public boolean loadMineStackGachaData(){
		String table = SeichiAssist.MINESTACK_GACHADATA_TABLENAME;
		List<MineStackGachaData> gachadatalist = new ArrayList<MineStackGachaData>();
		//SELECT `totalbreaknum` FROM `playerdata` WHERE 1 ORDER BY `playerdata`.`totalbreaknum` DESC
		String command = "select * from " + db + "." + table;
 		try{
			rs = stmt.executeQuery(command);
			while (rs.next()) {
				MineStackGachaData gachadata = new MineStackGachaData();
				Inventory inventory = BukkitSerialization.fromBase64(rs.getString("itemstack").toString());
				gachadata.itemstack = (inventory.getItem(0));
				gachadata.amount = rs.getInt("amount");
				gachadata.level = rs.getInt("level");
				gachadata.obj_name = rs.getString("obj_name");
				gachadata.probability = rs.getDouble("probability");
				gachadatalist.add(gachadata);
				  }
			rs.close();
		} catch (SQLException | IOException e) {
			java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
			exc = e.getMessage();
			e.printStackTrace();
			return false;
		}
 		SeichiAssist.msgachadatalist.clear();
 		SeichiAssist.msgachadatalist.addAll(gachadatalist);
 		return true;

	}

	//ガチャデータセーブ
	public boolean saveGachaData(){
		String table = SeichiAssist.GACHADATA_TABLENAME;

		//まずmysqlのガチャテーブルを初期化(中身全削除)
		String command = "truncate table " + db + "." + table;
		if(!putCommand(command)){
			return false;
		}

		//次に現在のgachadatalistでmysqlを更新
		for(GachaData gachadata : SeichiAssist.gachadatalist){
			//Inventory作ってガチャのitemstackに突っ込む
			Inventory inventory = SeichiAssist.plugin.getServer().createInventory(null, 9*1);
			inventory.setItem(0,gachadata.itemstack);

			command = "insert into " + db + "." + table + " (probability,amount,itemstack)"
					+ " values"
					+ "(" + Double.toString(gachadata.probability)
					+ "," + Integer.toString(gachadata.amount)
					+ ",'" + BukkitSerialization.toBase64(inventory) + "'"
					+ ")";
			if(!putCommand(command)){
				return false;
			}
		}
		return true;
	}

	//MineStack用ガチャデータセーブ
	public boolean saveMineStackGachaData(){
		String table = SeichiAssist.MINESTACK_GACHADATA_TABLENAME;

		//まずmysqlのガチャテーブルを初期化(中身全削除)
		String command = "truncate table " + db + "." + table;
		if(!putCommand(command)){
			return false;
		}

		//次に現在のgachadatalistでmysqlを更新
		for(MineStackGachaData gachadata : SeichiAssist.msgachadatalist){
			//Inventory作ってガチャのitemstackに突っ込む
			Inventory inventory = SeichiAssist.plugin.getServer().createInventory(null, 9*1);
			inventory.setItem(0,gachadata.itemstack);

			command = "insert into " + db + "." + table + " (probability,amount,level,obj_name,itemstack)"
					+ " values"
					+ "(" + Double.toString(gachadata.probability)
					+ "," + Integer.toString(gachadata.amount)
					+ "," + Integer.toString(gachadata.level)
					+ ",'" + gachadata.obj_name.toString() + "'"
					+ ",'" + BukkitSerialization.toBase64(inventory) + "'"
					+ ")";
			if(!putCommand(command)){
				return false;
			}
		}
		return true;
	}

	//ランキング表示用に総破壊ブロック数のカラムだけ全員分引っ張る
	public boolean setRanking() {
		//plugin.getServer().getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "ランキング更新中…");
		//Util.sendEveryMessage(ChatColor.DARK_AQUA + "ランキング更新中…");
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		List<RankData> ranklist = SeichiAssist.ranklist;
		ranklist.clear();
		SeichiAssist.allplayerbreakblockint = 0;

		//SELECT `totalbreaknum` FROM `playerdata` WHERE 1 ORDER BY `playerdata`.`totalbreaknum` DESC
		String command = "select name,level,totalbreaknum from " + db + "." + table
				+ " order by totalbreaknum desc";
 		try{
			rs = stmt.executeQuery(command);
			while (rs.next()) {
				RankData rankdata = new RankData();
				rankdata.name = rs.getString("name");
				rankdata.level = rs.getInt("level");
				rankdata.totalbreaknum = rs.getLong("totalbreaknum");
				ranklist.add(rankdata);
				SeichiAssist.allplayerbreakblockint += rankdata.totalbreaknum;
				  }
			rs.close();
		} catch (SQLException e) {
			java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
			exc = e.getMessage();
			e.printStackTrace();
			return false;
		}
		//plugin.getServer().getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "ランキング更新完了");
		//Util.sendEveryMessage(ChatColor.DARK_AQUA + "ランキング更新完了");
 		return true;
	}

	//ランキング表示用にプレイ時間のカラムだけ全員分引っ張る
	public boolean setRanking_playtick() {
		//plugin.getServer().getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "ランキング更新中…");
		//Util.sendEveryMessage(ChatColor.DARK_AQUA + "ランキング更新中…");
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		List<RankData> ranklist = SeichiAssist.ranklist_playtick;
		ranklist.clear();
		//SeichiAssist.allplayerbreakblockint = 0;

		//SELECT `totalbreaknum` FROM `playerdata` WHERE 1 ORDER BY `playerdata`.`totalbreaknum` DESC
		String command = "select name,playtick from " + db + "." + table
				+ " order by playtick desc";
 		try{
			rs = stmt.executeQuery(command);
			while (rs.next()) {
				RankData rankdata = new RankData();
				rankdata.name = rs.getString("name");
				//rankdata.level = rs.getInt("level");
				//rankdata.totalbreaknum = rs.getInt("totalbreaknum");
				rankdata.playtick = rs.getInt("playtick");
				ranklist.add(rankdata);
				//SeichiAssist.allplayerbreakblockint += rankdata.totalbreaknum;
				  }
			rs.close();
		} catch (SQLException e) {
			java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
			exc = e.getMessage();
			e.printStackTrace();
			return false;
		}
		//plugin.getServer().getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "ランキング更新完了");
		//Util.sendEveryMessage(ChatColor.DARK_AQUA + "ランキング更新完了");
 		return true;
	}

	//ランキング表示用に投票数のカラムだけ全員分引っ張る
	public boolean setRanking_p_vote() {
		//plugin.getServer().getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "ランキング更新中…");
		//Util.sendEveryMessage(ChatColor.DARK_AQUA + "ランキング更新中…");
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		List<RankData> ranklist = SeichiAssist.ranklist_p_vote;
		ranklist.clear();
		//SeichiAssist.allplayerbreakblockint = 0;

		//SELECT `totalbreaknum` FROM `playerdata` WHERE 1 ORDER BY `playerdata`.`totalbreaknum` DESC
		String command = "select name,p_vote from " + db + "." + table
				+ " order by p_vote desc";
 		try{
			rs = stmt.executeQuery(command);
			while (rs.next()) {
				RankData rankdata = new RankData();
				rankdata.name = rs.getString("name");
				//rankdata.level = rs.getInt("level");
				//rankdata.totalbreaknum = rs.getInt("totalbreaknum");
				rankdata.p_vote = rs.getInt("p_vote");
				ranklist.add(rankdata);
				//SeichiAssist.allplayerbreakblockint += rankdata.totalbreaknum;
				  }
			rs.close();
		} catch (SQLException e) {
			java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
			exc = e.getMessage();
			e.printStackTrace();
			return false;
		}
		//plugin.getServer().getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "ランキング更新完了");
		//Util.sendEveryMessage(ChatColor.DARK_AQUA + "ランキング更新完了");
 		return true;
	}

	//ランキング表示用にプレミアムエフェクトポイントのカラムだけ全員分引っ張る
	public boolean setRanking_premiumeffectpoint() {
		//plugin.getServer().getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "ランキング更新中…");
		//Util.sendEveryMessage(ChatColor.DARK_AQUA + "ランキング更新中…");
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		List<RankData> ranklist = SeichiAssist.ranklist_premiumeffectpoint;
		ranklist.clear();
		//SeichiAssist.allplayerbreakblockint = 0;

		//SELECT `totalbreaknum` FROM `playerdata` WHERE 1 ORDER BY `playerdata`.`totalbreaknum` DESC
		String command = "select name,premiumeffectpoint from " + db + "." + table
				+ " order by premiumeffectpoint desc";
 		try{
			rs = stmt.executeQuery(command);
			while (rs.next()) {
				RankData rankdata = new RankData();
				rankdata.name = rs.getString("name");
				//rankdata.level = rs.getInt("level");
				//rankdata.totalbreaknum = rs.getInt("totalbreaknum");
				rankdata.premiumeffectpoint = rs.getInt("premiumeffectpoint");
				ranklist.add(rankdata);
				//SeichiAssist.allplayerbreakblockint += rankdata.totalbreaknum;
				  }
			rs.close();
		} catch (SQLException e) {
			java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
			exc = e.getMessage();
			e.printStackTrace();
			return false;
		}
		//plugin.getServer().getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "ランキング更新完了");
		//Util.sendEveryMessage(ChatColor.DARK_AQUA + "ランキング更新完了");
 		return true;
	}

	//プレイヤーレベル全リセット
	public boolean resetAllPlayerLevel(){
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String command = "update " + db + "." + table
				+ " set level = 1";
		return putCommand(command);
	}

	//プレイヤーのレベルと整地量をセット
	public boolean resetPlayerLevelandBreaknum(UUID uuid){
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String struuid = uuid.toString();
		PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		int level = playerdata.level;
		long totalbreaknum = playerdata.totalbreaknum;

		String command = "update " + db + "." + table
				+ " set"

				+ " level = " + Integer.toString(level)
				+ ",totalbreaknum = " + Long.toString(totalbreaknum);

		//最後の処理
		command = command + " where uuid like '" + struuid + "'";

		return putCommand(command);
	}

	//プレイヤーのレベルと整地量をセット(プレイヤーデータが無い場合)
	public boolean resetPlayerLevelandBreaknum(UUID uuid, int level){
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String struuid = uuid.toString();
		//PlayerData playerdata = SeichiAssist.playermap.get(uuid);
		int totalbreaknum = SeichiAssist.levellist.get(level-1);

		String command = "update " + db + "." + table
				+ " set"

				+ " level = " + Integer.toString(level)
				+ ",totalbreaknum = " + Integer.toString(totalbreaknum);

		//最後の処理
		command = command + " where uuid like '" + struuid + "'";

		return putCommand(command);
	}



	//全員に詫びガチャの配布
	public boolean addAllPlayerBug(int amount){
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String command = "update " + db + "." + table
				+ " set numofsorryforbug = numofsorryforbug + " + amount;
		return putCommand(command);
	}

	//指定プレイヤーの四次元ポケットの中身取得
	public Inventory selectInventory(UUID uuid){
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String struuid = uuid.toString();
		Inventory inventory = null;
		String command = "select inventory from " + db + "." + table
					+ " where uuid like '" + struuid + "'";
			try{
				rs = stmt.executeQuery(command);
				while (rs.next()) {
	 				inventory = BukkitSerialization.fromBase64(rs.getString("inventory").toString());
				  }
				rs.close();
			} catch (SQLException | IOException e) {
				java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
				exc = e.getMessage();
				e.printStackTrace();
				return null;
			}
		return inventory;
	}

	//指定プレイヤーのlastquitを取得
	public String selectLastQuit(String name){
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String lastquit = "";
		String command = "select lastquit from " + db + "." + table
				+ " where name = '" + name + "'";
		try{
			rs = stmt.executeQuery(command);
			while (rs.next()) {
				lastquit = rs.getString("lastquit");
			  }
			rs.close();
		} catch (SQLException e) {
			java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
			exc = e.getMessage();
			e.printStackTrace();
			return null;
		}
		return lastquit;
	}

	//lastquitがdays日以上(または未登録)のプレイヤー名を配列で取得
	public Map<UUID, String> selectLeavers(int days){
		Map<UUID, String> leavers = new HashMap<>();
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String command = "select name, uuid from " + db + "." + table
				+ " where ((lastquit <= date_sub(curdate(), interval " + Integer.toString(days) + " day))"
				+ " or (lastquit is null)) and (name != '') and (uuid != '')";
			try{
				rs = stmt.executeQuery(command);
				while (rs.next()) {
					try {
						//結果のStringをUUIDに変換
						UUID uuid = UUID.fromString(rs.getString("uuid"));
						if (leavers.containsKey(uuid)) {
							java.lang.System.out.println("playerdataにUUIDが重複しています: " + rs.getString("uuid"));
						} else {
							//HashMapにUUIDとnameを登録
							leavers.put(uuid, rs.getString("name"));
						}
					} catch (IllegalArgumentException e) {
						java.lang.System.out.println("不適切なUUID: " + rs.getString("name") + ": " + rs.getString("uuid"));
					}
				}
				rs.close();
			} catch (SQLException e) {
				java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
				exc = e.getMessage();
				e.printStackTrace();
				return null;
			}
		return leavers;
	}

	//
	public boolean addPremiumEffectBuy(PlayerData playerdata,
			ActiveSkillPremiumEffect effect) {
		String table = SeichiAssist.DONATEDATA_TABLENAME;
		//
		String command = "insert into " + db + "." + table
					+ " (playername,playeruuid,effectnum,effectname,usepoint,date) "
					+ "value("
					+ "'" + playerdata.name + "',"
					+ "'" + playerdata.uuid.toString() + "',"
					+ Integer.toString(effect.getNum()) + ","
					+ "'" + effect.getsqlName() + "',"
					+ Integer.toString(effect.getUsePoint()) + ","
					+ "cast( now() as datetime )"
					+ ")";
		return putCommand(command);
	}
	public boolean addDonate(String name,int point) {
		String table = SeichiAssist.DONATEDATA_TABLENAME;
		String command = "insert into " + db + "." + table
					+ " (playername,getpoint,date) "
					+ "value("
					+ "'" + name + "',"
					+ Integer.toString(point) + ","
					+ "cast( now() as datetime )"
					+ ")";
		return putCommand(command);
	}

	public boolean loadDonateData(PlayerData playerdata, Inventory inventory) {
		ItemStack itemstack;
		ItemMeta itemmeta;
		Material material;
		List<String> lore = new ArrayList<String>();
		int count = 0;
		ActiveSkillPremiumEffect effect[] = ActiveSkillPremiumEffect.values();

		String table = SeichiAssist.DONATEDATA_TABLENAME;
		String command = "select * from " + db + "." + table + " where playername = '" + playerdata.name + "'";
 		try{
			rs = stmt.executeQuery(command);
			while (rs.next()) {
				//ポイント購入の処理
				if(rs.getInt("getpoint")>0){
					itemstack = new ItemStack(Material.DIAMOND);
					itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND);
					itemmeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "寄付");
					lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "" + "金額：" + rs.getInt("getpoint")*100,
							ChatColor.RESET + "" +  ChatColor.GREEN + "" + "プレミアムエフェクトポイント：+" + rs.getInt("getpoint"),
							ChatColor.RESET + "" +  ChatColor.GREEN + "" + "日時：" + rs.getString("date")
							);
					itemmeta.setLore(lore);
					itemstack.setItemMeta(itemmeta);
					inventory.setItem(count,itemstack);
				}else if(rs.getInt("usepoint")>0){
					int num = rs.getInt("effectnum")-1;
					material = effect[num].getMaterial();
					itemstack = new ItemStack(material);
					itemmeta = Bukkit.getItemFactory().getItemMeta(material);
					itemmeta.setDisplayName(ChatColor.RESET + "" +  ChatColor.YELLOW + "購入エフェクト：" + effect[num].getName());
					lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GOLD + "" + "プレミアムエフェクトポイント： -" + rs.getInt("usepoint"),
							ChatColor.RESET + "" +  ChatColor.GOLD + "" + "日時：" + rs.getString("date")
							);
					itemmeta.setLore(lore);
					itemstack.setItemMeta(itemmeta);
					inventory.setItem(count,itemstack);
				}
				count ++;
			}
			rs.close();
		} catch (SQLException e) {
			java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
			exc = e.getMessage();
			e.printStackTrace();
			return false;
		}
 		return true;
	}

	public boolean saveShareInv(Player player, PlayerData playerdata, String data) {
		if (!playerdata.shareinvcooldownflag) {
			player.sendMessage(ChatColor.RED + "しばらく待ってからやり直してください");
			return false;
		}
		//連打による負荷防止の為クールダウン処理
		new CoolDownTaskRunnable(player, CoolDownTaskRunnable.SHAREINV).runTaskLater(plugin, 200);
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String struuid = playerdata.uuid.toString();
		String command = "SELECT shareinv FROM " + db + "." + table + " " +
				"WHERE uuid = '" + struuid + "'";
 		try {
			rs = stmt.executeQuery(command);
			rs.next();
			String shareinv = rs.getString("shareinv");
			rs.close();
			if (shareinv != "" && shareinv != null) {
				player.sendMessage(ChatColor.RED + "既にアイテムが収納されています");
				return false;
			}
			command = "UPDATE " + db + "." + table + " " +
					"SET shareinv = '" + data + "' " +
					"WHERE uuid = '" + struuid + "'";
			if (!putCommand(command)) {
				player.sendMessage(ChatColor.RED + "アイテムの収納に失敗しました");
				Bukkit.getLogger().warning(Util.getName(player) + " sql failed. -> saveShareInv(putCommand failed)");
				return false;
			}
 		} catch (SQLException e) {
			player.sendMessage(ChatColor.RED + "共有インベントリにアクセスできません");
			Bukkit.getLogger().warning(Util.getName(player) + " sql failed. -> clearShareInv(SQLException)");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public String loadShareInv(Player player, PlayerData playerdata) {
		if(!playerdata.shareinvcooldownflag){
			player.sendMessage(ChatColor.RED + "しばらく待ってからやり直してください");
			return null;
		}
		//連打による負荷防止の為クールダウン処理
		new CoolDownTaskRunnable(player,CoolDownTaskRunnable.SHAREINV).runTaskLater(plugin,200);
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String struuid = playerdata.uuid.toString();
		String command = "SELECT shareinv FROM " + db + "." + table + " " +
				"WHERE uuid = '" + struuid + "'";
		String shareinv = null;
 		try {
			rs = stmt.executeQuery(command);
			rs.next();
			shareinv = rs.getString("shareinv");
			rs.close();
 		} catch (SQLException e) {
			player.sendMessage(ChatColor.RED + "共有インベントリにアクセスできません");
			Bukkit.getLogger().warning(Util.getName(player) + " sql failed. -> loadShareInv");
			e.printStackTrace();
		}
		return shareinv;
	}

	public boolean clearShareInv(Player player, PlayerData playerdata) {
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String struuid = playerdata.uuid.toString();
		String command = "UPDATE " + db + "." + table + " " +
					"SET shareinv = '' " +
					"WHERE uuid = '" + struuid + "'";
		if (!putCommand(command)) {
			player.sendMessage(ChatColor.RED + "アイテムのクリアに失敗しました");
			Bukkit.getLogger().warning(Util.getName(player) + " sql failed. -> clearShareInv");
			return false;
		}
		return true;
	}

	/**
	 * 実績予約領域書き換え処理
	 *
	 * @param sender 発行Player
	 * @param targetName 対象Playerのname
	 * @param achvNo 対象実績No
	 * @return 成否…true: 成功、false: 失敗
	 */
	public boolean writegiveachvNo(Player sender, String targetName, String achvNo) {
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String select = "SELECT giveachvNo FROM " + db + "." + table + " " +
				"WHERE name LIKE '" + targetName + "'";
		String update = "UPDATE " + db + "." + table + " " +
 				" SET giveachvNo = " + achvNo +
 				" WHERE name LIKE '" + targetName + "'";

		// selectで確認
 		try {
			rs = stmt.executeQuery(select);
			// 初回のnextがnull→データが1件も無い場合
			if (!rs.next()) {
				sender.sendMessage(ChatColor.RED + "" + targetName + " はデータベースに登録されていません");
				return false;
			}
			// 現在予約されている値を取得
			int giveachvNo = rs.getInt("giveachvNo");
			// 既に予約がある場合
			if (giveachvNo != 0) {
				sender.sendMessage(ChatColor.RED + "" + targetName + " には既に実績No " + giveachvNo + " が予約されています");
				return false;
			}
			rs.close();
			// 実績を予約
			stmt.executeUpdate(update);
		} catch (SQLException e) {
			sender.sendMessage(ChatColor.RED + "実績の予約に失敗しました");
			Bukkit.getLogger().warning(Util.getName(sender) + " sql failed. -> writegiveachvNo");
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
