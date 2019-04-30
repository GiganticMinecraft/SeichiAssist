package com.github.unchama.seichiassist.database.migration.V1_1_0;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/* package-private */ class SubHomeMigration implements Migration {
    /* package-private */ SubHomeMigration() {}

    private static void copySubHomeColumns(final Connection connection, final String serverId) throws SQLException {
        final String homePointColumnName = "homepoint_" + serverId;
        final String subHomeNameColumnName = "subhome_name_" + serverId;
        final String selectQuery =
                "select uuid, " + homePointColumnName + ", " + subHomeNameColumnName + " from playerdata " +
                "where " + homePointColumnName + " is not null";

        final HashSet<SubHomeDTO> subHomes = new HashSet<>();
        try (final ResultSet homePointResultSet = connection.createStatement().executeQuery(selectQuery)) {
            while (homePointResultSet.next()) {
                final String uuid = homePointResultSet.getString("uuid");
                final @NotNull String homePointRawData = homePointResultSet.getString(homePointColumnName);
                final @Nullable String subHomeNameRawData = homePointResultSet.getString(subHomeNameColumnName);

                final SubHomeDTOParser parser = new SubHomeDTOParser(uuid, serverId);

                subHomes.addAll(parser.parseRawDataAndFilterUndefineds(homePointRawData, subHomeNameRawData));
            }
        }

        for (SubHomeDTO subHomeDTO : subHomes) {
            final String templateCommand = subHomeDTO.generateTemplateForInsertionCommand();
            try (final PreparedStatement statement = connection.prepareStatement(templateCommand)) {
                statement.setString(1, subHomeDTO.name);
                statement.execute();
            }
        }
    }

    private static void deleteSubHomeColumns(final Statement statement, final String serverId) throws SQLException {
        for (String baseTableName : Arrays.asList("homepoint", "subhome_name")) {
            statement.executeUpdate("alter table playerdata drop column " + baseTableName + "_" + serverId);
        }
    }

    private static void migrateSubHomeColumns(final Connection connection, final Set<String> serverIds) {
        serverIds.forEach(serverId -> {
            try (Statement statement = connection.createStatement()) {
                copySubHomeColumns(connection, serverId);
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
