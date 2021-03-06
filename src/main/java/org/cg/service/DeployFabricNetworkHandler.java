package org.cg.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.util.Map;
import org.cg.config.AppConfiguration;
import org.cg.pojo.NetworkConfig;
import org.cg.pojo.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Chris.Ge
 */

@RestController
@RequestMapping("/gbaas")
public class DeployFabricNetworkHandler {

  private static final Logger log = LoggerFactory.getLogger(DeployFabricNetworkHandler.class);
  private final AppConfiguration properties;
  private final DeploymentService service;
  private final ObjectMapper objectMapper;

  @Autowired
  public DeployFabricNetworkHandler(AppConfiguration properties, DeploymentService service,
      ObjectMapper objectMapper) {
    this.properties = properties;
    this.service = service;
    this.objectMapper = objectMapper;
  }

  @PostMapping(value = "/fabric", headers = "Accept=application/json")
  public String deployFabricNetwork(
      @RequestParam(value = "create_instance", defaultValue = "false", required = false)
          Boolean createInstance,
      @RequestParam(value = "install_software", defaultValue = "false", required = false)
          Boolean installSoftware,
      @RequestParam(value = "install_example_chaincode", defaultValue = "false", required = false)
          Boolean installExampleChaincode,
      @RequestBody NetworkConfig config) {

    service.deployFabric(config, createInstance, installSoftware, installExampleChaincode);

    service.runScript();
    return "succeed";
  }


  @PostMapping(value = "/composer", headers = "Accept=application/json")
  public String deployComposer(@RequestBody NetworkConfig config,@RequestParam(value = "bna_url", required = false)
      String bnaUrl) {
    service.deployComposer(config,bnaUrl, service.getInstanceNameIPMap(config));
    service.runScript();
    return "succeed";
  }

  @GetMapping("/sample")
  public String sampleConfig() throws JsonProcessingException {

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
    config.setDomain("sample-network");
    config.setProperties(Lists.newArrayList(property1, property2));

    return objectMapper.writeValueAsString(config);
  }

  @GetMapping("/list_instances")
  public String listInstances(@RequestBody NetworkConfig config) {

    Map<String, Map<String, String>> orgNameIpMap = service.getInstanceNameIPMap(config);

    return orgNameIpMap.toString();


  }


}
