package com.github.unchama.seichiassist.database.migration.V1_1_0;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.unchama.seichiassist.util.TypeConverter.isParsableToInteger;

/* package-private */ class GridTemplateMigration implements Migration {
    /* package-private */ GridTemplateMigration() {}

    private static void migrateGridTemplateColumn(final Connection connection, final int gridTemplateColumnCount) {
        IntStream.range(0, gridTemplateColumnCount).forEach(templateId -> {
            try (Statement statement = connection.createStatement()) {
                // コピー
                final String copyQuery = "insert into grid_template" +
                        "(id, designer_uuid, ahead_length, behind_length, right_length, left_length) " +
                        "select " + templateId + " as id, uuid as designer_uuid, " +
                        "ahead_"  + templateId + " as ahead_length, " +
                        "behind_" + templateId + " as behind_length, " +
                        "right_"  + templateId + " as right_length, " +
                        "left_"   + templateId + " as left_length from playerdata where " +
                        "ahead_"  + templateId + " != 0 or " +
                        "behind_" + templateId + " != 0 or " +
                        "right_"  + templateId + " != 0 or " +
                        "left_"   + templateId + " != 0";

                statement.executeUpdate(copyQuery);

                // 削除
                final List<String> deleteQueries = Stream.of("ahead", "behind", "left", "right")
                        .map(direction -> "alter table playerdata drop column " + direction + "_" + templateId)
                        .collect(Collectors.toList());

                for (String deleteQuery : deleteQueries) {
                    statement.executeQuery(deleteQuery);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void migrate(Connection connection, Set<String> playerDataColumnNames) {
        /*
         * ahead_i, behind_i, left_i, right_iのインデックスが必ず同時に生成されており、
         * 非負整数i, j, i < jに関してahead_jが存在するならahead_iも存在するという
                * 仮定に基づいたロジックである。
         */
        final long gridTemplateColumnCount = playerDataColumnNames.stream()
                .filter(tableName ->
                        tableName.startsWith("ahead_") &&
                                isParsableToInteger(tableName.substring("ahead_".length())))
                .count();
        migrateGridTemplateColumn(connection, (int) gridTemplateColumnCount);
    }
}
