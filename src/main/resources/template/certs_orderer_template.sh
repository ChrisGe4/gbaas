#!/usr/bin/env bash
export FABRIC_CFG_PATH=$PWD

echo "Generating crypto material with cryptogen"
bash -c "sleep 2 && ./bin/cryptogen generate --config=cryptogen.yaml"
echo "Generating orderer genesis block with configtxgen"
mkdir -p ~/channel
./bin/configtxgen -profile OrdererGenesis -outputBlock ~/channel/genesis.block
#hardcode channel name
echo "Generating channel config transaction for common"
./bin/configtxgen -profile common -outputCreateChannelTx "~/channel/common.tx" -channelID common

./bin/configtxgen -profile common -outputAnchorPeersUpdate ~/channel/ORG1MSPanchors.tx -channelID common -asOrg ORG1MSP
./bin/configtxgen -profile common -outputAnchorPeersUpdate ~/channel/ORG2MSPanchors.tx -channelID common -asOrg ORG2MSP




