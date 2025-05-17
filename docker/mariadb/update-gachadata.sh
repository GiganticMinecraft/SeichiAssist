#!/bin/bash

wget -O /gachadata/gachadata.sql https://gachadata.public-gigantic-api.seichi.click/

# 外部キー制約がかかっているgachadataテーブルをDROPしたいので、一旦外部キー制約のチェックをオフにする
mysql -uroot -punchamaisgod -e '
  SET foreign_key_checks = 0;
  USE seichiassist;
  DROP TABLE IF EXISTS gachadata, gacha_events;
  SOURCE /gachadata/gachadata.sql;
  SET foreign_key_checks = 1;'
