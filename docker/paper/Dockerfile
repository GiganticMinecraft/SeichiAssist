# syntax=docker/dockerfile:1
FROM ghcr.io/giganticminecraft/chunk-search-rs:latest@sha256:d08779d3f72cec13b60b2aeb9003ab77aaa857987e60c34ab35b921e432466c3 as chunk-search-provider
FROM ghcr.io/giganticminecraft/seichiassist-runner-v2:latest@sha256:0e1eb4ae2620061745b5d383a57fa01667743df380f9d31e1473fc6340df8fa6
FROM mikefarah/yq:4.54.3 as yq

FROM itzg/minecraft-server:stable-java17@sha256:35f069ed801f4b14655acb52fdb7745dccfe69399867f57608ae4136a32c3c9e

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
