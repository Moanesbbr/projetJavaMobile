#!/bin/bash

# Set Java 17 as JAVA_HOME for this script
export JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

# Verify Java version
java -version

# Run Gradle with Java 17
./gradlew assembleDebug 