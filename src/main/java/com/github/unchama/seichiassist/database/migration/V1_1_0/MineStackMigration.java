package com.github.unchama.seichiassist.database.migration.V1_1_0;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.stream.Collectors;

/* package-private */ class MineStackMigration implements Migration {
    /* package-private */ MineStackMigration() {}

    private static void migrateMineStackColumns(final Connection connection, final Set<String> targetObjectNames) {
        targetObjectNames.forEach(mineStackObjectName -> {
            try (Statement statement = connection.createStatement()) {
                final String copyQuery = "insert into mine_stack(player_uuid, object_name, amount) select " +
                        "uuid as player_uuid, " + mineStackObjectName + " as object_name " +
                        "stack_" + mineStackObjectName + " as amount from playerdata";

                statement.executeUpdate(copyQuery);

                // 削除
                statement.executeUpdate("alter table playerdata drop column stack_" + mineStackObjectName);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void migrate(Connection connection, Set<String> playerDataColumnNames) {
        final Set<String> mineStackObjectNames = playerDataColumnNames.stream()
                .filter(tableName -> tableName.startsWith("stack_"))
                .map(tableName -> tableName.substring("stack_".length()))
                .collect(Collectors.toSet());
        migrateMineStackColumns(connection, mineStackObjectNames);
    }
}
