@echo off
set Dir=%2
set ifcreate=%3
if "%ifcreate%"=="Create" mkdir %Dir%
call :wget %1 %2
echo �����B
goto :END

:wget
set URL=%1
set Dir=%2
set FILENAME=%~n1%~x1
echo %1 ��� %~n1%~x1 ���_�E�����[�h���܂��B�E�B���h�E����Ȃ��ł��������B
bitsadmin /RawReturn /TRANSFER getfile %URL% %CD%\%Dir%\%FILENAME%
goto :EOF

:END
