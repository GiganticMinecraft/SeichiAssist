FROM openjdk:8-jdk as spigot-builder

WORKDIR /work
ADD https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar BuildTools.jar
RUN java -jar BuildTools.jar -o /artifact --rev 1.12.2

FROM docker.pkg.github.com/giganticminecraft/chunk-search-rs/chunk-search-rs:8673f70e880b as chunk-search-provider

FROM openjdk:8-jdk as runner

# /spigot-filesをコンテナが提供するファイル群とする

COPY --from=chunk-search-provider /build/chunk-search-rs /usr/local/bin/chunk-search-rs
COPY --from=spigot-builder /artifact/ /spigot-files/

COPY docker/spigot/serverfiles/ /spigot-files/
COPY localDependencies/ /spigot-files/plugins/
COPY target/build /spigot-files/plugins/

RUN apt update && apt install nkf
RUN nkf -Lu --overwrite /spigot-files/start-server.sh

CMD bash /spigot-files/start-server.sh
