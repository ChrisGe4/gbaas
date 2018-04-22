#!/usr/bin/env bash

#wget https://dl.google.com/go/go1.9.4.linux-amd64.tar.gz
#tar xpzf go1.9.4.linux-amd64.tar.gz
#
#cat << EOF >> ~/.profile
#export PATH=$PATH:~/go/bin/
#export GOPATH=/opt/gopath
#export GOROOT=~/go
#EOF
#
#
#curl -sSL https://goo.gl/6wtTN5 | bash -s 1.1.0-rc1

#rm ~/docker-compose*.yaml
#rm -rf ~/crypto-config
#rm -rf ~/channel
#rm ~/cryptogen*.yaml
#rm ~/fabric-ca-server-config*.yaml
#rm ~/configtx.yaml
#rm ~/init-docker.sh
#rm ~/setup.sh
#rm -rf ~/chaincode

docker-compose -f ~/gbaas/docker-compose.yaml down
#docker-compose -f ~/gbaas/docker-compose.yaml kill
#docker-compose -f ~/gbaas/docker-compose.yaml rm -f
#docker-compose down
sleep 5

sudo rm -rf ~/gbaas
rm ~/composer_connection.json
rm ~/*.card
mkdir -p -m u+x gbaas/channel
mkdir  -p -m u+x gbaas/chaincode
mkdir  -p -m u+x gbaas/crypto-config
