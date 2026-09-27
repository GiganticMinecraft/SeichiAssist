# syntax=docker/dockerfile:1
FROM ghcr.io/giganticminecraft/chunk-search-rs:latest@sha256:86fa73788b8e77a764f0c5a29b4002edd0630d56dcda3e9972fc4aa0fefec57b as chunk-search-provider
FROM ghcr.io/giganticminecraft/seichiassist-runner-v2:latest@sha256:63936652d077933059965e759d9563e46eb69b8e3ae2eaf7a45b516de2ea354c
FROM mikefarah/yq:4.53.6 as yq

FROM itzg/minecraft-server:stable-java17@sha256:0198cb9f0c7e7b1747645040e1245a6915c122662f3106f3b0979473a953ab61

# nkf と zip 展開ツールをインストール
RUN \
    --mount=type=cache,target=/var/lib/apt,sharing=locked \
    --mount=type=cache,target=/var/cache/apt,sharing=locked \
    apt-get update && apt-get install -y nkf unzip

#yqをインストール
COPY --from=yq /usr/bin/yq /usr/bin/yq

# chunk-search-rsをインストール
COPY --link --from=chunk-search-provider /build/chunk-search-rs /usr/bin/chunk-search-rs

# プラグインをローカルからコピーする
COPY --link ./target/build/ /SeichiAssist/
ADD ./localDependencies/ /data/plugins/

COPY --link --chmod=755 ./docker/paper/update-seichiassist.sh /usr/local/bin/update-seichiassist.sh
RUN nkf -Lu --overwrite /usr/local/bin/update-seichiassist.sh

ENTRYPOINT ["/usr/local/bin/update-seichiassist.sh"]
