#!/usr/bin/env bash
bash -c "sleep 2 && ~/bin/cryptogen generate --config=cryptogen.yaml"
ca_private_key=$(basename `ls -t ./crypto-config/peerOrganizations/ORG.DOMAIN/ca/*_sk`)
[[ -z  ${ca_private_key}  ]] && echo "empty CA private key"
# && exit 1
sed -i -e "s/CA_PRIVATE_KEY/${ca_private_key}/g" docker-compose.yaml

f="docker-compose.yaml"
d="crypto-config/peerOrganizations/ORG.DOMAIN/peers/PEER_NAME.ORG.DOMAIN/tls"
echo "Copying generated TLS cert files from $d to be served by www.ORG.DOMAIN"
mkdir -p "www/${d}"
cp "${d}/ca.crt" "www/${d}"
d="crypto-config/peerOrganizations/ORG.DOMAIN"
echo "Copying generated MSP cert files from $d to be served by www.PEER_NAME.ORG.DOMAIN"
cp -r "${d}/msp" "www/${d}"
docker-compose --file ${f} up -d "www.PEER_NAME.ORG.DOMAIN"

