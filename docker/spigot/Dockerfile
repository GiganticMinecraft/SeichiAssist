FROM openjdk:8-jdk as builder

WORKDIR /work

ADD https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar BuildTools.jar
RUN java -jar BuildTools.jar -o /artifact --rev 1.12

FROM openjdk:8-jdk as runner

COPY --from=builder /artifact/ /spigot/
COPY plugins/ plugins/
COPY eula.txt eula.txt
COPY server.properties server.properties
CMD java -jar /spigot/*.jar nogui
