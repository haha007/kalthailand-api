package th.co.krungthaiaxa.api.elife.tmc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;
import th.co.krungthaiaxa.api.common.utils.JsonUtil;
import th.co.krungthaiaxa.api.elife.exception.ElifeException;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.enums.DocumentType;
import th.co.krungthaiaxa.api.elife.products.ProductType;
import th.co.krungthaiaxa.api.elife.products.utils.ProductUtils;
import th.co.krungthaiaxa.api.elife.tmc.wsdl.ReceivePDFJSON;
import th.co.krungthaiaxa.api.elife.tmc.wsdl.ReceivePDFJSONResponse;

import java.io.IOException;
import java.nio.charset.Charset;

import static th.co.krungthaiaxa.api.common.utils.JsonUtil.getJson;
import static th.co.krungthaiaxa.api.elife.exception.ExceptionUtils.notNull;

@Component
public class TMCClient extends WebServiceGatewaySupport {
    private final static Logger logger = LoggerFactory.getLogger(TMCClient.class);

    @Value("${tmc.webservice.url}")
    private String tmcWebServiceUrl;

    public void sendPDFToTMC(Policy policy, String pdfBase64Encoded, DocumentType documentType) {
        notNull(pdfBase64Encoded, new ElifeException("Cannot send null document"));
        notNull(documentType, new ElifeException("Cannot send document with no document type"));
        notNull(policy, new ElifeException("Cannot send document on a null policy"));
        notNull(policy.getInsureds().get(0), new ElifeException("Cannot send document with no insured"));
        notNull(policy.getInsureds().get(0).getPerson(), new ElifeException("Cannot send document when insured has no details"));
        notNull(policy.getInsureds().get(0).getPerson().getRegistrations(), new ElifeException("Cannot send document when insured has no ID"));

        Jaxb2Marshaller jaxb2Marshaller = marshaller();
        setMessageSender(webServiceMessageSender());
        setDefaultUri(tmcWebServiceUrl);
        setMarshaller(jaxb2Marshaller);
        setUnmarshaller(jaxb2Marshaller);

        TMCSendingPDFRequest tmcSendingPDFRequest = new TMCSendingPDFRequest();
        tmcSendingPDFRequest.setContent(pdfBase64Encoded);
        //TODO Product name here is the product display name. Should never use it. Should use productId.
        String productLogicName = policy.getCommonData().getProductId();
        ProductType productType = ProductUtils.validateExistProductTypeByLogicName(productLogicName);
        tmcSendingPDFRequest.setProductType(productType.getLogicDisplayName());//Cannot change it because TMC system already register iFine and iProtect with logicDisplayName()
        tmcSendingPDFRequest.setCustomerName(
                policy.getInsureds().get(0).getPerson().getTitle() + " " +
                        policy.getInsureds().get(0).getPerson().getGivenName() + " " +
                        policy.getInsureds().get(0).getPerson().getSurName());
        tmcSendingPDFRequest.setCustomerTel(policy.getInsureds().get(0).getPerson().getMobilePhoneNumber().getNumber());
        tmcSendingPDFRequest.setDocumentType(documentType.name());
        tmcSendingPDFRequest.setIdCard(policy.getInsureds().get(0).getPerson().getRegistrations().get(0).getId());
        tmcSendingPDFRequest.setMimeType("application/pdf");
        tmcSendingPDFRequest.setPolicyNumber(policy.getPolicyId());

        ReceivePDFJSON request = new ReceivePDFJSON();
        request.setStringJSON(new String(getJson(tmcSendingPDFRequest), Charset.forName("UTF-8")));

        logger.info("Sending document [" + documentType.name() + "] for policy [" + policy.getPolicyId() + "].");
        ReceivePDFJSONResponse response = (ReceivePDFJSONResponse) getWebServiceTemplate().marshalSendAndReceive(tmcWebServiceUrl, request, new SoapActionCallback("http://tempuri.org/ReceivePDFJSON"));
        TMCSendingPDFResponse tmcSendingPDFResponse;
        try {
            tmcSendingPDFResponse = JsonUtil.mapper.readValue(response.getReceivePDFJSONResult(), TMCSendingPDFResponse.class);
        } catch (IOException e) {
            throw new ElifeException("There was an error while sending document to TMC. Error message is [" + e.getMessage() + "].", e);
        }

        if (tmcSendingPDFResponse.getRemark().getMessage().equalsIgnoreCase("Success")) {
            logger.info("Document sent successfully.");
        } else {
            throw new ElifeException("TMC returned an error message when sending document. Message is [" + tmcSendingPDFResponse.getRemark().getMessage() + "].");
        }
    }

    private Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("th.co.krungthaiaxa.api.elife.tmc.wsdl");
        return marshaller;
    }

    private WebServiceMessageSender webServiceMessageSender() {
        HttpComponentsMessageSender sender = new HttpComponentsMessageSender();
        sender.setReadTimeout(120 * 1000);
        sender.setConnectionTimeout(120 * 1000);
        return sender;
    }

}
