# syntax=docker/dockerfile:1.16
FROM mariadb:10.11.13

WORKDIR /docker-entrypoint-initdb.d

COPY --link ./docker/mariadb/update-gachadata.sh /

RUN apt-get update -y && apt-get install wget -y && \
    # gachadataを保存するディレクトリに権限がないとgachadata.sqlをダウンロードしてリネームする処理が権限不足でできない
    mkdir -m a=rwx /gachadata/ && \
    chmod +x /update-gachadata.sh
