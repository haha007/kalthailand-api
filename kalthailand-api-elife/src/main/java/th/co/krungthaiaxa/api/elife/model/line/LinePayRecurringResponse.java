package th.co.krungthaiaxa.api.elife.model.line;

public class LinePayRecurringResponse extends BaseLineResponse {
    private LinePayRecurringResponseInfo info = new LinePayRecurringResponseInfo();

    public LinePayRecurringResponseInfo getInfo() {
        return info;
    }

    public void setInfo(LinePayRecurringResponseInfo info) {
        this.info = info;
    }

}
