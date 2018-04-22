
package org.cg.pojo.composer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "url",
        "eventUrl",
        "grpcOptions",
        "tlsCACerts"
})
public class PeerConfig {

    @JsonProperty("url")
    private String url;
    @JsonProperty("eventUrl")
    private String eventUrl;
    @JsonProperty("grpcOptions")
    private GrpcOptions grpcOptions;
    @JsonProperty("tlsCACerts")
    private TlsCACerts tlsCACerts;

    public PeerConfig(String url, String eventUrl, GrpcOptions grpcOptions, TlsCACerts tlsCACerts) {
        this.url = url;
        this.eventUrl = eventUrl;
        this.grpcOptions = grpcOptions;
        this.tlsCACerts = tlsCACerts;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("eventUrl")
    public String getEventUrl() {
        return eventUrl;
    }

    @JsonProperty("eventUrl")
    public void setEventUrl(String eventUrl) {
        this.eventUrl = eventUrl;
    }

    @JsonProperty("grpcOptions")
    public GrpcOptions getGrpcOptions() {
        return grpcOptions;
    }

    @JsonProperty("grpcOptions")
    public void setGrpcOptions(GrpcOptions grpcOptions) {
        this.grpcOptions = grpcOptions;
    }

    @JsonProperty("tlsCACerts")
    public TlsCACerts getTlsCACerts() {
        return tlsCACerts;
    }

    @JsonProperty("tlsCACerts")
    public void setTlsCACerts(TlsCACerts tlsCACerts) {
        this.tlsCACerts = tlsCACerts;
    }

}
