package th.co.krungthaiaxa.api.elife.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@ApiModel(description = "Data concerning policy documents content")
public class DocumentDownload {
    @Id
    private String id;
    @Indexed
    private String documentId;
    private String name;
    private String mimeType;
    private String content;

    @ApiModelProperty(value = "The document conent id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ApiModelProperty(value = "The document Id")
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    @ApiModelProperty(value = "The document name, without extension")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ApiModelProperty(value = "The internet media type of the document")
    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @ApiModelProperty(value = "The document content encoded in Base64")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
