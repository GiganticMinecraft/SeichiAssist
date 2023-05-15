# syntax=docker/dockerfile:1.4
FROM ghcr.io/giganticminecraft/chunk-search-rs:sha-f1943b1 as chunk-search-provider
FROM ghcr.io/giganticminecraft/seichiassist-runner:ba2aa54

FROM itzg/minecraft-server:java17-jdk

# nkfをインストール
RUN apt-get update && apt-get install -y nkf \
&& apt-get clean \
&& rm -rf /var/lib/apt/lists/*

#yqをインストール
RUN curl -LJO https://github.com/mikefarah/yq/releases/latest/download/yq_linux_amd64 && \
mv yq_linux_amd64 /usr/local/bin/yq && \
chmod a+x /usr/local/bin/yq

# chunk-search-rsをインストール
COPY --link --from=chunk-search-provider /build/chunk-search-rs /usr/bin/chunk-search-rs

# プラグインとスクリプトをローカルからコピーする
COPY --link ./target/build/ /SeichiAssist/
COPY --link ./docker/spigot/ /data/
ADD ./localDependencies/ /data/plugins/

RUN chmod a+x /data/update-seichiassist.sh
RUN nkf -Lu --overwrite /data/update-seichiassist.sh

ENTRYPOINT ["/data/update-seichiassist.sh"]
