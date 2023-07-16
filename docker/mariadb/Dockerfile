# syntax=docker/dockerfile:1.4
FROM mariadb:10.11.4

WORKDIR /docker-entrypoint-initdb.d

COPY --link ./docker/mariadb/update-gachadata.sh /

# TODO: 定期的にdumpされたgachadata.sqlの最新版をダウンロードできるようにする https://github.com/GiganticMinecraft/SeichiAssist/issues/2172
# mariadbイメージのドキュメント(https://hub.docker.com/_/mariadb#:~:text=Initializing%20a%20fresh%20instance)によると、
# 初めて mariadb が起動したときに /docker-entrypoint-initdb.d ディレクトリにある .sql ファイルが実行される
ADD https://redmine.seichi.click/attachments/download/997/gachadata.sql /docker-entrypoint-initdb.d

RUN sed '1i USE seichiassist;' gachadata.sql && apt-get update -y && apt-get install wget -y
