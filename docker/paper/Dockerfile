# syntax=docker/dockerfile:1.17
FROM ghcr.io/giganticminecraft/chunk-search-rs:sha-f1943b1 as chunk-search-provider
FROM ghcr.io/giganticminecraft/seichiassist-runner-v2:890105f
FROM mikefarah/yq:4.45.4 as yq

FROM itzg/minecraft-server:java17-jdk

# nkfをインストール
RUN apt-get update && apt-get install -y nkf \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

#yqをインストール
COPY --from=yq /usr/bin/yq /usr/bin/yq

# chunk-search-rsをインストール
COPY --link --from=chunk-search-provider /build/chunk-search-rs /usr/bin/chunk-search-rs

# プラグインとスクリプトをローカルからコピーする
COPY --link ./target/build/ /SeichiAssist/
COPY --link ./docker/paper/ /data/
ADD ./localDependencies/ /data/plugins/
ADD ./plugins /plugins

RUN chmod a+x /data/update-seichiassist.sh
RUN nkf -Lu --overwrite /data/update-seichiassist.sh

ENTRYPOINT ["/data/update-seichiassist.sh"]
