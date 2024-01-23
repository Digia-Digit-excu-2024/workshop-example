package com.digia.integration.route;

import com.digia.integration.model.Feedback;
import com.digia.integration.model.Request;
import com.digia.integration.model.ServiceRequests;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.AggregationStrategies;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.component.jackson.ListJacksonDataFormat;
import org.apache.camel.component.jacksonxml.JacksonXMLDataFormat;
import org.apache.camel.component.jacksonxml.ListJacksonXMLDataFormat;
import org.apache.camel.processor.aggregate.GroupedBodyAggregationStrategy;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

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
        rest(apiBase)
                .get("/demo")
                .to("direct:extraction-route")
        ;

        // extract - Lataa tietoa jostain
        from("direct:extraction-route").routeId("http-demo-route")
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
                .marshal(jsonDataFormat)
                .log(LoggingLevel.INFO, "${body}")
                .stop()
        ;


        // load - Lataa tieto Blob Storageen (to be set up), optionaalisesti myös debuggina logitus
    }

    public void defineExceptions(){

        onException()
                .log("There was an exception on the route:  " + exceptionMessage())
                .stop()
        ;
    }
}
