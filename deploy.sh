#!/usr/bin/env bash

export dockerUser=$1
export dockerPassword=$2

sudo -E su
set -e

docker login -u ${dockerUser} -p ${dockerPassword}
cd /breezy && docker-compose -f docker-compose.yml -f docker-compose.production.yml up -d