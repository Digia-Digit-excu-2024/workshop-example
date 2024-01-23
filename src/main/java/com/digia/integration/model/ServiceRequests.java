package com.digia.integration.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Getter @Setter
@RegisterForReflection
@ApplicationScoped
@Builder @Jacksonized
@NoArgsConstructor @AllArgsConstructor
@JacksonXmlRootElement(localName = "service_requests")
public class ServiceRequests {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "request")
    List<Request> requests;
}
