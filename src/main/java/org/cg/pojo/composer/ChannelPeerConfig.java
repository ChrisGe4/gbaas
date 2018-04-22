
package org.cg.pojo.composer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "endorsingPeer",
    "chaincodeQuery",
    "eventSource"
})
public class ChannelPeerConfig {

    @JsonProperty("endorsingPeer")
    private Boolean endorsingPeer;
    @JsonProperty("chaincodeQuery")
    private Boolean chaincodeQuery;
    @JsonProperty("eventSource")
    private Boolean eventSource;


  public ChannelPeerConfig(Boolean endorsingPeer, Boolean chaincodeQuery,
      Boolean eventSource) {
    this.endorsingPeer = endorsingPeer;
    this.chaincodeQuery = chaincodeQuery;
    this.eventSource = eventSource;
  }

  @JsonProperty("endorsingPeer")
    public Boolean getEndorsingPeer() {
        return endorsingPeer;
    }

    @JsonProperty("endorsingPeer")
    public void setEndorsingPeer(Boolean endorsingPeer) {
        this.endorsingPeer = endorsingPeer;
    }

  public ChannelPeerConfig withEndorsingPeer(Boolean endorsingPeer) {
    this.endorsingPeer = endorsingPeer;
    return this;
  }

  @JsonProperty("chaincodeQuery")
  public Boolean getChaincodeQuery() {
    return chaincodeQuery;
  }

  @JsonProperty("chaincodeQuery")
  public void setChaincodeQuery(Boolean chaincodeQuery) {
    this.chaincodeQuery = chaincodeQuery;
  }

  public ChannelPeerConfig withChaincodeQuery(Boolean chaincodeQuery) {
    this.chaincodeQuery = chaincodeQuery;
    return this;
  }

  @JsonProperty("eventSource")
  public Boolean getEventSource() {
    return eventSource;
  }

  @JsonProperty("eventSource")
  public void setEventSource(Boolean eventSource) {
    this.eventSource = eventSource;
  }

  public ChannelPeerConfig withEventSource(Boolean eventSource) {
    this.eventSource = eventSource;
    return this;
  }

}
