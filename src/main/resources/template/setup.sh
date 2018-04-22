#!/usr/bin/env bash

docker-compose -f ~/gbaas/docker-compose.yaml down
sleep 5

sudo rm -rf ~/gbaas
rm ~/composer_connection.json
rm ~/*.card
mkdir -p -m u+x gbaas/channel
mkdir  -p -m u+x gbaas/chaincode
mkdir  -p -m u+x gbaas/crypto-config
