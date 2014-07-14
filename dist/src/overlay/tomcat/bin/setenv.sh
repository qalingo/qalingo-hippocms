#!/bin/sh
# Increase the default amount of memory as well as perm space, because there are a lot of strings in the demo.
JAVA_OPTS="$JAVA_OPTS -Xmx512m -Xms256m -XX:PermSize=256m -XX:MaxPermSize=256m"
# Set log4j configuration location
CATALINA_OPTS="$CATALINA_OPTS -Dlog4j.configuration=file:${CATALINA_BASE}/conf/log4j.xml"