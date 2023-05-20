USE seichiassist;

-- 接続時間がオーバーフローしているプレイヤーがいるのでbigintに
ALTER TABLE playerdata MODIFY playtick BIGINT DEFAULT 0;
-- ref. https://discord.com/channels/288275544234131456/324180473649692682/1031533117078511776
-- 「4294967295」は、上記計算式中の定数を足し合わせたもの
UPDATE playerdata SET playtick = 4294967295 - playtick WHERE playtick < 0;
