package com.digia.integration.processor;

import com.digia.integration.model.Feedback;
import com.digia.integration.model.ServiceRequests;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class XmlDataProcessorTest {

    @Inject
    XmlDataProcessor xmlDataProcessor;

    private static final String testFilePath = "src/test/resources/test.xml";
    private final XmlMapper xmlMapper = new XmlMapper();

    private ServiceRequests testData;
    private static final Logger log = LoggerFactory.getLogger(XmlDataProcessorTest.class);

    @BeforeAll
    void setUp() throws IOException {
        log.info("Aloitetaan prosessorin yksikkötestien alustaminen");

        Path filePath = Paths.get(testFilePath);

        List<String>fileContent = Files.readAllLines(filePath);
        Assertions.assertNotNull(fileContent);
        fileContent.forEach(Assertions::assertNotNull);

        testData = xmlMapper.readValue(new File(testFilePath), ServiceRequests.class);
        Assertions.assertNotNull(testData);
    }

    @Test
    void tryDataFormat() throws Exception {

        log.info("Aloitetaan prosessorin yksikkötesti");

        Assertions.assertNotNull(testData.getRequests().get(0));

        log.info(testData.getRequests().get(0).getDescription());

        CamelContext testContext = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(testContext);

        exchange.getIn().setBody(testData);

        xmlDataProcessor.process(exchange);

        Assertions.assertInstanceOf(List.class, exchange.getIn().getBody());
        Object testObject = exchange.getIn().getBody(List.class).get(0);
        Assertions.assertInstanceOf(Feedback.class, testObject);
        Feedback feedback = (Feedback) testObject;
        Assertions.assertEquals(testData.getRequests().get(0).getDescription(), feedback.getContent());

    }

}
