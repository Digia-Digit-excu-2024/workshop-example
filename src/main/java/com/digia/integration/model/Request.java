package com.digia.integration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Getter @Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder @Jacksonized
@ApplicationScoped
@RegisterForReflection
public class Request {

    @JsonProperty("description")
    String description;
    @JsonProperty("requested_datetime")
    String dateTimeRequested;
    @JsonProperty("service_code")
    String serviceCode;
    @JsonProperty("service_name")
    String serviceName;
    @JsonProperty("service_request_id")
    String requestId;
    @JsonProperty
    String status;
    @JsonProperty("status_notes")
    String notes;
    @JsonProperty("updated_datetime")
    String dateTimeUpdated;

    @Override
    public String toString(){
        return description;
    }
}
