FROM openjdk:8-jdk as builder

WORKDIR /work

ADD https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar BuildTools.jar
RUN java -jar BuildTools.jar -o /artifact --rev 1.12.2

FROM openjdk:8-jdk as runner

# /spigot-filesをコンテナが提供するファイル群にする

COPY --from=builder /artifact/ /spigot-files/
COPY localDependencies/ /spigot-files/plugins/
COPY docker/spigot/serverfiles/ /spigot-files/

COPY target/build /spigot-files/plugins/

ADD ./src/main/resources/config.yml /spigot-files/plugins/SeichiAssist/config.yml
