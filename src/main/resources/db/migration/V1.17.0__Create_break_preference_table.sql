USE seichiassist;

CREATE TABLE player_break_preference(
    uuid CHAR(36) NOT NULL,
    block_category ENUM('Chest', 'MadeFromNetherQuartz') NOT NULL,
    do_break BOOL NOT NULL DEFAULT TRUE,
    PRIMARY KEY(uuid, block_category),
    INDEX index_player_break_preference_on_uuid (uuid)
);
