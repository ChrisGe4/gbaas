package org.cg.auth;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.common.io.Resources;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GCPAuthClient {


  @Bean
  public GoogleCredentials getGoogleCredential() throws IOException, URISyntaxException {
    GoogleCredentials credentials;
    File credentialsPath = new File(Resources.getResource("secrets/hyperledger-poc.json").toURI());

    try (FileInputStream serviceAccountStream = new FileInputStream(credentialsPath)) {
      //ServiceAccountCredentials.fromStream(new FileInputStream("/path/to/my/key.json")))
      credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);
      System.out.println("credentials = " + credentials);
    }
    return credentials;
  }
}
