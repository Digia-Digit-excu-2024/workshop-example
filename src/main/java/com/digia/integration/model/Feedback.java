package com.digia.integration.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Setter @Getter
@Builder @Jacksonized
@RegisterForReflection
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {

    String content;

    String response;
}
