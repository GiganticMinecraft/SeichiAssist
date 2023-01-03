call rd /s /q target\build

call sbt assembly || goto :onerror

call docker compose down
call docker compose up --build

exit /b

:onerror
echo Failed with error. Quiting batch...
exit /b %errorlevel%
