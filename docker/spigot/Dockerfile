FROM ghcr.io/giganticminecraft/chunk-search-rs:sha-598517c as chunk-search-provider
FROM ghcr.io/giganticminecraft/seichiassist-runner:ba2aa54

FROM itzg/minecraft-server:java8-multiarch as minecraft-server

ARG SERVER_NUM
ARG SERVER_ID
ARG DB_HOST
ARG DB_PASSWORD
ARG REDIS_HOST
ARG REDOS_PORT

# Javaとyqをインストール
RUN apt install openjdk-8-jdk -y

RUN curl -LJO https://github.com/mikefarah/yq/releases/latest/download/yq_linux_amd64
RUN mv yq_linux_amd64 /usr/local/bin/yq
RUN chmod a+x /usr/local/bin/yq

# chunk-search-rsをインストール
COPY --from=chunk-search-provider /build/chunk-search-rs /usr/bin/chunk-search-rs

# プラグインをローカルからコピーする
COPY ./localDependencies/ /data/plugins/
COPY ./target/build/ /data/


WORKDIR /data/plugins/

# 既存のSeichiAssistの設定ファイルを削除
RUN rm -f SeichiAssist* || true
RUN mkdir -p SeichiAssist

# ビルドされたSeichiAssistをコンテナにコピーする
RUN mv ../SeichiAssist.jar SeichiAssist.jar

# SeichiAssistのconfigを環境変数を利用して上書きする
ARG config_update_expr="\
      .servernum = \"${SERVER_NUM}\" |\
      .server-id = \"${SERVER_ID}\" |\
      .host = \"${DB_HOST}\" |\
      .pw = \"${DB_PASSWORD}\" |\
      .BungeeSemaphoreResponder.Redis.Host = \"${REDIS_HOST}\" |\
      .BungeeSemaphoreResponder.Redis.Port = \"${REDIS_PORT}\" |\
      .RedisBungee.redis-host = \"${REDIS_HOST}\" |\
      .RedisBungee.redis-port = \"${REDIS_PORT}\""

RUN jar xf SeichiAssist.jar config.yml && mv config.yml SeichiAssist/config.yml

WORKDIR /data/plugins/SeichiAssist/

RUN yq e "${config_update_expr}" config.yml > tmpfile ; mv tmpfile config.yml

# ワールドデータなどが読み書きできず、エラーになるので/data/以下の所有者をrootにする
RUN chown root -R /data/
