use seichiassist;

-- MySQLのスロークエリを引いた結果遅かったSELECTのクエリに対してINDEXを貼るようにする。
ALTER TABLE playerdata ADD INDEX index_playerdata_playtick(playtick);
