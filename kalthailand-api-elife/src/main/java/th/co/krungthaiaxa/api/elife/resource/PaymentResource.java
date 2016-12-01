package th.co.krungthaiaxa.api.elife.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import th.co.krungthaiaxa.api.common.model.error.Error;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.PaymentNewerCompletedResult;
import th.co.krungthaiaxa.api.elife.service.PaymentService;

import javax.inject.Inject;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@Api(value = "Payments")
public class PaymentResource {
    public final static Logger LOGGER = LoggerFactory.getLogger(PaymentResource.class);
    private final PaymentService paymentService;

    @Value("${environment.name}")
    private String environmentName;
    @Value("${kal.api.auth.header}")
    private String tokenHeader;

    @Inject
    public PaymentResource(PaymentService paymentService) {this.paymentService = paymentService;}

    @ApiOperation(value = "Get payment detail", notes = "Get the detail of payment. If not found, return null.", response = Payment.class)
    @RequestMapping(value = "/payments/{paymentId}", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    @ApiResponses({
            @ApiResponse(code = 500, message = "If there's an unexpected error", response = Error.class)
    })
    public Payment getPayment(@ApiParam(value = "The payment ID", required = true)
    @PathVariable String paymentId) {
        return paymentService.findPaymentById(paymentId);
    }

    @ApiOperation(value = "Find whether is there any newer completed payment or not.",
            notes = "Find whether is there any newer completed payment or not. If there's newer completed payment, you may don't need to process the retry payment again.")
    @RequestMapping(value = "/payments/{paymentId}/newer-completed", produces = APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody
    @ApiResponses({
            @ApiResponse(code = 500, message = "If there's an unexpected error", response = Error.class)
    })
    public PaymentNewerCompletedResult checkNewerCompletedPaymentSinceInputPayment(@ApiParam(value = "The payment ID", required = true)
    @PathVariable String paymentId) {
        //TODO when checking retry payment, maybe Collection process was handled failed payment. So in that case, if the client click retry payment again, they will be charged one more time.
        return paymentService.findCompletedRetryPaymentInSamePolicy(paymentId);
    }

}
