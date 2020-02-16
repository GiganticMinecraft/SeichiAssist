call sbt assembly

REM https://stackoverflow.com/a/23812179
call echo n | copy /-y docker\spigot\eula.txt docker\spigot\serverfiles\eula.txt

call docker-compose build -m 2g
call docker-compose up
