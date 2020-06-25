FROM openjdk:8-jdk as spigot-builder

WORKDIR /work
ADD https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar BuildTools.jar
RUN java -jar BuildTools.jar -o /artifact --rev 1.12.2

FROM docker.pkg.github.com/giganticminecraft/inventorysearch/inventory-search:dfb78f558125 as inventory-search-provider

FROM openjdk:8-jdk as runner

# /spigot-filesをコンテナが提供するファイル群とする

COPY --from=spigot-builder /artifact/ /spigot-files/
COPY docker/spigot/serverfiles/ /spigot-files/

COPY localDependencies/ /spigot-files/plugins/
COPY target/build /spigot-files/plugins/

ADD ./src/main/resources/config.yml /spigot-files/plugins/SeichiAssist/config.yml

COPY --from=inventory-search-provider /build/ /inventory-search/

ADD https://packages.microsoft.com/config/ubuntu/20.04/packages-microsoft-prod.deb packages-microsoft-prod.deb
RUN dpkg -i packages-microsoft-prod.deb
RUN apt update && apt install -y apt-transport-https && apt update && apt install -y dotnet-runtime-3.1
