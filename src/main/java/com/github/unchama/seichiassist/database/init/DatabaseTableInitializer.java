package com.github.unchama.seichiassist.database.init;

import com.github.unchama.seichiassist.Config;
import com.github.unchama.seichiassist.database.DatabaseGateway;
import com.github.unchama.seichiassist.database.DatabaseConstants;
import com.github.unchama.seichiassist.database.init.ddl.DonateDataTableQueryGenerator;
import com.github.unchama.seichiassist.database.init.ddl.GachaDataTableQueryGenerator;
import com.github.unchama.seichiassist.database.init.ddl.MineStackGachaDataTableQueryGenerator;
import com.github.unchama.seichiassist.database.init.ddl.PlayerDataTableQueryGenerator;
import com.github.unchama.util.ActionStatus;
import com.github.unchama.util.Try;
import com.github.unchama.util.ValuelessTry;

import java.util.function.Function;
import java.util.logging.Logger;

import static com.github.unchama.util.ActionStatus.Fail;
import static com.github.unchama.util.ActionStatus.Ok;

public class DatabaseTableInitializer {
    private final DatabaseGateway gateway;
    private final Logger logger;
    private final Config config;

    public DatabaseTableInitializer(DatabaseGateway gateway, Logger logger, Config config) {
        this.gateway = gateway;
        this.logger = logger;
        this.config = config;
    }

    public ActionStatus initializeTables() {
        final Function<String, String> errorMessageForTable = (tableName) -> tableName + "テーブル作成に失敗しました";

        final Try<String> tryResult =
                Try.begin(errorMessageForTable.apply("gachadata"), this::createGachaDataTable)
                        .ifOkThen(errorMessageForTable.apply("MineStack用gachadata"), this::createMineStackGachaDataTable)
                        .ifOkThen(errorMessageForTable.apply("donatedata"), this::createDonateDataTable)
                        .ifOkThen(errorMessageForTable.apply("playerdata"), this::createPlayerDataTable);

        return tryResult.mapFailValue(Ok, failedMessage -> { logger.info(failedMessage); return Fail; });
    }

    /**
     * playerdataテーブルの作成及び初期化を行うメソッド。
     *
     * @return 成否
     */
    private ActionStatus createPlayerDataTable(){
        final String tableName = DatabaseConstants.PLAYERDATA_TABLENAME;
        final String tableReference = gateway.databaseName + "." + tableName;

        final PlayerDataTableQueryGenerator queryGenerator =
                new PlayerDataTableQueryGenerator(tableReference, config);

        return ValuelessTry
                .begin(() -> gateway.executeQuery(queryGenerator.generateCreateQuery()))
                .ifOkThen(() -> gateway.executeQuery(queryGenerator.generateAdditionalColumnAlterQuery()))
                .overallStatus();
    }

    /**
     * gachadataテーブルの作成及び初期化を行うメソッド。
     *
     * @return 成否
     */
    private ActionStatus createGachaDataTable() {
        final String tableName = DatabaseConstants.GACHADATA_TABLENAME;
        final String tableReference = gateway.databaseName + "." + tableName;

        final GachaDataTableQueryGenerator queryGenerator =
                new GachaDataTableQueryGenerator(tableReference);

        return ValuelessTry
                .begin(() -> gateway.executeQuery(queryGenerator.generateCreateQuery()))
                .ifOkThen(() -> gateway.executeQuery(queryGenerator.generateAdditionalColumnAlterQuery()))
                .overallStatus();
    }

    /**
     * minestackテーブルの作成及び初期化を行うメソッド。
     *
     * @return 成否
     */
    private ActionStatus createMineStackGachaDataTable(){
        final String tableName = DatabaseConstants.MINESTACK_GACHADATA_TABLENAME;
        final String tableReference = gateway.databaseName + "." + tableName;

        final MineStackGachaDataTableQueryGenerator queryGenerator =
                new MineStackGachaDataTableQueryGenerator(tableReference);

        return ValuelessTry
                .begin(() -> gateway.executeQuery(queryGenerator.generateCreateQuery()))
                .ifOkThen(() -> gateway.executeQuery(queryGenerator.generateAdditionalColumnAlterQuery()))
                .overallStatus();
    }

    private ActionStatus createDonateDataTable() {
        final String tableName = DatabaseConstants.DONATEDATA_TABLENAME;
        final String tableReference = gateway.databaseName + "." + tableName;

        final DonateDataTableQueryGenerator queryGenerator =
                new DonateDataTableQueryGenerator(tableReference);

        return ValuelessTry
                .begin(() -> gateway.executeQuery(queryGenerator.generateCreateQuery()))
                .ifOkThen(() -> gateway.executeQuery(queryGenerator.generateAdditionalColumnAlterQuery()))
                .overallStatus();
    }

}
