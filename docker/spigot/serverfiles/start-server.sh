#!/bin/sh

set -e

rm /spigot/plugins/SeichiAssist* || true

cp -Rf /spigot-files/* /spigot/
mkdir -p /spigot/plugins/SeichiAssist/

cd /spigot/plugins/SeichiAssist

rm config.yml || true
jar xf ../SeichiAssist-*.jar config.yml

cd /spigot/plugins/SeichiAssist

config_update_expr="\
  .servernum = \"$SERVER_NUM\" |\
  .server-id = \"$SERVER_ID\" |\
  .host = \"$DB_HOST\" |\
  .pw = \"$DB_PASSWORD\" |\
  .BungeeSemaphoreResponder.Redis.Host = \"$REDIS_HOST\" |\
  .BungeeSemaphoreResponder.Redis.Port = \"$REDIS_PORT\""

yq e "$config_update_expr" config.yml > tmpfile ; mv tmpfile config.yml

cd /spigot/

JMX_PORT=${JMX_PORT:-7091}
JMX_BINDING=${JMX_BINDING:-0.0.0.0}
JMX_HOST=${JMX_HOST:-localhost}

java \
  -Dcom.sun.management.jmxremote.local.only=false \
  -Dcom.sun.management.jmxremote.port=${JMX_PORT} \
  -Dcom.sun.management.jmxremote.rmi.port=${JMX_PORT} \
  -Dcom.sun.management.jmxremote.authenticate=false \
  -Dcom.sun.management.jmxremote.ssl=false \
  -Dcom.sun.management.jmxremote.host=${JMX_BINDING} \
  -Djava.rmi.server.hostname=${JMX_HOST} \
  -Xmx4g -Xms256m \
  -jar /spigot/spigot*.jar nogui
