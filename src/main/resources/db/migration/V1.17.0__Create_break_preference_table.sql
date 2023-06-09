USE seichiassist;

CREATE TABLE player_break_preference(
    uuid CHAR(36) NOT NULL,
    flag_name ENUM('Chest', 'MadeFromNetherQuartz') NOT NULL,
    include BOOL NOT NULL DEFAULT TRUE,
    PRIMARY KEY(uuid, flag_name),
    INDEX index_player_break_preference_on_uuid (uuid)
);
