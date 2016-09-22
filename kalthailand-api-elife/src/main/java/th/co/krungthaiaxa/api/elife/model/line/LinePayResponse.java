package th.co.krungthaiaxa.api.elife.model.line;

public class LinePayResponse extends BaseLineResponse {
    private LinePayResponseInfo info = new LinePayResponseInfo();

    public LinePayResponseInfo getInfo() {
        return info;
    }

    public void setInfo(LinePayResponseInfo info) {
        this.info = info;
    }
}
