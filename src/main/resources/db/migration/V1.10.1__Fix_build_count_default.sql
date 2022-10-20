use seichiassist;

-- 1.10.0 の alter table で default 0 が吹き飛んでいた
alter table playerdata modify build_count double not null default 0;
