package th.co.krungthaiaxa.api.elife.tmc;

public class TMCSendingPDFResponse {
    private String status;
    private TMCSendingPDFResponseRemark remark;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public TMCSendingPDFResponseRemark getRemark() {
        return remark;
    }

    public void setRemark(TMCSendingPDFResponseRemark remark) {
        this.remark = remark;
    }
}