# syntax=docker/dockerfile:1
FROM ghcr.io/giganticminecraft/chunk-search-rs:latest@sha256:8dd3565a58250b76acdd13c85154224f9ecf4a94499c4cc087594e033dbbb53f as chunk-search-provider
FROM ghcr.io/giganticminecraft/seichiassist-runner-v2:latest@sha256:0e1eb4ae2620061745b5d383a57fa01667743df380f9d31e1473fc6340df8fa6
FROM mikefarah/yq:4.53.6 as yq

FROM itzg/minecraft-server:stable-java17@sha256:67004c00a459dc69e252bc4931285cc6153e958d83a3c32f9dd8db8d7704ccb4

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
