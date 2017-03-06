package th.co.krungthaiaxa.api.elife.thirdParty.mocab;

import th.co.krungthaiaxa.api.elife.model.enums.PolicyStatus;

import java.io.Serializable;

/**
 * Created by tuong.le on 3/6/17.
 */
public class MocabRequest implements Serializable {

    private String content;
    private String productType;
    private String mimeType;
    private String policyNumber;
    private String documentType;
    private String customerName;
    private String customerTel;
    private String idCard;
    private PolicyStatus policyStatus;


    public MocabRequest() {
        
    }

    public MocabRequest(final String content, final String productType, final String mimeType, final String policyNumber, final String documentType, final String customerName, final String customerTel, final String idCard, final PolicyStatus policyStatus) {
        this.content = content;
        this.productType = productType;
        this.mimeType = mimeType;
        this.policyNumber = policyNumber;
        this.documentType = documentType;
        this.customerName = customerName;
        this.customerTel = customerTel;
        this.idCard = idCard;
        this.policyStatus = policyStatus;
    }

    public MocabRequest setContent(final String content) {
        this.content = content;
        return this;
    }

    public MocabRequest setProductType(final String productType) {
        this.productType = productType;
        return this;
    }

    public MocabRequest setMimeType(final String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public MocabRequest setPolicyNumber(final String policyNumber) {
        this.policyNumber = policyNumber;
        return this;
    }

    public MocabRequest setDocumentType(final String documentType) {
        this.documentType = documentType;
        return this;
    }

    public MocabRequest setCustomerName(final String customerName) {
        this.customerName = customerName;
        return this;
    }

    public MocabRequest setCustomerTel(final String customerTel) {
        this.customerTel = customerTel;
        return this;
    }

    public MocabRequest setIdCard(final String idCard) {
        this.idCard = idCard;
        return this;
    }

    public MocabRequest setPolicyStatus(final PolicyStatus policyStatus) {
        this.policyStatus = policyStatus;
        return this;
    }

    public MocabRequest builder() {
        return new MocabRequest(content, productType, mimeType, policyNumber, documentType, 
                customerName, customerTel, idCard, policyStatus);
    }
}
