@echo off
set Dir=%2
set ifcreate=%3
if "%ifcreate%"=="Create" mkdir %Dir%
set URL=%1
set Dir=%2
set FILENAME=%~n1%~x1
echo %1 より %~n1%~x1 をダウンロードします。ウィンドウを閉じないでください。
bitsadmin /RawReturn /TRANSFER getfile %URL% %CD%\%Dir%\%FILENAME%
goto :EOF
echo 完了。

