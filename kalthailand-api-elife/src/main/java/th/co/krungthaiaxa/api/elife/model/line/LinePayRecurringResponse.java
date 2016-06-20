package th.co.krungthaiaxa.api.elife.model.line;

public class LinePayRecurringResponse {
	private String returnCode;
    private String returnMessage;
    private LinePayRecurringResponseInfo info = new LinePayRecurringResponseInfo();
	public String getReturnCode() {
		return returnCode;
	}
	public void setReturnCode(String returnCode) {
		this.returnCode = returnCode;
	}
	public String getReturnMessage() {
		return returnMessage;
	}
	public void setReturnMessage(String returnMessage) {
		this.returnMessage = returnMessage;
	}
	public LinePayRecurringResponseInfo getInfo() {
		return info;
	}
	public void setInfo(LinePayRecurringResponseInfo info) {
		this.info = info;
	}
    
    
}
