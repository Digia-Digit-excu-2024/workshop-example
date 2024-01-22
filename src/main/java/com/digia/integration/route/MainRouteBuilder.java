package com.digia.integration.route;

import com.digia.integration.model.Request;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jacksonxml.JacksonXMLDataFormat;
import org.eclipse.microprofile.config.inject.ConfigProperty;

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

        defineExceptions();
        JacksonXMLDataFormat xmlDataFormat = new JacksonXMLDataFormat(Request.class);
        String weekAgo = ZonedDateTime.now(ZoneOffset.UTC).minus(7, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        rest(apiBase)
                .get("/demo/{limit}")
                .to("direct:start-routing")
                .get("/demo")
                .to("direct:start-routing")
        ;

        // extract - Lataa tietoa jostain
        from("direct:start-routing").routeId("http-demo-route")
                .log(LoggingLevel.INFO, "Käynnistetään reitti")
                .log(LoggingLevel.INFO, "Haetaan palautteet")
                .removeHeaders("*", "limit")
                .setHeader(Exchange.HTTP_QUERY, constant("start_date=" + weekAgo + "Z"))
                .to(turkuApi)
                .split().tokenizeXML("request").streaming()
                .unmarshal(xmlDataFormat)
                .process("xmlDataProcessor")
                .stop()
        ;

        // transform - Tee käsittelyt sekä formaatinmuutokset



        // load - Lataa tieto johonkin suuntaan, tässä tapauksessa palauta naamalle

    }

    public void defineExceptions(){
        onException()
                .log("There was an exception on the route:  " + exceptionMessage())
                .stop()
        ;
    }
}
