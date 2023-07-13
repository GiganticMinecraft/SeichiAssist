# syntax=docker/dockerfile:1.4
FROM mariadb:10.11.4

WORKDIR /docker-entrypoint-initdb.d

# mariadbイメージのドキュメント(https://hub.docker.com/_/mariadb#:~:text=Initializing%20a%20fresh%20instance)によると、
# /docker-entrypoint-initdb.d ディレクトリにある.sqlファイルが初めてmariadbが起動したときに実行される
ADD https://redmine.seichi.click/attachments/download/995/gachadata.sql /docker-entrypoint-initdb.d

RUN sed '1i USE seichiassist;' gachadata.sql
