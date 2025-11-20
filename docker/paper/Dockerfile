# syntax=docker/dockerfile:1
FROM ghcr.io/giganticminecraft/chunk-search-rs:sha-7515d8c as chunk-search-provider
FROM ghcr.io/giganticminecraft/seichiassist-runner-v2:776da10
FROM mikefarah/yq:4.48.2 as yq

FROM itzg/minecraft-server:java17-jdk

# nkfをインストール
RUN \
    --mount=type=cache,target=/var/lib/apt,sharing=locked \
    --mount=type=cache,target=/var/cache/apt,sharing=locked \
    apt-get update && apt-get install -y nkf

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
