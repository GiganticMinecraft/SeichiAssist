-- V1.7.5でseichiassistのDBを指定しなかったので、flyway_managed_schemaの下にテーブルができてしまった
DROP TABLE IF EXISTS flyway_managed_schema.build_count_rate_limit;

CREATE TABLE IF NOT EXISTS seichiassist.build_count_rate_limit(
    uuid CHAR(36) PRIMARY KEY NOT NULL,
    -- 注: DECIMALは(全体の桁数, 小数点以下の桁数)というフォーマットである
    available_permission DECIMAL(17, 5) UNSIGNED NOT NULL,
    -- update_dateではなくrecord_dateにsubsystemではなっていたので変更
    record_date DATETIME NOT NULL
);
