
package org.tempuri;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ReceiveDataJSON_IMGResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "receiveDataJSONIMGResult"
})
@XmlRootElement(name = "ReceiveDataJSON_IMGResponse")
public class ReceiveDataJSONIMGResponse {

    @XmlElement(name = "ReceiveDataJSON_IMGResult")
    protected String receiveDataJSONIMGResult;

    /**
     * Gets the value of the receiveDataJSONIMGResult property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReceiveDataJSONIMGResult() {
        return receiveDataJSONIMGResult;
    }

    /**
     * Sets the value of the receiveDataJSONIMGResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReceiveDataJSONIMGResult(String value) {
        this.receiveDataJSONIMGResult = value;
    }

}
