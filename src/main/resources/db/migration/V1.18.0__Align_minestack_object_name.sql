USE seichiassist;

-- gachadataはガチャ景品に対して依存を持つが、ガチャ景品のIDより語尾の数が2つ小さいので、
-- gachadata0_[ガチャ景品ID]という形式にする
UPDATE mine_stack SET object_name = CONVERT(REPLACE(object_name, 'gachadata0_', ''), INT) + 2  WHERE object_name NOT LIKE 'gachadata0_exp_bottle' AND object_name LIKE 'gachadata0_%';

-- 上の変換により、mine_stack_gacha_objectsを永続化する必要がなくなったので削除する
DROP TABLE IF EXISTS mine_stack_gacha_objects;
