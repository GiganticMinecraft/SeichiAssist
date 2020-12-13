#!/bin/sh

set -e

rm /spigot/plugins/SeichiAssist* || true

cp -Rf /spigot-files/* /spigot/
mkdir -p /spigot/plugins/SeichiAssist/

cd /spigot/plugins/SeichiAssist

rm config.yml || true
jar xf ../SeichiAssist-*.jar config.yml

cd /spigot/plugins/SeichiAssist

yq w config.yml "servernum" "$SERVER_NUM" | \
  yq w - "server-id" "$SERVER_ID" | \
  yq w - "host" "$DB_HOST" | \
  yq w - "pw" "$DB_PASSWORD" | \
  yq w - "BungeeSemaphoreResponder.Redis.Host" "$REDIS_HOST" | \
  yq w - "BungeeSemaphoreResponder.Redis.Port" "$REDIS_PORT" > tmpfile

mv tmpfile config.yml


cd /spigot/

java -jar /spigot/spigot*.jar nogui
