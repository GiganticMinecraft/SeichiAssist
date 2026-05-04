# syntax=docker/dockerfile:1
FROM ghcr.io/giganticminecraft/chunk-search-rs:latest@sha256:c80fc37be0d7e5079fcb1e399e0df1a0e9cf91d99bb65bcc0581cc2fa4854641 as chunk-search-provider
FROM ghcr.io/giganticminecraft/seichiassist-runner-v2:latest@sha256:31a6bbbd31b21f51f262a5753e17ded8bdac3903ffe14e0480407fdf1b3c4243
FROM mikefarah/yq:4.52.5 as yq

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

RUN chmod a+x /data/update-seichiassist.sh
RUN nkf -Lu --overwrite /data/update-seichiassist.sh

ENTRYPOINT ["/data/update-seichiassist.sh"]
