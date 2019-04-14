@echo off
call :wget %1
goto :END

:wget
set URL=%1
set FILENAME=%~n1%~x1
bitsadmin /RawReturn /TRANSFER getfile %URL% %CD%\%FILENAME%
goto :EOF

:END
