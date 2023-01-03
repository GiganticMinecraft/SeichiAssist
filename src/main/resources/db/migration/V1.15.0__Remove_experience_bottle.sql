USE seichiassist;

-- ガチャ景品の中から経験値瓶を削除する
DELETE FROM mine_stack_gacha_objects WHERE id = 2;
DELETE FROM gachadata WHERE id = 2;
