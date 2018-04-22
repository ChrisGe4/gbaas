
package org.cg.pojo.composer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "mspid",
    "peers",
    "certificateAuthorities"
})
public class OrgConfig {

  @JsonProperty("mspid")
  private String mspid;
  @JsonProperty("peers")
  private List<String> peers;
  @JsonProperty("certificateAuthorities")
  private List<String> certificateAuthorities;

  public OrgConfig(String mspid, List<String> peers,
      List<String> certificateAuthorities) {
    this.mspid = mspid;
    this.peers = peers;
    this.certificateAuthorities = certificateAuthorities;
  }

  @JsonProperty("mspid")
  public String getMspid() {
    return mspid;
  }

  @JsonProperty("mspid")
  public void setMspid(String mspid) {
    this.mspid = mspid;
  }

  @JsonProperty("peers")
  public List<String> getPeers() {
    return peers;
  }

  @JsonProperty("peers")
  public void setPeers(List<String> peers) {
    this.peers = peers;
  }

  @JsonProperty("certificateAuthorities")
  public List<String> getCertificateAuthorities() {
    return certificateAuthorities;
  }

  @JsonProperty("certificateAuthorities")
  public void setCertificateAuthorities(List<String> certificateAuthorities) {
    this.certificateAuthorities = certificateAuthorities;
  }

}
