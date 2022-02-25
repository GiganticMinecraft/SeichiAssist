CREATE TABLE IF NOT EXISTS build_count_rate_limit(
    uuid CHAR(36) PRIMARY KEY NOT NULL,
    -- 注: DECIMALは(全体の桁数, 小数点以下の桁数)というフォーマットである
    available_permission DECIMAL(17, 5) UNSIGNED NOT NULL,
    updated_date DATETIME NOT NULL
);
