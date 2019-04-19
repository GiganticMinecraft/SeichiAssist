package com.github.unchama.seichiassist.database;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.database.init.DatabaseTableInitializer;
import com.github.unchama.seichiassist.database.manipulators.DonateDataManipulator;
import com.github.unchama.seichiassist.database.manipulators.GachaDataManipulator;
import com.github.unchama.seichiassist.database.manipulators.MineStackGachaDataManipulator;
import com.github.unchama.seichiassist.database.manipulators.PlayerDataManipulator;
import com.github.unchama.util.ActionStatus;
import com.github.unchama.util.failable.Try;
import com.github.unchama.util.Unit;
import com.github.unchama.util.failable.TryWithoutFailValue;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.sql.*;

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
    public final GachaDataManipulator gachaDataManipulator;
    public final MineStackGachaDataManipulator mineStackGachaDataManipulator;
    public final DonateDataManipulator donateDataManipulator;

	public Connection con = null;
	private Statement stmt = null;

	private SeichiAssist plugin = SeichiAssist.instance;

	private DatabaseGateway(@NotNull String databaseUrl, @NotNull String databaseName, @NotNull String loginId, @NotNull String password){
		this.databaseUrl = databaseUrl;
		this.databaseName = databaseName;
		this.loginId = loginId;
		this.password = password;

		this.playerDataManipulator = new PlayerDataManipulator(this);
        this.gachaDataManipulator = new GachaDataManipulator(this);
        this.mineStackGachaDataManipulator = new MineStackGachaDataManipulator(this);
        this.donateDataManipulator = new DonateDataManipulator(this);
    }

	public static DatabaseGateway createInitializedInstance(@NotNull String databaseUrl,
													 @NotNull String databaseName,
													 @NotNull String loginId,
													 @NotNull String password) {
	    final DatabaseGateway instance = new DatabaseGateway(databaseUrl, databaseName, loginId, password);
	    final DatabaseTableInitializer tableInitializer =
				new DatabaseTableInitializer(instance, instance.plugin.getLogger(), SeichiAssist.config);

	    final ActionStatus initializationStatus = TryWithoutFailValue
				.sequence(
					instance::connectToAndInitializeDatabase,
					tableInitializer::initializeTables
				)
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
				.sequence(
						Pair.of("Mysqlドライバーのインスタンス生成に失敗しました", () -> {
							try {
								Class.forName("com.mysql.jdbc.Driver").newInstance();
								return Ok;
							} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
								e.printStackTrace();
								return Fail;
							}
						}),
						Pair.of("SQL接続に失敗しました", this::establishMySQLConnection),
						Pair.of("データベース作成に失敗しました", this::createDB)
				)
				.mapFailed(failedMessage -> { plugin.getLogger().info(failedMessage); return Unit.instance; })
				.overallStatus();
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

}
