CREATE TABLE IF NOT EXISTS player_rate_limit(
    uuid CHAR(36) NOT NULL,
    rate_limit_name CHAR(32) NOT NULL,
    current_value INT NOT NULL,
    PRIMARY KEY(uuid, rate_limit_name)
);
