
package org.cg.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "networkName",
    "gcpProjectName",
    "gcpZoneName",
    "ordererName",
    "channelName",
    "properties"
})
public class NetworkConfig {

  @JsonProperty("networkName")
  private String networkName;
  @JsonProperty("gcpProjectName")
  private String gcpProjectName;
  @JsonProperty("gcpZoneName")
  private String gcpZoneName;
  @JsonProperty("ordererName")
  private String ordererName;
  @JsonProperty("channelName")
  private String channelName;
  @JsonProperty("properties")
  private List<Property> properties = null;

  @JsonProperty("networkName")
  public String getNetworkName() {
    return networkName;
  }

  @JsonProperty("networkName")
  public void setNetworkName(String networkName) {
    this.networkName = networkName;
  }

  @JsonProperty("gcpProjectName")
  public String getGcpProjectName() {
    return gcpProjectName;
  }

  @JsonProperty("gcpProjectName")
  public void setGcpProjectName(String gcpProjectName) {
    this.gcpProjectName = gcpProjectName;
  }

  @JsonProperty("gcpZoneName")
  public String getGcpZoneName() {
    return gcpZoneName;
  }

  @JsonProperty("gcpZoneName")
  public void setGcpZoneName(String gcpZoneName) {
    this.gcpZoneName = gcpZoneName;
  }

  @JsonProperty("ordererName")
  public String getOrdererName() {
    return ordererName;
  }

  @JsonProperty("ordererName")
  public void setOrdererName(String ordererName) {
    this.ordererName = ordererName;
  }

  @JsonProperty("channelName")
  public String getChannelName() {
    return channelName;
  }

  @JsonProperty("channelName")
  public void setChannelName(String channelName) {
    this.channelName = channelName;
  }

  @JsonProperty("properties")
  public List<Property> getProperties() {
    return properties;
  }

  @JsonProperty("properties")
  public void setProperties(List<Property> properties) {
    this.properties = properties;
  }

}
