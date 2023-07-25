# syntax=docker/dockerfile:1.4
FROM mariadb:10.11.4

WORKDIR /docker-entrypoint-initdb.d

# mariadbイメージのドキュメント(https://hub.docker.com/_/mariadb#:~:text=Initializing%20a%20fresh%20instance)によると、
# 初めて mariadb が起動したときに /docker-entrypoint-initdb.d ディレクトリにある .sh ファイルが実行される
COPY --link ./docker/mariadb/update-gachadata.sh /docker-entrypoint-initdb.d

RUN apt-get update -y && apt-get install wget -y
