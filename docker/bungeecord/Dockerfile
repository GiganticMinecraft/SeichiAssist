FROM docker.pkg.github.com/giganticminecraft/bungeesemaphore/bungeesemaphore-jar:c86c959ebc44 as bungeesemaphore-provider

# use seichiassist-runner to overwrite line endings of shell script using nkf
FROM docker.pkg.github.com/giganticminecraft/dockers/seichiassist-runner:3248d2056b2a

WORKDIR /bungeecord
ADD 'https://ci.md-5.net/job/BungeeCord/lastSuccessfulBuild/artifact/bootstrap/target/BungeeCord.jar' BungeeCord.jar

COPY --from=bungeesemaphore-provider /build/BungeeSemaphore.jar /bungeecord-files/plugins/
COPY docker/bungeecord/serverfiles /bungeecord-files/

RUN nkf -Lu --overwrite /bungeecord-files/start-server.sh

CMD bash /bungeecord-files/start-server.sh
