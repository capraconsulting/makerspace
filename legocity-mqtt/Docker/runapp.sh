#!/bin/sh
set -e

# The module to start.
APP_JAR="/application/application.jar"

echo " --- RUNNING $(basename "$0") $(date -u "+%Y-%m-%d %H:%M:%S Z") --- "
set -x

# Print some debug info about the JVM and Heap
exec "/usr/bin/java" \
  -XX:+PrintFlagsFinal \
  -version | grep Heap

exec "/usr/bin/java" \
  -jar "$APP_JAR"