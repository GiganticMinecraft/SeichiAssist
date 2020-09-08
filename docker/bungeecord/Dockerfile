FROM openjdk:8-jdk

WORKDIR /work
ADD 'https://ci.md-5.net/job/BungeeCord/lastSuccessfulBuild/artifact/bootstrap/target/BungeeCord.jar' BungeeCord.jar
COPY config.yml config.yml
CMD java -jar BungeeCord.jar