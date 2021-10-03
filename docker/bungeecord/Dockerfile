FROM maven:3.6.3-jdk-8 as redisbungee-provider
WORKDIR /
RUN git clone https://github.com/minecrafter/RedisBungee.git
WORKDIR /RedisBungee
# version 0.5.1
RUN git checkout 934e4543d695f1e79f6690ac4ee435e3fa5d50d4
RUN mvn clean install

FROM ghcr.io/giganticminecraft/bungeesemaphore/bungeesemaphore-jar:b61f4e1de9d5 as bungeesemaphore-provider

# use seichiassist-runner to overwrite line endings of shell script using nkf
FROM ghcr.io/giganticminecraft/seichiassist-runner:ba2aa54

WORKDIR /bungeecord
ADD 'https://ci.md-5.net/job/BungeeCord/lastSuccessfulBuild/artifact/bootstrap/target/BungeeCord.jar' BungeeCord.jar

COPY --from=redisbungee-provider /RedisBungee/target/RedisBungee-0.5.jar /bungeecord-files/plugins/
COPY --from=bungeesemaphore-provider /build/BungeeSemaphore.jar /bungeecord-files/plugins/
COPY docker/bungeecord/serverfiles /bungeecord-files/

RUN nkf -Lu --overwrite /bungeecord-files/start-server.sh

CMD bash /bungeecord-files/start-server.sh
