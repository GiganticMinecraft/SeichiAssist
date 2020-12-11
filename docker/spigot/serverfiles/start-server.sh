#!/bin/sh

set -e

rm /spigot/plugins/SeichiAssist* || true

cp -Rf /spigot-files/* /spigot/
mkdir -p /spigot/plugins/SeichiAssist/

cd /spigot/plugins/SeichiAssist

rm config.yml || true
jar xf ../SeichiAssist-*.jar config.yml

sed -i -e "s/servernum: .*/servernum: ${SERVER_NUM}/" /spigot/plugins/SeichiAssist/config.yml
sed -i -e "s/server-id: .*/server-id: ${SERVER_ID}/" /spigot/plugins/SeichiAssist/config.yml
sed -i -e "s/host: .*/host: ${DB_HOST}/" /spigot/plugins/SeichiAssist/config.yml
sed -i -e "s/pw: .*/pw: ${DB_PASSWORD}/" /spigot/plugins/SeichiAssist/config.yml

cd /spigot/

java -jar /spigot/spigot*.jar nogui
