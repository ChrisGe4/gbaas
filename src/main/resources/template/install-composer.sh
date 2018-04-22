#!/bin/bash

# Script to install Hyperledger Fabric and Hyperledger Composer on GCP instance
echo Requirement: Ubuntu 16.04 instance in GCP

echo 1. Download and run prerequisites:
#curl -O https://hyperledger.github.io/composer/prereqs-ubuntu.sh
curl -O https://raw.githubusercontent.com/ChrisGe4/blockchain-poc/master/src/main/resources/template/prereqs-ubuntu.sh
chmod u+x prereqs-ubuntu.sh
bash prereqs-ubuntu.sh

echo 2. Update Path
export PATH=$PATH:$HOME/.nvm/versions/node/$(ls $HOME/.nvm/versions/node/)/bin

echo 3. Install composer

npm install -g composer-cli@next

echo 4. Install generator
npm install -g generator-hyperledger-composer@next

echo 5. Install rest server
npm install -g composer-rest-server@next

echo 6. Install yo
npm install -g y

echo 7. Install composer-playground
npm install -g composer-playground@next
