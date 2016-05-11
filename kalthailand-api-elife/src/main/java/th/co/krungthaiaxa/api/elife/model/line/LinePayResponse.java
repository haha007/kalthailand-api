package th.co.krungthaiaxa.api.elife.model.line;

public class LinePayResponse {
    private String returnCode;
    private String returnMessage;
    private LinePayResponseInfo info = new LinePayResponseInfo();

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

    public LinePayResponseInfo getInfo() {
        return info;
    }

    public void setInfo(LinePayResponseInfo info) {
        this.info = info;
    }
}
