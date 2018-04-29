package org.cg.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.cg.config.AppConfiguration;
import org.cg.error.CommandFailedToRunException;
import org.cg.pojo.NetworkConfig;
import org.cg.pojo.Property;
import org.cg.pojo.composer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

/**
 * @author Chris.Ge
 */
@Component
public class DeploymentService {

    public static final String LIST_INSTANCES = "bash gcloud compute instances list";
    public static final String SET_PROJECT = "gcloud config set project ";
    public static final String CREATE_VM =
        "gcloud compute instances create :PEERNAME --zone :ZONE --machine-type \"custom-1-6144\" --image \"ubuntu-1604-xenial-v20180418\" --image-project \"ubuntu-os-cloud\" --boot-disk-size \"20\" --boot-disk-type \"pd-standard\" --boot-disk-device-name :PEERNAME ";
    public static final String SCP = "bash "
        + "gcloud compute scp --recurse --project GCP_PROJECT --zone ZONE FILE PEERNAME:~/DEST_FILE";
    public static final String SCP_TO_SERVER = "bash "
        + "gcloud compute scp --recurse --project GCP_PROJECT --zone ZONE  PEERNAME:~/FILE DEST";
    public static final String SSH = "bash " + "gcloud compute ssh ";
    private static final Logger log = LoggerFactory.getLogger(DeploymentService.class);
    public static String ORG_NAMES = "                    - *";
    public static String PROJECT_VM_DIR = "gbaas/";
    public static String PEER_NAME_SUFFIX = "-peer";
    public static String EXTRA_HOSTS_PREFIX = "       - ";
    public static String CONTAINER_WORKING_DIR = "/etc/hyperledger/artifacts/";
    public static String DOCKER_CMD =
        "docker-compose -f ./gbaas/docker-compose.yaml run CLI bash -c \\\"COMMAND\\\"";
    public static String MSP_SUFFIX = "MSP";
    public static String PEER_CA_FILE =
        "crypto-config/peerOrganizations/ORG.DOMAIN/peers/peer0.ORG.DOMAIN/tls/ca.crt";
    public static String CRYPTO_FOLDER_FOR_COMPOSER =
        PROJECT_VM_DIR + "crypto-config/peerOrganizations/ORG.DOMAIN/users/Admin@ORG.DOMAIN/msp/";
    public static String ORG_PLACEHOLDER = "ORG";
    public static String DOMAIN_PLACEHOLDER = "DOMAIN";
    public static String CONNECTION_FILE_NAME_TEMPLATE = "NETWORKNAME-connection-ORG";
    public static String ANCHORPEER_CMD =
        "peer channel update -o ORDERER_HOST:ORDERER_PORT -c CHANNEL_NAME -f ANCHOR_FILE --tls --cafile ORDERER_CA";
    public static String ORDERER_CA_IN_CONTAINER = "/etc/hyperledger/crypto/orderer/tls/ca.crt";
    public static String ANCHOR_FILE_SUFFIX = "anchors.tx";
    public static String COMMPOSER_FW_DIR =
        "/usr/local/google/home/chrisge/.nvm/versions/node/v9.11.1/bin/";
    public static String CREATE_CARD_CMD =
        "composer card create -p CONNECTION_JSON -u PeerAdmin -c ADMIN_PEM -k SK_FILE -r PeerAdmin -r ChannelAdmin -f PeerAdmin@NAME.card";
    public static String COMPOSER_IMPORT_CARD_CMD =
        "composer card import -f PeerAdmin@NAME.card --card PeerAdmin@NAME";
    public static String COMPOSER_DELETE_CARD_CMD = "composer card delete -c PeerAdmin@NAME";

    public static String ORDERER_CA_FILE =
        "crypto-config/ordererOrganizations/DOMAIN/orderers/orderer.DOMAIN/tls/ca.crt";
    private final ObjectMapper mapper;
    private final AppConfiguration appConfiguration;
    public String cryptoGenCmd;
    private String workingDir;
    private String scriptFile;
    private String cryptoPath;
    private String composerPath;


    @Autowired
    public DeploymentService(ObjectMapper mapper, AppConfiguration appConfiguration) {
        this.mapper = mapper;
        this.appConfiguration = appConfiguration;

    }

    public static void main(String[] args) {

        ObjectMapper mapper = new ObjectMapper();
        AppConfiguration appConfiguration = new AppConfiguration();
        Property property1 = new Property();
        property1.setOrg("google");
        property1.setNumOfPeers(2);
        Property property2 = new Property();
        property2.setOrg("boa");
        property2.setNumOfPeers(2);
        NetworkConfig config = new NetworkConfig();
        config.setGcpProjectName("hyperledger-poc");
        config.setOrdererName("orderer-google-boa");
        config.setGcpZoneName("us-east1-b");
        config.setChannelName("common");
        config.setProperties(Lists.newArrayList(property1, property2));
        DeploymentService ds = new DeploymentService(mapper, appConfiguration);

        //Map<String, Map<String, String>> orgNameIpMap = ds.deployFabric(config, false, false);

        //for testing
        Map<String, Map<String, String>> orgNameIpMap = ds.getInstanceNameIPMap(config);
        //
        // ds.createComposerConnectionFile(orgNameIpMap, config);
        // ds.createComposerAdminCard(orgNameIpMap, config);
        // ds.runScript();

    }


