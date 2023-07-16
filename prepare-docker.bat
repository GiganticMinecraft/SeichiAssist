call rd /s /q target\build

call sbt assembly || goto :onerror

if "%1" == "update-gachadata" (
    call docker compose down
    echo Updating gachadata...
    call docker compose up -d db

    REM ここで遅延を入れないとdbが起動する前にgachadataを更新するスクリプトが走ってしまう
    timeout 3

    call docker exec -it seichiassist-db-1 /docker-entrypoint-initdb.d/update-gachadata.sh
    echo Completed updating gachadata.
)

call docker compose down

call docker compose up --build

exit /b

:onerror
echo Failed with error. Quiting batch...
exit /b %errorlevel%
