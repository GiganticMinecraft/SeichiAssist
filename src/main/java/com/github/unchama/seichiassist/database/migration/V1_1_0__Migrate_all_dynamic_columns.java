package com.github.unchama.seichiassist.database.migration;

import com.github.unchama.seichiassist.ActiveSkillEffect;
import com.github.unchama.seichiassist.ActiveSkillPremiumEffect;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.unchama.seichiassist.util.TypeConverter.isParsableToInteger;

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

    /**
     * 1.1.0におけるマイグレーションタスクの抽象インターフェース
     */
    private interface Migration {
        /**
         * マイグレーションを行う。
         * @param connection 使用するConnectionオブジェクト
         * @param playerDataColumnNames seichiassist.playerdataのカラム名すべてを含むSet
         */
        void migrate(final Connection connection, final Set<String> playerDataColumnNames);
    }

    private static class GridTemplateMigration implements Migration {

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

                    for (String deleteCommand : deleteQueries) {
                        statement.executeUpdate(deleteCommand);
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
            System.out.println(gridTemplateColumnCount + " tables to migrate for grid template.");
            migrateGridTemplateColumn(connection, (int) gridTemplateColumnCount);
        }
    }

    private static class MineStackMigration implements Migration {
        private static void migrateMineStackColumns(final Connection connection, final Set<String> targetObjectNames) {
            targetObjectNames.forEach(mineStackObjectName -> {
                try (Statement statement = connection.createStatement()) {
                    final String copyQuery = "insert into mine_stack(player_uuid, object_name, amount) select " +
                            "uuid as player_uuid, '" + mineStackObjectName + "' as object_name, " +
                            "stack_" + mineStackObjectName + " as amount from playerdata " +
                            "where stack_" + mineStackObjectName + " != 0";

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

    private static class SkillEffectUnlockStateMigration implements Migration {
        private static void migrateEffectUnlockStateColumn(final Connection connection,
                                                           final String newTableName,
                                                           final Set<String> effectColumnNames) {
            effectColumnNames.forEach(effectName -> {
                try (Statement statement = connection.createStatement()) {
                    final String copyCommand = "insert ignore into " + newTableName + "(player_uuid, effect_name) " +
                            "select uuid as player_uuid, '" + effectName + "' as effect_name from playerdata " +
                            "where " +effectName;
                    statement.executeUpdate(copyCommand);

                    // 削除
                    statement.executeUpdate("alter table playerdata drop column " + effectName);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }

        private static void migrateActiveSkillEffect(final Connection connection,
                                                     Set<String> activeSkillEffectNames) {
            migrateEffectUnlockStateColumn(
                    connection, "unlocked_active_skill_effect", activeSkillEffectNames);
        }

        private static void migrateActiveSkillPremiumEffect(final Connection connection,
                                                            Set<String> activeSkillPremiumEffectNames) {
            migrateEffectUnlockStateColumn(
                    connection, "unlocked_active_skill_premium_effect", activeSkillPremiumEffectNames);
        }

        @Override
        public void migrate(Connection connection, Set<String> playerDataColumnNames) {
            /*
             * マイグレーションが実行される環境において、
             * ActiveSkillEffect及びActiveSkillPremiumEffectのenumに入っている値に対応するカラムのみが
             * DBに格納されているという仮定に基づいたロジックである。
             *
             * カラム名がそもそもenumのパラメータにより指定されているためこうする他無い。
             */
            final Set<String> activeSkillEffectNames = playerDataColumnNames
                    .stream()
                    .filter(columnName -> ActiveSkillEffect.fromSqlName(columnName) != null)
                    .collect(Collectors.toSet());
            migrateActiveSkillEffect(connection, activeSkillEffectNames);

            final Set<String> activeSkillPremiumEffectNames = playerDataColumnNames
                    .stream()
                    .filter(columnName -> ActiveSkillPremiumEffect.fromSqlName(columnName) != null)
                    .collect(Collectors.toSet());
            migrateActiveSkillPremiumEffect(connection, activeSkillPremiumEffectNames);
        }
    }

    /**
     * サブホームの情報を含むData Transfer Objectのクラス
     */
    private static class SubHomeDTO {
        private final @NotNull String id;
        private final @NotNull String playerUuid;
        private final @NotNull String serverId;
        public final @Nullable String name;
        private final @NotNull String xCoordinate;
        private final @NotNull String yCoordinate;
        private final @NotNull String zCoordinate;
        private final @NotNull String worldName;

        SubHomeDTO(@NotNull String id,
                   @NotNull String playerUuid,
                   @NotNull String serverId,
                   @Nullable String name,
                   @NotNull String xCoordinate,
                   @NotNull String yCoordinate,
                   @NotNull String zCoordinate,
                   @NotNull String worldName) {
            this.id = id;
            this.playerUuid = playerUuid;
            this.serverId = serverId;
            this.name = name;
            this.xCoordinate = xCoordinate;
            this.yCoordinate = yCoordinate;
            this.zCoordinate = zCoordinate;
            this.worldName = worldName;
        }

        /* package-private */ String generateTemplateForInsertionCommand() {
            return "insert into sub_home " +
                    "(player_uuid, server_id, id, name, location_x, location_y, location_z, world_name) " +
                    "values ('" + playerUuid + "', " + serverId + ", " + id + ", " +
                    "?, " + xCoordinate + ", " + yCoordinate + ", " + zCoordinate + ", '" + worldName + "')";
        }

    }

    private static class SubHomeDTOParser {
        private final @NotNull String uuid;
        private final @NotNull String serverId;

        /* package-pribate */ SubHomeDTOParser(@NotNull String uuid, @NotNull String serverId) {
            this.uuid = uuid;
            this.serverId = serverId;
        }

        private Optional<SubHomeDTO> parseIndividualRawData(int index,
                                                            @NotNull List<@NotNull String> homePointData,
                                                            @NotNull String subHomeName) {
            final @NotNull String xCoordinate = homePointData.get(0);
            final @NotNull String yCoordinate = homePointData.get(1);
            final @NotNull String zCoordinate = homePointData.get(2);
            final @NotNull String worldName = homePointData.get(3);

            // セットされていないかどうかはx座標データについて空文字テストをすれば十分である
            if (xCoordinate.equals("")) return Optional.empty();

            final SubHomeDTO dto =
                    new SubHomeDTO(
                            String.valueOf(index),
                            uuid, serverId,
                            subHomeName.equals("") ? null : subHomeName,
                            xCoordinate, yCoordinate, zCoordinate, worldName
                    );

            return Optional.of(dto);
        }

        /**
         * @param <T> リストの型
         * @return {@code list}から{@code chunkSize}個ずつ要素を取り出して作ったリストのリスト
         *
         * 余った要素は捨てられるので、戻り値の要素はすべて同じ長さ({@code chunkSize})を持つことになる。
         */
        private static <T> ArrayList<ArrayList<T>> chunk(@NotNull ArrayList<T> inputList, int chunkSize) {
            final int inputListSize = inputList.size();
            final int outputListSize = inputListSize / chunkSize;

            return IntStream
                    .range(0, outputListSize)
                    .mapToObj(outputIndex ->
                            new ArrayList<>(inputList.subList(outputIndex * chunkSize, (outputIndex + 1) * chunkSize))
                    )
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        private List<Optional<SubHomeDTO>> parseRawData(@NotNull String homePointRawData,
                                                        @Nullable String parsedSubHomeNameData) {
            final ArrayList<String> homePointSplitData = new ArrayList<>(Arrays.asList(homePointRawData.split(",")));
            // NOTE: https://github.com/GiganticMinecraft/SeichiAssist/pull/110#discussion_r281012395 (変えると不整合が生じる)
            final ArrayList<ArrayList<String>> rawHomePoints = chunk(homePointSplitData, 4);
            final int subHomeCount = rawHomePoints.size();

            // NOTE: https://github.com/GiganticMinecraft/SeichiAssist/pull/110#discussion_r281027497
            final ArrayList<@NotNull String> rawSubHomesNames = parsedSubHomeNameData == null
                    ? new ArrayList<>(Collections.nCopies(subHomeCount, ""))
                    : new ArrayList<>(Arrays.asList(parsedSubHomeNameData.split(",")));

            return IntStream
                    .range(0, subHomeCount)
                    .mapToObj(index ->
                            parseIndividualRawData(index, rawHomePoints.get(index), rawSubHomesNames.get(index)))
                    .collect(Collectors.toList());
        }

        private @Nullable String parseSubHomeNameData(@Nullable String subHomeNameRawData) {
            if (subHomeNameRawData == null) return null;
            try {
                return new String(Hex.decodeHex(subHomeNameRawData.toCharArray()), StandardCharsets.UTF_8);
            } catch (DecoderException e) {
                e.printStackTrace();
                return null;
            }
        }

        /* package-private */ List<SubHomeDTO> parseRawDataAndFilterUndefineds(@NotNull String homePointRawData,
                                                                               @Nullable String subHomeNameRawData) {
            return parseRawData(homePointRawData, parseSubHomeNameData(subHomeNameRawData)).stream()
                    .map(optionalData -> optionalData.orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

    private static class SubHomeMigration implements Migration {
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
            // NOTE: https://github.com/GiganticMinecraft/SeichiAssist/pull/110#discussion_r281027504
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
}
