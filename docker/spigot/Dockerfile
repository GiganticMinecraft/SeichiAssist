# syntax=docker/dockerfile:1.6
FROM ghcr.io/giganticminecraft/chunk-search-rs:sha-598517c as chunk-search-provider
FROM ghcr.io/giganticminecraft/seichiassist-runner:ba2aa54

FROM itzg/minecraft-server:java8-multiarch

# JDKとnkfをインストール
RUN apt-get update && apt-get install -y \
    nkf \
    openjdk-8-jdk \
&& apt-get clean \
&& rm -rf /var/lib/apt/lists/*

#yqをインストール
RUN curl -LJO https://github.com/mikefarah/yq/releases/latest/download/yq_linux_amd64 && \
mv yq_linux_amd64 /usr/local/bin/yq && \
chmod a+x /usr/local/bin/yq

# chunk-search-rsをインストール
COPY --link --from=chunk-search-provider /build/chunk-search-rs /usr/bin/chunk-search-rs

# プラグインとスクリプトをローカルからコピーする
COPY --link ./localDependencies/ /data/plugins/
COPY --link ./target/build/ /SeichiAssist/
COPY --link ./docker/spigot/ /data/

RUN chmod a+x /data/update-seichiassist.sh
RUN nkf -Lu --overwrite /data/update-seichiassist.sh

ENTRYPOINT ["/data/update-seichiassist.sh"]
