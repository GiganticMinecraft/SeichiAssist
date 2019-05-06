package com.github.unchama.seichiassist.database.migration.V1_1_0;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class V1_1_0__Migrate_all_dynamic_columns extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        final Connection connection = context.getConnection();
        connection.createStatement().execute("use seichiassist");

        final HashSet<String> playerDataColumnNames = new HashSet<>();

        final ResultSet columnNamesResult = connection.createStatement().executeQuery("show columns from playerdata");
        while (columnNamesResult.next()) {
            playerDataColumnNames.add(columnNamesResult.getString("Field"));
        }

        final List<Migration> migrations = Arrays.asList(
                new MineStackMigration(),
                new GridTemplateMigration(),
                new SubHomeMigration(),
                new SkillEffectUnlockStateMigration()
        );

        migrations.forEach(migration -> migration.migrate(connection, playerDataColumnNames));
    }
}
