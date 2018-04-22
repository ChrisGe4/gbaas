package org.cg.pojo.composer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"verify"})
public class HttpOptions {

    @JsonProperty("verify")
    private Boolean verify;

    public HttpOptions(Boolean verify) {
        this.verify = verify;
    }

    @JsonProperty("verify")
    public Boolean getVerify() {
        return verify;
    }

    @JsonProperty("verify")
    public void setVerify(Boolean verify) {
        this.verify = verify;
    }

}
