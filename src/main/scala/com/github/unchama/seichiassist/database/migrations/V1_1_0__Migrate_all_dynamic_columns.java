package com.github.unchama.seichiassist.database.migrations;

import com.github.unchama.seichiassist.PackagePrivate;
import com.github.unchama.util.collection.SetFactory;
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

/**
 * V1.1.0のマイグレーションを担当するオブジェクトのクラス。
 *
 * このマイグレーションは1.1.0リリースにより本番環境に適用済みである。
 *
 * クラスの変更は整合性を崩しかねないため、行ってはならない。
 * DBスキーマの変更やレコードへの干渉はマイグレーションを追加することで行うこと。
 */
@SuppressWarnings("unused")
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
             * https://github.com/GiganticMinecraft/SeichiAssist/commit/e1c69b76e5dc92db0133544342d424b7be9cd0ea
             * の時点でActiveSkillEffect及びActiveSkillPremiumEffectのenumに入っている値に対応するカラムのみが
             * DBに格納されているという仮定に基づいたロジックである。
             */
            final Set<String> activeSkillEffectNames = SetFactory.of("ef_explosion", "ef_blizzard", "ef_meteo");
            final Set<String> activeSkillEffectTableNames = playerDataColumnNames
                    .stream()
                    .filter(activeSkillEffectNames::contains)
                    .collect(Collectors.toSet());
            migrateActiveSkillEffect(connection, activeSkillEffectTableNames);

            final Set<String> activeSkillPremiumEffectNames = SetFactory.of("ef_magic");
            final Set<String> activeSkillPremiumEffectTableNames = playerDataColumnNames
                    .stream()
                    .filter(activeSkillPremiumEffectNames::contains)
                    .collect(Collectors.toSet());
            migrateActiveSkillPremiumEffect(connection, activeSkillPremiumEffectTableNames);
        }
    }

    private static class SubHomeMetaLocation {
        private final @NotNull String serverId;
        private final @NotNull SubHomeWorldLocation worldLocation;

        private SubHomeMetaLocation(@NotNull String serverId, @NotNull SubHomeWorldLocation worldLocation) {
            this.serverId = serverId;
            this.worldLocation = worldLocation;
        }
    }

    private static class SubHomeWorldLocation {
        private final @NotNull String x;
        private final @NotNull String y;
        private final @NotNull String z;
        private final @NotNull String worldName;

        private SubHomeWorldLocation(@NotNull String x, @NotNull String y, @NotNull String z, @NotNull String worldName) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.worldName = worldName;
        }
    }

    /**
     * サブホームの情報を含むData Transfer Objectのクラス
     */
    private static class SubHomeDTO {
        private final @NotNull String id;
        private final @NotNull String ownerUuid;
        public final @Nullable String name;
        private final @NotNull SubHomeMetaLocation metaLocation;

        SubHomeDTO(@NotNull String id,
                   @NotNull String playerUuid,
                   @Nullable String name,
                   @NotNull SubHomeMetaLocation metaLocation) {
            this.id = id;
            this.ownerUuid = playerUuid;
            this.name = name;
            this.metaLocation = metaLocation;
        }

        @PackagePrivate
        String generateTemplateForInsertionCommand() {
            final String serverId = metaLocation.serverId;

            final SubHomeWorldLocation worldLocation = metaLocation.worldLocation;
            final String xCoordinate = worldLocation.x;
            final String yCoordinate = worldLocation.y;
            final String zCoordinate = worldLocation.z;
            final String worldName = worldLocation.worldName;

            return "insert into sub_home " +
                    "(player_uuid, server_id, id, name, location_x, location_y, location_z, world_name) " +
                    "values ('" + ownerUuid + "', " + serverId + ", " + id + ", " +
                    "?, " + xCoordinate + ", " + yCoordinate + ", " + zCoordinate + ", '" + worldName + "')";
        }

    }

    private static class SubHomeDTOParser {
        private final @NotNull String uuid;
        private final @NotNull String serverId;

        @PackagePrivate
        SubHomeDTOParser(@NotNull String uuid, @NotNull String serverId) {
            this.uuid = uuid;
            this.serverId = serverId;
        }

        private Optional<SubHomeDTO> parseIndividualRawData(@NotNull String subHomeId,
                                                            @NotNull List<@NotNull String> homePointData,
                                                            @NotNull String subHomeName) {
            final @NotNull String x = homePointData.get(0);
            final @NotNull String y = homePointData.get(1);
            final @NotNull String z = homePointData.get(2);
            final @NotNull String worldName = homePointData.get(3);

            final @Nullable String nonEmptyName = subHomeName.equals("") ? null : subHomeName;

            // セットされていないかどうかはx座標データについて空文字テストをすれば十分である
            if (x.equals("")) return Optional.empty();

            final SubHomeWorldLocation worldLocation = new SubHomeWorldLocation(x, y, z, worldName);
            final SubHomeMetaLocation metaLocation = new SubHomeMetaLocation(serverId, worldLocation);

            final SubHomeDTO dto = new SubHomeDTO(subHomeId, uuid, nonEmptyName, metaLocation);

            return Optional.of(dto);
        }

        /**
         * @param <T> リストの型
         * @return {@code list}から{@code chunkSize}個ずつ要素を取り出して作ったリストのリスト
         *
         * 余った要素は捨てられるので、戻り値の要素はすべて同じ長さ({@code chunkSize})を持つことになる。
         */
        private static <T> List<List<T>> chunk(@NotNull List<T> inputList, @SuppressWarnings("SameParameterValue") int chunkSize) {
            final int inputListSize = inputList.size();
            final int outputListSize = inputListSize / chunkSize;

            return IntStream
                    .range(0, outputListSize)
                    .mapToObj(outputIndex -> {
                        final int chunkStartIndex = outputIndex * chunkSize;
                        final int chunkEndIndex = chunkStartIndex + chunkSize;

                        return inputList.subList(chunkStartIndex, chunkEndIndex);
                    })
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        private List<Optional<SubHomeDTO>> parseRawData(@NotNull String homePointRawData,
                                                        @Nullable String parsedSubHomeNameData) {
            final List<String> homePointSplitData = Arrays.asList(homePointRawData.split(","));
            final List<List<String>> rawHomePoints = chunk(homePointSplitData, 4);

            final int subHomeCount = rawHomePoints.size();

            final List<@NotNull String> rawSubHomesNames = parsedSubHomeNameData == null
                    ? Collections.nCopies(subHomeCount, "")
                    : Arrays.asList(parsedSubHomeNameData.split(","));

            return IntStream
                    .range(0, subHomeCount)
                    .mapToObj(index ->
                            parseIndividualRawData(
                                    String.valueOf(index), rawHomePoints.get(index), rawSubHomesNames.get(index)
                            )
                    )
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

        @PackagePrivate
        List<SubHomeDTO> parseRawDataAndFilterUndefineds(@NotNull String homePointRawData,
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
