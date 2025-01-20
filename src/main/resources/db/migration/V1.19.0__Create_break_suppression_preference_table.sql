USE seichiassist;

CREATE TABLE IF NOT EXISTS player_break_suppression_preference(
    -- `SET ...` がないと Foreign key constraint is incorrectly formed が発生し
    -- `SHOW ENGINE INNODB STATUS` を実行すると
    -- Error in foreign key constraint of table seichiassist.player_break_suppression_preference:
    -- Create table seichiassist.player_break_suppression_preference with foreign key fk_player_break_suppression_preference_uuid constraint failed.
    -- Field type or character set for column 'uuid' does not match referenced column 'uuid'
    -- となり、 playerdata.uuid のデータ型は CHAR(36) 文字コードを指定する
    uuid CHAR(36) SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL PRIMARY KEY,
    do_break_suppression_due_to_mana BOOL NOT NULL DEFAULT FALSE,
    FOREIGN KEY fk_player_break_suppression_preference_uuid(uuid) REFERENCES playerdata(uuid)
);
