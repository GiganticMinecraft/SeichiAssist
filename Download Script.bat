@echo off
echo Q. �J�����̃_�E�����[�h�͍s���܂���?
set /p que="y/n>"
if "%que%"=="n" goto libdownload
echo Eclipse 4.4 Luna
set /p que="y/n>"
if "%que%"=="n" goto jdkdownload 
echo Eclipse 4.4 Luna ���_�E�����[�h���܂��B�C���X�g�[����7-zip�Ƃ�����������ł������ǕW���ł͓�������ĂȂ��̂Ŗ����ł��B
echo 32bit?
set /p que="y/n>"
echo Eclipse 4.4 luna ���_�E�����[�h���Ă��܂��B�E�B���h�E����Ȃ��ł��������B
if "%que%"=="y" call core http://ftp.jaist.ac.jp/pub/mergedoc/pleiades/4.4/pleiades-e4.4-java-32bit_20150310.zip eclipse Create
if "%que%"=="n" echo for 64Bit
if "%que%"=="n" call core http://ftp.jaist.ac.jp/pub/mergedoc/pleiades/4.4/pleiades-e4.4-java_20150310.zip eclipse Create
echo �_�E�����[�h���I�����܂����B
:jdkdownload
echo JDK 1.8�̃_�E�����[�h���s���܂�
set /p que="y/n>"
if "%que%"=="n" goto mysqldownload
echo 32bit?
set /p que="y/n>"
echo JDK 1.8 ���_�E�����[�h���Ă��܂��B�E�B���h�E����Ȃ��ł��������B
if "%que%"=="y" call core https://download.oracle.com/otn-pub/java/jdk/8u201-b09/42970487e3af4f5aa5bca3f542482c60/jdk-8u201-windows-i586.exe jdk Create
if "%que%"=="n" echo for 64bit
if "%que%"=="n" call core https://download.oracle.com/otn-pub/java/jdk/8u201-b09/42970487e3af4f5aa5bca3f542482c60/jdk-8u201-windows-x64.exe jdk Create
echo �_�E�����[�h���I�����܂����B
:mysqldownload
echo Mysql-Connecter-Java�̃_�E�����[�h���s���܂�
set /p que="y/n>"
if "%que%"=="n" goto libdownload
echo 32bit?
set /p que="y/n>"
echo Mysql-Connecter-Java ���_�E�����[�h���Ă��܂��B�E�B���h�E����Ȃ��ł��������B
if "%que%"=="y" core call https://downloads.mysql.com/archives/get/file/mysql-connector-java-gpl-5.1.35.msi plugin Create
echo �_�E�����[�h���I�����܂����B
:libdownload
echo ���C�u�������_�E�����[�h���܂�
set /p que="y/n>"
if "%que%"=="n" goto sqldownload
echo CoreProtect
call core https://tt002.mcimserver.net/textures/upload/CoreProtect-2.14.4.jar lib Create
echo ����
echo item-nbt-api
call core https://tt002.mcimserver.net/textures/upload/item-nbt-api-plugin-1.8.2-SNAPSHOT.jar lib
echo ����
echo Multiverse-Core
call core https://media.forgecdn.net/files/2428/161/Multiverse-Core-2.5.0.jar lib
echo ����
echo Multiverse-Portals
call core https://media.forgecdn.net/files/2428/333/Multiverse-Portals-2.5.0.jar lib
echo ����
echo ParticleAPI
call core https://tt002.mcimserver.net/textures/upload/ParticleAPI_v2.1.1.jar lib
echo ����
echo RegenWorld
call core https://red.minecraftserver.jp/attachments/download/890/RegenWorld-1.0.jar lib
echo ����
echo SeasonalEvents
call core https://red.minecraftserver.jp/attachments/download/893/SeasonalEvents.jar lib
echo ����
echo SeichiAssist
call core https://tt002.mcimserver.net/textures/upload/SeichiAssist.jar lib
echo ����
echo Spigot 1.12.2
call core https://cdn.getbukkit.org/spigot/spigot-1.12.2.jar lib
echo ����
echo WorldBoarder
call core https://media.forgecdn.net/files/2415/838/WorldBorder.jar lib
echo ����
echo WorldEdit
call core https://media.forgecdn.net/files/2597/538/worldedit-bukkit-6.1.9.jar lib
echo ����
echo WorldGuard
call core https://media.forgecdn.net/files/2610/618/worldguard-bukkit-6.2.2.jar lib
echo ����
:sqldownload
echo �ꕔ�K�v��SQL�f�[�^�̃_�E�����[�h���s���܂�
set /p que="y/n>"
if "%que%"=="n" exit
echo �_�E�����[�h���Ă��܂�...
call core https://red.minecraftserver.jp/attachments/download/892/gachadata.sql sql
call core https://red.minecraftserver.jp/attachments/download/891/msgachadata.sql sql
echo �������܂����B
pause
:end
