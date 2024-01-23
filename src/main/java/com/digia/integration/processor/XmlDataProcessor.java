package com.digia.integration.processor;

import com.digia.integration.model.Feedback;
import com.digia.integration.model.Request;
import com.digia.integration.model.ServiceRequests;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

@ApplicationScoped
@Named("xmlDataProcessor")
public class XmlDataProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(XmlDataProcessor.class);
    // me ollaan kiinnostuneita palautteen sisällöstä ja vastauksesta
    // relevantit kentät lähdexml:ssä siis description ja status_notes
    @Override
    public void process(Exchange exchange) throws Exception {
        ServiceRequests requests = exchange.getIn().getBody(ServiceRequests.class);
        ArrayList<Feedback> feedbacks = new ArrayList<>();

        for(Request request : requests.getRequests()) {
            String cityResponse;

            if (request.getNotes().isEmpty()) {
                cityResponse = "Ei vastausta.";
            } else {
                cityResponse = request.getNotes();
            }

            Feedback feedback = Feedback.builder()
                    .content(request.getDescription())
                    .response(cityResponse)
                    .build();

            feedbacks.add(feedback);
        }
        exchange.getIn().setBody(feedbacks);
    }
}
