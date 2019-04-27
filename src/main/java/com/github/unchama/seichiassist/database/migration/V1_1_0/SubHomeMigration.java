package com.github.unchama.seichiassist.database.migration.V1_1_0;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/* package-private */ class SubHomeMigration implements Migration {
    /* package-private */ SubHomeMigration() {}

    private static void copySubHomeColumns(final Statement statement, final String serverId) throws SQLException {
        final String homePointColumnName = "homepoint_" + serverId;
        final String subHomeNameColumnName = "subhome_name_" + serverId;
        final String selectQuery =
                "select uuid, " + homePointColumnName + ", " + subHomeNameColumnName + " from playerdata";

        final ResultSet homePointDataRow = statement.executeQuery(selectQuery);

        while (homePointDataRow.next()) {
            final String uuid = homePointDataRow.getString("uuid");
            final String homePointRawData = homePointDataRow.getString(homePointColumnName);
            final String subHomeNameRawData = homePointDataRow.getString(subHomeNameColumnName);

            final SubHomeDTOParser parser = new SubHomeDTOParser(uuid, serverId);
            for (SubHomeDTO subHomeDTO : parser.parseRawDataAndFilterUndefineds(homePointRawData, subHomeNameRawData)) {
                statement.executeQuery(subHomeDTO.generateSingletonInsertionQuery());
            }
        }
    }

    private static void deleteSubHomeColumns(final Statement statement, final String serverId) throws SQLException {
        for (String baseTableName : Arrays.asList("homepoint", "subhome_name")) {
            statement.executeQuery("alter table playerdata drop column " + baseTableName + "_" + serverId);
        }
    }

    private static void migrateSubHomeColumns(final Connection connection, final Set<String> serverIds) {
        serverIds.forEach(serverId -> {
            try (Statement statement = connection.createStatement()) {
                copySubHomeColumns(statement, serverId);
                deleteSubHomeColumns(statement, serverId);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void migrate(Connection connection, Set<String> playerDataColumnNames) {
        /*
         * homepoint_iとsubhome_name_iが必ず同時に生成されているという仮定に基づいたロジックである。
         */
        final Set<String> serverIds = playerDataColumnNames.stream()
                .filter(tableName -> tableName.startsWith("homepoint_"))
                .map(tableName -> tableName.substring("homepoint_".length()))
                .collect(Collectors.toSet());
        migrateSubHomeColumns(connection, serverIds);
    }
}
