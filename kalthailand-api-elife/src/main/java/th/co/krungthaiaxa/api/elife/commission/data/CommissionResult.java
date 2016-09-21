package th.co.krungthaiaxa.api.elife.commission.data;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import th.co.krungthaiaxa.api.common.data.BaseEntity;

@Document(collection = "commissionResult")
public class CommissionResult extends BaseEntity {
	
	private String rowId;
	private String commissionMonth;
	private List<CommissionCalculation> policies;
	private Integer commissionPoliciesCount;
	
	
	public String getRowId() {
		return rowId;
	}
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}
	public String getCommissionMonth() {
		return commissionMonth;
	}
	public void setCommissionMonth(String commissionMonth) {
		this.commissionMonth = commissionMonth;
	}
	public List<CommissionCalculation> getPolicies() {
		return policies;
	}
	public void setPolicies(List<CommissionCalculation> policies) {
		this.policies = policies;
	}
	public Integer getCommissionPoliciesCount() {
		return commissionPoliciesCount;
	}
	public void setCommissionPoliciesCount(Integer commissionPoliciesCount) {
		this.commissionPoliciesCount = commissionPoliciesCount;
	}
	
	
	

}
