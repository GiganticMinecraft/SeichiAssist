#!/bin/sh

## ソースコードからSeichiAssist.jarをビルド
sbt assembly

## dockerイメージのビルド（初回は数十分かかります）
docker-compose build -m 2g

## デバッグに必要なdockerコンテナを起動
## (起動後はCtrl+Cで停止できます)
docker-compose up



## 以下、リファレンス ##

## バックグラウンドでdockerコンテナを起動
## (カレントディレクトリにdocker-compose.ymlが存在する必要がある)
# docker-compose up -d

## バックグラウンドで起動したdockerコンテナを停止
# docker-compose stop

## dockerコンテナを削除(データが初期化される)
# docker-compose down
# or
# docker-compose rm -f

## 起動中のdockerコンテナ内に入る
## (Container ID は # docker ps で確認する)
# docker exec -it <Container ID> /bin/bash

## キャッシュを使用しないdockerイメージビルド(トラブルシューティング時に使用)
# docker-compose build --no-cache -m 2g
