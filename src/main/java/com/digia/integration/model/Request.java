package com.digia.integration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@ApplicationScoped
@RegisterForReflection
public class Request {

    @JacksonXmlProperty(localName = "description")
    String description;
    @JacksonXmlProperty(localName = "requested_datetime")
    String dateTimeRequested;
    @JacksonXmlProperty(localName = "service_code")
    String serviceCode;
    @JacksonXmlProperty(localName = "service_name")
    String serviceName;
    @JacksonXmlProperty(localName = "service_request_id")
    String requestId;
    String status;
    @JacksonXmlProperty(localName = "status_notes")
    String notes;
    @JacksonXmlProperty(localName = "updated_datetime")
    String dateTimeUpdated;

    @Override
    public String toString(){
        return description;
    }
}
