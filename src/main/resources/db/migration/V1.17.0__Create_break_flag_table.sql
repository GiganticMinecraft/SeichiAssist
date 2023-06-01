USE seichiassist;

CREATE TABLE break_flags(
    uuid CHAR(36) NOT NULL PRIMARY KEY,
    flag_name ENUM('Chest', 'MadeFromNetherQuartz') NOT NULL,
    can_break BOOL NOT NULL DEFAULT TRUE
);
