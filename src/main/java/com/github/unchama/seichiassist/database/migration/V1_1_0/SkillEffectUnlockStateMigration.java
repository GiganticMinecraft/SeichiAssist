package com.github.unchama.seichiassist.database.migration.V1_1_0;

import com.github.unchama.seichiassist.ActiveSkillEffect;
import com.github.unchama.seichiassist.ActiveSkillPremiumEffect;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/* package-private */ class SkillEffectUnlockStateMigration implements Migration {
    /* package-private */ SkillEffectUnlockStateMigration() {}

    private static void migrateEffectUnlockStateColumn(final Connection connection,
                                                final String newTableName,
                                                final Set<String> effectColumnNames) {
        effectColumnNames.forEach(effectName -> {
            try (Statement statement = connection.createStatement()) {
                final String copyQuery = "insert into " + newTableName + "(player_uuid, effect_name) " +
                        "select uuid as player_uuid, " + effectName + " as effect_name where " + effectName;
                statement.executeUpdate(copyQuery);

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
        final Set<String> activeSkillEffectNames = Arrays
                .stream(ActiveSkillEffect.values())
                .map(ActiveSkillEffect::getsqlName)
                .collect(Collectors.toSet());
        migrateActiveSkillEffect(connection, activeSkillEffectNames);

        final Set<String> activeSkillPremiumEffectNames = Arrays
                .stream(ActiveSkillPremiumEffect.values())
                .map(ActiveSkillPremiumEffect::getsqlName)
                .collect(Collectors.toSet());
        migrateActiveSkillPremiumEffect(connection, activeSkillPremiumEffectNames);
    }
}
