package org.cg.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Chris.Ge
 */
@Component
public class AppConfiguration {

  // Configurable program variables
  @Value("${orderer.port:7050}")
  public String ORDERER_PORT = "7050";
  @Value("${ca.port:7054}")
  public String CA_PORT = "7054";
  @Value("${working.dir:./blockchain/artifacts/}")
  public String WORKING_DIR = "/usr/local/google/home/chrisge/blockchain/artifacts/";
  @Value("${peer.port:7051}")
  public String PEER_PORT = "7051";
  @Value("${peer.event.port:7053}")
  public String PEER_EVENT_PORT = "7053";
  @Value("${composer.connnection.file:composer_connection}")
  public String COMPOSER_CONNECTION_FILE = "composer_connection";
  @Value("${gcloud.dir:/usr/bin/}")
  public String GCLOUD_DIR = "/usr/bin/";

}