#!/bin/bash

build_image() {
  set -e

  rm -r target/build || true

  ## ソースコードからSeichiAssist.jarをビルド
  ./sbt assembly

  ## dockerイメージのビルド
  docker compose build -m 2g
}

stop_docker_service() {
  set -e
  docker compose down
}

set -e

# 子プロセス側で関数をコマンドとして参照したいためexportする
export -f build_image
export -f stop_docker_service

# 既存のサービスを落とし、ビルド完了を待つ処理を並列実行する
echo "stop_docker_service build_image" | xargs -P 0 -n 1 bash -c

if [ $1 == "update-gachadata" ]; then
  echo Updating gachadata...
  call docker compose up -d db

  # ここで遅延を入れないとdbが起動する前にgachadataを更新するスクリプトが走ってしまう
  sleep 3
  docker exec -it seichiassist-db-1 /docker-entrypoint-initdb.d/update-gachadata.sh
  echo "Completed updating gachadata."
  stop_docker_service
fi

## デバッグに必要なdockerコンテナを起動
## (起動後はCtrl+Cで停止できます)
docker compose up --abort-on-container-exit



## 以下、リファレンス ##

## バックグラウンドでdockerコンテナを起動
## (カレントディレクトリにdocker-compose.ymlが存在する必要がある)
# docker compose up -d

## バックグラウンドで起動したdockerコンテナを停止
# docker compose stop

## dockerコンテナを削除(データが初期化される)
# docker compose down
# or
# docker compose rm -f

## 起動中のdockerコンテナ内に入る
## (Container ID は # docker ps で確認する)
# docker exec -it <Container ID> /bin/bash

## キャッシュを使用しないdockerイメージビルド(トラブルシューティング時に使用)
# docker compose build --no-cache -m 2g
