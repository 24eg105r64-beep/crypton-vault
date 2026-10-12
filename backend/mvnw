#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper Shell Script for macOS and Linux
# ----------------------------------------------------------------------------

set -e

# Resolve maven directory
PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
MAVEN_PROJECTBASEDIR=`dirname "$PRG"`

# Check if system mvn exists in PATH or common Homebrew paths
if command -v mvn >/dev/null 2>&1; then
    exec mvn "$@"
elif [ -x "/opt/homebrew/bin/mvn" ]; then
    exec /opt/homebrew/bin/mvn "$@"
elif [ -x "/usr/local/bin/mvn" ]; then
    exec /usr/local/bin/mvn "$@"
else
    echo "Maven (mvn) was not found in PATH or standard locations (/opt/homebrew/bin/mvn, /usr/local/bin/mvn)."
    echo "Please install Maven or ensure it is in your PATH."
    exit 1
fi
