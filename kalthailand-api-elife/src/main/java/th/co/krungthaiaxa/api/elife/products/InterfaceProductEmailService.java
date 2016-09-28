package th.co.krungthaiaxa.api.elife.products;

import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Quote;

public interface InterfaceProductEmailService {
	
	void sendQuoteEmail(Quote quote);	
	void sendEreceiptEmail(Policy policy);
	void sendPolicyBookedEmail(Policy policy);
	void sendWrongPhoneNumberEmail(Policy policy);
	void sendUserNotResponseEmail(Policy policy);

}
