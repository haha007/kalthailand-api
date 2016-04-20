package th.co.krungthaiaxa.elife.api.tmc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import th.co.krungthaiaxa.elife.api.exception.ElifeException;
import th.co.krungthaiaxa.elife.api.model.Policy;
import th.co.krungthaiaxa.elife.api.model.enums.DocumentType;
import th.co.krungthaiaxa.elife.api.tmc.wsdl.ReceiveDataJSON;
import th.co.krungthaiaxa.elife.api.tmc.wsdl.ReceiveDataJSONResponse;
import th.co.krungthaiaxa.elife.api.utils.JsonUtil;

import java.io.IOException;
import java.nio.charset.Charset;

import static th.co.krungthaiaxa.elife.api.exception.ExceptionUtils.notNull;

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

        TMCSendingPDFRequest tmcSendingPDFRequest = new TMCSendingPDFRequest();
        tmcSendingPDFRequest.setContent(pdfBase64Encoded);
        tmcSendingPDFRequest.setCustomerName(
                policy.getInsureds().get(0).getPerson().getTitle() + " " +
                policy.getInsureds().get(0).getPerson().getGivenName() + " " +
                policy.getInsureds().get(0).getPerson().getSurName());
        tmcSendingPDFRequest.setCustomerTel(policy.getInsureds().get(0).getPerson().getMobilePhoneNumber().getNumber());
        tmcSendingPDFRequest.setDocumentType(documentType.name());
        tmcSendingPDFRequest.setIdCard(policy.getInsureds().get(0).getPerson().getRegistrations().get(0).getId());
        tmcSendingPDFRequest.setMimeType("application/pdf");
        tmcSendingPDFRequest.setPolicyNumber(policy.getPolicyId());

        ReceiveDataJSON request = new ReceiveDataJSON();
        request.setStringJSON(new String(JsonUtil.getJson(tmcSendingPDFRequest), Charset.forName("UTF-8")));

        logger.info("Sending document [" + documentType.name() + "] for policy [" + policy.getPolicyId() + "].");
        ReceiveDataJSONResponse response = (ReceiveDataJSONResponse) getWebServiceTemplate().marshalSendAndReceive(tmcWebServiceUrl, request, null);
        TMCSendingPDFResponse tmcSendingPDFResponse = null;
        try {
            tmcSendingPDFResponse = JsonUtil.mapper.readValue(response.getReceiveDataJSONResult(), TMCSendingPDFResponse.class);
        } catch (IOException e) {
            throw new ElifeException("There was an error while sending document to TMC. Error message is [" + e.getMessage() + "].", e);
        }

        if (tmcSendingPDFResponse.getRemark().getMessage().equalsIgnoreCase("Success")) {
            logger.info("Document sent successfully.");
        } else {
            throw new ElifeException("TMC returned an error message when sending document. Message is [" + tmcSendingPDFResponse.getRemark().getMessage() + "].");
        }
    }

    private class TMCSendingPDFRequest {
        private String content;
        private String mimeType;
        private String policyNumber;
        private String documentType;
        private String customerName;
        private String customerTel;
        private String idCard;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getMimeType() {
            return mimeType;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }

        public String getPolicyNumber() {
            return policyNumber;
        }

        public void setPolicyNumber(String policyNumber) {
            this.policyNumber = policyNumber;
        }

        public String getDocumentType() {
            return documentType;
        }

        public void setDocumentType(String documentType) {
            this.documentType = documentType;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getCustomerTel() {
            return customerTel;
        }

        public void setCustomerTel(String customerTel) {
            this.customerTel = customerTel;
        }

        public String getIdCard() {
            return idCard;
        }

        public void setIdCard(String idCard) {
            this.idCard = idCard;
        }
    }

    private class TMCSendingPDFResponse {
        private String status;
        private TMCSendingPDFResponseRemark remark;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public TMCSendingPDFResponseRemark getRemark() {
            return remark;
        }

        public void setRemark(TMCSendingPDFResponseRemark remark) {
            this.remark = remark;
        }
    }

    private class TMCSendingPDFResponseRemark {
        private String policyNo;
        private String message;

        public String getPolicyNo() {
            return policyNo;
        }

        public void setPolicyNo(String policyNo) {
            this.policyNo = policyNo;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
