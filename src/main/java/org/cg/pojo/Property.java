
package org.cg.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "org",
    "numOfPeers"
})
public class Property {

    @JsonProperty("org")
    private String org;
    @JsonProperty("numOfPeers")
    private Integer numOfPeers;

    @JsonProperty("org")
    public String getOrg() {
        return org;
    }

    @JsonProperty("org")
    public void setOrg(String org) {
        this.org = org;
    }

    @JsonProperty("numOfPeers")
    public Integer getNumOfPeers() {
        return numOfPeers;
    }

    @JsonProperty("numOfPeers")
    public void setNumOfPeers(Integer numOfPeers) {
        this.numOfPeers = numOfPeers;
    }

}
