package com.github.unchama.seichiassist;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.github.unchama.seichiassist.data.GachaData;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.RankData;
import com.github.unchama.seichiassist.task.LoadPlayerDataTask;
import com.github.unchama.seichiassist.util.BukkitSerialization;
import com.github.unchama.seichiassist.util.Util;

//MySQL操作関数
public class Sql{
	private final String url, db, id, pw;
	public Connection con = null;
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
		if(!connectDB()){
			plugin.getLogger().info("データベース接続に失敗しました");
			return false;
		}
		if(!createPlayerDataTable(SeichiAssist.PLAYERDATA_TABLENAME)){
			plugin.getLogger().info("playerdataテーブル作成に失敗しました");
			return false;
		}

		if(!createGachaDataTable(SeichiAssist.GACHADATA_TABLENAME)){
			plugin.getLogger().info("gachadataテーブル作成に失敗しました");
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
	private boolean putCommand(String command){
		try {
			stmt.executeUpdate(command);
			return true;
		} catch (SQLException e) {
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

	private boolean connectDB() {
		String command;
		command = "use " + db;
		return putCommand(command);
	}

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
				"CREATE TABLE IF NOT EXISTS " + table +
				"(name varchar(30) unique," +
				"uuid varchar(128) unique)";
		if(!putCommand(command)){
			return false;
		}
		//必要なcolumnを随時追加
		command =
				"alter table " + table +
				" add column if not exists effectflag boolean default true" +
				",add column if not exists minestackflag boolean default true" +
				",add column if not exists messageflag boolean default false" +
				",add column if not exists activemineflagnum int default 0" +
				",add column if not exists activeskilltype int default 0" +
				",add column if not exists activeskillnum int default 1" +
				",add column if not exists arrowskill int default 0" +
				",add column if not exists multiskill int default 0" +
				",add column if not exists breakskill int default 0" +
				",add column if not exists condenskill int default 0" +
				",add column if not exists effect_explosion boolean default false" +
				",add column if not exists effect_blizzard boolean default false" +
				",add column if not exists effect_meteo boolean default false" +
				",add column if not exists effectnum int default 0" +
				",add column if not exists gachapoint int default 0" +
				",add column if not exists gachaflag boolean default true" +
				",add column if not exists level int default 1" +
				",add column if not exists numofsorryforbug int default 0" +
				",add column if not exists inventory blob default null" +
				",add column if not exists rgnum int default 0" +
				",add column if not exists totalbreaknum int default 0" +
				",add column if not exists lastquit datetime default null" +
				",add column if not exists stack_dirt int default 0" +
				",add column if not exists stack_gravel int default 0" +
				",add column if not exists stack_cobblestone int default 0" +
				",add column if not exists stack_stone int default 0" +
				",add column if not exists stack_sand int default 0" +
				",add column if not exists stack_sandstone int default 0" +
				",add column if not exists stack_netherrack int default 0" +
				",add column if not exists stack_ender_stone int default 0" +
				",add column if not exists stack_grass int default 0" +
				",add column if not exists stack_quartz int default 0" +
				",add column if not exists stack_quartz_ore int default 0" +
				",add column if not exists stack_soul_sand int default 0" +
				",add column if not exists stack_magma int default 0" +
				",add column if not exists stack_coal int default 0" +
				",add column if not exists stack_coal_ore int default 0" +
				",add column if not exists stack_iron_ore int default 0" +
				",add column if not exists stack_packed_ice int default 0" +
				",add column if not exists playtick int default 0" +
				",add column if not exists killlogflag boolean default false" +
				",add column if not exists pvpflag boolean default false" +
				",add column if not exists loginflag boolean default false" +
				",add column if not exists p_vote int default 0" +
				",add column if not exists p_givenvote int default 0" +
				",add column if not exists effectpoint int default 0" +
				",add index if not exists name_index(name)" +
				",add index if not exists uuid_index(uuid)" +
				",add index if not exists ranking_index(totalbreaknum)" +
				"";
		return putCommand(command);
	}

	public boolean createGachaDataTable(String table){
		if(table==null){
			return false;
		}
		//テーブルが存在しないときテーブルを新規作成
		String command =
				"CREATE TABLE IF NOT EXISTS " + table +
				"(id int auto_increment unique,"
				+ "amount int(11))";
		if(!putCommand(command)){
			return false;
		}
		//必要なcolumnを随時追加
		command =
				"alter table " + table +
				" add column if not exists probability double default 0.0" +
				",add column if not exists itemstack blob default null" +
				"";
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
 		command = "select count(*) as count from " + table
 				+ " where uuid = '" + struuid + "'";
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
 			command = "insert into " + table
 	 				+ " (name,uuid,loginflag) values('" + name
 	 				+ "','" + struuid+ "','1')";
 			if(!putCommand(command)){
 				return false;
 			}
 			//PlayerDataを新規作成
 			playermap.put(uuid, new PlayerData(p));
 			return true;

 		}else if(count == 1){
 			//uuidが存在するときの処理
 			if(SeichiAssist.DEBUG){
 				p.sendMessage("sqlにデータが保存されています。");
 			}
 			new LoadPlayerDataTask(p).runTaskTimer(plugin, 0, 10);;
 			return true;

 		}else{
 			//mysqlに該当するplayerdataが2個以上ある時エラーを吐く
 			Bukkit.getLogger().info(Util.getName(p) + "のplayerdata読込時に原因不明のエラー発生");
 			return false;
 		}
	}
	public boolean savePlayerData(PlayerData playerdata) {
		//引数のplayerdataをsqlにデータを送信

		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String struuid = playerdata.uuid.toString();
		String command = "";

		command = "update " + table
				+ " set"

				//名前更新処理
				+ " name = '" + playerdata.name + "'"

				//各種数値更新処理
				+ ",effectflag = " + Boolean.toString(playerdata.effectflag)
				+ ",minestackflag = " + Boolean.toString(playerdata.minestackflag)
				+ ",messageflag = " + Boolean.toString(playerdata.messageflag)
				+ ",activemineflagnum = " + Integer.toString(playerdata.activeskilldata.mineflagnum)
				+ ",activenum = " + Integer.toString(playerdata.activeskilldata.skilltype)
				+ ",activenum = " + Integer.toString(playerdata.activeskilldata.skillnum)
				+ ",arrowskill = " + Integer.toString(playerdata.activeskilldata.arrowskill)
				+ ",multiskill = " + Integer.toString(playerdata.activeskilldata.multiskill)
				+ ",breakskill = " + Integer.toString(playerdata.activeskilldata.breakskill)
				+ ",condenskill = " + Integer.toString(playerdata.activeskilldata.condenskill)
				+ ",effect_explosion = " + Boolean.toString(playerdata.activeskilldata.effect_explosion)
				+ ",effect_blizzard = " + Boolean.toString(playerdata.activeskilldata.effect_blizzard)
				+ ",effect_meteo = " + Boolean.toString(playerdata.activeskilldata.effect_meteo)
				+ ",effectnum = " + Integer.toString(playerdata.activeskilldata.effectnum)
				+ ",gachapoint = " + Integer.toString(playerdata.gachapoint)
				+ ",gachaflag = " + Boolean.toString(playerdata.gachaflag)
				+ ",level = " + Integer.toString(playerdata.level)
				+ ",numofsorryforbug = " + Integer.toString(playerdata.numofsorryforbug)
				+ ",rgnum = " + Integer.toString(playerdata.rgnum)
				+ ",totalbreaknum = " + Integer.toString(playerdata.totalbreaknum)
				+ ",inventory = '" + BukkitSerialization.toBase64(playerdata.inventory) + "'"
				+ ",playtick = " + Integer.toString(playerdata.playtick)
				+ ",lastquit = cast( now() as datetime )"
				+ ",killlogflag = " + Boolean.toString(playerdata.dispkilllogflag)
				+ ",pvpflag = " + Boolean.toString(playerdata.pvpflag)
				+ ",p_vote = " + Integer.toString(playerdata.p_vote)
				+ ",effectpoint = " + Integer.toString(playerdata.effectpoint)

				//MineStack機能の数値更新処理
				+ ",stack_dirt = " + Integer.toString(playerdata.minestack.dirt)
				+ ",stack_gravel = " + Integer.toString(playerdata.minestack.gravel)
				+ ",stack_cobblestone = " + Integer.toString(playerdata.minestack.cobblestone)
				+ ",stack_stone = " + Integer.toString(playerdata.minestack.stone)
				+ ",stack_sand = " + Integer.toString(playerdata.minestack.sand)
				+ ",stack_sandstone = " + Integer.toString(playerdata.minestack.sandstone)
				+ ",stack_netherrack = " + Integer.toString(playerdata.minestack.netherrack)
				+ ",stack_ender_stone = " + Integer.toString(playerdata.minestack.ender_stone)
				+ ",stack_grass = " + Integer.toString(playerdata.minestack.grass)
				+ ",stack_quartz = " + Integer.toString(playerdata.minestack.quartz)
				+ ",stack_quartz_ore = " + Integer.toString(playerdata.minestack.quartz_ore)
				+ ",stack_soul_sand = " + Integer.toString(playerdata.minestack.soul_sand)
				+ ",stack_magma = " + Integer.toString(playerdata.minestack.magma)
				+ ",stack_coal = " + Integer.toString(playerdata.minestack.coal)
				+ ",stack_coal_ore = " + Integer.toString(playerdata.minestack.coal_ore)
				+ ",stack_iron_ore = " + Integer.toString(playerdata.minestack.iron_ore)
				+ ",stack_packed_ice = " + Integer.toString(playerdata.minestack.packed_ice)

				+ " where uuid like '" + struuid + "'";

		return putCommand(command);
	}


	//loginflagのフラグ折る処理(ondisable時とquit時に実行させる)
	public boolean logoutPlayerData(PlayerData playerdata) {
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String struuid = playerdata.uuid.toString();
		String command = "";

		command = "update " + table
				+ " set"

				//ログインフラグ折る
				+ " loginflag = false"

				+ " where uuid like '" + struuid + "'";

		return putCommand(command);

	}

	//ガチャデータロード
	public boolean loadGachaData(){
		String table = SeichiAssist.GACHADATA_TABLENAME;
		List<GachaData> gachadatalist = new ArrayList<GachaData>();
		//SELECT `totalbreaknum` FROM `playerdata` WHERE 1 ORDER BY `playerdata`.`totalbreaknum` DESC
		String command = "select * from " + table;
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

	//ガチャデータセーブ
	public boolean saveGachaData(){
		String table = SeichiAssist.GACHADATA_TABLENAME;

		//まずmysqlのガチャテーブルを初期化(中身全削除)
		String command = "truncate table " + table;
		if(!putCommand(command)){
			return false;
		}

		//次に現在のgachadatalistでmysqlを更新
		for(GachaData gachadata : SeichiAssist.gachadatalist){
			//Inventory作ってガチャのitemstackに突っ込む
			Inventory inventory = SeichiAssist.plugin.getServer().createInventory(null, 9*1);
			inventory.setItem(0,gachadata.itemstack);

			command = "insert into " + table + " (probability,amount,itemstack)"
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

	//ランキング表示用に総破壊ブロック数のカラムだけ全員分引っ張る
	public boolean setRanking() {
		plugin.getServer().getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "ランキング更新中…");
		Util.sendEveryMessage(ChatColor.DARK_AQUA + "ランキング更新中…");
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		List<RankData> ranklist = SeichiAssist.ranklist;
		ranklist.clear();
		SeichiAssist.allplayerbreakblockint = 0;

		//SELECT `totalbreaknum` FROM `playerdata` WHERE 1 ORDER BY `playerdata`.`totalbreaknum` DESC
		String command = "select name,level,totalbreaknum from " + table
				+ " order by totalbreaknum desc";
 		try{
			rs = stmt.executeQuery(command);
			while (rs.next()) {
				RankData rankdata = new RankData();
				rankdata.name = rs.getString("name");
				rankdata.level = rs.getInt("level");
				rankdata.totalbreaknum = rs.getInt("totalbreaknum");
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
		plugin.getServer().getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "ランキング更新完了");
		Util.sendEveryMessage(ChatColor.DARK_AQUA + "ランキング更新完了");
 		return true;
	}

	//プレイヤーレベル全リセット
	public boolean resetAllPlayerLevel(){
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String command = "update " + table
				+ " set level = 1";
		return putCommand(command);
	}

	//全員に詫びガチャの配布
	public boolean addAllPlayerBug(int amount){
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String command = "update " + table
				+ " set numofsorryforbug = numofsorryforbug + " + amount;
		return putCommand(command);
	}

	//指定されたプレイヤーにガチャ券を送信する
	public boolean addPlayerBug(UUID uuid,int num) {
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String struuid = uuid.toString();
		String command = "update " + table
				+ " set numofsorryforbug = numofsorryforbug + " + num
				+ " where uuid like '" + struuid + "'";
		return putCommand(command);
	}

	//指定プレイヤーの四次元ポケットの中身取得
	public Inventory selectInventory(UUID uuid){
		String table = SeichiAssist.PLAYERDATA_TABLENAME;
		String struuid = uuid.toString();
		Inventory inventory = null;
		String command = "select inventory from " + table
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

}