    public Map<String, Map<String, String>> deployFabric(NetworkConfig config,
        boolean createInstance, boolean installSoftware) {
        initialEnvVariables(config);
        if (createInstance) {
            createInstances(config);
        }
        Map<String, Map<String, String>> orgNameIpMap = getInstanceNameIPMap(config);
        log.info("The instance list is " + orgNameIpMap);
        initService(config);

        copyInstallScripts(orgNameIpMap, config);
        copyInstallScripts(orgNameIpMap, config);
        setupVm(orgNameIpMap, config);
        if (installSoftware) {
            setupDocker(orgNameIpMap, config);
            setupComposer(orgNameIpMap, config);
        }
        createCrypto(orgNameIpMap, config);
        Map<String, String> orgPk = createCryptoFiles(orgNameIpMap, config);
        createConfigtxYaml(orgNameIpMap, config);
        createConfigtxFiles(orgNameIpMap, config);

        createDockerComposeFiles(orgNameIpMap, config, orgPk);
        createCaServerYaml(orgNameIpMap, config);

        distributeCerts(orgNameIpMap, config);
        distributeTx(orgNameIpMap, config);
        startOrderer(orgNameIpMap, config);
        distributeChaincode(orgNameIpMap, config);

        startContainers(orgNameIpMap, config);
        createChannelBlock(orgNameIpMap, config);
        distributeChannelBlock(orgNameIpMap, config);
        joinChannel(orgNameIpMap, config);
        updateAnchorPeer(orgNameIpMap, config);
        // ds.installChaincode(orgNameIpMap, config);
        //may not need this
        //ds.copyChannelBlockToContainer(orgNameIpMap, config);
        return orgNameIpMap;
    }

    public void deployComposer(NetworkConfig config,
        Map<String, Map<String, String>> orgNameIpMap) {
        initialEnvVariables(config);
        createScriptFile(config);
        createComposerConnectionFile(orgNameIpMap, config);
        createComposerAdminCard(orgNameIpMap, config);

    }

    private void initialEnvVariables(NetworkConfig config) {

        workingDir = appConfiguration.WORKING_DIR + config.getNetworkName() + "/";
        composerPath = workingDir + "composer/";
        cryptoGenCmd = "~/bin/cryptogen generate --output=" + workingDir + "crypto-config --config="
            + workingDir + "cryptogen.yaml";
        scriptFile = workingDir + "script.sh";
        cryptoPath = workingDir + "crypto-config/";

    }

