FROM itzg/minecraft-server:java8-multiarch

ARG SERVER_NUM
ARG SERVER_ID
ARG DB_HOST
ARG DB_PASSWORD
ARG REDIS_HOST
ARG REDOS_PORT

RUN apt install openjdk-8-jdk -y

RUN curl -LJO https://github.com/mikefarah/yq/releases/latest/download/yq_linux_amd64
RUN mv yq_linux_amd64 /usr/local/bin/yq
RUN chmod a+x /usr/local/bin/yq

#FROM ghcr.io/giganticminecraft/chunk-search-rs:ea37d2d as chunk-search-provider
#
#COPY --from=chunk-search-provider /build/chunk-search-rs /usr/local/bin/chunk-search-rs

COPY ./localDependencies/ /data/plugins/
COPY ./target/build/ /data/
COPY ./docker/spigot/ /data/

RUN rm -f /data/plugins/SeichiAssist* || true

RUN mkdir -p /data/plugins/SeichiAssist
RUN cd /data/plugins

RUN mv /data/SeichiAssist.jar /data/plugins/SeichiAssist.jar

ARG config_update_expr="\
      .servernum = \"${SERVER_NUM}\" |\
      .server-id = \"${SERVER_ID}\" |\
      .host = \"${DB_HOST}\" |\
      .pw = \"${DB_PASSWORD}\" |\
      .BungeeSemaphoreResponder.Redis.Host = \"${REDIS_HOST}\" |\
      .BungeeSemaphoreResponder.Redis.Port = \"${REDIS_PORT}\" |\
      .RedisBungee.redis-host = \"${REDIS_HOST}\" |\
      .RedisBungee.redis-port = \"${REDIS_PORT}\""

RUN jar xf /data/plugins/SeichiAssist.jar config.yml && mv config.yml /data/plugins/SeichiAssist/config.yml

RUN yq e "${config_update_expr}" /data/plugins/SeichiAssist/config.yml > /data/plugins/SeichiAssist/tmpfile ; mv /data/plugins/SeichiAssist/tmpfile /data/plugins/SeichiAssist/config.yml
