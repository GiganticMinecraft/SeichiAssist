# syntax=docker/dockerfile:1
FROM ghcr.io/giganticminecraft/chunk-search-rs:latest@sha256:cb345f86b96fa44746d010e406ddee662713bd9be728205a297e6a178e5ee5b6 as chunk-search-provider
FROM ghcr.io/giganticminecraft/seichiassist-runner-v2:latest@sha256:ba8eff5547de3cea8c32d849506a92d33f15c57e4f4359d1e621785f5801b6fd
FROM mikefarah/yq:4.53.3 as yq

FROM itzg/minecraft-server:stable-java17@sha256:9747457a6928d675847672bf0c8f6201498247a1eda6ef3277ba188720f6a8f5

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
