use seichiassist;

-- 接続時間がオーバーフローしているプレイヤーがいるのでbigintに
alter table playerdata modify playtick bigint default 0;
-- ref. https://discord.com/channels/288275544234131456/324180473649692682/1031533117078511776
update playerdata set playtick = 2147483648 - playtick + 2147483647 WHERE playtick < 0;
