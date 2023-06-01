USE seichiassist;

CREATE TABLE player_break_preference(
    uuid CHAR(36) NOT NULL PRIMARY KEY,
    flag_name ENUM('Chest', 'MadeFromNetherQuartz') NOT NULL,
    include BOOL NOT NULL DEFAULT TRUE
);
