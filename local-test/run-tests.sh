#!/usr/bin/env bash
Xvfb -ac :99 -screen 0 1280x1024x16 &
cd /code
./gradlew clean -Dos.name=Linux -Dtest.single=AvitoAdvertsProviderComplexTest \
      --stacktrace --debug test