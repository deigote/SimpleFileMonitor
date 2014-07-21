#!/bin/bash

GROUP_ID=com.deigote
ARTIFACT_ID=simple-file-monitor
VERSION=$(grep 'version = ' build.gradle | awk -F' = ' '{ print $2}' | cut -d"'" -f2)
BUILD_DIR=build/deploy
JAR_FILE="build/libs/SimpleFileMonitor-$VERSION.jar"
MVN_DIR="$(echo $GROUP_ID | tr '.' '/')/$ARTIFACT_ID"
DEST_BASE_DIR="../github.io/simple-file-monitor/maven/releases"

[ -d "$BUILD_DIR" ] || mkdir -p "$BUILD_DIR"

echo "Building jar..."
gradle jar

if [ ! -f "$JAR_FILE" ]; then
	echo "$JAR_FILE not found! Aborting deployment"
	exit 1
fi

POM_FILE_CONTENT='<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"
      xmlns="http://maven.apache.org/POM/4.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <modelVersion>4.0.0</modelVersion>
   <groupId>'$GROUP_ID_'</groupId>
   <artifactId>'$ARTIFACT_ID'</artifactId>
   <version>'$VERSION'</version>
</project>'

echo "Creating pom file"
echo $POM_FILE_CONTENT > SimpleFileMonitor.pom

echo "Deploying version $VERSION with file $FILE"
mvn install:install-file -Dmaven.repo.local="$BUILD_DIR" -Dfile="$JAR_FILE" \
	-DgroupId=$GROUP_ID -DartifactId=$ARTIFACT_ID -Dversion="$VERSION" \
	-DpomFile=SimpleFileMonitor.pom -Dpackaging=jar -DcreateChecksum=true

echo "Removing pom file"
rm SimpleFileMonitor.pom

ORIGIN_DIR="$BUILD_DIR/$MVN_DIR"
DEST_DIR="$DEST_BASE_DIR/$MVN_DIR"

if [ ! -d "$ORIGIN_DIR" ] ; then
	echo "$ORIGIN_DIR not found! Aborting deployment"
	return 1
fi

echo "Copying build to $DEST_DIR"
[ -d "$DEST_DIR" ] || mkdir -p "$DEST_DIR"
echo cp -R "$ORIGIN_DIR/*" "$DEST_DIR"
cp -R "$ORIGIN_DIR"/* "$DEST_DIR"

echo "Releasing $DEST_DIR"
cd "$DEST_BASE_DIR"
git add .
git ci . -m "Releasing $ARTIFACT_ID version $VERSION"
git push
