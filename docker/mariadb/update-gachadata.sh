#!/bin/bash

# TODO: 定期的にdumpされたgachadata.sqlの最新版をダウンロードできるようにする https://github.com/GiganticMinecraft/SeichiAssist/issues/2172
wget -O gachadata.sql -P / https://redmine.seichi.click/attachments/download/997/gachadata.sql

# 外部キー制約がかかっているgachadataテーブルをDROPしたいので、一旦外部キー制約のチェックをオフにする
mysql -uroot -punchamaisgod -e '
  SET foreign_key_checks = 0;
  DROP TABLE seichiassist.gachadata;
  USE seichiassist;
  SOURCE gachadata.sql;
  SET foreign_key_checks = 1;'
