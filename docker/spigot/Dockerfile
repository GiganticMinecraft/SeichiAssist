FROM ghcr.io/giganticminecraft/chunk-search-rs:sha-598517c as chunk-search-provider
FROM ghcr.io/giganticminecraft/seichiassist-runner:ba2aa54

FROM itzg/minecraft-server:java8-multiarch

ARG SERVER_NUM
ARG SERVER_ID
ARG DB_HOST
ARG DB_PASSWORD
ARG REDIS_HOST
ARG REDIS_PORT

# Javaとyqをインストール
RUN apt install openjdk-8-jdk -y && \
curl -LJO https://github.com/mikefarah/yq/releases/latest/download/yq_linux_amd64 && \
mv yq_linux_amd64 /usr/local/bin/yq && \
chmod a+x /usr/local/bin/yq

# chunk-search-rsをインストール
COPY --from=chunk-search-provider /build/chunk-search-rs /usr/bin/chunk-search-rs

# プラグインとスクリプトをローカルからコピーする
COPY ./localDependencies/ /data/plugins/
COPY ./target/build/ /SeichiAssist/
COPY ./docker/spigot/ /data/

RUN chmod a+x /data/update-seichiassist.sh
ENTRYPOINT ["/data/update-seichiassist.sh"]
