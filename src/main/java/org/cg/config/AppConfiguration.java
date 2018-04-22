package org.cg.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Chris.Ge
 */
@Component
public class AppConfiguration {

  // Configurable program variables
  @Value("${domain:sample.com}")
  public String DOMAIN = "sample.com";
  @Value("${orderer.port:7050}")
  public String ORDERER_PORT = "7050";
  @Value("${ca.port:7054}")
  public String CA_PORT = "7054";
  @Value("${working.dir:~/blockchain/artifacts/}")
  public String WORKING_DIR = "~/blockchain/artifacts/";
  @Value("${peer.port:7051}")
  public String PEER_PORT = "7051";
  @Value("${peer.event.port:7053}")
  public String PEER_EVENT_PORT = "7053";
  @Value("${composer.connnection.file:composer_connection}")
  public String COMPOSER_CONNECTION_FILE = "composer_connection";
  @Value("${gcloud.dir:/usr/local/google/home/chrisge/exit/google-cloud-sdk/bin/}")
  public String GCLOUD_DIR = "/usr/local/google/home/chrisge/exit/google-cloud-sdk/bin/";

}