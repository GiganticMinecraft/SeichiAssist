use seichiassist;

alter table playerdata
    modify column effectflag smallint not null default 0;
