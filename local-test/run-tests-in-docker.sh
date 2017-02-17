#!/usr/bin/env bash
docker build --rm=false -t oyouin/local-test-image .
docker run -v `pwd`/..:/code -v ~/.m2:/root/.m2 -v ~/.gradle:/root/.gradle -i oyouin/local-test-image /bin/bash \
   < run-tests.sh
