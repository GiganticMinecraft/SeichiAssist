FROM openjdk:8-jdk as builder

WORKDIR /work

ADD https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar BuildTools.jar
# TODO バージョン指定必要
# RUN java -jar BuildTools.jar -o /artifact --rev hoge
RUN java -jar BuildTools.jar -o /artifact

FROM openjdk:8-jdk as runner

COPY --from=builder /artifact/ /spigot/
ADD plugins/ plugins/
ADD eula.txt eula.txt
ADD server.properties server.properties
CMD java -jar /spigot/*.jar nogui
