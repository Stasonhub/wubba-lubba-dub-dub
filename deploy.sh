#!/bin/bash

# Continuous deployment script (use ansible instead?)
# Runs step by step:
#  - Deploy/start postgres image
#  - Configures backup
#  - Stop app image
#  - Deploy/Start app image
#  - Report about statuses
sudo su

pgImageName=postgres-oyouin
pgDataPath=/data/postgres
pgBackupPath=/data/backup/postgres

appImageName=oyouin/main
appStaticImagePath=/data/photos

# Deploy/start postgres image
mkdir -p ${pgDataPath}
if 0 -eq docker ps | grep -q ${pgImageName}
then
  docker run --name ${pgImageName} \
           postgresql:9.4 \
           -p 4466:5432 \
           -e POSTGRES_PASSWORD=AQGnthVu73AjBfBF \
           -d postgres \
           -v ${pgDataPath}:/var/lib/postgresql/data
fi

# !!!! Configure backup for postgres

# Stop app image
if 0 -eq docker ps | grep -q ${appImageName}
then
  docker stop ${appImageName}
fi

# Deploy/Start app image
docker start ${appImageName} \
          -v ${appStaticImagePath}:/photos

# Report statuses
# Check postgres connectivity at first
# Check app connectivity at second
echo "Checking postgresql connectivity ... "
if 0 -eq lsof -i :4466 -P -n  | grep -q 4466
then
 echo "OK"
else
 echo "Failed"
fi

echo "Checking app connectivity ... "
if 0 -eq lsof -i :8080 -P -n  | grep -q 8080
then
 echo "OK"
else
 echo "Failed"
fi