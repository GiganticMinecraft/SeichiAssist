USE seichiassist;

CREATE TABLE player_break_trigger_preference(
    uuid CHAR(36) NOT NULL,
    trigger_category ENUM('ManaFullyConsumed') NOT NULL,
    do_not_break BOOL NOT NULL DEFAULT FALSE,
    PRIMARY KEY(uuid, trigger_category),
    INDEX index_player_break_trigger_preference_on_uuid (uuid)
);
