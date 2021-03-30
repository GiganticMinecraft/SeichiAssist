use seichiassist;

-- MySQLドライバは tinyint を boolean であると認識するらしく
-- (https://stackoverflow.com/questions/16798744/why-does-tinyint1-function-as-a-boolean-but-int1-does-not)、
-- 永続化層でScalikeJDBC等のJDBCドライバを介すライブラリを使うと整数を読めずにブール値が返ってくる。
-- これを回避するために smallint に型を変更する。
alter table playerdata
    modify column effectflag smallint;
