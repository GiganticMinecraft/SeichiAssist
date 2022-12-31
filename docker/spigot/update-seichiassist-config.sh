#!/bin/sh

set -e

rm -f /data/plugins/SeichiAssist* || true

mkdir -p /data/plugins/SeichiAssist
cd /data/plugins

mv ../SeichiAssist.jar SeichiAssist.jar

rm -f config.yml || true

cd SeichiAssist

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