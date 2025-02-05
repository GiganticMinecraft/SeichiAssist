USE seichiassist;

CREATE TABLE IF NOT EXISTS player_break_suppression_preference(
    uuid CHAR(36) NOT NULL PRIMARY KEY,
    do_break_suppression_due_to_mana BOOL NOT NULL DEFAULT FALSE,
);
