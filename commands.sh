#!/usr/bin/env bash

#----------------composer------------------------

~/blockchain/artifacts/crypto-config/peerOrganizations/boa.sample.com/users/Admin@boa.sample.com/msp


cd ~/blockchain/composer
composer card create -p ~/blockchain/composer/connection-boa.json -u PeerAdmin -c ~/blockchain/artifacts/crypto-config/peerOrganizations/boa.sample.com/users/Admin@boa.sample.com/msp/signcerts/Admin@boa.sample.com-cert.pem -k ~/blockchain/artifacts/crypto-config/peerOrganizations/boa.sample.com/users/Admin@boa.sample.com/msp/keystore/*_sk -r PeerAdmin -r ChannelAdmin -f PeerAdmin@boa.card

composer card create -p ~/blockchain/composer/connection-google.json -u PeerAdmin -c ~/blockchain/artifacts/crypto-config/peerOrganizations/google.sample.com/users/Admin@google.sample.com/msp/signcerts/Admin@google.sample.com-cert.pem -k ~/blockchain/artifacts/crypto-config/peerOrganizations/google.sample.com/users/Admin@google.sample.com/msp/keystore/*_sk -r PeerAdmin -r ChannelAdmin -f PeerAdmin@google.card

gcloud compute scp --recurse --project hyperledger-poc --zone us-east1-b PeerAdmin@boa.card boa-peer0:~/
gcloud compute scp --recurse --project hyperledger-poc --zone us-east1-b PeerAdmin@google.card google-peer0:~/

gcloud compute scp --recurse --project hyperledger-poc --zone us-east1-b trade-network.bna boa-peer0:~/
gcloud compute scp --recurse --project hyperledger-poc --zone us-east1-b trade-network.bna google-peer0:~/



#ssh to peer0
composer card import -f PeerAdmin@boa.card --card PeerAdmin@boa
composer card import -f PeerAdmin@google.card --card PeerAdmin@google


composer runtime install -c PeerAdmin@boa -n trade-network



loud config set project hyperledger-poc
gcloud config set compute/zone us-east4-a
gcloud config set compute/region us-east4


gcloud container clusters create gbaas \
      --num-nodes 3 \
	  --machine-type n1-standard-2 \
      --scopes cloud-platform


	  gcloud container clusters get-credentials gbaas --zone us-east4-a --project hyperledger-poc


gcloud iam service-accounts keys create  --iam-account chrisge@google.com gcloud_key.json
gcloud iam service-accounts keys create  --iam-account 568020407566-compute@developer.gserviceaccount.com gcloud_key.json