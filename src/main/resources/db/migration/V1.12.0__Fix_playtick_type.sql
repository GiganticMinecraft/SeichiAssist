use seichiassist;

-- 接続時間がオーバーフローしているプレイヤーがいるのでbigintに
alter table playerdata modify playtick bigint default 0;
