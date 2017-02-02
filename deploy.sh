#!/usr/bin/env bash

# Args:
# 1. - docker user
# 2. - docker pass

# Continuous deployment script (use ansible instead?)
# Runs step by step:
#  - 1. Get root access/configure/login into docker
#  - 2. Deploy/start postgres image
#  - 3. Configures backup
#  - 4. Stop app image
#  - 5. Deploy/Start app image
#  - 6. Report about statuses

export dockerUser=$1
export dockerPassword=$2

### -- Step 1 -- ###
sudo -E su
set -e

pgContainerName=postgres-oyouin
pgImageName=postgres:9.4
pgDataPath=/data/postgres
pgBackupPath=/data/backup/postgres

appContainerName=oyouin-main
appImageName=oyouin/main
appStaticImagePath=/data/photos
appLogPath=/data/logs

echo "Login to docker hub as ${dockerUser}."
docker login -u ${dockerUser} -p ${dockerPassword}

### -- Step 2 -- ###
if ! docker ps | grep -q ${pgImageName}
then
  echo "Run docker postgres image"
  docker run --name ${pgContainerName} \
           -p 4466:5432 \
           -e POSTGRES_PASSWORD=AQGnthVu73AjBfBF \
           -v ${pgDataPath}:/var/lib/postgresql/data \
           -d \
           ${pgImageName}
fi

### -- Step 3 -- ###

### -- Step 4 -- ###
if docker ps -a | grep -q ${appContainerName}
then
  echo "Stop & remove app container"
  docker stop ${appContainerName}
  docker rm ${appContainerName}
fi

### -- Step 5 -- ###
echo "Run app container"
docker run --name ${appContainerName} \
         -p 3212:8080 \
         -e LOG_DIR=/logs \
         -v ${appStaticImagePath}:/photos \
         -v ${appLogPath}:/logs \
         -v /tmp/.m2:/root/.m2 \
         -d \
         ${appImageName}

### -- Step 6 -- ###
# Check postgres connectivity at first
# Check app connectivity at second
#echo "Checking postgresql connectivity ... "
#if ! lsof -i :4466 -P -n  | grep -q 4466
#then
# echo "OK"
#else
# echo "Failed"
#fi
#
#echo "Checking app connectivity ... "
#if ! lsof -i :8080 -P -n  | grep -q 8080
#then
# echo "OK"
#else
# echo "Failed"
#fi