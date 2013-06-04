echo "Simple-Proxy-Master launch"

@echo off
setlocal

if "%OS%"=="Windows_NT" goto nt
echo This script only works with NT-based versions of Windows.
goto :eof

:nt
rem @echo Obteniendo lib/*.jar
rem set CLASSPATH=.
rem FOR %%f IN (lib\*.jar) DO (call :append_classpath %%f)

set CLASSPATH=.\target\simple-proxy-1.0-SNAPSHOT.jar
echo CLASSPATH = %CLASSPATH%

java -cp %CLASSPATH% com.carlosprados.lab.simpleproxy.Proxy 172.19.17.109 1522 172.19.17.109 1521 60000
rem java -cp %CLASSPATH% com.carlosprados.lab.simpleproxy.Proxy 172.19.17.109 1522 158.130.67.154 80 60000
rem java -cp %CLASSPATH% com.carlosprados.lab.simpleproxy.Proxy 172.19.17.109 1522 173.194.34.213 443 60000

pause
goto :eof

:append_classpath
set CLASSPATH=%CLASSPATH%;%1
GOTO :eof

echo "bye"