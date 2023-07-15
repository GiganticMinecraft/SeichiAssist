#!/bin/bash

wget -O gachadata.sql -P /docker-entrypoint-initdb.d https://redmine.seichi.click/attachments/download/997/gachadata.sql

# TODO: 定期的にdumpされたgachadata.sqlの最新版をダウンロードできるようにする https://github.com/GiganticMinecraft/SeichiAssist/issues/2172
# 外部キー制約がかかっているgachadataテーブルをDROPしたいので、一旦外部キー制約のチェックをオフにする
mysql -uroot -punchamaisgod -e '
  SET foreign_key_checks = 0;
  DROP TABLE seichiassist.gachadata;
  USE seichiassist;
  SOURCE /docker-entrypoint-initdb.d/gachadata.sql;
  SET foreign_key_checks = 1;'
