package com.github.unchama.seichiassist.database.migrations;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public class V1_7_1__Migrate_voting_fairy_times extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        try (final Connection connection = context.getConnection()) {
            connection.createStatement().execute("use seichiassist");
            // まず、",,,,,,"を""にする
            connection.createStatement()
                    .execute("UPDATE playerdata SET newVotingFairyTime = '' WHERE newVotingFairyTime = ',,,,,,'");

            // 次に、iso8601WithoutSecond_EncodeでISO 8601準拠の日付フォーマットに設定する
            final ResultSet alreadySetTimes = connection.createStatement()
                    .executeQuery("SELECT uuid, newVotingFairyTime FROM playerdata WHERE newVotingFairyTime <> ''");

            final Map<UUID, String> mappings = new HashMap<>(
                    connection.createStatement()
                            .executeQuery("SELECT COUNT(*) AS entries FROM playerdata")
                            .getInt("entries")
            );

            final DateTimeFormatter iso8601WithoutSecond = DateTimeFormatter.ofPattern("yyyy-MM-ddTHH:mm");
            while (alreadySetTimes.next()) {
                final UUID uuid = UUID.fromString(alreadySetTimes.getString("uuid"));
                final String rawDate = alreadySetTimes.getString("newVotingFairyTime");
                final String[] parts = rawDate.split(",");
                final LocalDateTime ld = LocalDateTime.of(
                        Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3]),
                        Integer.parseInt(parts[4])
                );
                mappings.put(uuid, ld.format(iso8601WithoutSecond));
            }

            try (PreparedStatement batch = connection.prepareStatement(
                    "UPDATE SET newVotingFairyTime = ? WHERE uuid = ?"
            )) {
                mappings.entrySet().stream().forEach(x -> {
                    try {
                        batch.setString(1, x.getKey().toString());
                        batch.setString(2, x.getValue());
                        batch.addBatch();
                    } catch (SQLException e) {
                        // throw eするとSAMの内側なのでエラーを吐く
                        e.printStackTrace();
                    }
                });
                batch.execute();
            }
            // 終了
        }
    }
}
