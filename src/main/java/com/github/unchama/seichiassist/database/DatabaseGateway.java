package com.github.unchama.seichiassist.database;

import com.github.unchama.seichiassist.ActiveSkillPremiumEffect;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.GachaData;
import com.github.unchama.seichiassist.data.MineStackGachaData;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.RankData;
import com.github.unchama.seichiassist.database.init.DatabaseTableInitializer;
import com.github.unchama.seichiassist.database.manipulators.PlayerDataManipulator;
import com.github.unchama.seichiassist.task.CheckAlreadyExistPlayerDataTaskRunnable;
import com.github.unchama.seichiassist.task.PlayerDataSaveTaskRunnable;
import com.github.unchama.seichiassist.util.BukkitSerialization;
import com.github.unchama.util.ActionStatus;
import com.github.unchama.util.Try;
import com.github.unchama.util.ValuelessTry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.*;
import java.util.*;

import static com.github.unchama.util.ActionStatus.Fail;
import static com.github.unchama.util.ActionStatus.Ok;

/**
 * データベースとのデータをやり取りするためのゲートウェイとして機能するオブジェクトのクラス
 */
public class DatabaseGateway {
    //TODO: 直接SQLに変数を連結しているが、順次PreparedStatementに置き換えていきたい

    private @NotNull final String databaseUrl;
	public @NotNull final String databaseName;
	private @NotNull final String loginId;
	private @NotNull final String password;

	// TODO これらはこのクラスに入るべきではなさそう
    public final PlayerDataManipulator playerDataManipulator;

	public Connection con = null;
	private Statement stmt = null;

	private SeichiAssist plugin = SeichiAssist.instance;

	private DatabaseGateway(@NotNull String databaseUrl, @NotNull String databaseName, @NotNull String loginId, @NotNull String password){
		this.databaseUrl = databaseUrl;
		this.databaseName = databaseName;
		this.loginId = loginId;
		this.password = password;

		this.playerDataManipulator = new PlayerDataManipulator(this);
	}

	public static DatabaseGateway createInitializedInstance(@NotNull String databaseUrl,
													 @NotNull String databaseName,
													 @NotNull String loginId,
													 @NotNull String password) {
	    final DatabaseGateway instance = new DatabaseGateway(databaseUrl, databaseName, loginId, password);
	    final DatabaseTableInitializer tableInitializer =
				new DatabaseTableInitializer(instance, instance.plugin.getLogger(), SeichiAssist.config);

	    final ActionStatus initializationStatus =
				ValuelessTry
					.begin(instance::connectToAndInitializeDatabase)
					.ifOkThen(tableInitializer::initializeTables)
					.overallStatus();

	    if (initializationStatus == Fail) {
	        instance.plugin.getLogger().info("データベース初期処理にエラーが発生しました");
        }

	    return instance;
    }

