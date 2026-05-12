# syntax=docker/dockerfile:1
FROM ghcr.io/giganticminecraft/chunk-search-rs:latest@sha256:23c2093e4f574460283bc367819ebceb5a57b234a67042468dc2d12d398cbe4d as chunk-search-provider
FROM ghcr.io/giganticminecraft/seichiassist-runner-v2:latest@sha256:31a6bbbd31b21f51f262a5753e17ded8bdac3903ffe14e0480407fdf1b3c4243
FROM mikefarah/yq:4.53.2 as yq

FROM itzg/minecraft-server:stable-java17@sha256:76c8d92cc93854838543cbbadcb5522f1d325d97882d4e9753005d53c228176e

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
