package th.co.krungthaiaxa.api.elife.data;

import java.time.LocalDate;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class LineToken {
	
	private Integer rowId;
	private String accessToken;
	private String refreshToken;
	private String expireDate;		
	
	public Integer getRowId() {
		return rowId;
	}
	public void setRowId(Integer rowId) {
		this.rowId = rowId;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	public String getExpireDate() {
		return expireDate;
	}
	public void setExpireDate(String expireDate) {
		this.expireDate = expireDate;
	}
	@Override
	public String toString() {
		return "LineToken [accessToken=" + accessToken + ", refreshToken=" + refreshToken + ", expireDate=" + expireDate
				+ "]";
	}

}
