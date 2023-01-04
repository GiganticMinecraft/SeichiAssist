use seichiassist;

-- MySQLのスロークエリを引いた結果遅かったSELECTのクエリに対してINDEXを貼るようにする。
ALTER TABLE mine_stack ADD INDEX index_mine_stack_on_object_name_amount(object_name, amount);
ALTER TABLE playerdata ADD INDEX index_playerdata_on_lastquit(lastquit);
