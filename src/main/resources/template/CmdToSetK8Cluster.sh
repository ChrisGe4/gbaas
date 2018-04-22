#!/usr/bin/env bash
#Enable VM api
#Kubernetes engine-> create clusters  note: when user image, add a tag, dont use default latest version    term: coordinate
#note: closely related process can be run in same pod, will be scaled together   single unit of work  share same ip
# use cloud shell will grab the credential for you. use console you need to run cmd to grab it
#vi ~/.kube/config contains the credential

#in yaml, turn off sytax by typing  :syntax off


kubectl    create==insert    apply==upsert

--record will record cmd in metadata

-oyaml  output yaml file
 you can output the yaml from master

 "fluentd" process/per node to fetch logs from docker logs - var.log,  each log is a json containing pod info.  One pod of fluentd running by k8

 in yaml, should lable everything.  learn more about  "selector" --> like select query,  in service use that to target servcies to "load balace?"

 gcePersistentDisk  -> read only then can be shared among pods   or use clould sql .  In the example, the volumn can only to use in one VM.


 dynamic provisioning to create compute disk

 name space

 deployment is the service itself    service serves like LB,  i.e. you can have prod service pointing to the pods have label prod

kubectl get -n (name space)    get daemonset    get

use both probe,  at least readyneedprobe   ,  can use script to check system


augument over??? config map


statefulSet  like  zookeeper   0 index  is usually the master   ping couchbase-0.couchbase  service name as suffix

getent hosts  SERVICE