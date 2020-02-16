FROM openjdk:8-jdk as builder

WORKDIR /work

ADD https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar BuildTools.jar
RUN java -jar BuildTools.jar -o /artifact --rev 1.12.2

FROM openjdk:8-jdk as runner

COPY --from=builder /artifact/ /spigot/
COPY eula.txt /spigot/eula.txt
COPY server.properties /spigot/server.properties
COPY bukkit.yml /spigot/bukkit.yml
COPY spigot.yml /spigot/spigot.yml
COPY permissions.yml /spigot/permissions.yml

WORKDIR /spigot
CMD java -jar /spigot/spigot*.jar nogui
