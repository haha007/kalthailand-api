package th.co.krungthaiaxa.api.elife.factory;

import org.mockito.exceptions.misusing.MockitoConfigurationException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import th.co.krungthaiaxa.api.elife.model.line.LinePayRecurringResponse;
import th.co.krungthaiaxa.api.elife.model.line.LinePayRecurringResponseInfo;
import th.co.krungthaiaxa.api.elife.model.line.LinePayResponse;
import th.co.krungthaiaxa.api.elife.line.LineService;

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
    public static LineService initServiceDefault() {
        return initServiceWithResponseCode(LineService.RESPONSE_CODE_SUCCESS);
    }

    /**
     * @param responseCode view more in {@link LineService#RESPONSE_CODE_SUCCESS}
     * @return
     */
    public static LineService initServiceWithResponseCode(String responseCode) {
        LineService lineService = mock(LineService.class);
        try {
            preApproveWithResponseCode(lineService, responseCode);
            capturePayment(lineService, responseCode);
        } catch (IOException e) {
            throw new MockitoConfigurationException("Cannot create mock", e);
        }
        return lineService;
    }

    private static void preApproveWithResponseCode(LineService lineService, String responseCode) throws IOException {
        when(lineService.preApproved(anyString(), anyDouble(), anyString(), anyString(), anyString())).thenAnswer(new Answer<LinePayRecurringResponse>() {

            @Override
            public LinePayRecurringResponse answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] arguments = invocationOnMock.getArguments();
                String regKey = (String) arguments[0];
                Double amount = (Double) arguments[1];
                String currency = (String) arguments[2];
                String productName = (String) arguments[3];
                String orderId = (String) arguments[4];

                LinePayRecurringResponse result = new LinePayRecurringResponse();
                LinePayRecurringResponseInfo linePayRecurringResponseInfo = new LinePayRecurringResponseInfo();
                linePayRecurringResponseInfo.setTransactionId("MockTransId_" + System.currentTimeMillis());
                linePayRecurringResponseInfo.setTransactionDate(LocalDateTime.now().toString());
                result.setReturnCode(responseCode);
                result.setReturnMessage("Mocktest response");
                result.setInfo(linePayRecurringResponseInfo);
                return result;
            }
        });
    }

    private static void capturePayment(LineService lineService, String responseCode) throws IOException {
        when(lineService.capturePayment(anyString(), anyDouble(), anyString())).thenAnswer(new Answer<LinePayResponse>() {

            @Override
            public LinePayResponse answer(InvocationOnMock invocationOnMock) throws Throwable {
                LinePayResponse linePayResponse = new LinePayResponse();
                LinePayResponse result = new LinePayResponse();
                LinePayRecurringResponseInfo linePayRecurringResponseInfo = new LinePayRecurringResponseInfo();
                linePayRecurringResponseInfo.setTransactionId("MockTransId_" + System.currentTimeMillis());
                linePayRecurringResponseInfo.setTransactionDate(LocalDateTime.now().toString());
                result.setReturnCode(responseCode);
                result.setReturnMessage("Mocktest response");
                return result;
            }
        });
    }
}
