#!/bin/sh
APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`
DIRNAME=`dirname "$0"`
JARFILE="$DIRNAME/gradle/wrapper/gradle-wrapper.jar"
JAVA_HOME="${JAVA_HOME:-}"
if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi
exec "$JAVACMD" -Xmx64m -Xms64m -jar "$JARFILE" "$@"
