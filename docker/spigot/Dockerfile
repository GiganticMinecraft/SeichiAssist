FROM openjdk:8-jdk as builder

WORKDIR /work

ADD https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar BuildTools.jar
RUN java -jar BuildTools.jar -o /artifact --rev 1.12.2

FROM openjdk:8-jdk as runner

COPY --from=builder /artifact/ /spigot/
COPY localDependencies/ /spigot/plugins/
COPY docker/spigot/serverfiles/ /spigot/

COPY target/build /build/
ADD ./src/main/resources/config.yml /conf/config.yml

WORKDIR /spigot
CMD java -jar /spigot/spigot*.jar nogui
