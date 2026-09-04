#!/usr/bin/env bash

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Attempt to set APP_HOME
SAVE="$(pwd)"
cd "`dirname "$0"`" >/dev/null
APP_HOME="$(pwd -P)"
cd "$SAVE" >/dev/null

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

# OS specific support (must be 'true' or 'false').
darwin=false
case "`uname`" in
  Darwin* )
    darwin=true
    ;;
esac

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/bin/java" ] ; then
        JAVACMD="$JAVA_HOME/bin/java"
    else
        JAVACMD="java"
    fi
else
    JAVACMD="java"
fi

# Provide a "standardized" way to retrieve the CLI args that should be passed to a Java process,
# e.g. to offer a way to say "that Ivy file was found in the it.settings.xml
if [ -z "$GRADLE_OPTS" ] ; then
    GRADLE_OPTS='--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED'
fi

# Collect all arguments for the java command, following the shell quoting and substitution rules
APP_ARGS=""
for arg in "$@" ; do
    APP_ARGS="$APP_ARGS '$arg'"
done

eval set -- $JAVA_OPTS $GRADLE_OPTS -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain $APP_ARGS

exec "$JAVACMD" "$@"
