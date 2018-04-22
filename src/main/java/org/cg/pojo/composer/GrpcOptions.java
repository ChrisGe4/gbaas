
package org.cg.pojo.composer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "ssl-target-name-override"
})
public class GrpcOptions {

    @JsonProperty("ssl-target-name-override")
    private String sslTargetNameOverride;

    public GrpcOptions(String sslTargetNameOverride) {
        this.sslTargetNameOverride = sslTargetNameOverride;
    }

    @JsonProperty("ssl-target-name-override")
    public String getSslTargetNameOverride() {
        return sslTargetNameOverride;
    }

    @JsonProperty("ssl-target-name-override")
    public void setSslTargetNameOverride(String sslTargetNameOverride) {
        this.sslTargetNameOverride = sslTargetNameOverride;
    }

}
