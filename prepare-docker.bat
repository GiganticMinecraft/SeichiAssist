call rd /s /q target\build

call sbt assembly || goto :onerror

call docker-compose build -m 2g || goto :onerror

call docker-compose down
call docker-compose up --abort-on-container-exit

exit /b

:onerror
echo Failed with error. Quiting batch...
exit /b %errorlevel%
