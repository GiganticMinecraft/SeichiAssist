call sbt assembly
call robocopy /xc /xn /xo docker\spigot\eula.txt docker\spigot\serverfiles\eula.txt
call docker-compose build -m 2g
call docker-compose up
