
package org.cg.pojo.composer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "pem"
})
public class TlsCACerts {

  @JsonProperty("pem")
  private String pem;

  public TlsCACerts(String pem) {
    this.pem = pem;
  }

  @JsonProperty("pem")
  public String getPem() {
    return pem;
  }

  @JsonProperty("pem")
  public void setPem(String pem) {
    this.pem = pem;
  }

}
