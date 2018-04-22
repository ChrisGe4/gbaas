package org.cg.pojo.composer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"url", "caName", "httpOptions"})
public class CaConfig {

    @JsonProperty("url")
    private String url;
    @JsonProperty("caName")
    private String caName;
    @JsonProperty("httpOptions")
    private HttpOptions httpOptions;

    public CaConfig(String url, String caName, HttpOptions httpOptions) {
        this.url = url;
        this.caName = caName;
        this.httpOptions = httpOptions;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("caName")
    public String getCaName() {
        return caName;
    }

    @JsonProperty("caName")
    public void setCaName(String caName) {
        this.caName = caName;
    }

    @JsonProperty("httpOptions")
    public HttpOptions getHttpOptions() {
        return httpOptions;
    }

    @JsonProperty("httpOptions")
    public void setHttpOptions(HttpOptions httpOptions) {
        this.httpOptions = httpOptions;
    }

}
