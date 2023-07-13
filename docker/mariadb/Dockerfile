# syntax=docker/dockerfile:1.4
FROM mariadb:10.11.4

WORKDIR /docker-entrypoint-initdb.d

RUN apt-get update && \
    apt-get install -y wget && \
    wget https://redmine.seichi.click/attachments/download/995/gachadata.sql && \
    sed '1i USE seichiassist;' gachadata.sql
