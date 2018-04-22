#!/usr/bin/env bash

PREFIX=-peer-

#########################
# The command line help #
#########################
display_help() {
    echo
    echo "Usage: $0 -p test1 -g hyperledger-poc -z us-east1-b -n 1" >&2
    echo
    echo "   -p            project name. It will be used as part of the prefix of the peer name"
    echo "   -g            GCP Project name"
    echo "   -z            GCP zone name"
    echo "   -n            Number of peers"
    echo
    exit 1
}

while getopts p:g:z:n::hhelp option
do
 case "${option}"
 in
 h|help) display_help;;
 p) PROJECT=${OPTARG};;
 g) GCP_PROJECT=${OPTARG};;
 z) ZONE=${OPTARG};;
 n) NUM=$OPTARG;;
 esac
done

echo ZONE                    = "${ZONE}"
echo INSTANCE_PREFIX         = "${PREFIX}"
echo GCP_PROJECT             = "${GCP_PROJECT}"
echo NUMBER_OF_INSTANCES     = "${NUM}"

echo Provisioning VMs

let "NUM += 1"
echo creatintg $NUM instances

for i in `seq ${NUM}`; do
     peerName=$PROJECT$PREFIX$i
     echo creating peer $peerName
     $(exec gcloud config set project ${GCP_PROJECT} )
     $(exec gcloud compute instances create ${peerName} --zone ${ZONE} --machine-type "custom-1-6144" --image "ubuntu-1604-xenial-v20180109" --image-project "ubuntu-os-cloud" --boot-disk-size "10" --boot-disk-type "pd-standard" --boot-disk-device-name ${peerName})
done

sleep 30


NAMES=$(gcloud compute instances list --filter="name~'"${PROJECT}"' AND status~'RUNNING'" --format=text | grep '^name:' | sed 's/^.* //g')

for peer in $NAMES; do
  echo ">>> Copy scripts to peer"
  echo Copying scripts at "${peer}"
  echo gcloud compute scp --zone $ZONE "install-scripts/*" "${peer}:~/"
  $(gcloud compute scp --project "${GCP_PROJECT}" --zone $ZONE install-scripts/* "${peer}:~/")

  #2. Run install prerequisites and composer on machine
  echo "  gcloud compute ssh $peer --zone $ZONE --command bash install-composer.sh"
  $(gcloud compute ssh ${peer}  --zone ${ZONE} --command "chmod u+x install-composer.sh ;chmod u+x install-fabric.sh ; chmod u+x install-tools.sh")
  $(gcloud compute ssh ${peer}  --zone ${ZONE} --command " bash install-composer.sh ")

  echo Run install Fabric on $peer
  #5. Run install Fabric on machine
  $(gcloud compute ssh ${peer} --zone ${ZONE} --command "bash install-fabric.sh")


#  $(gcloud compute ssh ${peer} --zone ${ZONE} --command "bash install-tools.sh")



  echo Done Installation on $peer
done


