USE seichiassist;

# gachadataはガチャ景品に対して依存を持つが、ガチャ景品のIDより語尾の数が2つ小さいので、
# gachadata0_[ガチャ景品ID]という形式にする
UPDATE mine_stack SET object_name = CONVERT(INT, REPLACE('gachadata0_', '')) + 2  LIKE 'gachadata0_%';

# 上の変換により、mine_stack_gacha_objectsを永続化する必要がなくなったので削除する
DROP TABLE mine_stack_gacha_objects;
