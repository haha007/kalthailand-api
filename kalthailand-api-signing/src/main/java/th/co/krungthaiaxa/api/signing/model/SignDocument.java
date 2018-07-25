package th.co.krungthaiaxa.api.signing.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Optional;

public class SignDocument implements Serializable {

    @JsonProperty("content")
    private String content;
    @JsonProperty("signingLocation")
    private String signingLocation;
    @JsonProperty("signingReason")
    private String signingReason;
    @JsonProperty("certificate")
    private String certificate;
    @JsonProperty("password")
    private String password;

    public Optional<String> getContent() {
        return Optional.ofNullable(content);
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Optional<String> getSigningLocation() {
        return Optional.ofNullable(signingLocation);
    }

    public void setSigningLocation(String signingLocation) {
        this.signingLocation = signingLocation;
    }

    public Optional<String> getSigningReason() {
        return Optional.ofNullable(signingReason);
    }

    public void setSigningReason(String signingReason) {
        this.signingReason = signingReason;
    }

    public Optional<String> getCertificate() {
        return Optional.ofNullable(certificate);
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

}
