package com.digia.integration.route;

import com.digia.integration.model.Feedback;
import com.digia.integration.model.ServiceRequests;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.ListJacksonDataFormat;
import org.apache.camel.component.jacksonxml.JacksonXMLDataFormat;
import org.apache.camel.model.rest.RestBindingMode;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@ApplicationScoped
@RegisterForReflection
public class MainRouteBuilder extends RouteBuilder {

    @ConfigProperty(name="api.base")
    String apiBase;
    @ConfigProperty(name="turku.api")
    String turkuApi;

    @Override
    public void configure() throws Exception {

        JacksonXMLDataFormat xmlDataFormat = new JacksonXMLDataFormat(ServiceRequests.class);
        ListJacksonDataFormat jsonDataFormat = new ListJacksonDataFormat(Feedback.class);
        String weekAgo = ZonedDateTime.now(ZoneOffset.UTC).minus(7, ChronoUnit.DAYS)
                .truncatedTo(ChronoUnit.DAYS).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";

        defineExceptions();

        restConfiguration()
                .bindingMode(RestBindingMode.off)
                .dataFormatProperty("prettyPrint", "true")
        ;

        rest(apiBase)
                .get("/demo")
                .produces("text/plain")
                .responseMessage()
                    .code(200)
                    .message("ok")
                .endResponseMessage()
                .to("seda:extraction-route")

        ;

        // extract - Lataa tietoa jostain
        from("seda:extraction-route").routeId("workshop extraction route")
                .log(LoggingLevel.INFO, "Käynnistetään reitti")
                .log(LoggingLevel.INFO, "Haetaan palautteet")
                .removeHeaders("*", "limit")
                .setHeader(Exchange.HTTP_QUERY, constant("start_date=" + weekAgo))
                .to(turkuApi)
                .to("direct:processing-route")
        ;

        // transform - Tee käsittelyt sekä formaatinmuutokset
        from("direct:processing-route").routeId("workshop processing route")
                .log(LoggingLevel.INFO, "Parsitaan XML")
                .streamCaching()
                .unmarshal(xmlDataFormat)
                .process("xmlDataProcessor")
                .log(LoggingLevel.INFO, "${body[0].toString}")
                .marshal(jsonDataFormat)
                .to("direct:load-route")
        ;

        // load - Lataa tieto ny johonki

        from("direct:load-route").routeId("workshop load route")
                .log(LoggingLevel.INFO, "XML käsitelty, ladataan tiedosto kohdejärjestelmään")
                .setHeader(Exchange.FILE_NAME, constant(Instant.now().getEpochSecond() + ".json"))
                .to("file://output?charset=utf-8")
                .setBody(simple("${header.FILE_NAME}"))
                .removeHeaders("*")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .stop()
        ;
    }

    public void defineExceptions(){

        onException()
                .log("There was an exception on the route:  " + exceptionMessage())
                .stop()
        ;
    }
}
