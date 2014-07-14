@echo off
rem Increase the default amount of memory as well as perm space, because there are a lot of strings in the demo.
set "JAVA_OPTS=%JAVA_OPTS% -Xmx512m -Xms256m -XX:PermSize=256m -XX:MaxPermSize=256m"
rem Set log4j configuration location
set "CATALINA_OPTS=%CATALINA_OPTS% -Dlog4j.configuration=file:%CATALINA_BASE%\conf\log4j.xml"