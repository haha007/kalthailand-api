package th.co.krungthaiaxa.api.elife.data;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class PolicyQuota {
	
	private Integer rowId;
	private Integer percent;
	private List<String> emailList;
	public Integer getRowId() {
		return rowId;
	}
	public void setRowId(Integer rowId) {
		this.rowId = rowId;
	}
	public Integer getPercent() {
		return percent;
	}
	public void setPercent(Integer percent) {
		this.percent = percent;
	}
	public List<String> getEmailList() {
		return emailList;
	}
	public void setEmailList(List<String> emailList) {
		this.emailList = emailList;
	}
	@Override
	public String toString() {
		return "PolicyQuota [rowId=" + rowId + ", percent=" + percent + ", emailList=" + emailList + "]";
	}
	
}
