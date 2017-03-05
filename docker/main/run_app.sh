#!/usr/bin/env bash
export DISPLAY=:99
Xvfb -ac :99 -screen 0 1280x1024x16 &
java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar