USE seichiassist;

CREATE TABLE player_break_suppression_preference(
    uuid CHAR(36) NOT NULL,
    do_break_suppression_due_to_mana BOOL NOT NULL DEFAULT FALSE,
    PRIMARY KEY (uuid),
    CONSTRAINT fk_player_break_suppression_preference_uuid FOREIGN KEY (uuid) REFERENCES playerdata(uuid)
);
