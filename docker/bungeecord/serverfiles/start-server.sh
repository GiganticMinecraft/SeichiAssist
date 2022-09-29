#!/bin/sh

set -e

rm -r /bungeecord/plugins/ || true

# overwrite server directory
cp -Rf /bungeecord-files/* /bungeecord/

# extract fresh config and rePrepend for BungeeSemaphore
mkdir -p /bungeecord/plugins/BungeeSemaphore/
cd /bungeecord/plugins/BungeeSemaphore
rm config.yml || true
jar xf ../BungeeSemaphore.jar config.yml

config_update_expr="\
  .redis.host = \"$REDIS_HOST\" |\
  .redis.port = $REDIS_PORT"

yq e "$config_update_expr" config.yml >tmpfile
mv tmpfile config.yml

# extract fresh config and rePrepend for RedisBungee
mkdir -p /bungeecord/plugins/RedisBungee/
cd /bungeecord/plugins/RedisBungee
rm config.yml || true
jar xf ../RedisBungee-0.5.jar example_config.yml
mv example_config.yml config.yml

config_update_expr="\
  .redis-server = \"$REDIS_HOST\" |\
  .redis-port = $REDIS_PORT |\
  .server-id = \"$SERVER_ID\""

yq e "$config_update_expr" config.yml >tmpfile
cp tmpfile config.yml

cd /bungeecord/ && java -jar /bungeecord/BungeeCord.jar
