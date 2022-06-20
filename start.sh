#!/bin/bash
echo "Updating package..."
git pull -q
echo "Package updated!"
echo "Building application..."
mvn clean
mvn package -q
echo "Applicaiton ready!"
echo "Deploying application..."
java -jar target/werewolves-server-0.0.1-SNAPSHOT.jar
echo "Done!"
