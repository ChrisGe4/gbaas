package org.cg.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.cg.config.AppConfiguration;
import org.cg.pojo.NetworkConfig;
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
  public String deployFabricNetwork(@RequestParam("install_everything") String ifInstall,
      @RequestBody NetworkConfig config) {

    return properties.DOMAIN;

  }

  @GetMapping("/hello-world")
  public String sayHello(
  ) {
    return properties.DOMAIN + " - " + properties.CA_PORT;
  }

}