	/**
	 * 接続関数
	 */
	private ActionStatus connectToAndInitializeDatabase() {
		return Try
				.begin("Mysqlドライバーのインスタンス生成に失敗しました", () -> {
					try {
						Class.forName("com.mysql.jdbc.Driver").newInstance();
						return Ok;
					} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
						e.printStackTrace();
						return Fail;
					}
				})
				.ifOkThen("SQL接続に失敗しました", this::establishMySQLConnection)
				.ifOkThen("データベース作成に失敗しました", this::createDB)
				.mapFailValue(Ok, failedMessage -> { plugin.getLogger().info(failedMessage); return Fail; });
	}

	private ActionStatus establishMySQLConnection(){
		try {
			if(stmt != null && !stmt.isClosed()){
				stmt.close();
				con.close();
			}
			con = DriverManager.getConnection(databaseUrl, loginId, password);
			stmt = con.createStatement();
			return Ok;
		} catch (SQLException e) {
			e.printStackTrace();
			return Fail;
		}
	}

	/**
	 * 接続正常ならOk、そうでなければ再接続試行後正常でOk、だめならFailを返す
	 */
	// TODO このメソッドの戻り値はどこにも使われていない。異常系はその状態を引きずらずに処理を止めるべき
	public ActionStatus ensureConnection(){
		try {
			if(con.isClosed()){
				plugin.getLogger().warning("sqlConnectionクローズを検出。再接続試行");
				con = DriverManager.getConnection(databaseUrl, loginId, password);
			}
			if(stmt.isClosed()){
				plugin.getLogger().warning("sqlStatementクローズを検出。再接続試行");
				stmt = con.createStatement();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			//イクセプションった時に接続再試行
			plugin.getLogger().warning("sqlExceptionを検出。再接続試行");
			if(establishMySQLConnection() == Ok){
				plugin.getLogger().info("sqlコネクション正常");
				return Ok;
			}else{
				plugin.getLogger().warning("sqlコネクション不良を検出");
				return Fail;
			}
		}

		return Ok;
	}

	/**
	 * コネクション切断処理
	 *
	 * @return 成否
	 */
	public ActionStatus disconnect(){
		if (con != null) {
			try {
				stmt.close();
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
				return Fail;
			}
		}
		return Ok;
	}

	/**
	 * コマンド実行関数
	 * @param command コマンド内容
	 * @return 成否
	 */
	public ActionStatus executeUpdate(String command) {
		ensureConnection();
		try {
			stmt.executeUpdate(command);
			return Ok;
		}catch (SQLException e) {
			java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
			e.getMessage();
			e.printStackTrace();
			return Fail;
		}
	}

	public ResultSet executeQuery(String query) throws SQLException {
	    return stmt.executeQuery(query);
    }

	/**
	 * データベース作成
	 *
	 * @return 成否
	 */
	private ActionStatus createDB(){
		String command = "CREATE DATABASE IF NOT EXISTS " + databaseName
				+ " character set utf8 collate utf8_general_ci";
		return executeUpdate(command);
	}

	//ガチャデータロード
	public boolean loadGachaData(){
		final String tableReference = databaseName + "." + SeichiAssist.GACHADATA_TABLENAME;
		List<GachaData> gachadatalist = new ArrayList<>();

		String command = "select * from " + tableReference;
		try (ResultSet lrs = stmt.executeQuery(command)) {
			while (lrs.next()) {
				GachaData gachadata = new GachaData();
				Inventory inventory = BukkitSerialization.fromBase64(lrs.getString("itemstack"));
				gachadata.itemstack = (inventory.getItem(0));
				gachadata.amount = lrs.getInt("amount");
				gachadata.probability = lrs.getDouble("probability");
				gachadatalist.add(gachadata);
			}
		} catch (SQLException | IOException e) {
			java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
			e.printStackTrace();
			return false;
		}
		SeichiAssist.gachadatalist.clear();
		SeichiAssist.gachadatalist.addAll(gachadatalist);
		return true;

	}

	//MineStack用ガチャデータロード
	public boolean loadMineStackGachaData(){
		final String tableReference = databaseName + "." + SeichiAssist.MINESTACK_GACHADATA_TABLENAME;
		List<MineStackGachaData> gachadatalist = new ArrayList<>();

		String command = "select * from " + tableReference;
		try (ResultSet lrs = stmt.executeQuery(command)){
			while (lrs.next()) {
				MineStackGachaData gachadata = new MineStackGachaData();
				Inventory inventory = BukkitSerialization.fromBase64(lrs.getString("itemstack"));
				gachadata.itemstack = (inventory.getItem(0));
				gachadata.amount = lrs.getInt("amount");
				gachadata.level = lrs.getInt("level");
				gachadata.obj_name = lrs.getString("obj_name");
				gachadata.probability = lrs.getDouble("probability");
				gachadatalist.add(gachadata);
			}
		} catch (SQLException | IOException e) {
			java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
			e.printStackTrace();
			return false;
		}
		SeichiAssist.msgachadatalist.clear();
		SeichiAssist.msgachadatalist.addAll(gachadatalist);
		return true;
	}

	//ガチャデータセーブ
	public boolean saveGachaData(){
		String tableReference = databaseName + "." + SeichiAssist.GACHADATA_TABLENAME;

		//まずmysqlのガチャテーブルを初期化(中身全削除)
		String command = "truncate table " + tableReference;
		if(executeUpdate(command) == Fail){
			return false;
		}

		//次に現在のgachadatalistでmysqlを更新
		for(GachaData gachadata : SeichiAssist.gachadatalist){
			//Inventory作ってガチャのitemstackに突っ込む
			Inventory inventory = SeichiAssist.instance.getServer().createInventory(null, 9*1);
			inventory.setItem(0,gachadata.itemstack);

			command = "insert into " + tableReference + " (probability,amount,itemstack)"
					+ " values"
					+ "(" + gachadata.probability
					+ "," + gachadata.amount
					+ ",'" + BukkitSerialization.toBase64(inventory) + "'"
					+ ")";
			if(executeUpdate(command) == Fail){
				return false;
			}
		}
		return true;
	}

	//MineStack用ガチャデータセーブ
	public boolean saveMineStackGachaData(){
		String tableReference = databaseName + "." + SeichiAssist.MINESTACK_GACHADATA_TABLENAME;

		//まずmysqlのガチャテーブルを初期化(中身全削除)
		String command = "truncate table " + tableReference;
		if(executeUpdate(command) == Fail){
			return false;
		}

		//次に現在のgachadatalistでmysqlを更新
		for(MineStackGachaData gachadata : SeichiAssist.msgachadatalist){
			//Inventory作ってガチャのitemstackに突っ込む
			Inventory inventory = SeichiAssist.instance.getServer().createInventory(null, 9*1);
			inventory.setItem(0,gachadata.itemstack);

			command = "insert into " + tableReference + " (probability,amount,level,obj_name,itemstack)"
					+ " values"
					+ "(" + gachadata.probability
					+ "," + gachadata.amount
					+ "," + gachadata.level
					+ ",'" + gachadata.obj_name + "'"
					+ ",'" + BukkitSerialization.toBase64(inventory) + "'"
					+ ")";

			if (executeUpdate(command) == Fail) {
				return false;
			}
		}
		return true;
	}

	public ActionStatus addPremiumEffectBuy(PlayerData playerdata,
											ActiveSkillPremiumEffect effect) {
		String tableReference = databaseName + "." + SeichiAssist.DONATEDATA_TABLENAME;
		String command = "insert into " + tableReference
				+ " (playername,playeruuid,effectnum,effectname,usepoint,date) "
				+ "value("
				+ "'" + playerdata.name + "',"
				+ "'" + playerdata.uuid.toString() + "',"
				+ effect.getNum() + ","
				+ "'" + effect.getsqlName() + "',"
				+ effect.getUsePoint() + ","
				+ "cast( now() as datetime )"
				+ ")";

		return executeUpdate(command);
	}

	public ActionStatus addDonate(String name, int point) {
		String tableReference = databaseName + "." + SeichiAssist.DONATEDATA_TABLENAME;
		String command = "insert into " + tableReference
				+ " (playername,getpoint,date) "
				+ "value("
				+ "'" + name + "',"
				+ point + ","
				+ "cast( now() as datetime )"
				+ ")";
		return executeUpdate(command);
	}

	public boolean loadDonateData(PlayerData playerdata, Inventory inventory) {
		ItemStack itemstack;
		ItemMeta itemmeta;
		Material material;
		List<String> lore;
		int count = 0;
		ActiveSkillPremiumEffect[] effect = ActiveSkillPremiumEffect.values();

		String tableReference = databaseName + "." + SeichiAssist.DONATEDATA_TABLENAME;
		String command = "select * from " + tableReference + " where playername = '" + playerdata.name + "'";
		try (ResultSet lrs = stmt.executeQuery(command)){
			while (lrs.next()) {
				//ポイント購入の処理
				if(lrs.getInt("getpoint")>0){
					itemstack = new ItemStack(Material.DIAMOND);
					itemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND);
					itemmeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "寄付");
					lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GREEN + "" + "金額：" + lrs.getInt("getpoint")*100,
							ChatColor.RESET + "" +  ChatColor.GREEN + "" + "プレミアムエフェクトポイント：+" + lrs.getInt("getpoint"),
							ChatColor.RESET + "" +  ChatColor.GREEN + "" + "日時：" + lrs.getString("date")
					);
					itemmeta.setLore(lore);
					itemstack.setItemMeta(itemmeta);
					inventory.setItem(count,itemstack);
				}else if(lrs.getInt("usepoint")>0){
					int num = lrs.getInt("effectnum")-1;
					material = effect[num].getMaterial();
					itemstack = new ItemStack(material);
					itemmeta = Bukkit.getItemFactory().getItemMeta(material);
					itemmeta.setDisplayName(ChatColor.RESET + "" +  ChatColor.YELLOW + "購入エフェクト：" + effect[num].getName());
					lore = Arrays.asList(ChatColor.RESET + "" +  ChatColor.GOLD + "" + "プレミアムエフェクトポイント： -" + lrs.getInt("usepoint"),
							ChatColor.RESET + "" +  ChatColor.GOLD + "" + "日時：" + lrs.getString("date")
					);
					itemmeta.setLore(lore);
					itemstack.setItemMeta(itemmeta);
					inventory.setItem(count,itemstack);
				}
				count ++;
			}
		} catch (SQLException e) {
			java.lang.System.out.println("sqlクエリの実行に失敗しました。以下にエラーを表示します");
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
