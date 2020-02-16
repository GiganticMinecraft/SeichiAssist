#!/bin/sh
sbt assembly
docker-compose down
docker-compose up -d