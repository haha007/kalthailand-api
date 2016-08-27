package th.co.krungthaiaxa.api.elife.mock;

import org.mockito.exceptions.misusing.MockitoConfigurationException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import th.co.krungthaiaxa.api.elife.model.line.LinePayRecurringResponse;
import th.co.krungthaiaxa.api.elife.model.line.LinePayRecurringResponseInfo;
import th.co.krungthaiaxa.api.elife.service.LineService;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author khoi.tran on 8/27/16.
 */
public class LineServiceMockFactory {
    public static LineService createDefault() {
        LineService lineService = mock(LineService.class);
        try {
            when(lineService.preApproved(anyString(), anyDouble(), anyString(), anyString(), anyString())).thenAnswer(new Answer<LinePayRecurringResponse>() {

                @Override
                public LinePayRecurringResponse answer(InvocationOnMock invocationOnMock) throws Throwable {
                    //                Object[] arguments = invocationOnMock.getArguments();
                    //                String regKey = (String) arguments[0];
                    //                Double amount = (Double) arguments[1];
                    //                String currency = (String) arguments[2];
                    //                String productName = (String) arguments[3];
                    //                String orderId = (String) arguments[4];

                    LinePayRecurringResponse result = new LinePayRecurringResponse();
                    LinePayRecurringResponseInfo linePayRecurringResponseInfo = new LinePayRecurringResponseInfo();
                    linePayRecurringResponseInfo.setTransactionId("MockTransId_" + System.currentTimeMillis());
                    linePayRecurringResponseInfo.setTransactionDate(LocalDateTime.now().toString());

                    result.setReturnCode(LineService.PREAPPROVE_CODE_SUCCESS);
                    result.setReturnMessage("Mocktest return success");
                    result.setInfo(linePayRecurringResponseInfo);
                    return result;
                }
            });
        } catch (IOException e) {
            throw new MockitoConfigurationException("Cannot create mock", e);
        }
        return lineService;
    }
}
