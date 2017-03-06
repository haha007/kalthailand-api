package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;
import th.co.krungthaiaxa.api.elife.model.enums.DocumentType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * This class is actually contains only metadata, the binary data of file is stored in {@link DocumentDownload}.
 */
@org.springframework.data.mongodb.core.mapping.Document(collection = "document")
@ApiModel(description = "Data concerning policy documents saved on server side")
public class Document implements Serializable {
    @Id
    private String id;
    private String policyId;
    private DocumentType typeName;
    private LocalDateTime creationDate;
    private String mocabStatus = "000";
    /**
     * The object which relates to this document.
     * That object can be a payment, a policy, or a quote... (view more in {@link DocumentReferenceType}.
     * For example:
     * With a document with type {@link DocumentType#ERECEIPT_IMAGE}, it's related to a {@link DocumentReferenceType#PAYMENT} because each payment must have an ERECEIPT_IMAGE
     */
    private String referenceId;
    private DocumentReferenceType referenceType;

    @ApiModelProperty(value = "Identifier of the document")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ApiModelProperty(value = "The identifier of the policy associated to the document")
    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    @ApiModelProperty(value = "Localized type of the document")
    public DocumentType getTypeName() {
        return typeName;
    }

    public void setTypeName(DocumentType typeName) {
        this.typeName = typeName;
    }

    @ApiModelProperty(value = "Creation date of the document on server side")
    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getMocabStatus() {
        return mocabStatus;
    }

    public void setMocabStatus(final String mocabStatus) {
        this.mocabStatus = mocabStatus;
    }

    @ApiModelProperty(value = "Indicate document is sent to MOCAB or not")


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(id, document.id) &&
                Objects.equals(policyId, document.policyId) &&
                Objects.equals(typeName, document.typeName) &&
                Objects.equals(referenceId, document.referenceId) &&
                Objects.equals(referenceType, document.referenceType) &&
                Objects.equals(creationDate, document.creationDate) &&
                Objects.equals(mocabStatus, document.mocabStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, policyId, typeName, creationDate, mocabStatus);
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public DocumentReferenceType getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(DocumentReferenceType referenceType) {
        this.referenceType = referenceType;
    }
}
