use seichiassist;

-- 1.10.0 の alter table で default 0 が吹き飛んでいた
alter table playerdata modify playtick bigint default 0;
