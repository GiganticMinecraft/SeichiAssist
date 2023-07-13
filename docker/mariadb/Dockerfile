# syntax=docker/dockerfile:1.4
FROM mariadb:10.11.4

WORKDIR /docker-entrypoint-initdb.d

ADD https://redmine.seichi.click/attachments/download/995/gachadata.sql /docker-entrypoint-initdb.d

RUN sed '1i USE seichiassist;' gachadata.sql
