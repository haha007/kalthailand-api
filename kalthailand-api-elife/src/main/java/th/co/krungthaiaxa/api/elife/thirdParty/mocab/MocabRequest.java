package th.co.krungthaiaxa.api.elife.thirdParty.mocab;

import th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus;

/**
 * Created by tuong.le on 3/6/17.
 */
public class MocabRequest {

    private String content;
    private String productType;
    private String mimeType;
    private String policyNumber;
    private String documentType;
    private String customerName;
    private String customerTel;
    private String idCard;
    private PolicyStatus policyStatus;

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(final String productType) {
        this.productType = productType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(final String mimeType) {
        this.mimeType = mimeType;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(final String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(final String documentType) {
        this.documentType = documentType;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(final String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerTel() {
        return customerTel;
    }

    public void setCustomerTel(final String customerTel) {
        this.customerTel = customerTel;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(final String idCard) {
        this.idCard = idCard;
    }

    public PolicyStatus getPolicyStatus() {
        return policyStatus;
    }

    public void setPolicyStatus(final PolicyStatus policyStatus) {
        this.policyStatus = policyStatus;
    }
}