    public void initService(NetworkConfig config) {

        try {
            deleteFolders(workingDir);
            createDir(workingDir);
            createScriptFile(config);
            Files.copy(Paths.get(Resources.getResource("template/base.yaml").toURI()),
                Paths.get(workingDir, "base.yaml"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(Paths.get(Resources.getResource("template/init-docker.sh").toURI()),
                Paths.get(workingDir, "init-docker.sh"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(Paths.get(Resources.getResource("template/setup.sh").toURI()),
                Paths.get(workingDir, "setup.sh"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(Paths.get(Resources.getResource("template/install-composer.sh").toURI()),
                Paths.get(workingDir, "install-composer.sh"), StandardCopyOption.REPLACE_EXISTING);
        } catch (Throwable t) {
            throw new RuntimeException("Cannot init the service ", t);
        }

    }


    private void createScriptFile(NetworkConfig config) {
        try {
            Files.deleteIfExists(Paths.get(scriptFile));
            Files.write(Paths.get(scriptFile),
                ("export PATH=$PATH:" + appConfiguration.GCLOUD_DIR + "\n").getBytes(),
                StandardOpenOption.CREATE);
            appendToFile(scriptFile, SET_PROJECT + config.getGcpProjectName());

            log.info("script file created");
            appendToFile(scriptFile, "");
        } catch (Throwable t) {
            throw new RuntimeException("Cannot create script file ", t);
        }
    }

    public void createInstances(NetworkConfig config) {

        try {
            for (Property property : config.getProperties()) {
                for (int i = 0; i < property.getNumOfPeers(); i++) {
                    CommandRunner.runCommand(Lists.newArrayList("/bin/bash", "-c",
                        appConfiguration.GCLOUD_DIR + CREATE_VM
                            .replaceAll(":PEERNAME", property.getOrg() + PEER_NAME_SUFFIX + i)
                            .replaceAll(":ZONE", config.getGcpZoneName())), log);
                }
            }
            CommandRunner.runCommand(Lists.newArrayList("/bin/bash", "-c",
                appConfiguration.GCLOUD_DIR + CREATE_VM
                    .replaceAll(":PEERNAME", config.getOrdererName())
                    .replaceAll(":ZONE", config.getGcpZoneName())), log);
        } catch (Throwable t) {

            throw new CommandFailedToRunException("Cannot create instances", t);
        }
    }


    public Map<String, Map<String, String>> getInstanceNameIPMap(NetworkConfig config) {
        List<String> orgs =
            config.getProperties().stream().map(Property::getOrg).collect(Collectors.toList());
        orgs.add(config.getOrdererName());
        Map<String, Map<String, String>> orgNameIpMap = new HashMap<>();
        try {
            List<String> vms =
                CommandRunner.runCommand(appConfiguration.GCLOUD_DIR, LIST_INSTANCES);

            for (String org : orgs) {
                Map<String, String> nameIpMap = getNameIpMap(vms, org);
                orgNameIpMap.put(org, nameIpMap);
            }
        } catch (Throwable t) {

            throw new CommandFailedToRunException("Cannot get the list of instances", t);
        }

        return orgNameIpMap;
    }

    private Map<String, String> getNameIpMap(List<String> vms, String org) {
        //order of the list of peers is eg google-peer-0, google-peer-1 ...
        Map<String, String> nameIpMap = new LinkedHashMap<>();
        vms.stream().map(s -> s.split("\\s+"))
            .filter(a -> a[0].startsWith(org) && "RUNNING".equals(a[a.length - 1])).forEach(a -> {
            nameIpMap.put(a[0], a[8]);
        });
        return nameIpMap;
    }


    public void copyInstallScripts(Map<String, Map<String, String>> orgNameIpMap,
        NetworkConfig config) {
        for (String org : orgNameIpMap.keySet()) {
            String domain = String.join(".", org, appConfiguration.DOMAIN);
            for (String instance : orgNameIpMap.get(org).keySet()) {
                try {
                    copyFileToGcpVm(workingDir + "init-docker.sh", "init-docker.sh", instance,
                        config);
                    copyFileToGcpVm(workingDir + "setup.sh", "setup.sh", instance, config);

                    copyFileToGcpVm(workingDir + "install-composer.sh", "install-composer.sh",
                        instance, config);
                    appendToFile(scriptFile, String
                        .join("", SSH, instance, " --zone ", config.getGcpZoneName(),
                            " --command \" chmod u+x init-docker.sh; chmod u+x install-composer.sh; chmod u+x setup.sh\""));
                    appendToFile(scriptFile, "sleep 2");

                } catch (Throwable t) {
                    throw new RuntimeException("Cannot write to script file ", t);
                }


            }
        }


    }

    public void createCrypto(Map<String, Map<String, String>> orgNameIpMap, NetworkConfig config) {
        try {
            log.info("Creating cryptogen.yaml");
            String ordererTemplate = new String(Files.readAllBytes(Paths
                .get(Resources.getResource("template/cryptogentemplate-orderer.yaml").toURI())));

            String peerTemplate = new String(Files.readAllBytes(
                Paths.get(Resources.getResource("template/cryptogentemplate-peer.yaml").toURI())));

            Set<String> orgs = orgNameIpMap.keySet();
            String peersConfig = orgs.stream().filter(o -> !o.equals(config.getOrdererName())).map(
                o -> peerTemplate.replaceAll("ORG", o)
                    .replace("COUNT", String.valueOf(orgNameIpMap.get(o).entrySet().size())))
                .collect(Collectors.joining("\n"));
            String path = workingDir + "cryptogen.yaml";
            Files.write(Paths.get(path), ordererTemplate.replace(":PEERS", peersConfig)
                    .replaceAll("DOMAIN", appConfiguration.DOMAIN).getBytes(),
                StandardOpenOption.CREATE);
            copyFileToGcpVm(path, "cryptogen.yaml", config.getOrdererName(), config);

            log.info("Cryptogen.yaml created");

        } catch (Throwable e) {
            throw new RuntimeException("Cannot create crypto yaml file ", e);
        }

    }

    public Map<String, String> createCryptoFiles(Map<String, Map<String, String>> orgNameIpMap,
        NetworkConfig config) {
        try {
            log.info("Generating crypto files");
            deleteFolders(workingDir + "crypto-config");
            CommandRunner.runCommand(Lists.newArrayList("/bin/bash", "-c", cryptoGenCmd), log);
            Set<String> orgs = Sets.newHashSet(orgNameIpMap.keySet());
            orgs.remove(config.getOrdererName());
            Map<String, String> orgDomainPkMap = new HashMap<>(orgs.size());

            for (String org : orgs) {
                String domain = String.join(".", org, appConfiguration.DOMAIN);
                String path = cryptoPath + "peerOrganizations/" + domain + "/ca/";
                Optional<String> skFile =
                    Files.list(Paths.get(path)).map(f -> f.getFileName().toString())
                        .filter(p -> p.endsWith("_sk"))
                        //.map(f -> f.substring(0, f.indexOf('_')))
                        .findFirst();
                if (skFile.isPresent()) {
                    orgDomainPkMap.put(domain, skFile.get());
                } else {
                    throw new RuntimeException("Cannot file ca files ");
                }
            }
            log.info("Crypto files were created");

            return orgDomainPkMap;

        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Cannot create crypto  files ", t);
        }
    }

    public void distributeCerts(Map<String, Map<String, String>> orgNameIpMap,
        NetworkConfig config) {

        for (String org : orgNameIpMap.keySet()) {
            String domain = String.join(".", org, appConfiguration.DOMAIN);
            for (String instance : orgNameIpMap.get(org).keySet()) {
                try {
                    String path = cryptoPath;
                    copyFileToGcpVm(path, PROJECT_VM_DIR, instance, config);
                } catch (Throwable t) {
                    throw new RuntimeException("Cannot write to script file ", t);
                }


            }
        }
    }


    public void createConfigtxYaml(Map<String, Map<String, String>> orgNameIpMap,
        NetworkConfig config) {

        try {
            String orgTemplate = new String(Files.readAllBytes(
                Paths.get(Resources.getResource("template/configtx-orgtemplate.yaml").toURI())));

            String template = new String(Files.readAllBytes(
                Paths.get(Resources.getResource("template/configtxtemplate.yaml").toURI())));
            Set<String> orgs = orgNameIpMap.keySet();
            String orgNames = orgs.stream().filter(o -> !o.equals(config.getOrdererName()))
                .map(o -> ORG_NAMES + o).collect(Collectors.joining("\n"));

            String orgConfigs =
                orgs.stream().filter(o -> !o.equals(config.getOrdererName())).map(o ->

                    orgTemplate.replaceAll("ORG", o)

                ).collect(Collectors.joining(System.lineSeparator()));
            String configtxfPath = workingDir + "configtx.yaml";
            deleteFolders(configtxfPath);
            String content =
                template.replace(":ORG-CONFIGS", orgConfigs).replace("ORGNAMES", orgNames)
                    .replaceAll("DOMAIN", appConfiguration.DOMAIN)
                    .replace("CHANNEL_NAME", config.getChannelName());

            Files.write(Paths.get(configtxfPath), content.getBytes(), StandardOpenOption.CREATE);
            copyFileToGcpVm(configtxfPath, "configtx.yaml", config.getOrdererName(), config);

        } catch (Throwable e) {
            throw new RuntimeException("Cannot create configtx yaml file ", e);
        }


    }

    public void createConfigtxFiles(Map<String, Map<String, String>> orgNameIpMap,
        NetworkConfig config) {

        try {
            String dir = workingDir + "channel/";
            deleteFolders(dir);
            createDir(dir);

            appendToFile(scriptFile, "export FABRIC_CFG_PATH=" + workingDir);
            log.info("Generating channel config transaction for %s channel",
                config.getChannelName());
            appendToFile(scriptFile, String
                .join("", "~/bin/configtxgen -profile OrdererGenesis -outputBlock ", dir,
                    "genesis.block"));
            appendToFile(scriptFile, String
                .join("", "~/bin/configtxgen -profile ", config.getChannelName(),
                    " -outputCreateChannelTx ", dir, config.getChannelName(), ".tx -channelID ",
                    config.getChannelName()));

            Set<String> orgs = Sets.newHashSet(orgNameIpMap.keySet());
            orgs.remove(config.getOrdererName());

            for (String org : orgs) {
                appendToFile(scriptFile, String
                    .join("", "~/bin/configtxgen -profile ", config.getChannelName(),
                        " -outputAnchorPeersUpdate ", dir, org, "MSPanchors.tx -channelID ",
                        config.getChannelName(), " -asOrg ", org, "MSP"));
            }

        } catch (Throwable e) {
            throw new RuntimeException("Cannot create channel related files ", e);
        }

    }

    public void distributeTx(Map<String, Map<String, String>> orgNameIpMap, NetworkConfig config) {
        log.info("Distributing tx files");
        for (String org : orgNameIpMap.keySet()) {
            for (String instance : orgNameIpMap.get(org).keySet()) {
                try {
                    String path = workingDir + "channel";
                    appendToFile(scriptFile, "echo copying channel folder to " + instance);
                    copyFileToGcpVm(path, PROJECT_VM_DIR, instance, config);
                } catch (Throwable t) {
                    throw new RuntimeException("Cannot write to script file ", t);
                }


            }
        }


    }

    //todo: make it configurable
    public void distributeChaincode(Map<String, Map<String, String>> orgNameIpMap,
        NetworkConfig config) {
        log.info("Calling distributeChaincode method");
        try {

            FileUtils.copyDirectory(new File(Resources.getResource("chaincode").toURI()),
                new File(Paths.get(workingDir, "chaincode").toUri()), false);

        } catch (Throwable t) {
            throw new RuntimeException("Cannot copy chaincode to from resource", t);
        }
        for (String org : orgNameIpMap.keySet()) {
            for (String instance : orgNameIpMap.get(org).keySet()) {
                try {
                    String path = workingDir + "chaincode";

                    appendToFile(scriptFile, "echo copying chaincode folder to " + instance);
                    copyFileToGcpVm(path, PROJECT_VM_DIR, instance, config);
                } catch (Throwable t) {
                    throw new RuntimeException("Cannot write to script file ", t);
                }
            }
        }


    }


    public void createCaServerYaml(Map<String, Map<String, String>> orgNameIpMap,
        NetworkConfig config) {
        log.info("Creating Ca Server Yaml files");

        try {
            String caTemplate = new String(Files.readAllBytes(Paths.get(
                Resources.getResource("template/fabric-ca-server-configtemplate.yaml").toURI())));

            Set<String> orgs = Sets.newHashSet(orgNameIpMap.keySet());
            orgs.remove(config.getOrdererName());

            for (String org : orgs) {
                String fileName = "fabric-ca-server-config-" + org + ".yaml";
                String caServerYamlFilePath = workingDir + fileName;
                String content = caTemplate.replaceAll("ORG", org);
                Files.write(Paths.get(caServerYamlFilePath), content.getBytes(),
                    StandardOpenOption.CREATE);
                for (String instance : orgNameIpMap.get(org).keySet()) {
                    copyFileToGcpVm(caServerYamlFilePath, PROJECT_VM_DIR + fileName, instance,
                        config);

                }
            }


        } catch (Throwable e) {
            throw new RuntimeException("Cannot create ca server yaml file ", e);
        }

    }

    public void createDockerComposeFiles(Map<String, Map<String, String>> orgNameIpMap,
        NetworkConfig config, Map<String, String> orgDomainPkMap) {
        log.info("Creating docker compose files");

        try {
            String ordererTemplate = new String(Files.readAllBytes(Paths.get(
                Resources.getResource("template/docker-composetemplate-orderer.yaml").toURI())));

            String peerBaseTemplate = new String(Files
                .readAllBytes(Paths.get(Resources.getResource("template/peer-base.yaml").toURI())));

            String peerTemplate = new String(Files.readAllBytes(Paths
                .get(Resources.getResource("template/docker-composetemplate-peer.yaml").toURI())));

            String ordererNameIp = String.join("", "orderer.", appConfiguration.DOMAIN, ":",
                orgNameIpMap.get(config.getOrdererName()).get(config.getOrdererName()));
            Set<String> orgs = Sets.newHashSet(orgNameIpMap.keySet());
            orgs.remove(config.getOrdererName());

            Map<String, String> extraHostsMappingByOrg = orgs.stream()
                //   .filter(o -> !o.equals(config.getOrdererName()))
                .collect(toMap(org -> org, org ->

                    orgNameIpMap.get(org).entrySet().stream().map(e ->

                        String.join("", EXTRA_HOSTS_PREFIX, e.getKey().split("-")[1], ".", org, ".",
                            appConfiguration.DOMAIN, ":", e.getValue()))
                        .collect(Collectors.joining(System.lineSeparator()))

                ));

            String ordererHost =
                String.join("", System.lineSeparator(), EXTRA_HOSTS_PREFIX, ordererNameIp);
            System.out.println("orgs = " + orgs);

            //creating peer docker compose files
            for (String org : orgs) {
                for (String peer : orgNameIpMap.get(org).keySet()) {
                    String extraHosts = String
                        .join("", extraHostsMappingByOrg.get(org), ordererHost,
                            System.lineSeparator());
                    String content = String.join("",
                        peerTemplate.replaceAll("CLI_EXTRA_HOSTS", extraHosts)
                            .replaceAll("CA_PORT", appConfiguration.CA_PORT),
                        System.lineSeparator(),
                        peerBaseTemplate.replaceAll("PEER_PORT", appConfiguration.PEER_PORT)
                            .replaceAll("PEER_EVENT_PORT", appConfiguration.PEER_EVENT_PORT)
                            .replaceAll("PEER_EXTRA_HOSTS", extraHosts))
                        .replaceAll("DOMAIN", appConfiguration.DOMAIN).replaceAll("ORG", org)
                        .replaceAll("PEER_NAME", peer.split("-")[1]).replaceAll("CA_PRIVATE_KEY",
                            orgDomainPkMap.get(org + "." + appConfiguration.DOMAIN));

                    String composeFileName =
                        String.join("", "docker-compose-", org, "-", peer, ".yaml");
                    String fileName = String.join("", workingDir, composeFileName);
                    Files.write(Paths.get(fileName), content.getBytes(), StandardOpenOption.CREATE);
                    appendToFile(scriptFile, "echo copying file " + fileName);
                    copyFileToGcpVm(fileName, PROJECT_VM_DIR + "docker-compose.yaml", peer, config);
                    // appendToFile(scriptFile, "sleep 2");
                    copyFileToGcpVm(workingDir + "base.yaml", PROJECT_VM_DIR + "base.yaml", peer,
                        config);
                }


            }

            //create orderer docker compose file
            String orderer =
                ordererTemplate.replaceAll("ORDERER_PORT", appConfiguration.ORDERER_PORT)

                    .replaceAll("DOMAIN", appConfiguration.DOMAIN).replaceAll("CLI_EXTRA_HOSTS",
                    String.join(System.lineSeparator(), extraHostsMappingByOrg.values())
                        .concat(ordererHost));
            String fileName = String.join("", workingDir, "docker-compose-orderer.yaml");
            Files.write(Paths.get(fileName), orderer.getBytes(), StandardOpenOption.CREATE);
            appendToFile(scriptFile, "echo copying file " + fileName);
            copyFileToGcpVm(fileName, PROJECT_VM_DIR + "docker-compose.yaml",
                config.getOrdererName(), config);
            copyFileToGcpVm(workingDir + "base.yaml", PROJECT_VM_DIR + "base.yaml",
                config.getOrdererName(), config);
        } catch (Throwable e) {
            throw new RuntimeException("Cannot create docker compose yaml file ", e);
        }

    }


    public void setupDocker(Map<String, Map<String, String>> orgNameIpMap, NetworkConfig config) {
        try {
            String cmd = " --command \"sh init-docker.sh\"";

            for (String org : orgNameIpMap.keySet()) {
                for (String instance : orgNameIpMap.get(org).keySet()) {

                    appendToFile(scriptFile,
                        String.join("", SSH, instance, " --zone ", config.getGcpZoneName(), cmd));
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException("Cannot write to script file ", t);
        }
    }

    public void setupVm(Map<String, Map<String, String>> orgNameIpMap, NetworkConfig config) {
        try {
            String cmd = " --command \"sh setup.sh\"";

            for (String org : orgNameIpMap.keySet()) {
                for (String instance : orgNameIpMap.get(org).keySet()) {

                    appendToFile(scriptFile,
                        String.join("", SSH, instance, " --zone ", config.getGcpZoneName(), cmd));
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException("Cannot write to script file ", t);
        }
    }


    public void setupComposer(Map<String, Map<String, String>> orgNameIpMap, NetworkConfig config) {
        try {
            String cmd = " --command \"sh install-composer.sh\"";

            for (String org : orgNameIpMap.keySet()) {
                for (String instance : orgNameIpMap.get(org).keySet()) {
                    if (!config.getOrdererName().equals(instance)) {
                        appendToFile(scriptFile, String
                            .join("", SSH, instance, " --zone ", config.getGcpZoneName(), cmd));
                    }
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException("Cannot write to script file ", t);
        }
    }


    public void startOrderer(Map<String, Map<String, String>> orgNameIpMap, NetworkConfig config) {
        String cmd = " --command \" docker-compose -f ./" + PROJECT_VM_DIR
            + "docker-compose.yaml up -d 2>&1\"";
        appendToFile(scriptFile, String
            .join("", SSH, config.getOrdererName(), " --zone ", config.getGcpZoneName(), cmd));


    }

    public void startContainers(Map<String, Map<String, String>> orgNameIpMap,
        NetworkConfig config) {

        String cmd = " --command \" docker-compose -f ./" + PROJECT_VM_DIR
            + "docker-compose.yaml up -d 2>&1\"";

        for (String org : orgNameIpMap.keySet()) {
            for (String instance : orgNameIpMap.get(org).keySet()) {

                if (instance.equals(config.getOrdererName())) {
                    continue;
                }
                appendToFile(scriptFile,
                    String.join("", SSH, instance, " --zone ", config.getGcpZoneName(), cmd));
            }
        }


    }

    public void createChannelBlock(Map<String, Map<String, String>> orgNameIpMap,
        NetworkConfig config) {
        log.info("calling createChannelBlock method");

        String org = orgNameIpMap.keySet().stream().filter(o -> !o.equals(config.getOrdererName()))
            .findFirst().get();
        String instance = (String) orgNameIpMap.get(org).keySet().toArray()[0];
        //todo: make channel name a variable
        String domain = String.join(".", org, appConfiguration.DOMAIN);

        String peerCmd = String
            .join("", "peer channel create -o orderer.", appConfiguration.DOMAIN, ":",
                appConfiguration.ORDERER_PORT, " -c ", config.getChannelName(), " -f ",
                CONTAINER_WORKING_DIR, "channel/", config.getChannelName(), ".tx --tls --cafile ",
                ORDERER_CA_IN_CONTAINER);

        String dockerCmd = DOCKER_CMD.replace("CLI", "cli." + domain).replace("COMMAND", peerCmd);
        String cmd = String
            .join("", SSH, instance, " --zone ", config.getGcpZoneName(), " --command \"",
                dockerCmd, "\"");

        appendToFile(scriptFile, cmd);
        appendToFile(scriptFile, "echo " + config.getChannelName() + ".block created");
        appendToFile(scriptFile, "sleep 2");

        dockerCmd = String.join("", "docker cp ", "cli." + domain, ":", CONTAINER_WORKING_DIR,
            config.getChannelName(), ".block ~/");
        cmd = String.join("", SSH, instance, " --zone ", config.getGcpZoneName(), " --command \"",
            dockerCmd, "\"");
        appendToFile(scriptFile, cmd);

        appendToFile(scriptFile, "sleep 2");
        appendToFile(scriptFile, String.join("", SSH, instance, " --zone ", config.getGcpZoneName(),
            " --command \" chmod u+x ~/", config.getChannelName(), ".block\""));

        appendToFile(scriptFile,
            "echo " + config.getChannelName() + ".block copied from container");

        //todo: make channel name a variable
        copyVmFileToGCP(config.getChannelName() + ".block", workingDir, instance, config);
        appendToFile(scriptFile, "echo " + config.getChannelName() + ".block copied to server");

    }

    public void distributeChannelBlock(Map<String, Map<String, String>> orgNameIpMap,
        NetworkConfig config
        // , String instance
    ) {
        log.info("calling distributeChannelBlock method");

        orgNameIpMap.values().stream().map(Map::keySet).forEach(ins -> ins.stream().forEach(vm -> {
            appendToFile(scriptFile, "echo senting " + config.getChannelName() + ".block to " + vm);

            copyFileToGcpVm(workingDir + config.getChannelName() + ".block",
                PROJECT_VM_DIR + config.getChannelName() + ".block", vm, config);
        }));

    }

    public void copyChannelBlockToContainer(Map<String, Map<String, String>> orgNameIpMap,
        NetworkConfig config) {
        log.info("calling copyChannelBlockToContainer method");

        Set<String> orgs = Sets.newHashSet(orgNameIpMap.keySet());
        orgs.remove(config.getOrdererName());
        for (String org : orgs) {
            String domain = String.join(".", org, appConfiguration.DOMAIN);

            for (String peer : orgNameIpMap.get(org).keySet()) {
                String dockerCmd = String
                    .join("", "docker cp ~/", config.getChannelName(), ".block", " cli." + domain,
                        ":", CONTAINER_WORKING_DIR);
                String cmd = String
                    .join("", SSH, peer, " --zone ", config.getGcpZoneName(), " --command \"",
                        dockerCmd, "\"");

                appendToFile(scriptFile, cmd);
            }
        }

    }

    public void joinChannel(Map<String, Map<String, String>> orgNameIpMap, NetworkConfig config) {
        appendToFile(scriptFile, "echo Joining channel...");

        Set<String> orgs = Sets.newHashSet(orgNameIpMap.keySet());
        orgs.remove(config.getOrdererName());
        for (String org : orgs) {
            String domain = String.join(".", org, appConfiguration.DOMAIN);

            for (String peer : orgNameIpMap.get(org).keySet()) {
                String peerCmd = "peer channel join -b " + config.getChannelName() + ".block";
                String dockerCmd =
                    DOCKER_CMD.replace("CLI", "cli." + domain).replace("COMMAND", peerCmd);
                String cmd = String
                    .join("", SSH, peer, " --zone ", config.getGcpZoneName(), " --command \"",
                        dockerCmd, "\"");

                appendToFile(scriptFile, cmd);
            }
        }


    }

    public void updateAnchorPeer(Map<String, Map<String, String>> orgNameIpMap,
        NetworkConfig config) {
        log.info("Updating anchorPeer...");
        appendToFile(scriptFile, "echo Updating anchorPeer...");

        Set<String> orgs = Sets.newHashSet(orgNameIpMap.keySet());
        orgs.remove(config.getOrdererName());
        for (String org : orgs) {
            String domain = String.join(".", org, appConfiguration.DOMAIN);

            for (String peer : orgNameIpMap.get(org).keySet()) {
                if (!peer.contains("peer0")) {
                    continue;
                }
                String updateAnchorCmd =
                    ANCHORPEER_CMD.replace("ORDERER_HOST", "orderer." + appConfiguration.DOMAIN)
                        .replace("CHANNEL_NAME", config.getChannelName())
                        .replace("ORDERER_PORT", appConfiguration.ORDERER_PORT)
                        .replace("ANCHOR_FILE", "channel/" + org + MSP_SUFFIX + ANCHOR_FILE_SUFFIX)
                        .replace("ORDERER_CA", ORDERER_CA_IN_CONTAINER);
                String dockerCmd =
                    DOCKER_CMD.replace("CLI", "cli." + domain).replace("COMMAND", updateAnchorCmd);
                String cmd = String
                    .join("", SSH, peer, " --zone ", config.getGcpZoneName(), " --command \"",
                        dockerCmd, "\"");

                appendToFile(scriptFile, cmd);
            }
        }

    }

    public void installChaincode(Map<String, Map<String, String>> orgNameIpMap,
        NetworkConfig config) {

        Set<String> orgs = Sets.newHashSet(orgNameIpMap.keySet());
        orgs.remove(config.getOrdererName());
        for (String org : orgs) {
            String domain = String.join(".", org, appConfiguration.DOMAIN);

            for (String peer : orgNameIpMap.get(org).keySet()) {
                String peerCmd = "peer chaincode install -n test -v 1.0 -p chaincode_example02 ";
                String dockerCmd =
                    DOCKER_CMD.replace("CLI", "cli." + domain).replace("COMMAND", peerCmd);
                String cmd = String
                    .join("", SSH, peer, " --zone ", config.getGcpZoneName(), " --command \"",
                        dockerCmd, "\"");

                appendToFile(scriptFile, cmd);
            }
        }

    }


    public void createComposerConnectionFile(Map<String, Map<String, String>> orgNameIpMap,
        NetworkConfig config) {

        try {
            deleteFolders(composerPath);
            createDir(composerPath);
            String connectionTemplate = new String(Files.readAllBytes(
                Paths.get(Resources.getResource("template/connection-template.json").toURI())));

            String orderer = String.join("", "orderer.", appConfiguration.DOMAIN);
            String ordererIp =
                orgNameIpMap.get(config.getOrdererName()).get(config.getOrdererName());
            Set<String> orgs = Sets.newHashSet(orgNameIpMap.keySet());
            orgs.remove(config.getOrdererName());

            Map<String, List<String>> OrghostsListMap = new HashMap<>(orgs.size());
            Map<String, String> hostsIpMap = new HashMap<>();
            Map<String, String> hostOrgMap = new HashMap<>();
            for (String org : orgs) {

                Map<String, String> hostIpMapEntry = orgNameIpMap.get(org);
                hostsIpMap.putAll(hostIpMapEntry);

                hostIpMapEntry.entrySet().stream().forEach(e -> hostOrgMap.put(e.getKey(), org));

                OrghostsListMap.put(org, hostIpMapEntry.entrySet().stream().map(e -> String
                    .join("", e.getKey().split("-")[1], ".", org, ".", appConfiguration.DOMAIN))
                    .collect(Collectors.toList()));


            }

            String peerChannelConfigs = hostsIpMap.keySet().stream().map(host -> {

                try {
                    String peerChannelConfigJson =
                        mapper.writeValueAsString(new ChannelPeerConfig(true, true, true));

                    return String.join("", "\"", String
                        .join(".", host.split("-")[1], hostOrgMap.get(host),
                            appConfiguration.DOMAIN), "\":", peerChannelConfigJson);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

            }).collect(joining("," + System.lineSeparator()));

            String orgConfigs = OrghostsListMap.keySet().stream().map(org -> {

                try {

                    String orgConfigJson = mapper.writeValueAsString(
                        new OrgConfig(org + MSP_SUFFIX, OrghostsListMap.get(org), Lists
                            .newArrayList(String.join(".", "ca", org, appConfiguration.DOMAIN))));

                    return String.join("", "\"", org, "\":", orgConfigJson);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }


            }).collect(joining("," + System.lineSeparator()));

            OrdererConfig ordererConfig =
                new OrdererConfig("grpcs://" + ordererIp + ":" + appConfiguration.ORDERER_PORT,
                    new GrpcOptions(orderer), new TlsCACerts(readOrdererCaCert()));

            String ordererConfigJson =
                "\"" + orderer + "\":" + mapper.writeValueAsString(ordererConfig);

            String peersConfigJson = hostsIpMap.entrySet().stream().map(e -> {
                try {
                    String host = e.getKey();
                    // String pemFile = PEER_CA_FILE.replaceAll(ORG_PLACEHOLDER, hostOrgMap.get(host))
                    //     .replaceAll(DOMAIN_PLACEHOLDER, appConfiguration.DOMAIN);
                    // System.out.println("pemFile = " + pemFile);
                    PeerConfig peerConfig = new PeerConfig(
                        "grpcs://" + hostsIpMap.get(host) + ":" + appConfiguration.PEER_PORT,
                        "grpcs://" + hostsIpMap.get(host) + ":" + appConfiguration.PEER_EVENT_PORT,
                        new GrpcOptions(e.getKey()),
                        new TlsCACerts(readPeerCaCert(hostOrgMap.get(host))));
                    String configJson = mapper.writeValueAsString(peerConfig);

                    return String.join("", "\"", String
                        .join(".", host.split("-")[1], hostOrgMap.get(host),
                            appConfiguration.DOMAIN), "\":", configJson);


                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }

            }).collect(joining("," + System.lineSeparator()));

            //todo: need to modularize this CA code

            String caConfigJson = orgs.stream().map(o -> {

                //todo:  hard code for now
                String caRealHost = String.join("-", o, "peer0");
                String caHost = String.join(".", "ca", o, appConfiguration.DOMAIN);
                CaConfig caConfig = new CaConfig(
                    "https://" + hostsIpMap.get(caRealHost) + ":" + appConfiguration.CA_PORT,
                    caHost, new HttpOptions(false));
                try {
                    return String
                        .join("", "\"", caHost, "\"", ":", mapper.writeValueAsString(caConfig));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

            }).collect(joining("," + System.lineSeparator()));

            String networkName = String.join("-", orgs) + "-network";

            for (String org : orgs) {

                String content = connectionTemplate.replace("CONNECTION_NAME", networkName)
                    .replaceAll("ORG_NAME", org).replace("ORDERER_NAMES", "\"" + orderer + "\"")
                    .replace("PEERS_CHANNEL_INFO", peerChannelConfigs)
                    .replace("ORGS_CONFIG", orgConfigs).replace("ORDERER_CONFIG", ordererConfigJson)
                    .replace("PEERS_CONFIG", peersConfigJson).replace("CA_CONFIG", caConfigJson)
                    .replace("CHANNEL_NAME", config.getChannelName());

                String fileName = String.join("", composerPath,
                    CONNECTION_FILE_NAME_TEMPLATE.replace("NETWORKNAME", networkName)
                        .replace("ORG", org), ".json");
                log.info("fileName = " + fileName);
                Files.write(Paths.get(fileName), content.getBytes(), StandardOpenOption.CREATE);
                String host = String.join("-", org, "peer0");
                appendToFile(scriptFile, "echo copying connection.json file to " + host);
                copyFileToGcpVm(fileName, appConfiguration.COMPOSER_CONNECTION_FILE + ".json", host,
                    config);

            }

        } catch (Throwable e) {
            throw new RuntimeException("Cannot create composer connection json file ", e);
        }


    }

    public String readOrdererCaCert() throws IOException {

        String file =
            workingDir + ORDERER_CA_FILE.replaceAll(DOMAIN_PLACEHOLDER, appConfiguration.DOMAIN);
        return readCaCert(file);
    }

    public String readPeerCaCert(String org) throws IOException {

        String file =
            workingDir + PEER_CA_FILE.replaceAll(DOMAIN_PLACEHOLDER, appConfiguration.DOMAIN)
                .replaceAll(ORG_PLACEHOLDER, org);
        return readCaCert(file);


    }

    private String readCaCert(String file) throws IOException {

        return Files.readAllLines(Paths.get(file)).stream().collect(joining("\n")) + "\n";
    }


    public void createComposerAdminCard(Map<String, Map<String, String>> orgNameIpMap,
        NetworkConfig config) {
        try {
            log.info("Generating composer admin card files");
            // deleteFolders(COMPOSER_CARD_FOLDER);
            // createDir(COMPOSER_CARD_FOLDER);

            Set<String> orgs = Sets.newHashSet(orgNameIpMap.keySet());
            orgs.remove(config.getOrdererName());
            String networkName = String.join("-", orgs) + "-network";

            // String fileNameTemplate =
            //     CONNECTION_FILE_NAME_TEMPLATE.replace("NETWORKNAME", networkName);
            for (String org : orgs) {

                //String connectionFile = fileNameTemplate.replace("ORG", org);
                String connectionFile = appConfiguration.COMPOSER_CONNECTION_FILE;
                String adminPemFileFolder =
                    CRYPTO_FOLDER_FOR_COMPOSER.replaceAll(ORG_PLACEHOLDER, org)
                        .replaceAll(DOMAIN_PLACEHOLDER, appConfiguration.DOMAIN);

                // copyFileToGcpVm(cardFile, PROJECT_VM_DIR + "connection.yaml", host, config);
                String cardName = CONNECTION_FILE_NAME_TEMPLATE.replace("NETWORKNAME", networkName)
                    .replace("ORG", org);
                String createCardCommand =
                    CREATE_CARD_CMD.replace("CONNECTION_JSON", connectionFile + ".json")
                        .replace("ADMIN_PEM", adminPemFileFolder + "signcerts/A*.pem")
                        .replace("SK_FILE", adminPemFileFolder + "keystore/*_sk")
                        .replace("NAME", cardName);
                //.replace("FOLDER", COMPOSER_CARD_FOLDER);
                String deleteOldCardCmd = COMPOSER_DELETE_CARD_CMD.replace("NAME", cardName);

                String cardFile = "PeerAdmin@" + cardName;
                // CommandRunner.runCommand(Lists.newArrayList("/bin/bash", "-c", COMMPOSER_FW_DIR
                //         + "composer card create -p ~/blockchain/artifacts/composer/boa-google-network-connection-boa.json -u PeerAdmin -c ~/blockchain/artifacts/crypto-config/peerOrganizations/boa.sample.com/users/Admin@boa.sample.com/msp/signcerts/Admin@boa.sample.com-cert.pem -k ~/blockchain/artifacts/crypto-config/peerOrganizations/boa.sample.com/users/Admin@boa.sample.com/msp/keystore/*_sk -r PeerAdmin -r ChannelAdmin -f ~/blockchain/artifacts/composer/PeerAdmin@boa.card"),
                //     log);

                String host = String.join("-", org, "peer0");
                appendToFile(scriptFile, "echo Deleting file " + cardFile + " in " + host);
                appendToFile(scriptFile, String
                    .join("", SSH, host, " --zone ", config.getGcpZoneName(), " --command \" ",
                        deleteOldCardCmd, "\""));
                String gcpCmd = String
                    .join("", SSH, host, " --zone ", config.getGcpZoneName(), " --command \"",
                        createCardCommand, "\"");
                appendToFile(scriptFile,
                    "echo creating file " + cardFile + ".card" + " in " + host);
                appendToFile(scriptFile, gcpCmd);
                String importCardCmd = COMPOSER_IMPORT_CARD_CMD.replaceAll("NAME", cardFile);
                appendToFile(scriptFile, String
                    .join("", SSH, host, " --zone ", config.getGcpZoneName(), " --command \" ",
                        importCardCmd, "\""));
            }
            log.info("Composer admin cards were created");


        } catch (Throwable t) {
            throw new RuntimeException("Cannot Composer admin cards files ", t);
        }
    }


    public void runScript() {
        try {
            log.info("Running script file created");
            CommandRunner.runCommand(Lists.newArrayList("/bin/bash", "-c",
                String.join(" ", "chmod u+x", workingDir + "script.sh")), log);

            //CommandRunner.runCommand(Lists.newArrayList("/bin/sh", "-c", workingDir + "script.sh"), log);
            CommandRunner.runCommand(Lists.newArrayList(workingDir + "script.sh"), log);

        } catch (Throwable t) {
            throw new RuntimeException("Cannot run script file ", t);
        }

    }


    public void copyFileToGcpVm(String file, String destFileName, String peerName,
        NetworkConfig config) {

        try {

            appendToFile(scriptFile, SCP.replace("GCP_PROJECT", config.getGcpProjectName())
                .replace("ZONE", config.getGcpZoneName()).replace("PEERNAME", peerName)
                .replace("DEST_FILE", destFileName).replace("FILE", file));
        } catch (Throwable t) {

            throw new CommandFailedToRunException("Cannot copy " + file + " to " + peerName, t);
        }

    }

    public void copyVmFileToGCP(String file, String destFileName, String peerName,
        NetworkConfig config) {

        try {
            appendToFile(scriptFile,
                SCP_TO_SERVER.replace("GCP_PROJECT", config.getGcpProjectName())
                    .replace("ZONE", config.getGcpZoneName()).replace("PEERNAME", peerName)
                    .replace("DEST", destFileName).replace("FILE", file));
        } catch (Throwable t) {

            throw new CommandFailedToRunException("Cannot copy " + file + " to " + peerName, t);
        }

    }

    public void appendToFile(String fileName, String cmd) {

        try (BufferedWriter bw = Files
            .newBufferedWriter(Paths.get(fileName), StandardOpenOption.APPEND)) {
            bw.append(cmd);
            bw.newLine();
        } catch (Throwable t) {
            log.info(String.join(":", "Cannot add ", cmd, " to file", fileName));
            throw new RuntimeException(t);
        }
    }

    public void createDir(String dir) {
        try {
            if (!Files.exists(Paths.get(dir))) {
                Files.createDirectories(Paths.get(dir));
            }
        } catch (Throwable t) {
            log.info(String.join(":", "Cannot create folder", dir, t.toString()));
            throw new RuntimeException(t);
        }

    }

    public void deleteFolders(String dir) {

        try {
            if (Paths.get(dir).toFile().exists()) {
                Files.walk(Paths.get(dir)).sorted(Comparator.reverseOrder()).map(Path::toFile)
                    .forEach(File::delete);
            }
        } catch (IOException e) {
            System.err.println("Cannot delete folder " + dir);
            throw new RuntimeException(e);
        }

    }

}
