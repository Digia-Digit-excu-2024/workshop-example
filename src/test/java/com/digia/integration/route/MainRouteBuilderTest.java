package com.digia.integration.route;

import com.digia.integration.model.ServiceRequests;
import com.digia.integration.processor.XmlDataProcessor;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.apache.camel.builder.Builder.constant;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MainRouteBuilderTest {

    @Inject
    CamelContext camelContext;
    @Inject
    ProducerTemplate producerTemplate;

    @Inject
    XmlDataProcessor xmlDataProcessor;

    @ConfigProperty(name="turku.api")
    String turkuApi;

    private static final String TEST_FILE_PATH = "src/test/resources/test.xml";
    private ServiceRequests testData;
    private final XmlMapper xmlMapper = new XmlMapper();
    private static final Logger log = LoggerFactory.getLogger(XmlDataProcessor.class);
    private static final String TEST_STRING = "test";

    @BeforeAll
    void setUp() throws IOException {
        log.info("Aloitetaan reitin yksikkötestien alustaminen");
        File testFile = new File(TEST_FILE_PATH);
        Assertions.assertNotNull(testFile);
        testData = xmlMapper.readValue(testFile, ServiceRequests.class);
        Assertions.assertNotNull(testData);

    }

    @Test
    void testStartUpRoute() throws Exception {

        log.info("Aloitetaan tiedon noutamisen reitin yksikkötestit");

        MockEndpoint mockEnd = camelContext.getEndpoint("mock:mockExtractionEnd", MockEndpoint.class);

        AdviceWith.adviceWith(camelContext, "workshop extraction route", route ->{
           route.replaceFromWith("direct:start");
           route.weaveByToUri(turkuApi).replace().transform(constant(xmlMapper.writeValueAsString(testData)));
           route.weaveByToUri("direct:processing-route").replace().to(mockEnd.getEndpointUri());
        });

        mockEnd.expectedBodyReceived().body(String.class);
        mockEnd.expectedMessageCount(1);

        producerTemplate.sendBody("direct:start", "");

        mockEnd.assertIsSatisfied();

    }

    @Test
    void testProcessingRoute() throws Exception {

        log.info("Aloitetaan tiedon muokkauksen yksikkötestit");

        MockEndpoint mockEnd = camelContext.getEndpoint("mock:mockProcessingEnd", MockEndpoint.class);

        AdviceWith.adviceWith(camelContext, "workshop processing route", route ->
                route.weaveByToUri("direct:load-route").replace().to(mockEnd.getEndpointUri())
        );

        mockEnd.expectedMessageCount(1);

        producerTemplate.sendBody("direct:processing-route", xmlMapper.writeValueAsString(testData));

        mockEnd.assertIsSatisfied();
    }

    @Test
    void testLoadRoute() throws Exception {

        log.info("Aloitetaan tiedon latauksen yksikkötestit");

        MockEndpoint mockEnd = camelContext.getEndpoint("mock:mockLoadingEnd", MockEndpoint.class);

        AdviceWith.adviceWith(camelContext, "workshop load route", route ->
           route.weaveByToUri("file://*").replace().to(mockEnd.getEndpointUri())
        );

        mockEnd.expectedBodyReceived().body(String.class);
        mockEnd.expectedMessageCount(1);

        producerTemplate.sendBody("direct:load-route", TEST_STRING);

        mockEnd.assertIsSatisfied();
        Assertions.assertNotNull(mockEnd.getReceivedExchanges().get(0).getIn().getHeader(Exchange.FILE_NAME));
        Assertions.assertEquals(TEST_STRING, mockEnd.getReceivedExchanges().get(0).getIn().getBody());
    }

}
