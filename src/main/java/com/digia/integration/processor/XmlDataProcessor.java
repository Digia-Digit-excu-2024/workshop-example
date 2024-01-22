package com.digia.integration.processor;

import com.digia.integration.model.Request;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Named("xmlDataProcessor")
public class XmlDataProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(XmlDataProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        Request request = exchange.getIn().getBody(Request.class);
        log.info(request.toString());

    }
}
