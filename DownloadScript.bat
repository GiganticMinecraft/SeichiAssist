@echo off
openfiles > NUL 2>&1 
if NOT %ERRORLEVEL% EQU 0 goto NotAdmin
goto startDownloadprocess

:NotAdmin 
powershell start-process DownloadScript.bat -verb runas
goto End

:startDownloadprocess
title Seichi.click �v���O�C���J���ɂ����郉�C�u�������̃C���X�g�[��
color f0
PowerShell Set-ExecutionPolicy Remotesigned
cd /d %~dp0
set currentDir= %~dp0
echo Q. �J�����̃_�E�����[�h�͍s���܂���?
set /p que="y/n>"
if "%que%"=="n" goto jdkdownload
echo Eclipse 4.4 Luna
set /p que="y/n>"
if "%que%"=="n" goto jdkdownload 
echo Eclipse 4.4 Luna ���_�E�����[�h���܂��B
echo 32bit?
set /p que="y/n>"
if "%que%"=="y" call core http://ftp.jaist.ac.jp/pub/mergedoc/pleiades/4.4/pleiades-e4.4-java-32bit_20150310.zip
if "%que%"=="n" echo for 64Bit
if "%que%"=="n" call core http://ftp.jaist.ac.jp/pub/mergedoc/pleiades/4.4/pleiades-e4.4-java_20150310.zip
echo �_�E�����[�h���I�����܂����B
:jdkdownload
echo JDK���_�E�����[�h�A�C���X�g�[�����܂��B
echo ���̑O��
echo Java�̗��p�K��ɓ��ӂ���K�v������܂��B
echo https://www.oracle.com/technetwork/java/javase/terms/license/index.html
echo �����ł��B
echo ���ӂ��܂���?
set /p que="y/n>"
if "%que%"=="y" goto jdkdl
goto mysqldownload
:jdkdl
powershell %currentDir%\jdkDownload.ps1
echo �𓀂��s���܂�
powershell %currentDir%\ExpandZip.ps1
jdk-x32.exe /s
:mysqldownload
echo Mysql-Connecter-Java�̃_�E�����[�h���s���܂�
call core https://downloads.mysql.com/archives/get/file/mysql-connector-java-gpl-5.1.35.msi plugin Create
echo �_�E�����[�h���I�����܂����B
:libdownload
echo ���C�u�������_�E�����[�h���܂��B
set /p que="y/n>"
if "%que%"=="n" goto sqldownload
echo CoreProtect
call core https://media.forgecdn.net/files/2591/537/CoreProtect-2.14.4.jar libraries Create
echo item-nbt-api
call core https://ci.codemc.org/job/Tr7zw/job/Item-NBT-API/lastSuccessfulBuild/artifact/item-nbt-plugin/target/item-nbt-api-plugin-1.8.2-SNAPSHOT.jar libraries
echo Multiverse-Core
call core https://media.forgecdn.net/files/2428/161/Multiverse-Core-2.5.0.jar libraries
echo Multiverse-Portals
call core https://media.forgecdn.net/files/2428/333/Multiverse-Portals-2.5.0.jar libraries
echo ParticleAPI
copy DownloadLib.ps1 libraries\
powershell %currentDir%\DownloadLib.ps1
cd libraries
del DownloadLib.ps1
cd ..
echo RegenWorld
call core https://red.minecraftserver.jp/attachments/download/890/RegenWorld-1.0.jar libraries
echo SeasonalEvents
call core https://red.minecraftserver.jp/attachments/download/893/SeasonalEvents.jar libraries
echo Spigot 1.12.2
call core https://cdn.getbukkit.org/spigot/spigot-1.12.2.jar libraries
echo WorldBoarder
call core https://media.forgecdn.net/files/2415/838/WorldBorder.jar libraries
echo WorldEdit
call core https://media.forgecdn.net/files/2597/538/worldedit-bukkit-6.1.9.jar libraries
echo WorldGuard
call core https://media.forgecdn.net/files/2610/618/worldguard-bukkit-6.2.2.jar libraries
:sqldownload
echo �ꕔ�K�v��SQL�f�[�^�̃_�E�����[�h���s���܂�
set /p que="y/n>"
if "%que%"=="n" exit
echo �_�E�����[�h���Ă��܂�...
mkdir sql
copy sqlDownloader.ps1 sql\
powershell %currentDir%\sql\sqlDownloader.ps1
cd sql
del sqlDownloader.ps1
cd ..
:End
