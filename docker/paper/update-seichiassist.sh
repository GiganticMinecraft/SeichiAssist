#!/bin/sh

set -e

rm -f /data/plugins/SeichiAssist*.jar || true

mkdir -p /data/plugins/SeichiAssist
cd /data/plugins

mv /SeichiAssist/SeichiAssist.jar SeichiAssist.jar

cd SeichiAssist

rm -f config.yml || true

jar xf ../SeichiAssist.jar config.yml

config_update_expr="\
  .servernum = \"$SERVER_NUM\" |\
  .server-id = \"$SERVER_ID\" |\
  .host = \"$DB_HOST\" |\
  .pw = \"$DB_PASSWORD\" |\
  .BungeeSemaphoreResponder.Redis.Host = \"$REDIS_HOST\" |\
  .BungeeSemaphoreResponder.Redis.Port = \"$REDIS_PORT\" |\
  .RedisBungee.redis-host = \"$REDIS_HOST\" |\
  .RedisBungee.redis-port = \"$REDIS_PORT\""

yq e "$config_update_expr" config.yml > tmpfile ; mv tmpfile config.yml

# 初期状態ではワールドデータなどが読み書きできず、エラーになるので/data/以下の所有者をrootにする
chown root -R /data/

# itzg/docker-minecraft-serverを起動する
/start
